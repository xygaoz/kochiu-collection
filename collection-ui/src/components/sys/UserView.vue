<template>
    <div class="user-view">
        <div class="header">
            <h2>用户管理</h2>
            <div class="action-buttons">
                <el-button type="primary" @click="handleAdd">新增用户</el-button>
                <el-button @click="refreshData">刷新</el-button>
            </div>
        </div>

        <div class="search-bar">
            <el-form :inline="true" :model="searchForm">
                <el-form-item label="用户编码">
                    <el-input v-model="searchForm.userCode" placeholder="请输入用户编码"></el-input>
                </el-form-item>
                <el-form-item label="用户名称">
                    <el-input v-model="searchForm.userName" placeholder="请输入用户名称"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="handleSearch">查询</el-button>
                    <el-button @click="resetSearch">重置</el-button>
                </el-form-item>
            </el-form>
        </div>

        <el-table :data="userList" border style="width: 100%" v-loading="loading">
            <el-table-column prop="userCode" label="用户编码" width="150" fixed></el-table-column>
            <el-table-column prop="userName" label="用户名称" width="120"></el-table-column>
            <el-table-column label="用户角色" width="220">
                <template #default="{row}">
                    <div class="role-tags">
                        <el-tag
                            v-for="role in row.roles"
                            :key="role.roleId"
                            size="small"
                            class="role-tag"
                        >
                            {{ role.roleName }}
                        </el-tag>
                        <el-tag v-if="!row.roles || row.roles.length === 0" size="small" type="info">
                            无角色
                        </el-tag>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="存储策略" width="120">
                <template #default="{row}">
                    {{ getStrategyName(row.strategy) }}
                </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
                <template #default="{row}">
                    <el-tag v-if="row.status === 1" type="success">启用</el-tag>
                    <el-tag v-else type="danger">停用</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="key" label="加密密钥" width="120"></el-table-column>
            <el-table-column prop="token" label="Api Token" width="250"></el-table-column>
            <el-table-column label="操作" width="300">
                <template #default="{row}">
                    <el-button size="small" @click="handleEdit(row)">编辑</el-button>
                    <el-button
                        v-if="row.canDel === 1"
                        size="small"
                        type="danger"
                        @click="handleDelete(row.userId, row.userCode)"
                    >
                        删除
                    </el-button>
                    <el-button
                        v-if="row.canDel === 1"
                        size="small"
                        :type="row.status === 'active' ? 'danger' : 'success'"
                        @click="handleStatusChange(row)"
                    >
                        {{ row.status === 1 ? '停用' : '启用' }}
                    </el-button>
                    <el-button size="small" type="warning" v-if="row.canDel === 1" @click="handleResetPassword(row)">重置密码</el-button>
                </template>
            </el-table-column>
        </el-table>

        <div class="pagination">
            <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pagination.currentPage"
                :page-size="pagination.pageSize"
                layout="prev, pager, next"
                :total="pagination.total">
            </el-pagination>
        </div>

        <!-- 新增/编辑用户对话框 -->
        <el-dialog :title="dialogTitle" v-model="dialogVisible" width="50%">
            <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="120px">
                <el-form-item label="用户编码" prop="userCode">
                    <el-input v-model="userForm.userCode" placeholder="请输入用户编码" :disabled="!isAdd"></el-input>
                </el-form-item>
                <el-form-item label="用户名称" prop="userName">
                    <el-input v-model="userForm.userName" placeholder="请输入用户名称"></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password" v-if="isAdd">
                    <el-input v-model="userForm.password" type="password" placeholder="请输入密码"></el-input>
                </el-form-item>
                <el-form-item label="存储策略" prop="strategy">
                    <el-select v-model="userForm.strategy" placeholder="请选择存储策略">
                        <el-option
                            v-for="strategy in strategyList"
                            :key="strategy.strategyCode"
                            :label="strategy.strategyName"
                            :value="strategy.strategyCode">
                        </el-option>
                    </el-select>
                </el-form-item>

                <!-- 角色选择区域 -->
                <el-form-item
                    label="用户角色"
                    prop="roles"
                    :rules="rules.roles"
                    :class="{'is-error': rolesError}"
                >
                    <div class="role-tags-container">
                        <el-tag
                            v-for="role in allRoles"
                            :key="role.roleId"
                            :type="isRoleSelected(role.roleId) ? '' : 'info'"
                            :effect="isRoleSelected(role.roleId) ? 'dark' : 'plain'"
                            :class="{'dashed-tag': !isRoleSelected(role.roleId)}"
                            @click="toggleRole(role)"
                        >
                            {{ role.roleName }}
                        </el-tag>
                    </div>
                    <div class="el-form-item__error" v-if="rolesError">请选择用户角色</div>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitForm">确认</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 删除确认对话框 -->
        <el-dialog
            v-model="deleteDialogVisible"
            title="删除用户"
            width="500px"
        >
            <p>确定要删除用户 {{ deleteUserInfo.userCode }} 吗？</p>
            <el-radio-group v-model="deleteOption">
                <el-radio label="keep">保留资源文件</el-radio>
                <el-radio label="delete">删除资源文件</el-radio>
            </el-radio-group>
            <template #footer>
            <span class="dialog-footer">
                <el-button @click="deleteDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="confirmDelete">确认</el-button>
            </span>
            </template>
        </el-dialog>

        <!-- 重置密码 -->
        <el-dialog
            v-model="resetDialogVisible"
            title="重置密码"
            width="500px"
        >
            <el-form :model="resetPwdForm" :rules="resetRules" ref="resetFormRef" label-width="80px">
                <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="resetPwdForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="resetPwdForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
            <span class="dialog-footer">
                <el-button @click="resetDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="confirmReset">确认</el-button>
            </span>
            </template>
        </el-dialog>

        <!-- 启用/停用对话框 -->
        <el-dialog
            v-model="statusDialogVisible"
            :title="`确定要${statusAction === 'enable' ? '启用' : '停用'}用户 ${currentUser.userCode} 吗？`"
            width="400px"
        >
            <template #footer>
            <span class="dialog-footer">
                <el-button @click="statusDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="confirmStatusChange">确认</el-button>
            </span>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { nextTick, onMounted, reactive, ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage } from "element-plus";
