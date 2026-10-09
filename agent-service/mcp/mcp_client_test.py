from mcp import StdioServerParameters, Client
from pathlib import Path
import sys
import asyncio


async def main():
    path = Path(__file__).with_name("mcp_server.py").resolve()

    server = StdioServerParameters(
        command=sys.executable,
        args=[str(path)],
        env={"PYTHONPATH": str(path.parent.parent)},
    )

    async with Client(server) as mcp_client:
        tools = await mcp_client.list_tools()
        print(tools)


if __name__ == "__main__":
    asyncio.run(main())
