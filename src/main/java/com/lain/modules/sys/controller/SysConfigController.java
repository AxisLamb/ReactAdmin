package com.lain.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 系统配置页面
 * <p>
 * 登录页图片直接落库到 file_info 表（businessType = loginPage），
 * 查询时只需 businessType=loginPage 且 status=1 即可返回给前端轮询播放。
 */
@RestController
@RequestMapping("/sys/config")
@Tag(name = "Sys Config", description = "系统配置页面")
public class SysConfigController {

    private static final String BUSINESS_TYPE = "loginPage";

    @Autowired
    private FileInfoService fileInfoService;

    /**
     * 上传登录页图片（可多次上传组成一组，供登录页轮询播放）
     */
    @PostMapping("/upload")
    @SaCheckPermission("sys:config:loginPageUpload")
    @Operation(summary = "上传登录页图片", description = "上传登录页自定义图片，可多次上传组成轮播图组")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "上传成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FileUploadResult.class)))
    })
    public R<FileUploadResult> loginPageUpload(
            @Parameter(description = "上传的图片文件", required = true) @RequestParam("file") MultipartFile file
    ) throws IOException {
        // ========== 文件校验开始 ==========
        // 1. 文件非空校验
        if (file == null || file.isEmpty()) {
            return R.error("上传文件不能为空");
        }

        // 2. 文件大小校验（限制为 5MB，登录页大图）
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            return R.error("图片文件大小不能超过 5MB");
        }

        // 3. 文件类型校验（只允许图片）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return R.error("只允许上传图片文件");
        }

        // 4. 文件扩展名校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return R.error("文件名不能为空");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        Set<String> allowedExtensions = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
        if (!allowedExtensions.contains(extension)) {
            return R.error("不支持的文件格式，仅支持: " + String.join(", ", allowedExtensions));
        }
        // ========== 文件校验结束 ==========

        String loginIdAsString = StpUtil.getLoginIdAsString();

        FileUploadRequest request = FileUploadRequest.builder()
                .originalName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .inputStream(file.getInputStream())
                .businessType(BUSINESS_TYPE)
                .businessId(loginIdAsString)
                .build();
        return R.ok(fileInfoService.uploadAndSave(request));
    }

    /**
     * 查询登录页轮播图片列表（登录前调用，供前端轮询播放）
     */
    @SaIgnore
    @GetMapping("/loginPageList")
    @Operation(summary = "登录页轮播图片列表", description = "返回登录页自定义图片组（businessType=loginPage, status=1）的访问URL列表，前端轮询播放")
    public R<List<String>> list() {
        List<FileInfo> files = fileInfoService.listByBusiness(null, BUSINESS_TYPE, null);
        List<String> urls = files.stream()
                .map(f -> fileInfoService.getFileUrl(f.getFileId()))
                .toList();
        return R.ok(urls);
    }
}
