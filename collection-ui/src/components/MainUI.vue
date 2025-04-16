<template>
    <div style="height: calc(100vh); overflow: hidden; background-color: rgb(243 244 246);">
        <el-container style="height: 100%; overflow: hidden">
            <el-aside style="width: 250px; display: flex; flex-direction: column;">
                <div class="el-menu-box">
                    <div class="logo-image"></div>
                    <div style="padding-left: 5px; padding-top: 3px">
                        KoChiu Collection
                    </div>
                </div>
                <div style="flex: 1; overflow-y: auto;">
                    <el-menu
                        class="el-menu-vertical-demo"
                        :default-active="activeMenu"
                        background-color="#fff"
                        text-color="#525252"
                        active-text-color="rgb(59,130,246)"
                        style="height: 100%"
                        :default-openeds="defaultOpeneds"
                    >
                        <div v-for="menuItem in menu" :key="menuItem.name">
                            <el-sub-menu
                                v-if="menuItem.children && menuItem.children.length"
                                :index="menuItem.path"
                                :key="menuItem.name"
                            >
                                <template #title>
                                    <div class="menu-icon">
                                        <el-icon v-if="menuItem.meta?.iconType === 'icons-vue' " :style="String(menuItem.meta?.style)">
                                            <component :is="menuItem.meta.icon" />
                                        </el-icon>
                                        <i v-else class="iconfont" :class="menuItem.meta?.icon"
                                           :style="String(menuItem.meta?.style)"></i>
                                    </div>
                                    <div class="menu-label">
                                        {{ menuItem.meta?.title || menuItem.name }}
                                    </div>
                                    <!-- 添加按钮 -->
                                    <div class="action-button" v-if="menuItem.path === '/Category'">
                                        <i class="iconfont icon-col-shujiegou add_category menu-tree" title="目录结构"/>
                                        <el-icon @click="addCategory" class="add_category" title="新分类">
                                            <Plus />
                                        </el-icon>
                                    </div>
                                </template>
                                <el-menu-item
                                    v-for="subMenuItem in menuItem.children"
                                    :index="subMenuItem.path"
                                    :route="{ name: subMenuItem.name }"
                                    :key="subMenuItem.name"
                                    @click="menuItemClick(subMenuItem)"
                                    style="cursor: pointer"
                                    :disabled="subMenuItem.meta?.disabled"
                                >
                                    <template #title>
                                        <div class="menu-icon">
                                            <el-icon v-if="subMenuItem.meta?.iconType === 'icons-vue' " :style="String(subMenuItem.meta?.style)">
                                                <component :is="subMenuItem.meta.icon" />
                                            </el-icon>
                                            <i v-else class="iconfont" :class="subMenuItem.meta?.icon"
                                               :style="String(subMenuItem.meta?.style)"></i>
                                        </div>
                                        <div class="menu-label">
                                            {{ subMenuItem.meta?.title || subMenuItem.name }}
                                        </div>
                                    </template>
                                </el-menu-item>
                            </el-sub-menu>

                            <el-menu-item
                                v-else
                                :index="menuItem.path"
                                :key="menuItem.path"
                                :route="{ name: menuItem.name }"
                                @click="menuItemClick(menuItem)"
                                style="cursor: pointer"
                                :disabled="menuItem.meta?.disabled"
                            >
                                <template #title>
                                    <div class="menu-icon">
                                        <el-icon v-if="menuItem.meta?.iconType === 'icons-vue' " :style="String(menuItem.meta?.style)">
                                            <component :is="menuItem.meta.icon" />
                                        </el-icon>
                                        <i v-else class="iconfont" :class="menuItem.meta?.icon"
                                           :style="String(menuItem.meta?.style)"></i>
                                    </div>
                                    <div class="menu-label">
                                        {{ menuItem.meta?.title || menuItem.name }}
                                    </div>
                                </template>
                            </el-menu-item>
                        </div>
                    </el-menu>
                </div>
            </el-aside>

            <el-container>
                <el-header class="headerCss">
                    <div style="display: flex; height: 100%; align-items: center">
                        <div
                            style="
                                text-align: left;
                                width: 33%;
                                font-size: 18px;
                                display: flex;
                            "
                        >
                        </div>
                        <div
                            style="
                                text-align: right;
                                width: 67%;
                                display: flex;
                                justify-content: right;
                                cursor: pointer;
                            "
                        >
                            <div style="width: 28px; height: 28px;">
                                <el-avatar :size="32" src="/images/user.gif" />
                            </div>
                            <div style="padding-left: 9px; padding-top: 6px">gaozhao</div>
                        </div>
                    </div>
                </el-header>

                <el-main class="el-main">
                    <RouterView v-slot="{ Component, route }">
                        <component
                            :is="Component"
                            :key="route.fullPath"
                            @menuItemClick="menuItemClick"
                        />
                    </RouterView>
                </el-main>
            </el-container>
        </el-container>
    </div>
    <!-- 对话框组件 -->
    <CategoryDialog
        ref="categoryDialog"
        @confirm="handleCategoryConfirm"
    />
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from "vue";
import router, { routes } from "@/apis/base-routes";
import { RouteRecordRaw } from "vue-router";
import { Plus } from "@element-plus/icons-vue"; // 导入listCategory方法
import { Category, ResourceType, Tag } from "@/apis/interface";
import CategoryDialog from "@/components/category/CategoryDialog.vue";
import { listCategory } from "@/apis/category-api";
import { listTag } from "@/apis/tag-api";
import { getResourceTypes } from "@/apis/system-api"; // 导入Category接口

