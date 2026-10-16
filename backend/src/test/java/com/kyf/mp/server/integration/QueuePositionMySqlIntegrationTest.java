package com.kyf.mp.server.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;

import com.kyf.mp.server.modules.queue.dto.AddSongToQueueDTO;
import com.kyf.mp.server.modules.queue.mapper.QueueCustomMapper;
import com.kyf.mp.server.modules.queue.service.workflow.QueuesWorkflow;

/** 使用 MySQL 的逐行唯一键检查验证队列移位，H2 无法替代这项验证。 */
@SpringBootTest(properties = { "spring.cache.type=simple", "spring.flyway.enabled=true" })
@ActiveProfiles("test")
class QueuePositionMySqlIntegrationTest {
    private static MySQLContainer<?> mysql;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        // 无 Docker 时可指定专用测试库；测试会清空该库中的队列和歌曲。
        String jdbcUrl = System.getProperty("queue.test.jdbc-url");
        if (jdbcUrl == null) {
            mysql = new MySQLContainer<>("mysql:8.0");
            mysql.start();
            registry.add("spring.datasource.url", mysql::getJdbcUrl);
            registry.add("spring.datasource.username", mysql::getUsername);
            registry.add("spring.datasource.password", mysql::getPassword);
        } else {
            registry.add("spring.datasource.url", () -> jdbcUrl);
            registry.add("spring.datasource.username", () -> System.getProperty("queue.test.username", "root"));
            registry.add("spring.datasource.password", () -> System.getProperty("queue.test.password", ""));
        }
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @AfterAll
    static void stopDatabase() {
        if (mysql != null) {
            mysql.stop();
        }
    }

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private QueuesWorkflow workflow;
    @Autowired
    private QueueCustomMapper mapper;
    @Autowired
    private TransactionTemplate transaction;

    @BeforeEach
    void prepareQueue() {
        jdbc.update("DELETE FROM play_state");
        jdbc.update("DELETE FROM queue_items");
        jdbc.update("DELETE FROM queues");
        jdbc.update("DELETE FROM songs");
        jdbc.update("DELETE FROM users");
        jdbc.update("INSERT INTO users (user_id, user_name, user_email, password) "
                + "VALUES (7, 'queue-test', 'queue-test@example.com', 'test')");
        jdbc.update("INSERT INTO queues (queue_id, queue_name, creator_id, song_count) VALUES (10, 'test', 7, 4)");
        for (int songId = 1; songId <= 5; songId++) {
            jdbc.update("INSERT INTO songs (song_id, song_title, uploader_id, uploader_name, song_url, file_format) "
                    + "VALUES (?, ?, 7, 'test', '/test.mp3', 'mp3')", songId, "song-" + songId);
        }
        // 主键顺序与播放顺序不一致，模拟经历过重排/删除重插的队列。
        jdbc.update("INSERT INTO queue_items (queue_item_id, queue_id, song_id, queue_item_position) "
                + "VALUES (10, 10, 1, 3), (20, 10, 2, 1), (30, 10, 3, 4), (40, 10, 4, 2)");
        jdbc.update("INSERT INTO play_state (user_id, current_queue_id, current_song_id, current_position) "
                + "VALUES (7, 10, 1, 3)");
    }

    @ParameterizedTest
    @CsvSource({
            "true, 2, '4,1,2,3', 3", "false, 2, '4,1,2,3', 2",
            "true, 4, '2,1,4,3', 3", "false, 4, '2,1,4,3', 2",
            "true, 1, '2,4,1,3', 3", "false, 1, '2,4,1,3', 3",
            "true, 3, '2,4,1,3', 4", "false, 3, '2,4,1,3', 3"
    })
    void reinsertingExistingSongPreservesUniquePositionsAndPlayback(boolean immediate, long songId,
            String expectedOrder, int currentPosition) {
        var dto = new AddSongToQueueDTO();
        dto.setSongId(songId);
        dto.setMode(immediate);

        var result = workflow.addSongToQueue(7L, 10L, dto);

        assertThat(orderedSongIds()).containsExactlyElementsOf(
                java.util.Arrays.stream(expectedOrder.split(",")).map(Long::valueOf).toList());
        assertThat(positions()).containsExactly(1, 2, 3, 4);
        assertThat(result.getQueueItemPosition()).isEqualTo(songId == 3 ? 4 : 3);
        assertThat(jdbc.queryForObject("SELECT song_count FROM queues WHERE queue_id = 10", Integer.class))
                .isEqualTo(4);
        assertThat(jdbc.queryForObject("SELECT current_song_id FROM play_state WHERE user_id = 7", Long.class))
                .isEqualTo(immediate ? songId : 1L);
        assertThat(jdbc.queryForObject("SELECT current_position FROM play_state WHERE user_id = 7", Integer.class))
                .isEqualTo(currentPosition);
    }

