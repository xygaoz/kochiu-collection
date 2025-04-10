<template>
    <div style="height: calc(100vh); overflow: hidden; background-color: rgb(243 244 246);">
        <el-container style="height: 100%; overflow: hidden">
            <el-aside style="width: 250px;">
                <el-menu
                    class="el-menu-vertical-demo"
                    :default-active="activeMenu"
                    background-color="#fff"
                    text-color="#525252"
                    active-text-color="rgb(59,130,246)"
                    style="height: 100%"
                    :default-openeds="defaultOpeneds"
                >
                    <div class="el-menu-box">
                        <div
                            class="logo-image"
                        ></div>
                        <div style="padding-left: 5px; padding-top: 3px">
                            KoChiu Collection
                        </div>
                    </div>
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
                                <el-icon v-if="menuItem.path === '/Category'" @click="addCategory" class="add_category" title="新分类">
                                    <Plus />
                                </el-icon>
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
                            <div style="width: 22px; height: 22px;">
                                <img class="user-image" src="../assets/imgs/user.gif" alt="" />
                            </div>
                            <div style="padding-left: 5px; padding-top: 3px">gaozhao</div>
                        </div>
                    </div>
                </el-header>

                <el-main class="el-main">
                    <RouterView v-slot="{ Component }" :menuItemClick="menuItemClick">
                        <component :is="Component"></component>
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
import { ref, onMounted, nextTick } from "vue";
import router, { routes } from "@/apis/base-routes";
import { RouteRecordRaw } from "vue-router";
import { listCategory, listTag } from "@/apis/services";
import { Plus } from "@element-plus/icons-vue"; // 导入listCategory方法
import { Category, Tag } from "@/apis/interface";
import CategoryDialog from "@/components/category/CategoryDialog.vue"; // 导入Category接口

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
    // 保存当前路由路径
    const currentPath = router.currentRoute.value.path

    // 重新初始化菜单
    initMenu();

    // 找到/category路由
    const categoryRoute = menu.value.find(route => route.path === '/Category');
    if (!categoryRoute || !categoryRoute.children) return;

    // 获取现有的"所有分类"菜单项
    const allCategoryItem = categoryRoute.children.find(child => child.name === 'allCategory');

    // 创建动态分类菜单项数组
    const dynamicItems = categories.map(category => ({
        path: `/Category/${category.sno}`,
        name: `category-${category.sno}`,
        meta: {
            title: category.cateName,
            cateId: category.sno,
            icon: 'icon-col-fenlei3',
            iconType: 'iconfont',
            style: 'font-size: 18px; color: rgb(59,130,246)'
        }
    }));

    // 更新children数组
    categoryRoute.children = [
        ...dynamicItems,
        ...(allCategoryItem ? [allCategoryItem] : [])
    ];

    // 如果当前路由是分类页面，保持当前路由不变
    if (currentPath.startsWith('/Category')) {
        const currentSno = currentPath.split('/').pop()
        const exists = categories.some(c => c.sno.toString() === currentSno)

        // 如果当前分类仍然存在，保持选中状态
        if (exists) {
            nextTick(() => {
                /// 设置激活菜单项
                activeMenu.value = currentPath;
                // 如果需要，也可以手动滚动到可见区域
                setTimeout(() => {
                    const activeItem = document.querySelector(`.el-menu-item[index="${currentPath}"]`);
                    activeItem?.scrollIntoView({ block: 'nearest' });
                }, 100);
            })
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
        if (tags && tags.length > 0) {
            // 找到/tag路由
            const tagRoute = menu.value.find(route => route.path === '/Tag');
            if (tagRoute && tagRoute.children) {
                // 获取现有的"所有分类"菜单项
                const allTagItem = tagRoute.children.find(child => child.name === 'allTag');

                // 创建动态分类菜单项数组
                const dynamicItems = tags.map(tag => ({
                    path: `/Tag/${tag.tagId}`,
                    meta: {
                        title: tag.tagName,
                        tagId: tag.tagId,
                        icon: 'icon-col-biaoqian',  // 添加图标
                        iconType: 'iconfont',
                        style: 'font-size: 18px; color: rgb(59,130,246)'
                    }
                }));

                // 重新设置children数组，动态项在前，"所有分类"在后
                tagRoute.children = [
                    ...dynamicItems,
                    ...(allTagItem ? [allTagItem] : [])
                ];
            }
        }
    } catch (error) {
        console.error('加载标签失败:', error);
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
    /* display: flex; */
}

.logo-image {
    background-image: url("../assets/imgs/logo.jpg");
    width: 28px;
    height: 28px;
    background-size: 28px 28px;
    border-radius: 15px;
}

.user-image {
    width: 22px;
    height: 22px;
}

.demo-tabs /deep/ .el-tabs__header {
    color: #333; /* 标签页头部字体颜色 */
    margin: 0 0 5px !important;
}

.demo-tabs /deep/ .el-tabs__nav-wrap {
    padding-left: 10px;
}

.demo-tabs /deep/ .el-tabs__content {
    padding: 5px;
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
}

.add_category{
    cursor: pointer;
    margin-left: auto;
}

.add_category:hover{
    color: #409EFF;
}
</style>