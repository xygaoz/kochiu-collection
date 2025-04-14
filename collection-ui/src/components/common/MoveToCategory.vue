<template>
    <el-dialog
        v-model="dialogVisible"
        title="移动到分类"
        width="400px"
        :close-on-click-modal="false"
        align-center
        class="move-category-dialog"
    >
        <el-form label-width="80px">
            <el-form-item label="目标分类" required>
                <el-select
                    v-model="selectedCategoryId"
                    placeholder="请选择分类"
                    filterable
                    clearable
                    style="width: 100%"
                >
                    <el-option
                        v-for="category in categories"
                        :key="category.cateId"
                        :label="category.cateName"
                        :value="category.cateId"
                    />
                </el-select>
            </el-form-item>
        </el-form>

        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button
                type="primary"
                @click="handleConfirm"
                :loading="loading"
                :disabled="!selectedCategoryId"
            >
                确认移动
            </el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getAllCategory } from '@/apis/category-api';
import { moveToCategory } from '@/apis/resource-api';
import type { Category } from '@/apis/interface';

const emit = defineEmits(['success']);

const dialogVisible = ref(false);
const loading = ref(false);
const categories = ref<Category[]>([]);
const selectedCategoryId = ref<string | number | null>(null);
const resourceIds = ref<number[]>([]);

// 打开弹窗
const open = (ids: number[]) => {
    resourceIds.value = ids;
    dialogVisible.value = true;
    selectedCategoryId.value = null;
};

// 加载分类数据
const loadCategories = async () => {
    try {
        categories.value = await getAllCategory();
        if (categories.value.length === 0) {
            ElMessage.warning('暂无可用分类');
        }
    } catch (error) {
        ElMessage.error('加载分类失败');
        console.error(error);
    }
};

// 确认移动
const handleConfirm = async () => {
    if (!selectedCategoryId.value || !resourceIds.value.length) return;

    try {
        loading.value = true;
        let result = await moveToCategory(resourceIds.value, {
            cateId: selectedCategoryId.value
        });
        if(result) {
            ElMessage.success('移动成功');
            emit('success');
            dialogVisible.value = false;
        }
    } catch (error) {
        ElMessage.error('移动失败');
        console.error(error);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadCategories();
});

defineExpose({ open });
</script>

<style scoped>
/* 确保对话框垂直居中 */
.move-category-dialog {
    display: flex;
    align-items: center;
    justify-content: center;
}

/* 对话框内容垂直居中 */
.move-category-dialog .el-dialog {
    margin-top: 0 !important;
    top: 50%;
    transform: translateY(-50%);
}

/* 选择框样式调整 */
.el-select {
    width: 100%;
}
</style>