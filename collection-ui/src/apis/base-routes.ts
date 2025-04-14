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
            {
                path: '/AllCategory',
                name: 'allCategory',
                component: () => import('@/components/category/AllCategory.vue'),
                meta: { title: '所有分类', icon: 'icon-col-fenlei2', iconType: 'iconfont', style: 'font-size: 21px; color: rgb(59,130,246' },
            },
        ]
    },
    {
        path: '/Tag',
        name: 'tag',
        meta: { title: '标签', icon: 'icon-col-biaoqian1', iconType: 'iconfont', style: 'font-size: 20px; color: rgb(59,130,246); margin: 0 1px 0 0;' },
        children: [
            {
                path: '/AllTag',
                name: 'allTag',
                component: () => import('@/components/tag/AllTag.vue'),
                meta: { title: '所有标签', icon: 'icon-col-24gl-tags4', iconType: 'iconfont', style: 'font-size: 17px; color: rgb(59,130,246' },
            },
        ]
    },
    {
        path: '/Type',
        name: 'type',
        meta: { title: '文件类型', icon: 'icon-col-duomeitiicon-', iconType: 'iconfont', style: 'font-size: 20px; color: rgb(59,130,246); margin: 0 1px 0 0;' },
        children: [
        ]
    },
    {
        path: '/My',
        name: "my",
        meta: { title: '我的', icon: 'Avatar', iconType: 'icons-vue', style: 'color: rgb(59,130,246); margin: 0 -1px 0 0;' },
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
            {
                path: '/Recycle',
                name: 'recycle',
                component: () => import('@/components/my/Recycle.vue'),
                meta: { title: '回收站', icon: 'icon-col-huishouzhanx', iconType: 'iconfont', style: 'font-size: 22px; color: rgb(59,130,246); margin: 0 1px 0 0' },
            },
        ]
    },
    {
        path: '/System',
        name: 'system',
        meta: { title: '系统管理', icon: 'icon-col-shezhi', iconType: 'iconfont', style: 'font-size: 21px; color: rgb(59,130,246' },
        children: [
        ]
    },
    {
        path: '/Help',
        component: Help,
        name: "help",
        meta: { title: '帮助', icon: 'Help', iconType: 'icons-vue', style: 'color: rgb(59,130,246)' },
    },
    // router.ts 中添加动态路由规则
    {
        path: '/Category/:cateId',  // 动态参数（注意大小写统一）
        name: 'category-detail',    // 固定名称
        component: () => import('@/components/category/CategoryResource.vue'),
        meta: { showInMenu: false },
        props: true  // 将路由参数自动作为props传递
    },
    {
        path: '/Tag/:tagId',  // 动态参数（注意大小写统一）
        name: 'tag-detail',    // 固定名称
        component: () => import('@/components/tag/TagResource.vue'),
        meta: { showInMenu: false },
        props: true  // 将路由参数自动作为props传递
    },
    {
        path: '/Type/:typeName',  // 动态参数（注意大小写统一）
        name: 'type-detail',    // 固定名称
        component: () => import('@/components/type/TypeResource.vue'),
        meta: { showInMenu: false },
        props: true  // 将路由参数自动作为props传递
    },
)

//创建路由，并且暴露出去
const router = createRouter({
    history: createWebHashHistory(), //开发环境
    //history:createWebHistory(), //正式环境
    routes
})
export default router