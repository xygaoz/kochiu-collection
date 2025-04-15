<template>
    <div class="upload-dragger">
        <div class="title">文件上传</div>
        <!-- 添加分类选择下拉框 -->
        <div class="category">
            <div class="select-label">选择分类</div>
            <div class="select-container">
                <el-select v-model="selectedCategory" placeholder="请选择分类">
                    <el-option
                        v-for="category in categories"
                        :key="category.sno"
                        :label="category.cateName"
                        :value="category.sno"
                    />
                </el-select>
            </div>
        </div>
        <div class="overwrite">
            <div class="select-label">覆盖</div>
            <div class="select-container">
                <el-radio-group v-model="overwrite">
                    <el-radio :label="true">是</el-radio>
                    <el-radio :label="false">否</el-radio>
                </el-radio-group>
            </div>
        </div>

        <el-upload
            class="upload-area"
            drag
            action="#"
            multiple
            :auto-upload="false"
            :on-change="handleFileChange"
            :show-file-list="false"
        >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
                拖拽文件到这里，或点击<em>这里</em>上传，支持多文件同时上传
            </div>
            <template #tip>
                <div class="el-upload__tip">
                    支持格式：图片（JPG/PNG/GIF等）、文档（PDF/DOCX等）、视频（MP4/AVI等）、音频（MP3/WAV等，单文件最大 100MB
                </div>
            </template>
        </el-upload>

        <div class="upload-actions">
            <el-button type="primary" @click="uploadFiles" :loading="uploading" :disabled="files.length === 0 || !selectedCategory">
                开始上传
            </el-button>
            <el-button @click="clearFiles" :disabled="uploading || files.length === 0">
                清空列表
            </el-button>
        </div>
        <!-- 文件预览区域 -->
        <div class="file-preview-container" v-if="files.length > 0">
            <div class="file-preview-item" v-for="(file, index) in files" :key="index">
                <div class="preview-content">
                    <div class="preview-image" v-if="isImage(file.raw)">
                        <img :src="getImagePreview(file.raw)" alt="Preview" />
                    </div>
                    <div class="preview-icon" v-else>
                        <i :class="`iconfont ${getFileIcon(file.raw)}`"
                           :style="getFileIconStyle(file.raw)"></i>
                    </div>
                    <div class="file-info">
                        <div class="file-name">{{ file.name }}</div>
                        <div class="file-size">{{ formatFileSize(file.size) }}</div>
                    </div>
                </div>
                <el-progress
                    :percentage="file.progress || 0"
                    :status="file.status"
                    v-if="file.progress !== undefined"
                />
                <div class="file-actions">
                    <el-button size="small" type="danger" @click="removeFile(index)" :disabled="uploading">
                        删除
                    </el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from "element-plus";
import { ref, onMounted, reactive } from "vue";
import { uploadFile } from "@/apis/resource-api";
import { Category } from "@/apis/interface";
import { getAllCategory } from "@/apis/category-api";
import { AxiosProgressEvent } from "axios";

interface UploadFileItem {
    name: string;
    size: number;
    raw: File;
    progress?: number;
    status?: 'success' | 'exception' | undefined;
}

// 定义允许的文件类型和最大文件大小（100MB）
const allowedTypes = [
    'image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp', 'image/vnd.adobe.photoshop',
    'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'application/pdf', 'text/plain', 'video/mp4', 'video/avi', 'video/wmv', 'video/quicktime',
    'video/x-matroska', 'audio/mpeg', 'audio/wav', 'audio/flac'
];
const maxSize = 100 * 1024 * 1024; // 100MB in bytes

// 分类相关
const categories = reactive<Category[]>([])
const selectedCategory = ref<number | null>(null);
const overwrite = ref<boolean>(false);
const files = reactive<UploadFileItem[]>([]);
const uploading = ref(false);

// 处理文件选择
const handleFileChange = (file: any, fileList: any[]) => {
    const isAllowedType = allowedTypes.includes(file.raw.type);
    if (!isAllowedType) {
        ElMessage.error(`文件类型 ${file.raw.type} 不支持`);
        return false;
    }

    const isLtMaxSize = file.size <= maxSize;
    if (!isLtMaxSize) {
        ElMessage.error(`文件大小不能超过 100MB`);
        return false;
    }

    // 检查是否已存在同名文件
    if (files.some(f => f.name === file.name)) {
        ElMessage.warning(`文件 ${file.name} 已存在`);
        return false;
    }

    files.push({
        name: file.name,
        size: file.size,
        raw: file.raw
    });

    return false;
};

// 判断是否是图片
const isImage = (file: File) => {
    return file.type.startsWith('image/');
};

// 获取图片预览
const getImagePreview = (file: File) => {
    return URL.createObjectURL(file);
};

