import { Router } from "express";
import * as userCtrl from "../ctrl/userCtrl.js";
const userRt = Router();

userRt.use("/login", userCtrl.login);
userRt.use("/register", userCtrl.register);

export default userRt;
