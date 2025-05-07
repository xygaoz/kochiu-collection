<template>
    <div :class="[route.meta.theme || themeStore.currentTheme]" style="height: calc(100vh); overflow: hidden;">
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
                            :text-color="'var(--el-menu-text-color)'"
                            :active-text-color="'var(--el-color-primary)'"
                            :default-openeds="defaultOpeneds"
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
                            :text-color="'var(--el-menu-text-color)'"
                            :active-text-color="'var(--el-color-primary)'"
                            :default-openeds="defaultOpeneds"
                        >
                            <el-sub-menu index="category-group">
                                <template #title>
                                    <div class="menu-icon">
                                        <i class="iconfont icon-col-fenlei"
                                           style="font-size: 21px; color: rgb(59,130,246)"></i>
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
                                    :key="category.cateId"
                                    :index="`/Category/${category.cateId}`"
                                    @click="menuItemClick({path: `/Category/${category.cateId}`})"
                                >
                                    <template #title>
                                        <div
                                            class="menu-item-content"
                                            @mouseenter="showCategoryActions = category.cateId"
                                            @mouseleave="showCategoryActions = null"
                                        >
                                            <div class="menu-icon">
                                                <i class="iconfont icon-col-fenlei3"
                                                   style="font-size: 18px; color: rgb(59,130,246)"></i>
                                            </div>
                                            <div class="menu-label">
                                                {{ category.cateName }}
                                            </div>
                                            <div class="action-buttons" v-if="showCategoryActions === category.cateId">
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
                                            <i class="iconfont icon-col-fenlei2"
                                               style="font-size: 21px; color: rgb(59,130,246)"></i>
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
                        :text-color="'var(--el-menu-text-color)'"
                        :active-text-color="'var(--el-color-primary)'"
                        :default-openeds="defaultOpeneds"
                    >
                        <!-- 标签菜单 -->
                        <el-sub-menu index="tag-group">
                            <template #title>
                                <div class="menu-icon">
                                    <i class="iconfont icon-col-biaoqian1"
                                       style="font-size: 20px; color: rgb(59,130,246); margin: 0 1px 0 0;"></i>
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
                                        <i class="iconfont icon-col-biaoqian"
                                           style="font-size: 18px; color: rgb(59,130,246)"></i>
                                    </div>
                                    <div class="menu-label">{{ tag.tagName }}</div>
                                </template>
                            </el-menu-item>
                            <el-menu-item index="/AllTag" @click="menuItemClick({path: '/AllTag'})">
                                <template #title>
                                    <div class="menu-icon">
                                        <i class="iconfont icon-col-24gl-tags4"
                                           style="font-size: 17px; color: rgb(59,130,246)"></i>
                                    </div>
                                    <div class="menu-label">所有标签</div>
                                </template>
                            </el-menu-item>
                        </el-sub-menu>

                        <!-- 文件类型菜单 -->
                        <el-sub-menu index="type-group">
                            <template #title>
                                <div class="menu-icon">
                                    <i class="iconfont icon-col-duomeitiicon-"
                                       style="font-size: 20px; color: rgb(59,130,246); margin: 0 1px 0 0;"></i>
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
                                        <i :class="`iconfont icon-col-${type.value}`"
                                           style="font-size: 21px; color: rgb(59,130,246)"></i>
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
                                :index="menu.meta?.group"
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
                                :index="menu.meta.group"
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

                                <!-- 子菜单项 -->
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
                        <div style="width: 33%;">
                            <i class="iconfont icon-col-zhutise theme" @click="toggleTheme"
                               :style="isDark ? 'color: #fff': 'color: #000'"
                               :title="isDark ? '切换到明亮主题' : '切换到暗黑主题'"
                            />
                        </div> <!-- 左侧占位 -->
                        <div class="user-dropdown-container">
                            <el-dropdown>
                                <div class="user-avatar-wrapper">
                                    <el-avatar
                                        :size="32"
                                        :src="userAvatar"
                                        class="user-avatar"
                                    />
                                    <span class="username">{{ username }}</span>
                                    <el-icon class="dropdown-icon">
                                        <ArrowDown />
                                    </el-icon>
                                </div>
                                <template #dropdown>
                                    <el-dropdown-menu class="user-dropdown-menu">
                                        <el-dropdown-item @click="showProfile">
                                            <el-icon>
                                                <User />
                                            </el-icon>
                                            <span>个人资料</span>
                                        </el-dropdown-item>
                                        <el-dropdown-item divided @click="handleLogout">
                                            <el-icon>
                                                <SwitchButton />
                                            </el-icon>
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
import { computed, onMounted, ref, StyleValue, watch } from "vue";
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
import { useThemeStore } from "@/apis/themeStore";
import userAvatar from "../assets/imgs/user.gif";

