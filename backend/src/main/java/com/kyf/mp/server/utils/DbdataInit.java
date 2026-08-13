package com.kyf.mp.server.utils;


import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.kyf.mp.server.ServerApplication;

import cn.hutool.core.lang.Snowflake;
import jakarta.annotation.Resource;

public class DbdataInit {
    @Resource
    Snowflake snowFlake;
    public static void main(String[] args){
        ApplicationContext context = loadEnvironment();
        
    }

    public static ApplicationContext loadEnvironment(){
        return SpringApplication.run(ServerApplication.class,new String[0]);
    }


}
