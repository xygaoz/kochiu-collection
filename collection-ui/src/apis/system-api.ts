import httpInstance from "@/apis/utils"; // 导入httpInstance
import { loading } from "@/apis/utils";
import { ResourceType } from "@/apis/interface";

export const tokenStore = {
    setToken(token: string, expirySeconds: number) {
        const item = {
            value: token,
            expiry: Date.now() + expirySeconds * 1000
        };
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
    return httpInstance.get("/sys/publicKey").then((model) => {
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
    return httpInstance.post("/sys/login", loginForm).then((model) => {
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

export const getResourceTypes = async (): Promise<ResourceType[]> => {
    return httpInstance.get("/sys/resourceTypes").then((model) => {
        if (model) {
            // 确保返回值始终是 ResourceType[]
            return Array.isArray(model) ? model : [];
        }
        return [];
    }).catch((error) => {
        console.error("获取资源类型失败:", error);
        return [];
    });
}

export const getResourceType = async (typeName: string): Promise<ResourceType> => {
    return httpInstance.get("/sys/resourceType/get/" + typeName).then((model: any) => {
        if (model) {
            return model as ResourceType;
        }
        return { label: "未知", value: "unknown" } as ResourceType;
    }).catch((error) => {
        console.error("获取资源类型失败:", error);
        return { label: "未知", value: "unknown" } as ResourceType;
    });
}

export const testServerPath = async (path: string, importMethod: string): Promise<boolean> => {
    const params = {
        path: path,
        importMethod: importMethod
    };
    return httpInstance.post("/sys/testServerPath", params).then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("测试服务器路径失败:", error);
        return false;
    });
}

