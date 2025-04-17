<template>
    <el-dialog
        v-model="visible"
        title="新增目录"
        width="400px"
        :close-on-click-modal="false"
    >
        <el-form :model="form" label-width="80px" :rules="rules">
            <el-form-item label="目录名称">
                <el-input v-model="form.cataName" />
            </el-form-item>
            <el-form-item label="上级目录">
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
import { addCatalog, getCatalogTree } from "@/apis/catalog-api";

const visible = ref(false);
const form = ref({
    cataName: '',
    parentId: null as number | null
});
const catalogTree = ref<Catalog[]>([]);
const emit = defineEmits(['confirm']);
const loading = ref(false);

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

// 修改open方法接收parentId参数
const open = async (parentId?: number) => {
    visible.value = true;
    loading.value = true;
    try {
        catalogTree.value = await getCatalogTree();
        // 如果有传入parentId，设置为默认选中
        if (parentId !== undefined) {
            form.value.parentId = parentId;
        } else {
            form.value.parentId = null;
        }
    } catch (error) {
        console.error('加载目录树失败:', error);
    }
    finally {
        loading.value = false;
    }
};

const handleConfirm = async () => {
    if(await addCatalog(form.value)) {
        form.value = { cataName: '', parentId: null };
        visible.value = false;
        emit('confirm', form.value);
    }
};

defineExpose({ open });
</script>