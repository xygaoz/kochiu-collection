<template>
    <div class="strategy-settings">
        <el-card class="setting-card">
            <template #header>
                <div class="card-header">
                    <span>存储策略设置</span>
                </div>
            </template>

            <el-form
                :model="form"
                label-width="120px"
                :rules="rules"
                ref="formRef"
                class="strategy-form"
            >
                <el-form-item label="选择策略" prop="strategyId">
                    <el-select
                        v-model="form.strategyId"
                        placeholder="请选择存储策略"
                        @change="handleStrategyChange"
                        style="width: 100%"
                    >
                        <el-option
                            v-for="item in strategyList"
                            :key="item.strategyId"
                            :label="item.strategyName"
                            :value="item.strategyId"
                        />
                    </el-select>
                </el-form-item>

                <el-form-item label="策略编号" prop="strategyCode">
                    <el-input v-model="form.strategyCode" disabled />
                </el-form-item>

                <el-form-item label="服务地址" prop="serverUrl">
                    <el-input v-model="form.serverUrl" />
                </el-form-item>

                <el-form-item label="访问账号">
                    <el-input v-model="form.username" />
                </el-form-item>

                <el-form-item label="访问密码">
                    <el-input v-model="form.password" type="password" show-password />
                </el-form-item>

                <el-form-item label="其他配置">
                    <el-input
                        v-model="form.otherConfig"
                        type="textarea"
                        :rows="3"
                        placeholder="JSON格式配置"
                    />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" :loading="loading" v-if="userStore.hasPermission('strategy:update')" @click="submitForm">保存设置</el-button>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { checkServerPath, getStrategyList, testServerPath, updateStrategy } from "@/apis/system-api";
import { Strategy } from "@/apis/interface";
import { useUserStore } from "@/utils/global";

interface StrategyForm {
    strategyId: number | null
    strategyCode: string
    strategyName: string
    serverUrl: string
    username: string
    password: string
    otherConfig: string
}

const loading = ref(false);
const strategyList = ref<Strategy[]>([])
const formRef = ref<FormInstance>()
const form = ref<StrategyForm>({
    strategyId: null,
    strategyCode: '',
    strategyName: '',
    serverUrl: '',
    username: '',
    password: '',
    otherConfig: ''
})
const oldServiceUrl = ref('')
const userStore = useUserStore();

const rules = ref<FormRules>({
    strategyId: [
        { required: true, message: '请选择存储策略', trigger: 'change' }
    ],
    serverUrl: [
        { required: true, message: '请输入服务地址', trigger: 'blur' },
        { min: 5, max: 300, message: '长度在 5 到 300 个字符', trigger: 'blur' }
    ]
})

// 加载策略列表
const loadStrategies = async () => {
    try {
        const data: Strategy[] = await getStrategyList()
        strategyList.value = data
        if (data.length > 0) {
            form.value.strategyId = data[0].strategyId  // 这里赋值数字类型
            loadStrategyData(data[0].strategyId)
        }
    } catch (error) {
        ElMessage.error('加载策略列表失败')
        console.error(error)
    }
}


// 加载策略数据
const loadStrategyData = (strategyId: number) => {
    const strategy = strategyList.value.find(item => item.strategyId === strategyId)
    if (strategy) {
        form.value = {
            strategyId: strategy.strategyId,
            strategyCode: strategy.strategyCode,
            strategyName: strategy.strategyName,
            serverUrl: strategy.serverUrl,
            username: strategy.username,
            password: strategy.password,
            otherConfig: strategy.otherConfig
        }
        oldServiceUrl.value = strategy.serverUrl
    }
}

// 策略切换
const handleStrategyChange = (strategyId: number) => {
    loadStrategyData(strategyId)
}

// 提交表单
const submitForm = async () => {
    if (!formRef.value) return
    loading.value = true
    try {
        await formRef.value.validate()
        if (form.value.serverUrl !== oldServiceUrl.value && form.value.strategyCode === 'local') {
            const result = await testServerPath(form.value.serverUrl, 2)
            if(!result) {
                ElMessage.error('路径不可访问或无法创建')
                return
            }

            if (await checkServerPath()) {
                ElMessageBox.confirm('您旧的存储路径不是空的，保存后将迁移文件到新路径，但迁移有可能会失败，是否继续？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(async () => {
                    if (await updateStrategy(form.value)) {
                        ElMessage.success('保存成功')
                        await loadStrategies() // 刷新列表数据
                    }
                })
            }
            else{
                if(await updateStrategy(form.value)) {
                    ElMessage.success('保存成功')
                    await loadStrategies() // 刷新列表数据
                }
            }
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('保存失败')
    }finally {
        loading.value = false
    }
}

onMounted(() => {
    loadStrategies()
})
</script>

<style scoped>
.strategy-settings {
    padding: 20px;
}

.setting-card {
    margin: 0 auto;
}

.card-header {
    font-size: 18px;
    font-weight: bold;
}

.strategy-form {
    margin-top: 20px;
}
</style>