package com.kyf.mp.javaserver.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class restore {

    // --- 数据库配置（已根据你的 yml 填好） ---
    private static final String URL = "jdbc:mysql://localhost:3306/musicPlayer?serverTimezone=GMT%2B8&characterEncoding=utf-8";
    private static final String USER = "root";
    private static final String PASS = "061019";

    public static void main(String[] args) {
        // 你要删除的硬编码前缀
        String target = "http://127.0.0.1";

        System.out.println("🚀 正在连接数据库并清理 URL...");

        // 使用 try-with-resources 自动关闭连接
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            // 1. 修复歌曲播放地址
            String sqlSong = "UPDATE songs SET song_url = REPLACE(song_url, ?, '') WHERE song_url LIKE ?";
            int songCount = executeUpdate(conn, sqlSong, target);
            System.out.println("✅ 歌曲地址清理完成，受影响行数: " + songCount);

            // 2. 修复歌曲封面地址
            String sqlCover = "UPDATE songs SET song_cover_url = REPLACE(song_cover_url, ?, '') WHERE song_cover_url LIKE ?";
            int coverCount = executeUpdate(conn, sqlCover, target);
            System.out.println("✅ 封面地址清理完成，受影响行数: " + coverCount);

            System.out.println("\n🎉 数据库修复成功！现在所有路径均为相对路径。");

        } catch (SQLException e) {
            System.err.println("❌ 运行失败，请检查数据库是否启动或账号密码是否正确！");
            e.printStackTrace();
        }
    }

    private static int executeUpdate(Connection conn, String sql, String target) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, target);
            pstmt.setString(2, target + "%"); // 模糊匹配以该前缀开头的数据
            return pstmt.executeUpdate();
        }
    }
}