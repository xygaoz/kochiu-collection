<template>
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="400px"
        :before-close="handleClose"
    >
        <el-form
            ref="formRef"
            :model="form"
            label-width="100px"
            :rules="isDeleteMode ? {} : rules"
        >
            <template v-if="!isDeleteMode">
                <el-form-item label="分类ID" prop="cateId">
                    <span>{{ form.cateId }}</span>
                </el-form-item>
                <el-form-item label="分类名称" prop="cateName">
                    <el-input
                        v-model="form.cateName"
                        placeholder="请输入分类名称"
                        clearable
                    />
                </el-form-item>
            </template>

            <template v-else>
                <el-form-item label="资源移动">
                    <el-radio-group v-model="form.removeType">
                        <el-radio value="1">移动到其他分类</el-radio>
                        <el-radio value="2">删除分类下所有资源</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="移动到分类" prop="targetCateId" v-if="form.removeType === '1'">
                    <el-select
                        v-model="form.targetCateId"
                        placeholder="请选择目标分类"
                        clearable
                    >
                        <el-option
                            v-for="category in categories"
                            :key="category.cateId"
                            :label="category.cateName"
                            :value="category.cateId"
                        />
                    </el-select>
                </el-form-item>
            </template>
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
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import { createCategory, updateCategory, deleteCategory, listCategory } from '@/apis/category-api'
import { Category } from "@/apis/interface";

const emit = defineEmits(['confirm'])

const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()
const isEditMode = ref(false)
const isDeleteMode = ref(false)
const dialogTitle = ref('添加分类')
const categories = ref<any[]>([])

const form = reactive({
    cateName: '',
    cateId: null as number | null,
    removeType: '1',
    targetCateId: null as number | null
})

const rules = reactive<FormRules>({
    cateName: [
        { required: true, message: '请输入分类名称', trigger: 'blur' },
        { min: 1, max: 20, message: '长度在1到20个字符', trigger: 'blur' }
    ],
    targetCateId: [
        { required: true, message: '请选择目标分类', trigger: 'change' }
    ]
})

// 打开对话框
const open = async (category: Category) => {
    isDeleteMode.value = false
    isEditMode.value = !!category
    dialogTitle.value = isEditMode.value ? '修改分类' : '添加分类'

    resetForm()
    await loadCategories()

    if (category) {
        form.cateName = category.cateName || ''
        form.cateId = category.cateId || 0
    }
    else{
        form.cateName = ''
        form.cateId = null
    }

    dialogVisible.value = true
    await nextTick(() => {
        if (formRef.value) {
            formRef.value.clearValidate()
        }
    })
}

// 打开删除对话框
const openForDelete = async (category: Category) => {
    isDeleteMode.value = true
    isEditMode.value = false
    dialogTitle.value = `删除分类【${category.cateName}】`

    resetForm()
    await loadCategories()

    form.cateId = category.cateId || 0
    form.cateName = category.cateName || ''

    dialogVisible.value = true
    await nextTick(() => {
        if (formRef.value) {
            formRef.value.clearValidate()
        }
    })
}

// 加载分类列表
const loadCategories = async () => {
    try {
        categories.value = await listCategory()
    } catch (error) {
        console.error('加载分类列表失败:', error)
    }
}

// 重置表单
const resetForm = () => {
    form.cateName = ''
    form.cateId = 0
    form.removeType = '1'
    form.targetCateId = null
}

// 关闭对话框
const handleClose = () => {
    dialogVisible.value = false
}

// 提交表单
const submitForm = async () => {
    try {
        if (isDeleteMode.value) {
            if (form.removeType === '1' && !form.targetCateId) {
                ElMessage.error('请选择目标分类')
                return
            }

            const valid = await formRef.value?.validate()
            if (!valid) return

            ElMessageBox.confirm(
                form.removeType === '1' ? '您移动这些资源吗？' : '删除分类将永久删除分类下所有资源，不能恢复！！',
                '警告',
                {
                    confirmButtonText: '确认',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            ).then(async () => {
                loading.value = true
                if(await deleteCategory({
                    cateId: form.cateId,
                    removeType: form.removeType,
                    targetCateId: form.targetCateId
                })){
                    emit('confirm')
                    dialogVisible.value = false
                }
                loading.value = false
            });

        } else {
            const valid = await formRef.value?.validate()
            if (!valid) return

            loading.value = true

            const params = {
                cateId: form.cateId,
                cateName: form.cateName,
            }

            let result;
            if (isEditMode.value) {
                result = await updateCategory(params)
            } else {
                result = await createCategory(params)
            }

            if(result) {
                emit('confirm')
                dialogVisible.value = false
            }
        }
    } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error(
            isDeleteMode.value ? '删除分类失败' :
                isEditMode.value ? '修改分类失败' : '添加分类失败'
        )
    } finally {
        loading.value = false
    }
}

defineExpose({ open, openForDelete })
</script>