const activeMenu = ref(''); // 当前激活的菜单项
const menu = ref(routes.filter(route => route.meta?.showInMenu !== false));
const defaultOpeneds = ref(['/My', '/Category']); // 添加默认展开的子菜单路径
const categoryDialog = ref()

// 初始化菜单
const initMenu = () => {
    menu.value = JSON.parse(JSON.stringify(routes.filter(route => route.meta?.showInMenu !== false)));
};

// 初始加载dom
onMounted(() => {
    initMenu();
    router.push(routes[0]);
    loadCategories(); // 调用加载分类的方法
    loadTags(); // 调用加载标签的方法
    loadTypes(); // 调用加载资源类型的方法
});

// 加载分类并更新菜单
const loadCategories = async () => {
    try {
        const categories: Category[] = await listCategory();
        updateCategoryMenu(categories);
    } catch (error) {
        console.error('加载分类失败:', error);
    }
};

// 更新分类菜单项
const updateCategoryMenu = (categories: Category[]) => {
    const currentPath = router.currentRoute.value.path;
    const categoryRoute = menu.value.find(route => route.path === '/Category');

    if (!categoryRoute) return;

    // 1. 创建动态路由项（明确类型）
    const dynamicItems: RouteRecordRaw[] = categories.map(category => ({
        path: `/Category/${category.sno}`,
        name: `category-${category.sno}`,
        component: () => import('@/components/category/CategoryResource.vue'),
        meta: {
            title: category.cateName,
            cateId: category.sno,
            icon: 'icon-col-fenlei3',
            iconType: 'iconfont',
            style: 'font-size: 18px; color: rgb(59,130,246)'
        }
    }));

    // 2. 获取现有"所有分类"项（明确类型）
    const allCategoryItem: RouteRecordRaw | undefined = categoryRoute.children?.find(
        child => child.name === 'allCategory'
    );

    // 3. 更新children数组
    categoryRoute.children = [
        ...dynamicItems,
        ...(allCategoryItem ? [allCategoryItem] : [])
    ];

    // 保持当前路由激活状态
    if (currentPath.startsWith('/Category')) {
        const currentSno = currentPath.split('/').pop();
        const exists = categories.some(c => c.sno.toString() === currentSno);

        if (exists) {
            nextTick(() => {
                activeMenu.value = currentPath;
                setTimeout(() => {
                    const activeItem = document.querySelector(`.el-menu-item[index="${currentPath}"]`);
                    activeItem?.scrollIntoView({ block: 'nearest' });
                }, 100);
            });
        }
    }
};

// 添加分类
const addCategory = (e: Event) => {
    e.stopPropagation()
    categoryDialog.value.open()
}

// 处理分类确认
const handleCategoryConfirm = async () => {
    await loadCategories()
}

