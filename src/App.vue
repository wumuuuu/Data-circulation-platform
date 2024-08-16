<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script setup>
import Home from '@/components/Home.vue'
import ApprovalProcess from '@/components/ApplicationsProcess.vue'
import Application from '@/components/Application.vue'
import {useWebSocket} from './service/wsService.js'

// 在 setup 中调用 useWebSocket 函数，确保在应用启动时建立 WebSocket 连接
useWebSocket();
</script>
<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

</style>
