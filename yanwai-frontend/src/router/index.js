import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ResultView from '../views/ResultView.vue'
import CollectionView from '../views/CollectionView.vue'
import ProfileView from '../views/ProfileView.vue'
import HistoryView from '../views/HistoryView.vue'
import AuthView from '../views/AuthView.vue'
import NotFoundView from '../views/NotFoundView.vue'
import CheckinView from '../views/CheckinView.vue'
import FriendsView from '../views/FriendsView.vue'
import RankingView from '../views/RankingView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/auth',
    name: 'auth',
    component: AuthView
  },
  {
    path: '/home',
    redirect: '/'
  },
  {
    path: '/result',
    name: 'result',
    component: ResultView
  },
  {
    path: '/collection',
    name: 'collection',
    component: CollectionView
  },
  {
    path: '/profile',
    name: 'profile',
    component: ProfileView
  },
  {
    path: '/history',
    name: 'history',
    component: HistoryView
  },
  {
    path: '/checkin',
    name: 'checkin',
    component: CheckinView
  },
  {
    path: '/friends',
    name: 'friends',
    component: FriendsView
  },
  {
    path: '/ranking',
    name: 'ranking',
    component: RankingView
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFoundView
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('userId')
  if (!isLoggedIn && to.name !== 'auth') {
    next('/auth')
  } else {
    next()
  }
})

export default router
