import { PageInfo, User } from "@/apis/interface";
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
