<template>
    <div class="batch-edit-view">
        <div class="header">
            <div class="title">
                批量编辑 ({{ selectedCount }}个文件)
            </div>
            <div class="actions">
                <el-tooltip
                    v-for="action in actions"
                    :key="action.name"
                    :content="action.tooltip"
                    placement="bottom"
                    :show-after="300"
                >
                    <div class="action-item" @click="action.handler">
                        <el-icon :size="action.size || 'medium'">
                            <component :is="action.icon" />
                        </el-icon>
                    </div>
                </el-tooltip>
            </div>
        </div>

        <el-form
            label-width="80px"
            class="edit-form"
            label-position="top"
        >
        <el-form-item label="标题">
            <el-input
                v-model="batchForm.title"
                placeholder="设置统一标题"
                clearable
                class="full-width-input"
            />
        </el-form-item>

        <el-form-item label="描述">
            <el-input
                v-model="batchForm.description"
                type="textarea"
                :rows="3"
                placeholder="设置统一描述"
                clearable
                class="full-width-input"
            />
        </el-form-item>

        <el-form-item>
            <el-button
                size="small"
                type="primary"
                @click="handleBatchUpdate"
                :loading="saving"
                class="submit-button"
            >
            应用修改
            </el-button>
        </el-form-item>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, defineProps, defineEmits } from 'vue';
import { ElMessage } from 'element-plus';
import type { Resource, Tag } from '@/apis/interface';
import { CircleCheck, CloseBold, Connection, Delete } from "@element-plus/icons-vue";
import { bacthUpdateResource } from "@/apis/resource-api";

interface Props {
    selectedFiles: Resource[];
}

const props = defineProps<Props>();
const emit = defineEmits(['update-success', 'clear-selection', 'select-all', 'delete', 'move']);

const batchForm = ref({
    title: '',
    description: '',
    tags: [] as Tag[]
});

const tagInputVisible = ref(false);
const tagInputValue = ref('');
const tagInputRef = ref();
const saving = ref(false);

const selectedCount = computed(() => props.selectedFiles.length);

const actions = [
    {
        name: 'clear',
        icon: CloseBold,
        tooltip: '取消选择',
        handler: () => emit('clear-selection'),
    },
    {
        name: 'select-all',
        icon: CircleCheck,
        tooltip: '全选',
        handler: () => emit('select-all'),
    },
    {
        name: 'delete',
        icon: Delete,
        tooltip: '批量删除',
        handler: () => emit('delete'),
        size: 'default'
    },
    {
        name: 'move',
        icon: Connection,
        tooltip: '移动到',
        handler: () => emit('move'),
    }
]

// 显示标签输入框
const showTagInput = () => {
    tagInputVisible.value = true;
    nextTick(() => {
        tagInputRef.value?.focus();
    });
};

// 添加标签
const addTag = async () => {
    if (tagInputValue.value) {
        console.log('addTag', tagInputValue.value)
    } else {
        tagInputVisible.value = false;
    }
};

// 移除标签
const removeTag = (tag: Tag) => {
    batchForm.value.tags = batchForm.value.tags.filter(t => t.tagId !== tag.tagId);
};

// 清空选择
const clearSelection = () => {
    emit('clear-selection');
};

// 批量更新
const handleBatchUpdate = async () => {
    if (props.selectedFiles.length === 0) {
        ElMessage.warning('请先选择文件');
        return;
    }
    if(!batchForm.value.title && !batchForm.value.description){
        ElMessage.warning('请先设置标题或描述');
        return;
    }

    try {
        saving.value = true;

        const resourceIds = props.selectedFiles.map(file => file.resourceId);
        const updateData = {
            title: batchForm.value.title || undefined,
            description: batchForm.value.description || undefined,
        };

        if(await bacthUpdateResource(resourceIds, updateData)) {
            //更新资源数据
            props.selectedFiles.forEach(file => {
                if(batchForm.value.title) {
                    file.title = batchForm.value.title;
                }
                if(batchForm.value.description) {
                    file.description = batchForm.value.description;
                }
            });

            emit('update-success', props.selectedFiles);
            ElMessage.success('批量更新成功');
        }
    } catch (error) {
        ElMessage.error('批量更新失败');
    } finally {
        saving.value = false;
    }
};
</script>

<style scoped>
.batch-edit-view {
    padding: 16px;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.edit-form {
    flex: 1;  /* 表单区域填满剩余空间 */
    display: flex;
    flex-direction: column;
}

.full-width-input {
    width: 100%;
}

.tag-section {
    width: 100%;
}

.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
}

.tag-input {
    width: 100px;
}

.button-new-tag {
    height: 24px;
    line-height: 22px;
    padding-top: 0;
    padding-bottom: 0;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-shrink: 0;  /* 防止头部被压缩 */
}

.title {
    flex: 0 0 60%;
    font-size: 16px;
    font-weight: 500;
}

.actions {
    flex: 0 0 40%;
    display: flex;
    justify-content: flex-end;
}

.action-item {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 26px;
    height: 26px;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s;
}

.action-item:hover {
    color: #000000;
    background-color: #b4d5ff;
    transform: scale(1.1);
}

/* 确保所有图标大小一致 */
.el-icon {
    font-size: 18px !important;
    color: var(--el-text-color-regular);
}

</style>