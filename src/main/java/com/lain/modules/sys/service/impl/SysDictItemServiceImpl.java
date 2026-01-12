// SysDictItemServiceImpl.java
package com.lain.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lain.modules.sys.dao.SysDictItemMapper;
import com.lain.modules.sys.entity.SysDictItem;
import com.lain.modules.sys.service.SysDictItemService;
import com.lain.modules.sys.vo.SysDictItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

    @Override
    public Page<SysDictItemVO> pageDictItem(Page<SysDictItem> page, SysDictItemVO itemVO) {
        QueryWrapper<SysDictItem> queryWrapper = buildQueryWrapper(itemVO);
        queryWrapper.orderByAsc("order_num");
        Page<SysDictItem> resultPage = this.page(page, queryWrapper);

        Page<SysDictItemVO> voPage = new Page<>();
        BeanUtils.copyProperties(resultPage, voPage);
        List<SysDictItemVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<SysDictItemVO> listDictItem(SysDictItemVO itemVO) {
        QueryWrapper<SysDictItem> queryWrapper = buildQueryWrapper(itemVO);
        queryWrapper.orderByAsc("order_num");
        List<SysDictItem> itemList = this.list(queryWrapper);
        return itemList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public SysDictItemVO getDictItemById(Long itemId) {
        SysDictItem item = this.getById(itemId);
        return item != null ? convertToVO(item) : null;
    }

    @Override
    public List<SysDictItemVO> listDictItemsByType(String dictType) {
        // 这里需要关联查询sys_dict表获取dict_id
        // 实际实现中需要注入SysDictService来查询
        Long dictId = getDictIdByType(dictType);
        return this.list(new QueryWrapper<SysDictItem>()
                .eq("dict_id", dictId)
                .eq("status", 1)
                .orderByAsc("order_num"))
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean saveDictItem(SysDictItemVO itemVO) {
        // 必填字段校验
        validateDictItem(itemVO);

        SysDictItem item = convertToEntity(itemVO);
        return this.save(item);
    }

    @Override
    public boolean updateDictItem(SysDictItemVO itemVO) {
        // 必填字段校验
        if (itemVO.getItemId() == null) {
            throw new IllegalArgumentException("字典项ID不能为空");
        }
        validateDictItem(itemVO);

        SysDictItem item = convertToEntity(itemVO);
        return this.updateById(item);
    }

    @Override
    public boolean deleteDictItem(Long itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("字典项ID不能为空");
        }
        return this.removeById(itemId);
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<SysDictItem> buildQueryWrapper(SysDictItemVO itemVO) {
        QueryWrapper<SysDictItem> queryWrapper = new QueryWrapper<>();
        if (itemVO != null) {
            if (itemVO.getDictId() != null) {
                queryWrapper.eq("dict_id", itemVO.getDictId());
            }
            if (StringUtils.hasText(itemVO.getItemLabel())) {
                queryWrapper.like("item_label", itemVO.getItemLabel());
            }
            if (itemVO.getStatus() != null) {
                queryWrapper.eq("status", itemVO.getStatus());
            }
        }
        return queryWrapper;
    }

    /**
     * 校验必填字段
     */
    private void validateDictItem(SysDictItemVO itemVO) {
        if (itemVO.getDictId() == null) {
            throw new IllegalArgumentException("字典ID不能为空");
        }
        if (!StringUtils.hasText(itemVO.getItemLabel())) {
            throw new IllegalArgumentException("字典项标签不能为空");
        }
        if (!StringUtils.hasText(itemVO.getItemValue())) {
            throw new IllegalArgumentException("字典项值不能为空");
        }
    }

    /**
     * 根据字典类型获取字典ID
     */
    private Long getDictIdByType(String dictType) {
        // 实际实现中需要注入SysDictService来查询
        return 1L; // 示例返回值
    }

    /**
     * Entity转VO
     */
    private SysDictItemVO convertToVO(SysDictItem item) {
        SysDictItemVO vo = new SysDictItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    /**
     * VO转Entity
     */
    private SysDictItem convertToEntity(SysDictItemVO vo) {
        SysDictItem item = new SysDictItem();
        BeanUtils.copyProperties(vo, item);
        return item;
    }
}
