package com.kyf.mp.server.modules.song.service.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.modules.song.dto.EditSongDTO;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.mapper.SongsMapper;
import com.kyf.mp.server.modules.song.repository.impl.SongsRepositoryImpl;
import com.kyf.mp.server.modules.song.vo.GetSongsVO;
import com.kyf.mp.server.modules.song.vo.LyricsVO;
import com.kyf.mp.server.modules.user.mapper.UsersMapper;
import com.kyf.mp.server.modules.user.repository.impl.UsersRepositoryImpl;

@ExtendWith(MockitoExtension.class)
class SongsWorkflowTest {

    @Mock
    private SongsMapper songsMapper;
    @Mock
    private UsersMapper usersMapper;

    private SongsWorkflow songsWorkflow;

    @BeforeEach
    void setUp() {
        if (TableInfoHelper.getTableInfo(Songs.class) == null) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), Songs.class);
        }
        songsWorkflow = new SongsWorkflow(new UsersRepositoryImpl(usersMapper),
                new SongsRepositoryImpl(songsMapper));
        ReflectionTestUtils.setField(songsWorkflow, "songPageSize", 15);
    }

    @Test
    @DisplayName("歌曲存在且歌词为空时返回空字符串")
    void returnsEmptyLyricsForBlankLyrics() {
        Songs song = new Songs();
        song.setLyrics("  ");
        song.setTLyrics(null);
        when(songsMapper.selectOne(any(Wrapper.class))).thenReturn(song);

        LyricsVO result = songsWorkflow.getLyrics(1L);

        assertThat(result.getLyrics()).isEmpty();
        assertThat(result.getTLyrics()).isEmpty();
    }

    @Test
    @DisplayName("歌曲不存在时查询歌词抛出404")
    void rejectsMissingSongLyrics() {
        when(songsMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> songsWorkflow.getLyrics(1L));

        assertThat(exception.getCode()).isEqualTo(404);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {10, 50})
    @DisplayName("歌曲分页使用传入条数，未传时使用配置值")
    void returnsSongsPage(Integer size) {
        int expectedSize = size == null ? 15 : size;
        Songs song = new Songs();
        song.setSongId(8L);
        Page<Songs> page = new Page<>(2, expectedSize);
        page.setTotal(80);
        page.setRecords(List.of(song));
        when(songsMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        GetSongsVO result = songsWorkflow.getSongsPage(2, "hello", size);

        verify(songsMapper).selectPage(argThat((Page<Songs> query) ->
                query.getCurrent() == 2 && query.getSize() == expectedSize), any(Wrapper.class));
        assertThat(result.getSongs()).containsExactly(song);
        assertThat(result.getPagination().getTotalItems()).isEqualTo(80);
        assertThat(result.getPagination().getCurrentPage()).isEqualTo(2);
        assertThat(result.getPagination().getPageLimit()).isEqualTo(expectedSize);
    }

    @Test
    @DisplayName("编辑歌曲时没有修改字段则不更新数据库")
    void doesNotUpdateSongWhenNothingChanged() {
        Songs oldSong = new Songs();
        oldSong.setSongId(8L);
        oldSong.setUploaderId(2L);
        when(songsMapper.selectById(8L)).thenReturn(oldSong);

        songsWorkflow.editUploadSong(new EditSongDTO(), 2L, 8L);

        verify(songsMapper, never()).updateById(any(Songs.class));
    }

    @Test
    @DisplayName("编辑歌曲时上传者不匹配抛出403")
    void rejectsEditByAnotherUser() {
        Songs oldSong = new Songs();
        oldSong.setSongId(8L);
        oldSong.setUploaderId(2L);
        when(songsMapper.selectById(8L)).thenReturn(oldSong);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> songsWorkflow.editUploadSong(new EditSongDTO(), 3L, 8L));

        assertThat(exception.getCode()).isEqualTo(403);
    }
}
