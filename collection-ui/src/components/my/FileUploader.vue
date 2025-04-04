<template>
    <div class="upload-dragger">
        <div class="title">文件上传</div>
        <!-- 添加分类选择下拉框 -->
        <div class="category">
            <div class="select-label">选择分类</div>
            <div class="select-container"><el-select v-model="selectedCategory" placeholder="请选择分类">
                <el-option
                    v-for="category in categories"
                    :key="category.sno"
                    :label="category.cateName"
                    :value="category.sno"
                />
            </el-select>
            </div>
        </div>

        <el-upload
            class="upload-area"
            drag
            action="/upload"
            multiple
            :before-upload="beforeUpload"
        >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
                拖拽文件到这里，或点击<em>这里</em>上传，支持多文件同时上传
            </div>
            <template #tip>
                <div class="el-upload__tip">
                    每个文件大小不超过 100MB，支持上传文件类型：
                    <em>*.jpg、*.jpeg、*.png、*.gif、*.bmp、*.webp、*.psd、*.doc 、*.docx、*.xls、*.xlsx、*.ppt、*.pptx、*.pdf、*.txt、*.mp4、*.avi、*.wmv、*.mov、*.mkv、*.mp3、*.wav、*.flac</em>
                </div>
            </template>
        </el-upload>
    </div>
</template>

<script setup lang="ts">
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from "element-plus";
import { ref, onMounted, reactive } from "vue";
import { listCategory, uploadFile } from "@/apis/services"; // 导入uploadFile方法
import { Category } from "@/apis/interface"; // 导入Category接口

// 定义允许的文件类型和最大文件大小（2MB）
const allowedTypes = [
    'image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp', 'image/vnd.adobe.photoshop',
    'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'application/pdf', 'text/plain', 'video/mp4', 'video/avi', 'video/wmv', 'video/quicktime',
    'video/x-matroska', 'audio/mpeg', 'audio/wav', 'audio/flac'
];
const maxSize = 100 * 1024 * 1024; // 100MB in bytes

// 上传前的文件检查
const beforeUpload = (file: File) => {
    const isAllowedType = allowedTypes.includes(file.type);
    if (!isAllowedType) {
        ElMessage.error(`文件类型 ${file.type} 不支持`);
        return false;
    }

    const isLtMaxSize = file.size <= maxSize;
    if (!isLtMaxSize) {
        ElMessage.error(`文件大小不能超过 100MB`);
        return false;
    }

    // 调用上传文件的方法
    uploadFile(file, selectedCategory.value!).then((response) => {
        if (response) {
            ElMessage.success("文件上传成功");
        }
    }).catch((error) => {
        ElMessage.error("文件上传失败");
        console.error("文件上传失败:", error);
    });

    return false; // 阻止默认上传行为
}

// 分类相关
const categories = reactive<Category[]>([])
const selectedCategory = ref<string | null>(null);

// 获取分类信息
onMounted(() => {
    listCategory().then((response: Category[]) => {
        categories.length = 0
        response.forEach((item: Category) => {
            categories.push({
                sno: item.sno,
                cateName: item.cateName,
            })
        })
        if (response.length > 0) {
            selectedCategory.value = response[0].sno; // 默认选择第一个分类
        }
    }).catch((error) => {
        console.error("获取分类失败:", error);
    });
});
</script>

<style scoped>
.upload-dragger {
    margin: 30px;
    padding: 30px;
    background: white;
    border-radius: 5px;
}

.title{
    font-size: 21px;
    margin: 0 0 5px 0;
}

.upload-area{
    border-radius: 5px;
}

.category{
    width: 100%;
    float: left;
    display: flex;
    margin: 10px 0 10px 0;
}

.select-label{
    float: left;
    width: 80px;
    margin: 5px 0 0 0;
}

.select-container{
    float: right;
    width: 100%;
}
</style>