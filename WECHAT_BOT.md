# 微信机器人接入指南（AgentScope 2.0）

把 AgentScope 2.0 的 `HarnessAgent` 接进微信，让用户在微信里直接和 AI 对话。

---

## 一、先说清楚：个人微信能不能接？

**不能，而且不应该接。**

| 渠道 | 是否有官方 API | 能否收发消息 | 结论 |
|------|---------------|-------------|------|
| 个人微信号（微信聊天窗口） | ❌ 无 | — | 只能用逆向协议（itchat / 部分 wechaty puppet 等），**违反《微信软件许可及服务协议》，会导致封号**。本项目不提供此类实现 |
| **企业微信自建应用** | ✅ 有 | ✅ 收 + 发 | **推荐**，个人可免费注册，能力最完整 |
| **微信公众号**（已认证服务号） | ✅ 有 | ✅ 收 + 发（客服消息） | 可用，需要企业认证 |
| 微信公众号（未认证订阅号） | ✅ 部分 | ⚠️ 仅被动回复，且 5 秒内 | 能用，但复杂问题会超时 |
| 企业微信群机器人 Webhook | ✅ 有 | ❌ 只能推送，收不到用户消息 | 只适合做告警推送，不是 bot |

所以下面的接入目标是**企业微信自建应用**（主推）和**微信公众号**。

---

## 二、实现原理

AgentScope 2.0 内置了 `Gateway` / `Channel` 扩展点（官方只提供了 `chatui` 一个实现）。本模块按同一套契约实现了微信渠道：

```
微信服务器
   │  POST /wechat/callback (XML，AES 加密)
   ▼
WeChatCallbackController   ── 签名校验 → 解密 → 解析
   ▼
WeChatBotService           ── 去重 → 提交线程池
   ▼
WeChatChannel.dispatch()   ── 包装成 InboundMessage
   ▼
HarnessGateway.run()       ── 路由 + 会话串行化（SessionTurnGate）
   ▼
HarnessAgent               ── qwen 大模型 + 工具
   ▼
回传路径（二选一）
   ├─ 4.5 秒内完成 → 被动回复 XML（加密后直接返回）
   └─ 超时        → 立即响应 success，完成后调接口主动推送
```

**为什么要"双通道"**：微信要求回调 5 秒内必须响应，而大模型通常需要几十秒。所以能在时限内回答就直接回，来不及就先结束回调、稍后通过开放接口主动推送。这是企业微信 bot 的标准做法。

**会话隔离**：`DmScope.PER_PEER` —— 每个微信用户一个独立会话（`sessionId` 由渠道 + 用户标识派生），多轮对话自动保留上下文；同一用户的并发消息由 Gateway 串行化，不会串扰。

---

## 三、代码清单

| 文件 | 职责 |
|------|------|
| `com/lain/ai/wechat/WeChatBotProperties.java` | 配置绑定，前缀 `wechat.bot` |
| `com/lain/ai/wechat/WeChatCrypto.java` | 消息加解密（AES-256-CBC + PKCS7）与 SHA1 签名 |
| `com/lain/ai/wechat/WeChatXmlMessage.java` | XML 解析（防 XXE）与被动回复生成 |
| `com/lain/ai/wechat/WeChatApiClient.java` | access_token 缓存 + 主动发消息 |
| `com/lain/ai/wechat/WeChatChannel.java` | AgentScope `Channel` 实现 |
| `com/lain/ai/wechat/WeChatBotService.java` | 编排：去重、异步、双通道回复 |
| `com/lain/ai/wechat/WeChatCallbackController.java` | 回调端点（GET 校验 + POST 收消息） |
| `com/lain/ai/wechat/WeChatBotConfiguration.java` | 装配 Agent / Gateway / Channel |

---

## 四、你需要手动做的事（我无法代劳的部分）

### 4.1 注册企业微信并创建应用

1. 打开 <https://work.weixin.qq.com/wework_admin/register_wx>，**个人也能注册**（类型选"企业"，规模选 1-50 人，名称随便填，无需营业执照）。
2. 登录后进入管理后台：
   - **我的企业 → 企业信息** → 页面最下方复制 **企业ID（CorpID）** → 这就是 `app-id`
