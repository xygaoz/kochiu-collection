<template>
    <div
        class="search-form-wrapper"
        ref="wrapperRef"
        :class="{
      'expanded': isExpanded,
      'expanding': isExpanding,
      'collapsing': isCollapsing
    }"
    >
        <el-form
            :model="searchForm"
            ref="searchFormRef"
            inline
            class="search-form-container"
        >
            <!-- 左侧表单内容 -->
            <div class="form-content">
                <el-form-item label="关键词" prop="keyword" class="form-item">
                    <el-input
                        v-model="searchForm.keyword"
                        placeholder="搜索标题和描述"
                        clearable
                        size="small"
                        class="keyword-input"
                    />
                </el-form-item>

                <el-form-item label="类别" prop="types" class="form-item"
                              v-if="props.dataType != 'type'"
                >
                    <el-checkbox-group v-model="searchForm.types" size="small" class="checkbox-group"
                        @change="handleSearch">
                        <el-checkbox-button
                            v-for="type in typeOptions"
                            :key="type.value"
                            :label="type.value"
                            class="checkbox-button"
                        >
                            {{ type.label }}
                        </el-checkbox-button>
                    </el-checkbox-group>
                </el-form-item>

                <el-form-item label="标签" prop="tags" class="form-item tag-item"
                              v-if="props.dataType != 'tag'"
                >
                    <div class="tag-container">
                        <el-tag
                            v-for="tag in searchForm.tags"
                            :key="tag"
                            closable
                            size="small"
                            @close="handleClose(tag)"
                            class="custom-tag"
                        >
                            {{ tag }}
                        </el-tag>
                        <el-input
                            v-if="inputVisible"
                            ref="tagInputRef"
                            v-model="inputValue"
                            class="tag-input"
                            size="small"
                            @keyup.enter="handleInputConfirm"
                            @blur="handleInputConfirm"
                        />
                        <el-button
                            v-else
                            class="add-tag-btn"
                            size="small"
                            @click="showInput"
                        >
                            + 加标签
                        </el-button>
                    </div>
                </el-form-item>
            </div>

            <!-- 右侧操作按钮 -->
            <div class="action-buttons">
                <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
                <el-button size="small" @click="resetForm">重置</el-button>
                <el-button
                    v-if="showCollapseButton"
                    size="small"
                    type="text"
                    @click="toggleExpand"
                    class="expand-btn"
                >
                    {{ isExpanded ? '折叠' : '展开' }}
                    <el-icon :class="{ 'rotate-icon': isExpanded }">
                        <ArrowDown />
                    </el-icon>
                </el-button>
            </div>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, onMounted, onUnmounted, watch, defineEmits, defineProps } from "vue";
import { ElInput, ElMessage } from "element-plus";
import { ArrowDown } from '@element-plus/icons-vue';
import { getResourceTypes } from "@/apis/system-api";
import type { Resource, ResourceType, SearchForm } from "@/apis/interface";

const searchFormRef = ref();
const inputValue = ref('');
const inputVisible = ref(false);
const tagInputRef = ref<InstanceType<typeof ElInput>>();
const typeOptions = ref<ResourceType[]>([]);
const isExpanded = ref(false);
const isExpanding = ref(false);
const isCollapsing = ref(false);
const showCollapseButton = ref(false);
const wrapperRef = ref<HTMLElement>();
const emit = defineEmits(['expand-change', 'search']);

const props = defineProps({
    dataType: {
        type: String,
        default: 'category'
    }
});

const searchForm = reactive<SearchForm>({
    keyword: '',
    types: [],
    tags: []
});

const checkCollapseNeed = () => {
    nextTick(() => {
        if (wrapperRef.value) {
            const contentHeight = wrapperRef.value.scrollHeight;
            const needsCollapse = contentHeight > 40;

            showCollapseButton.value = needsCollapse;

            // 仅自动展开，不自动折叠
            // if (needsCollapse && !isExpanded.value) {
            //     toggleExpand();
            // }
        }
    });
};

