<template>
    <el-dialog
        v-model="visible"
        :title="dialogTitle"
        width="400px"
        :close-on-click-modal="false"
    >
        <el-form :model="form" label-width="90px" :rules="isDeleteMode ? {} : rules"
                 ref="formRef"
        >
            <el-form-item label="目录名称" v-if="!isDeleteMode" prop="cataName">
                <el-input v-model="form.cataName" />
            </el-form-item>
            <el-form-item :label="isDeleteMode ? '移动到目录' : '上级目录'" prop="parentId">
                <el-tree-select
                    v-model="form.parentId"
                    :data="catalogTree"
                    check-strictly
                    :props="treeProps"
                    placeholder="请选择"
                    :loading="loading"
                />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="visible = false">取消</el-button>
            <el-button type="primary" @click="handleConfirm">确定</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, defineEmits, defineExpose } from 'vue';
import { Catalog } from "@/apis/interface";
import { addCatalog, updateCatalog, deleteCatalog, getCatalogTree } from "@/apis/catalog-api";
import { ElMessage } from "element-plus";

const visible = ref(false);
const isDeleteMode = ref(false);
const dialogTitle = ref('新增目录');
const currentCatalog = ref<Catalog | null>(null);
const form = ref({
    cateId: null as number | null,
    cataName: '',
    parentId: null as number | null
});
const catalogTree = ref<Catalog[]>([]);
const emit = defineEmits(['confirm']);
const loading = ref(false);
const formRef = ref(null); // 添加表单引用

const treeProps = {
    value: 'id',
    label: 'label',
    children: 'children'
};
const rules = {
    cataName: [
        { required: true, message: '请输入目录名称', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
    ],
    parentId: [
        { required: true, message: '请选择上级目录', trigger: 'change' }
    ]
};

// 打开编辑对话框
const open = async (parentId?: number, catalog?: Catalog) => {
    isDeleteMode.value = false;
    dialogTitle.value = catalog ? '修改/移动目录' : '新增目录';
    currentCatalog.value = catalog || null;

    visible.value = true;
    loading.value = true;
    try {
        catalogTree.value = await getCatalogTree();
        if (catalog) {
            form.value = {
                cateId: catalog.id,
                cataName: catalog.label,
                parentId: catalog.parentId || parentId || null
            };
        } else {
            form.value = {
                cateId: null,
                cataName: '',
                parentId: parentId || null
            };
        }
    } catch (error) {
        console.error('加载目录树失败:', error);
    } finally {
        loading.value = false;
    }
};

// 打开删除对话框
const openForDelete = async (parentId: number | undefined, catalog: Catalog) => {
    isDeleteMode.value = true;
    dialogTitle.value = '删除目录';
    currentCatalog.value = catalog;

    visible.value = true;
    loading.value = true;
    try {
        catalogTree.value = await getCatalogTree();
        form.value = {
            cateId: catalog.id,
            cataName: catalog.label,
            parentId: parentId || null
        };
    } catch (error) {
        console.error('加载目录树失败:', error);
    } finally {
        loading.value = false;
    }
};

const handleConfirm = async () => {
    if (isDeleteMode.value) {
        // 删除操作
        try {
            if (currentCatalog.value?.id && form.value.parentId !== null) {
                await deleteCatalog(currentCatalog.value.id, form.value.parentId);
                ElMessage.success('目录删除成功');
                visible.value = false;
                emit('confirm');
            }
        } catch (error) {
            ElMessage.error('目录删除失败');
            console.error('删除目录失败:', error);
        }
    } else {
        // 新增/修改操作
        try {
            formRef.value.validate(async (valid) => {
                if (valid) {
                    if (form.value.cateId) {
                        // 修改
                        if (await updateCatalog(form.value)) {
                            ElMessage.success('目录修改成功');
                            visible.value = false;
                            emit('confirm');
                        }
                    } else {
                        // 新增
                        if(await addCatalog(form.value)) {
                            ElMessage.success('目录添加成功');
                            visible.value = false;
                            emit('confirm');
                        }
                    }
                }
                else{
                    ElMessage.error('请填写完整信息');
                }
            });
        } catch (error) {
            ElMessage.error(form.value.cateId ? '目录修改失败' : '目录添加失败');
            console.error('操作失败:', error);
        }
    }
};

defineExpose({ open, openForDelete });
</script>