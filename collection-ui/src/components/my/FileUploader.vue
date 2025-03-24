<template>
    <div class="upload-dragger">
        <div class="title">文件上传</div>
        <el-upload
            class="upload-area"
            drag
            action="https://run.mocky.io/v3/9d059bf9-4660-45f2-925d-ce80ad6c4d15"
            multiple
        >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
                拖拽文件到这里，或点击<em>这里</em>上传，支持多文件同时上传
            </div>
            <template #tip>
                <div class="el-upload__tip">
                    每个文件大小不超过 2MB，支持上传文件类型：
                    <em>*.jpg、*.jpeg、*.png、*.gif、*.bmp、*.webp、*.psd、*.doc 、*.docx、*.xls、*.xlsx、*.ppt、*.pptx、*.pdf、*.txt、*.mp4、*.avi、*.wmv、*.mov、*.mkv、*.mp3、*.wav、*.flac</em>
                </div>
            </template>
        </el-upload>
    </div>
</template>

<script setup lang="ts">
import { UploadFilled } from '@element-plus/icons-vue'
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
</style>