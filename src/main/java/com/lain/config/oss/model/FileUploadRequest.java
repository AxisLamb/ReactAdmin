package com.lain.config.oss.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {
    private String bucketName;
    private String originalName;
    private InputStream inputStream;
    private Long fileSize;
    private String fileType;
    private String serviceModule;
    private String businessType;
    private String businessId;
    private String createdBy;
    private BucketKey bucketKey;
}