// 加载标签的方法
const loadTags = async () => {
    try {
        const tags: Tag[] = await listTag();
        if (!tags || tags.length === 0) return;

        // 找到/tag路由并确保类型安全
        const tagRoute = menu.value.find(route => route.path === '/Tag') as RouteRecordRaw | undefined;
        if (!tagRoute || !tagRoute.children) return;

        // 获取现有的"所有标签"菜单项（明确类型）
        const allTagItem: RouteRecordRaw | undefined = tagRoute.children.find(
            child => child.name === 'allTag'
        );

        // 创建动态标签菜单项（明确RouteRecordRaw类型）
        const dynamicItems: RouteRecordRaw[] = tags.map(tag => ({
            path: `/Tag/${tag.tagId}`,
            name: `tag-${tag.tagId}`,
            component: () => import('@/components/tag/TagResource.vue'),
            meta: {
                title: tag.tagName,
                tagId: tag.tagId,
                icon: 'icon-col-biaoqian',
                iconType: 'iconfont',
                style: 'font-size: 18px; color: rgb(59,130,246)'
            }
        }));

        // 更新children数组（类型安全）
        tagRoute.children = [
            ...dynamicItems,
            ...(allTagItem ? [allTagItem] : [])
        ];

    } catch (error) {
        console.error('加载标签失败:', error);
    }
};

// 加载文件类型的方法
const loadTypes = async () => {
    try {
        const types: ResourceType[] = await getResourceTypes();
        if (!types || types.length === 0) return;

        // 找到/type路由并确保类型安全
        const typeRoute = menu.value.find(route => route.path === '/Type') as RouteRecordRaw | undefined;
        if (!typeRoute) return;

        // 创建动态类型菜单项（明确RouteRecordRaw类型）
        typeRoute.children = types.map(type => ({
            path: `/Type/${type.value}`,
            name: `type-${type.value}`,
            component: () => import('@/components/type/TypeResource.vue'),
            meta: {
                title: type.label,
                typeName: type.value,
                icon: `icon-col-${type.value}`,
                iconType: 'iconfont',
                style: 'font-size: 21px; color: rgb(59,130,246)'
            }
        })) as RouteRecordRaw[];

    } catch (error) {
        console.error('加载资源类型失败:', error);
    }
};

// 菜单项点击事件
const menuItemClick = (item: RouteRecordRaw) => {
    // 统一使用path跳转
    router.push(item.path).catch(err => {
        console.error('路由跳转失败:', err)
        // 失败时降级处理
        if (item.path.includes('/Category/')) {
            router.push('/AllCategory')
        }
        else if (item.path.includes('/Tag/')) {
            router.push('/AllTag')
        }
    })
}

</script>

<style scoped>
.el-menu-vertical-demo:not(.el-menu--collapse) {
    width: 250px;
    min-height: 400px;
    border-right: 0;
    font-weight: bold;
}

.el-menu-box {
    display: flex;
    padding-left: 25px;
    align-items: center;
    height: 58px;
    box-shadow: 0 1px 4px #00152914;
    border: 1px solid #00152914;
    color: white;
    background-color: rgb(75 85 99);
    flex-shrink: 0; /* Prevent the header from shrinking */
}

.el-main {
    padding-top: 0;
    padding-left: 1px;
    padding-right: 1px;
    margin: 0;
}

.headerCss {
    font-size: 12px;
    border: 1px solid #00152914;
    box-shadow: 1px 2px 4px #00152914;
    justify-content: right;
    align-items: center;
    height: 60px;
    color: white;
    background-color: rgb(55 65 81);
}

.logo-image {
    background-image: url("../assets/imgs/logo.jpg");
    width: 28px;
    height: 28px;
    background-size: 28px 28px;
    border-radius: 15px;
}

.el-header {
    --el-header-height: 45px
}

.el-main{
    margin: 0;
    padding: 0;
}

.menu-icon{
    width: 32px;
    float: left;
    text-align: center;
}

.menu-label{
    float: left;
    width: 100%;
    margin: 0 0 0 5px;
}

.add_category{
    cursor: pointer;
    margin-left: auto;
    font-size: 15px!important;
}

.add_category:hover{
    color: #409EFF;
}

.action-button{
    float: right;
    margin: 0 10px 0 0;
}

.menu-tree{
    float: left;
    font-size: 17px!important;
    margin: 2px 5px 0 0;
}
</style>