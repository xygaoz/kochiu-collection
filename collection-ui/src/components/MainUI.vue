<template>
    <div style="height: calc(100vh); overflow: hidden; background-color: rgb(243 244 246);">
        <el-container style="height: 100%; overflow: hidden">
            <el-aside style="width: 250px;">
                <el-menu
                    class="el-menu-vertical-demo"
                    background-color="#fff"
                    text-color="#525252"
                    active-text-color="rgb(215, 24, 24)"
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
                    <div v-for="menuItem in menu" :key="menuItem.path">
                        <el-sub-menu
                            v-if="menuItem.children && menuItem.children.length"
                            :index="menuItem.path"
                            :key="menuItem.name"
                        >
                            <template #title>
                                <div class="menu-icon">
                                    <el-icon v-if="menuItem.meta?.iconType === 'icons-vue' ">
                                        <component :is="menuItem.meta.icon" />
                                    </el-icon>
                                    <i v-else class="iconfont" :class="menuItem.meta?.icon"
                                       :style="String(menuItem.meta?.style)"></i>
                                </div>
                                <div class="menu-label">
                                    {{ menuItem.name }}
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
                                        <el-icon v-if="subMenuItem.meta?.iconType === 'icons-vue' ">
                                            <component :is="subMenuItem.meta.icon" />
                                        </el-icon>
                                        <i v-else class="iconfont" :class="subMenuItem.meta?.icon"
                                           :style="String(subMenuItem.meta?.style)"></i>
                                    </div>
                                    <div class="menu-label">
                                        {{ subMenuItem.name }}
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
                                    <el-icon v-if="menuItem.meta?.iconType === 'icons-vue' ">
                                        <component :is="menuItem.meta.icon" />
                                    </el-icon>
                                    <i v-else class="iconfont" :class="menuItem.meta?.icon"
                                       :style="String(menuItem.meta?.style)"></i>
                                </div>
                                <div class="menu-label">
                                    {{ menuItem.name }}
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
                        <KeepAlive :max="10">
                            <component :is="Component"></component>
                        </KeepAlive>
                    </RouterView>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, defineExpose } from "vue";
import router, { routes } from "@/apis/base-routes";
import { RouteRecordRaw } from "vue-router";
import { Collection } from "@element-plus/icons-vue";

export default defineComponent({
    setup() {
        const defaultActive = ref("/help");
        const menu = routes;
        const defaultOpeneds = ref(['/my']); // 添加默认展开的子菜单路径

        //初始加载dom
        onMounted(() => {
            router.push(routes[0]);
        });

        // 菜单项点击事件
        function menuItemClick(subMenuItem: RouteRecordRaw) {
            console.log('Clicked menu item:', subMenuItem);
            defaultActive.value = subMenuItem.path;
            router.push({ name: subMenuItem.name }).then(() => {
                console.log('Navigated to:', router.currentRoute.value);
            }).catch(err => {
                console.error('Navigation failed:', err);
            });
        }

        defineExpose({
            menuItemClick
        })

        return {
            menu,
            defaultActive,
            menuItemClick,
            defaultOpeneds
        };
    },
    components: { Collection }
});
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

.menu-icon{
    width: 32px;
    float: left;
    text-align: center;
}

.menu-label{
    float: left;
}
</style>
