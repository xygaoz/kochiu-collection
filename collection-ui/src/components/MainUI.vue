<template>
    <div style="height: calc(100vh); overflow: hidden;">
        <el-container style="height: 100%; overflow: hidden">
            <el-aside style="width: 250px; display: flex; flex-direction: column;">
                <div class="el-menu-box">
                    <div class="logo-image"></div>
                    <div style="padding-left: 5px; padding-top: 3px">
                        KoChiu Collection
                    </div>
                </div>
                <div class="menu-area">
                    <!-- 顶部切换区域：分类/目录 -->
                    <div style="flex-shrink: 0;">
                        <!-- 目录菜单 -->
                        <el-menu
                            v-if="showCatalogMenu"
                            class="catalog-menu"
                            :default-active="activeMenu"
                            background-color="#fff"
                            text-color="#525252"
                            active-text-color="rgb(59,130,246)"
                            :default-openeds="['catalog-group']"
                        >
                            <catalog-menu-item
                                index="catalog-group"
                                v-for="item in catalogTreeData"
                                :key="item.id"
                                :item="item"
                                @node-click="handleNodeClick"
                                @toggle="toggleCatalogMenu"
                                @new-catalog="handleAddCatalog"
                                @edit-catalog="handleEditCatalog"
                                @delete-catalog="handleDeleteCatalog"
                            />
                        </el-menu>

                        <!-- 分类菜单 -->
                        <el-menu
                            v-else
                            class="category-menu"
                            :default-active="activeMenu"
                            background-color="#fff"
                            text-color="#525252"
                            active-text-color="rgb(59,130,246)"
                            :default-openeds="['category-group']"
                        >
                            <el-sub-menu index="category-group">
                                <template #title>
                                    <div class="menu-icon">
                                        <i class="iconfont icon-col-fenlei" style="font-size: 21px; color: rgb(59,130,246)"></i>
                                    </div>
                                    <div class="menu-label">分类</div>
                                    <div class="action-button">
                                        <el-icon @click.stop="toggleCatalogMenu" class="new-level" title="切换到目录">
                                            <Switch />
                                        </el-icon>
                                        <el-icon @click.stop="handleAddCategory" class="new-level" title="新分类">
                                            <Plus />
                                        </el-icon>
                                    </div>
                                </template>
                                <el-menu-item
                                    v-for="category in categories"
                                    :key="category.sno"
                                    :index="`/Category/${category.sno}`"
                                    @click="menuItemClick({path: `/Category/${category.sno}`})"
                                >
                                    <template #title>
                                        <div
                                            class="menu-item-content"
                                            @mouseenter="showCategoryActions = category.sno"
                                            @mouseleave="showCategoryActions = null"
                                        >
                                            <div class="menu-icon">
                                                <i class="iconfont icon-col-fenlei3" style="font-size: 18px; color: rgb(59,130,246)"></i>
                                            </div>
                                            <div class="menu-label">
                                                {{ category.cateName }}
                                            </div>
                                            <div class="action-buttons" v-if="showCategoryActions === category.sno">
                                                <el-icon
                                                    @click.stop="handleEditCategory(category)"
                                                    class="action-icon"
                                                    title="修改分类"
                                                >
                                                    <Edit />
                                                </el-icon>
                                                <el-icon
                                                    @click.stop="handleDeleteCategory(category)"
                                                    class="action-icon"
                                                    title="删除分类"
                                                >
                                                    <Delete />
                                                </el-icon>
                                            </div>
                                        </div>
                                    </template>
                                </el-menu-item>
                                <el-menu-item index="/AllCategory" @click="menuItemClick({path: '/AllCategory'})">
                                    <template #title>
                                        <div class="menu-icon">
                                            <i class="iconfont icon-col-fenlei2" style="font-size: 21px; color: rgb(59,130,246)"></i>
                                        </div>
                                        <div class="menu-label">所有分类</div>
                                    </template>
                                </el-menu-item>
                            </el-sub-menu>
                        </el-menu>
                    </div>

                    <!-- 固定菜单区域 -->
                    <el-menu
                        class="fixed-menu"
                        :default-active="activeMenu"
                        background-color="#fff"
                        text-color="#525252"
                        active-text-color="rgb(59,130,246)"
                        :default-openeds="defaultOpeneds"
                    >
                        <!-- 标签菜单 -->
                        <el-sub-menu index="tag-group">
                            <template #title>
                                <div class="menu-icon">
                                    <i class="iconfont icon-col-biaoqian1" style="font-size: 20px; color: rgb(59,130,246); margin: 0 1px 0 0;"></i>
                                </div>
                                <div class="menu-label">标签</div>
                            </template>
                            <el-menu-item
                                v-for="tag in tags"
                                :key="tag.tagId"
                                :index="`/Tag/${tag.tagId}`"
                                @click="menuItemClick({path: `/Tag/${tag.tagId}`})"
                            >
                                <template #title>
                                    <div class="menu-icon">
                                        <i class="iconfont icon-col-biaoqian" style="font-size: 18px; color: rgb(59,130,246)"></i>
                                    </div>
                                    <div class="menu-label">{{ tag.tagName }}</div>
                                </template>
                            </el-menu-item>
                            <el-menu-item index="/AllTag" @click="menuItemClick({path: '/AllTag'})">
                                <template #title>
                                    <div class="menu-icon">
                                        <i class="iconfont icon-col-24gl-tags4" style="font-size: 17px; color: rgb(59,130,246)"></i>
                                    </div>
                                    <div class="menu-label">所有标签</div>
                                </template>
                            </el-menu-item>
                        </el-sub-menu>

                        <!-- 文件类型菜单 -->
                        <el-sub-menu index="type-group">
                            <template #title>
                                <div class="menu-icon">
                                    <i class="iconfont icon-col-duomeitiicon-" style="font-size: 20px; color: rgb(59,130,246); margin: 0 1px 0 0;"></i>
                                </div>
                                <div class="menu-label">文件类型</div>
                            </template>
                            <el-menu-item
                                v-for="type in resourceTypes"
                                :key="type.value"
                                :index="`/Type/${type.value}`"
                                @click="menuItemClick({path: `/Type/${type.value}`})"
                            >
                                <template #title>
                                    <div class="menu-icon">
                                        <i :class="`iconfont icon-col-${type.value}`" style="font-size: 21px; color: rgb(59,130,246)"></i>
                                    </div>
                                    <div class="menu-label">{{ type.label }}</div>
                                </template>
                            </el-menu-item>
                        </el-sub-menu>

                        <!-- 动态生成特定菜单项 -->
                        <template v-for="menu in fixedMenuItems" :key="menu.path">
                            <!-- 有子菜单的情况（资源、系统管理） -->
                            <el-sub-menu
                                v-if="menu.children && menu.children.length > 0"
                                :index="menu.path"
                            >
                                <template #title>
                                    <div class="menu-icon">
                                        <template v-if="menu.meta?.iconType === 'iconfont'">
                                            <i
                                                :class="`iconfont ${menu.meta?.icon}`"
                                                :style="menu.meta?.style as StyleValue"
                                            ></i>
                                        </template>
                                        <template v-else>
                                            <el-icon :style="menu.meta?.style as StyleValue">
                                                <component :is="menu.meta?.icon" />
                                            </el-icon>
                                        </template>
                                    </div>
                                    <div class="menu-label">{{ menu.meta?.title }}</div>
                                </template>

                                <el-menu-item
                                    v-for="child in menu.children"
                                    :key="child.path"
                                    :index="child.path"
                                    @click="menuItemClick({path: child.path})"
                                >
                                    <template #title>
                                        <div class="menu-icon">
                                            <template v-if="child.meta?.iconType === 'iconfont'">
                                                <i
                                                    :class="`iconfont ${child.meta?.icon}`"
                                                    :style="child.meta?.style as StyleValue"
                                                ></i>
                                            </template>
                                            <template v-else>
                                                <el-icon :style="child.meta?.style">
                                                    <component :is="child.meta?.icon" />
                                                </el-icon>
                                            </template>
                                        </div>
                                        <div class="menu-label">{{ child.meta?.title }}</div>
                                    </template>
                                </el-menu-item>
                            </el-sub-menu>

                            <!-- 没有子菜单的情况（帮助） -->
                            <el-menu-item
                                v-else
                                :index="menu.path"
                                @click="menuItemClick({path: menu.path})"
                            >
                                <template #title>
                                    <div class="menu-icon">
                                        <template v-if="menu.meta?.iconType === 'iconfont'">
                                            <i
                                                :class="`iconfont ${menu.meta?.icon}`"
                                                :style="menu.meta?.style as StyleValue"
                                            ></i>
                                        </template>
                                        <template v-else>
                                            <el-icon :style="menu.meta?.style">
                                                <component :is="menu.meta?.icon" />
                                            </el-icon>
                                        </template>
                                    </div>
                                    <div class="menu-label">{{ menu.meta?.title }}</div>
                                </template>
                            </el-menu-item>
                        </template>

                        <!-- 动态生成特定菜单项 -->
                        <template v-for="menu in dynamicMenus" :key="menu.path">
                            <!-- 有子菜单的情况 -->
                            <el-sub-menu
                                v-if="menu.children && menu.children.length > 0"
                                :index="menu.path"
                            >
                                <template #title>
                                    <div class="menu-icon">
                                        <template v-if="menu.meta.iconType === 'iconfont'">
                                            <i
                                                :class="`iconfont ${menu.meta.icon}`"
                                                :style="menu.meta.style"
                                            ></i>
                                        </template>
                                        <template v-else>
                                            <el-icon :style="menu.meta.style">
                                                <component :is="menu.meta.icon" />
                                            </el-icon>
                                        </template>
                                    </div>
                                    <div class="menu-label">{{ menu.meta.title }}</div>
                                </template>

                                <el-menu-item
                                    v-for="child in menu.children"
                                    :key="child.path"
                                    :index="child.path"
                                    @click="menuItemClick({path: child.path})"
                                >
                                    <template #title>
                                        <div class="menu-icon">
                                            <template v-if="child.meta.iconType === 'iconfont'">
                                                <i
                                                    :class="`iconfont ${child.meta.icon}`"
                                                    :style="child.meta.style"
                                                ></i>
                                            </template>
                                            <template v-else>
                                                <el-icon :style="child.meta.style">
                                                    <component :is="child.meta.icon" />
                                                </el-icon>
                                            </template>
                                        </div>
                                        <div class="menu-label">{{ child.meta.title }}</div>
                                    </template>
                                </el-menu-item>
                            </el-sub-menu>

                            <!-- 没有子菜单的情况 -->
                            <el-menu-item
                                v-else
                                :index="menu.path"
                                @click="menuItemClick({path: menu.path})"
                            >
                                <template #title>
                                    <div class="menu-icon">
                                        <template v-if="menu.meta.iconType === 'iconfont'">
                                            <i
                                                :class="`iconfont ${menu.meta.icon}`"
                                                :style="menu.meta.style"
                                            ></i>
                                        </template>
                                        <template v-else>
                                            <el-icon :style="menu.meta.style">
                                                <component :is="menu.meta.icon" />
                                            </el-icon>
                                        </template>
                                    </div>
                                    <div class="menu-label">{{ menu.meta.title }}</div>
                                </template>
                            </el-menu-item>
                        </template>
                    </el-menu>
                </div>
            </el-aside>

            <el-container class="content-area">
                <el-header class="headerCss">
                    <div style="display: flex; height: 100%; align-items: center; justify-content: space-between;">
                        <div style="width: 33%;"></div> <!-- 左侧占位 -->
                        <div class="user-dropdown-container">
                            <el-dropdown>
                                <div class="user-avatar-wrapper">
                                    <el-avatar
                                        :size="32"
                                        src="/images/user.gif"
                                        class="user-avatar"
                                    />
                                    <span class="username">{{ username }}</span>
                                    <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
                                </div>
                                <template #dropdown>
                                    <el-dropdown-menu class="user-dropdown-menu">
                                        <el-dropdown-item @click="showProfile">
                                            <el-icon><User /></el-icon>
                                            <span>个人资料</span>
                                        </el-dropdown-item>
                                        <el-dropdown-item divided @click="handleLogout">
                                            <el-icon><SwitchButton /></el-icon>
                                            <span>退出登录</span>
                                        </el-dropdown-item>
                                    </el-dropdown-menu>
                                </template>
                            </el-dropdown>
                        </div>
                    </div>
                </el-header>

                <el-main class="el-main">
                    <RouterView v-slot="{ Component, route }">
                        <component :is="Component" :key="route.fullPath" />
                    </RouterView>
                </el-main>
            </el-container>
        </el-container>
    </div>

    <!-- 对话框组件 -->
    <CategoryDialog ref="categoryDialog" @confirm="handleCategoryConfirm" />
    <CatalogDialog ref="catalogDialog" @confirm="handleCatalogConfirm" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, StyleValue } from "vue";