// 状态管理
const showCatalogMenu = ref(false);
const activeMenu = ref("");
const defaultOpeneds = ref(["tag-group"]);
const catalogTreeData = ref<Catalog[]>([]);
const categories = ref<Category[]>([]);
const tags = ref<Tag[]>([]);
const resourceTypes = ref<ResourceType[]>([]);
const categoryDialog = ref<InstanceType<typeof CategoryDialog>>();
const catalogDialog = ref<InstanceType<typeof CatalogDialog>>();
const currentCatalog = ref<Catalog | null>(null);
const route = useRoute();
const router = useRouter();
const showCategoryActions = ref<number | null>(null);
const dynamicMenus = ref<RouteMenu[]>([]);
const userStore = useUserStore();
const { username } = storeToRefs(userStore);
const themeStore = useThemeStore();
const { currentTheme, isDark } = storeToRefs(themeStore);

// 计算属性：获取特定菜单项（资源、系统管理、帮助）
const fixedMenuItems = computed(() => {
    // 固定菜单项
    return router.options.routes.filter(route =>
        ["/My", "/Help"].includes(route.path)
    );
});

const showProfile = () => {
    router.push("/profile");
};

const toggleTheme = async () => {
    const newTheme = currentTheme.value === "light" ? "dark" : "light";
    await themeStore.applyTheme(newTheme);
};

// 初始化数据
onMounted(async () => {
    await loadCategories();
    await loadTags();
    await loadResourceTypes();
    await loadDynamicMenus(); // 加载动态菜单
    updateActiveMenu();
});

const handleLogout = () => {
    logout();
};

// 加载数据
const loadCategories = async () => {
    try {
        categories.value = await listCategory();
    } catch (error) {
        console.error("加载分类失败:", error);
    }
};

const loadTags = async () => {
    try {
        tags.value = await listTag();
    } catch (error) {
        console.error("加载标签失败:", error);
    }
};

const loadResourceTypes = async () => {
    try {
        resourceTypes.value = await getResourceTypes();
    } catch (error) {
        console.error("加载资源类型失败:", error);
    }
};

const loadCatalogTree = async () => {
    try {
        catalogTreeData.value = await getCatalogTree();
    } catch (error) {
        console.error("加载目录树失败:", error);
    }
};

// 加载动态菜单
const loadDynamicMenus = async () => {
    try {
        const menus = await getMyMenu();
        dynamicMenus.value = menus.map((menu: Menu) => ({
            path: menu.path,
            name: menu.name,
            meta: {
                title: menu.title,
                icon: menu.icon,
                iconType: menu.iconType,
                style: menu.style,
                requiresAuth: true,
                group: `${menu.name}-group`
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
        } as RouteMenu));

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
                });

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
                    });
                });
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
                });
            }
        });

        router.afterEach((to) => {
            // 检查是否是动态菜单路径
            const isDynamicMenu = dynamicMenus.value.some(menu =>
                menu.path === to.path ||
                (menu.children && menu.children.some(child => child.path === to.path))
            );

            if (isDynamicMenu) {
                activeMenu.value = to.path;
                // 可以添加特定菜单组的处理逻辑
            }
        });
    } catch (error) {
        console.error("加载动态菜单失败:", error);
    }
};

// 解析组件路径
const resolveComponent = (name: string) => {
    try {
        // 根据菜单名称映射到不同位置的组件
        if (name.toLowerCase().includes("user")) {
            return import("@/components/sys/UserView.vue");
        } else if (name.toLowerCase().includes("role")) {
            return import("@/components/sys/RoleView.vue");
        } else if (name.toLowerCase().includes("strategy")) {
            return import("@/components/sys/StrategyView.vue");
        }
        // 默认尝试从sys目录加载
        return import("@/components/sys/" + name + "View.vue")
            .catch(() => import("@/components/" + name.toLowerCase() + "/" + name + ".vue"));
    } catch (error) {
        console.error("组件加载失败:", error);
        return import("@/components/NotFound.vue"); // 提供一个默认的NotFound组件
    }
};

// 处理菜单点击
const menuItemClick = (item: { path: string }) => {
    if (router.currentRoute.value.path !== item.path) {
        router.push(item.path).catch(err => {
            console.error("路由跳转失败:", err);
        });
    }
};

// 切换目录菜单显示
const toggleCatalogMenu = async (e: Event) => {
    e.stopPropagation();
    showCatalogMenu.value = !showCatalogMenu.value;
    if (showCatalogMenu.value) {
        await loadCatalogTree();
    }
};

// 处理目录节点点击
const handleNodeClick = (data: Catalog) => {
    currentCatalog.value = data; // 保存当前选中的目录
    router.push(`/Catalog/${data.id}`).catch(err => {
        console.error("路由跳转失败:", err);
    });
};

