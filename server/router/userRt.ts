import { Router } from "express";
import * as userCtrl from "../ctrl/userCtrl.js";
const userRt = Router();

userRt.post("/login", userCtrl.login);
userRt.post("/register", userCtrl.register);
userRt.post("/logout", userCtrl.logout);

export default userRt;
