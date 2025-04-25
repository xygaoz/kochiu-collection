<template>
    <div class="profile-container">
        <el-card class="profile-card">
            <template #header>
                <div class="card-header">
                    <h2>个人信息</h2>
                </div>
            </template>

            <el-form :model="userInfo" label-width="120px" label-position="left">
                <!-- 用户编码 - 不可编辑 -->
                <el-form-item label="用户编码">
                    <el-input v-model="userInfo.userCode" disabled></el-input>
                </el-form-item>

                <!-- 用户名称 - 可编辑 -->
                <el-form-item label="用户名称">
                    <el-input v-model="userInfo.userName" :disabled="!editingName"></el-input>
                    <el-button
                        type="primary"
                        @click="handleEditName"
                        style="margin-left: 10px;"
                    >
                        {{ editingName ? '保存' : '修改' }}
                    </el-button>
                    <el-button
                        v-if="editingName"
                        @click="cancelEditName"
                        style="margin-left: 10px;"
                    >
                        取消
                    </el-button>
                </el-form-item>

                <!-- 策略 - 显示 -->
                <el-form-item label="策略">
                    <el-input v-model="userInfo.strategy" disabled></el-input>
                </el-form-item>

                <!-- Key - 可重置 -->
                <el-form-item label="Key">
                    <el-input v-model="userInfo.key" disabled></el-input>
                    <el-button
                        type="warning"
                        @click="confirmReset('key')"
                        style="margin-left: 10px;"
                    >
                        重置Key
                    </el-button>
                </el-form-item>

                <!-- Token - 可重置 -->
                <el-form-item label="Token">
                    <el-input v-model="userInfo.token" disabled></el-input>
                    <el-button
                        type="warning"
                        @click="confirmReset('token')"
                        style="margin-left: 10px;"
                    >
                        重置Token
                    </el-button>
                </el-form-item>

                <!-- 状态 - 显示 -->
                <el-form-item label="状态">
                    <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
                        {{ userInfo.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
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
            <span>重置{{ resetType === 'key' ? '加密密钥' : 'Api Token' }}可能会影响相关功能的使用，确定要继续吗？</span>
            <template #footer>
                <el-button @click="resetDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="resetItem">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import type { User } from '@/apis/interface';
import { tokenStore } from '@/apis/system-api';

export default defineComponent({
    name: 'ProfilePage',
    setup() {
        // 用户信息 - 初始化为空对象
        const userInfo = ref<User>({
            userId: '',
            userCode: '',
            userName: '',
            password: '',
            strategy: '',
            key: '',
            token: '',
            status: 0,
            roles: []
        });

        // 编辑状态
        const editingName = ref(false);
        const originalName = ref('');

        // 重置相关状态
        const resetDialogVisible = ref(false);
        const resetType = ref<'key' | 'token'>('key');

        // 模拟从存储中获取用户信息
        const fetchUserInfo = () => {
            // 这里应该是从API或store获取用户信息的逻辑
            // 模拟数据
            userInfo.value = {
                userId: '123456',
                userCode: 'USER001',
                userName: '张三',
                password: '',
                strategy: '默认策略',
                key: 'asdfghjkl123456',
                token: tokenStore.getToken() || 'no-token',
                status: 1,
                roles: [
                    {
                        roleId: '1',
                        roleName: '管理员',
                        permissions: []
                    },
                    {
                        roleId: '2',
                        roleName: '编辑',
                        permissions: []
                    }
                ]
            };
            originalName.value = userInfo.value.userName;
        };

        // 编辑用户名
        const handleEditName = () => {
            if (editingName.value) {
                // 保存逻辑
                if (!userInfo.value.userName.trim()) {
                    ElMessage.error('用户名不能为空');
                    return;
                }

                // 这里应该是调用API更新用户名的逻辑
                ElMessage.success('用户名修改成功');
                originalName.value = userInfo.value.userName;
                editingName.value = false;
            } else {
                editingName.value = true;
            }
        };

        // 取消编辑用户名
        const cancelEditName = () => {
            userInfo.value.userName = originalName.value;
            editingName.value = false;
        };

        // 确认重置
        const confirmReset = (type: 'key' | 'token') => {
            resetType.value = type;
            resetDialogVisible.value = true;
        };

        // 执行重置
        const resetItem = () => {
            resetDialogVisible.value = false;

            // 这里应该是调用API重置key或token的逻辑
            if (resetType.value === 'key') {
                // 模拟重置key
                userInfo.value.key = 'new-key-' + Math.random().toString(36).substring(2, 10);
                ElMessage.success('Key重置成功');
            } else {
                // 模拟重置token
                const newToken = 'new-token-' + Math.random().toString(36).substring(2, 10);
                userInfo.value.token = newToken;
                tokenStore.setToken(newToken, 3600); // 假设有效期1小时
                ElMessage.success('Token重置成功');
            }
        };

        onMounted(() => {
            fetchUserInfo();
        });

        return {
            userInfo,
            editingName,
            originalName,
            resetDialogVisible,
            resetType,
            handleEditName,
            cancelEditName,
            confirmReset,
            resetItem
        };
    }
});
</script>

<style scoped>
.profile-container {
    max-width: 800px;
    margin: 20px auto;
    padding: 0 20px;
}

.profile-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.role-tags {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
}
</style>