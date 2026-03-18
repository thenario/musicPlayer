import { Router } from "express";
import * as queueCtrl from "../ctrl/queueCtrl.js";
import { authMiddleWare } from "../utils/auth.js";
const queueRt = Router();

queueRt.get("/queues", authMiddleWare, queueCtrl.getMyQueues);
queueRt.get("/:queue_id", authMiddleWare, queueCtrl.getQueueById);
queueRt.get("/current", authMiddleWare, queueCtrl.getCurrentQueue);
queueRt.put(
  "/player/current-queue",
  authMiddleWare,
  queueCtrl.alterQueueToCurrent,
);
queueRt.delete("/:id", authMiddleWare, queueCtrl.deletQueue);
queueRt.post(":queue_id/songs", authMiddleWare, queueCtrl.addSongToQueue);
queueRt.delete(
  "/:queue_id/songs/:queue_item_id",
  authMiddleWare,
  queueCtrl.removeSongFromQueue,
);
queueRt.patch("/:queue_id", authMiddleWare, queueCtrl.setPlayMode);
queueRt.patch("/:queue_id", authMiddleWare, queueCtrl.reorderQueue);
queueRt.post("/", authMiddleWare, queueCtrl.createQueueFromPlaylist);
queueRt.patch(
  "/current/state",
  authMiddleWare,
  queueCtrl.updateCurrentQueueState,
);
queueRt.delete("/:queue_id/songs", authMiddleWare, queueCtrl.clearQueue);

export default queueRt;