// 添加分类
const handleAddCategory = (e: Event) => {
    e.stopPropagation();
    categoryDialog.value?.open(null as unknown as Category);
};

// 添加目录
const handleAddCatalog = (e: Event) => {
    e.stopPropagation();
    catalogDialog.value?.open(currentCatalog.value?.id); // 传递当前目录ID
};

// 处理分类确认
const handleCategoryConfirm = async () => {
    await loadCategories();
    if (route.path.startsWith("/Category/")) {
        let currentId = Number(route.params.cateId);
        // 判断当前分类是否存在
        let found = categories.value.some(category => category.cateId === currentId);
        if (!found) {
            currentId = categories.value[0]?.cateId || 0; // 确保有默认值
        }
        await router.replace(`/Category/${currentId}`);
    }
};

// 处理目录确认
const handleCatalogConfirm = async () => {
    await loadCatalogTree();
    // 如果当前路由是/Catalog页面，则重新加载
    if (route.path.startsWith("/Catalog/")) {
        const currentId = route.params.id;
        await router.replace(`/Catalog/${currentId}`);
    }
};

// 更新当前激活菜单
const updateActiveMenu = () => {
    activeMenu.value = router.currentRoute.value.path;
};

const handleEditCatalog = (catalog: Catalog) => {
    catalogDialog.value?.open(catalog.parentId, catalog);
};

const handleDeleteCatalog = (catalog: Catalog) => {
    catalogDialog.value?.openForDelete(catalog.parentId, catalog);
};

const handleEditCategory = (category: Category) => {
    categoryDialog.value?.open(category);
};

const handleDeleteCategory = (category: Category) => {
    categoryDialog.value?.openForDelete(category);
};

// 监听路由变化
watch(() => route.path, (newPath) => {
    activeMenu.value = newPath;

    // 根据当前路由更新默认打开的菜单组
    if (newPath.startsWith("/Catalog")) {
        defaultOpeneds.value = ["catalog-group"];
    } else if (newPath.startsWith("/Category")) {
        defaultOpeneds.value = ["category-group"];
    } else if (newPath.startsWith("/Tag")) {
        defaultOpeneds.value = ["tag-group"];
    } else if (newPath.startsWith("/Type")) {
        defaultOpeneds.value = ["type-group"];
    } else if (newPath.startsWith("/My")) {
        defaultOpeneds.value = ["my-group"];
    } else if (newPath.startsWith("/System")) {
        defaultOpeneds.value = ["system-group"];
    }
    // 动态菜单部分可以根据需要添加更多判断
}, { immediate: true });
</script>

<style scoped>
/* 固定菜单区域 */
.fixed-menu {
    flex: 1;
}

.el-menu-box {
    display: flex;
    padding-left: 25px;
    align-items: center;
    height: 58px;
    flex-shrink: 0;
    background-color: var(--el-bg-color-header);
    color: var(--el-text-color-master);
}

.el-main {
    padding: 0;
    margin: 0;
}

.headerCss {
    font-size: 12px;
    justify-content: right;
    align-items: center;
    height: 58px;
    background-color: var(--el-bg-color-header);
    color: var(--el-text-color-master);
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

/* Light 主题：强制显示右侧边框 */
.el-menu-box:not(.el-menu--dark),
.el-menu:not(.el-menu--dark) {
    border-right: 1px solid var(--el-border-color-light) !important;
}

/* Dark 主题：隐藏边框 */
html.dark .el-menu-box,
html.dark .el-menu {
    border-right: none !important;
}

/* 统一菜单项悬停背景色 */
.el-menu-item:hover,
.el-sub-menu__title:hover {
    background-color: var(--el-menu-hover-bg-color) !important;
}

.menu-icon {
    width: 32px;
    float: left;
    text-align: center;
}

.menu-label {
    float: left;
    width: 100%;
    margin: 0 0 0 5px;
}

.new-level {
    cursor: pointer;
    margin-left: auto;
    font-size: 15px !important;
}

.new-level:hover {
    color: #409EFF;
}

.action-button {
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
    color: rgb(59, 130, 246);
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

.content-area {
    background-color: var(--el-bg-color);
}

.menu-area {
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
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

.user-avatar:focus, .user-avatar:hover, .user-avatar-wrapper:hover,
.user-dropdown-container:hover {
    outline: none;
    box-shadow: none;
}

.username {
    margin-left: 8px;
    font-size: 14px;
    color: var(--el-text-color-master);
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

.theme {
    cursor: pointer;
}

.el-menu-item.is-active {
    color: var(--el-color-primary) !important;
    background-color: var(--el-menu-hover-bg-color) !important;
}

.el-menu-item.is-active .menu-icon {
    color: var(--el-color-primary) !important;
}
</style>