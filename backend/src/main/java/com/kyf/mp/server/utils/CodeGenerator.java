package com.kyf.mp.server.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.annotation.IdType;

public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/musicPlayer", "root", "[REDACTED]")
                .globalConfig(builder -> {
                    builder.author("kyf")
                            .outputDir("Z:\\vue3_projects\\vue_musicplayer\\backend\\src\\main\\java"); // 输出目录，选到java这一层
                })
                .packageConfig(builder -> {
                    builder.parent("com.kyf.mp.server")
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .controller("controller");
                })
                .strategyConfig(builder -> {
                    builder.addInclude("play_state", "playlists", "queue_items", "queues", "songs",
                            "songs_playlists_relation", "users", "users_likeplaylists_relation",
                            "users_playlists_relation", "tags", "songs_tags_relation", "comments",
                            "play_history") // 填入你需要生成的表名，多个用逗号隔开
                            .entityBuilder()
                            .enableLombok() // 开启 Lombok 模式，省去 Getter/Setter
                            .idType(IdType.ASSIGN_ID); // 雪花ID，应用层分配
                })
                .execute();
    }
}