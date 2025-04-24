import httpInstance from "@/apis/utils";
import { Module } from "@/apis/interface";

const moduleApi = "/module";
export const listModulesWithActions = (): Promise<Module[]> => {
    return httpInstance.get(moduleApi + "/with-actions").then((res: any) => {
        if (res) {
            return res as Module[];
        }
        return [];
    }).catch((error) => {
        console.error("获取模块列表失败:", error);
        return [];
    });
}