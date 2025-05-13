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
        </el-card>

        <!-- 清空系统确认对话框 -->
        <el-dialog
            v-model="clearDialogVisible"
            title="警告"
            width="30%"
            :close-on-click-modal="false">
            <span>此操作将永久清空所有系统数据，且不可恢复！确认继续吗？</span>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="clearDialogVisible = false">取消</el-button>
                    <el-button type="danger" @click="clearSystemData">确认清空</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    clearAllData,
    resetRSAKeys,
    resetEncryptKey, loadCurrentKeys
} from "@/apis/system-api";
import { logout } from "@/apis/user-api";
import emitter from "@/utils/event-bus";
import { useUserStore } from "@/utils/global";

const userStore = useUserStore();

// 加载状态
const clearLoading = ref(false)
const rsaLoading = ref(false)
const keyLoading = ref(false)

// 对话框控制
const clearDialogVisible = ref(false)

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
        if(await clearAllData()) {
            ElMessage.success('系统数据已清空')
            clearDialogVisible.value = false
            // 触发数据刷新事件
            emitter.emit('refresh-data')
        }
    } catch (error) {
        ElMessage.error('清空系统失败')
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

onMounted(async () => {
    const result = await loadCurrentKeys();
    if(result){
        currentPublicKey.value = result.publicKey
        currentPrivateKey.value = result.privateKey
        currentEncryptKey.value = result.commonKey
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
</style>