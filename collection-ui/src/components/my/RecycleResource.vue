<template>
    <ResourceView
        v-model:files="files"
        :loading="loading"
        :data-type="dataType"
        :id="0"
        @filter-data="handleSearch"
    />
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { listRecycleFiles } from "@/apis/resource-api";
import { Resource, SearchForm } from "@/apis/interface";
import ResourceView from "@/components/common/ResourceView.vue";

const files = ref<Resource[]>([]);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(500);
const total = ref(0);
const dataType = ref("recycle")

onMounted(() => {
    handleSearch({ keyword: "", types: [], tags: [] });
});

const handleSearch = async (searchForm: SearchForm) => {
    try {
        loading.value = true;
        currentPage.value = 1
        const data = await listRecycleFiles(currentPage.value, pageSize.value, searchForm);
        files.value = data.list;
        total.value = data.total;
        currentPage.value = data.pageNum;
    } catch (error) {
        console.error("加载失败:", error);
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped>
</style>