package com.lain.modules.oss.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lain.common.exception.LainException;
import com.lain.common.oss.TestBucketKeyBuilder;
import com.lain.common.vo.R;
import com.lain.config.oss.model.BucketKey;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.modules.oss.entity.FileInfo;
import com.lain.modules.oss.service.FileInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "FILE/IMAGE Management", description = "图片上传、下载、浏览和删除接口")
public class OSSController {

    private final FileInfoService fileInfoService;

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    @SaCheckPermission("oss:file:upload")
    @Operation(summary = "upload IMG", description = "上传图片文件并保存相关信息")
    public R<FileUploadResult> uploadImage(
            @RequestPart("file") MultipartFile file,
            String serviceModule,
            String businessType,
            String businessId) {
        try {

            BucketKey key = TestBucketKeyBuilder.build();

            FileUploadRequest request = FileUploadRequest.builder()
                    .inputStream(file.getInputStream())
                    .originalName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .serviceModule(serviceModule)
                    .businessType(businessType)
                    .businessId(businessId)
                    .bucketKey(key)
                    .build();

            return R.ok(fileInfoService.uploadAndSave(request));
        } catch (IOException e) {
            throw new LainException("图片上传失败", e);
        }
    }

    /**
     * 下载图片
     */
    @GetMapping("/download/{fileId}")
    @Operation(summary = "download FILE", description = "根据文件ID下载图片")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "下载成功"),
        @ApiResponse(responseCode = "500", description = "下载失败")
    })
    @SaCheckPermission("oss:file:download")
    public void downloadImage(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId,
            HttpServletResponse response) {
        fileInfoService.downloadFile(fileId, response);
    }

    /**
     * 获取图片访问链接
     */
    @GetMapping("/url/{fileId}")
    @Operation(summary = "get link of img", description = "根据文件ID获取图片的访问URL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功",
            content = @Content(mediaType = "text/plain",
                schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "获取失败")
    })
    @SaCheckPermission("oss:file:url")
    public R<String> getImageUrl(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        return R.ok(fileInfoService.getFileUrl(fileId));
    }

    /**
     * 删除图片
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "delete img", description = "根据文件ID删除图片")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "500", description = "删除失败")
    })
    @SaCheckPermission("oss:file:delete")
    public R<Boolean> deleteImage(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        return R.ok(fileInfoService.deleteByFileId(fileId));
    }

    /**
     * 根据业务信息查询图片列表
     */
    @GetMapping("/list")
    @Operation(summary = "list img", description = "根据业务信息查询相关图片列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @SaCheckPermission("oss:file:list")
    public R<List<FileInfo>> listImages(
            @Parameter(description = "服务模块")
            @RequestParam(value = "serviceModule", required = false) String serviceModule,
            @Parameter(description = "业务类型")
            @RequestParam(value = "businessType", required = false) String businessType,
            @Parameter(description = "业务ID")
            @RequestParam(value = "businessId", required = false) String businessId) {
        return R.ok(fileInfoService.listByBusiness(serviceModule, businessType, businessId));
    }
}
