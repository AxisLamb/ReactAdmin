package com.lain.modules.oss.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lain.modules.oss.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface FileInfoMapper extends BaseMapper<FileInfo> {

}