3. **应用管理 → 自建 → 创建应用**：
   - 上传一个 logo、填名称（如 "AI 助手"）、选择可见成员（先选自己）
   - 创建完成后进入应用详情页，记录：
     - **AgentId** → 这就是 `agent-id`
     - **Secret** → 点"查看"后会推送到你的企业微信 → 这就是 `secret`
4. 设置**企业可信 IP**（这步最容易漏）：
   - 应用详情页 → **企业可信IP** → 填入你服务器的**公网 IP**
   - 不填的话调用接口会报 `errcode 60020 not allow to access from your ip`

### 4.2 准备一个公网可访问的回调地址

微信服务器要主动访问你的服务，所以必须是公网 HTTPS/HTTP 地址，**且只能用 80/443 端口**（不支持自定义端口）。

- **有服务器 + 域名**：直接部署，用 Nginx 反代到 8888 端口。
- **本地开发**：用内网穿透，推荐任选其一：
  - `cpolar`：`cpolar http 8888`
  - `natapp`：官网申请免费隧道后运行客户端
  - `Cloudflare Tunnel`：`cloudflared tunnel --url http://localhost:8888`
  - `ngrok`：`ngrok http 8888`
  - 得到的地址形如 `https://abc123.r6.cpolar.top`

> 免费穿透的域名每次会变，变一次就要去微信后台改一次回调 URL。想稳定就买个固定子域名（几十元/年）。

### 4.3 在微信后台配置回调

**先保证服务已启动且公网可访问**，再填配置——点保存时微信会立刻发一个 GET 请求校验。

企业微信：应用详情页 → **接收消息 → 设置 API 接收**：

| 字段 | 填什么 |
|------|--------|
| URL | `https://你的域名/wechat/callback` |
| Token | 点"随机获取"，或自己填 3-32 位字符 |
| EncodingAESKey | 点"随机获取"（43 位） |

把这三个值记下来，Token → `token`，EncodingAESKey → `encoding-aes-key`。

### 4.4 配置环境变量（不要把密钥提交到 Git）

```bash
# Windows CMD（设置后需重启终端/IDE 才生效）
setx WECHAT_APP_ID   "ww1234567890abcdef"
setx WECHAT_SECRET   "你的应用Secret"
setx WECHAT_TOKEN    "你的Token"
setx WECHAT_AES_KEY  "你的43位EncodingAESKey"
setx WECHAT_AGENT_ID "1000002"
setx DASHSCOPE_API_KEY "sk-你的阿里云百炼APIKey"

# Linux / macOS
export WECHAT_APP_ID="ww..."
export DASHSCOPE_API_KEY="sk-..."
```

也可以直接写在 `application-dev.yml` 里（仅本地开发，注意别提交密钥）。

然后打开开关：

```yaml
wechat:
  bot:
    enabled: true      # ← 改成 true
    channel-type: wecom
```

### 4.5 启动并验证

```bash
mvn spring-boot:run
```

看到日志 `微信机器人已启动，渠道=wechat，Agent=wechat-bot` 即接入成功。

打开手机**企业微信 → 工作台 → 你的应用**，发一句"你好"，就会收到 AI 回复。

---

## 五、改用微信公众号（可选）

把 `channel-type` 改成 `official-account`：

```yaml
wechat:
  bot:
    enabled: true
    channel-type: official-account   # 公众号模式，不再需要 agent-id
    app-id: 你的AppID
    secret: 你的AppSecret
```

公众号后台需要做的：

1. <https://mp.weixin.qq.com> 注册（订阅号个人可注册，服务号需企业认证）。
2. **开发 → 基本配置 → 公众号开发信息** → 拿到 AppID / AppSecret。
3. 同页面设置 **IP 白名单**，填服务器公网 IP（不填调用接口会报 `40164`）。
4. **服务器配置**：URL 填 `https://你的域名/wechat/callback`，Token / EncodingAESKey 随机获取，消息加解密方式选**安全模式**。

> ⚠️ **重要限制**：未认证的订阅号**没有"客服消息"接口权限**，主动推送会返回 `48001`。
> 这类账号只能在 5 秒内被动回复，复杂问题会超时。
> 想要完整体验，请用**企业微信**（免费、无认证要求）或**已认证的服务号**。

---

