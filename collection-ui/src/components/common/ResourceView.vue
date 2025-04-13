<template>
    <el-container class="page-container">
        <el-container class="main-container">
            <el-header class="search-header" :style="headerHeightStyle">
                <SearchFormView
                    @expand-change="handleExpandChange"
                    @search="handleSearch"
                />
            </el-header>

            <el-main class="content-area">
                <div v-if="loading" class="loading">加载中...</div>
                <div v-else-if="!props.files.length" class="empty">暂无数据</div>
                <component
                    ref="layoutRef"
                    :is="currentComponent"
                    :files="props.files"
                    :selectedResources="selectedResources"
                    :selectedResource="selectedResource"
                    @preview="handlePreview"
                    @multiple-selected="handleMultipleSelected"
                />

            </el-main>
        </el-container>

        <el-aside class="detail-aside">
            <FileDetailView
                v-if="selectedResource && selectedResources.length === 0"
                :file="selectedResource"
                @preview-doc="handleShowDoc"
                @update-file="handleUpdateFile"
            />
            <BatchEditView
                v-else-if="selectedResources.length > 0"
                :selected-files="selectedResources"
                @clear-selection="handleClearSelection"
                @update-success="handleUpdateSuccess"
            />
            <el-empty v-else description="请选择文件查看详情" />
        </el-aside>
    </el-container>

    <PdfPreviewDialog
        v-model="pdfDialogVisible"
        :url="pdfPreviewUrl"
    />
</template>

<script lang="ts" setup>
import { ref, computed, defineProps, defineEmits } from "vue";
import SearchFormView from "@/components/common/SearchFormView.vue";
import { SearchForm } from "@/apis/interface";
import type { Resource } from "@/apis/interface";
import WaterfallLayout from "@/components/common/WaterfallLayout.vue";
import FileDetailView from "@/components/common/FileDetailView.vue";
import BatchEditView from "@/components/common/BatchEditView.vue";
import { ElMessage } from "element-plus";
import PdfPreviewDialog from "@/components/common/PdfPreviewDialog.vue";

const formExpanded = ref(false);
const headerHeight = ref('40px');
const props = defineProps({
    files: {
        type: Array as () => Resource[],
        required: true
    },
    loading: {
        type: Boolean,
        default: false
    }
});
const currentComponent = WaterfallLayout;
const selectedResources = ref<Resource[]>([]);
const selectedResource = ref<Resource | null>(null);
const pdfDialogVisible = ref(false);
const pdfPreviewUrl = ref('');
const layoutRef = ref<{
    clearSelection: () => void
} | null>(null);

const emit = defineEmits(['update-file', 'filter-data']);

// 数据列表
const loading = ref(false)

const headerHeightStyle = computed(() => ({
    height: headerHeight.value,
    minHeight: '40px'
}));

const handleExpandChange = (expanded: boolean) => {
    formExpanded.value = expanded;
    headerHeight.value = expanded ? 'auto' : '40px';
};

// 搜索处理
const handleSearch = (searchForm: SearchForm) => {
    selectedResource.value = null;
    selectedResources.value = []
    emit('filter-data', searchForm);
};

const handlePreview = (resource: Resource) => {
    selectedResource.value = resource;
};

const handleMultipleSelected = (resources: Resource[]) => {
    selectedResources.value = resources;
}

const handleShowDoc = (resource: Resource) => {
    if(!resource.previewUrl){
        ElMessage.warning('该文件不支持预览');
        return;
    }
    pdfPreviewUrl.value = resource.previewUrl;
    pdfDialogVisible.value = true;
};

const handleUpdateFile = (params: Resource) => {
    const index = props.files.findIndex(file => file.resourceId === params.resourceId);
    if (index !== -1) {
        emit('update-file', params);
    }
};

const handleClearSelection = () => {
    layoutRef.value?.clearSelection?.();
    selectedResources.value = [];
};

const handleUpdateSuccess = (resources: Resource[]) => {
    resources.forEach(resource => {
        handleUpdateFile(resource);
    });
};

</script>

<style scoped>
.page-container {
    height: 100vh;
}

.main-container {
    position: relative;
    flex: 1;
}

.search-header {
    padding: 5px 18px;
    position: relative;
    overflow: visible;
    transition: height 0.3s ease;
    z-index: 100;
}


.content-area {
    will-change: margin-top;
    overflow: auto;
    height: calc(100% - 32px);
}


.detail-aside {
    width: 280px;
    background-color: #fff;
    border-left: 1px solid #e6e6e6;
    overflow-y: auto;
    height: 100%;
    flex-shrink: 0;
    padding: 0 !important;
}

.loading, .empty {
    text-align: center;
    padding: 50px;
    color: #999;
}
</style>