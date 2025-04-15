<template>
    <el-form-item label="标签" prop="tags" class="form-item">
        <el-popover
            placement="bottom-start"
            trigger="click"
            :width="popoverWidth"
            popper-class="tag-selector-popper"
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
import { ref, computed, onMounted, defineProps, defineEmits, watch } from "vue";
import { getAllTag } from '@/apis/tag-api';
import type { Tag } from '@/apis/interface';

const props = defineProps({
    modelValue: {
        type: Array as () => string[],
        default: () => []
    },
    forceClose: { // 新增forceClose属性
        type: Boolean,
        default: false
    }
});
const popoverRef = ref();

const emit = defineEmits(['update:modelValue']);

const allTags = ref<Tag[]>([]);
const popoverWidth = ref(200);

const selectedTags = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
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
        popoverWidth.value = Math.min(600, Math.max(300, allTags.value.length * 100));
    } catch (error) {
        console.error('加载标签失败:', error);
    }
};

onMounted(() => {
    loadTags();
});

watch(() => props.forceClose, (newVal) => {
    if (newVal && popoverRef.value) {
        popoverRef.value.hide(); // 强制关闭popover
    }
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
    background-color: #f5f7fa;
}

.tag-selector-trigger:hover {
    border-color: #c0c4cc;
}

.selected-tag {
    margin-right: 8px;
}

.tag-selector-content {
    max-height: 300px;
    overflow-y: auto;
}

.tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.form-item{
    cursor: pointer;
    background-color: #f5f7fa;
    color: #606266;
}

.form-item :deep(.el-form-item__label) {
    padding: 0 15px 0 0;
}

.tag-item {
    cursor: pointer;
    background-color: #f5f7fa;
    border: 1px dashed #5a5a5a;
    color: #606266;
}

.tag-item.selected {
    background-color: #ecf5ff;
    border: 1px solid #409eff;
    color: #409eff;
}

.tag-select-label{
    //margin: 0 0 0 8px;
    border: 1px solid #e0e0e0;
    padding: 0 6px;
    border-radius: 5px;
}
</style>