import express from "express";
import path from "node:path";
import cors from "cors";
import userRt from "./router/userRt.js";
import songRt from "./router/songRt.js";
import playlistRt from "./router/playlistRt.js";
import queueRt from "./router/queueRt.js";
import choreRt from "./router/choreRt.js";
import { fileURLToPath } from "node:url";

const app = express();
const __dirname = path.dirname(fileURLToPath(import.meta.url));

app.use(express.json());
app.use(cors());
app.use("/static", express.static(path.join(process.cwd(), "static")));
app.use(express.static(path.join(process.cwd(), "dist")));
app.use("/api/users", userRt);
app.use("/api/songs", songRt);
app.use("/api/playlists", playlistRt);
app.use("/api/queues", queueRt);
app.use("/api/stats", choreRt);

app.get(/.*/, (req, res) => {
  res.sendFile(path.join(process.cwd(), "dist", "index.html"));
});

const PORT = 3000;
const HOST = "127.0.0.1";

app.listen(PORT, HOST, () => {
  console.log(`后端服务运行于: http://${HOST}:${PORT}`);
});
