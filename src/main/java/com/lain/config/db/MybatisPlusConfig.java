package com.lain.config.db;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.lain.modules.*.dao")
public class MybatisPlusConfig {

}
