<template>
    <template v-for="menu in menus" :key="menu.path">
        <!-- 有子菜单的情况 -->
        <el-sub-menu
            v-if="hasChildren(menu)"
            :index="getMenuIndex(menu)"
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

            <el-menu-item
                v-for="child in menu.children"
                :key="child.path"
                :index="child.path"
                @click="emit('menu-click', { path: child.path })"
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

        <!-- 没有子菜单的情况 -->
        <el-menu-item
            v-else
            :index="menu.path"
            @click="emit('menu-click', { path: menu.path })"
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
</template>

<script setup lang="ts">
import { defineEmits, defineProps, StyleValue } from "vue";
import type { RouteRecordRaw } from 'vue-router'

defineProps<{
    menus: RouteRecordRaw[]
}>()

const emit = defineEmits(['menu-click'])

const hasChildren = (menu: RouteRecordRaw) =>
    Array.isArray(menu.children) && menu.children.length > 0

const getMenuIndex = (menu: RouteRecordRaw) =>
    menu.meta?.group || menu.path
</script>