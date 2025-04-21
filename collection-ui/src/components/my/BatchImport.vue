<template>
    <div class="batch-import-container">
        <el-card shadow="hover">
            <template #header>
                <div class="card-header">
                    <span>批量导入资源</span>
                </div>
            </template>

            <el-form ref="importForm" :model="form" :rules="rules" label-width="180px">
                <!-- 1. 服务端资源路径 -->
                <el-form-item label="服务端资源路径" prop="sourcePath">
                    <el-input v-model="form.sourcePath" placeholder="请输入可访问的服务端资源路径">
                        <template #append>
                            <el-button @click="testPathConnection">测试连接</el-button>
                        </template>
                    </el-input>
                    <div class="form-tip">该路径必须是服务端可访问的有效路径</div>
                </el-form-item>

                <!-- 2. 选择现有分类 -->
                <el-form-item label="选择分类" prop="categoryId">
                    <el-select
                        v-model="form.categoryId"
                        placeholder="请选择分类"
                        clearable
                        filterable
                        style="width: 100%"
                        @change="handleCategoryChange"
                    >
                        <el-option
                            v-for="category in categoryList"
                            :key="category.cateId"
                            :label="category.cateName"
                            :value="category.cateId"
                        />
                    </el-select>
                </el-form-item>

                <!-- 3. 选择现有目录 -->
                <el-form-item label="选择目录" prop="catalogId">
                    <el-cascader
                        v-model="form.catalogId"
                        :options="catalogTree"
                        :props="cascaderProps"
                        placeholder="请选择目录"
                        clearable
                        filterable
                        style="width: 100%"
                    />
                    <div class="form-tip">不选择目录时，将按下方规则自动创建目录</div>
                </el-form-item>

                <!-- 4. 自动创建目录选项 -->
                <el-form-item label="自动创建目录规则" prop="autoCreateRule">
                    <el-radio-group v-model="form.autoCreateRule" :disabled="form.importMethod === 3">
                        <el-radio :label="1">在根目录创建日期目录 (格式: YYYY-MM-DD)</el-radio>
                        <el-radio :label="2">在根目录按服务端路径子目录结构创建</el-radio>
                        <el-radio :label="3">不自动创建 (仅导入到根目录)</el-radio>
                    </el-radio-group>
                </el-form-item>

                <!-- 5. 导入方式 -->
                <el-form-item label="导入方式" prop="importMethod">
                    <el-radio-group v-model="form.importMethod" style="width: 100%">
                        <el-radio :label="1">复制到新位置</el-radio>
                        <el-radio :label="2">移动到新位置</el-radio>
                        <el-radio :label="3">保持原路径 (仅建立索引)</el-radio>
                    </el-radio-group>
                    <div class="form-tip">选择保持原路径，创建目录规则强制为"不自动创建 (仅导入到根目录)"</div>
                </el-form-item>

                <!-- 操作按钮 -->
                <el-form-item>
                    <el-button type="primary" @click="submitForm">开始导入</el-button>
                    <el-button @click="resetForm">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <!-- 导入进度对话框 -->
        <el-dialog v-model="progressDialogVisible" title="导入进度" width="50%" :close-on-click-modal="false">
            <el-progress :percentage="progressPercent" :status="progressStatus" />
            <div class="progress-info">
                <div>已处理: {{ processedCount }} / {{ totalCount }}</div>
                <div>成功: {{ successCount }} 失败: {{ failCount }}</div>
                <div>当前文件: {{ currentFile }}</div>
                <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
            </div>
            <template #footer>
                <el-button v-if="!importComplete" type="danger" @click="cancelImport">取消导入</el-button>
                <el-button v-else type="primary" @click="progressDialogVisible = false">完成</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { listCategory } from '@/apis/category-api'
import { getCatalogTree } from '@/apis/catalog-api'
import { Catalog, Category } from "@/apis/interface";
import { testServerPath } from "@/apis/system-api";
import { cancelBatchImport, startBatchImport } from "@/apis/resource-api";

// 表单数据
const form = ref({
    taskId: '',
    sourcePath: '',
    categoryId: null as number | null,
    catalogId: null as number | null,
    autoCreateRule: 1,
    importMethod: 1
})

// 表单验证规则
const rules = ref<FormRules>({
    sourcePath: [
        { required: true, message: '请输入服务端资源路径', trigger: 'blur' }
    ],
    categoryId: [
        { required: true, message: '请选择分类', trigger: 'change' }
    ]
})

// 分类列表
const categoryList = ref<Category[]>([])
// 目录树
const catalogTree = ref<Catalog[]>([])
// 级联选择器配置
const cascaderProps = {
    value: 'id',
    label: 'label',
    children: 'children',
    checkStrictly: true,
    emitPath: false
}

// 进度对话框相关
const progressDialogVisible = ref(false)
const progressPercent = ref(0)
const progressStatus = ref('')
const processedCount = ref(0)
const successCount = ref(0)
const failCount = ref(0)
const totalCount = ref(0)
const currentFile = ref('')
const errorMessage = ref('')
const importComplete = ref(false)

// 表单引用
const importForm = ref<FormInstance>()

