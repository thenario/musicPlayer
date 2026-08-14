package com.kyf.mp.server.tools;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * MyBatis-Plus 代码生成器（开发工具，非运行时组件）。
 * 用法：修改下方数据库连接、输出目录、作者、表名后，直接运行 main 方法。
 */
public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/musicPlayer", "root", "你的密码")
                .globalConfig(builder -> {
                    builder.author("your-name")
                            .outputDir("输出目录，选到 java 这一层"); // 例如 /path/to/backend/src/main/java
                })
                .packageConfig(builder -> {
                    builder.parent("com.kyf.mp.server")
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .controller("controller");
                })
                .strategyConfig(builder -> {
                    builder.addInclude("users") // 填入需要生成的表名，多个用逗号隔开
                            .entityBuilder()
                            .enableLombok()
                            .idType(IdType.ASSIGN_ID); // 雪花ID，应用层分配
                })
                .execute();
    }
}
