<template>
    <el-dialog
        :model-value="modelValue"
        :title="title"
        :width="width"
        :top="top"
        :destroy-on-close="destroyOnClose"
        @update:model-value="$emit('update:modelValue', $event)"
    >
        <div ref="pdfContainer" class="pdf-container">
            <div v-if="error" class="error-message">
                <el-icon><CircleCloseFilled /></el-icon>
                <span>预览失败，请<a :href="pdfUrl" download>下载文件</a></span>
            </div>
        </div>
    </el-dialog>
</template>

<script>
import { ref, watch, onMounted } from 'vue'
import PDFObject from 'pdfobject'
import { CircleCloseFilled } from '@element-plus/icons-vue'

export default {
    components: { CircleCloseFilled },
    props: {
        modelValue: { type: Boolean, required: true },
        url: { type: String, required: true },
        title: { type: String, default: '文档预览' },
        width: { type: String, default: '80%' },
        top: { type: String, default: '5vh' },
        destroyOnClose: { type: Boolean, default: true }
    },
    emits: ['update:modelValue'],
    setup(props) {
        const pdfContainer = ref(null)
        const error = ref(false)
        const pdfUrl = ref('')

        // 生成完整的 PDF URL（处理相对路径）
        const getFullPdfUrl = () => {
            return props.url.startsWith('http') ? props.url :
                `${window.location.origin}${props.url}`
        }

        // 初始化 PDF 预览
        const initPdfPreview = () => {
            if (!props.modelValue || !pdfContainer.value) return

            error.value = false
            pdfUrl.value = getFullPdfUrl()

            // 清空容器
            pdfContainer.value.innerHTML = ''

            // 使用 PDFObject 嵌入 PDF
            const success = PDFObject.embed(
                pdfUrl.value,
                pdfContainer.value,
                {
                    height: '70vh',
                    width: '100%',
                    fallbackLink: false, // 禁用默认的 fallback 链接
                    PDFJS_URL: 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.12.313/pdf.min.js' // 明确指定 PDF.js 地址
                }
            )

            if (!success) {
                error.value = true
                console.error('PDF 嵌入失败，请检查文件路径或浏览器兼容性')
            }
        }

        // 监听对话框打开/关闭
        watch(() => props.modelValue, (val) => {
            if (val) {
                // 延迟初始化确保 DOM 已渲染
                setTimeout(initPdfPreview, 50)
            } else {
                error.value = false
            }
        })

        // 组件挂载时初始化（防止首次打开时未触发 watch）
        onMounted(() => {
            if (props.modelValue) initPdfPreview()
        })

        return {
            pdfContainer,
            error,
            pdfUrl
        }
    }
}
</script>

<style scoped>
.pdf-container {
    width: 100%;
    min-height: 70vh;
    position: relative;
}

.error-message {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    align-items: center;
    gap: 8px;
    color: #f56c6c;
}

.error-message a {
    color: #409eff;
    text-decoration: underline;
    margin-left: 5px;
}

@media (max-width: 768px) {
    .pdf-container {
        min-height: 60vh;
    }
}
</style>