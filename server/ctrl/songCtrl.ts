import path from "node:path";
import db from "../db.js";
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
      dateToInsert, // 使用处理后的时间
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
