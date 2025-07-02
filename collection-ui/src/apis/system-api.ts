import httpInstance from "@/utils/utils";
import { loading } from "@/utils/utils";
import { Keys, LoginInfo, ResourceType, Strategy, SysProperty } from "@/apis/interface";

const sysApi = "/sys";
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
export const getPublicKey = async(): Promise<string | null> => {
    return httpInstance.get(sysApi + "/publicKey").then((model: any) => {
        if (model) {
            console.log("获取公钥成功:", model);
            return model as string;
        }
        return null;
    }).catch((error) => {
        console.error("获取公钥失败:", error);
        throw error; // 抛出错误以便调用者处理
    });
};

// 登录
export const loginService = async (loginForm: any): Promise<LoginInfo | null> => {
    const ld = loading("登录中");
    return httpInstance.post(sysApi + "/login", loginForm).then((model: any) => {
        if (model) {
            console.log("登录成功，获取token:", model);
            return model as LoginInfo;
        }
        return null;
    }).catch((error) => {
        console.error("登录失败:", error);
        throw error; // 抛出错误以便调用者处理
    }).finally(() => {
        ld.close();
    });
};

export const getResourceTypes = async (): Promise<ResourceType[]> => {
    return httpInstance.get(sysApi + "/resourceTypes").then((model) => {
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
    return httpInstance.get(sysApi + "/resourceType/get/" + typeName).then((model: any) => {
        if (model) {
            return model as ResourceType;
        }
        return { label: "未知", value: "unknown" } as ResourceType;
    }).catch((error) => {
        console.error("获取资源类型失败:", error);
        return { label: "未知", value: "unknown" } as ResourceType;
    });
}

export const testServerPath = async (path: string, importMethod: number): Promise<boolean> => {
    const params = {
        path: path,
        importMethod: importMethod
    };
    return httpInstance.post(sysApi + "/testServerPath", params).then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("测试服务器路径失败:", error);
        return false;
    });
}

export const getStrategyList = async (): Promise<Strategy[]> => {
    return httpInstance.get(sysApi + "/strategy/list").then((model) => {
        if (model) {
            // 确保返回值始终是 Strategy[]
            return Array.isArray(model) ? model : [];
        }
        return [];
    }).catch((error) => {
        console.error("获取资源类型失败:", error);
        return [];
    });
}

export const updateStrategy = async (params: any): Promise<boolean> => {
    return httpInstance.post(sysApi + "/strategy/update", params).then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("更新策略失败:", error);
        return false;
    });
}

export const checkServerPath = async (): Promise<boolean> => {
    return httpInstance.get(sysApi + "/strategy/check-local").then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("添加策略失败:", error);
        return false;
    });
}

export const clearAllData = async (password: string | undefined): Promise<boolean> => {
    return httpInstance.post(sysApi + "/test/clear",  {
        password: password
    }).then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("清除数据失败:", error);
        return false;
    });
}

export const resetRSAKeys = async (): Promise<boolean> => {
    return httpInstance.get(sysApi + "/reset-rsa-keys").then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("添加策略失败:", error);
        return false;
    });
}

export const resetEncryptKey = async (): Promise<boolean> => {
    return httpInstance.get(sysApi + "/reset-key").then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("添加策略失败:", error);
        return false;
    });
}

export const loadCurrentKeys = async (): Promise<Keys | null> => {
    return httpInstance.get(sysApi + "/load-key").then((model: any) => {
        if (model) {
            return model as Keys;
        }
        return null;
    }).catch((error) => {
        console.error("添加策略失败:", error);
        return null;
    });
}

export const getSysConfig = async (): Promise<SysProperty> => {
    return httpInstance.get(sysApi + "/sys-config").then((model: any) => {
        if (model) {
            return model as SysProperty;
        }
        return {uploadMaxSize: '500MB'};
    }).catch((error) => {
        console.error("获取系统配置失败:", error);
        return {uploadMaxSize: '500MB'};
    });
}

export const setSysConfig = async (params: any): Promise<boolean> => {
    return httpInstance.post(sysApi + "/set-sys-config", params).then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("设置系统配置失败:", error);
        return false;
    });
}