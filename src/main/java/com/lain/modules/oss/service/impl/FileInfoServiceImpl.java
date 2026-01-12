package com.lain.modules.oss.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lain.modules.oss.service.ObjectStorageService;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.modules.oss.dao.FileInfoMapper;
import com.lain.modules.oss.entity.FileInfo;
import com.lain.modules.oss.service.FileInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {

    @Autowired
    private ObjectStorageService objectStorageService;

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Override
    @Transactional
    public FileUploadResult uploadAndSave(FileUploadRequest request) {
        // 上传到对象存储
        FileUploadResult uploadResult = objectStorageService.uploadFile(request);

        // 保存元数据到数据库
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(uploadResult.getFileId());
        fileInfo.setOriginalName(request.getOriginalName());
        fileInfo.setFileSize(request.getFileSize());
        fileInfo.setFileType(request.getFileType());
        fileInfo.setBucketName(uploadResult.getBucketName());
        fileInfo.setObjectName(uploadResult.getObjectName());
        fileInfo.setFilePath(uploadResult.getFilePath());
        fileInfo.setServiceModule(request.getServiceModule());
        fileInfo.setBusinessType(request.getBusinessType());
        fileInfo.setBusinessId(request.getBusinessId());

        this.save(fileInfo);

        return uploadResult;
    }

    @Override
    public FileInfo getByFileId(String fileId) {
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getFileId, fileId)
                .eq(FileInfo::getStatus, 1);
        return this.getOne(queryWrapper, false);
    }

    @Override
    public List<FileInfo> listByBusiness(String serviceModule, String businessType, String businessId) {
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getServiceModule, serviceModule)
                .eq(FileInfo::getBusinessType, businessType)
                .eq(FileInfo::getBusinessId, businessId)
                .eq(FileInfo::getStatus, 1); // 状态为有效
        return this.list(queryWrapper);
    }

    @Override
    @Transactional
    public boolean deleteByFileId(String fileId) {
        FileInfo fileInfo = this.getByFileId(fileId);
        if (fileInfo != null) {
            // 从对象存储删除文件
            objectStorageService.deleteFile(fileInfo.getBucketName(), fileInfo.getObjectName());

            // 逻辑删除数据库记录 - 使用MyBatis Plus的update方法
            FileInfo updateFileInfo = new FileInfo();
            updateFileInfo.setStatus(0); // 设置状态为删除状态
            LambdaUpdateWrapper<FileInfo> queryWrapper = new LambdaUpdateWrapper<>();
            queryWrapper.eq(FileInfo::getFileId, fileId);
            return this.update(updateFileInfo, queryWrapper);
        }
        return false;
    }

    @Override
    public void downloadFile(String fileId, HttpServletResponse response) {
        FileInfo fileInfo = this.getByFileId(fileId);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }

        try {
            response.setContentType(fileInfo.getFileType());
            response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileInfo.getOriginalName(), "UTF-8"));

            OutputStream outputStream = response.getOutputStream();
            objectStorageService.downloadFile(fileInfo.getBucketName(), fileInfo.getObjectName(), outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    public String getFileUrl(String fileId) {
        FileInfo fileInfo = this.getByFileId(fileId);
        if (fileInfo == null) {
            return null;
        }
        return objectStorageService.getFileUrl(fileInfo.getBucketName(), fileInfo.getObjectName());
    }
}
