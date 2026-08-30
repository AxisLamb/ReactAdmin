package com.lain.modules.xianyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lain.modules.xianyu.entity.XianyuItem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface XianyuItemMapper extends BaseMapper<XianyuItem> {
}
