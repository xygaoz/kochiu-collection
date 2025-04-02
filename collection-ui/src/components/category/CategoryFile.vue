<template>
    <div class="category-file-container">
        <div class="image-gallery">
            <div v-for="file in files" :key="file.resourceId" @click="selectImage(file)">
                <img :src="file.thumbnailUrl" :alt="file.title ? file.title : file.sourceFileName" />
            </div>
        </div>
        <div class="image-details" v-if="selectedImage">
            <p><strong>名称:</strong> {{ selectedImage.sourceFileName }}</p>
            <p><strong>评分:</strong> {{ selectedImage.star }}</p>
            <p><strong>大小:</strong> {{ selectedImage.size }}</p>
            <p><strong>创建时间:</strong> {{ selectedImage.createdTime }}</p>
            <p><strong>修改时间:</strong> {{ selectedImage.updateTime }}</p>
            <div class="tags">
                <span v-for="tag in selectedImage.tags" :key="tag">{{ tag }}</span>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { listCategoryFiles } from "@/apis/services"; // 假设存在此方法用于获取文件列表
import { Resource } from "@/apis/interface";
import { useRoute } from "vue-router"; // 引入useRoute来获取路由参数

let files = reactive<Resource[]>([]);
const selectedImage = ref<Resource | null>(null);
const route = useRoute(); // 获取路由实例
const cateId = route.params.cateId as string; // 从路由参数中获取分类ID

onMounted(async () => {
    try {
        alert(cateId)
        files = await listCategoryFiles(cateId); // 使用分类ID加载文件列表
    } catch (error) {
        console.error('加载文件失败:', error);
    }
});

const selectImage = (file: Resource) => {
    selectedImage.value = file;
};
</script>

<style scoped>
.category-file-container {
    display: flex;
}

.image-gallery {
    width: 70%;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
}

.image-gallery img {
    width: 100%;
    height: auto;
    cursor: pointer;
    border-radius: 8px;
    transition: transform 0.2s;
}

.image-gallery img:hover {
    transform: scale(1.05);
}

.image-details {
    width: 30%;
    padding: 24px;
    background-color: #f9f9f9;
    border-left: 1px solid #ddd;
}

.image-details h2 {
    margin-bottom: 16px;
}

.image-details p {
    margin-bottom: 8px;
}

.image-details .tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.image-details .tags span {
    background-color: #eaeaea;
    padding: 4px 8px;
    border-radius: 4px;
}
</style>