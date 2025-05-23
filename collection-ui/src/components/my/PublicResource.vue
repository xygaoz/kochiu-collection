<template>
    <ResourceView
        v-model:files="files"
        :loading="loading"
        :data-type="dataType"
        id="0"
        :has-more="hasMore"
        :total="total"
        @update-file="handleFileUpdate"
        @filter-data="handleSearch"
        @load-more="loadMore"
    />
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { listPublicFiles } from "@/apis/resource-api";
import { Resource, SearchForm } from "@/apis/interface";
import ResourceView from "@/components/common/ResourceView.vue";
import { getMyConfig } from "@/apis/user-api";

const files = ref<Resource[]>([]);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(100);
const total = ref(0);
const dataType = ref("public")
const hasMore = ref(false);

onMounted(async () => {
    await handleSearch({ cateId: '', keyword: "", types: [], tags: [], include: false })

    const config = await getMyConfig()
    if (config && config.resourcePageSize) {
        pageSize.value = config.resourcePageSize
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

const handleSearch = async (searchForm: SearchForm) => {
    try {
        loading.value = true;
        currentPage.value = 1
        const data = await listPublicFiles(currentPage.value, pageSize.value, searchForm);
        files.value = data.list;
        total.value = data.total;
        currentPage.value = data.pageNum;
        hasMore.value = data.pages > currentPage.value;
    } catch (error) {
        console.error("加载失败:", error);
    } finally {
        loading.value = false;
    }
};

const loadMore = async (searchForm: SearchForm) => {
    if (loading.value) return;

    try {
        loading.value = true;
        currentPage.value += 1;
        const data = await listPublicFiles(currentPage.value, pageSize.value, searchForm);

        // 追加新数据而不是替换
        files.value = [...files.value, ...data.list];
        currentPage.value = data.pageNum;
        total.value = data.total;
        hasMore.value = data.pages > currentPage.value;
    } catch (error) {
        console.error("加载更多失败:", error);
        currentPage.value -= 1; // 回退页码
    } finally {
        loading.value = false;
    }
};

</script>

<style scoped>
</style>