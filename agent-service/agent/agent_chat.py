import json
import os
from collections.abc import AsyncIterator
from copy import deepcopy
from pathlib import Path
from typing import Any

from dotenv import load_dotenv
from mcp import Client
from openai import AsyncOpenAI

# 修改原因：按文件位置加载配置，从其他目录启动也能找到服务自己的 .env。
load_dotenv(Path(__file__).resolve().parent.parent / ".env")
MODEL_NAME = os.getenv("MODEL_NAME")


def create_model_client() -> AsyncOpenAI:
    """启动时创建一次，由调用方的 async with 负责关闭连接池。"""
    api_key = os.getenv("DEEPSEEK_APIKEY")
    if not api_key or not MODEL_NAME:
        raise ValueError("请在 agent-service/.env 中配置 DEEPSEEK_APIKEY 和 MODEL_NAME")
    # 修改原因：同步客户端会阻塞异步接口；模型客户端与 MCP 客户端要分开。
    return AsyncOpenAI(
        api_key=api_key,
        base_url=os.getenv("BASEURL") or None,
        timeout=60.0,
    )


def _tool_result_text(result: Any) -> str:
    """把 MCP 结果转换为 function_call_output 接受的字符串。"""
    # 修改原因：content 可以有多个类型，不能假定每个块都有 .text。
    text = "\n".join(part.text for part in result.content if part.type == "text")
    if result.is_error:
        return json.dumps({"error": text or "工具执行失败"}, ensure_ascii=False)
    if result.structured_content is not None:
        return json.dumps(result.structured_content, ensure_ascii=False)
    return text


async def chat(
    msg: list[dict[str, Any]],
    tools: list[dict[str, Any]],
    mcp_client: Client,
    *,
    model_client: AsyncOpenAI,
    max_rounds: int = 5,
) -> AsyncIterator[str]:
    """流式输出正文；成功后把本轮完整记录提交到调用方的 msg 列表。"""
    if max_rounds < 1:
        raise ValueError("max_rounds 必须大于等于 1")

    # 修改原因：处理副本，流中断/循环超限时不留下缺少工具结果的半轮历史。
    messages = deepcopy(msg)
    allowed_tools = {tool["name"] for tool in tools}

    for _ in range(max_rounds):
        stream = await model_client.responses.create(
            stream=True,
            model=MODEL_NAME,
            input=messages,
            tools=tools,
            # 自定义 BASEURL 的模型未必支持 reasoning.effort，按需再添加。
        )
        complete_response = None
        emitted_text = False
        # 修改原因：异步迭代避免阻塞，退出时及时释放本次 HTTP 响应的连接。
        async with stream:
            async for event in stream:
                if event.type == "response.output_text.delta":
                    emitted_text = emitted_text or bool(event.delta)
                    yield event.delta
                elif event.type == "response.completed":
                    complete_response = event.response
                elif event.type in {"response.failed", "response.incomplete"}:
                    raise RuntimeError(f"模型响应未成功完成：{event.type}")
                elif event.type == "error":
                    raise RuntimeError(f"模型流式错误：{event.message}")

        if complete_response is None:
            raise RuntimeError("模型连接已结束，但没有收到 response.completed 事件")

        # 修改原因：正文、函数调用、推理项一起保留，模型才能恢复完整上下文。
        messages.extend(
            item.model_dump(mode="json", by_alias=True, exclude_none=True)
            for item in complete_response.output
        )
        # 兼容只返回完整正文的服务；已有 delta 时不能再输出一次，否则正文重复。
        if not emitted_text and complete_response.output_text:
            yield complete_response.output_text

        tool_calls = [
            item for item in complete_response.output if item.type == "function_call"
        ]
        if not tool_calls:
            # 修改原因：切片赋值更新调用方的列表，局部重新赋值不会更新调用方。
            msg[:] = messages
            return

        for tool_call in tool_calls:
            try:
                if tool_call.name not in allowed_tools:
                    raise ValueError(f"当前 agent 没有注册工具：{tool_call.name}")
                arguments = json.loads(tool_call.arguments)
                if not isinstance(arguments, dict):
                    raise ValueError("工具参数必须是 JSON 对象")
                result = await mcp_client.call_tool(tool_call.name, arguments)
                output = _tool_result_text(result)
            except Exception as exc:
                # 修改原因：外部工具失败也要回传对应 call_id，供模型解释或修正。
                output = json.dumps({"error": str(exc)}, ensure_ascii=False)

            messages.append(
                {
                    "type": "function_call_output",
                    "call_id": tool_call.call_id,
                    "output": output,
                }
            )

    # 修改原因：限制一次用户请求内的模型轮数，超限不能悄悄当成回答成功。
    raise RuntimeError(f"agent 达到 {max_rounds} 轮模型调用上限，仍未生成最终回答")

