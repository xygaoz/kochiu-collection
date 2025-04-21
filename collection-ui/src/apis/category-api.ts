import { Category } from "@/apis/interface";
import httpInstance from "@/apis/utils";

const categoryApi = '/category'
export const listCategory = (): Promise<Category[]> => {
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

export const getAllCategory = (): Promise<Category[]> => {
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

export const getCategory = (sno: number): Promise<Category> => {
    return httpInstance.get(categoryApi + "/get/" + sno).then((model: any) => {
        if (model) {
            return model as Category;
        }
        return {sno: 0, cateName: "", cateId: 0};
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return {sno: 0, cateName: "", cateId: 0};
    });
}

export const updateCategory = (params: any): Promise<any> => {
    return httpInstance.post(categoryApi + "/update", params).then((model: any) => {
        if (model) {
            console.log("修改成功:", model);
            return model;
        }
    });
}

export const deleteCategory = (params: any): Promise<any> => {
    return httpInstance.post(categoryApi + "/remove", params).then((model: any) => {
        if (model) {
            console.log("删除成功:", model);
            return model;
        }
    });
}

export const createCategory = (params: any): Promise<any> => {
    return httpInstance.post(categoryApi + "/add", params).then((model: any) => {
        if (model) {
            console.log("新增成功:", model);
            return model;
        }
    });
}