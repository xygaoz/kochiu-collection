<template>
    <el-form-item label="标签" prop="tags" class="form-item">
        <el-popover
            placement="bottom-start"
            trigger="click"
            :width="popoverWidth"
            popper-class="tag-selector-popper"
            ref="popoverRef"
        >
            <template #reference>
                <el-button size="small" class="tag-selector-trigger">
                    <el-tag
                        v-for="tag in selectedTags"
                        :key="tag"
                        size="small"
                        closable
                        @close="removeTag(tag)"
                        class="selected-tag"
                    >
                        {{ tag }}
                    </el-tag>
                    <span class="tag-select-label">选择标签</span>
                </el-button>
            </template>

            <div class="tag-selector-content">
                <div class="tag-list">
                    <el-tag
                        v-for="tag in allTags"
                        :key="tag.tagId"
                        :class="{ 'tag-item': true, 'selected': isTagSelected(tag.tagName) }"
                        size="small"
                        @click="toggleTag(tag.tagName)"
                    >
                        {{ tag.tagName }}
                    </el-tag>
                </div>
            </div>
        </el-popover>
    </el-form-item>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, defineProps, defineEmits, watch, defineExpose } from "vue";
import { getAllTag } from '@/apis/tag-api';
import type { Tag } from '@/apis/interface';

const props = defineProps({
    modelValue: {
        type: Array as () => string[],
        default: () => []
    },
    forceClose: {
        type: Boolean,
        default: false
    }
});

const popoverRef = ref();
const emit = defineEmits(['update:modelValue', 'change']);

const allTags = ref<Tag[]>([]);
const popoverWidth = ref(200);

const selectedTags = computed({
    get: () => props.modelValue,
    set: (value) => {
        emit('update:modelValue', value);
        emit('change', value);
    }
});

const isTagSelected = (tagName: string) => {
    return selectedTags.value.includes(tagName);
};

const toggleTag = (tagName: string) => {
    const newTags = [...selectedTags.value];
    const index = newTags.indexOf(tagName);

    if (index > -1) {
        newTags.splice(index, 1);
    } else {
        newTags.push(tagName);
    }

    selectedTags.value = newTags;
};

const removeTag = (tagName: string) => {
    selectedTags.value = selectedTags.value.filter(t => t !== tagName);
};

const loadTags = async () => {
    try {
        allTags.value = await getAllTag();
        // 根据标签数量调整下拉框宽度
        popoverWidth.value = Math.min(400, Math.max(300, allTags.value.length * 100));
    } catch (error) {
        console.error('加载标签失败:', error);
    }
};

const closePopover = () => {
    if (popoverRef.value) {
        popoverRef.value.hide();
    }
};

onMounted(() => {
    loadTags();
});

watch(() => props.forceClose, (newVal) => {
    if (newVal) {
        closePopover();
    }
});

defineExpose({
    closePopover
});
</script>

<style scoped>
.tag-selector-trigger {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 0 10px;
    height: 24px;
    line-height: 22px;
    border: 0;
}

.tag-selector-trigger:hover {
    border-color: var(--el-border-color-light);
    background-color: transparent;
}

.selected-tag {
    margin-right: 8px;
}

.tag-selector-content {
    max-height: 300px;
    overflow-y: auto;
    min-height: 100px;
}

.tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.form-item{
    cursor: pointer;
    color: var(--el-text-color-regular);
}

.form-item :deep(.el-form-item__label) {
    padding: 0 15px 0 0;
}

.tag-item {
    cursor: pointer;
    background-color: var(--el-fill-color-light);
    border: 1px dashed var(--el-border-color-light);
    color: var(--el-text-color-regular);
}

.tag-item.selected {
    background-color: var(--el-color-primary-light-9);
    border: 1px solid var(--el-color-primary);
    color: var(--el-color-primary);
}

.tag-select-label{
    border: 1px solid var(--el-border-color-light);
    padding: 0 6px;
    border-radius: 5px;
}
</style>