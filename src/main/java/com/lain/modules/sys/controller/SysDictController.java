// SysDictController.java
package com.lain.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lain.common.vo.R;
import com.lain.modules.sys.service.SysDictService;
import com.lain.modules.sys.vo.SysDictVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/dict")
@RequiredArgsConstructor
@Tag(name = "数据字典管理", description = "数据字典相关接口")
public class SysDictController {

    private final SysDictService sysDictService;

    /**
     * 分页查询字典列表
     */
    @GetMapping("/page")
    @SaCheckPermission("sys:dict:list")
    @Operation(summary = "分页查询字典列表", description = "根据条件分页查询数据字典列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Page.class)))
    })
    public R<Page<SysDictVO>> pageDict(
            @Parameter(description = "分页参数") Page<SysDictVO> page,
            @Parameter(description = "字典查询条件") SysDictVO dictVO) {
        return R.ok(sysDictService.pageDict(
            new Page<>(page.getCurrent(), page.getSize()),
            dictVO
        ));
    }

    /**
     * 查询字典列表
     */
    @GetMapping("/list")
    @SaCheckPermission("sys:dict:list")
    @Operation(summary = "查询字典列表", description = "根据条件查询数据字典列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = List.class)))
    })
    public R<List<SysDictVO>> listDict(@Parameter(description = "字典查询条件") SysDictVO dictVO) {
        return R.ok(sysDictService.listDict(dictVO));
    }

    /**
     * 根据ID获取字典详情
     */
    @GetMapping("/{dictId}")
    @SaCheckPermission("sys:dict:list")
    @Operation(summary = "根据ID获取字典详情", description = "根据字典ID获取字典详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SysDictVO.class))),
        @ApiResponse(responseCode = "404", description = "字典不存在")
    })
    public R<SysDictVO> getDictById(
            @Parameter(description = "字典ID", required = true)
            @PathVariable Long dictId) {
        return R.ok(sysDictService.getDictById(dictId));
    }

    /**
     * 新增字典
     */
    @PostMapping
    @SaCheckPermission("sys:dict:save")
    @Operation(summary = "新增字典", description = "新增数据字典")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "新增成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class)))
    })
    public R saveDict(
            @Parameter(description = "字典信息", required = true)
            @RequestBody SysDictVO dictVO) {
        return R.ok(sysDictService.saveDict(dictVO));
    }

    /**
     * 修改字典
     */
    @PutMapping
    @SaCheckPermission("sys:dict:update")
    @Operation(summary = "修改字典", description = "修改数据字典信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "修改成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class)))
    })
    public R updateDict(
            @Parameter(description = "字典信息", required = true)
            @RequestBody SysDictVO dictVO) {
        return R.ok(sysDictService.updateDict(dictVO));
    }

    /**
     * 删除字典
     */
    @DeleteMapping("/{dictId}")
    @SaCheckPermission("sys:dict:delete")
    @Operation(summary = "删除字典", description = "根据字典ID删除数据字典")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class)))
    })
    public R deleteDict(
            @Parameter(description = "字典ID", required = true)
            @PathVariable Long dictId) {
        return R.ok(sysDictService.deleteDict(dictId));
    }
}
