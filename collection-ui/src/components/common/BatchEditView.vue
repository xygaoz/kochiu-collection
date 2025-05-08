<template>
    <div class="batch-edit-view">
        <div class="header">
            <div class="title">
                批量编辑 ({{ selectedCount }}个文件)
            </div>
            <div class="actions">
                <el-tooltip
                    v-for="action in actions.filter(action => action.show())"
                    :key="action.name"
                    :content="action.tooltip"
                    placement="bottom"
                    :show-after="300"
                >
                    <div class="action-item" @click="action.handler">
                        <el-icon v-if="action.icon" :size="action.size || 'medium'">
                            <component :is="action.icon" />
                        </el-icon>
                        <i v-else
                           :class="action.class"
                        />
                    </div>
                </el-tooltip>
            </div>
        </div>

        <div class="detail-row" v-if="!isRecycleBin">
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
        <div class="detail-row">
            <el-checkbox
                v-model="checkAll"
                :indeterminate="isIndeterminate"
                @change="handleBatchShare"
            >
                公开
            </el-checkbox>
        </div>
        <div class="detail-row">
            <TransitionGroup name="tag" tag="div" class="tags">
                <div class="tag-label">标签</div>
                <!-- 实线标签（可删除） -->
                <el-tag
                    v-for="tag in tagState.commonTags"
                    :key="tag.tagId"
                    :closable="!isRecycleBin"
                    @close="removeTag(tag)">
                    {{ tag.tagName }}
                </el-tag>

                <el-tag
                    v-for="tag in tagState.partialTags"
                    :key="tag.tagName"
                    :class="isRecycleBin ? 'dashed-tag-dis' : 'dashed-tag'"
                    @click="addTagToMissingFiles(tag)">
                    {{ tag.tagName }}
                </el-tag>
                <Transition name="fade">
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
                        v-else-if="!isRecycleBin"
                        class="button-new-tag"
                        size="small"
                        @click="showTagInput"
                        @click.stop
                    >
                        + 新标签
                    </el-button>
                </Transition>
            </TransitionGroup>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, defineProps, defineEmits, watch, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import type { Resource, Tag } from '@/apis/interface';
import { CircleCheck, CloseBold, Connection, Delete } from "@element-plus/icons-vue";
import { batchAddTag, batchRemoveTag, bacthUpdateResource, batchShareResources } from "@/apis/resource-api";

interface Props {
    selectedFiles: Resource[];
    dataType: string;
}

const props = defineProps<Props>();
const emit = defineEmits(['update-success', 'clear-selection', 'select-all', 'delete', 'move', 'restore']);

const tagInputVisible = ref(false);
const tagInputValue = ref('');
const tagInputRef = ref();
const saving = ref(false);

const checkAll = ref(false)
const isIndeterminate = ref(true)

const selectedCount = computed(() => props.selectedFiles.length);
const isRecycleBin = computed(() => props.dataType === 'recycle');

const actions = [
    {
        name: 'clear',
        icon: CloseBold,
        tooltip: '取消选择',
        show: () => true,
        handler: () => emit('clear-selection'),
    },
    {
        name: 'select-all',
        icon: CircleCheck,
        tooltip: '全选',
        show: () => true,
        handler: () => emit('select-all'),
    },
    {
        name: 'delete',
        icon: Delete,
        tooltip: '批量删除',
        show: () => true,
        handler: () => emit('delete', props.selectedFiles, props.dataType === 'recycle'),
        size: 'default'
    },
    {
        name: 'move',
        icon: Connection,
        tooltip: '移动到',
        show: () => !isRecycleBin.value,
        handler: () => emit('move', props.selectedFiles),
    },
    {
        name: 'restore',
        class: 'iconfont icon-col-huanyuan',
        tooltip: '恢复',
        show: () => isRecycleBin.value,
        handler: () => emit('restore', props.selectedFiles),
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
    shareClassification();
});

// 判断标签是否属于所有文件
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

const shareClassification = () => {
    let isShare = true;
    let isNotShare = true;
    localSelectedFiles.value.forEach((file: Resource) => {
        if(!file.share){
            isShare = false;
        }
        else{
            isNotShare = false;
        }
    });
    isIndeterminate.value = !isShare && !isNotShare;
    checkAll.value = isShare;
}

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
        if(isRecycleBin.value){
            return
        }
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
        await batchAddTag(resourceIds, { tagName: tag.tagName });

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
        // 1. 防御性检查
        if (!localSelectedFiles.value || !Array.isArray(localSelectedFiles.value)) {
            console.error("文件数据未正确初始化");
            return
        }

        // 2. 创建更新副本（深拷贝）
        const updatedFiles = JSON.parse(JSON.stringify(localSelectedFiles.value));

        // 3. 更新本地状态
        updatedFiles.forEach((file: Resource) => {
            file.tags = (file.tags || []).filter(t => t.tagId !== tag.tagId);
        });

        // 4. 立即更新UI状态
        localSelectedFiles.value = updatedFiles;
        updateTagClassification();

        // 5. 异步操作
        await batchRemoveTag(
            updatedFiles.map((f: Resource) => f.resourceId),
            { tagId: tag.tagId }
        );

        // 6. 通知父组件
        emit('update-success', updatedFiles);
        ElMessage.success('标签已移除');
    } catch (error) {
        console.error('移除标签失败:', error);
        ElMessage.error(`移除失败: ${error instanceof Error ? error.message : '未知错误'}`);

        // 7. 回滚UI状态
        updateTagClassification();
    }
};

