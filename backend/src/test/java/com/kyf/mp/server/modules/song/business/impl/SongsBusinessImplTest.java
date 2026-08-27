package com.kyf.mp.server.modules.song.business.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.modules.song.dto.EditSongDTO;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.mapper.SongsMapper;
import com.kyf.mp.server.modules.song.vo.GetSongsVO;
import com.kyf.mp.server.modules.song.vo.LyricsVO;
import com.kyf.mp.server.modules.user.mapper.UsersMapper;

@ExtendWith(MockitoExtension.class)
class SongsBusinessImplTest {

    @Mock
    private SongsMapper songsMapper;
    @Mock
    private UsersMapper usersMapper;

    private SongsBusinessImpl songsBusiness;

    @BeforeEach
    void setUp() {
        if (TableInfoHelper.getTableInfo(Songs.class) == null) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), Songs.class);
        }
        songsBusiness = new SongsBusinessImpl(usersMapper);
        ReflectionTestUtils.setField(songsBusiness, "baseMapper", songsMapper);
        ReflectionTestUtils.setField(songsBusiness, "songPageSize", 15);
    }

    @Test
    @DisplayName("歌曲存在且歌词为空时返回空字符串")
    void returnsEmptyLyricsForBlankLyrics() {
        Songs song = new Songs();
        song.setLyrics("  ");
        song.setTLyrics(null);
        when(songsMapper.selectOne(any(Wrapper.class))).thenReturn(song);

        LyricsVO result = songsBusiness.getLyrics(1L);

        assertThat(result.getLyrics()).isEmpty();
        assertThat(result.getTLyrics()).isEmpty();
    }

    @Test
    @DisplayName("歌曲不存在时查询歌词抛出404")
    void rejectsMissingSongLyrics() {
        when(songsMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> songsBusiness.getLyrics(1L));

        assertThat(exception.getCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("分页查询歌曲时返回歌曲和分页信息")
    void returnsSongsPage() {
        Songs song = new Songs();
        song.setSongId(8L);
        Page<Songs> page = new Page<>(2, 15);
        page.setTotal(16);
        page.setRecords(List.of(song));
        when(songsMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        GetSongsVO result = songsBusiness.getSongsPage(2, "hello");

        assertThat(result.getSongs()).containsExactly(song);
        assertThat(result.getPagination().getTotalItems()).isEqualTo(16);
        assertThat(result.getPagination().getCurrentPage()).isEqualTo(2);
        assertThat(result.getPagination().getPageLimit()).isEqualTo(15);
    }

    @Test
    @DisplayName("编辑歌曲时没有修改字段则不更新数据库")
    void doesNotUpdateSongWhenNothingChanged() {
        Songs oldSong = new Songs();
        oldSong.setSongId(8L);
        oldSong.setUploaderId(2L);
        when(songsMapper.selectById(8L)).thenReturn(oldSong);

        songsBusiness.editUploadSong(new EditSongDTO(), 2L, 8L);

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
                () -> songsBusiness.editUploadSong(new EditSongDTO(), 3L, 8L));

        assertThat(exception.getCode()).isEqualTo(403);
    }
}