// 监听导入方式变化
watch(() => form.value.importMethod, (newVal) => {
    // 当选择"保持原路径"时，强制设置不自动创建目录
    if (newVal === 3) {
        form.value.autoCreateRule = 3
        form.value.catalogId = null
    }
})

// 监听自动创建规则变化
watch(() => form.value.autoCreateRule, (newVal) => {
    // 当导入方式为"保持原路径"时，不允许修改自动创建规则
    if (form.value.importMethod === 3 && newVal !== 3) {
        form.value.autoCreateRule = 3
        form.value.catalogId = null
        ElMessage.warning('保持原路径模式下，必须选择"不自动创建"')
    }
})

// 加载分类和目录数据
onMounted(async () => {
    try {
        categoryList.value = await listCategory()
        if(categoryList.value.length > 0){
            form.value.categoryId = categoryList.value[0].cateId
        }
        catalogTree.value = await getCatalogTree()
    } catch (error) {
        ElMessage.error('加载分类或目录数据失败')
        console.error(error)
    }
})

// 测试路径连接
const testPathConnection = async () => {
    if (!form.value.sourcePath) {
        ElMessage.warning('请输入服务端资源路径')
        return
    }

    try {
        // 这里调用API测试路径是否可访问
        const result = await testServerPath(form.value.sourcePath, form.value.importMethod)
        if(result) {
            ElMessage.success('路径可访问')
        }
        else{
            ElMessage.error('路径不可访问或不存在')
        }
    } catch (error) {
        ElMessage.error('路径不可访问或不存在')
    }
}

// 提交表单
const submitForm = async () => {
    try {
        await importForm.value?.validate()

        // 确认对话框
        await ElMessageBox.confirm(
            '确认开始批量导入资源吗？此操作可能需要较长时间。',
            '确认导入',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )

        // 开始导入
        await startImport()
    } catch (error) {
        // 验证失败或用户取消
        console.log('导入取消或验证失败', error)
    }
}

// 开始导入过程
const startImport = async () => {
    progressDialogVisible.value = true;
    progressPercent.value = 0;
    processedCount.value = 0;
    successCount.value = 0;
    failCount.value = 0;
    importComplete.value = false;
    errorMessage.value = '';

    try {
        // 1. 调用后端启动导入
        const taskId = await startBatchImport(form.value);
        if(!taskId){
            return
        }
        console.log("Task ID:", taskId); // 调试日志
        form.value.taskId = taskId;

        // 2. 动态获取 WebSocket URL
        const wsUrl = `${import.meta.env.VUE_WS_TARGET_URL}/ws/import-progress?task-id=${taskId}`;
        console.log("WebSocket URL:", wsUrl); // 调试日志

        // 3. 建立 WebSocket 连接
        const ws = new WebSocket(wsUrl);

        ws.onopen = () => {
            console.log("WebSocket 连接已建立"); // 调试日志
        };

        ws.onmessage = (event) => {
            console.log("收到进度消息:", event.data); // 调试日志
            const progress = JSON.parse(event.data);
            progressPercent.value = Math.floor((progress.current / progress.total) * 100);
            if(progress.current >= 0) {
                processedCount.value = progress.current;
            }
            if(progress.total >= 0) {
                totalCount.value = progress.total;
            }
            currentFile.value = progress.currentFile;
            if(progress.success >= 0) {
                successCount.value = progress.success;
            }
            if(progress.fail >= 0) {
                failCount.value = progress.fail;
            }

            if (progress.status === 'completed') {
                importComplete.value = true;
                progressStatus.value = 'success';
                ws.close();
                form.value = {
                    sourcePath: '',
                    categoryId: null,
                    catalogId: null,
                    autoCreateRule: 1,
                    importMethod: 1
                };
            } else if (progress.status === 'error') {
                importComplete.value = true;
                progressStatus.value = 'exception';
                errorMessage.value = progress.errorMessage;
                ws.close();
            }
        };

        ws.onerror = (error) => {
            console.error("WebSocket 错误:", error); // 调试日志
            errorMessage.value = `连接失败: ${error.type}`;
            importComplete.value = true;
        };

        ws.onclose = (event) => {
            console.log("WebSocket 关闭:", event.code, event.reason); // 调试日志
            if (!importComplete.value) {
                errorMessage.value = '连接意外关闭';
            }
        };

    } catch (error) {
        console.error("导入启动异常:", error); // 调试日志
        progressStatus.value = 'exception';
        errorMessage.value = error instanceof Error ? error.message : '导入启动失败';
        importComplete.value = true;
    }
};

// 取消导入
const cancelImport = async () => {
    importComplete.value = true;
    const result = await cancelBatchImport(form.value.taskId);
    if(result) {
        ElMessage.info("任务取消了");
    }
};

// 重置表单
const resetForm = () => {
    importForm.value?.resetFields()
    form.value.autoCreateRule = 1
    form.value.importMethod = 1
}
</script>

<style scoped>
.batch-import-container {
    padding: 20px;
}

.card-header {
    font-size: 18px;
    font-weight: bold;
}

.form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 2px;
    display: block; /* 确保以块级元素显示 */
}

.progress-info {
    margin-top: 20px;
}

.error-message {
    color: #f56c6c;
    margin-top: 10px;
}
</style>