    @Test
    void removingItemClosesGapInPositionOrder() {
        workflow.removeSongFromQueue(7L, 10L, 20L);

        assertThat(orderedSongIds()).containsExactly(4L, 1L, 3L);
        assertThat(positions()).containsExactly(1, 2, 3);
        assertThat(jdbc.queryForObject("SELECT current_position FROM play_state WHERE user_id = 7", Integer.class))
                .isEqualTo(2);
    }

    @Test
    void reinsertingSongDoesNotDependOnOptimizerChoosingPositionIndex() {
        // 隐藏位置索引以覆盖其他扫描顺序；唯一约束仍然生效。
        jdbc.execute("ALTER TABLE queue_items ALTER INDEX uq_queue_item_position INVISIBLE, "
                + "ALTER INDEX idx_queue_position INVISIBLE");
        try {
            var dto = new AddSongToQueueDTO();
            dto.setSongId(2L);
            dto.setMode(true);

            workflow.addSongToQueue(7L, 10L, dto);

            assertThat(orderedSongIds()).containsExactly(4L, 1L, 2L, 3L);
            assertThat(positions()).containsExactly(1, 2, 3, 4);
        } finally {
            jdbc.execute("ALTER TABLE queue_items ALTER INDEX uq_queue_item_position VISIBLE, "
                    + "ALTER INDEX idx_queue_position VISIBLE");
        }
    }

    @Test
    void temporaryPositionsDoNotCollideWithLargePositivePositions() {
        prepareLargePositions();

        transaction.executeWithoutResult(status -> {
            mapper.moveItemPositionsToTemporary(10L, 1);
            assertThat(positions()).containsExactly(-2147483646, -1000001, -1);
            jdbc.update("INSERT INTO queue_items (queue_item_id, queue_id, song_id, queue_item_position) "
                    + "VALUES (99, 10, 5, 1)");
            mapper.restoreShiftedItemPositions(10L, 1);
        });

        assertThat(positions()).containsExactly(1, 2, 1000002, Integer.MAX_VALUE);
        assertOtherQueueUnchanged();
    }

    @Test
    void reorderUsesDisjointTemporaryPositionsAndUpdatesPlayback() {
        prepareLargePositions();

        workflow.reorderQueue(7L, 10L, List.of(3L, 1L, 2L));

        assertThat(orderedSongIds()).containsExactly(3L, 1L, 2L);
        assertThat(positions()).containsExactly(1, 2, 3);
        assertThat(jdbc.queryForObject("SELECT current_position FROM play_state WHERE user_id = 7", Integer.class))
                .isEqualTo(2);
        assertOtherQueueUnchanged();
    }

    @Test
    void failedInsertRollsBackTemporaryPositions() {
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            mapper.moveItemPositionsToTemporary(10L, 1);
            jdbc.update("INSERT INTO queue_items (queue_item_id, queue_id, song_id, queue_item_position) "
                    + "VALUES (99, 10, 1, 1)");
        })).isInstanceOf(DuplicateKeyException.class);

        assertThat(orderedSongIds()).containsExactly(2L, 4L, 1L, 3L);
        assertThat(positions()).containsExactly(1, 2, 3, 4);
    }

    private void prepareLargePositions() {
        jdbc.update("DELETE FROM queue_items WHERE queue_id = 10");
        jdbc.update("INSERT INTO queue_items (queue_item_id, queue_id, song_id, queue_item_position) "
                + "VALUES (10, 10, 1, 1), (20, 10, 2, 1000001), (30, 10, 3, 2147483646)");
        jdbc.update("INSERT INTO queues (queue_id, queue_name, creator_id, song_count) VALUES (11, 'other', 7, 1)");
        jdbc.update("INSERT INTO queue_items (queue_item_id, queue_id, song_id, queue_item_position) "
                + "VALUES (50, 11, 1, 1)");
    }

    private void assertOtherQueueUnchanged() {
        assertThat(jdbc.queryForObject("SELECT queue_item_position FROM queue_items WHERE queue_id = 11", Integer.class))
                .isEqualTo(1);
    }

    private List<Long> orderedSongIds() {
        return jdbc.queryForList("SELECT song_id FROM queue_items WHERE queue_id = 10 ORDER BY queue_item_position", Long.class);
    }

    private List<Integer> positions() {
        return jdbc.queryForList("SELECT queue_item_position FROM queue_items WHERE queue_id = 10 ORDER BY queue_item_position", Integer.class);
    }
}
