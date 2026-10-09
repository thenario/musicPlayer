from mcp import StdioServerParameters, Client
from pathlib import Path
import sys
import asyncio
async def main():
    path = Path(__file__).with_name("mcp_server.py").resolve()

    server = StdioServerParameters(
        command=sys.executable,
        args=[str(path)],  # 参数需要是字符串列表
        env={"PYTHONPATH": str(path.parent.parent)},
    )

    # 进入上下文时才启动服务并建立连接
    async with Client(server) as mcp_client:
        tools = await mcp_client.list_tools()
        print(tools)
if __name__ == "__main__":
    asyncio.run(main())
