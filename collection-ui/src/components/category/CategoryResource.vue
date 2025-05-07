<template>
    <ResourceView
        v-model:files="files"
        :loading="loading"
        :data-type="dataType"
        :id="cateId"
        :current-select="cateName"
        :has-more="hasMore"
        @update-file="handleFileUpdate"
        @filter-data="handleSearch"
        @load-more="loadMore"
    />
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useRoute } from "vue-router";
import { listCategoryFiles } from "@/apis/resource-api";
import { Resource, SearchForm } from "@/apis/interface";
import ResourceView from "@/components/common/ResourceView.vue";
import { getCategory } from "@/apis/category-api";

const route = useRoute();
const files = ref<Resource[]>([]);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(100);
const total = ref(0);
const cateId = ref("")
const cateName = ref("未知")
const dataType = ref("category")
const hasMore = ref(false);

watch(
    () => route.params.cateId,
    async (newId) => {
        if (newId) {
            // 重新加载数据
            try {
                loading.value = true;
                const id = Array.isArray(newId) ? newId[0] : newId;
                cateId.value = id
                const data = await listCategoryFiles(id, currentPage.value, pageSize.value, {});
                files.value = data.list;
                total.value = data.total;
                currentPage.value = data.pageNum;
                hasMore.value = data.pages > currentPage.value;

                const category = await getCategory(id);
                cateName.value = category.cateName
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
        const data = await listCategoryFiles(cateId.value, currentPage.value, pageSize.value, searchForm);
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
        const data = await listCategoryFiles(
            cateId.value,
            currentPage.value,
            pageSize.value,
            searchForm
        );

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