import { ArrowDown, Delete, Edit, Plus, Switch, SwitchButton, User } from "@element-plus/icons-vue";
import { listCategory } from "@/apis/category-api";
import { listTag } from "@/apis/tag-api";
import { getResourceTypes } from "@/apis/system-api";
import { getCatalogTree } from "@/apis/catalog-api";
import CatalogMenuItem from "@/components/catalog/CatalogMenuItem.vue";
import { Catalog, Category, Menu, ResourceType, RouteMenu, Tag } from "@/apis/interface";
import CategoryDialog from "@/components/category/CategoryDialog.vue";
import CatalogDialog from "@/components/catalog/CatalogDialog.vue";
import { useRoute, useRouter } from "vue-router";
import { getMyMenu, logout } from "@/apis/user-api";
import { useUserStore } from "@/apis/global";
import { storeToRefs } from "pinia";

// 状态管理
const showCatalogMenu = ref(false)
const activeMenu = ref('')
const defaultOpeneds = ref(['tag-group'])
const catalogTreeData = ref<Catalog[]>([])
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const resourceTypes = ref<ResourceType[]>([])
const categoryDialog = ref<InstanceType<typeof CategoryDialog>>()
const catalogDialog = ref<InstanceType<typeof CatalogDialog>>()
const currentCatalog = ref<Catalog | null>(null);
const route = useRoute()
const router = useRouter()
const showCategoryActions = ref<number | null>(null)
const dynamicMenus = ref<RouteMenu[]>([])
const userStore = useUserStore()
const { username } = storeToRefs(userStore)

