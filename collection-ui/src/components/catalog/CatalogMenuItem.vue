<template>
    <el-sub-menu
        :index="`catalog-${item.id}`"
        v-if="item.children && item.children.length > 0 && item.level < 4"
        @click="handleSubMenuClick"
    >
        <template #title>
            <div class="menu-item-content" @click.stop="handleTitleClick(item)">
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
            </div>
        </template>
        <catalog-menu-item
            v-for="child in item.children"
            :key="child.id"
            :item="child"
            @node-click="$emit('node-click', $event)"
            @toggle="$emit('toggle', $event)"
            @new-catalog="$emit('new-catalog', $event)"
        />
    </el-sub-menu>
    <el-menu-item
        v-else
        :index="`catalog-${item.id}`"
        @click="handleItemClick(item)"
    >
        <el-icon><Folder /></el-icon>
        <span>{{ item.label }}</span>
    </el-menu-item>
</template>

<script setup lang="ts">
import { Folder, Plus, Switch } from "@element-plus/icons-vue";
import { Catalog } from "@/apis/interface";

defineProps({
    item: {
        type: Object as () => Catalog,
        required: true
    },
});

const emit = defineEmits(['node-click', 'toggle', 'new-catalog']);

const handleTitleClick = (item: Catalog) => {
    emit('node-click', item);
};

const handleItemClick = (item: Catalog) => {
    emit('node-click', item);
};

const handleSubMenuClick = (e: Event) => {
    // 阻止点击子菜单时的默认展开行为
    e.preventDefault();
};
</script>

<style scoped>
.menu-item-content {
    display: flex;
    align-items: center;
    width: 100%;
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

.new-level {
    cursor: pointer;
    font-size: 15px !important;
}

.new-level:hover {
    color: #409EFF;
}

.expand-icon {
    cursor: pointer;
    font-size: 14px;
    transition: transform 0.3s;
}

.expand-icon.is-expanded {
    transform: rotate(180deg);
}
</style>