import { Role, Strategy, User } from "@/apis/interface";
import { addUser, deleteUser, enableOrDisable, listUsers, resetPwd, updateUser } from "@/apis/user-api";
import { getPublicKey, getStrategyList } from "@/apis/system-api";
import { listRoles } from "@/apis/role-api";
import { encryptPassword } from "@/apis/utils";

interface Pagination {
    currentPage: number
    pageSize: number
    total: number
}

interface SearchForm {
    userCode: string
    userName: string
}

const userList = ref<User[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isAdd = ref(false)
const userFormRef = ref<FormInstance>()
const strategyList = ref<Strategy[]>([])
const allRoles = ref<Role[]>([]);
const rolesError = ref(false);
const publicKey = ref<string | null>(null); // 用于存储公钥
const deleteDialogVisible = ref(false);
const deleteOption = ref<'keep' | 'delete'>('keep');
const resetDialogVisible = ref(false);
const resetFormRef = ref<FormInstance>();
const resetPwdForm = reactive({
    userId: '',
    newPassword: '',
    confirmPassword: ''
});

const statusDialogVisible = ref(false);
const currentUser = reactive({
    userId: '',
    userCode: '',
    status: null as number | null,
});
const statusAction = ref<'enable' | 'disable'>('enable');

const deleteUserInfo = reactive({
    userId: '',
    userCode: '',
    deleteOption: ''
});

const pagination = reactive<Pagination>({
    currentPage: 1,
    pageSize: 10,
    total: 0
})

const searchForm = reactive<SearchForm>({
    userCode: '',
    userName: ''
})

interface Form {
    userId: string
    userCode: string
    userName: string
    password: string
    strategy: string
    roles: string[]
}

const userForm = reactive<Form>({
    userId: '',
    userCode: '',
    userName: '',
    password: '',
    strategy: 'local',
    roles: []
})

// 密码验证规则
const validatePassword = (rule: any, value: string | undefined, callback: any) => {
    if (!value || value.trim() === '') {
        callback(new Error('请输入密码'));
    } else if (value.length < 6) {
        callback(new Error('密码长度不能少于6位'));
    } else {
        // Check if confirm password matches
        if (resetPwdForm.confirmPassword && resetPwdForm.confirmPassword !== value) {
            // Trigger validation of confirm password field
            resetFormRef.value?.validateField('confirmPassword');
        }
        callback();
    }
};

const validateConfirmPassword = (rule: any, value: string | undefined, callback: any) => {
    if (!value || value.trim() === '') {
        callback(new Error('请再次输入密码'));
    } else if (value !== resetPwdForm.newPassword) {
        callback(new Error('两次输入密码不一致'));
    } else {
        callback();
    }
};

const resetRules = reactive({
    newPassword: [
        { required: true, validator: validatePassword, trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, validator: validateConfirmPassword, trigger: 'blur' }
    ]
});

const getStrategyName = (strategyCode: string) => {
    const strategy = strategyList.value.find(s => s.strategyCode === strategyCode);
    return strategy ? strategy.strategyName : strategyCode;
}

const rules = reactive<FormRules>({
    userCode: [
        { required: true, message: '请输入用户编码', trigger: 'blur' },
        { min: 3, max: 100, message: '长度在 3 到 100 个字符', trigger: 'blur' }
    ],
    userName: [
        { required: true, message: '请输入用户名称', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
    ],
    strategy: [
        { required: true, message: '请选择存储策略', trigger: 'change' }
    ],
    roles: [
        {
            validator: (rule, value, callback) => {
                if (!value || value.length === 0) {
                    rolesError.value = true;
                    callback(new Error('请选择用户角色'));
                } else {
                    rolesError.value = false;
                    callback();
                }
            },
            trigger: 'change' // 虽然不自动触发，但保留
        }
    ]
})

const fetchAllRoles = async () => {
    try {
        allRoles.value = await listRoles();
    } catch (error) {
        console.error("获取角色列表失败:", error);
        ElMessage.error("获取角色列表失败");
    }
};

const fetchUsers = async () => {
    loading.value = true
    try {
        strategyList.value = await getStrategyList();

        const params = {
            pageNum: pagination.currentPage,
            pageSize: pagination.pageSize,
            ...searchForm
        }
        const response = await listUsers(params);
        userList.value = response.list
        pagination.total = response.total
    } catch (error: any) {
        ElMessage.error('获取用户列表失败: ' + error.message)
    } finally {
        loading.value = false
    }
}

const isRoleSelected = (roleId: string) => {
    return userForm.roles.includes(roleId);
};

const toggleRole = (role: Role) => {
    if (isRoleSelected(role.roleId)) {
        userForm.roles = userForm.roles.filter(r => r !== role.roleId);
    } else {
        userForm.roles.push(role.roleId);
    }
};

const handleSearch = () => {
    pagination.currentPage = 1
    fetchUsers()
}

const resetSearch = () => {
    searchForm.userCode = ''
    searchForm.userName = ''
    handleSearch()
}

const handleSizeChange = (val: number) => {
    pagination.pageSize = val
    fetchUsers()
}

const handleCurrentChange = (val: number) => {
    pagination.currentPage = val
    fetchUsers()
}

const handleAdd = () => {
    dialogTitle.value = '新增用户'
    isAdd.value = true
    resetForm()
    dialogVisible.value = true
}

const handleEdit = (row: User) => {
    dialogTitle.value = '编辑用户';
    isAdd.value = false;
    Object.assign(userForm, {
        ...row,
        // 将角色对象数组转换为角色ID数组
        roles: row.roles ? row.roles.map(role => role.roleId) : []
    });
    dialogVisible.value = true;
};

const handleDelete = (userId: string, userCode: string) => {
    deleteUserInfo.userId = userId;
    deleteUserInfo.userCode = userCode;
    deleteDialogVisible.value = true;
};

const confirmDelete = async () => {
    try {
        deleteUserInfo.deleteOption  = deleteOption.value;
        if(await deleteUser(deleteUserInfo)) {
            ElMessage.success('删除成功');
            await fetchUsers();
            deleteDialogVisible.value = false;
        }
    } catch (error: any) {
        ElMessage.error('删除失败: ' + error.message);
    }
};

const submitForm = async () => {
    if (!userFormRef.value) return

    // 手动检查角色
    if (userForm.roles.length === 0) {
        rolesError.value = true;
        ElMessage.error('请至少选择一个角色');
        return;
    }
    await userFormRef.value.validate()

    try {
        if (isAdd.value) {

            const encryptedPassword = encryptPassword(publicKey.value!, userForm.password);
            const params = {
                ...userForm,
                password: encryptedPassword,
            }

            if(!await addUser(params)) {
                return
            }
            ElMessage.success('新增用户成功')
        } else {
            if(!await updateUser(userForm)) {
                return
            }
            ElMessage.success('更新用户成功')
        }
        dialogVisible.value = false
        await fetchUsers()
    } catch (error: any) {
        ElMessage.error('操作失败: ' + error.message)
    }
}

const resetForm = () => {
    Object.assign(userForm, {
        userId: '',
        userCode: '',
        userName: '',
        password: '',
        strategy: 'local',
        roles: []
    })
}

const refreshData = () => {
    fetchUsers()
}

const handleResetPassword = (row: User) => {
    resetPwdForm.userId = row.userId;
    resetPwdForm.newPassword = '';
    resetPwdForm.confirmPassword = '';
    resetDialogVisible.value = true;

    // 清除之前的验证错误
    nextTick(() => {
        resetFormRef.value?.clearValidate();
    });
};

const confirmReset = async () => {
    try {
        // 手动检查空值
        if (!resetPwdForm.newPassword || resetPwdForm.newPassword.trim() === '') {
            ElMessage.error('请输入新密码');
            return;
        }

        if (!resetPwdForm.confirmPassword || resetPwdForm.confirmPassword.trim() === '') {
            ElMessage.error('请确认新密码');
            return;
        }

        if (resetPwdForm.newPassword !== resetPwdForm.confirmPassword) {
            ElMessage.error('两次输入密码不一致');
            return;
        }

        const encryptedPassword = encryptPassword(publicKey.value!, resetPwdForm.newPassword);
        const params = {
            userId: resetPwdForm.userId,
            newPassword: encryptedPassword
        };

        if (await resetPwd(params)) {
            ElMessage.success('密码重置成功');
            resetDialogVisible.value = false;
        }
    } catch (error) {
        console.error('密码重置失败:', error);
        ElMessage.error('密码重置失败');
    }
};

const handleStatusChange = (row: User) => {
    currentUser.userId = row.userId;
    currentUser.userCode = row.userCode;
    currentUser.status = row.status;
    statusAction.value = row.status === 1 ? 'disable' : 'enable';
    statusDialogVisible.value = true;
};

const confirmStatusChange = async () => {
    try {
        const params = {
            userId: currentUser.userId,
            status: statusAction.value
        };

        if (await enableOrDisable(params)) {
            ElMessage.success(`用户已${statusAction.value === 'enable' ? '启用' : '停用'}`);
            await fetchUsers();
            statusDialogVisible.value = false;
        }
    } catch (error) {
        ElMessage.error('操作失败');
    }
};

onMounted(async () => {
    try {
        publicKey.value = await getPublicKey();
    } catch (error) {
        console.error("获取公钥失败:", error);
        ElMessage.error("获取公钥失败");
    }
    await fetchUsers();
    await fetchAllRoles();
});
</script>

<style scoped>
.user-view {
    padding: 20px;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    height: 32px;
}

.header h2 {
    font-size: 18px;
}

.search-bar {
    margin-bottom: 10px;
}

.pagination {
    margin-top: 20px;
    text-align: right;
}

.role-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
}

.role-tag {
    margin-right: 4px;
}

.role-tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.dashed-tag {
    border-style: dashed !important;
    cursor: pointer;
}

.el-tag {
    cursor: pointer;
    transition: all 0.3s;
}

.el-tag:hover {
    opacity: 0.8;
}

.is-error .role-tags-container {
    border: 1px solid #f56c6c;
    border-radius: 4px;
    padding: 8px;
}

.el-form-item__error {
    color: #f56c6c;
    font-size: 12px;
    line-height: 1;
    padding-top: 4px;
    position: absolute;
    top: 100%;
    left: 0;
}
</style>