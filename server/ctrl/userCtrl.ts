import type { Request, Response } from "express";
import db from "../db.js";
import dotenv from "dotenv";
import jwt from "jsonwebtoken";
import bcrypt from "bcrypt";

dotenv.config();

const JWT_SECRET = process.env.JWT_SECRET || "your_default_secret_safestring";
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || "24h";

interface LoginBody {
  user_name: string;
  password: string;
}

export const login = async (req: Request, res: Response) => {
  const { user_name, password } = req.body as LoginBody;

  if (!user_name || !password) {
    return res.status(400).json({ message: "用户名和密码必填", data: null });
  }

  try {
    const sql = `SELECT * FROM users WHERE user_name = ?`;

    const [rows]: any = await db.query(sql, [user_name]);

    if (!rows || rows.length === 0) {
      return res.status(404).json({ message: "账户不存在", data: null });
    }

    const user = rows[0];

    const isMatch = await bcrypt.compare(password, user.password);

    if (!isMatch) {
      return res.status(401).json({ message: "密码错误", data: null });
    }

    const token = jwt.sign(
      {
        user_id: user.user_id,
        user_name: user.user_name,
        user_email: user.user_email,
      },
      JWT_SECRET,
      { expiresIn: JWT_EXPIRES_IN as any },
    );

    return res.status(200).json({
      success: true,
      message: "登录成功",
      data: {
        user: {
          user_id: user.user_id,
          user_name: user.user_name,
          user_email: user.user_email,
        },
        token: token,
      },
    });
  } catch (error) {
    console.error("Login Error:", error);
    return res.status(500).json({ message: "服务器内部错误", data: null });
  }
};

export const register = async (req: Request, res: Response) => {
  const { user_name, user_email, password } = req.body;

  if (!user_email || !user_name || !password) {
    return res.status(400).json({
      message: "请输入完整信息（用户名，邮箱，密码）",
      data: null,
    });
  }

  try {
    let sql = ` 
      SELECT user_id FROM users WHERE user_name = ? OR user_email = ? LIMIT 1
    `;
    const [rows]: any = await db.query(sql, [user_name, user_email]);

    if (rows && rows.length > 0) {
      return res.status(409).json({
        message: "该用户名或邮箱已被注册",
        data: null,
      });
    } else {
      const saltRounds = 10;
      const hashedPassword = await bcrypt.hash(password, saltRounds);

      let insertSql = `
        INSERT INTO users (user_name, user_email, password) VALUES (?, ?, ?)
      `;
      await db.query(insertSql, [user_name, user_email, hashedPassword]);

      return res.status(200).json({
        success: true,
        message: "注册成功",
        data: null,
      });
    }
  } catch (error) {
    console.error("Register Error:", error);
    return res.status(500).json({
      message: "网络连接异常，请稍后重试",
      data: null,
    });
  }
};

export const logout = async (req: any, res: any) => {
  try {
    return res.json({
      success: true,
      message: "登出成功，期待下次再见",
      data: null,
    });
  } catch (err: any) {
    console.error("Logout Error:", err);

    return res.status(500).json({
      success: false,
      message: "登出过程中服务器出现异常",
      error: err.message,
    });
  }
};
