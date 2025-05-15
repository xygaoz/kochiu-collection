<template>
    <el-container class="resource-view-container">
        <el-container class="main-container">
            <el-header class="search-header" :style="headerHeightStyle">
                <SearchFormView
                    :data-type="props.dataType"
                    :id="props.id"
                    :current-select="props.currentSelect"
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
                    :has-more="props.hasMore"
                    :selectedResources="selectedResources"
                    :selectedResource="selectedResource"
                    @preview="handlePreview"
                    @multiple-selected="handleMultipleSelected"
                    @move-to-category="handleMoveTo"
                    @move-to-recycle="handleMoveToRecycle"
                    @restore="handleRestore"
                    @load-more="handleLoadMore"
                />

            </el-main>
            <el-footer class="total-area">
                <div class="total-area-left">
                    <el-checkbox v-model="selectAll" size="small" @change="handleAll">
                        全选
                    </el-checkbox>
                </div>
                <div class="total-area-right">
                    <span>总量 {{ props.total }}</span><span>已加载 {{ props.files.length }}</span>
                </div>
            </el-footer>
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
                @move="handleMoveTo"
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
    <MoveToCatalog ref="moveToCatalogDialog" @success="handleMoveSuccess" />
</template>

<script lang="ts" setup>
import { computed, defineEmits, defineProps, ref } from "vue";
import SearchFormView from "@/components/common/SearchFormView.vue";
import type { Resource } from "@/apis/interface";
import { SearchForm } from "@/apis/interface";
import WaterfallLayout from "@/components/common/WaterfallLayout.vue";
import FileDetailView from "@/components/common/FileDetailView.vue";
import BatchEditView from "@/components/common/BatchEditView.vue";
import { ElMessage, ElMessageBox } from "element-plus";
import PdfPreviewDialog from "@/components/common/PdfPreviewDialog.vue";
import MoveToCategory from "@/components/category/MoveToCategory.vue";
import MoveToCatalog from "@/components/catalog/MoveToCatalog.vue";
import { moveToRecycle, restoreFormRecycle } from "@/apis/resource-api";

const formExpanded = ref(false);
const selectAll = ref(false);
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
    },
    id: {
        type: String,
        required: true
    },
    currentSelect:{
        type: String,
        default: ''
    },
    hasMore:{
        type: Boolean,
        default: false
    },
    total:{
        type: Number,
        default: 0
    }
});
const currentComponent = WaterfallLayout;
const selectedResources = ref<Resource[]>([]);
const selectedResource = ref<Resource | null>(null);
const pdfDialogVisible = ref(false);
const pdfPreviewUrl = ref('');
const moveToCategoryDialog = ref();
const moveToCatalogDialog = ref();
const layoutRef = ref<{
    clearSelection: () => void,
    selectAll: () => void,
} | null>(null);

const searchData = ref<SearchForm>({
    cateId: '',
    keyword: '',
    types: [],
    tags: [],
    include: false
});

const emit = defineEmits(['update-file', 'filter-data', 'load-more']);

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

    const protocol = window.location.protocol;
    const host = window.location.host;
    pdfPreviewUrl.value = process.env.NODE_ENV === 'production'
        ? `${protocol}//${host}${resource.previewUrl}`
        : resource.previewUrl;
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

const handleMoveTo = (resources: Resource[]) => {
    //打开移动到分类弹窗
    const resourceIds = resources.map(r => r.resourceId);
    if(props.dataType === 'category') {
        moveToCategoryDialog.value?.open(resourceIds);
    }
    else if(props.dataType === 'catalog'){
        moveToCatalogDialog.value?.open(resourceIds);
    }
};

// 移动到分类
const handleMoveSuccess = () => {
    handleClearSelection();
    emit('filter-data', searchData);
};

const handleMoveToRecycle = (resources: Resource[], isDelete: boolean) => {
    if (isDelete) {
        // Directly show delete confirmation if isDelete=true
        ElMessageBox.confirm(
            '您将彻底删除选择的资源，还要继续吗？',
            '警告',
            {
                confirmButtonText: '确认',
                cancelButtonText: '取消',
                type: 'warning',
            }
        ).then(async () => {
            await performMoveToRecycle(resources, true);
        });
    } else {
        // Show option dialog when isDelete=false
        ElMessageBox({
            title: '删除选项',
            message: '请选择删除方式',
            showCancelButton: true,
            confirmButtonText: '放入回收站',
            cancelButtonText: '彻底删除',
            distinguishCancelAndClose: true,
            closeOnClickModal: false,
            beforeClose: async (action, instance, done) => {
                if (action === 'confirm') {
                    // Move to recycle bin
                    await performMoveToRecycle(resources, false);
                    done();
                } else if (action === 'cancel') {
                    // Delete permanently
                    ElMessageBox.confirm(
                        '您将彻底删除选择的资源，还要继续吗？',
                        '警告',
                        {
                            confirmButtonText: '确认',
                            cancelButtonText: '取消',
                            type: 'warning',
                        }
                    ).then(async () => {
                        await performMoveToRecycle(resources, true);
                        done();
                    }).catch(() => {
                        done();
                    });
                } else {
                    done(); // Just close the dialog
                }
            }
        });
    }
};

const performMoveToRecycle = async (resources: Resource[], isDelete: boolean) => {
    const resourceIds = resources.map(r => r.resourceId);
    let result = await moveToRecycle(resourceIds, {
        deleted: isDelete
    });
    if (result) {
        ElMessage.success(isDelete ? '删除成功' : '已移动到回收站');
        handleClearSelection();
        emit('filter-data', searchData);
    }
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

const handleAll = () => {
    if(selectAll.value){
        handleSelectAll();
    }
    else{
        handleClearSelection();
    }
};

// 处理加载更多
const handleLoadMore = async () => {
    try {
        // 调用父组件的方法加载更多数据
        emit('load-more', searchData);
    } catch (error) {
        console.error('加载更多失败:', error);
    }
};
</script>

<style scoped>
.resource-view-container {
    display: flex;
    height: calc(100vh - 58px);
    overflow: hidden;
    background-color: var(--el-bg-color-view);
}

.main-container {
    position: relative;
    flex: 1;
}

.search-header {
    padding: 5px 0;
    position: relative;
    overflow: visible;
    transition: height 0.3s ease;
    z-index: 100;
}


.content-area {
    padding: 10px!important;
    will-change: margin-top;
    overflow: auto;
    height: calc(100% - 32px);
}


.detail-aside {
    width: 280px;
    background-color: var(--el-bg-color-view);
    border-left: 1px solid var(--el-border-color-light);
    overflow-y: auto;
    height: 100%;
    flex-shrink: 0;
    padding: 0 !important;
}

.loading, .empty {
    text-align: center;
    padding: 50px;
    color: var(--el-text-color-secondary);
}

.total-area{
    height: 24px;
    background-color: var(--el-footer-bg-color);
}

.total-area-left{
    float: left;
}

.total-area-right{
    float: right;
    font-size: 12px;
    margin: 3px 0 0 0;
}

.total-area-right span{
    margin-left: 10px;
}
</style>