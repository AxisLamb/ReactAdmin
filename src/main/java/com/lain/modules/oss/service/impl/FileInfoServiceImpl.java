package com.lain.modules.oss.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lain.common.constant.StatusEnum;
import com.lain.common.exception.LainException;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.config.oss.model.StorageLocation;
import com.lain.config.oss.strategy.StoragePathStrategy;
import com.lain.modules.oss.dao.FileInfoMapper;
import com.lain.modules.oss.entity.FileInfo;
import com.lain.modules.oss.service.FileInfoService;
import com.lain.modules.oss.service.ObjectStorageService;
import com.lain.modules.sys.service.SysDictItemService;
import com.lain.modules.sys.vo.SysDictItemVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {

    private final ObjectStorageService objectStorageService;
    private final StoragePathStrategy storagePathStrategy;
    private final SysDictItemService sysDictItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResult uploadAndSave(FileUploadRequest request) {
        // 校验前端是否传值正确，bussiness type必须要在字典项定义范围内
        SysDictItemVO voParam = new SysDictItemVO();
        voParam.setItemLabel(request.getBusinessType());
        voParam.setStatus(StatusEnum.ENABLE.getCode());
        List<SysDictItemVO> itemList = sysDictItemService.listDictItem(voParam);
        if (itemList.size() != 2) {
            throw new LainException("文件业务类型字典项不存在，请联系管理员");
        }
        List<String> collect = itemList.stream().map(SysDictItemVO::getItemValue).toList();
        request.setServiceModule(collect.get(0));
        request.setBusinessTable(collect.get(1));

        // 1. 解析存储位置（桶 + 对象路径）
        StorageLocation location = storagePathStrategy.resolve(request);
        request.setBucketName(location.getBucketName());
        request.setObjectName(location.getObjectName());

        // 2. 上传至对象存储
        FileUploadResult result = objectStorageService.uploadFile(request);

        FileInfo fileInfo = getFileInfo(request, result);
        save(fileInfo);

        return result;
    }

    @Override
    public FileInfo getByFileId(String fileId) {
        return getOne(new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getFileId, fileId)
                .eq(FileInfo::getStatus, 1));
    }

    @Override
    public List<FileInfo> listByBusiness(String serviceModule, String businessType, String businessId) {
        List<FileInfo> files = list(new LambdaQueryWrapper<FileInfo>()
                .eq(StringUtils.hasText(serviceModule), FileInfo::getServiceModule, serviceModule)
                .eq(StringUtils.hasText(businessType), FileInfo::getBusinessType, businessType)
                .eq(StringUtils.hasText(businessId), FileInfo::getBusinessId, businessId)
                .eq(FileInfo::getStatus, 1));

        return files;
    }

    @Override
    public FileInfo getFileByBusiness(String serviceModule, String businessType, String businessId) {
        List<FileInfo> refs = listByBusiness(serviceModule, businessType, businessId);
        if (refs.isEmpty()) {
            return null;
        }
        return getByFileId(refs.get(0).getFileId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByFileId(String fileId) {
        FileInfo fileInfo = getByFileId(fileId);
        if (fileInfo == null) {
            throw new LainException("文件不存在");
        }

        boolean deleted = objectStorageService.deleteFile(fileInfo.getBucketName(), fileInfo.getObjectName());
        if (deleted) {
            removeById(fileInfo.getId());
        }
        return deleted;
    }

    @Override
    public void downloadFile(String fileId, HttpServletResponse response) {
        FileInfo fileInfo = getByFileId(fileId);
        if (fileInfo == null) {
            throw new LainException("文件不存在");
        }

        try (InputStream inputStream = objectStorageService.downloadFile(fileInfo.getBucketName(), fileInfo.getObjectName());
             OutputStream outputStream = response.getOutputStream()) {

            response.setContentType(fileInfo.getFileType());
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8));

            inputStream.transferTo(outputStream);
            response.flushBuffer();
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new LainException("文件下载失败");
        }
    }

    @Override
    public String getFileUrl(String fileId) {
        FileInfo fileInfo = getByFileId(fileId);
        if (fileInfo == null) {
            throw new LainException("文件不存在");
        }
        return objectStorageService.getFileUrl(fileInfo.getBucketName(), fileInfo.getObjectName());
    }

    @Override
    public String url(String businessType) {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        List<FileInfo> files = list(new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getUserId, loginIdAsLong)
                .eq(FileInfo::getBusinessType, businessType)
                .eq(FileInfo::getStatus, StatusEnum.ENABLE.getCode())
                .orderByDesc(FileInfo::getUpdateTime));

        if (CollectionUtil.isEmpty(files)) {
            throw new LainException("文件不存在");
        }

        return objectStorageService.getFileUrl(files.getFirst().getBucketName(), files.getFirst().getObjectName());
    }

    @NotNull
    private static FileInfo getFileInfo(FileUploadRequest request, FileUploadResult result) {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();

        // 3. 保存文件信息
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(result.getFileId());
        fileInfo.setUserId(loginIdAsLong);
        fileInfo.setOriginalName(result.getOriginalName());
        fileInfo.setFileSize(result.getFileSize());
        fileInfo.setFileType(result.getFileType());
        fileInfo.setBucketName(result.getBucketName());
        fileInfo.setObjectName(result.getObjectName());
        fileInfo.setFilePath(result.getFilePath());
        fileInfo.setServiceModule(request.getServiceModule());
        fileInfo.setBusinessType(request.getBusinessType());
        fileInfo.setBusinessTable(request.getBusinessTable());
        fileInfo.setBusinessId(request.getBusinessId());
        return fileInfo;
    }
}
