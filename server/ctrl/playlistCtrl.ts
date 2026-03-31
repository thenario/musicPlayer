import db from "../db.js";
import fs from "node:fs";
import type { Request, Response } from "express";
import path from "node:path";

const BASE_URL = "http://127.0.0.1:3000/static";

interface CustomRequest extends Request {
  user?: { user_id: string | number } | undefined;
  file?: Express.Multer.File | undefined;
}

export const createPlaylist = async (req: CustomRequest, res: Response) => {
  const { name, description, created_date } = req.body;
  const user_id = req.user?.user_id;
  const file = req.file;

  if (!name || !user_id || !file) {
    return res.status(400).json({ message: "请输入完整上传信息", data: null });
  }

  const playlist_cover_url = `${BASE_URL}/playlist_covers/${file.filename}`;
  const connection = await db.getConnection();

  try {
    const is_public = true;
    const song_count = 0;
    const like_count = 0;
    const play_count = 0;

    const formattedDate = new Date(created_date || Date.now())
      .toISOString()
      .slice(0, 19)
      .replace("T", " ");

    const params = [
      name,
      user_id,
      playlist_cover_url,
      song_count,
      like_count,
      play_count,
      is_public,
      formattedDate,
      formattedDate,
      description || "",
    ];

    const playlist_sql = `
      INSERT INTO playlists (
        playlist_name, creator_id, playlist_cover_url,
        song_count, like_count, play_count, is_public,
        created_date, updated_date, description
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;

    await connection.beginTransaction();

    const [result]: any = await connection.execute(playlist_sql, params);
    const playlist_id = result.insertId;

    await connection.execute(
      `INSERT INTO users_playlists_relation (user_id, playlist_id) VALUES (?, ?)`,
      [user_id, playlist_id],
    );

    await connection.commit();
    return res
      .status(200)
      .json({ success: true, message: "创建成功", data: { playlist_id } });
  } catch (error) {
    console.error("Create playlist error:", error);

    await connection.rollback();

    if (file?.path && fs.existsSync(file.path)) {
      try {
        fs.unlinkSync(file.path);
        console.log(`[清理成功] 歌单创建失败，已删除冗余封面: ${file.path}`);
      } catch (unlinkError) {
        console.error("清理文件失败:", unlinkError);
      }
    }

    return res
      .status(500)
      .json({ success: false, message: "创建失败，请稍后重试", data: null });
  } finally {
    connection.release();
  }
};

const API_BASE = "http://127.0.0.1:3000";

export const editPlaylistDetail = async (req: any, res: Response) => {
  const { playlist_id, name, description } = req.body;
  const user_id = req.user?.user_id;
  const file = req.file;

  if (!playlist_id) {
    return res.status(400).json({ success: false, message: "缺少歌单ID" });
  }

  const connection = await db.getConnection();

  try {
    // 1. 查询旧资料
    const [rows]: any = await connection.execute(
      "SELECT creator_id, playlist_cover_url FROM playlists WHERE playlist_id = ?",
      [playlist_id],
    );

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: "歌单不存在" });
    }

    const oldPlaylist = rows[0];

    if (String(oldPlaylist.creator_id) !== String(user_id)) {
      return res
        .status(403)
        .json({ success: false, message: "无权修改此歌单" });
    }

    let newCoverUrl = oldPlaylist.playlist_cover_url;
    let oldFilePath: string | null = null;
    let oldFileName: string | null = null;

    if (file) {
      newCoverUrl = `${API_BASE}/static/playlist_covers/${file.filename}`;

      const oldFileName = oldPlaylist.playlist_cover_url?.split("/").pop();
      if (oldFileName && oldFileName.includes("cover_image")) {
        // 使用 process.cwd() 确保路径从项目根目录开始
        oldFilePath = path.join(
          process.cwd(),
          "static/playlist_covers",
          oldFileName,
        );
      }
    }

    const updateFields = [];
    const params = [];

    if (name) {
      updateFields.push("playlist_name = ?");
      params.push(name);
    }
    if (description !== undefined) {
      updateFields.push("description = ?");
      params.push(description);
    }
    if (file) {
      updateFields.push("playlist_cover_url = ?");
      params.push(newCoverUrl);
    }

    if (updateFields.length === 0) {
      return res.status(200).json({ success: true, message: "资料未变动" });
    }

    updateFields.push("updated_date = NOW()");
    params.push(playlist_id);

    const sql = `UPDATE playlists SET ${updateFields.join(", ")} WHERE playlist_id = ?`;

    await connection.beginTransaction();
    await connection.execute(sql, params);
    await connection.commit();

    if (oldFilePath && fs.existsSync(oldFilePath)) {
      try {
        fs.unlinkSync(oldFilePath);
        console.log(`[清理成功] 已删除旧封面文件: ${oldFileName}`);
      } catch (e) {
        console.error("删除旧文件失败:", e);
      }
    }

    return res.status(200).json({
      success: true,
      message: "歌单信息已更新",
      data: { cover_url: newCoverUrl },
    });
  } catch (error) {
    await connection.rollback();
    console.error("Edit playlist error:", error);

    if (file?.path && fs.existsSync(file.path)) {
      fs.unlinkSync(file.path);
    }

    return res.status(500).json({ success: false, message: "服务器错误" });
  } finally {
    connection.release();
  }
};

export const deletePlaylist = async (req: CustomRequest, res: Response) => {
  const playlistId = req.params.id;
  const userId = req.user?.user_id;

  if (!playlistId) {
    return res.status(400).json({ message: "缺少歌单ID参数", data: null });
  }

  try {
    const playlist = await getPlaylistAndCheckAuth(
      playlistId as string,
      userId,
    );
    if (!playlist) {
      return res
        .status(403)
        .json({ success: false, message: "歌单不存在或无权删除" });
    }

    const [result]: any = await db.query(
      `DELETE FROM playlists WHERE playlist_id = ?`,
      [playlistId],
    );

    if (result.affectedRows === 0) {
      return res.status(500).json({ success: false, message: "删除失败" });
    }

    if (playlist.playlist_cover_url) {
      cleanupPlaylistCover(playlist.playlist_cover_url);
    }

    return res
      .status(200)
      .json({ success: true, message: "删除成功", data: null });
  } catch (error: any) {
    console.error("Delete playlist error:", error);
    const status = error.status || 500;
    return res.status(status).json({
      success: false,
      message: error.message || "服务器内部错误",
      data: null,
    });
  }
};

const getPlaylistAndCheckAuth = async (playlistId: string, userId: any) => {
  const [rows]: any = await db.query(
    `SELECT playlist_cover_url, creator_id FROM playlists WHERE playlist_id = ?`,
    [playlistId],
  );

  if (!rows?.length) return null;

  const playlist = rows[0];
  if (String(playlist.creator_id) !== String(userId)) {
    const err: any = new Error("无权删除此歌单");
    err.status = 403;
    throw err;
  }

  return playlist;
};

const cleanupPlaylistCover = (coverUrl: string) => {
  try {
    const urlParts = coverUrl.split("/static/");
    if (urlParts.length <= 1) return;

    const absolutePath = path.join(process.cwd(), "static", urlParts[1]!);

    if (fs.existsSync(absolutePath)) {
      fs.unlinkSync(absolutePath);
      console.log(`[清理成功] 已删除关联封面文件: ${absolutePath}`);
    }
  } catch (fileErr) {
    console.error("物理文件删除失败:", fileErr);
  }
};

export const likePlaylist = async (req: CustomRequest, res: Response) => {
  const playlist_id = req.params.id;
  const user_id = req.user?.user_id;

  if (!playlist_id)
    return res
      .status(400)
      .json({ success: false, message: "缺少歌单ID", data: null });
  if (!user_id)
    return res
      .status(401)
      .json({ success: false, message: "用户未登录", data: null });

  const connection = await db.getConnection();

  try {
    const updatePlaylistSql = `UPDATE playlists SET like_count = like_count + 1 WHERE playlist_id = ?`;
    const user_likeplaylistsSql = `INSERT INTO users_likeplaylists_relation (user_id, playlist_id) VALUES (?, ?)`;

    await connection.beginTransaction();
    await connection.execute(updatePlaylistSql, [playlist_id as string]);
    await connection.execute(user_likeplaylistsSql, [
      user_id as string,
      playlist_id as string,
    ]);
    await connection.commit();

    return res
      .status(200)
      .json({ success: true, message: "点赞成功", data: null });
  } catch (error) {
    console.error("Like playlist error:", error);
    await connection.rollback();
    return res
      .status(500)
      .json({ success: false, message: "点赞失败", data: null });
  } finally {
    connection.release();
  }
};

export const unlikePlaylist = async (req: CustomRequest, res: Response) => {
  const playlist_id = req.params.id;
  const user_id = req.user?.user_id;

  if (!playlist_id)
    return res
      .status(400)
      .json({ success: false, message: "缺少歌单ID", data: null });
  if (!user_id)
    return res
      .status(401)
      .json({ success: false, message: "用户未登录", data: null });

  const connection = await db.getConnection();

  try {
    const updatePlaylistSql = `UPDATE playlists SET like_count = like_count - 1 WHERE playlist_id = ?`;
    const user_likeplaylistsSql = `DELETE FROM users_likeplaylists_relation WHERE user_id = ? AND playlist_id = ?`;

    await connection.beginTransaction();
    await connection.execute(updatePlaylistSql, [playlist_id as string]);
    await connection.execute(user_likeplaylistsSql, [
      user_id as string,
      playlist_id as string,
    ]);
    await connection.commit();

    return res
      .status(200)
      .json({ success: true, message: "点赞已取消", data: null });
  } catch (error) {
    console.error("Unlike playlist error:", error);
    await connection.rollback();
    return res
      .status(500)
      .json({ success: false, message: "取消点赞失败", data: null });
  } finally {
    connection.release();
  }
};

export const getMyPlaylists = async (req: CustomRequest, res: Response) => {
  const user_id = req.user?.user_id;
  if (!user_id)
    return res
      .status(401)
      .json({ success: false, message: "用户未登录", data: null });

  try {
    const sql = `
      SELECT p.playlist_id, p.creator_id, p.created_date, p.playlist_cover_url, p.song_count,
             p.like_count, p.play_count, p.is_public, p.updated_date, p.description 
      FROM playlists p
      WHERE p.creator_id = ?
    `;
    const [rows]: any = await db.query(sql, [user_id]);
    return res.status(200).json({
      success: true,
      message: "获取成功",
      data: { playlists: rows || [] },
    });
  } catch (error) {
    console.error("Get my playlists error:", error);
    return res
      .status(500)
      .json({ success: false, message: "获取歌单列表失败", data: null });
  }
};

export const getPlaylistById = async (req: CustomRequest, res: Response) => {
  const playlist_id = req.params.id;
  const current_user_id = req.user?.user_id;

  try {
    const sql = `
      SELECT 
        p.*,
        EXISTS(
          SELECT 1 FROM users_likeplaylists_relation 
          WHERE user_id = ? AND playlist_id = p.playlist_id
        ) AS is_liked,
        (
          SELECT JSON_ARRAYAGG(
            JSON_OBJECT(
              'song_id', sg.song_id,
              'song_title', sg.song_title,
              'song_cover_url', sg.song_cover_url,
              'song_url', sg.song_url,
              'artist', sg.artist,
              'album', sg.album,
              'position', s.song_playlist_position,
              'duration', sg.duration
            )
          )
          FROM (
            SELECT spr.song_id, spr.song_playlist_position 
            FROM songs_playlists_relation spr 
            WHERE spr.playlist_id = p.playlist_id 
            ORDER BY spr.song_playlist_position ASC
          ) AS s
          JOIN songs sg ON s.song_id = sg.song_id
        ) AS songs
      FROM playlists p
      WHERE p.playlist_id = ?
    `;

    const [rows]: any = await db.query(sql, [current_user_id, playlist_id]);
    const playlist = rows[0];

    if (!playlist)
      return res.status(404).json({ success: false, message: "歌单不存在" });

    return res.status(200).json({
      success: true,
      data: {
        playlist: { ...playlist, songs: undefined },
        songs:
          typeof playlist.songs === "string"
            ? JSON.parse(playlist.songs)
            : playlist.songs || [],
        is_liked: !!playlist.is_liked,
      },
    });
  } catch (error) {
    console.error("Get playlist detail error:", error);
    return res.status(500).json({ success: false, message: "获取失败" });
  }
};

export const addSongToPlaylist = async (req: CustomRequest, res: Response) => {
  const { playlist_id, song_id } = req.params;

  const connection = await db.getConnection();
  try {
    await connection.beginTransaction();

    const [rows]: any = await connection.query(
      "SELECT IFNULL(MAX(song_playlist_position), 0) AS maxPos FROM songs_playlists_relation WHERE playlist_id = ?",
      [playlist_id],
    );
    const nextPosition = rows[0].maxPos + 1;

    const insertSql = `
      INSERT INTO songs_playlists_relation (playlist_id, song_id, song_playlist_position) 
      VALUES (?, ?, ?)
    `;
    await connection.query(insertSql, [playlist_id, song_id, nextPosition]);

    await connection.query(
      "UPDATE playlists SET song_count = song_count + 1 WHERE playlist_id = ?",
      [playlist_id],
    );

    await connection.commit();
    return res.status(200).json({
      success: true,
      message: "添加成功",
      data: { position: nextPosition },
    });
  } catch (error: any) {
    await connection.rollback();
    if (error.code === "ER_DUP_ENTRY") {
      return res
        .status(409)
        .json({ success: false, message: "歌曲已在歌单中" });
    }
    console.error("Add song error:", error);
    return res.status(500).json({ success: false, message: "添加失败" });
  } finally {
    connection.release();
  }
};

export const removeSongFromPlaylist = async (
  req: CustomRequest,
  res: Response,
) => {
  const { playlist_id, song_id } = req.params;

  const connection = await db.getConnection();
  try {
    await connection.beginTransaction();

    const [target]: any = await connection.query(
      "SELECT song_playlist_position FROM songs_playlists_relation WHERE playlist_id = ? AND song_id = ?",
      [playlist_id, song_id],
    );

    if (!target || target.length === 0) {
      await connection.rollback();
      return res
        .status(404)
        .json({ success: false, message: "歌曲不在该歌单中" });
    }

    const removedPosition = target[0].song_playlist_position;

    await connection.query(
      "DELETE FROM songs_playlists_relation WHERE playlist_id = ? AND song_id = ?",
      [playlist_id, song_id],
    );

    await connection.query(
      `UPDATE songs_playlists_relation SET song_playlist_position = song_playlist_position - 1 
       WHERE playlist_id = ? AND song_playlist_position > ?`,
      [playlist_id, removedPosition],
    );

    await connection.query(
      "UPDATE playlists SET song_count = song_count - 1 WHERE playlist_id = ? AND song_count > 0",
      [playlist_id],
    );

    await connection.commit();
    return res.status(200).json({ success: true, message: "已移除" });
  } catch (error) {
    await connection.rollback();
    console.error("Remove song error:", error);
    return res.status(500).json({ success: false, message: "移除失败" });
  } finally {
    connection.release();
  }
};
