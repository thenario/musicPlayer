import fs from "node:fs";
import path from "node:path";
import * as mm from "music-metadata";
import "dotenv/config";
import db from "./db.js";

const SONGS_DIR = path.resolve("static/songs");
const COVERS_DIR = path.resolve("static/song_covers");

async function syncSongs() {
  console.log("\n" + "=".repeat(40));
  console.log("🚀 音乐库重置并同步启动...");
  console.log("=".repeat(40));

  try {
    // --- 1. 环境清理 ---

    // 清空数据库表 (TRUNCATE 会重置自增 ID)
    console.log("🧹 正在清空数据库 songs 表...");
    await db.query("TRUNCATE TABLE songs");

    // 清空封面文件夹
    if (fs.existsSync(COVERS_DIR)) {
      console.log("📂 正在清理旧封面图片...");
      const oldCovers = fs.readdirSync(COVERS_DIR);
      for (const cover of oldCovers) {
        fs.unlinkSync(path.join(COVERS_DIR, cover));
      }
    } else {
      fs.mkdirSync(COVERS_DIR, { recursive: true });
    }

    // --- 2. 扫描音乐文件 ---

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
    let failCount = 0;

    // --- 3. 遍历处理 ---

    for (let i = 0; i < totalFiles; i++) {
      const file = musicFiles[i]!;
      const currentIndex = i + 1;
      const filePath = path.join(SONGS_DIR, file);
      const fileStat = fs.statSync(filePath);
      const fileNameWithoutExt = path.parse(file).name;

      let [title, artist] = fileNameWithoutExt.split("_");
      if (!artist) artist = "未知歌手";

      process.stdout.write(
        `[${currentIndex}/${totalFiles}] 正在处理: ${title!.trim()}... `,
      );

      try {
        const metadata = await mm.parseFile(filePath);
        const duration = Math.floor(metadata.format.duration || 0);
        const bitrate = Math.floor((metadata.format.bitrate || 0) / 1000);
        const fileFormat = path.extname(file).slice(1);

        let coverUrl = "";
        const picture = metadata.common.picture?.[0];

        if (picture) {
          // 使用时间戳确保唯一，但因为这次是清空后运行，文件夹是干净的
          const timestamp = Date.now();
          const coverFileName = `${fileNameWithoutExt}_${timestamp}.jpg`;
          coverUrl = `http://127.0.0.1:3000/static/song_covers/${coverFileName}`;
          fs.writeFileSync(path.join(COVERS_DIR, coverFileName), picture.data);
        }

        const songData = [
          title!.trim(),
          artist.trim(),
          "空与风的收藏",
          (fileStat.size / (1024 * 1024)).toFixed(2) + "MB",
          1,
          "空与风",
          duration,
          bitrate,
          coverUrl,
          `http://127.0.0.1:3000/static/songs/${file}`,
          new Date(),
          fileFormat,
        ];

        const sql = `
          INSERT INTO songs 
          (song_title, artist, album, file_size, uploader_id, uploader_name, duration, bitrate, song_cover_url, song_url, date_added, file_format) 
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `;

        await db.query(sql, songData);
        process.stdout.write(`✅\n`);
        successCount++;
      } catch (err) {
        process.stdout.write(`❌\n`);
        console.error(`   原因: ${err instanceof Error ? err.message : err}`);
        failCount++;
      }
    }

    console.log("\n" + "=".repeat(40));
    console.log(`✨ 重置同步完成！`);
    console.log(`✅ 成功导入: ${successCount} 首`);
    console.log("=".repeat(40) + "\n");
  } catch (error) {
    console.error("\n🔴 重大错误:", error);
  }
}

syncSongs();
