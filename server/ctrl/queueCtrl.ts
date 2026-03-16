import db from "../db.js";
import type { Response, Request } from "express";
export const getCurrentQueue = async (req: any, res: Response) => {
  const user_id = req.user.user_id;
  try {
    let sql = `
SELECT 
    JSON_OBJECT(
        'current_queue_id', ps.current_queue_id,
        'current_song_id', ps.current_song_id,
        'current_position', ps.current_position,
        'current_progress', ps.current_progress,
        'playmode', ps.playmode
    ) AS queue_state,

    JSON_OBJECT(
        'queue_id', q.queue_id,
        'queue_name', q.queue_name,
        'creator_id', q.creator_id,
        'song_count', q.song_count,
        'is_current', q.is_current,
        'created_date', q.created_date,
        'updated_date', q.updated_date,
        'queue_items', IF(COUNT(qi.queue_item_id) = 0, JSON_ARRAY(), 
            JSON_ARRAYAGG(
                JSON_OBJECT(
                    'queue_item_id', qi.queue_item_id,
                    'position', qi.position,
                    'queue_id', qi.queue_id,
                    'added_date', qi.added_date,
                    'song', JSON_OBJECT(
                        'song_id', s.song_id,
                        'song_title', s.song_title,
                        'song_cover_url', s.song_cover_url,
                        'song_url', s.song_url,
                        'artist', s.artist
                    )
                )
            )
        )
    ) AS queue

FROM play_state ps
LEFT JOIN queues q ON ps.user_id = q.creator_id AND q.is_current = 1
LEFT JOIN queue_items qi ON q.queue_id = qi.queue_id
LEFT JOIN songs s ON qi.song_id = s.song_id
WHERE ps.user_id = ?
GROUP BY ps.user_id, q.queue_id, ps.current_queue_id;
    `;
    const [rows]: [any[], any] = await db.query(sql, user_id);
    return res.status(200).json({
      data: { queue: rows[0].queue, queue_state: rows[0].queue_state },
    });
  } catch (error) {
    console.log(error);
    return res.status(400).json({
      message: "获取失败",
    });
  }
};
export const getMyQueues = async (req: any, res: Response) => {
  const user_id = req.user.user_id;
  try {
    let sql = `
  SELECT 
    q.queue_id,
    q.queue_name,
    q.creator_id,
    q.song_count,
    q.is_current,
    q.created_date,
    q.updated_date,
    IF(COUNT(qi.queue_item_id) = 0, JSON_ARRAY(), 
        JSON_ARRAYAGG(
            JSON_OBJECT(
                'queue_item_id', qi.queue_item_id,
                'poition',position,
                'queue_id', qi.queue_id,
                'added_date', qi.added_date,
                'song', JSON_OBJECT(
                    'song_id', s.song_id,
                    'song_title', s.song_title,
                    'song_cover_url',s.song_cover_url,
                    'song_url', s.song_url,
                    'artist', s.artist,
                )
            )
        )
    ) AS queue_items
FROM queues q
LEFT JOIN queue_items qi ON q.queue_id = qi.queue_id
LEFT JOIN songs s ON qi.song_id = s.song_id
WHERE q.creator_id = ?
GROUP BY q.queue_id;
  `;
    const [rows] = await db.query(sql, user_id);
    return res.status(200).json({
      data: {
        queues: rows,
      },
    });
  } catch (error) {
    console.log(error);
    return res.status(400).json({
      message: "获取失败",
    });
  }
};
export const getQueueById = async (req: Request, res: Response) => {
  const { queue_id } = req.params;
  try {
    let sql = `
      SELECT 
        q.queue_id,
        q.queue_name,
        q.creator_id,
        q.song_count,
        q.is_curd_date,
        q.updaterent,
        q.created_date,
        IF(COUNT(qi.queue_item_id) = 0, JSON_ARRAY(),
          JSON_ARRAYAGG(JSON_OBJECT(
            'queue_item_id', qi.queue_item_id,
            'position', qi.position,
            'queue_id', qi.queue_id,
            'added_date', qi.added_date,
            'song', JSON_OBJECT(
              'song_id', s.song_id,
              'song_title', s.song_title,
              'song_cover_url', s.song_cover_url,
              'song_url', s.song_url,
              'artist', s.artist
            )
          ))
        ) AS queue_items
      FROM queues q
      LEFT JOIN queue_items qi ON q.queue_id = qi.queue_id
      LEFT JOIN songs s ON qi.song_id = s.song_id
      WHERE q.queue_id = ?
      GROUP BY q.queue_id;
    `;

    const [rows]: any = await db.query(sql, [queue_id]);

    if (!rows || rows.length === 0) {
      return res.status(404).json({ message: "队列不存在" });
    }

    const row = rows[0];

    return res.status(200).json({
      success: true,
      data: {
        queue: row,
      },
    });
  } catch (error) {
    console.error("SQL Error:", error);
    return res.status(500).json({
      message: "获取失败",
    });
  }
};
export const deletQueue = async (req: Request, res: Response) => {
  const { queue_id } = req.params;
  const user_id = (req as any).user.user_id;

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();

    const [deleteRes]: any = await connection.execute(
      "DELETE FROM queues WHERE queue_id = ? AND creator_id = ?",
      [queue_id, user_id],
    );

    if (deleteRes.affectedRows === 0) {
      await connection.rollback();
      return res.status(404).json({ message: "队列不存在或无权删除" });
    }

    const [stateRows]: any = await connection.execute(
      "SELECT current_queue_id FROM play_state WHERE user_id = ?",
      [user_id],
    );

    if (stateRows.length > 0 && stateRows[0].current_queue_id == queue_id) {
      const [latestQueues]: any = await connection.execute(
        "SELECT queue_id FROM queues WHERE creator_id = ? ORDER BY updated_date DESC LIMIT 1",
        [user_id],
      );

      const nextQueueId =
        latestQueues.length > 0 ? latestQueues[0].queue_id : null;

      await connection.execute(
        `UPDATE play_state 
         SET current_queue_id = ?, current_song_id = NULL, current_progress = 0
         WHERE user_id = ?`,
        [nextQueueId, user_id],
      );
    }

    await connection.commit();
    return res.status(200).json({
      success: true,
      message: "删除成功，播放状态已自动同步",
    });
  } catch (error) {
    await connection.rollback();
    console.error("Delete and Sync Error:", error);
    return res.status(500).json({ message: "删除操作失败" });
  } finally {
    connection.release();
  }
};

