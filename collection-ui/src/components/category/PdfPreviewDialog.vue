<template>
    <el-dialog
        :model-value="modelValue"
        :title="title"
        :width="width"
        :top="top"
        :destroy-on-close="destroyOnClose"
        @update:model-value="$emit('update:modelValue', $event)"
    >
        <iframe
            v-if="modelValue"
            :src="iframeSrc"
            class="pdf-iframe"
            frameborder="0"
        />
    </el-dialog>
</template>

<script>
import { computed } from "vue";

export default {
    props: {
        modelValue: {
            type: Boolean,
            required: true
        },
        url: {
            type: String,
            required: true
        },
        title: {
            type: String,
            default: '文档预览'
        },
        width: {
            type: String,
            default: '80%'
        },
        top: {
            type: String,
            default: '5vh'
        },
        destroyOnClose: {
            type: Boolean,
            default: true
        }
    },
    emits: ['update:modelValue'],
    setup(props) {
        const iframeSrc = computed(() => {
            return `/pdfjs/web/viewer.html?file=${encodeURIComponent(props.url)}`
        })

        return {
            iframeSrc
        }
    }
}
</script>

<style scoped>
.pdf-iframe {
    width: 100%;
    height: 70vh;
    min-height: 500px;
    border: none;
    background-color: #f5f5f5;
}

/* 移动端适配 */
@media (max-width: 768px) {
    .pdf-iframe {
        height: 60vh;
        min-height: 300px;
    }
}
</style>