import { Menu, PageInfo, Strategy, User } from "@/apis/interface";
import httpInstance from "@/apis/utils";

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
    return httpInstance.get(userApi + "/my-menu").then((model) => {
        if (model) {
            // 确保返回值始终是 Menu[]
            return Array.isArray(model) ? model : [];
        }
        return [];
    }).catch((error) => {
        console.error("获取资源类型失败:", error);
        return [];
    });
}