export const createQueueFromPlaylist = async (req: Request, res: Response) => {
  const { playlist_id } = req.body;
  const user_id = (req as any).user.user_id;

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();

    const [queues]: any = await connection.execute(
      "SELECT queue_id FROM queues WHERE creator_id = ? ORDER BY created_date ASC",
      [user_id],
    );

    if (queues.length >= 5) {
      const oldestQueueId = queues[0].queue_id;
      await connection.execute("DELETE FROM queues WHERE queue_id = ?", [
        oldestQueueId,
      ]);
    }

    const [playlists]: any = await connection.execute(
      "SELECT playlist_name FROM playlists WHERE playlist_id = ?",
      [playlist_id],
    );
    const queueName = playlists[0]?.playlist_name || "新播放队列";

    const [queueRes]: any = await connection.execute(
      "INSERT INTO queues (queue_name, creator_id, is_current) VALUES (?, ?, 1)",
      [queueName, user_id],
    );
    const newQueueId = queueRes.insertId;

    await connection.execute(
      `
      INSERT INTO queue_items (queue_id, song_id, position)
      SELECT ?, song_id, position 
      FROM songs_playlists_relation 
      WHERE playlist_id = ?
    `,
      [newQueueId, playlist_id],
    );

    await connection.execute(
      `UPDATE play_state 
       SET current_queue_id = ?, current_song_id = NULL, current_progress = 0 
       WHERE user_id = ?`,
      [newQueueId, user_id],
    );

    await connection.commit();
    return res.status(201).json({
      success: true,
      message: "已从歌单创建新队列",
      data: { queue_id: newQueueId },
    });
  } catch (error) {
    await connection.rollback();
    console.error("Create Queue Error:", error);
    return res.status(500).json({ message: "创建队列失败" });
  } finally {
    connection.release();
  }
};

