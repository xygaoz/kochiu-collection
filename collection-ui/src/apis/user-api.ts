import { Menu, PageInfo, Strategy, User } from "@/apis/interface";
import httpInstance from "@/apis/utils";
import { tokenStore } from "@/apis/system-api";
import Cookies from "js-cookie";
import { useUserStore } from "@/apis/global";

const userApi = "/user";
export const listUsers = (params: any): Promise<PageInfo<User>> => {
    return httpInstance.post(userApi + "/list", params).then((model: any) => {
        if (model) {
            console.log("获取用户列表成功:", model);
            return model as PageInfo<User>;
        }
        return { pageNum: 0, pageSize: 0, total: 0, pages: 0, list: [] };
    }).catch((error) => {
        console.error("获取用户失败:", error);
        return { pageNum: 0, pageSize: 0, total: 0, pages: 0, list: [] };
    });
}

export const addUser = (params: any): Promise<boolean> => {
    return httpInstance.post(userApi + "/add", params).then((model: any) => {
        if (model) {
            console.log("添加用户成功:", model);
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("添加用户失败:", error);
        return false;
    });
}

export const updateUser = (params: any): Promise<boolean> => {
    return httpInstance.post(userApi + "/update", params).then((model: any) => {
        if (model) {
            console.log("更新用户成功:", model);
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("更新用户失败:", error);
        return false;
    });
}

export const deleteUser = (params: any): Promise<boolean> => {
    return httpInstance.post(userApi + "/delete", params).then((model: any) => {
        if (model) {
            console.log("更新用户成功:", model);
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("更新用户失败:", error);
        return false;
    });
}

export const resetPwd = (params: any): Promise<boolean> => {
    return httpInstance.post(userApi + "/resetpwd", params).then((model: any) => {
        if (model) {
            console.log("重置用户密码成功:", model);
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("重置用户密码失败:", error);
        return false;
    });
}

export const enableOrDisable = (params: any): Promise<boolean> => {
    return httpInstance.post(userApi + "/enable-disable", params).then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        return false;
    });
}

export const getMyMenu = async (): Promise<Menu[]> => {
    return httpInstance.get(userApi + "/my-menu").then((model: any) => {
        if (model) {
            // 确保返回值始终是 Menu[]
            return model as Menu[];
        }
        return [];
    }).catch((error) => {
        console.error("获取用户菜单失败:", error);
        return [];
    });
}

export const getMyInfo = async (): Promise<User | null> => {
    return httpInstance.get(userApi + "/my-info").then((model: any) => {
        if (model) {
            return model as User;
        }
        return null;
    }).catch((error) => {
        console.error("获取用户失败:", error);
        return null;
    });
}

export const setMyName = async (name: string): Promise<boolean> => {
    return httpInstance.post(userApi + "/set-my-name", {
        userName: name
    }).then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("获取用户失败:", error);
        return false;
    });
}

export const logout = () => {
    // 清除token和用户状态
    tokenStore.removeToken()
    Cookies.remove('refresh_token')
    const userStore = useUserStore()
    userStore.clearCurrentUser()

    // 跳转到登录页
    window.location.href = process.env.VUE_APP_CONTEXT_PATH + '/login' // 使用完整刷新确保状态清除
}

export const resetKey = async (): Promise<boolean> => {
    return httpInstance.get(userApi + "/reset-key").then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("获取重置key失败:", error);
        return false;
    });
}

export const resetToken = async (): Promise<boolean> => {
    return httpInstance.get(userApi + "/reset-token").then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("获取重置token失败:", error);
        return false;
    });
}
