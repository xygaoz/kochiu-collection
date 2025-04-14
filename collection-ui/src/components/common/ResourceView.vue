<template>
    <el-container class="resource-view-container">
        <el-container class="main-container">
            <el-header class="search-header" :style="headerHeightStyle">
                <SearchFormView
                    :data-type="props.dataType"
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
                    :data-type="props.dataType"
                    :selectedResources="selectedResources"
                    :selectedResource="selectedResource"
                    @preview="handlePreview"
                    @multiple-selected="handleMultipleSelected"
                    @move-to-category="handleMoveToCate"
                    @move-to-recycle="handleMoveToRecycle"
                    @restore="handleRestore"
                />

            </el-main>
        </el-container>

        <el-aside class="detail-aside">
            <FileDetailView
                v-if="selectedResource && selectedResources.length === 0"
                :file="selectedResource"
                :data-type="props.dataType"
                @preview-doc="handleShowDoc"
                @update-file="handleUpdateFile"
            />
            <BatchEditView
                v-else-if="selectedResources.length > 0"
                :selected-files="selectedResources"
                :data-type="props.dataType"
                @clear-selection="handleClearSelection"
                @update-success="handleUpdateSuccess"
                @select-all="handleSelectAll"
                @move="handleMoveToCate"
                @delete="handleMoveToRecycle"
                @restore="handleRestore"
            />
            <el-empty v-else description="请选择文件查看详情" />
        </el-aside>
    </el-container>

    <PdfPreviewDialog
        v-model="pdfDialogVisible"
        :url="pdfPreviewUrl"
    />

    <MoveToCategory ref="moveToCategoryDialog" @success="handleMoveSuccess" />
</template>

<script lang="ts" setup>
import { ref, computed, defineProps, defineEmits } from "vue";
import SearchFormView from "@/components/common/SearchFormView.vue";
import { SearchForm } from "@/apis/interface";
import type { Resource } from "@/apis/interface";
import WaterfallLayout from "@/components/common/WaterfallLayout.vue";
import FileDetailView from "@/components/common/FileDetailView.vue";
import BatchEditView from "@/components/common/BatchEditView.vue";
import { ElMessage, ElMessageBox } from "element-plus";
import PdfPreviewDialog from "@/components/common/PdfPreviewDialog.vue";
import MoveToCategory from "@/components/common/MoveToCategory.vue";
import { moveToRecycle, restoreFormRecycle } from "@/apis/resource-api";

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
    },
    dataType: {
        type: String,
        default: 'category'
    }
});
const currentComponent = WaterfallLayout;
const selectedResources = ref<Resource[]>([]);
const selectedResource = ref<Resource | null>(null);
const pdfDialogVisible = ref(false);
const pdfPreviewUrl = ref('');
const moveToCategoryDialog = ref();
const layoutRef = ref<{
    clearSelection: () => void,
    selectAll: () => void
} | null>(null);
const searchData = ref<SearchForm>({
    keyword: '',
    types: [],
    tags: []
});

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
    searchData.value = searchForm;
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

// 全选
const handleSelectAll = () => {
    layoutRef.value?.selectAll?.();
    selectedResources.value = [...props.files];
};

const handleMoveToCate = (resources: Resource[]) => {
    //打开移动到分类弹窗
    const resourceIds = resources.map(r => r.resourceId);
    moveToCategoryDialog.value?.open(resourceIds);
};

// 移动到分类
const handleMoveSuccess = () => {
    handleClearSelection();
    emit('filter-data', searchData);
};

const handleMoveToRecycle = (resources: Resource[], isDelete: boolean) => {
    ElMessageBox.confirm(
        isDelete ? '您将彻底删除选择的资源，还要继续吗？' : '您需要将选择资源删除吗？以后还能通过回收站恢复。',
        '警告',
        {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(async () => {
        const resourceIds = resources.map(r => r.resourceId);
        let result = await moveToRecycle(resourceIds, {
            deleted: isDelete
        });
        if (result) {
            ElMessage.success('删除成功');
            handleClearSelection();
            emit('filter-data', searchData);
        }
    });
};

const handleRestore = (resources: Resource[]) => {
    ElMessageBox.confirm(
        '您需要恢复选择的资源吗？',
        '警告',
        {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(async () => {
        const resourceIds = resources.map(r => r.resourceId);
        let result = await restoreFormRecycle(resourceIds, {});
        if (result) {
            ElMessage.success('恢复成功');
            handleClearSelection();
            emit('filter-data', searchData);
        }
    });

}
</script>

<style scoped>
.resource-view-container {
    display: flex;
    height: calc(100vh - 60px);
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