// 计算属性：获取特定菜单项（资源、系统管理、帮助）
const fixedMenuItems = computed(() => {
    // 固定菜单项
    return router.options.routes.filter(route =>
        ['/My', '/Help'].includes(route.path)
    )
})

const showProfile = () => {
    router.push('/profile')
}

// 初始化数据
onMounted(async () => {
    await loadCategories()
    await loadTags()
    await loadResourceTypes()
    await loadDynamicMenus() // 加载动态菜单
    updateActiveMenu()
})

const handleLogout = () => {
    logout()
}

// 加载数据
const loadCategories = async () => {
    try {
        categories.value = await listCategory()
    } catch (error) {
        console.error('加载分类失败:', error)
    }
}

const loadTags = async () => {
    try {
        tags.value = await listTag()
    } catch (error) {
        console.error('加载标签失败:', error)
    }
}

const loadResourceTypes = async () => {
    try {
        resourceTypes.value = await getResourceTypes()
    } catch (error) {
        console.error('加载资源类型失败:', error)
    }
}

const loadCatalogTree = async () => {
    try {
        catalogTreeData.value = await getCatalogTree()
    } catch (error) {
        console.error('加载目录树失败:', error)
    }
}

// 加载动态菜单
const loadDynamicMenus = async () => {
    try {
        const menus = await getMyMenu()
        dynamicMenus.value = menus.map((menu: Menu) => ({
            path: menu.path,
            name: menu.name,
            meta: {
                title: menu.title,
                icon: menu.icon,
                iconType: menu.iconType,
                style: menu.style,
                requiresAuth: true
            },
            children: menu.children?.map(child => ({
                path: child.path,
                name: child.name,
                meta: {
                    title: child.title,
                    icon: child.icon,
                    iconType: child.iconType,
                    style: child.style,
                    requiresAuth: true
                }
            })) || []
        } as RouteMenu))

        // 动态添加路由
        menus.forEach(menu => {
            if (menu.children && menu.children.length > 0) {
                router.addRoute({
                    path: menu.path,
                    name: menu.name,
                    meta: {
                        title: menu.title,
                        icon: menu.icon,
                        iconType: menu.iconType,
                        style: menu.style,
                        requiresAuth: true
                    },
                    children: []
                })

                menu.children.forEach(child => {
                    router.addRoute(menu.name, {
                        path: child.path,
                        name: child.name,
                        component: () => resolveComponent(child.name),
                        meta: {
                            title: child.title,
                            icon: child.icon,
                            iconType: child.iconType,
                            style: child.style,
                            requiresAuth: true
                        }
                    })
                })
            } else {
                router.addRoute({
                    path: menu.path,
                    name: menu.name,
                    component: () => resolveComponent(menu.name),
                    meta: {
                        title: menu.title,
                        icon: menu.icon,
                        iconType: menu.iconType,
                        style: menu.style,
                        requiresAuth: true
                    }
                })
            }
        })
    } catch (error) {
        console.error('加载动态菜单失败:', error)
    }
}

