package com.lain.config.db;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lain.config.auth.AuthConstant;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {

        SaSession tokenSession = StpUtil.getTokenSession();
        String userId = tokenSession.getString(AuthConstant.TOKEN_SESSION_KEY_USER_ID);
        Long userIdInt = Long.parseLong(userId);

        // 自动填充创建时间和更新时间字段
        this.strictInsertFill(metaObject, "createdBy", Long.class, userIdInt);
        this.strictInsertFill(metaObject, "updatedBy", Long.class, userIdInt);

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        SaSession tokenSession = StpUtil.getTokenSession();
        String userId = tokenSession.getString(AuthConstant.TOKEN_SESSION_KEY_USER_ID);
        Long userIdInt = Long.parseLong(userId);
        // 自动填充更新时间字段
        this.strictInsertFill(metaObject, "updatedBy", Long.class, userIdInt);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}