// src/router.js
import { createRouter, createWebHistory } from 'vue-router'
import Home from './components/Home.vue'
import Login from './components/Login.vue'
import ApprovalProcess from './components/ApprovalProcess.vue'
import Application from '@/components/Application.vue'

const BASE_URL = '/'; // 这里手动设置 BASE_URL

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login
  },
  {
    path: '/home',
    name: 'Home',
    component: Home
  },
  {
    path: '/application',
    name: 'Application',
    component: Application
  },
  {
    path: '/approvalProcess',
    name: 'ApprovalProcess',
    component: ApprovalProcess
  },
]

const router = createRouter({
  history: createWebHistory(BASE_URL),
  routes
})

export default router
