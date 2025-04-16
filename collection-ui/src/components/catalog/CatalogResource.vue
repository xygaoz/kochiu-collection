<template>
    <div class="catalog-resource">
        <h2>目录资源: {{ id }}</h2>
        <!-- 这里可以添加目录资源的展示逻辑 -->
    </div>
</template>

<script setup lang="ts">
import { defineProps, onMounted } from 'vue';

const props = defineProps({
    id: {
        type: String,
        required: true
    }
});

onMounted(() => {
    // 加载目录资源数据
});
</script>

<style scoped>
.catalog-resource {
    padding: 20px;
}
</style>