export const addSongToQueue = async (req: Request, res: Response) => {
  let { queue_id } = req.params;
  const { song_id, mode } = req.body;
  const user_id = (req as any).user.user_id;

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();
    let finalQueueId: number | string = queue_id as string | number;
    if (finalQueueId === 0 || !finalQueueId || finalQueueId === "undefined") {
      const [existingQueues]: any = await connection.execute(
        "SELECT queue_id FROM queues WHERE creator_id = ? ORDER BY created_at DESC LIMIT 1",
        [user_id],
      );

      if (existingQueues.length === 0) {
        const [countRows]: any = await connection.execute(
          "SELECT COUNT(*) as total FROM queues WHERE creator_id = ?",
          [user_id],
        );

        if (countRows[0].total >= 5) {
          await connection.rollback();
          return res.status(400).json({ message: "最多只能创建 5 个队列" });
        }

        const [newQueue]: any = await connection.execute(
          "INSERT INTO queues (queue_name, creator_id, song_count, created_at) VALUES (?, ?, 0, NOW())",
          ["默认列表", user_id],
        );
        finalQueueId = newQueue.insertId;
      } else {
        finalQueueId = existingQueues[0].queue_id;
      }
    }

    const [stateRows]: any = await connection.execute(
      "SELECT current_position FROM play_state WHERE user_id = ?",
      [user_id],
    );

    const currentPos =
      stateRows.length > 0 ? stateRows[0].current_position || 0 : 0;
    const insertPos = currentPos + 1;

    const [delRes]: any = await connection.execute(
      "DELETE FROM queue_items WHERE queue_id = ? AND song_id = ?",
      [finalQueueId, song_id],
    );
    const wasExisted = delRes.affectedRows > 0;

    await connection.execute(
      "UPDATE queue_items SET position = position + 1 WHERE queue_id = ? AND position >= ?",
      [finalQueueId, insertPos],
    );

    await connection.execute(
      `INSERT INTO queue_items (queue_id, song_id, position, added_date) VALUES (?, ?, ?, NOW())`,
      [finalQueueId, song_id, insertPos],
    );

    if (mode === "now" || stateRows.length === 0) {
      await connection.execute(
        `
        INSERT INTO play_state 
          (user_id, current_queue_id, current_song_id, current_position, current_progress, updated_date)
        VALUES (?, ?, ?, ?, 0, NOW())
        ON DUPLICATE KEY UPDATE
          current_queue_id = VALUES(current_queue_id),
          current_song_id = VALUES(current_song_id),
          current_position = VALUES(current_position),
          current_progress = 0,
          updated_date = NOW()
      `,
        [user_id, finalQueueId, song_id, insertPos],
      );
    }

    if (!wasExisted) {
      await connection.execute(
        "UPDATE queues SET song_count = song_count + 1 WHERE queue_id = ?",
        [finalQueueId],
      );
    }

    await connection.commit();
    return res.status(200).json({
      success: true,
      data: { queue_id: finalQueueId },
      message: mode === "now" ? "正在播放" : "已添加到下一首",
    });
  } catch (error) {
    await connection.rollback();
    console.error(error);
    return res.status(500).json({ message: "操作失败" });
  } finally {
    connection.release();
  }
};

export const removeSongFromQueue = async (req: Request, res: Response) => {
  const { queue_item_id } = req.params;
  const user_id = (req as any).user.user_id;

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();

    const [itemRows]: any = await connection.execute(
      `SELECT qi.queue_id, qi.position, qi.song_id 
       FROM queue_items qi 
       JOIN queues q ON qi.queue_id = q.queue_id 
       WHERE qi.queue_item_id = ? AND q.creator_id = ?`,
      [queue_item_id, user_id],
    );

    if (itemRows.length === 0) {
      await connection.rollback();
      return res.status(404).json({ message: "未找到该歌曲或无权操作" });
    }

    const {
      queue_id,
      position: removedPos,
      song_id: removedSongId,
    } = itemRows[0];

    await connection.execute(
      "DELETE FROM queue_items WHERE queue_item_id = ?",
      [queue_item_id as string],
    );

    await connection.execute(
      "UPDATE queue_items SET queue_item_position = queue_item_position - 1 WHERE queue_id = ? AND queue_item_position > ?",
      [queue_id, removedPos],
    );

    const [stateRows]: any = await connection.execute(
      "SELECT current_song_id FROM play_state WHERE user_id = ? AND current_queue_id = ?",
      [user_id, queue_id],
    );

    if (
      stateRows.length > 0 &&
      stateRows[0].current_song_id === removedSongId
    ) {
      const [nextSong]: any = await connection.execute(
        "SELECT song_id FROM queue_items WHERE queue_id = ? AND queue_item_position = ?",
        [queue_id, removedPos],
      );

      const nextSongId = nextSong.length > 0 ? nextSong[0].song_id : null;

      await connection.execute(
        "UPDATE play_state SET current_song_id = ?, current_progress = 0 WHERE user_id = ?",
        [nextSongId, user_id],
      );
    }

    await connection.execute(
      "UPDATE queues SET song_count = GREATEST(song_count - 1, 0) WHERE queue_id = ?",
      [queue_id],
    );

    await connection.commit();
    return res.status(200).json({ success: true, message: "删除成功" });
  } catch (error) {
    await connection.rollback();
    console.error("Remove Song Error:", error);
    return res.status(500).json({ message: "删除失败" });
  } finally {
    connection.release();
  }
};

