import os
import shutil
import random

# ==================== 路径配置 ====================
# 源图片目录（你的音乐播放器封面文件夹）
src_dir = r'Z:\python_code\musicplayer_improved\static\uploader\covers'

# 目标目录（酒店项目文件夹）
hotel_target = r'Z:\hotel_manage\hotel_book_manage\src\static\hotel_pictures'
room_target = r'Z:\hotel_manage\hotel_book_manage\src\static\room_pictures'


# ==================== 核心逻辑 ====================
def sync_pictures():
    # 1. 检查并创建目标文件夹（如果不存在）
    for path in [hotel_target, room_target]:
        if not os.path.exists(path):
            os.makedirs(path)
            print(f"📁 已创建目录: {path}")

    # 2. 获取源目录下所有的图片文件
    all_images = [f for f in os.listdir(src_dir) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]

    if len(all_images) < 8:
        print(f"⚠️ 警告：源目录图片不足 8 张（当前只有 {len(all_images)} 张），可能会有重复使用。")

    # 3. 定义我们需要的目标文件名
    # 2张酒店图，6张房间图
    tasks = {
        hotel_target: [
            "hotel_1_main.jpg",
            "hotel_2_main.jpg"
        ],
        room_target: [
            "room_101_deluxe.jpg", "room_102_standard.jpg", "room_103_suite.jpg",
            "room_201_garden.jpg", "room_202_family.jpg", "room_203_loft.jpg"
        ]
    }

    # 4. 执行复制和重命名
    print("\n🚀 开始复制图片...")
    for target_path, file_names in tasks.items():
        for name in file_names:
            # 随机选一张源图
            source_file = random.choice(all_images)
            src_full_path = os.path.join(src_dir, source_file)
            dest_full_path = os.path.join(target_path, name)

            # 执行复制操作
            shutil.copy2(src_full_path, dest_full_path)
            print(f"✅ 已将 {source_file} 复制并重命名为 {name}")

    print("\n🎉 所有图片处理完毕！快去文件夹看看吧。")


if __name__ == "__main__":
    sync_pictures()