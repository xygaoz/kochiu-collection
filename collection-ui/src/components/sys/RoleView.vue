<template>
    <div class="role-view">
        <div class="header">
            <h2>角色管理</h2>
            <div class="action-buttons">
                <el-button type="primary" @click="handleAdd">新增角色</el-button>
                <el-button @click="refreshData">刷新</el-button>
            </div>
        </div>

        <el-table :data="roleList" border style="width: 100%" v-loading="loading">
            <el-table-column prop="roleName" label="角色名称" width="120"></el-table-column>
            <el-table-column label="权限" min-width="300">
                <template #default="{row}">
                    <div class="permission-tags">
                        <el-tag
                            v-for="permission in row.permissions"
                            :key="permission.actionId"
                            size="small"
                            class="permission-tag"
                        >
                            {{ permission.moduleName }} - {{ permission.actionName }}
                        </el-tag>
                        <el-tag v-if="!row.permissions || row.permissions.length === 0" size="small" type="info">
                            无权限
                        </el-tag>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
                <template #default="{row}">
                    <el-button size="small" @click="handleEdit(row)">编辑</el-button>
                    <el-button
                        size="small"
                        type="danger"
                        @click="handleDelete(row.roleId, row.roleName)"
                    >
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 新增/编辑角色对话框 -->
        <el-dialog :title="dialogTitle" v-model="dialogVisible" width="60%">
            <el-form :model="roleForm" :rules="rules" ref="roleFormRef" label-width="120px">
                <el-form-item label="角色名称" prop="roleName">
                    <el-input v-model="roleForm.roleName" placeholder="请输入角色名称"></el-input>
                </el-form-item>

                <!-- 权限选择区域 -->
                <el-form-item label="权限选择" prop="permissions">
                    <div class="permission-tree-container">
                        <el-tree
                            ref="permissionTree"
                            :data="moduleTree"
                            show-checkbox
                            node-key="actionId"
                            :props="treeProps"
                            :default-expand-all="true"
                            :check-strictly="false"
                        >
                            <template #default="{ data }">
                    <span>
                        <!-- 显示模块或动作名称 -->
                        {{ data.moduleName || data.actionName }}
                        <!-- 如果是动作且已选中，显示已选标记 -->
                        <span v-if="'actionId' in data && roleForm.permissions.includes(data.actionId)"
                              class="selected-badge">✓</span>
                    </span>
                            </template>
                        </el-tree>
                    </div>
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
            title="删除角色"
            width="500px"
        >
            <p>确定要删除角色 "{{ deleteRoleInfo.roleName }}" 吗？</p>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="deleteDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="confirmDelete">确认</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { nextTick, onMounted, reactive, ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage } from "element-plus";
import { Action, Module, Role } from "@/apis/interface";
import { addRole, deleteRole, listRoles, updateRole } from "@/apis/role-api";
import { listModulesWithActions } from "@/apis/module-api";

const roleList = ref<Role[]>([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const isAdd = ref(false);
const roleFormRef = ref<FormInstance>();
const deleteDialogVisible = ref(false);
const permissionTree = ref();
const treeRefs = ref<any[]>([]);

// 修改treeProps定义
const treeProps = {
    children: (data: Module | Action) => {
        // 如果是模块，返回它的子模块或动作
        if ('moduleId' in data) {
            return data.children?.length ? data.children : data.actions;
        }
        return null;
    },
    label: (data: Module | Action) => {
        return 'moduleName' in data ? data.moduleName : data.actionName;
    },
    disabled: (data: Module | Action) => {
        // 只有动作可以被选中，模块不可选中
        return !('actionId' in data);
    }
};

interface DeleteRoleInfo {
    roleId: string;
    roleName: string;
}

const deleteRoleInfo = reactive<DeleteRoleInfo>({
    roleId: '',
    roleName: ''
});

interface RoleForm {
    roleId: string;
    roleName: string;
    permissions: number[]; // 存储选中的actionId数组
}

const roleForm = reactive<RoleForm>({
    roleId: '',
    roleName: '',
    permissions: []
});

const moduleTree = ref<Module[]>([]);

const rules = reactive<FormRules>({
    roleName: [
        { required: true, message: '请输入角色名称', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    permissions: [
        {
            required: true,
            validator: (rule, value, callback) => {
                if (!value || value.length === 0) {
                    callback(new Error('请至少选择一个权限'));
                } else {
                    callback();
                }
            },
            trigger: 'change'
        }
    ]
});

const fetchRoles = async () => {
    loading.value = true;
    try {
        roleList.value = await listRoles();
    } catch (error: any) {
        ElMessage.error('获取角色列表失败: ' + error.message);
    } finally {
        loading.value = false;
    }
};

const fetchModules = async () => {
    try {
        moduleTree.value = await listModulesWithActions();
    } catch (error: any) {
        ElMessage.error('获取权限列表失败: ' + error.message);
    }
};

const handleAdd = () => {
    dialogTitle.value = '新增角色';
    isAdd.value = true;
    treeRefs.value = []; // 清空treeRefs
    resetForm();
    dialogVisible.value = true;
};

const handleEdit = (row: Role) => {
    dialogTitle.value = '编辑角色';
    isAdd.value = false;

    // 填充表单数据
    Object.assign(roleForm, {
        roleId: row.roleId,
        roleName: row.roleName,
        permissions: row.permissions ? row.permissions.map(p => p.actionId) : []
    });

    dialogVisible.value = true;

    // 等待DOM更新后设置树的选择状态
    nextTick(() => {
        if (permissionTree.value) {
            permissionTree.value.setCheckedKeys(roleForm.permissions);
        }
    });
};


const handleDelete = (roleId: string, roleName: string) => {
    deleteRoleInfo.roleId = roleId;
    deleteRoleInfo.roleName = roleName;
    deleteDialogVisible.value = true;
};

const confirmDelete = async () => {
    try {
        if (await deleteRole(deleteRoleInfo.roleId)) {
            ElMessage.success('删除成功');
            await fetchRoles();
            deleteDialogVisible.value = false;
        }
    } catch (error: any) {
        ElMessage.error('删除失败: ' + error.message);
    }
};

const submitForm = async () => {
    if (!roleFormRef.value) return;

    // 获取选中的权限
    if (permissionTree.value) {
        roleForm.permissions = permissionTree.value.getCheckedKeys();
    }

    await roleFormRef.value.validate();

    try {
        if (isAdd.value) {
            if (await addRole(roleForm)) {
                ElMessage.success('新增角色成功');
                dialogVisible.value = false;
                await fetchRoles();
            }
        } else {
            if (await updateRole(roleForm)) {
                ElMessage.success('更新角色成功');
                dialogVisible.value = false;
                await fetchRoles();
            }
        }
    } catch (error: any) {
        ElMessage.error('操作失败: ' + error.message);
    }
};

const resetForm = () => {
    Object.assign(roleForm, {
        roleId: '',
        roleCode: '',
        roleName: '',
        permissions: []
    });
    nextTick(() => {
        if (permissionTree.value) {
            permissionTree.value.setCheckedKeys([]);
        }
    });
};


const refreshData = () => {
    fetchRoles();
};

onMounted(async () => {
    await fetchRoles();
    await fetchModules();
});
</script>

<style scoped>
.role-view {
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

.permission-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
}

.permission-tag {
    margin-right: 4px;
}

</style>