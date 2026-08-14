#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# 脚手架实例化脚本：从本模板生成一个可运行的新项目
#
# 用法：./scaffold.sh <项目名> <Java包名>
#   例：./scaffold.sh my-app com.example.myapp
#   生成结果：本仓库上级目录的 <项目名>/，含前后端 + 部署骨架。
#
# 自动推导：
#   数据库名 = 项目名（- 转 _），如 my-app -> my_app
# ============================================================

if [ $# -lt 2 ]; then
  echo "用法: ./scaffold.sh <项目名(kebab-case)> <Java包名>"
  echo "示例: ./scaffold.sh my-app com.example.myapp"
  exit 1
fi

PROJECT_NAME="$1"
PACKAGE="$2"
DB_NAME="${PROJECT_NAME//-/_}"

OLD_PKG_PATH="com/kyf/mp/server"
NEW_PKG_PATH="${PACKAGE//./\/}"

SCAFFOLD_DIR="$(cd "$(dirname "$0")" && pwd)"
TARGET_DIR="$(dirname "$SCAFFOLD_DIR")/${PROJECT_NAME}"

if [ -e "$TARGET_DIR" ]; then
  echo "错误：目标目录已存在：$TARGET_DIR"
  exit 1
fi

echo "==> 项目名: ${PROJECT_NAME}"
echo "==> 包名:   ${PACKAGE}"
echo "==> 数据库: ${DB_NAME}"
echo "==> 目标:   ${TARGET_DIR}"

# 1. 复制模板（排除大目录、git 元数据与本脚本）
mkdir -p "$TARGET_DIR"
tar -C "$SCAFFOLD_DIR" \
  --exclude='.git' --exclude='node_modules' --exclude='dist' --exclude='target' \
  --exclude='scaffold.sh' --exclude="$PROJECT_NAME" \
  -cf - . | tar -C "$TARGET_DIR" -xf -

# 2. 重命名 Java 目录 com/kyf/mp/server -> 目标包目录
for scope in main test; do
  old="$TARGET_DIR/backend/src/$scope/java/$OLD_PKG_PATH"
  new="$TARGET_DIR/backend/src/$scope/java/$NEW_PKG_PATH"
  if [ -d "$old" ]; then
    mkdir -p "$(dirname "$new")"
    mv "$old" "$new"
  fi
done

# 清理 mv 后残留的空父目录（如 com/kyf/mp）
find "$TARGET_DIR/backend/src" -type d -empty -delete 2>/dev/null || true

# 3. 全局替换：包名（点/斜杠）与数据库名
find "$TARGET_DIR" -type f \
  \( -name '*.java' -o -name '*.xml' -o -name '*.yml' -o -name '*.yaml' \
     -o -name '*.properties' -o -name '*.ts' -o -name '*.vue' -o -name '*.json' \
     -o -name '*.md' -o -name '*.html' \) \
  -exec sed -i \
    -e "s/com\.kyf\.mp\.server/${PACKAGE}/g" \
    -e "s#${OLD_PKG_PATH}#${NEW_PKG_PATH}#g" \
    -e "s/musicPlayer/${DB_NAME}/g" \
    {} +

# 4. pom.xml 的 artifactId/name（精确替换，避免误伤 server 通用词）
sed -i "s/<artifactId>server<\/artifactId>/<artifactId>${PROJECT_NAME}<\/artifactId>/" \
  "$TARGET_DIR/backend/pom.xml"
sed -i "s/<name>server<\/name>/<name>${PROJECT_NAME}<\/name>/" \
  "$TARGET_DIR/backend/pom.xml"

# 5. application.yml 应用名
sed -i "s/    name: server/    name: ${PROJECT_NAME}/" \
  "$TARGET_DIR/backend/src/main/resources/application.yml"

echo ""
echo "==> 完成！新项目在：$TARGET_DIR"
echo "    接下来：cd $TARGET_DIR && 填 .env，然后前后端分别启动"
