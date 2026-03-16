import { Router } from "express";
import * as choreCtrl from "../ctrl/choreCtrl.js";
const choreRt = Router();

choreRt.get("stats", choreCtrl.getStatics);

export default choreRt;
