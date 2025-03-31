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
                <el-form-item label="邮箱" prop="email">
                    <el-input
                        v-model="loginForm.email"
                        placeholder="请输入邮箱"
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
                <div style="text-align: right; transform: translate(0, 30px)">
                    <el-link
                        type="danger"
                        @click="changeUrl('/forget')"
                        style="margin-right: 140px"
                    >
                        忘记密码？
                    </el-link>

                    <el-link type="warning" @click="changeUrl('/register')"
                    >没有账号？去注册</el-link
                    >
                </div>
            </el-form>
        </div>
    </div>
</template>

<script setup>
import { ref, defineEmits, onMounted } from "vue"; // 添加onMounted
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router"; // 导入useRouter
import httpInstance from "@/apis/utils"; // 导入queryPublicKey方法

const loginForm = ref({
    email: "",
    password: "",
});

const rules = {
    email: [
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

const login = async () => {
    // 添加表单验证逻辑
    loginFormRef.value.validate(async (valid) => {
        if (valid) {
            console.log("发送请求");
            // const res = await loginService(loginForm.value);
            // if (res) {
            //     userStore.setInfo(res.data);
            //     tokenStore.setToken(res.data.token);
            // }

            ElMessage.success("登录成功!");
            emit('login-success');
        } else {
                ElMessage.error("请输入用户名或密码");
        }
    });
};

const changeUrl = (url) => {
    router.replace(url);
};

// 组件挂载时调用queryPublicKey方法
onMounted(() => {
    httpInstance.get("/publicKey").then((model) => {
        if(model) {
            const publicKeyModel = model; // 将model放到一个变量里，后续使用
            console.log("获取公钥成功:", publicKeyModel)
        }
    }).catch((error) => {
        console.error("获取公钥失败:", error);
        ElMessage.error("获取公钥失败，请重试");
    });
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
    background: rgba(255, 204, 255, 0.5);
    /*背景颜色为黑色，透明度为0.8*/
    box-sizing: border-box;
    /*box-sizing设置盒子模型的解析模式为怪异盒模型，
      将border和padding划归到width范围内*/
    box-shadow: 0px 15px 25px rgba(0, 0, 0, 0.5);
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
</style>
