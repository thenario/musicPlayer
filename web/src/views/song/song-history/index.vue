<template>
    <!-- 总container -->
    <div class="content-container"><!-- 页头，播放全部按钮 -->
        <div class="history-header">
            <span class="history-header-text">播放历史</span>
            <el-tooltip content="播放全部" placement="top">
                <el-button class="history-header-button" @click="handlePlayallSongFromHistory" text>
                    <el-icon>
                        <VideoPlay />
                    </el-icon>
                </el-button>
            </el-tooltip>
        </div>
        <!-- 内容区域,上限为一百项的滚动栏 -->
        <div class="history-content">
            <SongTable
                :songs="playHistory"
                :loading="false"
                :current-song-id="currentSong?.song_id"
                @play-now="handlePlayNow"
                @play-next="handlePlayNext"
            />
        </div>
    </div>
</template>
<script setup lang="ts">
import type { ISong } from '@/types';
import SongTable from '@/views/song/song-list/components/SongTable.vue';
import { VideoPlay } from '@element-plus/icons-vue';
import { onMounted, ref } from 'vue';

import { usePlayerStore } from '@/stores/player';
import { storeToRefs } from 'pinia';
// 假设这里有一个函数能获取历史播放，名字暂定为getPlayHistory
// song的结构体就用isong
const playerStore = usePlayerStore()
const { currentSong } = storeToRefs(playerStore)
const getPlayHistory = () => { }
// 这个函数同歌单全部播放的逻辑一样
const handlePlayallSongFromHistory = () => { }
const playHistory = ref<ISong[]>([
    {
        "song_id": "2097191485743222786",
        "song_title": "AIZO",
        "artist": "咒术回战",
        "album": "Unknown Album",
        "file_size": 7175271,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 216,
        "bitrate": 262,
        "song_cover_url": "/static/song_covers/auto-3d025082.jpg",
        "song_url": "/static/songs/咒术回战 - AIZO_57832e53.mp3",
        "date_added": "2026-09-08T13:13:09",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2092869288631459841",
        "song_title": "beyond the memory",
        "artist": "July",
        "album": "test",
        "file_size": 4941873,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 235,
        "bitrate": 132,
        "song_cover_url": "/static/song_covers/cover-3c05dd33.jpg",
        "song_url": "/static/songs/July - beyond the memory_6ce5b000.mp3",
        "date_added": "2026-08-27T14:58:17",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899232448811010",
        "song_title": "风月明日来",
        "artist": "鹤见江野",
        "album": "空与风的收藏",
        "file_size": 2676277,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 167,
        "bitrate": 123,
        "song_cover_url": "/static/song_covers/auto-83926cce.jpg",
        "song_url": "/static/songs/风月明日来_鹤见江野.mp3",
        "date_added": "2026-08-16T16:02:42",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899232385896449",
        "song_title": "风摇盛夏",
        "artist": "逆时针向",
        "album": "空与风的收藏",
        "file_size": 2599745,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 152,
        "bitrate": 126,
        "song_cover_url": "/static/song_covers/auto-578b001c.jpg",
        "song_url": "/static/songs/风摇盛夏_逆时针向.mp3",
        "date_added": "2026-08-16T16:02:42",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899232574640129",
        "song_title": "鸟之诗",
        "artist": "折戸伸治",
        "album": "AIR ORIGINAL SOUNDTRACK",
        "file_size": 1896349,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 202,
        "bitrate": 71,
        "song_cover_url": "/static/song_covers/auto-5646d069.jpg",
        "song_url": "/static/songs/鸟之诗 (八音盒版)_折户伸治.mp3",
        "date_added": "2026-08-16T16:02:42",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899232255873026",
        "song_title": "青衣",
        "artist": "九鸢&在下河帛",
        "album": "空与风的收藏",
        "file_size": 2256045,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 148,
        "bitrate": 119,
        "song_cover_url": "/static/song_covers/auto-773a0d8b.jpg",
        "song_url": "/static/songs/青衣_九鸢&在下河帛.mp3",
        "date_added": "2026-08-16T16:02:42",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899232771772417",
        "song_title": "이젠 남 (现在是陌生人) (Now, we are strangers｜Inst.)",
        "artist": "Noblesse (노블레스)",
        "album": "空与风的收藏",
        "file_size": 4406991,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 225,
        "bitrate": 146,
        "song_cover_url": "/static/song_covers/auto-1e7e54ca.jpg",
        "song_url": "/static/songs/이젠 남 (现在是陌生人) (Now, we are strangers｜Inst.)_Noblesse (노블레스).mp3",
        "date_added": "2026-08-16T16:02:42",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899232704663554",
        "song_title": "鸿迹",
        "artist": "变奏的梦想",
        "album": "空与风的收藏",
        "file_size": 4756018,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 273,
        "bitrate": 129,
        "song_cover_url": "/static/song_covers/auto-b369bd77.jpg",
        "song_url": "/static/songs/鸿迹_变奏的梦想.mp3",
        "date_added": "2026-08-16T16:02:42",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899232318787586",
        "song_title": "风与铃·夜与星",
        "artist": "邹牧虞",
        "album": "空与风的收藏",
        "file_size": 2885707,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 179,
        "bitrate": 118,
        "song_cover_url": "/static/song_covers/auto-5f4cdd5f.jpg",
        "song_url": "/static/songs/风与铃·夜与星_邹牧虞.mp3",
        "date_added": "2026-08-16T16:02:42",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899232511725570",
        "song_title": "飞升",
        "artist": "聲無哀樂乐队",
        "album": "空与风的收藏",
        "file_size": 5379941,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 295,
        "bitrate": 138,
        "song_cover_url": "/static/song_covers/auto-c16aa438.jpg",
        "song_url": "/static/songs/飞升_聲無哀樂乐队.mp3",
        "date_added": "2026-08-16T16:02:42",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899229009481730",
        "song_title": "海风，海风请带我走",
        "artist": "逆时针向",
        "album": "空与风的收藏",
        "file_size": 4520435,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 257,
        "bitrate": 131,
        "song_cover_url": "/static/song_covers/auto-8ddaa4d3.jpg",
        "song_url": "/static/songs/海风，海风请带我走_逆时针向.mp3",
        "date_added": "2026-08-16T16:02:41",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899228682326018",
        "song_title": "流星之夜",
        "artist": "MoFeansy",
        "album": "空与风的收藏",
        "file_size": 4045460,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 272,
        "bitrate": 106,
        "song_cover_url": "/static/song_covers/auto-389ac883.jpg",
        "song_url": "/static/songs/流星之夜_MoFeansy.mp3",
        "date_added": "2026-08-16T16:02:41",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899228745240577",
        "song_title": "浮光 (The History)",
        "artist": "Jannik",
        "album": "空与风的收藏",
        "file_size": 4082716,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 258,
        "bitrate": 120,
        "song_cover_url": "/static/song_covers/auto-14750041.jpg",
        "song_url": "/static/songs/浮光 (The History)_Jannik.mp3",
        "date_added": "2026-08-16T16:02:41",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899228808155137",
        "song_title": "浮生若梦",
        "artist": "逆时针向",
        "album": "空与风的收藏",
        "file_size": 2927065,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 200,
        "bitrate": 114,
        "song_cover_url": "/static/song_covers/auto-3e64051a.jpg",
        "song_url": "/static/songs/浮生若梦_逆时针向 .mp3",
        "date_added": "2026-08-16T16:02:41",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    },
    {
        "song_id": "2088899229072396289",
        "song_title": "游京",
        "artist": "海伦",
        "album": "空与风的收藏",
        "file_size": 3591395,
        "uploader_id": "2088884371316674561",
        "uploader_name": "空与风",
        "duration": 226,
        "bitrate": 118,
        "song_cover_url": "/static/song_covers/auto-64f38563.jpg",
        "song_url": "/static/songs/游京_海伦.mp3",
        "date_added": "2026-08-16T16:02:41",
        "file_format": "mp3",
        "lyrics": null,
        "t_lyrics": null
    }
])

const handlePlayNow = async (song: ISong) => {
    await playerStore.playSong(song, "now")
}

const handlePlayNext = async (song: ISong) => {
    const res = await playerStore.playSong(song, "next")
    if (res.success) {
        ElMessage.success(`已将《${song.song_title}》添加到下一首播放`)
    }
}

onMounted(() => {
    const songs = getPlayHistory()
    // playHistory.value = songs as ISong[]
})
</script>

<style scoped>
.content-container {
    box-sizing: border-box;
    width: 100%;
    height: 100%;
    min-height: 0;
    display: flex;
    flex-direction: column;
    padding: 24px;
    color: #20232d;
    background: #f7f8fa;
}

.history-header {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    margin-bottom: 20px;
}

.history-header-text {
    font-size: 24px;
    font-weight: 700;
    letter-spacing: -0.02em;
}

.history-header-button {
    font-size: 24px;
    color: #6256c5;
}

.history-content {
    flex: 1;
    min-width: 0;
    min-height: 0;
    overflow: hidden;
    border: 1px solid #e8eaef;
    border-radius: 12px;
    background: #ffffff;
}

@media (max-width: 640px) {
    .content-container {
        padding: 16px;
    }
}
</style>