// 解析组件路径
const resolveComponent = (name: string) => {
    try {
        // 根据菜单名称映射到不同位置的组件
        if (name.toLowerCase().includes('user')) {
            return import('@/components/sys/UserView.vue')
        } else if (name.toLowerCase().includes('role')) {
            return import('@/components/sys/RoleView.vue')
        }
        // 默认尝试从sys目录加载
        return import('@/components/sys/' + name + 'View.vue')
            .catch(() => import('@/components/' + name.toLowerCase() + '/' + name +'.vue'))
    } catch (error) {
        console.error('组件加载失败:', error)
        return import('@/components/NotFound.vue') // 提供一个默认的NotFound组件
    }
}

// 处理菜单点击
const menuItemClick = (item: { path: string }) => {
    if (router.currentRoute.value.path !== item.path) {
        router.push(item.path).catch(err => {
            console.error('路由跳转失败:', err)
        })
    }
}

// 切换目录菜单显示
const toggleCatalogMenu = async (e: Event) => {
    e.stopPropagation()
    showCatalogMenu.value = !showCatalogMenu.value
    if (showCatalogMenu.value) {
        await loadCatalogTree()
    }
}

// 处理目录节点点击
const handleNodeClick = (data: Catalog) => {
    currentCatalog.value = data; // 保存当前选中的目录
    router.push(`/Catalog/${data.sno}`).catch(err => {
        console.error('路由跳转失败:', err);
    });
};

