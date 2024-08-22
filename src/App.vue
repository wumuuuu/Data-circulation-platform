<script setup>
import {useWebSocket} from './service/wsService.js'
import { generateKeyPair, exportPublicKey, importServerPublicKey, deriveSharedKey, sendPublicKeyToServer, getServerPublicKey } from './service/DH.js';

async function initializeDH() {
  // 1. 前端生成密钥对
  const keyPair = await generateKeyPair();

  // 2. 导出前端的公钥并发送给后端
  const publicKey = await exportPublicKey(keyPair);
  await sendPublicKeyToServer(publicKey);

  // 3. 接收后端的公钥
  const serverPublicKeyData = await getServerPublicKey();
  const serverPublicKey = await importServerPublicKey(serverPublicKeyData);

  // 4. 生成共享密钥
  const sharedKey = await deriveSharedKey(keyPair, serverPublicKey);

  console.log('共享密钥已生成，所有通信将使用该密钥加密');
}

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
