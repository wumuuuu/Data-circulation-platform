import { createRouter, createWebHistory } from 'vue-router'
import Home from './components/Home.vue'
// import ApprovalProcess from './components/ApplicationsProcess.vue'
import Application from '@/components/Application.vue'
import Auth from '@/components/Auth.vue'
import UserMgr from '@/components/UserMgr.vue'
import Handle from '@/components/Handle.vue'
import Examine1 from '@/components/Examine1.vue'
import Examine2 from '@/components/Examine2.vue'

const BASE_URL = '/'; // 手动设置 BASE_URL

const routes = [
  {
    path: '/',
    name: 'Auth',
    component: Auth
  },
  // {
  //   path: '/home',
  //   name: 'Home',
  //   component: Home,
  //   meta: { requiresAuth: true }  // 需要登录权限
  // },
  {
    path: '/application',
    name: 'Application',
    component: Application,
    meta: { requiresAuth: true }  // 需要登录权限
  },
  {
    path: '/handle',
    name: 'Handle',
    component: Handle,
    meta: { requiresAuth: true }  // 需要登录权限
  },
  {
    path: '/examine1',
    name: 'Examine1',
    component: Examine1,
    meta: { requiresAuth: true }  // 需要登录权限
  },
  {
    path: '/examine2',
    name: 'Examine2',
    component: Examine2,
    meta: { requiresAuth: true }  // 需要登录权限
  },
  // {
  //   path: '/approvalProcess',
  //   name: 'ApprovalProcess',
  //   component: ApprovalProcess,
  //   meta: { requiresAuth: true }  // 需要登录权限
  // }
  {
    path: '/userMgr',
    name: 'UserMgr',
    component: UserMgr,
    meta: { requiresAuth: true }  // 需要登录权限
  }
];

const router = createRouter({
  history: createWebHistory(BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const isLoggedIn = !!localStorage.getItem('authToken'); // 通过 localStorage 判断用户是否已登录

  if (to.matched.some(record => record.meta.requiresAuth) && !isLoggedIn) {
    // 如果未登录且尝试访问需要登录的页面，则重定向到登录页面
    next({ name: 'Auth' });
  } else {
    // 否则允许访问
    next();
  }
});

// 添加 handleSelect 方法
export const handleSelect = (index) => {
  switch (index) {
    case '1':
      router.push('/home');
      break;
    case '2':
      router.push('/application');
      break;
    case '3':
      router.push('/handle');
      break;
    case '4':
      router.push('/examine1');
      break;
    case '5':
      router.push('/examine2');
      break;
    case '6':
      router.push('/userMgr');
      break;
  }
};

//点击登出并跳转到登录界面
export const handleCommand = (command) => {
  if (command === 'logout') {
    // 清理登录状态，例如移除 token 或用户信息
    localStorage.clear();
    sessionStorage.clear();
    console.log('localStorage');
    // 然后跳转到登录页面
    router.push('/');
  }
};

export default router;