export const updateCurrentQueueState = async (req: Request, res: Response) => {
  const { queue_id, song_id, position, progress } = req.body.stateData;
  const user_id = (req as any).user.user_id;

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();
    let finalPosition = position;

    if (finalPosition === undefined && song_id) {
      const [posRows]: any = await connection.execute(
        "SELECT queue_item_position FROM queue_items WHERE queue_id = ? AND song_id = ?",
        [queue_id, song_id],
      );
      if (posRows.length > 0) {
        finalPosition = posRows[0].queue_item_position;
      }
    }

    await connection.execute(
      `
      INSERT INTO play_state 
        (user_id, current_queue_id, current_song_id, current_position, current_progress, updated_date)
      VALUES 
        (?, ?, ?, ?, ?, NOW())
      ON DUPLICATE KEY UPDATE
        current_queue_id = VALUES(current_queue_id),
        current_song_id = VALUES(current_song_id),
        current_position = VALUES(current_position),
        current_progress = VALUES(current_progress),
        updated_date = NOW()
    `,
      [user_id, queue_id, song_id, finalPosition, progress || 0],
    );

    await connection.commit();
    return res.status(200).json({
      success: true,
      message: "播放状态已同步",
    });
  } catch (error) {
    await connection.rollback();
    console.error("Update PlayState Error:", error);
    return res.status(500).json({ message: "同步状态失败" });
  } finally {
    connection.release();
  }
};

export const reorderQueue = async (req: Request, res: Response) => {
  const { queue_id } = req.params;
  const { song_ids } = req.body; // 假设格式为 [101, 105, 102, ...]
  const user_id = (req as any).user.user_id;

  if (!Array.isArray(song_ids) || song_ids.length === 0) {
    return res.status(400).json({ message: "无效的歌曲列表" });
  }

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();

    const [queueCheck]: any = await connection.execute(
      "SELECT queue_id FROM queues WHERE queue_id = ? AND creator_id = ?",
      [queue_id, user_id],
    );

    if (queueCheck.length === 0) {
      await connection.rollback();
      return res.status(403).json({ message: "无权操作此队列" });
    }

    let sql = "UPDATE queue_items SET queue_item_position = CASE song_id ";
    const params: any[] = [];

    song_ids.forEach((songId, index) => {
      sql += "WHEN ? THEN ? ";
      params.push(songId, index + 1);
    });

    sql +=
      "END WHERE queue_id = ? AND song_id IN (" +
      song_ids.map(() => "?").join(",") +
      ")";

    params.push(queue_id, ...song_ids);

    await connection.execute(sql, params);

    await connection.execute(
      `
      UPDATE play_state ps
      JOIN queue_items qi ON ps.current_song_id = qi.song_id AND ps.current_queue_id = qi.queue_id
      SET ps.current_position = qi.queue_item_position
      WHERE ps.user_id = ? AND ps.current_queue_id = ?
    `,
      [user_id, queue_id],
    );

    await connection.commit();
    res.status(200).json({ success: true, message: "排序已更新" });
  } catch (error) {
    await connection.rollback();
    console.error("Reorder Error:", error);
    res.status(500).json({ message: "排序失败" });
  } finally {
    connection.release();
  }
};

