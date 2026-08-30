package com.lain.modules.xianyu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lain.modules.xianyu.entity.XianyuChatBargain;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface XianyuChatBargainMapper extends BaseMapper<XianyuChatBargain> {
}
