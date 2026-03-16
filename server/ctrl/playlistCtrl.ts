import db from "../db.js";
import type { Request, Response } from "express";

const BASE_URL = "http://127.0.0.1:3000/static";

interface CustomRequest extends Request {
  user?: { user_id: string | number } | undefined;
  file?: Express.Multer.File | undefined;
}

export const createPlaylist = async (req: CustomRequest, res: Response) => {
  const { name, description, creator_id, created_date } = req.body;
  const file = req.file;

  if (!name || !file) {
    return res.status(400).json({ message: "请输入完整上传信息" });
  }

  const playlist_cover_url = `${BASE_URL}/playlist_covers/${file.filename}`;
  const connection = await db.getConnection();

  try {
    const is_public = true;
    const song_count = 0;
    const like_count = 0;
    const play_count = 0;

    const params = [
      name,
      creator_id,
      created_date,
      playlist_cover_url,
      song_count,
      like_count,
      play_count,
      is_public,
      created_date,
      created_date,
      description,
    ];

    const playlist_sql = `
      INSERT INTO playlists (
        playlist_name, creator_id, created_date, playlist_cover_url,
        song_count, like_count, play_count, is_public,
        created_date, updated_date, description
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;

    await connection.beginTransaction();

    const [result]: any = await connection.execute(playlist_sql, params);
    const playlist_id = result.insertId;

    await connection.execute(
      `INSERT INTO users_playlists_relation (user_id, playlist_id) VALUES (?, ?)`,
      [creator_id, playlist_id],
    );

    await connection.commit();
    return res.status(200).json({ message: "创建成功", data: { playlist_id } });
  } catch (error) {
    console.error("Create playlist error:", error);
    await connection.rollback();
    return res.status(500).json({ message: "创建失败，请稍后重试" });
  } finally {
    connection.release();
  }
};

export const deletePlaylist = async (req: CustomRequest, res: Response) => {
  try {
    const playlist_id = req.params.id;
    const sql = `DELETE FROM playlists WHERE playlist_id = ?`;
    await db.query(sql, [playlist_id]);
    return res.status(200).json({ message: "删除成功" });
  } catch (error) {
    console.error("Delete playlist error:", error);
    return res.status(500).json({ message: "删除失败" });
  }
};

export const likePlaylist = async (req: CustomRequest, res: Response) => {
  const playlist_id = req.params.id;
  const user_id = req.user?.user_id;
  const connection = await db.getConnection();

  try {
    const updatePlaylistSql = ` UPDATE playlists SET like_count = like_count + 1 WHERE playlist_id = ? `;
    const user_likeplaylistsSql = ` INSERT INTO users_likeplaylists_relation (user_id, playlist_id) VALUES (?, ?) `;

    await connection.beginTransaction();
    await connection.execute(updatePlaylistSql, [playlist_id as string]);
    await connection.execute(user_likeplaylistsSql, [
      user_id as string,
      playlist_id as string,
    ]);
    await connection.commit();

    return res.status(200).json({ message: "点赞成功" });
  } catch (error) {
    console.error("Like playlist error:", error);
    await connection.rollback();
    return res.status(500).json({ message: "点赞失败" });
  } finally {
    connection.release();
  }
};

export const unlikePlaylist = async (req: CustomRequest, res: Response) => {
  const playlist_id = req.params.id;
  const user_id = req.user?.user_id;
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

    return res.status(200).json({ message: "点赞已取消" });
  } catch (error) {
    console.error("Unlike playlist error:", error);
    await connection.rollback();
    return res.status(500).json({ message: "取消点赞失败" });
  } finally {
    connection.release();
  }
};

export const getMyPlaylists = async (req: CustomRequest, res: Response) => {
  const user_id = req.user?.user_id;
  try {
    const sql = `
      SELECT p.creator_id, p.created_date, p.playlist_cover_url, p.song_count,
             p.like_count, p.play_count, p.is_public, p.updated_date, p.description 
      FROM playlists p
      WHERE p.creator_id = ?
    `;
    const [rows]: any = await db.query(sql, [user_id]);
    return res.status(200).json({
      data: { playlists: rows },
    });
  } catch (error) {
    console.error("Get my playlists error:", error);
    return res.status(500).json({ message: "获取歌单列表失败" });
  }
};

export const getPlaylistById = async (req: CustomRequest, res: Response) => {
  const playlist_id = req.params.id;
  try {
    const sql = `
      SELECT 
        p.creator_id, p.created_date, p.playlist_cover_url, p.song_count,
        p.like_count, p.play_count, p.is_public, p.updated_date, p.description,
        IF(COUNT(s.song_id) = 0, JSON_ARRAY(),
          JSON_ARRAYAGG(
            JSON_OBJECT(
              'song_id', s.song_id,
              'song_title', s.song_title,
              'song_cover_url', s.song_cover_url,
              'song_url', s.song_url,
              'artist', s.artist,
              'album', s.album,
              'play_count', s.play_count
            )
          )
        ) AS songs
      FROM playlists p
      LEFT JOIN songs_playlists_relation s ON p.playlist_id = s.playlist_id
      WHERE p.playlist_id = ?
      GROUP BY p.playlist_id
    `;
    const [rows]: any = await db.query(sql, [playlist_id]);

    if (!rows || rows.length === 0) {
      return res.status(404).json({ message: "歌单不存在" });
    }

    const playlist = rows[0];

    return res.status(200).json({
      data: {
        playlist: {
          creator_id: playlist.creator_id,
          created_date: playlist.created_date,
          playlist_cover_url: playlist.playlist_cover_url,
          song_count: playlist.song_count,
          like_count: playlist.like_count,
          play_count: playlist.play_count,
          is_public: playlist.is_public,
          updated_date: playlist.updated_date,
          description: playlist.description,
        },
        songs: playlist.songs,
      },
    });
  } catch (error) {
    console.error("Get playlist by id error:", error);
    return res.status(500).json({ message: "获取歌单详情失败" });
  }
};

export const addSongToPlaylist = async (req: CustomRequest, res: Response) => {
  const { playlist_id, song_id } = req.params;
  const connection = await db.getConnection();
  try {
    await connection.beginTransaction();
    const [rows]: any = await connection.query(
      "SELECT IFNULL(MAX(position), 0) AS maxPos FROM songs_playlists_relation WHERE playlist_id = ?",
      [playlist_id],
    );
    const nextPosition = rows[0].maxPos + 1;

    const sql = `
      INSERT INTO songs_playlists_relation (song_id, playlist_id, position) 
      VALUES (?, ?, ?)
    `;
    await connection.query(
      "UPDATE plsylists SET song_count = count + 1 WHERE playlist_id = ?",
      [playlist_id],
    );
    await connection.query(sql, [song_id, playlist_id, nextPosition]);
    await connection.commit();
    return res.status(200).json({
      message: "添加成功",
      data: { position: nextPosition },
    });
  } catch (error) {
    await connection.rollback();
    console.error("Add song error:", error);
    return res.status(500).json({ message: "添加失败" });
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
      "SELECT position FROM songs_playlists_relation WHERE playlist_id = ? AND song_id = ?",
      [playlist_id, song_id],
    );

    if (target.length === 0) {
      await connection.rollback();
      return res.status(404).json({ message: "歌曲不在该歌单中" });
    }

    const removedPosition = target[0].position;

    await connection.query(
      "DELETE FROM songs_playlists_relation WHERE playlist_id = ? AND song_id = ?",
      [playlist_id, song_id],
    );
    await connection.query(
      "UPDATE plsylists SET song_count = count - 1 WHERE playlist_id = ?",
      [playlist_id],
    );
    await connection.query(
      "UPDATE songs_playlists_relation SET position = position - 1 WHERE playlist_id = ? AND position > ?",
      [playlist_id, removedPosition],
    );

    await connection.commit();
    return res.status(200).json({ message: "删除成功并已更新顺序" });
  } catch (error) {
    await connection.rollback();
    console.error("Remove song error:", error);
    return res.status(500).json({ message: "删除失败" });
  } finally {
    connection.release();
  }
};
