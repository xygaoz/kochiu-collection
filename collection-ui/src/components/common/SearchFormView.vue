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
                <div class="cata-header" v-if="dataType === 'category'">
                    <span style="margin: 0 10px 0 0;">分类: {{props.currentSelect}}</span>
                </div>
                <div class="cata-header" v-if="dataType === 'tag'">
                    <span style="margin: 0 10px 0 0;">标签: {{props.currentSelect}}</span>
                </div>
                <div class="cata-header" v-if="dataType === 'type'">
                    <span style="margin: 0 10px 0 0;">文件类型: {{props.currentSelect}}</span>
                </div>
                <div class="cata-header" v-if="dataType === 'recycle'">
                    <span style="margin: 0 10px 0 0;">回收站</span>
                </div>
                <div class="cata-header" v-if="dataType === 'all-category'">
                    <span style="margin: 0 10px 0 0;">所有分类</span>
                </div>
                <div class="cata-header" v-if="dataType === 'all-tag'">
                    <span style="margin: 0 10px 0 0;">所有标签</span>
                </div>
                <div class="cata-header" v-if="dataType === 'catalog'">
                    <span style="margin: 0 10px 0 0;">目录:</span>
                    <span class="path-segment" v-for="(segment, index) in processedPath" :key="index">
                        <router-link
                            v-if="segment.sno !== undefined"
                            :to="`/Catalog/${segment.sno}`"
                            class="path-link"
                        >
                            {{ segment.name }}
                        </router-link>
                        <span v-else>{{ segment.name }}</span>
                        <span class="slash" v-if="index < processedPath.length - 1"> / </span>
                    </span>
                </div>
                <el-form-item prop="include" class="form-item" v-if="dataType == 'catalog'">
                    <el-checkbox v-model="searchForm.include" size="small"
                                 class="checkbox-include">包含子目录</el-checkbox>
                </el-form-item>
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
import { computed, defineEmits, defineProps, nextTick, onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { ElInput, ElMessage } from "element-plus";
import { ArrowDown } from "@element-plus/icons-vue";
import { getResourceTypes } from "@/apis/system-api";
import { Category, PathVo, ResourceType, SearchForm } from "@/apis/interface";
import { getAllCategory } from "@/apis/category-api";
import TagSelector from "@/components/common/TagSelector.vue";
import { getCatalogPath } from "@/apis/catalog-api";
import { useUserStore } from "@/apis/global";
import { storeToRefs } from 'pinia'

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
const userStore = useUserStore()
const { include_sub_dir } = storeToRefs(userStore)

const emit = defineEmits(['expand-change', 'search']);

const props = defineProps({
    dataType: {
        type: String,
        default: 'category'
    },
    id: {
        type: String,
        required: true
    },
    currentSelect:{
        type: String,
        default: ''
    }
});

const searchForm = reactive<SearchForm>({
    cateId: '',
    keyword: '',
    types: [],
    tags: [],
    include: include_sub_dir.value
});

const pathVo = ref<PathVo>({
    path: "/",
    pathInfo: []
})

// 计算属性，将 pathInfo 转换为可用的路径段数组
const processedPath = computed(() => {
    // 确保pathInfo存在且是数组
    const pathInfo = Array.isArray(pathVo.value.pathInfo) ? pathVo.value.pathInfo : [];

    // 处理API返回的pathInfo
    if (pathInfo.length > 0) {
        return pathInfo.map(info => ({
            name: info.cataName,
            sno: info.sno
        }));
    }

    // 备用方案：处理path字符串
    const segments = (pathVo.value.path || '').split('/').filter(Boolean);
    if (segments.length > 0) {
        return segments.map(segment => ({
            name: segment,
            sno: undefined
        }));
    }

    // 默认返回当前目录 - 修改这里，直接使用 props.id 而不是 props.id.value
    return [{ name: '当前目录', sno: props.id }];
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
    emit('search', { ...searchForm })
    if (isExpanded.value) {
        toggleExpand()
    }
}

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

        if(props.dataType == 'catalog') {
            if (props.id) {
                // 加载路径信息
                const pathData = await getCatalogPath(props.id);
                console.log('API返回的路径数据:', pathData); // 调试日志

                pathVo.value = {
                    path: pathData.path || `/${props.id}`,
                    pathInfo: Array.isArray(pathData.pathInfo) ? pathData.pathInfo : []
                };
            }
        }

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

// 监听 include 变化
watch(() => searchForm.include, (newVal) => {
    userStore.setIncludeSubDir(newVal)
    emit('search', { ...searchForm, include: newVal })
}, { immediate: true })
</script>

<style scoped>
.search-form-wrapper {
    padding: 0 18px;
    height: 36px;
    overflow: hidden;
    position: relative;
    transition:
        height 0.3s ease,
        background-color 0.3s ease;
    width: 100%;
    box-sizing: border-box;
    background-color: var(--el-bg-color-view);
    border-bottom: 1px solid var(--el-border-color-bottom);
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
    border-bottom: 1px solid var(--el-border-color-bottom);
    box-shadow: var(--el-box-shadow);
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
    width: 100px;
}

.category-select{
    width: 100px;
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

.cata-header {
    height: 20px;
    font-size: 13px;
    display: flex;
    align-items: center;
    padding: 5px 20px 0 0;
    color: var(--el-color-primary);
}

.path-segment {
    display: inline-flex;
    align-items: center;
}

.slash {
    margin: 0 2px;
    color: #666;
}

.path-link {
    color: var(--el-color-primary);
    padding: 0 3px;
    border-radius: 3px;
    transition: all 0.3s;
    text-decoration: none;
}

.path-link:hover {
    background: var(--el-color-primary-light-9);
    text-decoration: none;
}
</style>