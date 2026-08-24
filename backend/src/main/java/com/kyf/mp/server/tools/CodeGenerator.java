package com.kyf.mp.server.tools;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;

public class CodeGenerator {

        public static void main(String[] args) {
                FastAutoGenerator.create(requiredEnvironment("GENERATOR_DB_URL"),
                                requiredEnvironment("GENERATOR_DB_USERNAME"),
                                requiredEnvironment("GENERATOR_DB_PASSWORD"))
                                .globalConfig(builder -> builder.author("kyf")
                                                .outputDir(System.getenv().getOrDefault("GENERATOR_OUTPUT_DIR",
                                                                "src/main/java")))
                                .packageConfig(builder -> builder.parent("com.kyf.mp.server")
                                                .entity("entity")
                                                .mapper("mapper")
                                                .service("service")
                                                .controller("controller"))
                                .strategyConfig(builder -> builder
                                                .addInclude("play_state", "playlists", "queue_items", "queues", "songs",
                                                                "songs_playlists_relation", "users",
                                                                "users_likeplaylists_relation",
                                                                "users_playlists_relation", "tags",
                                                                "songs_tags_relation", "comments",
                                                                "play_history")
                                                .entityBuilder()
                                                .enableLombok()
                                                .idType(IdType.ASSIGN_ID))
                                .execute();
        }

        private static String requiredEnvironment(String name) {
                String value = System.getenv(name);
                if (value == null || value.isBlank()) {
                        throw new IllegalStateException("Missing required environment variable: " + name);
                }
                return value;
        }
}