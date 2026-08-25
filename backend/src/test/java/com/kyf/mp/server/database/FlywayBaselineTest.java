package com.kyf.mp.server.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class FlywayBaselineTest {

    @Test
    @DisplayName("初始迁移脚本不应包含破坏性数据库操作")
    void initialMigrationIsNonDestructive() throws IOException {
        try (var input = getClass().getResourceAsStream("/db/migration/V1__initial_schema.sql")) {
            assertThat(input).isNotNull();
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8).toUpperCase();

            assertThat(sql).contains("CREATE TABLE `USERS`");
            assertThat(sql).doesNotContain("DROP TABLE");
            assertThat(sql).doesNotContain("CREATE DATABASE");
            assertThat(sql).doesNotContain("\nUSE ");
        }
    }
}
