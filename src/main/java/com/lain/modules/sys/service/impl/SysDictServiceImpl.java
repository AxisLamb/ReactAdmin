// SysDictServiceImpl.java
package com.lain.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lain.modules.sys.dao.SysDictMapper;
import com.lain.modules.sys.entity.SysDict;
import com.lain.modules.sys.service.SysDictService;
import com.lain.modules.sys.vo.SysDictVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    @Override
    public Page<SysDictVO> pageDict(Page<SysDict> page, SysDictVO dictVO) {
        QueryWrapper<SysDict> queryWrapper = buildQueryWrapper(dictVO);
        Page<SysDict> resultPage = this.page(page, queryWrapper);

        Page<SysDictVO> voPage = new Page<>();
        BeanUtils.copyProperties(resultPage, voPage);
        List<SysDictVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<SysDictVO> listDict(SysDictVO dictVO) {
        QueryWrapper<SysDict> queryWrapper = buildQueryWrapper(dictVO);
        List<SysDict> dictList = this.list(queryWrapper);
        return dictList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public SysDictVO getDictById(Long dictId) {
        SysDict dict = this.getById(dictId);
        return dict != null ? convertToVO(dict) : null;
    }

    @Override
    public boolean saveDict(SysDictVO dictVO) {
        // 必填字段校验
        validateDict(dictVO);

        SysDict dict = convertToEntity(dictVO);
        return this.save(dict);
    }

    @Override
    public boolean updateDict(SysDictVO dictVO) {
        // 必填字段校验
        if (dictVO.getDictId() == null) {
            throw new IllegalArgumentException("字典ID不能为空");
        }
        validateDict(dictVO);

        SysDict dict = convertToEntity(dictVO);
        return this.updateById(dict);
    }

    @Override
    public boolean deleteDict(Long dictId) {
        if (dictId == null) {
            throw new IllegalArgumentException("字典ID不能为空");
        }
        return this.removeById(dictId);
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<SysDict> buildQueryWrapper(SysDictVO dictVO) {
        QueryWrapper<SysDict> queryWrapper = new QueryWrapper<>();
        if (dictVO != null) {
            if (StringUtils.hasText(dictVO.getDictName())) {
                queryWrapper.like("dict_name", dictVO.getDictName());
            }
            if (StringUtils.hasText(dictVO.getDictType())) {
                queryWrapper.like("dict_type", dictVO.getDictType());
            }
            if (dictVO.getStatus() != null) {
                queryWrapper.eq("status", dictVO.getStatus());
            }
        }
        return queryWrapper;
    }

    /**
     * 校验必填字段
     */
    private void validateDict(SysDictVO dictVO) {
        if (!StringUtils.hasText(dictVO.getDictName())) {
            throw new IllegalArgumentException("字典名称不能为空");
        }
        if (!StringUtils.hasText(dictVO.getDictType())) {
            throw new IllegalArgumentException("字典类型不能为空");
        }
    }

    /**
     * Entity转VO
     */
    private SysDictVO convertToVO(SysDict dict) {
        SysDictVO vo = new SysDictVO();
        BeanUtils.copyProperties(dict, vo);
        return vo;
    }

    /**
     * VO转Entity
     */
    private SysDict convertToEntity(SysDictVO vo) {
        SysDict dict = new SysDict();
        BeanUtils.copyProperties(vo, dict);
        return dict;
    }
}
