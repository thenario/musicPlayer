from mcp.server import MCPServer
from tools.search import searchPlaylists, searchSongs, searchUsers

server = MCPServer("server")

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
