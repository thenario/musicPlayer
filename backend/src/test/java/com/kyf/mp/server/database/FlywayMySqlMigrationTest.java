package com.kyf.mp.server.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class FlywayMySqlMigrationTest {
    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("musicPlayer")
            .withUsername("test")
            .withPassword("test");

    @Test
    @DisplayName("空 MySQL 数据库应迁移至最新版本")
    void migratesEmptyMySqlDatabaseToLatestSchema() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .locations("classpath:db/migration")
                .load();

        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(8);

        try (Connection connection = MYSQL.createConnection("");
                ResultSet history = connection.createStatement()
                        .executeQuery("SELECT MAX(version) FROM flyway_schema_history WHERE success = 1")) {
            assertThat(history.next()).isTrue();
            assertThat(history.getString(1)).isEqualTo("8");
        }
    }
}
