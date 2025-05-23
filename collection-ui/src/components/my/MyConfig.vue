<template>
    <div class="user-config-container">
        <el-card shadow="hover" class="config-card">
            <template #header>
                <div class="card-header">
                    <span>个性配置</span>
                </div>
            </template>

            <el-form
                ref="configForm"
                :model="formData"
                label-width="180px"
                label-position="right"
                :rules="rules"
            >
                <el-form-item label="首页展示的分类数量" prop="listCategoryNum">
                    <el-input-number class="left-align-input-number"
                        v-model="formData.listCategoryNum"
                        :min="1"
                        :max="20"
                        controls-position="right"
                    />
                </el-form-item>

                <el-form-item label="分类排序方式" prop="listCategoryBy">
                    <el-select v-model="formData.listCategoryBy" placeholder="请选择分类排序方式">
                        <el-option
                            v-for="item in categoryByOptions"
                            :key="item.code"
                            :label="item.desc"
                            :value="item.code"
                        />
                    </el-select>
                </el-form-item>

                <el-form-item label="资源分页大小" prop="resourcePageSize">
                    <el-input-number class="left-align-input-number"
                        v-model="formData.resourcePageSize"
                        :min="10"
                        :max="200"
                        controls-position="right"
                    />
                </el-form-item>

                <el-form-item label="首页展示的标签数量" prop="listTagNum">
                    <el-input-number class="left-align-input-number"
                        v-model="formData.listTagNum"
                        :min="1"
                        :max="20"
                        controls-position="right"
                    />
                </el-form-item>

                <el-form-item label="标签排序方式" prop="listTagBy">
                    <el-select v-model="formData.listTagBy" placeholder="请选择标签排序方式">
                        <el-option
                            v-for="item in tagByOptions"
                            :key="item.code"
                            :label="item.desc"
                            :value="item.code"
                        />
                    </el-select>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="submitForm">保存配置</el-button>
                    <el-button @click="resetForm">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getMyConfig, setMyConfig } from '@/apis/user-api'
import type { UserProperty } from '@/apis/interface'
import emitter from "@/utils/event-bus";

// 表单数据
const formData = ref<UserProperty>({
    listCategoryNum: 5,
    listCategoryBy: 1, // CREATE_TIME_ABS
    resourcePageSize: 100,
    listTagNum: 5,
    listTagBy: 1, // CREATE_TIME_ABS
})

// 表单引用
const configForm = ref<FormInstance>()

// 表单验证规则
const rules = ref<FormRules<UserProperty>>({
    listCategoryNum: [
        { required: true, message: '请输入分类数量', trigger: 'blur' },
        { type: 'number', min: 1, max: 20, message: '范围在1-20之间', trigger: 'blur' }
    ],
    listCategoryBy: [
        { required: true, message: '请选择分类排序方式', trigger: 'change' }
    ],
    resourcePageSize: [
        { required: true, message: '请输入分页大小', trigger: 'blur' },
        { type: 'number', min: 10, max: 200, message: '范围在10-200之间', trigger: 'blur' }
    ],
    listTagNum: [
        { required: true, message: '请输入标签数量', trigger: 'blur' },
        { type: 'number', min: 1, max: 20, message: '范围在1-20之间', trigger: 'blur' }
    ],
    listTagBy: [
        { required: true, message: '请选择标签排序方式', trigger: 'change' }
    ]
})

// 排序选项
const categoryByOptions = [
    { code: 1, desc: '创建时间顺序' },
    { code: 2, desc: '创建时间倒序' },
    { code: 3, desc: '资源数量倒叙' }
]

const tagByOptions = [
    { code: 1, desc: '创建时间顺序' },
    { code: 2, desc: '创建时间倒序' },
    { code: 3, desc: '资源数量倒叙' }
]

// 加载用户配置
const loadUserConfig = async () => {
    try {
        const config = await getMyConfig()
        if (config) {
            formData.value = {
                listCategoryNum: config.listCategoryNum,
                listCategoryBy: config.listCategoryBy,
                resourcePageSize: config.resourcePageSize,
                listTagNum: config.listTagNum,
                listTagBy: config.listTagBy,
            }
        }
    } catch (error) {
        ElMessage.error('加载用户配置失败')
        console.error('加载用户配置失败:', error)
    }
}

// 提交表单
const submitForm = async () => {
    try {
        const valid = await configForm.value?.validate()
        if (valid) {
            const success = await setMyConfig(formData.value)
            if (success) {
                ElMessage.success('配置保存成功')
                // 触发数据刷新事件
                emitter.emit('refresh-menu')
            } else {
                ElMessage.error('配置保存失败')
            }
        }
    } catch (error) {
        console.error('保存配置失败:', error)
        ElMessage.error('配置保存失败')
    }
}

// 重置表单
const resetForm = () => {
    ElMessageBox.confirm('确定要重置所有配置吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        configForm.value?.resetFields()
        ElMessage.success('重置成功')
    }).catch(() => {
        // 取消操作
    })
}

// 初始化加载配置
onMounted(() => {
    loadUserConfig()
})
</script>

<style scoped>
.user-config-container {
    padding: 20px;
}

.config-card {
    margin: 0 auto;
}

.card-header {
    font-size: 18px;
    font-weight: bold;
}

.left-align-input-number {
    width: 100%;
}

.left-align-input-number :deep(.el-input__inner) {
    text-align: left;
}
</style>