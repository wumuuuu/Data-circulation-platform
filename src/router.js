// src/router.js
import { createRouter, createWebHistory } from 'vue-router'
import Home from './components/Home.vue'
import Login from './components/Login.vue'
import ApprovalProcess from './components/ApplicationsProcess.vue'
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
    component: Home,
    meta: { requiresAuth: true }  // 需要登录权限
  },
  {
    path: '/application',
    name: 'Application',
    component: Application,
    meta: { requiresAuth: true }  // 需要登录权限
  },
  {
    path: '/approvalProcess',
    name: 'ApprovalProcess',
    component: ApprovalProcess,
    meta: { requiresAuth: true }  // 需要登录权限
  }
];

const router = createRouter({
  history: createWebHistory(BASE_URL),
  routes
})

// 添加路由守卫
router.beforeEach((to, from, next) => {
  const isLoggedIn = !!localStorage.getItem('userToken'); // 通过 localStorage 判断用户是否已登录

  if (to.matched.some(record => record.meta.requiresAuth) && !isLoggedIn) {
    // 如果未登录且尝试访问需要登录的页面，则重定向到登录页面
    next({ name: 'Login' });
  } else {
    // 否则允许访问
    next();
  }
});


export default router
