from typing import Annotated
from pydantic import Field
import httpx
import asyncio


async def searchSongs(
    keyword: Annotated[
        str,
        Field(description="搜索歌曲的关键字,关键字作用于歌曲名字，或者歌曲作者"),
    ],
    page: Annotated[
        int,
        Field(
            description="查询页码，从 1 开始；切换页码可获取其他结果",
            ge=1,
        ),
    ] = 1,
) -> dict:
    async with httpx.AsyncClient(timeout=10.0) as client:
        try:
            response = await client.get(
                "http://127.0.0.1:8080/api/agent/songs",
                params={"keyword": keyword, "page": page},
            )
            response.raise_for_status()
        except httpx.TimeoutException:
            return {"message": "请求超时"}

        except httpx.RequestError as exc:
            return {"message": f"网络请求失败：{exc}"}

        except httpx.HTTPStatusError as exc:
            return {
                "message": exc.response.json().get("message"),
            }
        song_data = response.json().get("data").get("records")
    return {"songs": song_data}


async def searchUsers(
    keyword: Annotated[
        str, Field(description="搜索用户的关键字，关键字作用于用户名字")
    ],
    page: Annotated[
        int, Field(description="查询页码，从 1 开始；切换页码可获取其他结果", ge=1)
    ] = 1,
):
    async with httpx.AsyncClient(timeout=10.0) as client:
        try:
            response = await client.get(
                "http://127.0.0.1:8080/api/agent/users",
                params={"keyword": keyword, "page": page},
            )
            response.raise_for_status()
        except httpx.TimeoutException:
            return {"message": "请求超时"}

        except httpx.RequestError as exc:
            return {"message": f"网络请求失败：{exc}"}

        except httpx.HTTPStatusError as exc:
            return {
                "message": exc.response.json().get("message"),
            }
        users_data = response.json().get("data").get("records")
    return {"users": users_data}


async def searchPlaylists(
    keyword: Annotated[
        str, Field(description="搜索歌单的关键字，关键字作用于歌单的名字")
    ],
    page: Annotated[
        int, Field(description="查询页码，从 1 开始；切换页码可获取其他结果", ge=1)
    ] = 1,
):
    async with httpx.AsyncClient(timeout=10.0) as client:
        try:
            response = await client.get(
                "http://127.0.0.1:8080/api/agent/playlists",
                params={"keyword": keyword, "page": page},
            )
            response.raise_for_status()
        except httpx.TimeoutException:
            return {"message": "请求超时"}

        except httpx.RequestError as exc:
            return {"message": f"网络请求失败：{exc}"}

        except httpx.HTTPStatusError as exc:
            return {
                "message": exc.response.json().get("message"),
            }
        playlists_data = response.json().get("data").get("records")
    return {"playlists": playlists_data}


if __name__ == "__main__":
    result1 = asyncio.run(searchUsers("空"))
    result2 = asyncio.run(searchSongs("空"))
    result3 = asyncio.run(searchPlaylists("空"))
    print(result1)
    print(result2)
    print(result3)