// 添加分类
const handleAddCategory = (e: Event) => {
    e.stopPropagation()
    categoryDialog.value?.open(null as unknown as Category)
}

// 添加目录
const handleAddCatalog = (e: Event) => {
    e.stopPropagation();
    catalogDialog.value?.open(currentCatalog.value?.id); // 传递当前目录ID
};

// 处理分类确认
const handleCategoryConfirm = async () => {
    await loadCategories()
    if (route.path.startsWith('/Category/')) {
        let currentSno = Number(route.params.sno)
        // 判断当前分类是否存在
        let found = categories.value.some(category => category.sno === currentSno)
        if (!found) {
            currentSno = categories.value[0]?.sno || 0 // 确保有默认值
        }
        await router.replace(`/Category/${currentSno}`)
    }
}

// 处理目录确认
const handleCatalogConfirm = async () => {
    await loadCatalogTree()
    // 如果当前路由是/Catalog页面，则重新加载
    if (route.path.startsWith('/Catalog/')) {
        const currentSno = route.params.sno
        await router.replace(`/Catalog/${currentSno}`)
    }
}

// 更新当前激活菜单
const updateActiveMenu = () => {
    activeMenu.value = router.currentRoute.value.path
}

const handleEditCatalog = (catalog: Catalog) => {
    catalogDialog.value?.open(catalog.parentId, catalog);
};

const handleDeleteCatalog = (catalog: Catalog) => {
    catalogDialog.value?.openForDelete(catalog.parentId, catalog);
};

const handleEditCategory = (category: Category) => {
    categoryDialog.value?.open(category)
}

const handleDeleteCategory = (category: Category) => {
    categoryDialog.value?.openForDelete(category)
}
</script>

<style scoped>
/* 固定菜单区域 */
.fixed-menu {
    flex: 1;
    border-top: 1px solid #ebeef5;
}

/* 菜单通用样式 */
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
    flex-shrink: 0;
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

.new-level{
    cursor: pointer;
    margin-left: auto;
    font-size: 15px!important;
}

.new-level:hover{
    color: #409EFF;
}

.action-button{
    float: right;
}

.menu-item-content {
    display: flex;
    align-items: center;
    width: 100%;
    position: relative;
}

.menu-icon {
    width: 32px;
    text-align: center;
    color: rgb(59,130,246);
    flex-shrink: 0;
}

.menu-label {
    flex: 1;
    margin-left: 5px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.action-buttons {
    display: flex;
    gap: 5px;
    margin-left: auto;
    padding-right: 8px;
}

.action-icon {
    cursor: pointer;
    font-size: 15px !important;
    color: #909399;
    transition: color 0.2s;
}

.action-icon:hover {
    color: #409EFF;
}

.content-area{
    background-color: rgb(243, 244, 246);
}

.menu-area{
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    padding: 0 0 20px 0;
}

.user-dropdown-container {
    display: flex;
    justify-content: flex-end;
    width: 67%;
    height: 100%;
    align-items: center;
}

.user-avatar-wrapper {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 0 10px;
    height: 100%;
}

.user-avatar {
    border: none;
    outline: none;
}

.user-avatar:focus, .user-avatar:hover {
    outline: none;
    box-shadow: none;
}

.username {
    margin-left: 8px;
    font-size: 14px;
    color: #d2d2d2;
}

.dropdown-icon {
    margin-left: 4px;
    transition: transform 0.3s;
}

.user-dropdown-menu {
    margin-top: 5px;
    min-width: 120px;
}

.user-dropdown-menu .el-dropdown-menu__item {
    padding: 8px 16px;
    display: flex;
    align-items: center;
}

.user-dropdown-menu .el-icon {
    margin-right: 8px;
    font-size: 16px;
}

.el-dropdown-menu {
    min-width: 120px;
}
.el-dropdown-menu__item {
    display: flex;
    align-items: center;
}
.el-dropdown-menu__item .el-icon {
    margin-right: 8px;
}
</style>