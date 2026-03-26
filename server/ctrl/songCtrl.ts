import path from "node:path";
import db from "../db.js";
import { URL } from "url";
import * as mm from "music-metadata";
import type { Request, Response } from "express";

const BASE_URL = "http://127.0.0.1:3000/static";

export const getSongs = async (req: Request, res: Response) => {
  const { page, keyword } = req.query;
  const pageLimit = 15;

  if (!page) {
    return res.status(400).json({ message: "缺少页码参数", data: null });
  }

  const sqlPage = parseInt(page as string);
  if (isNaN(sqlPage) || sqlPage < 1) {
    return res.status(400).json({ message: "请输入正确页码", data: null });
  }

  try {
    const offset = (sqlPage - 1) * pageLimit;

    let dataSql = `SELECT * FROM songs WHERE 1 = 1`;
    let countSql = `SELECT COUNT(*) as total FROM songs WHERE 1 = 1`;
    const queryParams: any[] = [];

    if (keyword) {
      const filter = ` AND song_title LIKE ?`;
      dataSql += filter;
      countSql += filter;
      queryParams.push(`%${keyword}%`);
    }

    const [countRows]: any = await db.query(countSql, queryParams);
    const totalItems =
      countRows && countRows.length > 0 ? countRows[0].total : 0;

    dataSql += ` LIMIT ? OFFSET ?`;
    const [rows] = await db.query(dataSql, [...queryParams, pageLimit, offset]);

    const totalPages = Math.ceil(totalItems / pageLimit);

    return res.status(200).json({
      success: true,
      message: "获取成功",
      data: {
        songs: rows || [],
        pagination: {
          total_items: totalItems,
          total_pages: totalPages,
          current_page: sqlPage,
          page_limit: pageLimit,
        },
      },
    });
  } catch (error) {
    console.error("SQL Error:", error);
    return res.status(500).json({ message: "歌曲查询失败", data: null });
  }
};

import fs from "fs"; // 记得引入 fs

export const uploadSong = async (req: Request, res: Response) => {
  const files = req.files as { [fieldname: string]: Express.Multer.File[] };
  const { added_date, title, artist, album, uploader_id, uploader_name } =
    req.body;

  if (
    !files?.["coverfile"] ||
    !files?.["audiofile"] ||
    !uploader_id ||
    !uploader_name
  ) {
    return res
      .status(400)
      .json({ message: "请上传完整信息(包含封面与音频文件)", data: null });
  }

  const audioFile = files["audiofile"][0];
  const coverFile = files["coverfile"][0];

  try {
    const metadata = await mm.parseFile(audioFile!.path);
    const duration = Math.round(metadata.format.duration || 0);
    const bitrate = Math.round((metadata.format.bitrate || 0) / 1000);

    const song_cover_url = `${BASE_URL}/song_covers/${coverFile!.filename}`;
    const song_url = `${BASE_URL}/songs/${audioFile!.filename}`;
    const file_format = path.extname(audioFile!.filename).replace(".", "");

    const dateToInsert = new Date(added_date || Date.now())
      .toISOString()
      .slice(0, 19)
      .replace("T", " ");

    const sql = ` 
      INSERT INTO songs 
      (song_title, artist, album, file_size, uploader_id, uploader_name, duration, bitrate, song_cover_url, song_url, date_added, file_format)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;

    const params = [
      title || audioFile!.originalname,
      artist || "未知艺术家",
      album || "未知专辑",
      audioFile!.size,
      uploader_id,
      uploader_name,
      duration,
      bitrate,
      song_cover_url,
      song_url,
      dateToInsert,
      file_format,
    ];

    await db.query(sql, params);

    return res.status(200).json({
      success: true,
      message: "歌曲上传成功",
      data: null,
    });
  } catch (error) {
    console.error("Upload Error:", error);

    [audioFile, coverFile].forEach((file) => {
      if (file && fs.existsSync(file.path)) {
        fs.unlinkSync(file.path);
        console.log(`已清理冗余文件: ${file.path}`);
      }
    });

    return res.status(500).json({
      success: false,
      message: "提交出错，数据库写入失败",
      data: null,
    });
  }
};

export const streamSong = async (req: Request, res: Response) => {
  try {
    const songId = req.params.id;

    const [rows] = await db.query<any[]>(
      "SELECT song_url FROM songs WHERE song_id = ?",
      [songId],
    );

    if (!rows || rows.length === 0) {
      return res.status(404).send("数据库中未找到该歌曲记录");
    }

    const dbSongUrl = rows[0].song_url;

    const parsedUrl = new URL(dbSongUrl);
    const fileName = path.basename(decodeURIComponent(parsedUrl.pathname));

    const filePath = path.join(process.cwd(), "static", "songs", fileName);

    if (!fs.existsSync(filePath)) {
      console.error("❌ 文件物理路径不存在:", filePath);
      return res.status(404).send("音频不存在");
    }

    const stat = fs.statSync(filePath);
    const fileSize = stat.size;
    const range = req.headers.range;

    if (range) {
      const parts = range.replace(/bytes=/, "").split("-");
      const start = parseInt(parts[0]!, 10);
      const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;

      if (start >= fileSize || end >= fileSize) {
        res
          .status(416)
          .set("Content-Range", `bytes */${fileSize}`)
          .send("Requested Range Not Satisfiable");
        return;
      }

      const chunkSize = end - start + 1;
      const file = fs.createReadStream(filePath, { start, end });

      res.writeHead(206, {
        "Content-Range": `bytes ${start}-${end}/${fileSize}`,
        "Accept-Ranges": "bytes",
        "Content-Length": chunkSize,
        "Content-Type": "audio/mpeg",
      });
      file.pipe(res);
    } else {
      res.writeHead(200, {
        "Content-Length": fileSize,
        "Content-Type": "audio/mpeg",
        "Accept-Ranges": "bytes",
      });
      fs.createReadStream(filePath).pipe(res);
    }
  } catch (error) {
    console.error("音频流处理崩溃:", error);
    res.status(500).send("服务器内部错误");
  }
};
