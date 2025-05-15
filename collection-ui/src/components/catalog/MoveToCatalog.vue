<template>
    <el-dialog
        v-model="visible"
        :title="dialogTitle"
        width="400px"
        :close-on-click-modal="false"
    >
        <el-form :model="form" label-width="100px" :rules="rules"
                 ref="formRef"
        >
            <el-form-item label="移动到目录" prop="cataId">
                <el-tree-select
                    v-model="form.cataId"
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
import { getCatalogTree } from "@/apis/catalog-api";
import { ElMessage, FormInstance } from "element-plus";
import { moveToCatalog } from "@/apis/resource-api";

const visible = ref(false);
const dialogTitle = ref('移动资源');
const form = ref({
    cataId: null as number | null,
});
const catalogTree = ref<Catalog[]>([]);
const emit = defineEmits(['success']);
const loading = ref(false);
const formRef = ref<FormInstance | null>(null); // 添加表单引用
const resourceIds = ref<number[]>([]);

const treeProps = {
    value: 'id',
    label: 'label',
    children: 'children'
};
const rules = {
    cataId: [
        { required: true, message: '请选择目标目录', trigger: 'change' }
    ]
};

// 打开编辑对话框
const open = async (ids: number[]) => {
    resourceIds.value = ids;
    visible.value = true;
    loading.value = true;
    try {
        catalogTree.value = await getCatalogTree();
        form.value = {
            cataId: null,
        };
    } catch (error) {
        console.error('加载目录树失败:', error);
    } finally {
        loading.value = false;
    }
};

const handleConfirm = async () => {
    try {
        if (!formRef.value) {
            console.error('表单引用未初始化');
            return;
        }

        formRef.value.validate(async (valid: boolean) => {
            if (valid) {
                let result = await moveToCatalog(resourceIds.value, {
                    cataId: form.value.cataId
                });
                if(result) {
                    ElMessage.success('移动成功');
                    emit('success');
                    visible.value = false;
                }
            }
        });
    } catch (error) {
        ElMessage.error('资源移动失败');
        console.error('操作失败:', error);
    }
};

defineExpose({ open });
</script>