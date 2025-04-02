import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router';
import Help from '@/components/HelpPage.vue';
import { reactive } from "vue";

export const routes: RouteRecordRaw[] = reactive([]);

routes.push(
    {
        path: '/Category',
        name: 'category',
        meta: { title: '分类', icon: 'icon-col-fenlei', iconType: 'iconfont', style: 'font-size: 21px; color: rgb(59,130,246' },
        children: [
        ]
    },
    {
        path: '/My',
        name: "my",
        meta: { title: '我的', icon: 'Avatar', iconType: 'icons-vue', style: 'color: rgb(59,130,246)' },
        children: [
            {
                path: '/Upload',
                name: 'upload',
                component: () => import('@/components/my/FileUploader.vue'),
                meta: { title: '上传文件', icon: 'UploadFilled', iconType: 'icons-vue', style: 'color: rgb(59,130,246)' },
            },
            {
                path: '/BatchImport',
                name: 'batch-import',
                component: () => import('@/components/my/FileUploader.vue'),
                meta: { title: '批量导入', icon: 'icon-col-piliangdaoru1', iconType: 'iconfont', style: 'font-size: 18px; color: rgb(59,130,246); margin: 0 3px 0 0' },
            },
            ]
    },
    {
        path: '/Help',
        component: Help,
        name: "help",
        meta: { title: '帮助', icon: 'Help', iconType: 'icons-vue', style: 'color: rgb(59,130,246)' },
    },
)

//创建路由，并且暴露出去
const router = createRouter({
    history: createWebHashHistory(), //开发环境
    //history:createWebHistory(), //正式环境
    routes
})
export default router