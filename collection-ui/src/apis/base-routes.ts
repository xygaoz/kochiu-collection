import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router';
import Help from '@/components/HelpPage.vue';
import { reactive } from "vue";

export const routes: RouteRecordRaw[] = reactive([]);

routes.push(
    {
        path: '/category',
        name: "分类",
        meta: { icon: 'icon-col-fenlei', iconType: 'iconfont', style: 'font-size: 21px; color: rgb(59,130,246' },
        children: [
        ]
    },
    {
        path: '/my',
        name: "我的",
        meta: { icon: 'Avatar', iconType: 'icons-vue', style: 'color: rgb(59,130,246)' },
        children: [
            {
                path: 'Upload',
                name: '上传文件',
                component: () => import('@/components/my/FileUploader.vue'),
                meta: { icon: 'UploadFilled', iconType: 'icons-vue', style: 'color: rgb(59,130,246)' },
            },
            {
                path: 'Batch',
                name: '批量导入',
                component: () => import('@/components/my/FileUploader.vue'),
                meta: { icon: 'icon-col-piliangdaoru1', iconType: 'iconfont', style: 'font-size: 18px; color: rgb(59,130,246); margin: 0 3px 0 0' },
            },
            ]
    },
    {
        path: '/help',
        component: Help,
        name: "帮助",
        meta: { icon: 'Help', iconType: 'icons-vue', style: 'color: rgb(59,130,246)' },
    },
)

//创建路由，并且暴露出去
const router = createRouter({
    history: createWebHashHistory(), //开发环境
    //history:createWebHistory(), //正式环境
    routes
})
export default router