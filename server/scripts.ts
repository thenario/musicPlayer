import fs from "node:fs";
import path from "node:path";
import * as mm from "music-metadata";
import "dotenv/config";
import db from "./db.js";

// 确保路径指向服务器上的真实位置
const SONGS_DIR = path.resolve("static/songs");
const COVERS_DIR = path.resolve("static/song_covers");

async function syncSongs() {
  console.log("\n" + "=".repeat(40));
  console.log("🚀 音乐库重置并同步启动 (Nginx 优化版)...");
  console.log("=".repeat(40));

  try {
    // --- 1. 环境清理 ---
    console.log("🧹 正在清空数据库 songs 表...");
    // 使用 TRUNCATE 清空数据并重置 ID
    await db.query("TRUNCATE TABLE songs");

    if (fs.existsSync(COVERS_DIR)) {
      console.log("📂 正在清理旧封面图片...");
      const oldCovers = fs.readdirSync(COVERS_DIR);
      for (const cover of oldCovers) {
        fs.unlinkSync(path.join(COVERS_DIR, cover));
      }
    } else {
      fs.mkdirSync(COVERS_DIR, { recursive: true });
    }

    // --- 2. 扫描文件 ---
    if (!fs.existsSync(SONGS_DIR)) {
      throw new Error(`找不到音乐目录: ${SONGS_DIR}`);
    }

    const musicFiles = fs
      .readdirSync(SONGS_DIR)
      .filter((file) =>
        [".mp3", ".flac", ".m4a", ".wav"].includes(
          path.extname(file).toLowerCase(),
        ),
      );

    const totalFiles = musicFiles.length;
    console.log(`📂 发现 ${totalFiles} 首音乐，开始重新导入...\n`);

    let successCount = 0;

    // --- 3. 遍历处理 ---
    for (let i = 0; i < totalFiles; i++) {
      const file = musicFiles[i]!;
      const filePath = path.join(SONGS_DIR, file);
      const fileStat = fs.statSync(filePath);
      const fileNameWithoutExt = path.parse(file).name;

      // 解析文件名: 歌曲名_歌手
      let [title, artist] = fileNameWithoutExt.split("_");
      if (!artist) artist = "未知歌手";

      try {
        const metadata = await mm.parseFile(filePath);
        const duration = Math.floor(metadata.format.duration || 0);
        const bitrate = Math.floor((metadata.format.bitrate || 0) / 1000);
        const fileFormat = path.extname(file).slice(1);

        let coverUrl = "";
        const picture = metadata.common.picture?.[0];

        // 生成适配 Nginx 的相对路径
        if (picture) {
          const timestamp = Date.now();
          const coverFileName = `${fileNameWithoutExt}_${timestamp}.jpg`;
          // 核心修改：不带 http 和端口
          coverUrl = `/static/song_covers/${coverFileName}`;
          fs.writeFileSync(path.join(COVERS_DIR, coverFileName), picture.data);
        }

        // 歌曲路径：同样使用相对路径
        // 注意：如果文件名含特殊字符，Nginx 访问时浏览器会自动编码，存原始文件名即可
        const songUrl = `/static/songs/${file}`;

        const songData = [
          title!.trim(),
          artist.trim(),
          "我的收藏",
          (fileStat.size / (1024 * 1024)).toFixed(2) + "MB",
          1,
          "Admin",
          duration,
          bitrate,
          coverUrl, // 存入 /static/song_covers/xxx.jpg
          songUrl, // 存入 /static/songs/xxx.mp3
          new Date(),
          fileFormat,
        ];

        const sql = `
                    INSERT INTO songs 
                    (song_title, artist, album, file_size, uploader_id, uploader_name, duration, bitrate, song_cover_url, song_url, date_added, file_format) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                `;

        await db.query(sql, songData);

        // 打印前几条数据用于实时校验
        if (successCount < 3) {
          console.log(`[验证] ${title} -> URL: ${songUrl}`);
        }

        successCount++;
      } catch (err) {
        console.error(`❌ 处理 ${file} 失败:`, err);
      }
    }

    console.log("\n" + "=".repeat(40));
    console.log(`✨ 同步完成！共导入 ${successCount} 首。`);
    console.log("=".repeat(40));
  } catch (error) {
    console.error("\n🔴 脚本运行崩溃:", error);
  } finally {
    // 重要：关闭数据库连接，确保所有 INSERT 事务已提交并退出进程
    if (db.end) await db.end();
    process.exit(0);
  }
}

syncSongs();
