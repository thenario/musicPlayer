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
            <SongTable :songs="historySongs" :loading="false" :current-song-id="currentSong?.song_id"
                @play-now="handlePlayNow" @play-next="handlePlayNext" />
        </div>
    </div>
</template>
<script setup lang="ts">
import type { ISong } from '@/types';
import SongTable from '@/views/song/song-list/components/SongTable.vue';
import { VideoPlay } from '@element-plus/icons-vue';
import { computed, onActivated, onMounted } from 'vue';

import { usePlayerStore } from '@/stores/player';
import { useSongStore } from '@/stores/song';
import { storeToRefs } from 'pinia';

const playerStore = usePlayerStore()
const songStore = useSongStore()
const { currentSong } = storeToRefs(playerStore)
const { playHistory } = storeToRefs(songStore)

const handlePlayallSongFromHistory = async () => {
    const res = await songStore.playAllHistory()
    if(!res.success){
        ElMessage.error("播放失败")
        return
    }
    ElMessage.success("播放成功")
    return
}

const handlePlayNow = async (song: ISong) => {
    await playerStore.playSong(song, "now")
}

const handlePlayNext = async (song: ISong) => {
    const res = await playerStore.playSong(song, "next")
    if (res.success) {
        ElMessage.success(`已将《${song.song_title}》添加到下一首播放`)
    }
}

const historySongs = computed(() =>
    playHistory.value.map(item => item.song)
)

const loadHistory = async () => {
    const res = await songStore.getPlayHistory()
    if (!res.success) {
        ElMessage.error("历史记录获取失败")
    }
}

let activatedOnce = false
const refreshHistory = async () => {
    if (!activatedOnce) {
        activatedOnce = true
        return
    }
    await loadHistory()
}

onMounted(loadHistory)
onActivated(refreshHistory)
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
