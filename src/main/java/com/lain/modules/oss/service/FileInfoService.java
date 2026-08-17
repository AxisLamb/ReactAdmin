package com.lain.modules.oss.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.modules.oss.entity.FileInfo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 文件信息服务接口
 */
public interface FileInfoService extends IService<FileInfo> {

    /**
     * 上传并保存文件
     * <p>
     * 存储位置（桶 + 对象路径）由 StoragePathStrategy 解析；
     * serviceModule/businessType/businessId 齐全时自动建立 file_biz_ref 业务关联。
     */
    FileUploadResult uploadAndSave(FileUploadRequest request);

    /**
     * 根据 fileId 查询文件信息
     */
    FileInfo getByFileId(String fileId);

    /**
     * 按业务维度查询文件列表（经 file_biz_ref 中间表，按 sortOrder 排序）
     */
    List<FileInfo> listByBusiness(String serviceModule, String businessType, String businessId);

    /**
     * 按业务维度 + 业务字段查询单个文件（如用户头像），取排序最前的一个
     */
    FileInfo getFileByBusiness(String serviceModule, String businessType, String businessId);

    /**
     * 物理删除文件（同时删除存储对象、数据库记录及全部业务关联）
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

    /**
     * 按业务维度 + 业务字段查询单个文件（如用户头像），取排序最前的一个
     */
    String url(String businessType);
}
