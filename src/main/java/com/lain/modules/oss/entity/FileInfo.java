package com.lain.modules.oss.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lain.common.vo.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_info")
public class FileInfo extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileId;

    private String originalName;

    private Long fileSize;

    private String fileType;

    private String bucketName;

    private String objectName;

    private String filePath;

    private String serviceModule;

    private String businessType;

    private String businessId;

    private Integer status = 1;

//    @TableField(fill = FieldFill.INSERT)
//    private String createdBy;
//
//    @TableField(fill = FieldFill.INSERT)
//    private LocalDateTime createdTime;
//
//    @TableField(fill = FieldFill.UPDATE)
//    private String updatedBy;
//
//    @TableField(fill = FieldFill.UPDATE)
//    private LocalDateTime updatedTime;
}