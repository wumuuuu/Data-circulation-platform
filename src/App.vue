<script setup>
import {useWebSocket} from './service/wsService.js'
import {initializeDH} from './utils/DH.js'

// 在 setup 中调用 initializeDH 函数，确保在应用启动时进行密钥交换
initializeDH();



// 在 setup 中调用 useWebSocket 函数，确保在应用启动时建立 WebSocket 连接
useWebSocket();

</script>
<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>
<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

</style>
