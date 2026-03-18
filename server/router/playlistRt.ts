import { Router } from "express";
import multer from "multer";
import path from "node:path";
import * as playlistCtrl from "../ctrl/playlistCtrl.js";
import { authMiddleWare } from "../utils/auth.js";
const playlistRt = Router();

const storage = multer.diskStorage({
  destination: (req: any, file: any, cb: any) => {
    if (!file.mimetype.startsWith("/image"))
      cb(new Error("不支持此封面"), false);
    else {
      const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1e9);
      const playlist_cover_name =
        file.fieldname + "-" + uniqueSuffix + path.extname(file.originalname);
      cb(null, playlist_cover_name);
    }
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

playlistRt.delete("/:id", authMiddleWare, playlistCtrl.deletePlaylist);

playlistRt.post("/:id/likes", authMiddleWare, playlistCtrl.likePlaylist);

playlistRt.get("/playlists", authMiddleWare, playlistCtrl.getMyPlaylists);

playlistRt.get("/playlists/:id", authMiddleWare, playlistCtrl.getPlaylistById);

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
