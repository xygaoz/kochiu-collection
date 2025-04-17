<template>
    <el-container>
        <el-header class="cata-header">
            路径:
            <span class="path-segment" v-for="(segment, index) in processedPath" :key="index">
                <router-link
                    v-if="segment.sno !== undefined"
                    :to="`/Catalog/${segment.sno}`"
                    class="path-link"
                >
                    {{ segment.name }}
                </router-link>
                <span v-else>{{ segment.name }}</span>
                <span class="slash" v-if="index < processedPath.length - 1"> / </span>
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
import { ref, watch, computed } from "vue";
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
const dataType = ref("catalog")
const pathVo = ref<PathVo>({
    path: "/",
    pathInfo: []
})

// 计算属性，将 pathInfo 转换为可用的路径段数组
const processedPath = computed(() => {
    // 确保pathInfo存在且是数组
    const pathInfo = Array.isArray(pathVo.value.pathInfo) ? pathVo.value.pathInfo : [];

    // 处理API返回的pathInfo
    if (pathInfo.length > 0) {
        return pathInfo.map(info => ({
            name: info.cataName,
            sno: info.sno
        }));
    }

    // 备用方案：处理path字符串
    const segments = (pathVo.value.path || '').split('/').filter(Boolean);
    if (segments.length > 0) {
        return segments.map(segment => ({
            name: segment,
            sno: undefined
        }));
    }

    // 默认返回当前目录
    return [{ name: '当前目录', sno: cataSno.value }];
});


watch(
    () => route.params.sno,
    async (newId) => {
        if (newId) {
            try {
                loading.value = true;
                cataSno.value = newId as string;

                // 加载路径信息
                const pathData = await getCatalogPath(newId as string);
                console.log('API返回的路径数据:', pathData); // 调试日志

                pathVo.value = {
                    path: pathData.path || `/${newId}`,
                    pathInfo: Array.isArray(pathData.pathInfo) ? pathData.pathInfo : []
                };

                // 加载文件列表
                const data = await listCatalogFiles(newId as string, currentPage.value, pageSize.value, {});
                files.value = data.list;
                total.value = data.total;
            } catch (error) {
                console.error("加载失败:", error);
                pathVo.value = {
                    path: `/${newId}`,
                    pathInfo: [{ sno: newId, cataName: '当前目录' }]
                };
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
.cata-header {
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
    margin: 0 2px;
    color: #666;
}

.path-link {
    color: #409EFF;
    padding: 0 3px;
    border-radius: 3px;
    transition: all 0.3s;
    text-decoration: none;
}
.path-link:hover {
    background: #ecf5ff;
    text-decoration: none;
}
</style>