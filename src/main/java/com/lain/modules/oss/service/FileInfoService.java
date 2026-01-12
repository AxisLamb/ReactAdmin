package com.lain.modules.oss.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.modules.oss.entity.FileInfo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface FileInfoService extends IService<FileInfo> {

    /**
     * 上传文件并保存元数据
     */
    FileUploadResult uploadAndSave(FileUploadRequest request);

    /**
     * 根据文件ID获取文件信息
     */
    FileInfo getByFileId(String fileId);

    /**
     * 根据业务信息查询文件列表
     */
    List<FileInfo> listByBusiness(String serviceModule, String businessType, String businessId);

    /**
     * 删除文件（逻辑删除）
     */
    boolean deleteByFileId(String fileId);

    /**
     * 下载文件
     */
    void downloadFile(String fileId, HttpServletResponse response);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String fileId);
}
