package com.lain.modules.xianyu.live;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lain.common.exception.LainException;
import com.lain.modules.xianyu.agent.XianyuReplyBot;
import com.lain.modules.xianyu.api.XianyuApis;
import com.lain.modules.xianyu.config.XianyuProperties;
import com.lain.modules.xianyu.service.ChatContextManager;
import com.lain.modules.xianyu.utils.XianyuUtils;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 闲鱼 WebSocket 常驻值守客户端
 * <p>
 * 职责：建立并维持 WebSocket 长连接、心跳保活、Token 周期刷新、消息接收/解密/AI 回复，
 * 支持人工接管模式切换与模拟人工输入延迟。
 */
@Component
public class XianyuLive {

    private static final Logger log = LoggerFactory.getLogger(XianyuLive.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** WebSocket 服务地址 */
    private static final String BASE_URL = "wss://wss-goofish.dingtalk.com/";

    /** 连接注册消息中的固定配置 */
    private static final String REG_APP_KEY = "444e9908a51d1cb236a27862abc769c9";
    private static final String REG_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 DingTalk(2.1.5) OS(Windows/10) Browser(Chrome/133.0.0.0) DingWeb/2.1.5 IMPaaS DingWeb/2.1.5";

    private final XianyuApis xianyuApis;
    private final ChatContextManager contextManager;
    private final XianyuReplyBot bot;
    private final XianyuProperties properties;

    /** 当前 WebSocket 连接 */
    private volatile WebSocket ws;

    /** 连接关闭信号（每次连接重新创建） */
    private volatile CountDownLatch closeLatch = new CountDownLatch(1);

    /** 运行开关 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** 连接线程 */
    private volatile Thread connectThread;

    /** 连接重启标志（Token 刷新后触发） */
    private volatile boolean connectionRestartFlag = false;

    /** 致命错误标志（风控/Cookie 失效，置位后终止重连循环） */
    private volatile boolean fatalError = false;

    /** 卖家自身用户 ID */
    private volatile String myId;

    /** 设备 ID */
    private volatile String deviceId;

    /** 当前 Token */
    private volatile String currentToken;

    /** 心跳相关状态 */
    private volatile long lastHeartbeatTime = 0;
    private volatile long lastHeartbeatResponse = 0;

    /** Token 刷新相关状态 */
    private volatile long lastTokenRefreshTime = 0;

    /** 人工接管模式会话集合 */
    private final Set<String> manualModeConversations = ConcurrentHashMap.newKeySet();

    /** 进入人工模式的时间戳 */
    private final Map<String, Long> manualModeTimestamps = new ConcurrentHashMap<>();

    /** 心跳/Token 刷新定时任务 */
    private volatile ScheduledExecutorService heartbeatExecutor;
    private volatile ScheduledExecutorService tokenRefreshExecutor;

    public XianyuLive(XianyuApis xianyuApis, ChatContextManager contextManager,
                      XianyuReplyBot bot, XianyuProperties properties) {
        this.xianyuApis = xianyuApis;
        this.contextManager = contextManager;
        this.bot = bot;
        this.properties = properties;
    }

    /**
     * 启动机器人（独立守护线程运行连接循环）
     */
    public void start() {
        if (!running.compareAndSet(false, true)) {
            log.warn("闲鱼机器人已在运行中");
            return;
        }
        String cookiesStr = properties.getCookiesStr();
        if (cookiesStr == null || cookiesStr.isBlank()) {
            log.error("闲鱼机器人启动失败：未配置 COOKIES_STR");
            running.set(false);
            return;
        }
        xianyuApis.init(cookiesStr);
        Map<String, String> cookies = XianyuUtils.transCookies(cookiesStr);
        myId = cookies.getOrDefault("unb", "");
        deviceId = XianyuUtils.generateDeviceId(myId);
        fatalError = false;
        log.info("闲鱼机器人启动，设备ID: {}, 用户ID: {}", deviceId, myId);

        connectThread = new Thread(this::connectLoop, "xianyu-live");
        connectThread.setDaemon(true);
        connectThread.start();
    }

    /**
     * 停止机器人（应用关闭时调用）
     */
    @PreDestroy
    public void stop() {
        log.info("闲鱼机器人停止中...");
        running.set(false);
        forceClose("shutdown");
        if (connectThread != null) {
            connectThread.interrupt();
        }
    }

    /**
     * 连接循环：断线自动重连，Token 刷新后立即重连
     */
    private void connectLoop() {
        while (running.get() && !fatalError) {
            try {
                connectionRestartFlag = false;
                connectOnce();
                if (!running.get()) {
                    break;
                }
                if (connectionRestartFlag) {
                    log.info("主动重启连接，立即重连...");
                } else {
                    log.info("等待5秒后重连...");
                    Thread.sleep(5000);
                }
            } catch (LainException e) {
                log.error("闲鱼机器人异常终止: {}", e.getMessage());
                running.set(false);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("连接发生错误: {}", e.getMessage());
                if (!running.get()) {
                    break;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.info("闲鱼机器人已停止");
    }

    /**
     * 建立单次连接并阻塞至连接关闭（含注册、心跳、Token 刷新任务）
     */
    private void connectOnce() throws Exception {
        closeLatch = new CountDownLatch(1);

        HttpClient client = HttpClient.newHttpClient();
        CompletableFuture<WebSocket> future = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .header("Cookie", properties.getCookiesStr())
                // Host/Connection 为 JDK 受限头，WebSocket 握手时由 HttpClient 自动生成（来自 URI），不可手动设置
                .header("Pragma", "no-cache")
                .header("Cache-Control", "no-cache")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36")
                .header("Origin", "https://www.goofish.com")
                .header("Accept-Encoding", "gzip, deflate, br, zstd")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .buildAsync(URI.create(BASE_URL), new LiveListener());

        ws = future.get(20, TimeUnit.SECONDS);

        // 初始化心跳时间
        lastHeartbeatTime = System.currentTimeMillis();
        lastHeartbeatResponse = System.currentTimeMillis();

        // 启动心跳与 Token 刷新任务
        startHeartbeatLoop();
        startTokenRefreshLoop();

        // 阻塞等待连接关闭
        closeLatch.await();

        // 清理定时任务
        stopHeartbeatLoop();
        stopTokenRefreshLoop();
        log.info("连接已结束，准备重连");
    }

    // ==================== 连接初始化 ====================

    /**
     * 连接注册
     */
    private void init(WebSocket webSocket) {
        // 如果没有 token 或者 token 过期，获取新 token
        if (currentToken == null
                || System.currentTimeMillis() - lastTokenRefreshTime >= properties.getTokenRefreshInterval() * 1000L) {
            log.info("获取初始token...");
            refreshToken();
        }

        if (currentToken == null) {
            throw new IllegalStateException("Token获取失败");
        }

        ObjectNode msg = MAPPER.createObjectNode();
        msg.put("lwp", "/reg");
        ObjectNode headers = msg.putObject("headers");
        headers.put("cache-header", "app-key token ua wv");
        headers.put("app-key", REG_APP_KEY);
        headers.put("token", currentToken);
        headers.put("ua", REG_UA);
        headers.put("dt", "j");
        headers.put("wv", "im:3,au:3,sy:6");
        headers.put("sync", "0,0;0;0;");
        headers.put("did", deviceId);
        headers.put("mid", XianyuUtils.generateMid());
        sendText(msg.toString());

        // 等待一段时间，确保连接注册完成
        sleep(1000);

        ObjectNode ackDiff = MAPPER.createObjectNode();
        ackDiff.put("lwp", "/r/SyncStatus/ackDiff");
        ackDiff.putObject("headers").put("mid", "5701741704675979 0");
        ArrayNode body = ackDiff.putArray("body");
        ObjectNode sync = body.addObject();
        sync.put("pipeline", "sync");
        sync.put("tooLong2Tag", "PNM,1");
        sync.put("channel", "sync");
        sync.put("topic", "sync");
        sync.put("highPts", 0);
        sync.put("pts", System.currentTimeMillis() * 1000L);
        sync.put("seq", 0);
        sync.put("timestamp", System.currentTimeMillis());
        sendText(ackDiff.toString());
        log.info("连接注册完成");
    }

    /**
     * 刷新 Token
     */
    public String refreshToken() {
        try {
            log.info("开始刷新token...");
            JsonNode tokenResult = xianyuApis.getToken(deviceId);
            JsonNode data = tokenResult.path("data");
            if (data.has("accessToken")) {
                String newToken = data.path("accessToken").asText();
                currentToken = newToken;
                lastTokenRefreshTime = System.currentTimeMillis();
                log.info("Token刷新成功");
                return newToken;
            } else {
                log.error("Token刷新失败: {}", tokenResult);
                return null;
            }
        } catch (LainException e) {
            // 风控/Cookie 失效为致命错误：终止机器人，避免每5秒重连高频请求加重风控
            log.error("Token刷新致命错误: {}", e.getMessage());
            fatalError = true;
            forceClose("fatal error");
            return null;
        } catch (Exception e) {
            log.error("Token刷新异常: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 定时任务 ====================

    /**
     * 心跳维护循环
     */
    private void startHeartbeatLoop() {
        heartbeatExecutor = newDaemonScheduler("xianyu-heartbeat");
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                long now = System.currentTimeMillis();
                long interval = properties.getHeartbeatInterval() * 1000L;

                // 检查是否需要发送心跳
                if (now - lastHeartbeatTime >= interval) {
                    sendHeartbeat();
                }

                // 检查上次心跳响应时间，如果超时则认为连接已断开
                if (now - lastHeartbeatResponse > interval + properties.getHeartbeatTimeout() * 1000L) {
                    log.warn("心跳响应超时，可能连接已断开");
                    forceClose("heartbeat timeout");
                }
            } catch (Exception e) {
                log.error("心跳循环出错: {}", e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Token 刷新循环
     */
    private void startTokenRefreshLoop() {
        tokenRefreshExecutor = newDaemonScheduler("xianyu-token-refresh");
        tokenRefreshExecutor.scheduleAtFixedRate(() -> {
            try {
                long now = System.currentTimeMillis();
                long interval = properties.getTokenRefreshInterval() * 1000L;

                if (now - lastTokenRefreshTime >= interval) {
                    log.info("Token即将过期，准备刷新...");
                    String newToken = refreshToken();
                    if (newToken != null) {
                        log.info("Token刷新成功，准备重新建立连接...");
                        connectionRestartFlag = true;
                        forceClose("token refreshed");
                    } else {
                        log.error("Token刷新失败，将在{}分钟后重试", properties.getTokenRetryInterval() / 60);
                        // 将下次检查时间推迟到重试间隔之后
                        lastTokenRefreshTime = now - interval + properties.getTokenRetryInterval() * 1000L;
                    }
                }
            } catch (Exception e) {
                log.error("Token刷新循环出错: {}", e.getMessage());
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    private void stopHeartbeatLoop() {
        shutdownScheduler(heartbeatExecutor);
        heartbeatExecutor = null;
    }

    private void stopTokenRefreshLoop() {
        shutdownScheduler(tokenRefreshExecutor);
        tokenRefreshExecutor = null;
    }

    private static ScheduledExecutorService newDaemonScheduler(String name) {
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        };
        return Executors.newSingleThreadScheduledExecutor(factory);
    }

    private static void shutdownScheduler(ScheduledExecutorService executor) {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    /**
     * 发送心跳包
     */
    private void sendHeartbeat() {
        ObjectNode msg = MAPPER.createObjectNode();
        msg.put("lwp", "/!");
        msg.putObject("headers").put("mid", XianyuUtils.generateMid());
        sendText(msg.toString());
        lastHeartbeatTime = System.currentTimeMillis();
        log.debug("心跳包已发送");
    }

    /**
     * 处理心跳响应
     */
    private boolean handleHeartbeatResponse(JsonNode messageData) {
        try {
            if (messageData.has("headers") && messageData.path("headers").has("mid")
                    && messageData.has("code") && messageData.path("code").asInt() == 200) {
                lastHeartbeatResponse = System.currentTimeMillis();
                log.debug("收到心跳响应");
                return true;
            }
        } catch (Exception e) {
            log.error("处理心跳响应出错: {}", e.getMessage());
        }
        return false;
    }

    // ==================== 消息处理 ====================

    /**
     * 处理所有类型的消息
     */
    private void handleMessage(JsonNode messageData, WebSocket webSocket) {
        try {
            // 发送通用 ACK 响应
            try {
                ObjectNode ack = MAPPER.createObjectNode();
                ack.put("code", 200);
                ObjectNode ackHeaders = ack.putObject("headers");
                JsonNode headers = messageData.path("headers");
                ackHeaders.put("mid", headers.has("mid") ? headers.path("mid").asText() : XianyuUtils.generateMid());
                ackHeaders.put("sid", headers.has("sid") ? headers.path("sid").asText() : "");
                if (headers.has("app-key")) {
                    ackHeaders.put("app-key", headers.path("app-key").asText());
                }
                if (headers.has("ua")) {
                    ackHeaders.put("ua", headers.path("ua").asText());
                }
                if (headers.has("dt")) {
                    ackHeaders.put("dt", headers.path("dt").asText());
                }
                sendText(ack.toString());
            } catch (Exception e) {
                // ACK 发送失败不影响主流程
            }

            // 如果不是同步包消息，直接返回
            if (!isSyncPackage(messageData)) {
                return;
            }

            // 获取并解密数据
            JsonNode syncData = messageData.path("body").path("syncPushPackage").path("data").path(0);
            if (!syncData.has("data")) {
                log.debug("同步包中无data字段");
                return;
            }

            // 解密数据
            JsonNode message;
            try {
                String data = syncData.path("data").asText();
                try {
                    // 无需解密的明文消息，直接返回
                    String plain = new String(Base64.getDecoder().decode(data), java.nio.charset.StandardCharsets.UTF_8);
                    MAPPER.readTree(plain);
                    return;
                } catch (Exception ignored) {
                    // 加密数据，走解密流程
                }
                String decryptedData = XianyuUtils.decrypt(data);
                message = MAPPER.readTree(decryptedData);
            } catch (Exception e) {
                log.error("消息解密失败: {}", e.getMessage());
                return;
            }

            // 判断是否为订单消息（付款/交易状态变化，仅记录日志）
            try {
                String redReminder = message.path("3").path("redReminder").asText();
                if ("等待买家付款".equals(redReminder)) {
                    log.info("等待买家付款: {}", userUrl(message));
                    return;
                } else if ("交易关闭".equals(redReminder)) {
                    log.info("买家交易关闭: {}", userUrl(message));
                    return;
                } else if ("等待卖家发货".equals(redReminder)) {
                    log.info("交易成功等待卖家发货: {}", userUrl(message));
                    return;
                }
            } catch (Exception ignored) {
                // 非订单消息
            }

            // 判断消息类型
            if (isTypingStatus(message)) {
                log.debug("用户正在输入");
                return;
            } else if (!isChatMessage(message)) {
                log.debug("其他非聊天消息: {}", message);
                return;
            }

            // 处理聊天消息
            long createTime = message.path("1").path("5").asLong();
            String sendUserName = message.path("1").path("10").path("reminderTitle").asText();
            String sendUserId = message.path("1").path("10").path("senderUserId").asText();
            String sendMessage = message.path("1").path("10").path("reminderContent").asText();

            // 时效性验证（过滤过期消息）
            if (System.currentTimeMillis() - createTime > properties.getMessageExpireTime()) {
                log.debug("过期消息丢弃");
                return;
            }

            // 获取商品 ID 和会话 ID
            String urlInfo = message.path("1").path("10").path("reminderUrl").asText();
            String itemId = extractItemId(urlInfo);
            String chatId = message.path("1").path("2").asText().split("@")[0];

            if (itemId == null) {
                log.warn("无法获取商品ID");
                return;
            }

            // 检查是否为卖家（自己）发送的控制命令
            if (sendUserId.equals(myId)) {
                log.debug("检测到卖家消息，检查是否为控制命令");
                if (checkToggleKeywords(sendMessage)) {
                    String mode = toggleManualMode(chatId);
                    if ("manual".equals(mode)) {
                        log.info("🔴 已接管会话 {} (商品: {})", chatId, itemId);
                    } else {
                        log.info("🟢 已恢复会话 {} 的自动回复 (商品: {})", chatId, itemId);
                    }
                    return;
                }
                // 记录卖家人工回复
                contextManager.addMessageByChat(chatId, myId, itemId, "assistant", sendMessage);
                log.info("卖家人工回复 (会话: {}, 商品: {}): {}", chatId, itemId, sendMessage);
                return;
            }

            log.info("用户: {} (ID: {}), 商品: {}, 会话: {}, 消息: {}", sendUserName, sendUserId, itemId, chatId, sendMessage);

            // 如果当前会话处于人工接管模式，不进行自动回复
            if (isManualMode(chatId)) {
                log.info("🔴 会话 {} 处于人工接管模式，跳过自动回复", chatId);
                contextManager.addMessageByChat(chatId, sendUserId, itemId, "user", sendMessage);
                return;
            }

            // 检查是否为带中括号的系统消息
            if (isBracketSystemMessage(sendMessage)) {
                log.info("检测到系统消息：'{}'，跳过自动回复", sendMessage);
                return;
            }
            if (isSystemMessage(message)) {
                log.debug("系统消息，跳过处理");
                return;
            }

            // 从数据库中获取商品信息，如果不存在则从 API 获取并保存
            JsonNode itemInfo = contextManager.getItemInfo(itemId);
            if (itemInfo == null) {
                log.info("从API获取商品信息: {}", itemId);
                JsonNode apiResult = xianyuApis.getItemInfo(itemId);
                if (apiResult.path("data").has("itemDO")) {
                    itemInfo = apiResult.path("data").path("itemDO");
                    contextManager.saveItemInfo(itemId, itemInfo);
                } else {
                    log.warn("获取商品信息失败: {}", apiResult);
                    return;
                }
            } else {
                log.info("从数据库获取商品信息: {}", itemId);
            }

            String itemDescription = "当前商品的信息如下：" + buildItemDescription(itemInfo);

            // 获取完整的对话上下文并生成回复
            List<Map<String, String>> context = contextManager.getContextByChat(chatId);
            String botReply = bot.generateReply(sendMessage, itemDescription, context);

            // 检查是否需要回复
            if ("-".equals(botReply)) {
                log.info("[无需回复] 用户 {} 的消息被识别为无需回复类型", sendUserName);
                return;
            }

            // 添加用户消息到上下文
            contextManager.addMessageByChat(chatId, sendUserId, itemId, "user", sendMessage);

            // 检查是否为价格意图，如果是则增加议价次数
            if ("price".equals(bot.getLastIntent())) {
                contextManager.incrementBargainCountByChat(chatId);
                int bargainCount = contextManager.getBargainCountByChat(chatId);
                log.info("用户 {} 对商品 {} 的议价次数: {}", sendUserName, itemId, bargainCount);
            }

            // 添加机器人回复到上下文
            contextManager.addMessageByChat(chatId, myId, itemId, "assistant", botReply);
            log.info("机器人回复: {}", botReply);

            // 模拟人工输入延迟
            if (properties.isSimulateHumanTyping()) {
                // 基础延迟 0-1 秒 + 每字 0.1-0.3 秒，最大 10 秒
                double baseDelay = RandomUtil.randomDouble(0, 1);
                double typingDelay = botReply.length() * RandomUtil.randomDouble(0.1, 0.3);
                double totalDelay = Math.min(baseDelay + typingDelay, 10.0);
                log.info("模拟人工输入，延迟发送 {} 秒...", String.format("%.2f", totalDelay));
                sleep((long) (totalDelay * 1000));
            }

            sendMsg(webSocket, chatId, sendUserId, botReply);

        } catch (Exception e) {
            log.error("处理消息时发生错误: {}", e.getMessage());
            log.debug("原始消息: {}", messageData);
        }
    }

    private String userUrl(JsonNode message) {
        String userId = message.path("1").asText().split("@")[0];
        return "https://www.goofish.com/personal?userId=" + userId;
    }

    /**
     * 从提醒 URL 中提取商品 ID
     */
    private String extractItemId(String urlInfo) {
        if (urlInfo != null && urlInfo.contains("itemId=")) {
            String after = urlInfo.split("itemId=")[1];
            return after.split("&")[0];
        }
        return null;
    }

    // ==================== 消息判断 ====================

    /**
     * 判断是否为用户聊天消息
     */
    private boolean isChatMessage(JsonNode message) {
        try {
            return message.has("1") && message.get("1").isObject()
                    && message.get("1").has("10") && message.get("1").get("10").isObject()
                    && message.get("1").get("10").has("reminderContent");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否为同步包消息
     */
    private boolean isSyncPackage(JsonNode messageData) {
        try {
            return messageData.has("body")
                    && messageData.path("body").has("syncPushPackage")
                    && messageData.path("body").path("syncPushPackage").has("data")
                    && messageData.path("body").path("syncPushPackage").path("data").size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否为用户正在输入状态消息
     */
    private boolean isTypingStatus(JsonNode message) {
        try {
            return message.has("1") && message.get("1").isArray() && message.get("1").size() > 0
                    && message.get("1").get(0).isObject()
                    && message.get("1").get(0).has("1")
                    && message.get("1").get(0).get("1").isTextual()
                    && message.get("1").get(0).get("1").asText().contains("@goofish");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否为系统消息
     */
    private boolean isSystemMessage(JsonNode message) {
        try {
            return message.has("3") && message.get("3").isObject()
                    && message.get("3").has("needPush")
                    && "false".equals(message.get("3").path("needPush").asText());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否为带中括号的系统消息
     */
    private boolean isBracketSystemMessage(String message) {
        try {
            if (message == null || message.isBlank()) {
                return false;
            }
            String cleanMessage = message.trim();
            return cleanMessage.startsWith("[") && cleanMessage.endsWith("]");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查消息是否包含切换关键词
     */
    private boolean checkToggleKeywords(String message) {
        String messageStripped = message.strip();
        return properties.getToggleKeywords().contains(messageStripped);
    }

    // ==================== 人工接管模式 ====================

    /**
     * 检查特定会话是否处于人工接管模式（含超时自动退出）
     */
    private boolean isManualMode(String chatId) {
        if (!manualModeConversations.contains(chatId)) {
            return false;
        }
        Long enterTime = manualModeTimestamps.get(chatId);
        if (enterTime != null
                && System.currentTimeMillis() - enterTime > properties.getManualModeTimeout() * 1000L) {
            // 超时，自动退出人工模式
            exitManualMode(chatId);
            return false;
        }
        return true;
    }

    private void enterManualMode(String chatId) {
        manualModeConversations.add(chatId);
        manualModeTimestamps.put(chatId, System.currentTimeMillis());
    }

    private void exitManualMode(String chatId) {
        manualModeConversations.remove(chatId);
        manualModeTimestamps.remove(chatId);
    }

    /**
     * 切换人工接管模式，返回切换后的模式
     */
    private String toggleManualMode(String chatId) {
        if (isManualMode(chatId)) {
            exitManualMode(chatId);
            return "auto";
        } else {
            enterManualMode(chatId);
            return "manual";
        }
    }

    // ==================== 商品描述构建 ====================

    /**
     * 标准化价格（分转元，脏数据返回 0）
     */
    private double formatPrice(JsonNode priceNode) {
        try {
            return Math.round(priceNode.asDouble(0) / 100.0 * 100.0) / 100.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 构建商品描述
     */
    private String buildItemDescription(JsonNode itemInfo) {
        // 处理 SKU 列表（真实数据可能不含 skuList 字段，缺失时按空列表处理）
        ArrayNode skuDetails = MAPPER.createArrayNode();
        JsonNode skuListNode = itemInfo.path("skuList");
        ArrayNode rawSkuList = skuListNode.isArray() ? (ArrayNode) skuListNode : MAPPER.createArrayNode();
        for (JsonNode sku : rawSkuList) {
            // 提取规格文本
            StringBuilder specText = new StringBuilder();
            for (JsonNode prop : sku.path("propertyList")) {
                String valueText = prop.path("valueText").asText("");
                if (!valueText.isEmpty()) {
                    if (specText.length() > 0) {
                        specText.append(' ');
                    }
                    specText.append(valueText);
                }
            }
            ObjectNode cleanSku = MAPPER.createObjectNode();
            cleanSku.put("spec", specText.length() > 0 ? specText.toString() : "默认规格");
            cleanSku.put("price", formatPrice(sku.path("price")));
            cleanSku.put("stock", sku.path("quantity").asInt(0));
            skuDetails.add(cleanSku);
        }

        // 获取价格区间
        String priceDisplay;
        double minPrice = Double.MAX_VALUE;
        double maxPrice = 0;
        boolean hasValidPrice = false;
        for (JsonNode sku : skuDetails) {
            double price = sku.path("price").asDouble(0);
            if (price > 0) {
                hasValidPrice = true;
                minPrice = Math.min(minPrice, price);
                maxPrice = Math.max(maxPrice, price);
            }
        }

        if (hasValidPrice) {
            if (minPrice == maxPrice) {
                priceDisplay = "¥" + minPrice;
            } else {
                priceDisplay = "¥" + minPrice + " - ¥" + maxPrice;
            }
        } else {
            // 如果没有 SKU 价格，回退使用商品主价格
            double mainPrice = Math.round(itemInfo.path("soldPrice").asDouble(0) * 100.0) / 100.0;
            priceDisplay = "¥" + mainPrice;
        }

        ObjectNode summary = MAPPER.createObjectNode();
        summary.put("title", itemInfo.path("title").asText(""));
        summary.put("desc", itemInfo.path("desc").asText(""));
        summary.put("price_range", priceDisplay);
        summary.put("total_stock", itemInfo.path("quantity").asInt(0));
        summary.set("sku_details", skuDetails);
        return summary.toString();
    }

    // ==================== 消息发送 ====================

    /**
     * 发送聊天消息
     */
    private void sendMsg(WebSocket webSocket, String cid, String toid, String text) {
        ObjectNode textBody = MAPPER.createObjectNode();
        textBody.put("contentType", 1);
        textBody.putObject("text").put("text", text);
        String textBase64 = Base64.getEncoder().encodeToString(textBody.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));

        ObjectNode msg = MAPPER.createObjectNode();
        msg.put("lwp", "/r/MessageSend/sendByReceiverScope");
        msg.putObject("headers").put("mid", XianyuUtils.generateMid());

        ArrayNode body = msg.putArray("body");
        ObjectNode sendItem = body.addObject();
        sendItem.put("uuid", XianyuUtils.generateUuid());
        sendItem.put("cid", cid + "@goofish");
        sendItem.put("conversationType", 1);
        ObjectNode content = sendItem.putObject("content");
        content.put("contentType", 101);
        ObjectNode custom = content.putObject("custom");
        custom.put("type", 1);
        custom.put("data", textBase64);
        sendItem.put("redPointPolicy", 0);
        sendItem.putObject("extension").put("extJson", "{}");
        ObjectNode ctx = sendItem.putObject("ctx");
        ctx.put("appVersion", "1.0");
        ctx.put("platform", "web");
        sendItem.putObject("mtags");
        sendItem.put("msgReadStatusSetting", 1);

        ObjectNode receivers = body.addObject();
        ArrayNode actualReceivers = receivers.putArray("actualReceivers");
        actualReceivers.add(toid + "@goofish");
        actualReceivers.add(myId + "@goofish");

        sendText(webSocket, msg.toString());
    }

    /**
     * 发送文本消息（阻塞等待发送完成，保证消息顺序）
     */
    private void sendText(String json) {
        sendText(ws, json);
    }

    private void sendText(WebSocket webSocket, String json) {
        if (webSocket == null) {
            log.warn("WebSocket 未连接，消息发送失败: {}", json);
            return;
        }
        try {
            webSocket.sendText(json, true).join();
        } catch (Exception e) {
            log.error("WebSocket 消息发送失败: {}", e.getMessage());
        }
    }

    /**
     * 强制关闭当前连接并放行重连等待
     */
    private void forceClose(String reason) {
        WebSocket current = ws;
        if (current != null) {
            try {
                current.abort();
            } catch (Exception ignored) {
                // 忽略关闭异常
            }
        }
        closeLatch.countDown();
        log.info("连接已关闭: {}", reason);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ==================== WebSocket 监听器 ====================

    /**
     * WebSocket 监听器（回调由 JDK 单线程串行调用，消息顺序处理）
     */
    private class LiveListener implements WebSocket.Listener {

        private final StringBuilder partialMessage = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("WebSocket 连接已建立");
            // 先绑定当前连接：onOpen 回调可能先于 connectOnce() 中 future.get() 的赋值执行，
            // 否则 init() 发送 /reg 注册消息时会因 ws 为空而发送失败，导致收不到任何推送
            ws = webSocket;
            webSocket.request(1);
            try {
                init(webSocket);
            } catch (Exception e) {
                log.error("连接注册失败: {}", e.getMessage());
                forceClose("init failed");
            }
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            partialMessage.append(data);
            if (last) {
                String message = partialMessage.toString();
                partialMessage.setLength(0);
                try {
                    handleWebSocketMessage(message);
                } catch (Exception e) {
                    log.error("处理消息时发生错误: {}", e.getMessage());
                }
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.warn("WebSocket连接已关闭: {} {}", statusCode, reason);
            closeLatch.countDown();
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("WebSocket 连接错误: {}", error.getMessage());
            closeLatch.countDown();
        }
    }

    /**
     * 处理单条 WebSocket 文本消息
     */
    private void handleWebSocketMessage(String message) {
        try {
            JsonNode messageData = MAPPER.readTree(message);

            // 处理心跳响应
            if (handleHeartbeatResponse(messageData)) {
                return;
            }

            // 发送通用 ACK 响应
            if (messageData.has("headers") && messageData.path("headers").has("mid")) {
                ObjectNode ack = MAPPER.createObjectNode();
                ack.put("code", 200);
                ObjectNode ackHeaders = ack.putObject("headers");
                JsonNode headers = messageData.path("headers");
                ackHeaders.put("mid", headers.path("mid").asText());
                ackHeaders.put("sid", headers.path("sid").asText(""));
                // 复制其他可能的 header 字段
                for (String key : List.of("app-key", "ua", "dt")) {
                    if (headers.has(key)) {
                        ackHeaders.put(key, headers.path(key).asText());
                    }
                }
                sendText(ack.toString());
            }

            // 处理其他消息
            handleMessage(messageData, ws);
        } catch (Exception e) {
            log.error("消息解析失败: {}", e.getMessage());
        }
    }
}
