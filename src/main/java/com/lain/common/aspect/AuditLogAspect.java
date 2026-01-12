package com.lain.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.lain.common.annotation.AuditLog;
import com.lain.modules.sys.entity.SysAuditLog;
import com.lain.modules.sys.service.SysAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private SysAuditLogService sysAuditLogService;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        long beginTime = System.currentTimeMillis();
        // 执行方法
        Object result = point.proceed();
        // 执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        // 保存日志（包含执行结果）
        saveAuditLog(point, time, auditLog.value(), result);
        return result;
    }


    private void saveAuditLog(ProceedingJoinPoint joinPoint, long time, String operation, Object result) {
        SysAuditLog auditLog = new SysAuditLog();

        // 获取当前用户信息
        if (StpUtil.isLogin()) {
            auditLog.setUserId(StpUtil.getLoginIdAsLong());
            // 这里可以根据需要获取用户名
        }

        // 获取注解上的操作描述
        auditLog.setOperation(operation);

        // 请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        auditLog.setMethod(className + "." + methodName + "()");

        // 请求的参数
        Object[] args = joinPoint.getArgs();
        StringBuilder params = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                params.append(arg.toString()).append(" ");
            }
        }
        auditLog.setParams(params.toString());

        // 执行结果
        if (result != null) {
            auditLog.setResult(result.toString());
        }

        // 获取request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        // 设置IP地址
        auditLog.setIp(getIpAddress(request));
        // 设置用户代理
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        // 设置执行时长
        auditLog.setTime(time);
        // 设置创建时间
//        auditLog.setCreateTime(LocalDateTime.now());

        // 保存数据库
        sysAuditLogService.save(auditLog);
    }


    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
