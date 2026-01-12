package com.lain.config.oss.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lain.config.cache.vo.StrPool;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.ArrayList;

/**
 * 存储桶缓存 key 构建器接口
 */
public interface BucketKeyBuilder {

    /**
     * 缓存前缀，用于区分项目，环境等等
     *
     * @return 缓存前缀
     */
    default String getPrefix() {
        return null;
    }

    /**
     * 服务模块名，用于区分后端服务、前端模块等
     *
     * @return 服务模块名
     */
    default String getModular() {
        return null;
    }

    /**
     * key的业务类型，用于区分表
     *
     * @return 通常是表名
     */
    @NonNull
    default String getTable() {
        return "bucket";
    }

    /**
     * key的字段名，用于区分字段
     *
     * @return 通常是key的字段名
     */
    default String getField() {
        return "name";
    }

    /**
     * 文件类型
     *
     * @return 文件类型
     */
    default FileType getFileType() {
        return FileType.IMAGE;
    }

    /**
     * 缓存自动过期时间
     *
     * @return 缓存自动过期时间
     */
    @Nullable
    default Duration getExpire() {
        return null;
    }

    /**
     * 存储桶名称
     *
     * @return 存储桶名称
     */
    @NonNull
    default String getBucketName() {
        return getBucketKey();
    }

    /**
     * 对象名称
     *
     * @return 对象名称
     */
    default String getObjectName() {
        return null;
    }

    /**
     * 构建存储桶缓存 key
     *
     * @param uniques 参数
     * @return BucketKey
     */
    default BucketKey bucketKey(Object... uniques) {
        String key = getBucketKey(uniques);
        return new BucketKey(key, getExpire(), getBucketName(), getObjectName());
    }

    /**
     * 构建存储桶对象缓存 key
     *
     * @param objectName 对象名称
     * @param uniques 参数
     * @return BucketKey
     */
    default BucketKey bucketObjectKey(@NonNull String objectName, Object... uniques) {
        String key = getBucketKey(uniques);
        return new BucketKey(key, getExpire(), getBucketName(), objectName);
    }

    /**
     * 根据动态参数拼接key
     *
     * @param uniques 动态参数
     * @return 字符串型的缓存的key
     */
    private String getBucketKey(Object... uniques) {
        // 实现key构建逻辑
        return buildKey(uniques);
    }

    /**
     * 构建key的核心逻辑
     */
    private String buildKey(Object... uniques) {
        ArrayList<String> regionList = new ArrayList<>();

        // 前缀
        String prefix = getPrefix();
        if (StrUtil.isNotEmpty(prefix)) {
            regionList.add(prefix);
        }

        // 服务模块名
        String modular = getModular();
        if (StrUtil.isNotEmpty(modular)) {
            regionList.add(modular);
        }

        // 业务类型
        String table = getTable();
        if (StrUtil.isNotEmpty(table)) {
            regionList.add(table);
        }

        // 业务字段
        String field = getField();
        if (StrUtil.isNotEmpty(field)) {
            regionList.add(field);
        }

        // 文件类型
        FileType fileType = getFileType();
        if (fileType != null) {
            regionList.add(fileType.name());
        }

        // 存储桶名称
//        String bucketName = getBucketName();
//        if (StrUtil.isNotEmpty(bucketName)) {
//            regionList.add(bucketName);
//        }

        // 对象名称
        String objectName = getObjectName();
        if (StrUtil.isNotEmpty(objectName)) {
            regionList.add(objectName);
        }

        // 业务值
        for (Object unique : uniques) {
            if (ObjectUtil.isNotEmpty(unique)) {
                regionList.add(String.valueOf(unique));
            }
        }

        return CollUtil.join(regionList, StrPool.DASH).toLowerCase();
    }

    enum FileType {
        /**
         * 图片
         */
        IMAGE,
        /**
         * 视频
         */
        VIDEO,
        /**
         * 音频
         */
        AUDIO,
        /**
         * 文档
         */
        DOC,
        /**
         * 其他
         */
        OTHER
    }
}
