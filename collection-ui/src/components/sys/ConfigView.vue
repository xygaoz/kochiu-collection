<template>
    <div class="config-view">
        <el-card class="config-card" shadow="hover">
            <!-- 清空系统功能 -->
            <div class="config-section" v-if="userStore.hasPermission('config:clear')">
                <h3>系统清理</h3>
                <p class="config-description">此操作将清空所有业务数据（用户数据、配置数据等），可在试用阶段使用，请谨慎操作！</p>
                <el-button
                    type="danger"
                    @click="confirmClearSystem"
                    :loading="clearLoading">
                    清空系统数据
                </el-button>
            </div>

            <!-- RSA密钥重置 -->
            <div class="config-section">
                <h3>RSA密钥管理</h3>
                <p class="config-description">RSA密钥影响登录生成的token，如果密钥泄露或第一次使用系统时，需要重置，请谨慎操作！</p>
                <p class="config-description">当前公钥：</p>
                <el-input
                    v-model="currentPublicKey"
                    type="textarea"
                    :rows="4"
                    readonly
                    class="key-display"
                    placeholder="加载中..."/>
                <p class="config-description">当前私钥（安全隐藏）：</p>
                <el-input
                    v-model="currentPrivateKey"
                    type="textarea"
                    :rows="3"
                    readonly
                    placeholder="安全敏感信息"/>
                <el-button
                    type="warning"
                    v-if="userStore.hasPermission('config:reset-rsa-keys')"
                    @click="confirmResetRSA"
                    :loading="rsaLoading"
                    style="margin-top: 15px">
                    重置RSA密钥对
                </el-button>
            </div>

            <!-- 公共加密Key重置 -->
            <div class="config-section">
                <h3>公共加密Key</h3>
                <p class="config-description">公共加密Key影响登录生成的token，如果密钥泄露或第一次使用系统时，需要重置，请谨慎操作！</p>
                <p class="config-description">当前Key（安全隐藏）：</p>
                <el-input
                    v-model="currentEncryptKey"
                    type="text"
                    readonly
                    placeholder="安全敏感信息"/>
                <el-button
                    type="warning"
                    v-if="userStore.hasPermission('config:reset-key')"
                    @click="confirmResetEncryptKey"
                    :loading="keyLoading"
                    style="margin-top: 15px">
                    重置公共加密Key
                </el-button>
            </div>
            <div class="config-section">
                <h3>其他配置</h3>
                <el-form
                    ref="configForm"
                    :model="formData"
                    label-width="140px"
                    label-position="right"
                    :rules="rules"
                >
                    <el-form-item label="上传文件最大限制" prop="uploadMaxSize">
                        <div class="upload-size-container">
                            <el-input-number
                                v-model="sizeValue"
                                :min="1"
                                :max="maxSizeLimit"
                                controls-position="right"
                                class="size-input"
                            />
                            <el-select
                                v-model="sizeUnit"
                                placeholder="选择单位"
                                class="unit-select"
                            >
                                <el-option label="MB" value="MB" />
                                <el-option label="GB" value="GB" />
                                <el-option label="TB" value="TB" />
                            </el-select>
                        </div>
                    </el-form-item>

                    <el-form-item
                        v-if="userStore.hasPermission('config:set-sys-config')"
                    >
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
                    <el-button type="danger" @click="clearSystemData" :loading="clearLoading">确认清空</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { ElMessage, ElMessageBox, FormInstance, FormRules } from "element-plus";
import {
    clearAllData,
    resetRSAKeys,
    resetEncryptKey,
    loadCurrentKeys,
    setSysConfig,
    getSysConfig, getPublicKey
} from "@/apis/system-api";
import { logout } from "@/apis/user-api";
import emitter from "@/utils/event-bus";
import { useUserStore } from "@/utils/global";
import { SysProperty } from "@/apis/interface";
import { encryptPassword } from "@/utils/utils";

const userStore = useUserStore();

// 加载状态
const clearLoading = ref(false)
const rsaLoading = ref(false)
const keyLoading = ref(false)

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

// 表单数据
const formData = ref<SysProperty>({
    uploadMaxSize: '500MB',
})
const sizeValue = ref(500);
const sizeUnit = ref('MB');
const maxSizeLimit = ref(10240); // 最大限制值
const publicKey = ref<string | null>(null); // 用于存储公钥

