<template>
    <el-container>
        <el-header class="cata-header">
            路径: <span class="path-segment" v-for="(segment, index) in pathVo.path.split('/')" :key="index">
                {{ segment }}<span class="slash" v-if="index < pathVo.path.split('/').length - 1"> / </span>
              </span>
        </el-header>
        <el-main style="margin: 0; padding: 0">
            <ResourceView
                v-model:files="files"
                :loading="loading"
                :data-type="dataType"
                @update-file="handleFileUpdate"
                @filter-data="handleSearch"
            />
        </el-main>
    </el-container>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { listCatalogFiles } from "@/apis/resource-api";
import { PathVo, Resource, SearchForm } from "@/apis/interface";
import ResourceView from "@/components/common/ResourceView.vue";
import { getCatalogPath } from "@/apis/catalog-api";

const route = useRoute();
const files = ref<Resource[]>([]);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(500);
const total = ref(0);
const cataSno = ref("")
const dataType = ref("category")
const pathVo = ref<PathVo>({
    path: "/",
    pathInfo: []
})

watch(
    () => route.params.sno,
    async (newId) => {
        if (newId) {
            // 重新加载数据
            try {
                loading.value = true;
                cataSno.value = newId
                const data = await listCatalogFiles(newId, currentPage.value, pageSize.value, {});
                files.value = data.list;
                total.value = data.total;
                currentPage.value = data.pageNum;
            } catch (error) {
                console.error("加载失败:", error);
            } finally {
                loading.value = false;
            }
        }
    },
    { immediate: true }
);

onMounted(async () => {
    if (cataSno.value) {
        pathVo.value = await getCatalogPath(cataSno.value);
    } else {
        console.warn("cataSno为空，无法获取路径");
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
.cata-header{
    height: 18px;
    font-size: 13px;
    display: flex;
    align-items: end;
    padding: 0 20px;
    line-height: 1;
    color: #5e5e5e;
}

.path-segment {
    display: inline-flex;
    align-items: center;
}

.slash {
    margin: 0 2px; /* 控制斜杠前后的间距 */
    color: #666; /* 可选：改变斜杠颜色 */
}
</style>