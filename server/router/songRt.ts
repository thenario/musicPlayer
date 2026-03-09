import { Router } from "express";
const songRt = Router();
import multer from "multer";
import path from "node:path";
import * as songCtrl from "../ctrl/songCtrl.js";

const storage = multer.diskStorage({
  destination: (req: any, file: any, cb: any) => {
    if (file.memetype.startWith("/image")) {
      cb(null, "/static/song_covers");
    } else if (file.mimeType.startWith("/audio")) {
      cb(null, "/static/songs");
    } else {
      cb(new Error("不支持此种文件类型"), false);
    }
  },
  filename: (req: any, file: any, cb: any) => {
    const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1e9);
    cb(
      null,
      file.fieldname + "-" + uniqueSuffix + path.extname(file.originalname),
    );
  },
});

const uploadSong = multer({ storage: storage });

songRt.post(
  "/api/songs",
  (req, res, next) => {
    uploadSong.fields([
      { name: "audiofile", maxCount: 1 },
      { name: "coverfile", maxCount: 1 },
    ])(req, res, (err) => {
      if (err instanceof multer.MulterError) {
        return res
          .status(400)
          .json({ message: `上传工具错误: ${err.message}` });
      } else if (err) {
        return res.status(400).json({ message: err.message });
      }
      next();
    });
  },
  songCtrl.uploadSong,
);

export default songRt;
