import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Detail from '../views/Detail.vue'
import Read from '../views/Read.vue'
import AuthorHome from '../views/author/AuthorHome.vue'
import ChapterManage from '../views/author/ChapterManage.vue'
import ChapterEdit from '../views/author/ChapterEdit.vue'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home
    },
    {
        path: '/novel/:id',
        name: 'Detail',
        component: Detail
    },
    {
        path: '/chapter/:id',
        name: 'Read',
        component: Read
    },
    {
        path: '/author',
        name: 'AuthorHome',
        component: AuthorHome
    },
    {
        path: '/author/novels/:novelId/chapters',
        name: 'ChapterManage',
        component: ChapterManage
    },
    {
        path: '/author/novels/:novelId/chapters/new',
        name: 'ChapterNew',
        component: ChapterEdit
    },
    {
        path: '/author/chapters/:id/edit',
        name: 'ChapterEdit',
        component: ChapterEdit
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
