<template>
    <el-dialog
        v-model="visible"
        title="新增目录"
        width="400px"
        :close-on-click-modal="false"
    >
        <el-form :model="form" label-width="80px">
            <el-form-item label="目录名称">
                <el-input v-model="form.folderName" />
            </el-form-item>
            <el-form-item label="上级目录">
                <el-tree-select
                    v-model="form.parentId"
                    :data="catalogTree"
                    check-strictly
                    :props="treeProps"
                    placeholder="请选择"
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
import { getCatalogTree } from "@/apis/catalog-api";

const visible = ref(false);
const form = ref({
    folderName: '',
    parentId: null as number | null
});
const catalogTree = ref<Catalog[]>([]);
const emit = defineEmits(['confirm']);

const treeProps = {
    value: 'folderId',
    label: 'folderName',
    children: 'children'
};

const open = async () => {
    visible.value = true;
    try {
        catalogTree.value = await getCatalogTree();
    } catch (error) {
        console.error('加载目录树失败:', error);
    }
};

const handleConfirm = () => {
    emit('confirm', form.value);
    visible.value = false;
    form.value = { folderName: '', parentId: null };
};

defineExpose({ open });
</script>