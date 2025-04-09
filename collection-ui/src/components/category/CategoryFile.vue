<template>
    <div class="category-file-container">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="files.length === 0" class="empty">暂无文件</div>
        <template v-else>
            <div class="main-layout">
                <component
                    :is="currentComponent"
                    :files="files"
                    :selectedImage="selectedImage"
                    @preview="handlePreview"
                />

                <el-aside class="detail-aside">
                    <FileDetailView
                        v-if="selectedImage"
                        :file="selectedImage"
                        @preview-doc="handleShowDoc"
                        @update-file="handleUpdateFile"
                    />
                    <el-empty v-else description="请选择文件查看详情" />
                </el-aside>
            </div>
        </template>
    </div>

    <PdfPreviewDialog
        v-model="pdfDialogVisible"
        :url="pdfPreviewUrl"
    />
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { listCategoryFiles } from "@/apis/services";
import { Resource } from "@/apis/interface";
import PdfPreviewDialog from "@/components/category/PdfPreviewDialog.vue";
import FileDetailView from "@/components/category/FileDetailView.vue";
import WaterfallLayout from "@/components/category/WaterfallLayout.vue";
import { ElMessage } from "element-plus";

const route = useRoute();
const cateId = route.params.cateId as string;
const files = ref<Resource[]>([]);
const selectedImage = ref<Resource | null>(null);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(500);
const total = ref(0);
const pdfDialogVisible = ref(false);
const pdfPreviewUrl = ref('');
const currentComponent = WaterfallLayout;

// 加载数据
onMounted(async () => {
    try {
        const data = await listCategoryFiles(cateId, currentPage.value, pageSize.value);
        files.value = data.list;
        total.value = data.total;
        currentPage.value = data.pageNum;
    } catch (error) {
        console.error("加载失败:", error);
    } finally {
        loading.value = false;
    }
});

const handlePreview = (image: Resource) => {
    selectedImage.value = image;
};

const handleShowDoc = (image: Resource) => {
    if(!image.previewUrl){
        ElMessage.warning('该文件不支持预览');
        return;
    }
    pdfPreviewUrl.value = image.previewUrl;
    pdfDialogVisible.value = true;
};

const handleUpdateFile = (params: Resource) => {
    const index = files.value.findIndex(file => file.resourceId === params.resourceId);
    if (index !== -1) {
        // 更新文件对象
        files.value[index] = { ...files.value[index], ...params };
        console.log('文件已更新:', params);
    } else {
        console.warn('未找到对应的文件:', params.resourceId);
    }
};

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
    font-size: 21px;
    width: 100%;
}

.category-file-container {
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

/* 响应式调整 */
@media (max-width: 768px) {
    .detail-aside {
        display: none;
    }
}
</style>