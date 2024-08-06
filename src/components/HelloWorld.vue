<template>
  <div v-bind:class="msg">测试</div>
  <button :disabled="ok">button</button>
</template>

<script>
  export default {
    data(){
      return{
        msg:"active",
        ok:false
      }
    }
  }
</script>