package com.lain.modules.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lain.modules.sys.entity.SysDict;
import com.lain.modules.sys.vo.SysDictVO;
import java.util.List;

public interface SysDictService extends IService<SysDict> {

    /**
     * 分页查询字典列表
     */
    Page<SysDictVO> pageDict(Page<SysDict> page, SysDictVO dictVO);

    /**
     * 查询字典列表
     */
    List<SysDictVO> listDict(SysDictVO dictVO);

    /**
     * 根据ID获取字典详情
     */
    SysDictVO getDictById(Long dictId);

    /**
     * 新增字典
     */
    boolean saveDict(SysDictVO dictVO);

    /**
     * 修改字典
     */
    boolean updateDict(SysDictVO dictVO);

    /**
     * 删除字典
     */
    boolean deleteDict(Long dictId);
}
