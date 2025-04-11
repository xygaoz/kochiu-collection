import { Category } from "@/apis/interface";
import httpInstance from "@/apis/utils";

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

export const getAllCategory = (): Promise<Category[]> => {
    return httpInstance.get("/category/all").then((model: any) => {
        if (model) {
            return model as Category[];
        }
        return [];
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return [];
    });
}

export const updateCategory = (params: any): Promise<any> => {
    return httpInstance.post("/category/update", params).then((model: any) => {
        if (model) {
            console.log("修改成功:", model);
            return model;
        }
    });
}

export const createCategory = (params: any): Promise<any> => {
    return httpInstance.post("/category/add", params).then((model: any) => {
        if (model) {
            console.log("新增成功:", model);
            return model;
        }
    });
}