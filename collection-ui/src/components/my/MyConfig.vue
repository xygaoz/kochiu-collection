<template>
    <div class="user-config-container">
        <el-card shadow="hover" class="config-card">
            <!-- 清空系统功能 -->
            <div class="config-section">
                <h3>清理数据</h3>
                <p class="config-description">此操作将清空个人所有业务数据（个人数据、配置数据等），可在试用阶段使用，请谨慎操作！</p>
                <el-button
                    type="danger"
                    @click="confirmClearSystem"
                    :loading="clearLoading">
                    清空系统数据
                </el-button>
            </div>

            <div>
                <h3>个性配置</h3>
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
            </div>
        </el-card>

        <!-- 清空系统确认对话框 -->
        <el-dialog
            v-model="clearDialogVisible"
            title="警告"
            width="30%"
            :close-on-click-modal="false">
            <span>此操作将永久清空所有系统数据，且不可恢复！确认继续吗？</span>
            <el-form :model="clearForm" :rules="clearRules" ref="clearFormRef" style="margin-top: 20px;">
                <el-form-item label="您的登录密码" prop="password">
                    <el-input
                        v-model="clearForm.password"
                        type="password"
                        placeholder="请输入您的登录密码"
                        show-password
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="clearDialogVisible = false">取消</el-button>
                    <el-button type="danger" @click="clearData" :loading="clearLoading">确认清空</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getMyConfig, setMyConfig } from '@/apis/user-api'
import type { UserProperty } from '@/apis/interface'
import emitter from "@/utils/event-bus";
import { encryptPassword } from "@/utils/utils";
import { getPublicKey } from "@/apis/system-api";
import { clearMyData } from '@/apis/user-api'

// 表单数据
const formData = ref<UserProperty>({
    listCategoryNum: 5,
    listCategoryBy: 1, // CREATE_TIME_ABS
    resourcePageSize: 100,
    listTagNum: 5,
    listTagBy: 1, // CREATE_TIME_ABS
})

const publicKey = ref<string | null>(null); // 用于存储公钥
// 表单引用
const configForm = ref<FormInstance>()
const clearLoading = ref(false)
// 对话框控制
const clearDialogVisible = ref(false)
// 清空系统表单
const clearForm = ref({
    password: ''
})
const clearFormRef = ref<FormInstance>()
const clearRules = ref({
    password: [
        { required: true, message: '请输入您的登录密码', trigger: 'blur' },
    ]
})

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

// 清空系统确认
const confirmClearSystem = () => {
    clearDialogVisible.value = true
}

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

// 执行清空系统
const clearData = async () => {
    clearLoading.value = true
    try {
        // 验证表单
        await clearFormRef.value?.validate()
        const encryptedPassword: string | undefined = encryptPassword(publicKey.value!, clearForm.value.password);

        if(await clearMyData(encryptedPassword)) {
            ElMessage.success('您的数据已清空')
            clearForm.value.password =  ''
            clearDialogVisible.value = false
            // 触发数据刷新事件
            emitter.emit('refresh-menu')
        }
    } catch (error) {
        ElMessage.error('清空数据失败')
        console.error(error)
    } finally {
        clearLoading.value = false
    }
}

// 初始化加载配置
onMounted(async () => {
    loadUserConfig()
    try {
        publicKey.value = await getPublicKey();
    } catch (error) {
        console.error("获取公钥失败:", error);
        ElMessage.error("获取公钥失败");
    }
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

.config-section {
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 1px solid var(--el-border-color);
}

</style>