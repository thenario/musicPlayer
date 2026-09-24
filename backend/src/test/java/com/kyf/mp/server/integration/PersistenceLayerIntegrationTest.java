package com.kyf.mp.server.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.modules.playlist.service.PlaylistsService;
import com.kyf.mp.server.modules.song.dto.EditSongDTO;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.service.SongsService;

/** 使用真实 Service、事务代理、Repository 和 Mapper 验证分层迁移后的行为。 */
@SpringBootTest(properties = {
        "spring.cache.type=simple",
        "spring.datasource.url=jdbc:h2:mem:layers;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "song.page-size=1"
})
@ActiveProfiles("test")
class PersistenceLayerIntegrationTest {
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private SongsService songsService;
    @Autowired
    private PlaylistsService playlistsService;
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void prepareData() {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS songs (
                    song_id BIGINT PRIMARY KEY, song_title VARCHAR(100), artist VARCHAR(100),
                    file_md5 VARCHAR(100), album VARCHAR(100), file_size BIGINT, uploader_id BIGINT,
                    uploader_name VARCHAR(100), duration INT, bitrate INT, song_cover_url VARCHAR(255),
                    song_url VARCHAR(255), date_added TIMESTAMP, file_format VARCHAR(20),
                    lyrics VARCHAR(1000), t_lyrics VARCHAR(1000)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS playlists (
                    playlist_id BIGINT PRIMARY KEY, playlist_name VARCHAR(100), creator_id BIGINT,
                    created_date TIMESTAMP, updated_date TIMESTAMP, playlist_cover_url VARCHAR(255),
                    song_count INT, like_count INT, play_count INT,
                    is_public BOOLEAN, description VARCHAR(255)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS songs_playlists_relation (
                    playlist_id BIGINT, song_id BIGINT, song_playlist_position INT,
                    PRIMARY KEY (playlist_id, song_id)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS play_history (
                    history_id BIGINT PRIMARY KEY, user_id BIGINT, song_id BIGINT, played_date TIMESTAMP
                )
                """);
        jdbc.update("DELETE FROM songs_playlists_relation");
        jdbc.update("DELETE FROM playlists");
        jdbc.update("DELETE FROM play_history");
        jdbc.update("DELETE FROM songs");
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        for (int i = 1; i <= 3; i++) {
            jdbc.update("""
                    INSERT INTO songs (song_id, song_title, uploader_id, date_added, lyrics, t_lyrics)
                    VALUES (?, ?, 7, ?, 'old lyrics', 'translation')
                    """, i, i == 3 ? "other" : "match-" + i, LocalDateTime.of(2026, 1, i, 0, 0));
        }
        jdbc.update("""
                INSERT INTO playlists (playlist_id, playlist_name, creator_id, song_count, is_public)
                VALUES (10, 'test', 7, 0, TRUE)
                """);
    }

    @Test
    void searchPreservesFilteringOrderingPaginationAndExcludedLyrics() {
        var first = songsService.getSongsPage(1, "match");
        assertThat(first.getPagination().getTotalItems()).isEqualTo(2);
        assertThat(first.getSongs()).extracting(Songs::getSongId).containsExactly(2L);
        assertThat(first.getSongs().get(0).getLyrics()).isNull();
        assertThat(first.getSongs().get(0).getTLyrics()).isNull();
        assertThat(songsService.getSongsPage(2, "match").getSongs())
                .extracting(Songs::getSongId).containsExactly(1L);
    }

    @Test
    void successfulEditEvictsLyricsAndRejectedEditPreservesCachedValue() {
        var cached = songsService.getLyrics(1L);
        var edit = new EditSongDTO();
        edit.setLyrics("new lyrics");

        assertThatThrownBy(() -> songsService.editUploadSong(edit, 99L, 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo(403));
        assertThat(cacheManager.getCache("song-lyrics").get(1L).get()).isSameAs(cached);
        assertThat(jdbc.queryForObject("SELECT lyrics FROM songs WHERE song_id = 1", String.class))
                .isEqualTo("old lyrics");

        songsService.editUploadSong(edit, 7L, 1L);
        assertThat(cacheManager.getCache("song-lyrics").get(1L)).isNull();
        assertThat(songsService.getLyrics(1L).getLyrics()).isEqualTo("new lyrics");
    }

    @Test
    void playlistRemovalCommitsAndRollsBackTogetherWithoutEvictingOnFailure() {
        jdbc.update("INSERT INTO songs_playlists_relation VALUES (10, 1, 1)");
        jdbc.update("UPDATE playlists SET song_count = 1 WHERE playlist_id = 10");
        playlistsService.removeSongFromPlaylist(10L, 1L, 7L);
        assertThat(jdbc.queryForObject("SELECT song_count FROM playlists WHERE playlist_id = 10", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM songs_playlists_relation", Integer.class)).isZero();

        jdbc.update("INSERT INTO songs_playlists_relation VALUES (10, 1, 1)");
        jdbc.update("UPDATE playlists SET song_count = 1 WHERE playlist_id = 10");
        var cache = cacheManager.getCache("playlist-detail");
        cache.put(10L, "existing cached content");
        // 测试专用约束让数量更新失败，检查之前的关系删除是否回滚。
        jdbc.execute("ALTER TABLE playlists ADD CONSTRAINT fail_decrement CHECK (song_count >= 1)");
        try {
            assertThatThrownBy(() -> playlistsService.removeSongFromPlaylist(10L, 1L, 7L))
                    .isInstanceOf(DataIntegrityViolationException.class);

            assertThat(jdbc.queryForList("SELECT song_id FROM songs_playlists_relation WHERE playlist_id = 10", Long.class))
                    .containsExactly(1L);
            assertThat(jdbc.queryForObject("SELECT song_count FROM playlists WHERE playlist_id = 10", Integer.class))
                    .isEqualTo(1);
            assertThat(cache.get(10L).get()).isEqualTo("existing cached content");
        } finally {
            jdbc.execute("ALTER TABLE playlists DROP CONSTRAINT fail_decrement");
        }
    }

    @Test
    void historyStillUsesXmlJoinAndNestedResultMapping() {
        jdbc.update("INSERT INTO play_history VALUES (11, 7, 1, '2026-01-01 00:00:00')");
        jdbc.update("INSERT INTO play_history VALUES (12, 7, 2, '2026-01-02 00:00:00')");
        jdbc.update("INSERT INTO play_history VALUES (13, 99, 3, '2026-01-03 00:00:00')");

        var history = songsService.getPlayHistory(7L);
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getSong().getSongId()).isEqualTo(2L);
        assertThat(history.get(0).getSong().getSongTitle()).isEqualTo("match-2");
        assertThat(history.get(0).getPlayTime()).isEqualTo(LocalDateTime.of(2026, 1, 2, 0, 0));
        assertThat(history.get(1).getSong().getSongId()).isEqualTo(1L);
    }
}
