// SysDictItemController.java
package com.lain.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lain.common.vo.R;
import com.lain.modules.sys.service.SysDictItemService;
import com.lain.modules.sys.vo.SysDictItemVO;
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
@RequestMapping("/sys/dict/item")
@RequiredArgsConstructor
@Tag(name = "字典项管理", description = "数据字典项相关接口")
public class SysDictItemController {

    private final SysDictItemService sysDictItemService;

    /**
     * 分页查询字典项列表
     */
    @GetMapping("/page")
    @SaCheckPermission("sys:dict:item:list")
    @Operation(summary = "分页查询字典项列表", description = "根据条件分页查询字典项列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Page.class)))
    })
    public R<Page<SysDictItemVO>> pageDictItem(
            @Parameter(description = "分页参数") Page<SysDictItemVO> page,
            @Parameter(description = "字典项查询条件") SysDictItemVO itemVO) {
        return R.ok(sysDictItemService.pageDictItem(
            new Page<>(page.getCurrent(), page.getSize()),
            itemVO
        ));
    }

    /**
     * 查询字典项列表
     */
    @GetMapping("/list")
    @SaCheckPermission("sys:dict:item:list")
    @Operation(summary = "查询字典项列表", description = "根据条件查询字典项列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = List.class)))
    })
    public R<List<SysDictItemVO>> listDictItem(
            @Parameter(description = "字典项查询条件") SysDictItemVO itemVO) {
        return R.ok(sysDictItemService.listDictItem(itemVO));
    }

    /**
     * 根据ID获取字典项详情
     */
    @GetMapping("/{itemId}")
    @SaCheckPermission("sys:dict:item:list")
    @Operation(summary = "根据ID获取字典项详情", description = "根据字典项ID获取字典项详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SysDictItemVO.class))),
        @ApiResponse(responseCode = "404", description = "字典项不存在")
    })
    public R<SysDictItemVO> getDictItemById(
            @Parameter(description = "字典项ID", required = true)
            @PathVariable Long itemId) {
        return R.ok(sysDictItemService.getDictItemById(itemId));
    }

    /**
     * 根据字典类型获取字典项列表
     */
    @GetMapping("/type/{dictType}")
    @SaCheckPermission("sys:dict:item:list")
    @Operation(summary = "根据字典类型获取字典项列表", description = "根据字典类型获取启用的字典项列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = List.class)))
    })
    public R<List<SysDictItemVO>> listDictItemsByType(
            @Parameter(description = "字典类型", required = true)
            @PathVariable String dictType) {
        return R.ok(sysDictItemService.listDictItemsByType(dictType));
    }

    /**
     * 新增字典项
     */
    @PostMapping
    @SaCheckPermission("sys:dict:item:save")
    @Operation(summary = "新增字典项", description = "新增字典项")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "新增成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class)))
    })
    public R saveDictItem(
            @Parameter(description = "字典项信息", required = true)
            @RequestBody SysDictItemVO itemVO) {
        return R.ok(sysDictItemService.saveDictItem(itemVO));
    }

    /**
     * 修改字典项
     */
    @PutMapping
    @SaCheckPermission("sys:dict:item:update")
    @Operation(summary = "修改字典项", description = "修改字典项信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "修改成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class)))
    })
    public R updateDictItem(
            @Parameter(description = "字典项信息", required = true)
            @RequestBody SysDictItemVO itemVO) {
        return R.ok(sysDictItemService.updateDictItem(itemVO));
    }

    /**
     * 删除字典项
     */
    @DeleteMapping("/{itemId}")
    @SaCheckPermission("sys:dict:item:delete")
    @Operation(summary = "删除字典项", description = "根据字典项ID删除字典项")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class)))
    })
    public R deleteDictItem(
            @Parameter(description = "字典项ID", required = true)
            @PathVariable Long itemId) {
        return R.ok(sysDictItemService.deleteDictItem(itemId));
    }
}
