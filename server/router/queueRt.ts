import { Router } from "express";
import * as queueCtrl from "../ctrl/queueCtrl.js";
import { authMiddleWare } from "../utils/auth.js";
const queueRt = Router();

queueRt.get("/current", authMiddleWare, queueCtrl.getCurrentQueue);

queueRt.patch(
  "/current/state",
  authMiddleWare,
  queueCtrl.updateCurrentQueueState,
);

queueRt.put(
  "/player/current-queue",
  authMiddleWare,
  queueCtrl.alterQueueToCurrent,
);

queueRt.get("/", authMiddleWare, queueCtrl.getMyQueues);

queueRt.post("/", authMiddleWare, queueCtrl.createQueueFromPlaylist);

queueRt.get("/:queue_id", authMiddleWare, queueCtrl.getQueueById);

queueRt.delete("/:queue_id", authMiddleWare, queueCtrl.deletQueue);

queueRt.delete("/:queue_id/songs", authMiddleWare, queueCtrl.clearQueue);

queueRt.post("/:queue_id/songs", authMiddleWare, queueCtrl.addSongToQueue);

queueRt.delete(
  "/:queue_id/songs/:queue_item_id",
  authMiddleWare,
  queueCtrl.removeSongFromQueue,
);

queueRt.patch("/:queue_id/play-mode", authMiddleWare, queueCtrl.setPlayMode);

queueRt.patch("/:queue_id/reorder", authMiddleWare, queueCtrl.reorderQueue);

export default queueRt;