## 六、配置项说明

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `wechat.bot.enabled` | `false` | 总开关，关闭时不会创建任何相关 Bean |
| `wechat.bot.channel-type` | `wecom` | `wecom` 企业微信 / `official-account` 公众号 |
| `wechat.bot.app-id` | — | 企业微信 CorpID / 公众号 AppID |
| `wechat.bot.secret` | — | 应用 Secret / AppSecret |
| `wechat.bot.token` | — | 回调 Token |
| `wechat.bot.encoding-aes-key` | — | 43 位 EncodingAESKey |
| `wechat.bot.agent-id` | — | 仅企业微信需要的 AgentId |
| `wechat.bot.callback-path` | `/wechat/callback` | 回调路径，需与微信后台一致 |
| `wechat.bot.encrypt-mode` | `true` | 安全模式；明文模式改为 `false` |
| `wechat.bot.sync-reply-timeout-ms` | `4500` | 被动回复等待窗口，超时转异步推送 |
| `wechat.bot.max-reply-bytes` | `1800` | 单条回复字节上限（微信限制 2048） |
| `wechat.bot.agent.model` | `dashscope:qwen3.7-plus` | 模型标识 |
| `wechat.bot.agent.sysPrompt` | 见配置类 | 系统提示词 |
| `wechat.bot.agent.enable-file-tools` | `false` | 是否挂载文件系统工具（生产慎用） |
| `wechat.bot.agent.worker-threads` | `8` | 处理消息的线程池大小 |

---

## 七、排错手册

| 现象 | 原因 / 处理 |
|------|------------|
| 微信后台保存提示"URL 校验失败" | ① 服务没启动 ② 公网地址不通（用浏览器直接访问 URL 应返回 `signature mismatch`）③ Token 或 EncodingAESKey 与后台不一致 ④ 用了非 80/443 端口 |
| `errcode 60020 not allow to access from your ip` | 企业微信"企业可信IP"没填服务器公网 IP |
| `errcode 40164` | 公众号 IP 白名单没填 |
| `errcode 48001` | 公众号未认证，无客服消息接口权限，改用企业微信 |
| `errcode 40001 / 40014 / 42001` | Secret 填错或 token 失效，代码已自动刷新重试一次 |
| 能收到消息但没回复 | 检查 `DASHSCOPE_API_KEY` 是否配置、查看服务日志 |
| 收到两条相同回复 | 微信重试导致，代码已按 MsgId 去重（10 分钟窗口） |
| 回复被截断 | 超出 2048 字节，调大也没用，属微信硬限制；可让 Agent 回答更精简 |

---

## 八、二次开发

**换模型**：改 `wechat.bot.agent.model`，支持 `openai:gpt-5.5`、`anthropic:claude-sonnet-4-5`、`ollama:llama3` 等，字符串形式由 AgentScope `ModelRegistry` 解析（需要对应的 API Key 环境变量）。

**挂载更多工具**：在 `WeChatBotConfiguration.weChatAgent()` 里注册：

```java
Toolkit toolkit = new Toolkit();
toolkit.registerTool(new BrowserAssistant());   // 浏览器自动化
toolkit.registerTool(new FileAssistant());      // 文件系统
```

**让不同用户/群走不同 Agent**：在 `WeChatChannel` 构造时给 `ChannelConfig` 加绑定：

```java
ChannelConfig.builder(CHANNEL_ID)
        .defaultAgentId("wechat-bot")
        .binding(ChannelBinding.forPeer("zhangsan", "admin-agent"))  // 指定用户走专属 Agent
        .dmScope(DmScope.PER_PEER)
        .build();
```

**改造成群聊机器人**：企业微信群聊需要 `@机器人` 触发，解析出来的是 `MsgType=text` 且 `Content` 带 `@` 前缀，在 `WeChatBotService` 里去掉前缀即可；会话维度把 `Peer.direct(userId)` 换成 `Peer.group(chatId)` 就能按群隔离会话。

---

## 九、安全提醒

- 回调接口已在 `AuthWhitelistConfig` 中放行 `/wechat/**`（微信服务器不带本站登录态），**不要把 `/wechat/**` 用于任何内部接口**。
- 所有回调请求都会校验 SHA1 签名，签名不匹配直接丢弃。
- XML 解析关闭了 DTD 与外部实体，防止 XXE 攻击。
- 解密时会校验 `ReceiveId`（CorpID / AppID），防止消息被投递到错误的主体。
- 密钥请走环境变量或配置中心，不要提交进 Git。
