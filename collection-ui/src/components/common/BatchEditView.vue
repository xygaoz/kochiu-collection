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

        <div class="detail-row">
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
        <!-- 显示标签 -->
        <div class="detail-row">
            标签
        </div>
        <div class="detail-row">
            <div class="detail-value">
                <div class="tags">
                    <!-- 实线标签（可删除） -->
                    <el-tag
                        v-for="tag in tagState.commonTags"
                        :key="tag.tagId"
                        closable
                        @close="removeTag(tag)">
                        {{ tag.tagName }}
                    </el-tag>

                    <el-tag
                        v-for="tag in tagState.partialTags"
                        :key="tag.tagName"
                        class="dashed-tag"
                        @click="addTagToMissingFiles(tag)">
                        {{ tag.tagName }}
                    </el-tag>
                    <el-input
                        v-if="tagInputVisible"
                        ref="tagInputRef"
                        v-model="tagInputValue"
                        class="tag-input"
                        size="small"
                        @keyup.enter="handleTagInputConfirm"
                        @blur="handleTagInputConfirm"
                        @click.stop
                    />
                    <el-button
                        v-else
                        class="button-new-tag"
                        size="small"
                        @click="showTagInput"
                        @click.stop
                    >
                        + 新标签
                    </el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, defineProps, defineEmits, watch, reactive, onMounted } from "vue";
import { ElMessage } from 'element-plus';
import type { Resource, Tag } from '@/apis/interface';
import { CircleCheck, CloseBold, Connection, Delete } from "@element-plus/icons-vue";
import { bacthAddTag, bacthRemoveTag, bacthUpdateResource } from "@/apis/resource-api";

interface Props {
    selectedFiles: Resource[];
}

const props = defineProps<Props>();
const emit = defineEmits(['update-success', 'clear-selection', 'select-all', 'delete', 'move']);

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

// 1. 首先声明所有响应式变量（确保初始化顺序）
const localSelectedFiles = ref<Resource[]>([]);
const tagState = reactive({
    commonTags: [] as Tag[],
    partialTags: [] as Tag[]
});

// 2. 然后声明其他变量
const batchForm = ref({
    title: '',
    description: '',
    tags: [] as Tag[]
});

// 3. 添加初始化检查
onMounted(() => {
    if (!localSelectedFiles.value) {
        console.error('localSelectedFiles 未正确初始化');
        localSelectedFiles.value = [];
    }
    updateTagClassification();
});

const updateTagClassification = () => {
    try {
        if (!localSelectedFiles.value || !Array.isArray(localSelectedFiles.value)) {
            console.warn('localSelectedFiles 不是有效数组');
            tagState.commonTags = [];
            tagState.partialTags = [];
            return;
        }

        const all = new Map<number, Tag>();
        localSelectedFiles.value.forEach((file: Resource) => {
            file.tags?.forEach(tag => {
                if (!all.has(tag.tagId)) {
                    all.set(tag.tagId, tag);
                }
            });
        });

        const allTagsArray = Array.from(all.values());

        tagState.commonTags = allTagsArray.filter(tag =>
            localSelectedFiles.value.every(file =>
                file.tags?.some(t => t.tagId === tag.tagId)
            )
        );

        tagState.partialTags = allTagsArray.filter(tag =>
            !tagState.commonTags.some(t => t.tagId === tag.tagId)
        );
    } catch (e) {
        console.error('更新标签分类失败:', e);
        tagState.commonTags = [];
        tagState.partialTags = [];
    }
};
// 显示标签输入框
const showTagInput = () => {
    tagInputVisible.value = true;
    nextTick(() => {
        tagInputRef.value?.focus();
    });
};

// 批量更新
const handleBatchUpdate = async () => {
    if (props.selectedFiles.length === 0) {
        ElMessage.warning('请先选择文件');
        return;
    }

    if (!batchForm.value.title && !batchForm.value.description) {
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

        const success = await bacthUpdateResource(resourceIds, updateData);

        if (success) {
            // 当API只返回布尔值时，直接构建更新后的数组
            const updatedFiles: Resource[] = props.selectedFiles.map(file => ({
                ...file,
                title: batchForm.value.title || file.title,
                description: batchForm.value.description || file.description,
                updateTime: new Date().toISOString() // 添加更新时间
            }));

            emit('update-success', updatedFiles);
            ElMessage.success(`成功更新 ${updatedFiles.length} 个资源`);
        } else {
            ElMessage.warning('更新操作未执行');
        }
    } catch (error) {
        console.error('批量更新失败:', error);
        ElMessage.error(`批量更新失败: ${error instanceof Error ? error.message : String(error)}`);
    } finally {
        saving.value = false;
    }
};

