<template>
    <ResourceView
        v-model:files="files"
        :loading="loading"
        :data-type="dataType"
        :id="tagId"
        :current-select="tagName"
        :has-more="hasMore"
        :total="total"
        @update-file="handleFileUpdate"
        @filter-data="handleSearch"
        @load-more="loadMore"
    />
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useRoute } from "vue-router";
import { listTagFiles } from "@/apis/resource-api";
import { Resource, SearchForm } from "@/apis/interface";
import ResourceView from "@/components/common/ResourceView.vue";
import { getTag } from "@/apis/tag-api";

const route = useRoute();
const files = ref<Resource[]>([]);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(100);
const total = ref(0);
const tagId = ref("")
const tagName = ref("未知")
const dataType = ref("tag")
const hasMore = ref(false);

watch(
    () => route.params.tagId,
    async (newId) => {
        if (newId) {
            // 重新加载数据
            try {
                loading.value = true;
                const id = Array.isArray(newId) ? newId[0] : newId;
                tagId.value = id
                const data = await listTagFiles(id, currentPage.value, pageSize.value, {});
                files.value = data.list;
                total.value = data.total;
                currentPage.value = data.pageNum;
                hasMore.value = data.pages > currentPage.value;

                const tag = await getTag(id);
                if(tag){
                    tagName.value = tag.tagName
                }
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
        const data = await listTagFiles(tagId.value, currentPage.value, pageSize.value, searchForm);
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
        const data = await listTagFiles(tagId.value, currentPage.value, pageSize.value, searchForm);

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