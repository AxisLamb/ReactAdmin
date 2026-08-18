package com.lain.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lain.common.annotation.AuditLog;
import com.lain.common.vo.R;
import com.lain.config.auth.AuthConstant;
import com.lain.config.oss.model.FileUploadRequest;
import com.lain.config.oss.model.FileUploadResult;
import com.lain.modules.oss.service.FileInfoService;
import com.lain.modules.sys.service.SysUserService;
import com.lain.modules.sys.vo.SysUserVO;
import com.lain.modules.sys.vo.UserInfoVO;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/sys/user")
@Tag(name = "Sys User", description = "系统用户管理")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private FileInfoService fileInfoService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    @SaCheckPermission("sys:user:list")
    @Operation(summary = "user page list", description = "分页查询用户列表")
    public R page(@ModelAttribute SysUserVO userVO) {
        Page<SysUserVO> voPage = sysUserService.pageList(userVO);
        return R.ok(voPage);
    }


    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/info")
    @SaCheckLogin
    @Operation(summary = "get user info", description = "根据用户ID获取用户信息")
    public R info() {
        SaSession tokenSession = StpUtil.getTokenSession();
        String userId = tokenSession.getString(AuthConstant.TOKEN_SESSION_KEY_USER_ID);
        UserInfoVO userInfoVO = sysUserService.getUserInfo(userId);

        return R.ok(userInfoVO);
    }

    /**
     * 新增用户
     */
    @PostMapping("/save")
    @SaCheckPermission("sys:user:save")
    @Operation(summary = "save user", description = "新增用户")
    @AuditLog("新增用户")
    public R save(@RequestBody SysUserVO userVO) {
        return sysUserService.saveUser(userVO);
    }

    /**
     * 修改用户
     */
    @PostMapping("/update")
    @SaCheckPermission("sys:user:update")
    @Operation(summary = "update user", description = "修改用户")
    @AuditLog("编辑用户")
    public R update(@RequestBody SysUserVO userVO) {
        return sysUserService.updateUser(userVO);
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @SaCheckPermission("sys:user:delete")
    @Operation(summary = "delete users", description = "批量删除用户")
    @AuditLog("删除用户")
    public R delete(@RequestBody Long[] userIds) {
        return sysUserService.deleteUserByIds(Arrays.asList(userIds));
    }

    /**
     * 用户头像上传
     */
    @PostMapping("/upload")
    @SaCheckPermission("sys:user:upload")
    @Operation(summary = "用户头像上传", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "上传成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FileUploadResult.class)))
    })
    public R<FileUploadResult> upload(
            @Parameter(description = "上传的文件", required = true) @RequestParam("file") MultipartFile file
    ) throws IOException {
        // ========== 文件校验开始 ==========
        // 1. 文件非空校验
        if (file == null || file.isEmpty()) {
            return R.error("上传文件不能为空");
        }

        // 2. 文件大小校验（限制为 2MB）
        long maxSize = 2 * 1024 * 1024; // 2MB
        if (file.getSize() > maxSize) {
            return R.error("头像文件大小不能超过 2MB");
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

        // 5. 可选：图片尺寸校验（如果需要）
        // 可以使用 ImageIO 读取图片尺寸
        // BufferedImage image = ImageIO.read(file.getInputStream());
        // if (image.getWidth() < 100 || image.getHeight() < 100) {
        //     return R.fail("头像图片尺寸至少为 100x100 像素");
        // }
        // ========== 文件校验结束 ==========

        String userId = StpUtil.getLoginIdAsString();
        FileUploadRequest request = FileUploadRequest.builder()
                .originalName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .inputStream(file.getInputStream())
                .businessType("avatar")
                .businessId(userId)
                .build();
        return R.ok(fileInfoService.uploadAndSave(request));
    }

    /**
     * 查看用户头像
     */
    @GetMapping("/url")
    @SaCheckLogin
    @Operation(summary = "查看用户头像", description = "查看用户头像")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = R.class)))
    })
    public R<String> url(){
        return R.ok(fileInfoService.url("avatar"));
    }


}
