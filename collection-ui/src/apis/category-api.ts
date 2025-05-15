import { Category } from "@/apis/interface";
import httpInstance from "@/utils/utils";

const categoryApi = '/category'
export const listCategory = async (): Promise<Category[]> => {
    return httpInstance.get(categoryApi + "/list").then((model: any) => {
        if (model) {
            return model as Category[];
        }
        return [];
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return [];
    });
}

export const getAllCategory = async (): Promise<Category[]> => {
    return httpInstance.get(categoryApi + "/all").then((model: any) => {
        if (model) {
            return model as Category[];
        }
        return [];
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return [];
    });
}

export const getCategory = async (id: string): Promise<Category> => {
    return httpInstance.get(categoryApi + "/get/" + id).then((model: any) => {
        if (model) {
            return model as Category;
        }
        return {sno: 0, cateName: "", cateId: 0};
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return {sno: 0, cateName: "", cateId: 0};
    });
}

export const updateCategory = async (params: any): Promise<any> => {
    return httpInstance.post(categoryApi + "/update", params).then((model: any) => {
        if (model) {
            console.log("修改成功:", model);
            return model;
        }
    });
}

export const deleteCategory = async (params: any): Promise<any> => {
    return httpInstance.post(categoryApi + "/remove", params).then((model: any) => {
        if (model) {
            console.log("删除成功:", model);
            return model;
        }
    });
}

export const createCategory = async (params: any): Promise<any> => {
    return httpInstance.post(categoryApi + "/add", params).then((model: any) => {
        if (model) {
            console.log("新增成功:", model);
            return model;
        }
    });
}