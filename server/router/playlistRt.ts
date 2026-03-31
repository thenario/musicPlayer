import { Router } from "express";
import multer from "multer";
import path from "node:path";
import * as playlistCtrl from "../ctrl/playlistCtrl.js";
import { authMiddleWare } from "../utils/auth.js";
const playlistRt = Router();

const storage = multer.diskStorage({
  destination: (req: any, file: any, cb: any) => {
    if (file.mimetype.startsWith("image/")) {
      cb(null, "static/playlist_covers");
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

const upload = multer({ storage: storage });

playlistRt.post(
  "/",
  (req, res, next) => {
    upload.single("cover_image")(req, res, (err: any) => {
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
  authMiddleWare,
  playlistCtrl.createPlaylist,
);

playlistRt.patch(
  "",
  (req, res, next) => {
    upload.single("cover_image")(req, res, (err: any) => {
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
  authMiddleWare,
  playlistCtrl.editPlaylistDetail,
);

playlistRt.delete("/:id", authMiddleWare, playlistCtrl.deletePlaylist);

playlistRt.post("/:id/likes", authMiddleWare, playlistCtrl.likePlaylist);

playlistRt.delete("/:id/unlikes", authMiddleWare, playlistCtrl.unlikePlaylist);

playlistRt.get("/", authMiddleWare, playlistCtrl.getMyPlaylists);

playlistRt.get("/:id", authMiddleWare, playlistCtrl.getPlaylistById);

playlistRt.post(
  "/:playlist_id/songs/:song_id",
  authMiddleWare,
  playlistCtrl.addSongToPlaylist,
);

playlistRt.delete(
  "/:playlist_id/songs/:song_id",
  authMiddleWare,
  playlistCtrl.removeSongFromPlaylist,
);

export default playlistRt;
