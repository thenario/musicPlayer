import asyncio
from contextlib import asynccontextmanager
from typing import Annotated, Any

import httpx
from pydantic import Field

_http_client: httpx.AsyncClient | None = None


@asynccontextmanager
async def search_lifespan(_server=None):
    global _http_client
    # 修改原因：三个查询工具共享 HTTP 连接池，在 MCP 服务关闭时再释放。
    async with httpx.AsyncClient(timeout=10.0) as client:
        previous_client = _http_client
        _http_client = client
        try:
            yield
        finally:
            _http_client = previous_client


async def _request_records(
    client: httpx.AsyncClient,
    resource: str,
    keyword: str,
    page: int,
    result_key: str,
) -> dict[str, Any]:
    try:
        response = await client.get(
            f"http://127.0.0.1:8080/api/agent/{resource}",
            params={"keyword": keyword, "page": page},
        )
        response.raise_for_status()
    except httpx.TimeoutException:
        return {"message": "请求超时"}
    except httpx.RequestError as exc:
        return {"message": f"网络请求失败：{exc}"}
    except httpx.HTTPStatusError as exc:
        # 修改原因：网关可能返回 HTML，错误处理不能再因为 JSON 解析而报错。
        try:
            body = exc.response.json()
        except ValueError:
            body = None
        message = body.get("message") if isinstance(body, dict) else None
        return {"message": message or f"业务接口返回 HTTP {exc.response.status_code}"}

    try:
        body = response.json()
    except ValueError:
        return {"message": "业务接口没有返回有效的 JSON"}
    if not isinstance(body, dict):
        return {"message": "业务接口响应格式错误：应返回 JSON 对象"}

    # 修改原因：data 可能为 null/缺失，不能直接连续调用两次 .get()。
    data = body.get("data")
    if not isinstance(data, dict) or not isinstance(data.get("records"), list):
        return {"message": body.get("message") or "业务接口响应缺少 data.records 列表"}
    return {result_key: data["records"]}


async def _search(
    resource: str, keyword: str, page: int, result_key: str
) -> dict[str, Any]:
    if _http_client is not None:
        return await _request_records(_http_client, resource, keyword, page, result_key)
    # 独立调用也能使用；MCP 运行时使用上面的长期连接池。
    async with httpx.AsyncClient(timeout=10.0) as client:
        return await _request_records(client, resource, keyword, page, result_key)


async def searchSongs(
    keyword: Annotated[
        str, Field(description="搜索歌曲的关键字，作用于歌曲名字或歌曲作者")
    ],
    page: Annotated[int, Field(description="查询页码，从 1 开始", ge=1)] = 1,
) -> dict[str, Any]:
    return await _search("songs", keyword, page, "songs")


async def searchUsers(
    keyword: Annotated[str, Field(description="搜索用户的关键字，作用于用户名字")],
    page: Annotated[int, Field(description="查询页码，从 1 开始", ge=1)] = 1,
) -> dict[str, Any]:
    return await _search("users", keyword, page, "users")


async def searchPlaylists(
    keyword: Annotated[str, Field(description="搜索歌单的关键字，作用于歌单名字")],
    page: Annotated[int, Field(description="查询页码，从 1 开始", ge=1)] = 1,
) -> dict[str, Any]:
    return await _search("playlists", keyword, page, "playlists")


async def main():
    # 修改原因：三个独立测试共用一个事件循环和连接池。
    async with search_lifespan():
        for search in (searchUsers, searchSongs, searchPlaylists):
            print(await search("空"))


if __name__ == "__main__":
    asyncio.run(main())

