package com.lain.config.oss.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult {
    private String fileId;
    private String bucketName;
    private String objectName;
    private String filePath;
    private String originalName;
    private Long fileSize;
    private String fileType;
}