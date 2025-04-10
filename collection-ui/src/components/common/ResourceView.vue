<template>
    <div class="resource-view-container">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="files.length === 0" class="empty">暂无文件</div>
        <template v-else>
            <div class="main-layout">
                <component
                    ref="layoutRef"
                    :is="currentComponent"
                    :files="files"
                    :selectedResources="selectedResources"
                    :selectedResource="selectedResource"
                    @preview="handlePreview"
                    @multiple-selected="handleMultipleSelected"
                />

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
import { ref, defineProps, defineEmits, watch } from "vue";
import { Resource } from "@/apis/interface";
import PdfPreviewDialog from "@/components/common/PdfPreviewDialog.vue";
import FileDetailView from "@/components/common/FileDetailView.vue";
import WaterfallLayout from "@/components/common/WaterfallLayout.vue";
import { ElMessage } from "element-plus";
import BatchEditView from "@/components/common/BatchEditView.vue";

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

const emit = defineEmits(['update-file']);

const selectedResource = ref<Resource | null>(null);
const pdfDialogVisible = ref(false);
const pdfPreviewUrl = ref('');
const currentComponent = WaterfallLayout;
const selectedResources = ref<Resource[]>([]);
const layoutRef = ref<{
    clearSelection: () => void
} | null>(null)

watch(() => props.files, (newFiles) => {
    // 如果选中的资源还在列表中，保持选中状态
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
    layoutRef.value?.clearSelection?.()
    selectedResources.value = []
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

.detail-aside {
    width: 280px;
    background-color: #fff;
    border-left: 1px solid #e6e6e6;
    overflow-y: auto;
    height: 100%;
    flex-shrink: 0;
}

@media (max-width: 768px) {
    .detail-aside {
        display: none;
    }
}
</style>