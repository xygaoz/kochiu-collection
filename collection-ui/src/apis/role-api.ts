import { Role } from "@/apis/interface";
import httpInstance from "@/utils/utils";

const roleApi = "/role";

export const listRoles = async (): Promise<Role[]> => {
    return httpInstance.get(roleApi + "/list").then((model: any) => {
        if (model) {
            return model as Role[];
        }
        return [];
    }).catch((error) => {
        console.error("获取角色列表失败:", error);
        return [];
    });
}

export const addRole = async (role: any): Promise<boolean> => {
    return httpInstance.post(roleApi + "/add", role).then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("新增角色失败:", error);
        return false;
    });
}

export const updateRole = async (role: any): Promise<boolean> => {
    return httpInstance.post(roleApi + "/update", role).then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("更新角色失败:", error);
        return false;
    });
}

export const deleteRole = async (roleId: string): Promise<boolean> => {
    return httpInstance.get(roleApi + "/delete/" + roleId).then((model: any) => {
        if (model) {
            return model as boolean;
        }
        return false;
    }).catch((error) => {
        console.error("删除角色失败:", error);
        return false;
    });
}