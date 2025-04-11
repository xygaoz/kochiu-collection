<template>
    <el-dialog
        v-model="dialogVisible"
        :title="isEditMode ? '修改分类' : '添加分类'"
        width="400px"
        :before-close="handleClose"
    >
        <el-form
            ref="formRef"
            :model="form"
            label-width="80px"
            :rules="rules"
        >
            <el-form-item label="分类编号" prop="sno" v-if="!isEditMode">
                <el-text
                    v-model="form.sno"
                />
            </el-form-item>
            <el-form-item label="分类名称" prop="cateName">
                <el-input
                    v-model="form.cateName"
                    placeholder="请输入分类名称"
                    clearable
                />
            </el-form-item>
        </el-form>

        <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="submitForm"
            :loading="loading"
        >
          确认
        </el-button>
      </span>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, defineEmits, defineExpose } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createCategory, updateCategory } from '@/apis/category-api'

const emit = defineEmits(['confirm'])

const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()
const isEditMode = ref(false)

const form = reactive({
    cateName: '',
    sno: 1,
    cateId: 0
})

const rules = reactive<FormRules>({
    cateName: [
        { required: true, message: '请输入分类名称', trigger: 'blur' },
        { min: 1, max: 20, message: '长度在1到20个字符', trigger: 'blur' }
    ],
})

// 打开对话框
const open = (category?: any) => {
    resetForm()

    if (category) {
        // 编辑模式
        isEditMode.value = true
        form.cateName = category.cateName || category.meta?.title || ''
        form.cateId = category.cateId || category.meta?.cateId || ''
    } else {
        // 添加模式
        isEditMode.value = false
    }

    dialogVisible.value = true
    nextTick(() => {
        formRef.value?.clearValidate()
    })
}

// 重置表单
const resetForm = () => {
    form.cateName = ''
    form.sno = 1
    form.cateId = 0
}

// 关闭对话框
const handleClose = () => {
    dialogVisible.value = false
}

// 提交表单
const submitForm = async () => {
    try {
        const valid = await formRef.value?.validate()
        if (!valid) return

        loading.value = true

        const params = {
            cateName: form.cateName,
            sno: form.sno
        }

        if (isEditMode.value) {
            await updateCategory({
                cateId: form.cateId,
                ...params
            })
        } else {
            await createCategory(params)
        }

        emit('confirm')
        dialogVisible.value = false
    } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error(isEditMode.value ? '修改分类失败' : '添加分类失败')
    } finally {
        loading.value = false
    }
}

defineExpose({ open })
</script>

<style scoped>
.dialog-footer button:first-child {
    margin-right: 10px;
}
</style>