// 表单引用
const configForm = ref<FormInstance>()
// 表单验证规则
const rules = ref<FormRules<SysProperty>>({
    uploadMaxSize: [
        { required: true, message: '请输入上传文件最大限制', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                if (!value) {
                    callback(new Error('请输入上传文件最大限制'));
                    return;
                }

                // 允许大小写（MB/mb/Gb/tB等）
                const regex = /^\d+(\.\d+)?\s*(MB|GB|TB)$/i;
                if (!regex.test(value)) {
                    callback(new Error('格式不正确，请使用 MB/GB/TB 单位（例如：500MB 或 1GB）'));
                } else {
                    callback();
                }
            },
            trigger: ['blur', 'change'] // 同时监听输入和变化
        }
    ],
})

// 密钥数据
const currentPublicKey = ref('')
const currentPrivateKey = ref('')
const currentEncryptKey = ref('')

// 清空系统确认
const confirmClearSystem = () => {
    clearDialogVisible.value = true
}

// 执行清空系统
const clearSystemData = async () => {
    clearLoading.value = true
    try {
        // 验证表单
        await clearFormRef.value?.validate()
        const encryptedPassword: string | undefined = encryptPassword(publicKey.value!, clearForm.value.password);

        if(await clearAllData(encryptedPassword)) {
            ElMessage.success('系统数据已清空')
            clearForm.value.password =  ''
            clearDialogVisible.value = false
            // 触发数据刷新事件
            emitter.emit('refresh-menu')
        }
    } catch (error) {
        ElMessage.error('清空系统数据失败')
        console.error(error)
    } finally {
        clearLoading.value = false
    }
}

// 重置RSA密钥
const confirmResetRSA = async () => {
    try {
        ElMessageBox.confirm(
            '重置RSA密钥将需要重新登录，确认继续吗？',
            '警告',
            {
                confirmButtonText: '确认重置',
                cancelButtonText: '取消',
                type: 'warning'
            }
        ).then(async () => {

            rsaLoading.value = true
            if(await resetRSAKeys()){
                await ElMessageBox.alert('RSA密钥已重置，即将退出系统', '提示', {
                    confirmButtonText: '确定',
                    type: 'warning',
                    callback: () => {
                        logout()
                    }
                })
            }
        });
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('重置RSA密钥失败')
            console.error(error)
        }
    } finally {
        rsaLoading.value = false
    }
}

// 重置加密Key
const confirmResetEncryptKey = async () => {
    try {
        ElMessageBox.confirm(
            '重置加密Key将需要重新登录，确认继续吗？',
            '警告',
            {
                confirmButtonText: '确认重置',
                cancelButtonText: '取消',
                type: 'warning'
            }
        ).then(async () => {
            keyLoading.value = true
            if(await resetEncryptKey()){
                await ElMessageBox.alert('加密Key已重置，即将退出系统', '提示', {
                    confirmButtonText: '确定',
                    type: 'warning',
                    callback: () => {
                        logout()
                    }
                })
            }
        })
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('重置加密Key失败')
            console.error(error)
        }
    } finally {
        keyLoading.value = false
    }
}

// 提交表单
const submitForm = async () => {
    try {
        const valid = await configForm.value?.validate()
        if (valid) {
            const success = await setSysConfig(formData.value)
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

// 初始化时解析现有值
const parseInitialValue = (value: string) => {
    if (!value) return;
    const match = value.match(/^(\d+)(MB|GB|TB)$/i);
    if (match) {
        sizeValue.value = parseInt(match[1]);
        sizeUnit.value = match[2].toUpperCase();
    }
};

// 监听变化组合成完整字符串
watch([sizeValue, sizeUnit], () => {
    formData.value.uploadMaxSize = `${sizeValue.value}${sizeUnit.value}`;
});

onMounted(async () => {
    try {
        publicKey.value = await getPublicKey();
    } catch (error) {
        console.error("获取公钥失败:", error);
        ElMessage.error("获取公钥失败");
    }

    const result = await loadCurrentKeys();
    if(result){
        currentPublicKey.value = result.publicKey
        currentPrivateKey.value = result.privateKey
        currentEncryptKey.value = result.commonKey
    }
    //加载上传大小配置
    const config = await getSysConfig();
    if (config?.uploadMaxSize) {
        parseInitialValue(config.uploadMaxSize);
    }
})
</script>

<style scoped>
.config-view {
    padding: 20px;
}

.config-card {
    margin: 0 auto;
}

.config-section {
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 1px solid var(--el-border-color);
}

.config-section:last-child {
    border-bottom: none;
}

.config-description {
    color: #666;
    margin: 10px 0;
    font-size: 14px;
}

.key-display {
    font-family: monospace;
    margin-bottom: 15px;
}

.upload-size-container {
    display: flex;
    gap: 10px;
    width: 100%;
}

.size-input {
    width: 100%;
}

.size-input :deep(.el-input__inner) {
    text-align: left;
}

.unit-select {
    width: 100px;
}
</style>