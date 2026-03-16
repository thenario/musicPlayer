import { Router } from "express";
import * as queueCtrl from "../ctrl/queueCtrl.js";
import { authMiddleWare } from "../utils/auth.js";
const queueRt = Router();

queueRt.get("/queues", authMiddleWare, queueCtrl.getMyQueues);
queueRt.get("/queues/:queue_id", authMiddleWare, queueCtrl.getQueueById);
queueRt.get("/queues/current", authMiddleWare, queueCtrl.getCurrentQueue);
queueRt.put(
  "/player/current-queue",
  authMiddleWare,
  queueCtrl.alterQueueToCurrent,
);
queueRt.delete("/queues/:id", authMiddleWare, queueCtrl.deletQueue);
queueRt.post(
  "/queues/:queue_id/songs",
  authMiddleWare,
  queueCtrl.addSongToQueue,
);
queueRt.delete(
  "/queues/:queue_id/songs/:queue_item_id",
  authMiddleWare,
  queueCtrl.removeSongFromQueue,
);
queueRt.patch("/queues/:queue_id", authMiddleWare, queueCtrl.setPlayMode);
queueRt.patch("/queues/:queue_id", authMiddleWare, queueCtrl.reorderQueue);
queueRt.post("/queues", authMiddleWare, queueCtrl.createQueueFromPlaylist);
queueRt.patch(
  "/queues/current/state",
  authMiddleWare,
  queueCtrl.updateCurrentQueueState,
);
queueRt.delete("/queues/:queue_id/songs", authMiddleWare, queueCtrl.clearQueue);

export default queueRt;
