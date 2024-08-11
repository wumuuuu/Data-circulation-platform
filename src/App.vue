<template>
  <div id="app">
<!--    <Home></Home>-->
<!--    <Application></Application>-->
<!--    <ApprovalProcess></ApprovalProcess>-->
    <router-view></router-view>
  </div>
</template>

<script>
import Home from '@/components/Home.vue'
import ApprovalProcess from '@/components/ApprovalProcess.vue'
import Application from '@/components/Application.vue'

export default {
  name: 'App',
  components: { Application, ApprovalProcess, Home }
}
</script>
<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

</style>
