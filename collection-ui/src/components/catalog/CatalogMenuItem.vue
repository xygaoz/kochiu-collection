<template>
    <el-sub-menu
        :index="getIndex(item)"
        :key="item.id"
        v-if="item.level < 4"
        :class="{ 'is-active': isActive(item) }"
    >
        <template #title>
            <div
                class="menu-item-content"
                @mouseenter="showActions = item.level > 0"
                @mouseleave="showActions = false"
                @click.stop="handleTitleClick(item)"
            >
                <div class="menu-icon">
                    <el-icon><Folder /></el-icon>
                </div>
                <div class="menu-label">{{ item.label }}</div>
                <div class="action-button" v-if="item.level === 0">
                    <el-icon
                        @click.stop="$emit('toggle', $event)"
                        title="切换到分类"
                        class="new-level" style="margin: 0"
                    >
                        <Switch />
                    </el-icon>
                    <el-icon
                        @click.stop="$emit('new-catalog', $event)"
                        class="new-level"
                        title="新文件夹"
                    >
                        <Plus />
                    </el-icon>
                </div>
                <div class="action-buttons" v-if="showActions && item.level > 0">
                    <el-icon
                        @click.stop="$emit('edit-catalog', item)"
                        class="action-icon"
                        title="修改目录"
                    >
                        <Edit />
                    </el-icon>
                    <el-icon
                        @click.stop="$emit('delete-catalog', item)"
                        class="action-icon"
                        title="删除目录"
                    >
                        <Delete />
                    </el-icon>
                </div>
            </div>
        </template>
        <catalog-menu-item
            v-for="child in item.children"
            :key="child.id"
            :item="child"
            @node-click="$emit('node-click', $event)"
            @toggle="$emit('toggle', $event)"
            @new-catalog="$emit('new-catalog', $event)"
            @edit-catalog="$emit('edit-catalog', $event)"
            @delete-catalog="$emit('delete-catalog', $event)"
        />
    </el-sub-menu>
    <el-menu-item
        v-else
        :index="getIndex(item)"
        :key="`${item.id}_${item.level}`"
        @click="handleItemClick(item)"
    >
        <div class="menu-item-content" @mouseenter="showActions = item.level > 0" @mouseleave="showActions = false">
            <el-icon><Folder /></el-icon>
            <span class="menu-label">{{ item.label }}</span>
            <div class="action-buttons" v-if="showActions && item.level > 0">
                <el-icon
                    @click.stop="$emit('edit-catalog', item)"
                    class="action-icon"
                    title="修改目录"
                >
                    <Edit />
                </el-icon>
                <el-icon
                    @click.stop="$emit('delete-catalog', item)"
                    class="action-icon"
                    title="删除目录"
                >
                    <Delete />
                </el-icon>
            </div>
        </div>
    </el-menu-item>
</template>

<script setup lang="ts">
import { defineProps, defineEmits, ref } from "vue"
import { Folder, Plus, Switch, Edit, Delete } from "@element-plus/icons-vue";
import { Catalog } from "@/apis/interface";
import { useRoute } from "vue-router"

defineProps({
    item: {
        type: Object as () => Catalog,
        required: true
    },
});

const route = useRoute()
const isActive = (item: Catalog) => {
    return route.path === `/Catalog/${item.id}`
}

const emit = defineEmits(['node-click', 'toggle', 'new-catalog', 'edit-catalog', 'delete-catalog']);
const showActions = ref(false);
const getIndex = (item: Catalog) => `/Catalog/${item.id}`;

const handleTitleClick = (item: Catalog) => {
    emit('node-click', item);
};

const handleItemClick = (item: Catalog) => {
    emit('node-click', item);
};
</script>

<style scoped>
.menu-item-content {
    display: flex;
    align-items: center;
    width: 100%;
    position: relative;
    margin: 0 5px;
}

.menu-icon {
    width: 32px;
    text-align: center;
    color: rgb(59,130,246)
}

.menu-label {
    flex: 1;
    cursor: pointer;
}

.action-button {
    display: flex;
    gap: 5px;
}

.action-buttons {
    display: flex;
    gap: 0;
    margin-left: auto;
}

.new-level, .action-icon {
    cursor: pointer;
    font-size: 15px !important;
}

.new-level:hover, .action-icon:hover {
    color: #409EFF;
}

.el-sub-menu.is-active > :deep(.el-sub-menu__title) {
    color: var(--el-color-primary) !important;
    background-color: var(--el-menu-hover-bg-color) !important;
}
.el-sub-menu.is-active > :deep(.el-sub-menu__title) .menu-icon {
    color: var(--el-color-primary) !important;
    background-color: var(--el-menu-hover-bg-color) !important;
}

.el-sub-menu.is-active > :deep(.el-sub-menu__title) .action-button {
    color: var(--el-menu-button) !important;
}

.el-sub-menu.is-active > :deep(.el-sub-menu__title) .action-buttons {
    color: var(--el-menu-button) !important;
}

</style>