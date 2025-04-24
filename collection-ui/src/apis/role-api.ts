import { Role } from "@/apis/interface";
import httpInstance from "@/apis/utils";

const roleApi = "/role";

export const listRoles = (): Promise<Role[]> => {
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

export const addRole = (role: any): Promise<boolean> => {
    return httpInstance.post(roleApi + "/add", role).then(() => {
        return true;
    }).catch((error) => {
        console.error("新增角色失败:", error);
        return false;
    });
}

export const updateRole = (role: any): Promise<boolean> => {
    return httpInstance.post(roleApi + "/update", role).then(() => {
        return true;
    }).catch((error) => {
        console.error("更新角色失败:", error);
        return false;
    });
}

export const deleteRole = (roleId: string): Promise<boolean> => {
    return httpInstance.post(roleApi + "/delete", { roleId }).then(() => {
        return true;
    }).catch((error) => {
        console.error("删除角色失败:", error);
        return false;
    });
}