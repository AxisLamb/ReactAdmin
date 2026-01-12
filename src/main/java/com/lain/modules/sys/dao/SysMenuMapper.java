package com.lain.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lain.modules.sys.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}