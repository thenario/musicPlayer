import sqlite3


def update_lyrics_simple():
    """将所有歌曲的歌词更新为默认值 - 简单版"""

    db_path = r"Z:\python_code\musicplayer_improved\instance\music_player.db"

    try:
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()

        # 检查列是否存在
        cursor.execute("PRAGMA table_info(songs)")
        columns = [col[1] for col in cursor.fetchall()]

        if 'lyrics' not in columns:
            print("❌ lyrics列不存在，请先添加列")
            conn.close()
            return

        # 更新所有歌词
        default_lyrics = "暂无歌词，请欣赏"
        cursor.execute("UPDATE songs SET lyrics = ?", (default_lyrics,))

        print(f"✅ 已更新 {cursor.rowcount} 首歌曲的歌词")
        conn.commit()

        # 验证
        cursor.execute("SELECT COUNT(*) FROM songs WHERE lyrics = ?", (default_lyrics,))
        count = cursor.fetchone()[0]
        print(f"✅ 验证通过: {count} 首歌曲已更新为默认歌词")

        conn.close()

    except Exception as e:
        print(f"❌ 更新失败: {e}")


if __name__ == "__main__":
    update_lyrics_simple()