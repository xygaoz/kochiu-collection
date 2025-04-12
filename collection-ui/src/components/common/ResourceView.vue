<template>
    <div class="resource-view-container">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="files.length === 0" class="empty">暂无文件</div>
        <template v-else>
            <div class="main-layout">
                <el-container class="main-container">
                    <el-header class="search-header" :style="headerHeightStyle">
                        <SearchFormView
                            @expand-change="handleExpandChange"
                            @search="handleSearch"
                        />
                    </el-header>
                    <el-main class="content-area" :style="contentMarginStyle">
                        <component
                            ref="layoutRef"
                            :is="currentComponent"
                            :files="files"
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
            </div>
        </template>

        <PdfPreviewDialog
            v-model="pdfDialogVisible"
            :url="pdfPreviewUrl"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, watch, computed } from "vue";
import { Resource, SearchForm } from "@/apis/interface";
import PdfPreviewDialog from "@/components/common/PdfPreviewDialog.vue";
import FileDetailView from "@/components/common/FileDetailView.vue";
import WaterfallLayout from "@/components/common/WaterfallLayout.vue";
import { ElMessage } from "element-plus";
import BatchEditView from "@/components/common/BatchEditView.vue";
import SearchFormView from "@/components/common/SearchFormView.vue";

const formExpanded = ref(false);
const headerHeight = ref('32px');

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

const emit = defineEmits(['update-file', 'filter-data']);

const selectedResource = ref<Resource | null>(null);
const pdfDialogVisible = ref(false);
const pdfPreviewUrl = ref('');
const currentComponent = WaterfallLayout;
const selectedResources = ref<Resource[]>([]);
const layoutRef = ref<{
    clearSelection: () => void
} | null>(null);

const handleExpandChange = (expanded: boolean) => {
    formExpanded.value = expanded;
    headerHeight.value = expanded ? 'auto' : '32px';
};

const headerHeightStyle = computed(() => ({
    height: headerHeight.value,
    minHeight: '32px'
}));

const contentMarginStyle = computed(() => ({
    marginTop: formExpanded.value ? '40px' : '0',
    transition: 'margin-top 0.3s ease'
}));

watch(() => props.files, (newFiles) => {
    if (selectedResource.value) {
        const exists = newFiles.some(file => file.resourceId === selectedResource.value?.resourceId);
        if (!exists) {
            selectedResource.value = null;
        }
    }
});

const handlePreview = (resource: Resource) => {
    selectedResource.value = resource;
};

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

const handleMultipleSelected = (resources: Resource[]) => {
    selectedResources.value = resources;
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

const handleSearch = (searchForm: SearchForm) => {
    emit('filter-data', searchForm);
}
</script>

<style scoped>
.loading {
    padding: 20px;
    text-align: center;
    color: #666;
}

.empty {
    padding: 100px;
    text-align: center;
    color: #666;
    font-size: 18px;
    width: 100%;
}

.resource-view-container {
    display: flex;
    flex-direction: column;
    height: calc(100vh - 60px);
    overflow: hidden;
}

.main-layout {
    display: flex;
    height: 100%;
    overflow: hidden;
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
    z-index: 1;
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

@media (max-width: 768px) {
    .detail-aside {
        display: none;
    }
}
</style>