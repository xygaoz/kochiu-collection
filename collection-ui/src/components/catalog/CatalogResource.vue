<template>
    <ResourceView
        v-if="cataSno"
        v-model:files="files"
        :loading="loading"
        :data-type="dataType"
        :id="cataSno"
        @update-file="handleFileUpdate"
        @filter-data="handleSearch"
    />
</template>

<script setup lang="ts">
import { ref, watch, computed } from "vue";
import { useRoute } from "vue-router";
import { listCatalogFiles } from "@/apis/resource-api";
import { Resource, SearchForm } from "@/apis/interface";
import ResourceView from "@/components/common/ResourceView.vue";

const route = useRoute();
const files = ref<Resource[]>([]);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(500);
const total = ref(0);
const dataType = ref("catalog")
const searchData = ref<SearchForm>({
    keyword: '',
    types: [],
    tags: [],
    include: false
});

const cataSno = ref("")

watch(
    () => route.params.sno,
    async (newId) => {
        if (newId) {
            try {
                loading.value = true;
                cataSno.value = newId as string;
                // 加载文件列表
                const data = await listCatalogFiles(newId as string, currentPage.value, pageSize.value,
                    searchData.value
                );
                files.value = data.list;
                total.value = data.total;
            } catch (error) {
                console.error("加载失败:", error);
            } finally {
                loading.value = false;
            }
        }
    },
    { immediate: true }
);

// 处理文件更新
const handleFileUpdate = (params: Resource): void => {
    const index = files.value.findIndex(file => file.resourceId === params.resourceId);
    if (index !== -1) {
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
        const data = await listCatalogFiles(cataSno.value, currentPage.value, pageSize.value, searchForm);
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