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
                <el-form-item label="分类" prop="cateId" class="form-item"
                              v-if="props.dataType == 'all-category' || props.dataType == 'catalog'"
                >
                    <el-select class="category-select" v-model="searchForm.cateId" placeholder="请选择分类" size="small">
                        <el-option
                            v-for="category in categories"
                            :key="category.cateId"
                            :label="category.cateName"
                            :value="category.cateId"
                        />
                    </el-select>
                </el-form-item>

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
                                       >
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

                <TagSelector
                    v-model="searchForm.tags"
                    v-if="props.dataType != 'tag' && showTagSelector"
                    :force-close="shouldCloseTagSelector || isCollapsing"
                    ref="tagSelectorRef"
                />
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
import { defineEmits, defineProps, nextTick, onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { ElInput, ElMessage } from "element-plus";
import { ArrowDown } from "@element-plus/icons-vue";
import { getResourceTypes } from "@/apis/system-api";
import { Category, ResourceType, SearchForm } from "@/apis/interface";
import { getAllCategory } from "@/apis/category-api";
import TagSelector from "@/components/common/TagSelector.vue";

const searchFormRef = ref();
const typeOptions = ref<ResourceType[]>([]);
const isExpanded = ref(false);
const isExpanding = ref(false);
const isCollapsing = ref(false);
const showCollapseButton = ref(false);
const wrapperRef = ref<HTMLElement>();
const categories = ref<Category[]>([])
const showTagSelector = ref(true);
const shouldCloseTagSelector = ref(false);
const tagSelectorRef = ref();

const emit = defineEmits(['expand-change', 'search']);

const props = defineProps({
    dataType: {
        type: String,
        default: 'category'
    }
});

const searchForm = reactive<SearchForm>({
    cateId: '',
    keyword: '',
    types: [],
    tags: []
});

const checkCollapseNeed = () => {
    nextTick(() => {
        if (wrapperRef.value) {
            const contentHeight = wrapperRef.value.scrollHeight;
            showCollapseButton.value = contentHeight > 40;

            // 检查TagSelector是否换行到第二行
            const formItems = wrapperRef.value.querySelectorAll('.form-item');
            if (formItems.length > 1) {
                const firstItem = formItems[0].getBoundingClientRect();
                const tagSelectorItem = formItems[formItems.length - 1].getBoundingClientRect();
                shouldCloseTagSelector.value = tagSelectorItem.top > firstItem.bottom + 5;
            }
        }
    });
};

const toggleExpand = () => {
    if (isExpanding.value || isCollapsing.value) return;

    if (isExpanded.value) {
        // 折叠操作
        isCollapsing.value = true;
        // 强制关闭TagSelector的popover
        if (tagSelectorRef.value) {
            tagSelectorRef.value.closePopover();
        }
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
    // 搜索后自动折叠
    if (isExpanded.value) {
        toggleExpand();
    }
};

const resetForm = () => {
    searchFormRef.value?.resetFields();
    searchForm.tags = [];
    checkCollapseNeed();
    emit('search', searchForm);
};

const getCategories = async () => {
    try {
        const res = await getAllCategory();
        // 在获取的分类数组开头插入"全部"选项
        categories.value = [
            {
                sno: 0,          // 使用0或其他唯一标识
                cateName: "全部",
                cateId: 0        // 同样保持唯一
            },
            ...(res || [])       // 展开原始数据
        ];
    } catch (error) {
        console.error("加载失败:", error);
        // 即使出错也保证有"全部"选项
        categories.value = [{
            sno: 0,
            cateName: "全部",
            cateId: 0
        }];
    }
};

onMounted(async () => {
    try {
        const res = await getResourceTypes();
        typeOptions.value = res || [];
        checkCollapseNeed();
        await getCategories();
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

watch(isExpanded, (newVal) => {
    if (!newVal) {
        // 折叠完成后确保TagSelector显示
        showTagSelector.value = true;
    }
});
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
    box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
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

.keyword-input {
    width: 180px;
}

.category-select{
    width: 120px;
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
</style>