const handleTagInputConfirm = async () => {
    // 0. 空值检查
    if (!tagInputValue.value?.trim()) {
        tagInputVisible.value = false;
        return;
    }

    try {
        saving.value = true;
        const tagName = tagInputValue.value.trim();

        // 1. 防御性检查
        if (!localSelectedFiles.value || !Array.isArray(localSelectedFiles.value)) {
            console.error("文件数据未初始化");
            return;
        }

        // 2. 检查标签是否已存在（不区分大小写）
        const alreadyExists = localSelectedFiles.value.some(file =>
            file.tags?.some(t => t.tagName.toLowerCase() === tagName.toLowerCase())
        );

        if (alreadyExists) {
            ElMessage.warning(`标签 "${tagName}" 已存在`);
            return;
        }

        // 3. 创建新标签对象
        const newTag: Tag = {
            tagId: Date.now(), // 临时ID，实际应由API返回
            tagName: tagName
        };

        // 4. 深拷贝当前状态
        const updatedFiles = JSON.parse(JSON.stringify(localSelectedFiles.value));

        // 5. 更新所有文件的标签
        updatedFiles.forEach((file: Resource) => {
            file.tags = [...(file.tags || []), newTag];
        });

        // 6. 立即更新本地状态
        localSelectedFiles.value = updatedFiles;
        updateTagClassification();

        // 7. 调用API（使用真实API返回的标签）
        const apiTag = await batchAddTag(
            updatedFiles.map((f: Resource) => f.resourceId),
            { tagName }
        );

        // 8. 用API返回的标签更新状态（如果不同）
        if (apiTag && apiTag.tagId !== newTag.tagId) {
            localSelectedFiles.value = localSelectedFiles.value.map(file => ({
                ...file,
                tags: file.tags?.map(t =>
                    t.tagId === newTag.tagId ? apiTag : t
                ) || []
            }));
            updateTagClassification();
        }

        // 9. 通知父组件
        emit('update-success', [...localSelectedFiles.value]);
        ElMessage.success(`标签 "${tagName}" 添加成功`);

    } catch (error) {
        console.error('添加标签失败:', error);
        ElMessage.error(`添加失败: ${getErrorMessage(error)}`);

        // 10. 回滚状态
        updateTagClassification();

    } finally {
        // 11. 重置UI状态
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

// 批量共享
const handleBatchShare = async (val: boolean) => {
    if (props.selectedFiles.length === 0) {
        ElMessage.warning('请先选择文件');
        return;
    }

    try {
        saving.value = true;
        isIndeterminate.value = false

        const resourceIds = props.selectedFiles.map(file => file.resourceId);
        const success = await batchShareResources(resourceIds, val);

        if (success) {
            // 当API只返回布尔值时，直接构建更新后的数组
            const updatedFiles: Resource[] = props.selectedFiles.map(file => ({
                ...file,
                share: val,
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

watch(() => props.selectedFiles, (newVal) => {
    localSelectedFiles.value = newVal ? [...newVal] : [];
    updateTagClassification();
    shareClassification();
}, { immediate: true, deep: true });

</script>

<style scoped>
.batch-edit-view {
    padding: 16px;
    display: flex;
    flex-direction: column;
    background-color: var(--el-bg-color-view);
    color: var(--el-text-color-regular);
}

.edit-form {
    flex: 1;  /* 表单区域填满剩余空间 */
    display: flex;
    flex-direction: column;
}

.full-width-input {
    width: 100%;
}

.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    transition: all 0.3s ease; /* 新增：容器过渡 */
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
    background-color: var(--el-color-primary-light-9);
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

.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
}

/* 标签文字 */
.tag-label {
    color: var(--el-text-color-secondary);
    padding-right: 5px;
    height: 32px;
    line-height: 32px;
    flex-shrink: 0;
}


.el-tag {
    background-color: var(--el-tag-bg-color);
    color: var(--el-tag-text-color);
    border-color: var(--el-border-color-light);
    transition:
        all 0.3s ease,
        transform 0.2s ease; /* 包含变形动画 */
}

.dashed-tag {
    border: dashed 1px var(--el-border-color-light);
    background-color: var(--el-tag-bg-color);
    color: var(--el-text-color-secondary);
    transition:
        border 0.3s ease,
        background-color 0.3s ease;
    cursor: pointer;
}

.dashed-tag:hover {
    background-color: var(--el-color-primary-light-9);
    border-color: var(--el-color-primary-light-7);
}

.dashed-tag-dis{
    border: dashed 1px rgb(198, 198, 198);
    background-color: #f2f2f2;
    color: #9e9e9e;
}
</style>