const toggleExpand = () => {
    if (isExpanding.value || isCollapsing.value) return;

    if (isExpanded.value) {
        // 折叠操作
        isCollapsing.value = true;

        setTimeout(() => {
            isExpanded.value = false;
            emit('expand-change', false);

            setTimeout(() => {
                isCollapsing.value = false;
            }, 150);
        }, 150);
    } else {
        // 展开操作
        isExpanding.value = true;
        isExpanded.value = true;
        emit('expand-change', true);

        setTimeout(() => {
            isExpanding.value = false;
        }, 300);
    }
};

const handleSearch = () => {
    emit('search', searchForm);
    isExpanded.value = false;
};

const resetForm = () => {
    searchFormRef.value?.resetFields();
    searchForm.tags = [];
    checkCollapseNeed();
    emit('search', searchForm);
};

const showInput = () => {
    inputVisible.value = true;
    nextTick(() => {
        tagInputRef.value?.focus();
    });
};

const handleInputConfirm = () => {
    const val = inputValue.value.trim();
    if (val) {
        if (!searchForm.tags.includes(val)) {
            searchForm.tags.push(val);
        }
    }
    inputVisible.value = false;
    inputValue.value = '';
    checkCollapseNeed();
    handleSearch();
};

const handleClose = (tag: string) => {
    searchForm.tags = searchForm.tags.filter(t => t !== tag);
    checkCollapseNeed();
};

onMounted(async () => {
    try {
        const res = await getResourceTypes();
        typeOptions.value = res || [];
        checkCollapseNeed();
        window.addEventListener('resize', checkCollapseNeed);
    } catch (error) {
        console.error("加载失败:", error);
        ElMessage.error("加载资源类型失败");
    }
});

onUnmounted(() => {
    window.removeEventListener('resize', checkCollapseNeed);
});

watch(() => [...searchForm.tags, searchForm.types, searchForm.keyword], () => {
    checkCollapseNeed();
}, { deep: true });
</script>

<style scoped>
.search-form-wrapper {
    height: 36px;
    overflow: hidden;
    position: relative;
    transition:
        height 0.3s ease,
        background-color 0.3s ease;
    width: 100%;
    box-sizing: border-box;
    background-color: #f5f7fa;
}

.search-form-wrapper.expanded {
    height: auto;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    z-index: 100;
    padding: 5px 18px 3px 18px;
    width: 100%;
    border-bottom: 1px solid #e9e9e9;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.search-form-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    width: 100%;
}

.form-content {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    flex: 1;
    min-width: 0;
}

.action-buttons {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
    margin-left: auto;
    margin-top: 4px;
}

.form-item {
    display: flex;
    align-items: center;
    margin-bottom: 0;
    height: 32px;
    flex-shrink: 0;
}

.tag-container {
    display: flex;
    align-items: center;
    gap: 6px;
    max-width: 100%;
    overflow-x: auto;
    scrollbar-width: none;
    padding-bottom: 2px;
}

.tag-container::-webkit-scrollbar {
    display: none;
}

.keyword-input {
    width: 180px;
}

:deep(.el-form-item__content) {
    display: flex;
    align-items: center;
}

:deep(.el-input__wrapper),
:deep(.el-checkbox-button__inner),
:deep(.el-button),
:deep(.el-tag) {
    height: 24px;
    line-height: 22px;
}

:deep(.el-form-item__label) {
    line-height: 24px;
    height: 24px;
    font-size: 12px;
    padding-bottom: 0;
}

.checkbox-group :deep(.el-checkbox-button) {
    margin-right: 4px;
}

.checkbox-group :deep(.el-checkbox-button__inner) {
    padding: 0 12px;
}

.expand-btn {
    padding: 0 5px;
}

.rotate-icon {
    transform: rotate(180deg);
    transition: transform 0.3s;
}

.tag-input {
    width: 60px;
}
</style>