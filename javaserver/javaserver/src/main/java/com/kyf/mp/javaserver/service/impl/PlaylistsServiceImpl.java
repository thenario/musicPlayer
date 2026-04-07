package com.kyf.mp.javaserver.service.impl;

import com.kyf.mp.javaserver.common.BusinessException;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.entity.Playlists;
import com.kyf.mp.javaserver.entity.Songs;
import com.kyf.mp.javaserver.entity.SongsPlaylistsRelation;
import com.kyf.mp.javaserver.entity.UsersLikeplaylistsRelation;
import com.kyf.mp.javaserver.entity.UsersPlaylistsRelation;
import com.kyf.mp.javaserver.mapper.PlaylistsMapper;
import com.kyf.mp.javaserver.mapper.SongsMapper;
import com.kyf.mp.javaserver.mapper.SongsPlaylistsRelationMapper;
import com.kyf.mp.javaserver.mapper.UsersLikeplaylistsRelationMapper;
import com.kyf.mp.javaserver.mapper.UsersPlaylistsRelationMapper;
import com.kyf.mp.javaserver.modelVO.AddSongToPlaylistVO;
import com.kyf.mp.javaserver.modelVO.MyPlaylistsVO;
import com.kyf.mp.javaserver.modelVO.PlaylistActionVO;
import com.kyf.mp.javaserver.modelVO.PlaylistDetailVO;
import com.kyf.mp.javaserver.modelVO.PlaylistSongVO;
import com.kyf.mp.javaserver.modelVO.PlaylistSummaryVO;
import com.kyf.mp.javaserver.service.IPlaylistsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PlaylistsServiceImpl extends ServiceImpl<PlaylistsMapper, Playlists> implements IPlaylistsService {

    @Value("${file.upload.playlist-cover-path}")
    private String playlistCoverPath;

    @Value("${file.static.playlist-cover-url}")
    private String coverUrlPrefix;

    private final SongsMapper songsMapper;
    private final UsersPlaylistsRelationMapper userPlaylistMapper;
    private final UsersLikeplaylistsRelationMapper likeRelationMapper;
    private final SongsPlaylistsRelationMapper songsPlaylistsRelationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<PlaylistActionVO> createPlaylist(MultipartFile file, String name, String description,
            Integer userId) {
        File savedFile = null;
        try {
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String newFileName = UUID.randomUUID() + "-" + System.currentTimeMillis() + "." + extension;

            savedFile = new File(playlistCoverPath, newFileName);
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
            playlist.setIsPublic(true);

            this.save(playlist);

            UsersPlaylistsRelation relation = new UsersPlaylistsRelation();
            relation.setUserId(userId);
            relation.setPlaylistId(playlist.getPlaylistId());
            userPlaylistMapper.insert(relation);

            PlaylistActionVO data = new PlaylistActionVO();
            data.setCoverUrl(coverUrl);
            data.setPlaylistId(playlist.getPlaylistId());

            return ResultModel.success(data);

        } catch (Exception e) {
            if (savedFile != null && savedFile.exists())
                savedFile.delete();
            log.error("创建歌单失败: ", e);
            throw new BusinessException(500, "创建歌单失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<PlaylistActionVO> editPlaylist(MultipartFile file, Integer playlistId, String name,
            String description, Integer userId) {
        File newSavedFile = null;
        try {
            Playlists oldPlaylist = baseMapper.selectById(playlistId);
            if (oldPlaylist == null)
                throw new BusinessException(404, "歌单不存在");
            if (!oldPlaylist.getCreatorId().equals(userId))
                throw new BusinessException(403, "无权修改此歌单");

            String newFileName = null;
            String oldCoverUrl = oldPlaylist.getPlaylistCoverUrl();

            if (file != null && !file.isEmpty()) {
                String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
                newFileName = UUID.randomUUID() + "-" + System.currentTimeMillis() + "." + extension;
                newSavedFile = new File(playlistCoverPath, newFileName);
                if (!newSavedFile.getParentFile().exists())
                    Files.createDirectories(newSavedFile.getParentFile().toPath());
                file.transferTo(newSavedFile);
            }

            Playlists updateEntity = new Playlists();
            updateEntity.setPlaylistId(playlistId);
            if (StringUtils.hasText(name))
                updateEntity.setPlaylistName(name);
            if (description != null)
                updateEntity.setDescription(description);
            if (newFileName != null)
                updateEntity.setPlaylistCoverUrl(coverUrlPrefix + newFileName);

            updateEntity.setUpdatedDate(LocalDateTime.now());
            baseMapper.updateById(updateEntity);

            if (newFileName != null && StringUtils.hasText(oldCoverUrl)) {
                cleanupPlaylistCover(oldCoverUrl);
            }

            PlaylistActionVO result = new PlaylistActionVO();
            result.setPlaylistId(playlistId);
            result.setCoverUrl(newFileName != null ? updateEntity.getPlaylistCoverUrl() : oldCoverUrl);
            return ResultModel.success(result);

        } catch (BusinessException be) {
            if (newSavedFile != null && newSavedFile.exists())
                newSavedFile.delete();
            throw be;
        } catch (Exception e) {
            if (newSavedFile != null && newSavedFile.exists())
                newSavedFile.delete();
            throw new BusinessException(500, "编辑失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<Void> deletePlaylist(Integer playlistId, Integer userId) {
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
        return ResultModel.success(null);
    }

    @Override
    public ResultModel<MyPlaylistsVO> getMyPlaylists(Integer userId) {
        if (userId == null)
            throw new BusinessException(401, "请先登录");

        List<Playlists> list = baseMapper.selectList(
                new LambdaQueryWrapper<Playlists>()
                        .eq(Playlists::getCreatorId, userId)
                        .orderByDesc(Playlists::getCreatedDate));

        List<PlaylistSummaryVO> playlistVOList = list.stream().map(p -> {
            PlaylistSummaryVO pVO = new PlaylistSummaryVO();
            BeanUtils.copyProperties(p, pVO);
            return pVO;
        }).collect(Collectors.toList());

        MyPlaylistsVO vo = new MyPlaylistsVO();
        vo.setPlaylists(playlistVOList);
        return ResultModel.success(vo);
    }

    @Override
    public ResultModel<PlaylistDetailVO> getPlaylistDetail(Integer playlistId, Integer userId) {
        Playlists playlist = baseMapper.selectById(playlistId);
        if (playlist == null)
            throw new BusinessException(404, "歌单不存在");

        List<SongsPlaylistsRelation> relations = songsPlaylistsRelationMapper.selectList(
                new LambdaQueryWrapper<SongsPlaylistsRelation>()
                        .eq(SongsPlaylistsRelation::getPlaylistId, playlistId));

        List<PlaylistSongVO> songVOList = new ArrayList<>();
        if (!relations.isEmpty()) {
            List<Integer> songIds = relations.stream().map(SongsPlaylistsRelation::getSongId)
                    .collect(Collectors.toList());
            List<Songs> songEntities = songsMapper
                    .selectList(new LambdaQueryWrapper<Songs>().in(Songs::getSongId, songIds));
            Map<Integer, Songs> songMap = songEntities.stream().collect(Collectors.toMap(Songs::getSongId, s -> s));

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

        boolean isLiked = false;
        if (userId != null) {
            isLiked = likeRelationMapper.selectCount(new LambdaQueryWrapper<UsersLikeplaylistsRelation>()
                    .eq(UsersLikeplaylistsRelation::getUserId, userId)
                    .eq(UsersLikeplaylistsRelation::getPlaylistId, playlistId)) > 0;
        }

        PlaylistDetailVO vo = new PlaylistDetailVO();
        vo.setPlaylist(playlist);
        vo.setSongs(songVOList);
        vo.setIsLiked(isLiked);
        return ResultModel.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<Void> toggleLike(Integer playlistId, Integer userId, boolean isLike) {
        if (playlistId == null)
            throw new BusinessException(400, "歌单ID不能为空");
        if (userId == null)
            throw new BusinessException(401, "请先登录");

        if (isLike) {
            boolean success = this.update().setSql("like_count = like_count + 1").eq("playlist_id", playlistId)
                    .update();
            if (!success)
                throw new BusinessException(404, "歌单不存在");

            UsersLikeplaylistsRelation relation = new UsersLikeplaylistsRelation();
            relation.setUserId(userId);
            relation.setPlaylistId(playlistId);
            likeRelationMapper.insert(relation);
        } else {
            this.update().setSql("like_count = GREATEST(like_count - 1, 0)").eq("playlist_id", playlistId).update();
            likeRelationMapper.delete(new LambdaQueryWrapper<UsersLikeplaylistsRelation>()
                    .eq(UsersLikeplaylistsRelation::getUserId, userId)
                    .eq(UsersLikeplaylistsRelation::getPlaylistId, playlistId));
        }
        return ResultModel.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<AddSongToPlaylistVO> addSongToPlaylist(Integer playlistId, Integer songId) {
        int nextPosition = songsPlaylistsRelationMapper.getMaxPosition(playlistId) + 1;

        SongsPlaylistsRelation relation = new SongsPlaylistsRelation();
        relation.setPlaylistId(playlistId);
        relation.setSongId(songId);
        relation.setSongPlaylistPosition(nextPosition);

        try {
            songsPlaylistsRelationMapper.insert(relation);
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry"))
                throw new BusinessException(409, "歌曲已在歌单中");
            throw new BusinessException(500, "添加失败: " + e.getMessage());
        }

        this.update().setSql("song_count = song_count + 1").eq("playlist_id", playlistId).update();

        AddSongToPlaylistVO vo = new AddSongToPlaylistVO();
        vo.setPosition(nextPosition);
        return ResultModel.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<Void> removeSongFromPlaylist(Integer playlistId, Integer songId) {
        SongsPlaylistsRelation target = songsPlaylistsRelationMapper.selectOne(
                new LambdaQueryWrapper<SongsPlaylistsRelation>()
                        .eq(SongsPlaylistsRelation::getPlaylistId, playlistId)
                        .eq(SongsPlaylistsRelation::getSongId, songId));

        if (target == null)
            throw new BusinessException(404, "歌曲不在该歌单中");

        songsPlaylistsRelationMapper.delete(new LambdaQueryWrapper<SongsPlaylistsRelation>()
                .eq(SongsPlaylistsRelation::getPlaylistId, playlistId).eq(SongsPlaylistsRelation::getSongId, songId));

        UpdateWrapper<SongsPlaylistsRelation> updatePosWrapper = new UpdateWrapper<>();
        updatePosWrapper.setSql("song_playlist_position = song_playlist_position - 1")
                .eq("playlist_id", playlistId)
                .gt("song_playlist_position", target.getSongPlaylistPosition());
        songsPlaylistsRelationMapper.update(null, updatePosWrapper);

        this.update().setSql("song_count = GREATEST(song_count - 1, 0)").eq("playlist_id", playlistId).update();
        return ResultModel.success(null);
    }

    private void cleanupPlaylistCover(String coverUrl) {
        try {
            String fileName = coverUrl.substring(coverUrl.lastIndexOf("/") + 1);
            File file = new File(playlistCoverPath, fileName);
            if (file.exists())
                file.delete();
        } catch (Exception e) {
            log.warn("物理文件清理失败: {}", e.getMessage());
        }
    }
}
