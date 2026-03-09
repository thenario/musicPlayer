import express from "express";
import path from "node:path";
import cors from "cors";
import userRt from "./router/userRt.js";
import songRt from "./router/songRt.js";
import playlistRt from "./router/playlistRt.js";
import queueRt from "./router/queueRt.js";
import choreRt from "./router/choreRt.js";

const app = express();
app.use(express.json);
app.use(cors());
app.use("/static", express.static(path.join(process.cwd(), "static")));
app.use("/api/user", userRt);
app.use("/api/songs", songRt);
app.use("/api/playlist", playlistRt);
app.use("/api/queue", queueRt);
app.use("/api/chore", choreRt);
