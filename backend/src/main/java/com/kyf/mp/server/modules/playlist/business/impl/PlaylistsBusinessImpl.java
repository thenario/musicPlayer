package com.kyf.mp.server.modules.playlist.business.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.common.file.UploadFileValidator;
import com.kyf.mp.server.common.file.StoragePathResolver;
import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.playlist.business.PlaylistsBusiness;
import com.kyf.mp.server.modules.playlist.entity.Playlists;
import com.kyf.mp.server.modules.playlist.entity.SongsPlaylistsRelation;
import com.kyf.mp.server.modules.playlist.entity.UsersLikeplaylistsRelation;
import com.kyf.mp.server.modules.playlist.entity.UsersPlaylistsRelation;
import com.kyf.mp.server.modules.playlist.mapper.PlaylistsMapper;
import com.kyf.mp.server.modules.playlist.mapper.SongsPlaylistsRelationMapper;
import com.kyf.mp.server.modules.playlist.mapper.UsersLikeplaylistsRelationMapper;
import com.kyf.mp.server.modules.playlist.mapper.UsersPlaylistsRelationMapper;
import com.kyf.mp.server.modules.playlist.vo.AddSongToPlaylistVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistActionVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistContentVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistDetailVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistSongVO;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.mapper.SongsMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 歌单数据访问实现：复杂数据库操作。`r`n
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PlaylistsBusinessImpl extends BaseBusinessImpl<PlaylistsMapper, Playlists>
        implements PlaylistsBusiness {

    @Value("${file.upload.playlist-cover-path}")
    private String playlistCoverPath;

    @Value("${file.static.playlist-cover-url}")
    private String coverUrlPrefix;

    private final SongsMapper songsMapper;
    private final UsersPlaylistsRelationMapper userPlaylistMapper;
    private final UsersLikeplaylistsRelationMapper likeRelationMapper;
    private final SongsPlaylistsRelationMapper songsPlaylistsRelationMapper;

    private static final String PLAYLIST_ID_COLUMN = "playlist_id";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlaylistActionVO createPlaylist(MultipartFile file, String name, String description,
            Long userId) {
        File savedFile = null;
        try {
            String extension = UploadFileValidator.validateImage(file);
            String newFileName = UUID.randomUUID() + "-" + System.currentTimeMillis() + "." + extension;

            savedFile = StoragePathResolver.resolveFile(playlistCoverPath, newFileName);
            if (!savedFile.getParentFile().exists()) {
                Files.createDirectories(savedFile.getParentFile().toPath());
            }

            file.transferTo(savedFile);

            Playlists playlist = new Playlists();
            playlist.setPlaylistName(name);
            playlist.setCreatorId(userId);
            String coverUrl = coverUrlPrefix + newFileName;
            playlist.setPlaylistCoverUrl(coverUrl);
            playlist.setDescription(StringUtils.hasText(description) ? description : "");
            playlist.setSongCount(0);
            playlist.setLikeCount(0);
            playlist.setPlayCount(0);
            playlist.setPubliclyVisible(true);

            this.save(playlist);

            UsersPlaylistsRelation relation = new UsersPlaylistsRelation();
            relation.setUserId(userId);
            relation.setPlaylistId(playlist.getPlaylistId());
            userPlaylistMapper.insert(relation);

            PlaylistActionVO data = new PlaylistActionVO();
            data.setCoverUrl(coverUrl);
            data.setPlaylistId(playlist.getPlaylistId());

            return data;

        } catch (BusinessException e) {
            cleanupNewPlaylistCover(savedFile);
            throw e;
        } catch (Exception e) {
            cleanupNewPlaylistCover(savedFile);
            log.error("创建歌单失败", e);
            throw new BusinessException(500, "创建歌单失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlaylistActionVO editPlaylist(MultipartFile file, Long playlistId, String name,
            String description, Long userId) {
        File newSavedFile = null;
        try {
            Playlists oldPlaylist = getEditablePlaylist(playlistId, userId);
            CoverUpload coverUpload = saveNewCover(file);
            newSavedFile = coverUpload == null ? null : coverUpload.file();

            Playlists updateEntity = buildPlaylistUpdate(playlistId, name, description, coverUpload);
            baseMapper.updateById(updateEntity);
            cleanupReplacedCover(oldPlaylist.getPlaylistCoverUrl(), coverUpload);
            return buildEditResult(playlistId, oldPlaylist, updateEntity, coverUpload);
        } catch (BusinessException exception) {
            cleanupNewPlaylistCover(newSavedFile);
            throw exception;
        } catch (Exception exception) {
            cleanupNewPlaylistCover(newSavedFile);
            throw new BusinessException(500, "编辑失败");
        }
    }

    private Playlists getEditablePlaylist(Long playlistId, Long userId) {
        Playlists playlist = baseMapper.selectById(playlistId);
        if (playlist == null) {
            throw new BusinessException(404, "歌单不存在");
        }
        if (!playlist.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "无权修改此歌单");
        }
        return playlist;
    }

    private CoverUpload saveNewCover(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String extension = UploadFileValidator.validateImage(file);
        String fileName = UUID.randomUUID() + "-" + System.currentTimeMillis() + "." + extension;
        File savedFile = StoragePathResolver.resolveFile(playlistCoverPath, fileName);
        Files.createDirectories(savedFile.getParentFile().toPath());
        file.transferTo(savedFile);
        return new CoverUpload(fileName, savedFile);
    }

    private Playlists buildPlaylistUpdate(Long playlistId, String name, String description,
            CoverUpload coverUpload) {
        Playlists updateEntity = new Playlists();
        updateEntity.setPlaylistId(playlistId);
        if (StringUtils.hasText(name)) {
            updateEntity.setPlaylistName(name);
        }
        if (description != null) {
            updateEntity.setDescription(description);
        }
        if (coverUpload != null) {
            updateEntity.setPlaylistCoverUrl(coverUrlPrefix + coverUpload.fileName());
        }
        updateEntity.setUpdatedDate(LocalDateTime.now(ZoneId.systemDefault()));
        return updateEntity;
    }

    private void cleanupReplacedCover(String oldCoverUrl, CoverUpload coverUpload) {
        if (coverUpload != null && StringUtils.hasText(oldCoverUrl)) {
            cleanupPlaylistCover(oldCoverUrl);
        }
    }

    private PlaylistActionVO buildEditResult(Long playlistId, Playlists oldPlaylist,
            Playlists updateEntity, CoverUpload coverUpload) {
        PlaylistActionVO result = new PlaylistActionVO();
        result.setPlaylistId(playlistId);
        result.setCoverUrl(coverUpload == null ? oldPlaylist.getPlaylistCoverUrl()
                : updateEntity.getPlaylistCoverUrl());
        return result;
    }

    private record CoverUpload(String fileName, File file) {
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlaylist(Long playlistId, Long userId) {
        Playlists playlist = baseMapper.selectById(playlistId);
        if (playlist == null)
            throw new BusinessException(404, "歌单不存在");
        if (!playlist.getCreatorId().equals(userId))
            throw new BusinessException(403, "无权删除此歌单");

        String coverUrl = playlist.getPlaylistCoverUrl();
        baseMapper.deleteById(playlistId);

        if (StringUtils.hasText(coverUrl)) {
            cleanupPlaylistCover(coverUrl);
        }
    }

    @Override
    public void assertCanViewPlaylist(Long playlistId, Long userId) {
        Playlists playlist = baseMapper.selectById(playlistId);
        if (playlist == null)
            throw new BusinessException(404, "歌单不存在");
        if (!Objects.equals(playlist.getCreatorId(), userId) && !Boolean.TRUE.equals(playlist.getPubliclyVisible()))
            throw new BusinessException(403, "无权访问此私密歌单");
    }

    @Override
    public PlaylistContentVO getPlaylistContent(Long playlistId) {
        Playlists playlist = baseMapper.selectById(playlistId);
        if (playlist == null)
            throw new BusinessException(404, "歌单不存在");

        List<SongsPlaylistsRelation> relations = songsPlaylistsRelationMapper.findByPlaylistId(playlistId);

        List<PlaylistSongVO> songVOList = new ArrayList<>();
        if (!relations.isEmpty()) {
            List<Long> songIds = relations.stream().map(SongsPlaylistsRelation::getSongId)
                    .toList();
            List<Songs> songEntities = songsMapper
                    .selectList(
                            new LambdaQueryWrapper<Songs>()
                                    .select(Songs::getSongId, Songs::getSongTitle, Songs::getArtist, Songs::getAlbum,
                                            Songs::getDuration, Songs::getBitrate, Songs::getSongCoverUrl,
                                            Songs::getSongUrl, Songs::getFileFormat, Songs::getFileSize)
                                    .in(Songs::getSongId, songIds));
            Map<Long, Songs> songMap = songEntities.stream().collect(Collectors.toMap(Songs::getSongId, s -> s));

            for (SongsPlaylistsRelation rel : relations) {
                Songs s = songMap.get(rel.getSongId());
                if (s != null) {
                    PlaylistSongVO sVO = new PlaylistSongVO();
                    BeanUtils.copyProperties(s, sVO);
                    sVO.setSongPlaylistPosition(rel.getSongPlaylistPosition());
                    songVOList.add(sVO);
                }
            }
        }

        PlaylistContentVO vo = new PlaylistContentVO();
        vo.setPlaylist(playlist);
        vo.setSongs(songVOList);
        return vo;
    }

    @Override
    public boolean isPlaylistLiked(Long playlistId, Long userId) {
        return userId != null && likeRelationMapper.countByUserAndPlaylist(userId, playlistId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long playlistId, Long userId, boolean isLike) {
        if (playlistId == null) {
            throw new BusinessException(400, "歌单ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (baseMapper.selectById(playlistId) == null) {
            throw new BusinessException(404, "歌单不存在");
        }

        if (isLike) {
            UsersLikeplaylistsRelation relation = new UsersLikeplaylistsRelation();
            relation.setUserId(userId);
            relation.setPlaylistId(playlistId);
            if (likeRelationMapper.insertIgnore(relation) > 0) {
                this.update().setSql("like_count = like_count + 1").eq(PLAYLIST_ID_COLUMN, playlistId).update();
            }
            return;
        }
        int deleted = likeRelationMapper.deleteByUserAndPlaylist(userId, playlistId);
        if (deleted > 0) {
            this.update().setSql("like_count = GREATEST(like_count - 1, 0)")
                    .eq(PLAYLIST_ID_COLUMN, playlistId).update();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddSongToPlaylistVO addSongToPlaylist(Long playlistId, Long songId, Long userId) {
        assertPlaylistOwner(playlistId, userId);
        if (songsMapper.selectById(songId) == null) {
            throw new BusinessException(404, "歌曲不存在");
        }
        int nextPosition = songsPlaylistsRelationMapper.getMaxPosition(playlistId) + 1;

        SongsPlaylistsRelation relation = new SongsPlaylistsRelation();
        relation.setPlaylistId(playlistId);
        relation.setSongId(songId);
        relation.setSongPlaylistPosition(nextPosition);

        try {
            songsPlaylistsRelationMapper.insert(relation);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(409, "歌曲已在歌单中或歌单更新冲突，请重试");
        } catch (Exception exception) {
            throw new BusinessException(500, "添加失败");
        }

        this.update().setSql("song_count = song_count + 1").eq(PLAYLIST_ID_COLUMN, playlistId).update();

        AddSongToPlaylistVO vo = new AddSongToPlaylistVO();
        vo.setPosition(nextPosition);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSongFromPlaylist(Long playlistId, Long songId, Long userId) {
        assertPlaylistOwner(playlistId, userId);
        SongsPlaylistsRelation target = songsPlaylistsRelationMapper.findByPlaylistAndSong(playlistId, songId);

        if (target == null)
            throw new BusinessException(404, "歌曲不在该歌单中");

        songsPlaylistsRelationMapper.deleteByPlaylistAndSong(playlistId, songId);
        songsPlaylistsRelationMapper.decrementPositionsAfter(playlistId, target.getSongPlaylistPosition());

        this.update().setSql("song_count = GREATEST(song_count - 1, 0)").eq(PLAYLIST_ID_COLUMN, playlistId).update();
    }

    private void assertPlaylistOwner(Long playlistId, Long userId) {
        Playlists playlist = baseMapper.selectById(playlistId);
        if (playlist == null) {
            throw new BusinessException(404, "歌单不存在");
        }
        if (!playlist.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "无权修改此歌单");
        }
    }

    private void cleanupNewPlaylistCover(File savedFile) {
        if (savedFile == null || !savedFile.exists()) {
            return;
        }
        try {
            Files.delete(savedFile.toPath());
        } catch (IOException e) {
            log.error("清理创建失败的歌单封面文件失败", e);
        }
    }

    private void cleanupPlaylistCover(String coverUrl) {
        try {
            String fileName = coverUrl.substring(coverUrl.lastIndexOf("/") + 1);
            File file = StoragePathResolver.resolveFile(playlistCoverPath, fileName);
            Files.deleteIfExists(file.toPath());
        } catch (Exception e) {
            log.warn("物理文件清理失败: {}", e.getMessage());
        }
    }
}
