package com.lain.config.oss.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;

/**
 * 文件上传请求
 */
@Getter
@Setter
@Builder
public class FileUploadRequest {

    /** 原始文件名 */
    private String originalName;

    /** 文件大小 */
    private Long fileSize;

    /** 文件类型(MIME) */
    private String fileType;

    /** 文件输入流 */
    private final InputStream inputStream;

    /** 服务模块（如 sys、order），用于桶隔离与业务归组 */
    private String serviceModule;

    /** 业务类型 如 avatar */
    private String businessType;

    /** 业务主键（如用户ID）；与 serviceModule、businessType 同时传入时，上传后自动建立 file_biz_ref 关联 */
    private String businessId;

    /** 业务表（如 sys_user、contract） */
    private String businessTable;

    /** 显式指定存储桶（可选，为空时由 StoragePathStrategy 决定） */
    private String bucketName;

    /** 显式指定对象名（可选，为空时由 StoragePathStrategy 决定） */
    private String objectName;
}
