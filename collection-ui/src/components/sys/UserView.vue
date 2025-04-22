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
            <el-table-column prop="userCode" label="用户编码" width="150"></el-table-column>
            <el-table-column prop="userName" label="用户名称" width="120"></el-table-column>
            <el-table-column label="存储策略" width="120">
                <template #default="{row}">
                    {{ getStrategyName(row.strategy) }}
                </template>
            </el-table-column>
            <el-table-column prop="key" label="加密密钥" width="120"></el-table-column>
            <el-table-column prop="token" label="Api Token" width="250"></el-table-column>
            <el-table-column label="操作">
                <template #default="{row}">
                    <el-button size="small" @click="handleEdit(row)">编辑</el-button>
                    <el-button size="small" type="danger" @click="handleDelete(row.userId)">删除</el-button>
                    <el-button size="small" type="warning" @click="handleDelete(row.userId)">重置密码</el-button>
                    <el-button size="small" type="info" @click="handleDelete(row.userId)">重置密钥</el-button>
                    <el-button size="small" type="primary" @click="handleDelete(row.userId)">重置Token</el-button>
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
                    <el-input v-model="userForm.userCode" placeholder="请输入用户编码"></el-input>
                </el-form-item>
                <el-form-item label="用户名称" prop="userName">
                    <el-input v-model="userForm.userName" placeholder="请输入用户名称"></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password" v-if="isAdd">
                    <el-input v-model="userForm.password" type="password" placeholder="请输入密码"></el-input>
                </el-form-item>
                <el-form-item label="存储策略" prop="strategy">
                    <el-option
                        v-for="strategy in strategyList"
                        :key="strategy.strategyCode"
                        :label="strategy.strategyName"
                        :value="strategy.strategyCode">
                    </el-option>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitForm">确认</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import axios from "axios";
import { Strategy, User } from "@/apis/interface";
import { listUsers } from "@/apis/user-api";
import { getStrategyList } from "@/apis/system-api";

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

const pagination = reactive<Pagination>({
    currentPage: 1,
    pageSize: 10,
    total: 0
})

const searchForm = reactive<SearchForm>({
    userCode: '',
    userName: ''
})

const userForm = reactive<User>({
    userId: '',
    userCode: '',
    userName: '',
    password: '',
    strategy: 'local',
    key: '',
    token: '',
})

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
    ]
})

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
    dialogTitle.value = '编辑用户'
    isAdd.value = false
    Object.assign(userForm, row)
    dialogVisible.value = true
}

const handleDelete = (id: string) => {
    ElMessageBox.confirm('确认删除该用户吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        try {
            await axios.delete(`/api/users/${id}`)
            ElMessage.success('删除成功')
            fetchUsers()
        } catch (error: any) {
            ElMessage.error('删除失败: ' + error.message)
        }
    }).catch(() => {
        ElMessage.info('已取消删除')
    })
}

const submitForm = async () => {
    if (!userFormRef.value) return
    await userFormRef.value.validate()

    try {
        if (isAdd.value) {
            await axios.post('/api/users', userForm)
            ElMessage.success('新增用户成功')
        } else {
            await axios.put(`/api/users/${userForm.userId}`, userForm)
            ElMessage.success('更新用户成功')
        }
        dialogVisible.value = false
        fetchUsers()
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
        key: '',
        token: '',
    })
}

const refreshData = () => {
    fetchUsers()
}

onMounted(() => {
    fetchUsers()
})
</script>

<style scoped>
.user-view {
    padding: 10px;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    height: 32px;
}

.search-bar {
    margin-bottom: 10px;
}

.pagination {
    margin-top: 20px;
    text-align: right;
}
</style>