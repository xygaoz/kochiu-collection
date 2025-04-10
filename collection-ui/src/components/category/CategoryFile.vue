<template>
    <ResourceView
        v-model:files="files"
        :loading="loading"
        @update-file="handleFileUpdate"
    />
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { listCategoryFiles } from "@/apis/services";
import type { Resource } from "@/apis/interface";
import ResourceView from "@/components/common/ResourceView.vue";

const route = useRoute();
const cateId = route.params.cateId as string;
const files = ref<Resource[]>([]);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(500);
const total = ref(0);

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

// 处理文件更新
const handleFileUpdate = (params: Resource): void => {
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
</style>