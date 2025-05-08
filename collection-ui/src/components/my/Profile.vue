<template>
    <div class="profile-container">
        <el-card shadow="hover">
            <template #header>
                <div class="card-header">
                    <span>个人信息</span>
                </div>
            </template>

            <el-form :model="userInfo" label-width="120px" label-position="right">
                <!-- 用户编码 - 不可编辑 -->
                <el-form-item label="用户编码">
                    <el-input v-model="userInfo.userCode" disabled></el-input>
                </el-form-item>

                <!-- 用户名称 - 可编辑 -->
                <el-form-item label="用户名称">
                    <el-input v-model="userInfo.userName">
                        <template #append>
                            <el-button
                                type="primary"
                                @click="handleEditName"
                            >保存</el-button>
                        </template>
                    </el-input>
                </el-form-item>

                <!-- 策略 - 显示 -->
                <el-form-item label="策略">
                    <el-input v-model="userInfo.strategy" disabled></el-input>
                </el-form-item>

                <!-- Key - 可重置 -->
                <el-form-item label="密钥">
                    <el-input v-model="userInfo.key" disabled>
                        <template #append>
                            <el-button
                                type="primary"
                                @click="confirmReset('key')"
                            >
                                重置
                            </el-button>
                        </template>
                    </el-input>
                    <span class="tip">Token加密密钥，请妥善保管</span>
                </el-form-item>

                <!-- Token - 可重置和复制 -->
                <el-form-item label="Token">
                    <div class="token-container">
                        <el-input
                            type="textarea"
                            v-model="userInfo.token"
                            disabled
                            resize="none"
                            :rows="4"
                        ></el-input>
                        <div class="token-actions">
                            <el-tooltip content="复制Token" placement="top">
                                <el-button
                                    type="primary"
                                    size="small"
                                    circle
                                    @click="copyToken"
                                    :icon="CopyDocument"
                                />
                            </el-tooltip>
                            <el-tooltip content="重置Token" placement="top">
                                <el-button
                                    type="warning"
                                    size="small"
                                    circle
                                    @click="confirmReset('token')"
                                    :icon="Refresh"
                                />
                            </el-tooltip>
                        </div>
                    </div>
                    <span class="tip">第三方客户端调用API使用</span>
                </el-form-item>

                <!-- 状态 - 显示 -->
                <el-form-item label="状态">
                    <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
                        {{ userInfo.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                </el-form-item>

                <!-- 主题切换 -->
                <el-form-item label="界面主题">
                    <el-switch
                        v-model="isDarkMode"
                        active-text="暗黑模式"
                        inactive-text="明亮模式"
                        @change="toggleTheme"
                    />
                </el-form-item>

                <!-- 角色 - 显示标签 -->
                <el-form-item label="角色">
                    <div class="role-tags">
                        <el-tag
                            v-for="role in userInfo.roles"
                            :key="role.roleId"
                            type="info"
                            style="margin-right: 8px; margin-bottom: 8px;"
                        >
                            {{ role.roleName }}
                        </el-tag>
                    </div>
                </el-form-item>
            </el-form>
        </el-card>

        <!-- 重置确认对话框 -->
        <el-dialog
            v-model="resetDialogVisible"
            :title="`确认重置${resetType === 'key' ? 'Key' : 'Token'}?`"
            width="30%"
        >
            <span v-if="resetType === 'key'">重置加密密钥要退出重新登录，确定要继续吗？</span>
            <span v-else>重置Api Token会影响客户端Api调用用，确定要继续吗？</span>
            <template #footer>
                <el-button @click="resetDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="resetItem">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, watch } from "vue";
import { storeToRefs } from 'pinia';
import { ElMessage, ElMessageBox } from "element-plus";
import { Refresh, CopyDocument } from '@element-plus/icons-vue';
import { Strategy, User } from "@/apis/interface";
import { getStrategyList } from "@/apis/system-api";
import { getMyInfo, logout, resetKey, resetToken, setMyName } from "@/apis/user-api";
import { useUserStore } from "@/apis/global";
import { useThemeStore } from "@/apis/themeStore";

const themeStore = useThemeStore();
const { currentTheme, isDark } = storeToRefs(themeStore);

const userInfo = ref<User>({
    userId: '',
    userCode: '',
    userName: '',
    password: '',
    strategy: '',
    key: '',
    token: '',
    status: 0,
    theme: currentTheme.value, // 使用响应式引用
    roles: []
});
const strategyList = ref<Strategy[]>([])
const userStore = useUserStore();

// 重置相关状态
const resetDialogVisible = ref(false);
const resetType = ref<'key' | 'token'>('key');

// 模拟从存储中获取用户信息
const fetchUserInfo = async () => {
    strategyList.value = await getStrategyList();
    await refreshUser();
};

const getStrategyName = (strategyCode: string) => {
    const strategy = strategyList.value.find(s => s.strategyCode === strategyCode);
    return strategy ? strategy.strategyName : strategyCode;
}

// 确保开关状态同步
const isDarkMode = computed(() => isDark.value);

const toggleTheme = async () => {
    const newTheme = currentTheme.value === 'light' ? 'dark' : 'light';
    await themeStore.applyTheme(newTheme);
    userInfo.value.theme = newTheme;
};

// 编辑用户名
const handleEditName = async () => {
    if (!userInfo.value.userName.trim()) {
        ElMessage.error('用户名不能为空');
        return;
    }

    if (await setMyName(userInfo.value.userName)) {
        ElMessage.success('用户名修改成功');
        await refreshUser();
    }
};

const refreshUser = async () => {
    const user = await getMyInfo();
    if(user){
        userStore.setUsername(user.userName)
        userInfo.value = user;
        user.strategy = getStrategyName(user.strategy);
    }
}

// 复制Token
const copyToken = async () => {
    try {
        await navigator.clipboard.writeText(userInfo.value.token);
        ElMessage.success('Token已复制到剪贴板');
    } catch (err) {
        console.error('复制失败:', err);
        ElMessage.error('复制失败');
    }
};

// 确认重置
const confirmReset = (type: 'key' | 'token') => {
    resetType.value = type;
    resetDialogVisible.value = true;
};

// 执行重置
const resetItem = async () => {
    resetDialogVisible.value = false;

    if (resetType.value === 'key') {
        if(await resetKey()) {
            ElMessageBox.alert('Key重置成功，请重新登录', '提示', {
                confirmButtonText: '确定',
                type: 'warning',
                callback: () => {
                    logout()
                }
            })
        }
    } else {
        if(await resetToken()) {
            ElMessage.success('Token重置成功');
            await refreshUser();
        }
    }
};

onMounted(() => {
    fetchUserInfo();
});

</script>

<style scoped>
.profile-container {
    padding: 20px;
}

.card-header {
    font-size: 18px;
    font-weight: bold;
}

.role-tags {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    margin: 4px 0 0 0;
}

.token-container {
    position: relative;
    width: 100%;
}

.token-actions {
    position: absolute;
    right: 10px;
    bottom: 10px;
    display: flex;
    gap: 1px;
}

.tip{
    color: var(--el-text-color-secondary);
}
</style>