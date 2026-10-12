from mcp.server import MCPServer
from tools.search import searchPlaylists, searchSongs, searchUsers, search_lifespan

# 修改原因：HTTP 连接池跟随 MCP 服务启动/关闭，不在每次工具调用时重复创建。
server = MCPServer("server", lifespan=search_lifespan)

server.add_tool(
    searchSongs,
    name="search_songs",
    description="可以查询数据库中的歌曲",
)
server.add_tool(
    searchUsers,
    name="search_users",
    description="可以查询数据库中的用户",
)
server.add_tool(
    searchPlaylists,
    name="search_playlists",
    description="可以查询数据库中的歌单",
)

if __name__ == "__main__":
    server.run()
