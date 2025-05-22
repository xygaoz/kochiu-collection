import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router';
import { reactive } from "vue";
import { tokenStore } from "@/apis/system-api";

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
                meta: { title: '所有分类', icon: 'icon-col-fenlei2', iconType: 'iconfont', style: 'font-size: 21px; color: rgb(59,130,246', requiresAuth: true },
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
                meta: { title: '所有标签', icon: 'icon-col-24gl-tags4', iconType: 'iconfont', style: 'font-size: 17px; color: rgb(59,130,246', requiresAuth: true },
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
        meta: { title: '我的', icon: 'Collection', iconType: 'icons-vue', style: 'color: rgb(59,130,246); margin: 0px 9px 0px 4px;', group: 'my-group' },
        children: [
            {
                path: '/Upload',
                name: 'upload',
                component: () => import('@/components/my/FileUploader.vue'),
                meta: { title: '上传文件', icon: 'UploadFilled', iconType: 'icons-vue', style: 'color: rgb(59,130,246); margin: 0 7px 0 3px;', requiresAuth: true },
            },
            {
                path: '/BatchImport',
                name: 'batch-import',
                component: () => import('@/components/my/BatchImport.vue'),
                meta: { title: '批量导入', icon: 'icon-col-piliangdaoru1', iconType: 'iconfont', style: 'font-size: 18px; color: rgb(59,130,246); margin: 0px 10px 0px 6px;', requiresAuth: true },
            },
            {
                path: '/Public',
                name: 'public',
                component: () => import('@/components/my/PublicResource.vue'),
                meta: { title: '公共资源', icon: 'icon-col-gonggongziyuan', iconType: 'iconfont', style: 'font-size: 18px; color: rgb(59,130,246); margin: 0px 10px 0px 6px;', requiresAuth: true },
            },
            {
                path: '/Recycle',
                name: 'recycle',
                component: () => import('@/components/my/RecycleResource.vue'),
                meta: { title: '回收站', icon: 'icon-col-huishouzhanx', iconType: 'iconfont', style: 'font-size: 25px; color: rgb(59,130,246); margin: 0px 6px 0px 3px;', requiresAuth: true },
            },
            {
                path: '/profile',
                name: 'Profile',
                component: () => import('@/components/my/Profile.vue'),
                meta: { title: '个人资料', icon: 'icon-col-gerenziliao', iconType: 'iconfont', style: 'font-size: 18px; color: rgb(59,130,246); margin: 0px 9px 0px 6px;', requiresAuth: true }
            },
        ]
    },
    {
        path: '/Help',
        name: "help",
        component: () => import('@/components/HelpPage.vue'),
        meta: { title: '帮助', icon: 'Help', iconType: 'icons-vue', style: 'color: rgb(59,130,246); margin: 0px 8px 0px 5px;', group: 'help-group' },
        children: []
    },
    // router.ts 中添加动态路由规则
    {
        path: '/Category/:cateId',  // 动态参数（注意大小写统一）
        name: 'category-detail',    // 固定名称
        component: () => import('@/components/category/CategoryResource.vue'),
        meta: { showInMenu: false, requiresAuth: true },
        props: true  // 将路由参数自动作为props传递
    },
    {
        path: '/Tag/:tagId',  // 动态参数（注意大小写统一）
        name: 'tag-detail',    // 固定名称
        component: () => import('@/components/tag/TagResource.vue'),
        meta: { showInMenu: false, requiresAuth: true },
        props: true  // 将路由参数自动作为props传递
    },
    {
        path: '/Type/:typeName',  // 动态参数（注意大小写统一）
        name: 'type-detail',    // 固定名称
        component: () => import('@/components/type/TypeResource.vue'),
        meta: { showInMenu: false, requiresAuth: true },
        props: true  // 将路由参数自动作为props传递
    },
    {
        path: '/Catalog/:id',  // 动态参数
        name: 'catalog-detail',
        component: () => import('@/components/catalog/CatalogResource.vue'),
        meta: { showInMenu: false, requiresAuth: true },
        props: true
    },
    {
        path: '/Catalog/refresh',
        redirect: to => {
            return { path: to.query.redirect as string || '/' }
        }
    },
    {
        path: '/Category/refresh',
        redirect: to => {
            return { path: to.query.redirect as string || '/' }
        }
    },
)

//创建路由，并且暴露出去
const router = createRouter({
    history: createWebHashHistory(), //开发环境
    //history:createWebHistory(), //正式环境
    routes
})

//路由守卫
router.beforeEach((to) => {
    if (to.meta.requiresAuth && !tokenStore.getToken()) {
        return '/login'
    }
})

export default router