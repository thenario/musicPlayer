package com.kyf.mp.server.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI musicPlayerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("音乐播放器 API")
                        .version("v1.0.0")
                        .description("""
                                    音乐播放器后端接口文档。

                                    需要登录的接口：
                                    1. 先调用登录接口获取 JWT。
                                    2. 点击右上角 Authorize。
                                    3. 输入 Bearer Token 后再测试接口。
                                """))
                .servers(List.of(new Server().url("/").description("当前环境")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT").description("登录成功后获得的 JWT")));
    }
}
