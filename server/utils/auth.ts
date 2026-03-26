import dotenv from "dotenv";
import type { Request, Response, NextFunction } from "express";
import jwt from "jsonwebtoken";

dotenv.config();

const SECRET_KEY =
  process.env.JWT_SECRET || "deault1234567890qwertyuiopasdfghjklzxcvbnm";

export const authMiddleWare = (
  req: Request,
  res: Response,
  next: NextFunction,
) => {
  const auth = req.headers["authorization"];
  const token = auth?.split(" ")[1] || (req.query.token as string);
  if (!token) {
    return res.status(401).json({
      code: 401,
      success: false,
      message: "未提供访问令牌",
    });
  } else {
    try {
      const decoded = jwt.verify(token, SECRET_KEY);
      (req as any).user = decoded;
      next();
    } catch (error) {
      console.log(error);
      return res.status(403).json({
        code: 403,
        success: false,
        message: "Token 无效或已过期",
      });
    }
  }
};
