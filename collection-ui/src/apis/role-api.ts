import { Role } from "@/apis/interface";
import httpInstance from "@/apis/utils";

const userApi = "/role";
export const listRoles = (): Promise<Role[]> => {
    return httpInstance.get(userApi + "/list").then((model: any) => {
        if (model) {
            console.log("获取角色列表成功:", model);
            return model as Role[];
        }
        return [];
    }).catch((error) => {
        console.error("获取用户失败:", error);
        return [];
    });
}