// 获取文件图标
const getFileIcon = (file: File) => {
    if (file.type.startsWith('image/')) return 'icon-col-picture';
    if (file.type.startsWith('video/')) return 'icon-col-video-camera';
    if (file.type.startsWith('audio/')) return 'icon-col-audio1';
    if (file.type.includes('pdf')) return 'icon-col-ppt-full';
    if (file.type.includes('txt')) return 'icon-col-txt-full';
    if (file.type.includes('word') || file.type.includes('officedocument.wordprocessingml.document')) return 'icon-col-Word';
    if (file.type.includes('excel') || file.type.includes('officedocument.spreadsheetml.sheet')) return 'icon-col-Excel';
    if (file.type.includes('powerpoint') || file.type.includes('officedocument.presentationml.presentation')) return 'icon-col-ppt-full';
    return 'icon-col-wendang'
};

// 获取文件图标颜色
const getFileIconStyle = (file: File) => {
    if (file.type.startsWith('image/')) return 'color: #ff7d7d';
    if (file.type.startsWith('video/')) return 'color: #7d7dff';
    if (file.type.startsWith('audio/')) return 'color: #7dff7d';
    if (file.type.includes('pdf')) return 'color: #ff7d7d';
    if (file.type.includes('word')) return 'color: #2b579a';
    if (file.type.includes('excel')) return 'color: #217346';
    if (file.type.includes('powerpoint')) return 'color: #d24726';
    return 'color: #999';
};

// 格式化文件大小
const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 删除文件
const removeFile = (index: number) => {
    files.splice(index, 1);
};

// 清空文件列表
const clearFiles = () => {
    files.length = 0;
};

// 上传文件
const uploadFiles = async () => {
    if (!selectedCategory.value) {
        ElMessage.warning('请选择分类');
        return;
    }

    uploading.value = true;

    for (const file of files) {
        try {
            file.progress = 0;
            file.status = undefined;

            await uploadFile(
                file.raw,
                selectedCategory.value,
                overwrite.value.toString(),
                (progressEvent: AxiosProgressEvent) => {
                    // 添加对 total 的检查
                    if (progressEvent.lengthComputable && progressEvent.total) {
                        file.progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                    }
                }
            );

            file.progress = 100;
            file.status = 'success';
        } catch (error) {
            console.error("文件上传失败:", error);
            file.status = 'exception';
            ElMessage.error(`文件 ${file.name} 上传失败`);
        }
    }

    uploading.value = false;
    ElMessage.success('所有文件上传完成');
};

// 获取分类信息
onMounted(() => {
    getAllCategory().then((response: Category[]) => {
        categories.length = 0;
        response.forEach((item: Category) => {
            categories.push({
                cateId: item.cateId,
                sno: item.sno,
                cateName: item.cateName,
            });
        });
        if (response.length > 0) {
            selectedCategory.value = response[0].cateId; // 默认选择第一个分类
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

.title {
    font-size: 21px;
    margin: 0 0 5px 0;
}

.upload-area {
    border-radius: 5px;
}

.category {
    width: 100%;
    float: left;
    display: flex;
    margin: 10px 0 10px 0;
}

.overwrite {
    width: 100%;
    margin: 10px 0 10px 0;
    float: left;
    display: flex;
}

.select-label {
    float: left;
    width: 80px;
    margin: 5px 0 0 0;
}

.select-container {
    float: right;
    width: 100%;
}

.file-preview-container {
    margin-top: 20px;
    border-top: 1px solid #eee;
    padding-top: 20px;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 15px;
    width: 100%;
}

.file-preview-item {
    padding: 15px;
    border: 1px solid #eee;
    border-radius: 5px;
    background: #f9f9f9;
    display: flex;
    flex-direction: column;
    min-height: 180px;
}

.preview-content {
    display: flex;
    flex-direction: column;
    margin-bottom: 10px;
    flex-grow: 1;
}

.preview-image, .preview-icon {
    width: 100%;
    height: 120px;
    margin-right: 0;
    margin-bottom: 10px;
    border: 1px solid #f2f2f2;
}

.preview-image img {
    width: 100%;
    height: 100%;
    object-fit: contain;
}

.preview-icon {
    width: 100px;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.preview-icon .iconfont {
    font-size: 80px; /* 比容器稍小 */
    line-height: 1;
    display: inline-block;
    transform: scale(1); /* 确保不缩放 */
}

.file-info {
    flex: 1;
}

.file-name {
    font-size: 14px;
    margin-bottom: 5px;
    word-break: break-all;
}

.file-size {
    color: #888;
    font-size: 12px;
}

.file-actions {
    margin-top: 10px;
    text-align: right;
}

.upload-actions {
    margin-top: 20px;
    text-align: right;
}
</style>