// SysDictItemService.java
package com.lain.modules.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lain.modules.sys.entity.SysDictItem;
import com.lain.modules.sys.vo.SysDictItemVO;
import java.util.List;

public interface SysDictItemService extends IService<SysDictItem> {

    /**
     * 分页查询字典项列表
     */
    Page<SysDictItemVO> pageDictItem(Page<SysDictItem> page, SysDictItemVO itemVO);

    /**
     * 查询字典项列表
     */
    List<SysDictItemVO> listDictItem(SysDictItemVO itemVO);

    /**
     * 根据ID获取字典项详情
     */
    SysDictItemVO getDictItemById(Long itemId);

    /**
     * 根据字典类型获取字典项列表
     */
    List<SysDictItemVO> listDictItemsByType(String dictType);

    /**
     * 新增字典项
     */
    boolean saveDictItem(SysDictItemVO itemVO);

    /**
     * 修改字典项
     */
    boolean updateDictItem(SysDictItemVO itemVO);

    /**
     * 删除字典项
     */
    boolean deleteDictItem(Long itemId);
}
