<template>
    <div class="login">
        <div class="loginPart">
            <h2>KoChiu Collection</h2>
            <el-form
                :model="loginForm"
                ref="loginFormRef"
                :rules="rules"
                label-width="100px"
                style="transform: translate(-30px)"
            >
                <el-form-item label="用户名" prop="username">
                    <el-input
                        v-model="loginForm.username"
                        placeholder="请输入用户名"
                        clearable
                    ></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input
                        type="password"
                        v-model="loginForm.password"
                        placeholder="请输入密码"
                        show-password
                        clearable
                    ></el-input>
                </el-form-item>

                <el-button
                    class="btn"
                    type="primary"
                    @click="login"
                    auto-insert-space
                    @keyup.enter="login"
                >登录</el-button
                >
<!--                <div style="text-align: right; transform: translate(0, 30px)">-->
<!--                    <el-link-->
<!--                        type="danger"-->
<!--                        @click="changeUrl('/forget')"-->
<!--                        style="margin-right: 140px"-->
<!--                    >-->
<!--                        忘记密码？-->
<!--                    </el-link>-->

<!--                    <el-link type="warning" @click="changeUrl('/register')"-->
<!--                    >没有账号？去注册</el-link-->
<!--                    >-->
<!--                </div>-->
            </el-form>
        </div>
    </div>
</template>

<script setup>
import { ref, defineEmits, onMounted } from "vue"; // 添加onMounted
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router"; // 导入useRouter
import { getPublicKey, loginService, tokenStore } from "@/apis/system-api"; // 导入getPublicKey方法
import Cookies from 'js-cookie';
import { encryptPassword } from "@/utils/utils";
import { useUserStore } from "@/utils/global";

const loginForm = ref({
    username: "",
    password: "",
});

const rules = {
    username: [
        { required: true, message: "请输入用户名", trigger: "blur" },
    ],
    password: [
        {
            required: true,
            message: "请输入密码",
            trigger: "blur",
        },
    ],
};

const emit = defineEmits(['login-success']);
const router = useRouter(); // 使用useRouter
const loginFormRef = ref(null); // 添加表单引用
let publicKey = ""; // 用于存储公钥

const login = async () => {
    // 添加表单验证逻辑
    loginFormRef.value.validate(async (valid) => {
        if (valid) {

            const encryptedPassword = encryptPassword(publicKey, loginForm.value.password); // 加密密码
            console.log("加密后的密码：", encryptedPassword)
            if (!encryptedPassword) {
                ElMessage.error('加密失败');
                return
            }
            const res = await loginService({ ...loginForm.value, password: encryptedPassword });
            if (res) {
                // 登录成功后初始化用户状态
                const userStore = useUserStore();
                userStore.initializeUser(res.userCode, res.userName, res.userId);
                userStore.setPermissions(res.permissions)
                userStore.setCanDel(res.canDel);
                if(res.token) {
                    tokenStore.setToken(res.token, res.expirySeconds); // 保存token
                }
                if(res.refreshToken){
                    Cookies.set('refresh_token', res.refreshToken, { expires: 7, path: '/' });
                }

                ElMessage.success("登录成功!");
                emit('login-success');
            }
        } else {
                ElMessage.error("请输入用户名或密码");
        }
    });
};

// 组件挂载时调用getPublicKey方法
onMounted(async () => {
    try {
        publicKey = await getPublicKey(); // 调用getPublicKey方法
    } catch (error) {
        ElMessage.error("获取公钥失败，请重试");
    }
});

</script>

<style lang="scss" scoped>
.login {
    height: 100%;
    width: 100%;
    overflow: hidden;
    background-size: cover;
    background-repeat: no-repeat;
    background-image: url("../assets/imgs/loginbg.png");
    opacity: 0.9;
    position: fixed;
}

.loginPart {
    position: absolute;
    /*定位方式绝对定位absolute*/
    top: 50%;
    left: 80%;
    /*顶和高同时设置50%实现的是同时水平垂直居中效果*/
    transform: translate(-50%, -50%);
    /*实现块元素百分比下居中*/
    width: 450px;
    padding: 50px;
    background: rgba(255, 204, 255, 0.2);
    /*背景颜色为黑色，透明度为0.8*/
    box-sizing: border-box;
    /*box-sizing设置盒子模型的解析模式为怪异盒模型，
      将border和padding划归到width范围内*/
    box-shadow: 0 15px 25px rgba(0, 0, 0, 0.5);
    /*边框阴影  水平阴影0 垂直阴影15px 模糊25px 颜色黑色透明度0.5*/
    border-radius: 15px;
    /*边框圆角，四个角均为15px*/
}

h2 {
    margin: 0 0 30px;
    padding: 0;
    color: #fff;
    text-align: center;
    /*文字居中*/
}

.btn {
    transform: translate(170px);
    width: 80px;
    height: 40px;
    font-size: 15px;
}

:deep(.el-input__wrapper) {
    background-color: transparent !important;
    box-shadow: none !important;
    padding: 0;
}

/* 可选：添加边框样式使输入框更可见 */
:deep(.el-input__inner) {
    background-color: transparent !important;
    border: 1px solid rgba(255, 255, 255, 0.3) !important;
    color: #252525 !important;
    border-radius: 4px;
    padding: 0 15px;
    width: 280px;
    padding-right: 30px !important; /* 为图标留出空间 */
}

/* 鼠标悬停和聚焦时的样式 */
:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
    box-shadow: none !important;
    border-color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-form-item__error) {
    color: red;
}

:deep(.el-input) {
    position: relative;
    width: 280px; /* 与输入框宽度一致 */
}

:deep(.el-input__clear),
:deep(.el-input__password) {
    position: absolute;
    right: 5px;
    top: 50%;
    transform: translateY(-50%);
    z-index: 2;
}

/* 调整密码输入框的图标位置 */
:deep(.el-input__suffix) {
    right: 5px;
}

</style>
