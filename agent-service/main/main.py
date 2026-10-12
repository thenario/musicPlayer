import asyncio
import os
import sys
from pathlib import Path
from typing import Any

from mcp import Client, StdioServerParameters

from agent import agent_chat

service_dir = Path(__file__).resolve().parent.parent
server_path = service_dir / "mcp" / "mcp_server.py"

# 修改原因：子进程使用当前解释器，并明确包搜索目录，才能导入 tools.search。
python_path = os.pathsep.join(
    part for part in (str(service_dir), os.getenv("PYTHONPATH")) if part
)
server = StdioServerParameters(
    command=sys.executable,
    args=[str(server_path)],
    env={"PYTHONPATH": python_path},
)


async def load_tools(mcp_client: Client) -> list[dict[str, Any]]:
    tools = []
    cursor = None
    while True:
        listed = await mcp_client.list_tools(cursor=cursor)
        tools.extend(
            {
                "type": "function",
                "name": tool.name,
                "description": tool.description or "",
                "parameters": tool.input_schema,
                # 修改原因：MCP 的 page 有默认值，关闭严格模式保留可选参数语义。
                "strict": False,
            }
            for tool in listed.tools
        )
        cursor = listed.next_cursor
        if cursor is None:
            return tools


async def main():
    # 修改原因：模型连接池、MCP 进程和工具定义在多轮聊天中复用，结束时统一关闭。
    async with agent_chat.create_model_client() as model_client:
        async with Client(server) as mcp_client:
            tools = await load_tools(mcp_client)
            turns: list[list[dict[str, Any]]] = []
            while True:
                try:
                    # 修改原因：input 阻塞，移到线程中让 MCP 后台接收任务继续运行。
                    question = (
                        await asyncio.to_thread(input, "请输入问题（exit 退出）\n")
                    ).strip()
                except EOFError:
                    return
                if question.lower() in {"exit", "quit"} or question == "退出":
                    return
                if not question:
                    continue

                message = [item for turn in turns for item in turn]
                start_index = len(message)
                # 修改原因：append 原地修改列表，返回 None，不能当成消息列表。
                message.append({"role": "user", "content": question})
                try:
                    async for content in agent_chat.chat(
                        message, tools, mcp_client, model_client=model_client
                    ):
                        # 流式分片不应每段换行，flush 确保即时显示。
                        print(content, end="", flush=True)
                except Exception as exc:
                    print(f"\n本轮请求失败，未保存本轮历史：{exc}")
                    continue

                print()
                # 修改原因：保存用户消息、模型输出及工具结果；append 不需要赋值。
                turns.append(message[start_index:])


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n对话已结束")

