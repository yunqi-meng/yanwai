import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据看板', icon: 'DataBoard' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'user/:id',
        name: 'UserDetail',
        component: () => import('@/views/user/detail.vue'),
        meta: { title: '用户详情', hidden: true }
      },
      {
        path: 'content/card',
        name: 'Card',
        component: () => import('@/views/content/card.vue'),
        meta: { title: '卡牌管理', icon: 'Postcard' }
      },
      {
        path: 'content/achievement',
        name: 'Achievement',
        component: () => import('@/views/content/achievement.vue'),
        meta: { title: '成就管理', icon: 'Trophy' }
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/analysis/index.vue'),
        meta: { title: '分析记录', icon: 'Document' }
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/config.vue'),
        meta: { title: '系统配置', icon: 'Setting' }
      },
      {
        path: 'system/log',
        name: 'OperationLog',
        component: () => import('@/views/system/log.vue'),
        meta: { title: '操作日志', icon: 'Notebook' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 言外管理后台` : '言外管理后台'
  
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
