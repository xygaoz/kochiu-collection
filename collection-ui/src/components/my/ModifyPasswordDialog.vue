<template>
    <el-dialog
        v-model="visible"
        title="修改密码"
        width="500px"
    >
        <el-form :model="modifyPwdForm" :rules="modifyRules" ref="modifyFormRef" label-width="80px">
            <el-form-item label="旧密码" prop="oldPassword">
                <el-input v-model="modifyPwdForm.oldPassword" type="password" placeholder="请输入旧密码" clearable></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="modifyPwdForm.newPassword" type="password" placeholder="请输入新密码" clearable></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="modifyPwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" clearable></el-input>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="visible = false">取消</el-button>
                <el-button type="primary" @click="confirmModify">确认</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, defineExpose, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox, FormInstance } from "element-plus";
import { logout, modifyPassword } from "@/apis/user-api";
import { encryptPassword } from "@/utils/utils";
import { getPublicKey } from "@/apis/system-api";

const modifyPwdForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
});
const modifyFormRef = ref<FormInstance>();
const publicKey = ref<string | null>(null); // 用于存储公钥

const visible = ref(false)

onMounted(async () => {
    try {
        publicKey.value = await getPublicKey();
    } catch (error) {
        console.error("获取公钥失败:", error);
        ElMessage.error("获取公钥失败");
    }
});

// 密码验证规则
const validatePassword = (rule: any, value: string | undefined, callback: any) => {
    if (!value || value.trim() === '') {
        callback(new Error('请输入密码'));
    } else if (value.length < 6) {
        callback(new Error('密码长度不能少于6位'));
    }
    else {
        if(value === modifyPwdForm.oldPassword){
            callback(new Error('新密码不能与旧密码相同'));
            return;
        }
        // Check if confirm password matches
        if (modifyPwdForm.confirmPassword && modifyPwdForm.confirmPassword !== value) {
            // Trigger validation of confirm password field
            modifyFormRef.value?.validateField('confirmPassword');
        }
        callback();
    }
};

const validateConfirmPassword = (rule: any, value: string | undefined, callback: any) => {
    if (!value || value.trim() === '') {
        callback(new Error('请再次输入密码'));
    } else if (value !== modifyPwdForm.newPassword) {
        callback(new Error('两次输入密码不一致'));
    } else {
        callback();
    }
};

const modifyRules = reactive({
    oldPassword: [
        {
            required: true,
            message: '请输入旧密码',
            trigger: ['blur', 'change']  // 同时监听变化和失去焦点
        }
    ],
    newPassword: [
        {
            required: true,
            validator: validatePassword,
            trigger: ['blur', 'change']
        }
    ],
    confirmPassword: [
        {
            required: true,
            validator: validateConfirmPassword,
            trigger: ['blur', 'change']
        }
    ]
});

const open = () => {
    modifyPwdForm.oldPassword = ''
    modifyPwdForm.newPassword = ''
    modifyPwdForm.confirmPassword = ''
    visible.value = true
}

const close = () => {
    visible.value = false
}

const confirmModify = () => {
    modifyFormRef.value?.validate(async (valid: boolean) => {
        if (valid) {
            // 提交修改密码请求
            if(await modifyPassword({
                oldPassword: encryptPassword(publicKey.value!, modifyPwdForm.oldPassword),
                newPassword: encryptPassword(publicKey.value!, modifyPwdForm.newPassword)
            })){
                visible.value = false
                ElMessageBox.alert(
                    '修改密码成功，请重新登录',
                    '信息',
                    {
                        confirmButtonText: '确认',
                        type: 'info',
                    }
                ).then(async () => {
                    logout()
                });
            }
        }
    })
}

// 暴露方法给父组件
defineExpose({
    open,
    close
})
</script>