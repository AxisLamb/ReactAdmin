package com.lain.modules.oss.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lain.common.exception.LainException;
import com.lain.common.vo.R;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * OSS文件控制器
 */
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Tag(name = "OSS文件管理", description = "文件上传、下载、业务关联等接口")
public class OSSController {

    private final FileInfoService fileInfoService;

    /**
     * 文件上传
     * serviceModule/businessType/businessId 三者齐全时，上传后自动建立业务关联
     */
    @PostMapping("/upload")
    @SaCheckPermission("oss:file:upload")
    @Operation(summary = "文件上传", description = "上传文件到对象存储；serviceModule/businessType/businessId 三者齐全时，上传后自动建立业务关联")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "上传成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = FileUploadResult.class)))
    })
    public R<FileUploadResult> upload(
            @Parameter(description = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "服务模块标识，如 sys、cms") @RequestParam(value = "serviceModule", required = false) String serviceModule,
            @Parameter(description = "业务名，如 avatar") @RequestParam(value = "businessType", required = false) String businessType,
            @Parameter(description = "业务主键ID") @RequestParam(value = "businessId", required = false) String businessId,
            @Parameter(description = "业务表，如 sys_user") @RequestParam(value = "businessTable", required = false) String businessTable) throws IOException {
        FileUploadRequest request = FileUploadRequest.builder()
                .originalName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .inputStream(file.getInputStream())
                .serviceModule(serviceModule)
                .businessType(businessType)
                .businessId(businessId)
                .businessTable(businessTable)
                .build();
        return R.ok(fileInfoService.uploadAndSave(request));
    }

    /**
     * 根据 fileId 查询文件详情
     */
    @GetMapping("/{fileId}")
    @SaCheckPermission("oss:file:list")
    @Operation(summary = "查询文件详情", description = "根据文件ID查询文件详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = FileInfo.class))),
        @ApiResponse(responseCode = "404", description = "文件不存在")
    })
    public R<FileInfo> info(
            @Parameter(description = "文件ID", required = true) @PathVariable("fileId") String fileId) {
        FileInfo fileInfo = fileInfoService.getByFileId(fileId);
        if (fileInfo == null) {
            throw new LainException("文件不存在");
        }
        return R.ok(fileInfo);
    }

    /**
     * 按业务维度查询文件列表
     * 示例: serviceModule=sys&businessType=sys_user&businessId=1 => 该用户绑定的全部文件
     */
    @GetMapping("/list")
    @SaCheckPermission("oss:file:list")
    @Operation(summary = "按业务查询文件列表", description = "根据业务维度查询该业务绑定的全部文件，示例: serviceModule=sys&businessType=sys_user&businessId=1")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = List.class)))
    })
    public R<List<FileInfo>> list(
            @Parameter(description = "服务模块标识", required = true) @RequestParam("serviceModule") String serviceModule,
            @Parameter(description = "业务类型", required = true) @RequestParam("businessType") String businessType,
            @Parameter(description = "业务主键ID", required = true) @RequestParam("businessId") String businessId) {
        return R.ok(fileInfoService.listByBusiness(serviceModule, businessType, businessId));
    }

    /**
     * 按业务维度 + 业务字段查询单个文件
     * 示例(用户头像): serviceModule=sys&businessType=sys_user&businessId=1&businessField=avatar
     */
    @GetMapping("/one")
    @SaCheckPermission("oss:file:list")
    @Operation(summary = "按业务查询单个文件", description = "根据业务维度及业务字段查询单个文件，示例(用户头像): serviceModule=sys&businessType=sys_user&businessId=1&businessField=avatar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = FileInfo.class)))
    })
    public R<FileInfo> one(
            @Parameter(description = "服务模块标识", required = true) @RequestParam("serviceModule") String serviceModule,
            @Parameter(description = "业务类型", required = true) @RequestParam("businessType") String businessType,
            @Parameter(description = "业务主键ID", required = true) @RequestParam("businessId") String businessId) {
        return R.ok(fileInfoService.getFileByBusiness(serviceModule, businessType, businessId));
    }

    /**
     * 文件下载
     */
    @GetMapping("/{fileId}/download")
    @SaCheckPermission("oss:file:download")
    @Operation(summary = "文件下载", description = "根据文件ID下载文件，返回文件流")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "下载成功，返回文件流",
            content = @Content(mediaType = "application/octet-stream")),
        @ApiResponse(responseCode = "404", description = "文件不存在")
    })
    public void download(
            @Parameter(description = "文件ID", required = true) @PathVariable("fileId") String fileId,
            HttpServletResponse response) {
        fileInfoService.downloadFile(fileId, response);
    }

    /**
     * 获取文件访问URL
     */
    @GetMapping("/{fileId}/url")
    @Operation(summary = "获取文件访问URL", description = "根据文件ID获取文件的访问链接")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = String.class)))
    })
    @SaCheckPermission("oss:file:url")
    public R<String> url(
            @Parameter(description = "文件ID", required = true) @PathVariable("fileId") String fileId) {
        return R.ok(fileInfoService.getFileUrl(fileId));
    }

    /**
     * 物理删除文件（同时删除存储对象与全部业务关联）
     */
    @DeleteMapping("/{fileId}")
    @SaCheckPermission("oss:file:delete")
    @Operation(summary = "删除文件", description = "物理删除文件，同时删除存储对象与全部业务关联")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class)))
    })
    public R<Boolean> delete(
            @Parameter(description = "文件ID", required = true) @PathVariable("fileId") String fileId) {
        return R.ok(fileInfoService.deleteByFileId(fileId));
    }
}
