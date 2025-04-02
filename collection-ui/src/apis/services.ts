import httpInstance from "@/apis/utils"; // 导入httpInstance
import { Category } from "@/apis/interface"; // 导入Category接口
import { loading } from "@/apis/utils";

export const tokenStore = {
    setToken(token: string, expirySeconds: number) {
        const item = {
            value: token,
            expiry: Date.now() + expirySeconds * 1000
        };``
        localStorage.setItem('token', JSON.stringify(item));
    },
    getToken(): string | null {
        const key = 'token';
        const itemStr = localStorage.getItem(key);
        if (!itemStr) return null;

        const item = JSON.parse(itemStr);
        if (Date.now() > item.expiry) {
            localStorage.removeItem(key); // 过期删除
            return null;
        }
        return item.value;
    },
    removeToken() {
        localStorage.removeItem('token');
    }
};

// 创建一个方法来获取公钥
export const getPublicKey = () => {
    return httpInstance.get("/publicKey").then((model) => {
        if (model) {
            console.log("获取公钥成功:", model);
            return model;
        }
    }).catch((error) => {
        console.error("获取公钥失败:", error);
        throw error; // 抛出错误以便调用者处理
    });
};

// 登录
export const loginService = (loginForm: any) => {
    const ld = loading("登录中");
    return httpInstance.post("/login", loginForm, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded' // 设置请求头为form-data
        }
    }).then((model) => {
        if (model) {
            console.log("登录成功，获取token:", model);
            return model;
        }
    }).catch((error) => {
        console.error("登录失败:", error);
        throw error; // 抛出错误以便调用者处理
    }).finally(() => {
        ld.close();
    });
};

export const listCategory = (): Promise<Category[]> => {
    return httpInstance.get("/category/list").then((model: any) => {
        if (model) {
            return model as Category[];
        }
        return [];
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return [];
    });
}