export const setPlayMode = async (req: Request, res: Response) => {
  const { queue_id } = req.params;
  const { play_mode } = req.body;
  const user_id = (req as any).user.user_id;

  const connection = await db.getConnection();

  try {
    const [queueCheck]: any = await connection.execute(
      "SELECT queue_id FROM queues WHERE queue_id = ? AND creator_id = ?",
      [queue_id, user_id],
    );

    if (queueCheck.length === 0) {
      return res.status(403).json({ message: "无权操作此队列" });
    }

    await connection.execute(
      `
      INSERT INTO play_state 
        (user_id, current_queue_id, play_mode, updated_date)
      VALUES 
        (?, ?, ?, NOW())
      ON DUPLICATE KEY UPDATE
        current_queue_id = VALUES(current_queue_id),
        play_mode = VALUES(play_mode),
        updated_date = NOW()
    `,
      [user_id, queue_id, play_mode],
    );

    return res.status(200).json({
      success: true,
      message: `播放模式已切换为 ${play_mode}`,
    });
  } catch (error) {
    console.error("Set PlayMode Error:", error);
    return res.status(500).json({ message: "设置失败" });
  } finally {
    connection.release();
  }
};

export const alterQueueToCurrent = async (req: Request, res: Response) => {
  const { queue_id } = req.body;
  const user_id = (req as any).user.user_id;

  if (!queue_id) {
    return res.status(400).json({ message: "缺少 queue_id" });
  }

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();

    // 1. 权限校验：确保该队列属于当前用户
    const [queueRows]: any = await connection.execute(
      "SELECT queue_id FROM queues WHERE queue_id = ? AND creator_id = ?",
      [queue_id, user_id],
    );

    if (queueRows.length === 0) {
      await connection.rollback();
      return res.status(403).json({ message: "队列不存在或无权访问" });
    }

    const [songRows]: any = await connection.execute(
      "SELECT song_id FROM queue_items WHERE queue_id = ? AND position = 1",
      [queue_id],
    );

    const firstSongId = songRows.length > 0 ? songRows[0].song_id : null;

    await connection.execute(
      `
      INSERT INTO play_state 
        (user_id, current_queue_id, current_song_id, current_position, current_progress, updated_date)
      VALUES 
        (?, ?, ?, 1, 0, NOW())
      ON DUPLICATE KEY UPDATE
        current_queue_id = VALUES(current_queue_id),
        current_song_id = VALUES(current_song_id),
        current_position = 1,
        current_progress = 0,
        updated_date = NOW()
    `,
      [user_id, queue_id, firstSongId],
    );

    await connection.commit();
    return res.status(200).json({
      success: true,
      message: "已切换当前播放队列",
      data: {
        current_song_id: firstSongId,
        current_position: 1,
      },
    });
  } catch (error) {
    await connection.rollback();
    console.error("Alter Queue Error:", error);
    return res.status(500).json({ message: "切换失败" });
  } finally {
    connection.release();
  }
};

export const clearQueue = async (req: Request, res: Response) => {
  const { queue_id } = req.params;
  const user_id = (req as any).user.user_id;

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();

    const [queueRows]: any = await connection.execute(
      "SELECT queue_id FROM queues WHERE queue_id = ? AND creator_id = ?",
      [queue_id, user_id],
    );

    if (queueRows.length === 0) {
      await connection.rollback();
      return res.status(403).json({ message: "队列不存在或无权操作" });
    }

    await connection.execute("DELETE FROM queue_items WHERE queue_id = ?", [
      queue_id as string,
    ]);

    await connection.execute(
      "UPDATE queues SET song_count = 0 WHERE queue_id = ?",
      [queue_id as string],
    );

    await connection.execute(
      `
      UPDATE play_state 
      SET current_song_id = NULL, 
          current_position = 0, 
          current_progress = 0 
      WHERE user_id = ? AND current_queue_id = ?
    `,
      [user_id, queue_id],
    );

    await connection.commit();
    return res.status(200).json({
      success: true,
      message: "队列已清空",
    });
  } catch (error) {
    await connection.rollback();
    console.error("Clear Queue Error:", error);
    return res.status(500).json({ message: "清空失败" });
  } finally {
    connection.release();
  }
};
