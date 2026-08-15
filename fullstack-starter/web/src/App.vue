<script setup lang="ts">
import { ref } from 'vue'
import { http, type ApiResponse } from './api/http'
const status = ref('checking…')
async function checkHealth() { const { data } = await http.get<ApiResponse<{ status: string }>>('/health'); status.value = data.data.status }
void checkHealth()
</script>
<template><main><h1>Fullstack Starter</h1><p>API: {{ status }}</p></main></template>
