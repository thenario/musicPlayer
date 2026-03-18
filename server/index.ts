import express from "express";
import path from "node:path";
import cors from "cors";
import userRt from "./router/userRt.js";
import songRt from "./router/songRt.js";
import playlistRt from "./router/playlistRt.js";
import queueRt from "./router/queueRt.js";
import choreRt from "./router/choreRt.js";

const app = express();
app.use(express.json());
app.use(cors());
app.use("/static", express.static(path.join(process.cwd(), "static")));
app.use("/api/users", userRt);
app.use("/api/songs", songRt);
app.use("/api/playlist", playlistRt);
app.use("/api/queues", queueRt);
app.use("/api/stats", choreRt);

const PORT = 3000;
const HOST = "127.0.0.1";

app.listen(PORT, HOST, () => {
  console.log(`后端服务运行于: http://${HOST}:${PORT}`);
});
