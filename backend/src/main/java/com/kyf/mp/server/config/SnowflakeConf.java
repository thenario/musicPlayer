package com.kyf.mp.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

@Configuration
public class SnowflakeConf {
    @Bean
    public Snowflake generateSnowFlake(){
        return IdUtil.getSnowflake(1,1);
    }
}
