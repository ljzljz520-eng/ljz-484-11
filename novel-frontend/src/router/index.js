import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Detail from '../views/Detail.vue'
import Read from '../views/Read.vue'
import AuthorHome from '../views/author/AuthorHome.vue'
import AuthorNovelManage from '../views/author/AuthorNovelManage.vue'
import AuthorChapterEdit from '../views/author/AuthorChapterEdit.vue'

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
    // 作者后台：作品与章节草稿管理（接口实现均在 novel-backend 工程）
    {
        path: '/author',
        name: 'AuthorHome',
        component: AuthorHome
    },
    {
        path: '/author/novel/:id',
        name: 'AuthorNovelManage',
        component: AuthorNovelManage
    },
    {
        path: '/author/novel/:novelId/chapter/:chapterId',
        name: 'AuthorChapterEdit',
        component: AuthorChapterEdit
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
