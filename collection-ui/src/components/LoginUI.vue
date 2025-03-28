<template>
    <div class="login-container">
        <h2>KoChiu Collection</h2>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef"
                 label-width="100px" class="login-form" label-position="top"
                style="width: 400px" size="large"
        >
            <el-form-item label="用户名" prop="username">
                <el-input v-model="loginForm.username"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
                <el-input type="password" v-model="loginForm.password"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="submitForm">登录</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, nextTick } from "vue"
import { ElMessage, ElForm } from "element-plus" // 添加 ElForm 类型导入
import router from "@/apis/base-routes"

export default defineComponent({
    setup() {
        // 表单数据
        const loginForm = reactive({
            username: "",
            password: ""
        })

        // 明确声明表单 Ref 类型
        const loginFormRef = ref<InstanceType<typeof ElForm>>()

        // 验证规则
        const rules = reactive({
            username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
            password: [{ required: true, message: "请输入密码", trigger: "blur" }]
        })

        // 提交逻辑
        const submitForm = async () => {
            await nextTick() // 确保 DOM 更新完成
            if (!loginFormRef.value) return

            loginFormRef.value.validate((valid: boolean) => {
                alert(valid)
                if (valid) {
                    ElMessage.success("登录成功")
                    router.push({ name: "MainUI" })
                } else {
                    ElMessage.error("请输入正确的用户名和密码")
                }
            })
        }

        return { loginForm, rules, submitForm, loginFormRef }
    }
})
</script>

<style scoped>
.login-container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f0f2f5;
}

.login-container h2 {
    margin-bottom: 20px;
    text-align: center;
}

.login-form {
    width: 300px;
    padding: 20px;
    background-color: #fff;
    border-radius: 5px;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
    margin-bottom: 20px; /* 添加底部间距 */
}
</style>