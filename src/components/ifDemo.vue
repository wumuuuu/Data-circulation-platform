<template>
  <h3> 条件渲染 </h3>
  <div v-if="flag">ok</div>
  <p v-for="i in names">{{i}}</p>
  <button @click="addit">add</button>
  <button @click="sum=0">clear</button>
  <p>{{sum}}</p>
  用户名：<input type="text">
  密码：<input type="text">
</template>

<script>
  export default {
    data() {
      return{
        flag : 1,
        names : [1,2,3,4],
        sum:0
      }
    },
    methods:{
      addit(){
        this.sum++
      }

    }
  }
</script>

<style scoped>

</style>