// 添加标签到缺失文件
const addTagToMissingFiles = async (tag: Tag) => {
    try {
        // 防御性检查
        if (!localSelectedFiles.value || !Array.isArray(localSelectedFiles.value)) {
            console.error("文件数据未正确初始化");
            return
        }

        const filesMissingTag = localSelectedFiles.value.filter(file =>
            !file.tags?.some(t => t.tagId === tag.tagId)
        );

        if (filesMissingTag.length === 0) return;

        // 创建更新副本
        const updatedFiles = JSON.parse(JSON.stringify(localSelectedFiles.value));
        const resourceIds = filesMissingTag.map(f => f.resourceId);

        updatedFiles.forEach((file: Resource) => {
            if (resourceIds.includes(file.resourceId)) {
                file.tags = [...(file.tags || []), { ...tag }];
            }
        });

        // 立即更新UI
        localSelectedFiles.value = updatedFiles;
        updateTagClassification();

        // 异步操作
        await bacthAddTag(resourceIds, { tagName: tag.tagName });

        emit('update-success', updatedFiles);
        ElMessage.success(`标签已添加`);

    } catch (error) {
        console.error('添加标签失败:', error);
        ElMessage.error(`添加失败: ${error instanceof Error ? error.message : '未知错误'}`);
        // 回滚UI状态
        updateTagClassification();
    }
};

// 从所有文件移除标签
const removeTag = async (tag: Tag) => {
    try {
        // 1. 从所有选中文件中移除
        const resourceIds = props.selectedFiles.map((file: Resource) => file.resourceId);
        await bacthRemoveTag(resourceIds, { tagId: tag.tagId });

        // 2. 创建新数组更新本地状态（不直接修改props）
        const updatedFiles = props.selectedFiles.map((file: Resource) => ({
            ...file,
            tags: (file.tags || []).filter(t => t.tagId !== tag.tagId)
        }));

        emit('update-success', updatedFiles);
    } catch (error) {
        ElMessage.error('移除标签失败');
        console.error('移除标签失败:', error);
    }
};

const handleTagInputConfirm = async () => {
    if (!tagInputValue.value?.trim()) {
        tagInputVisible.value = false;
        return;
    }

    try {
        saving.value = true;
        const tagName = tagInputValue.value.trim();

        // 检查是否已存在该标签
        const alreadyExists = props.selectedFiles.some(file =>
            file.tags?.some(t => t.tagName.toLowerCase() === tagName.toLowerCase())
        );

        if (alreadyExists) {
            ElMessage.warning(`标签 "${tagName}" 已存在`);
            return;
        }

        // 调用API添加标签
        const resourceIds = props.selectedFiles.map(file => file.resourceId);
        const newTag = await bacthAddTag(resourceIds, { tagName });

        // 创建新数组更新状态（不直接修改props）
        const updatedFiles = props.selectedFiles.map(file => ({
            ...file,
            tags: [...(file.tags || []), newTag]
        }));

        emit('update-success', updatedFiles);
        ElMessage.success(`成功添加标签到 ${updatedFiles.length} 个文件`);
    } catch (error) {
        console.error('添加标签失败:', error);
        ElMessage.error(`添加标签失败: ${getErrorMessage(error)}`);
    } finally {
        saving.value = false;
        tagInputVisible.value = false;
        tagInputValue.value = '';
    }
};

// 错误信息处理函数
const getErrorMessage = (error: unknown) => {
    if (error instanceof Error) {
        return error.message.includes('409') ? '标签已存在' : error.message;
    }
    return String(error);
};

watch(() => props.selectedFiles, (newVal) => {
    localSelectedFiles.value = newVal ? [...newVal] : [];
    updateTagClassification();
}, { immediate: true, deep: true });

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

.detail-row {
    display: flex;
    margin-bottom: 12px;
    font-size: 14px;
}

.detail-value {
    flex: 1;
}
.dashed-tag {
    border: dashed 1px rgb(198, 198, 198);
    cursor: pointer;
    background-color: #f2f2f2;
    color: #9e9e9e;
}

.dashed-tag:hover {
    background-color: #f0f9eb; /* 悬停效果 */
    border-color: #c2e7b0;
}
</style>