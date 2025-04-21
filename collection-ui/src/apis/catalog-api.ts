import { Catalog, PathVo } from "@/apis/interface";
import httpInstance from "@/apis/utils";

const catalogApi = '/catalog'
export const getCatalogTree = (): Promise<Catalog[]> => {
    return httpInstance.get(catalogApi + "/tree").then((model: any) => {
        if (model) {
            return model as Catalog[];
        }
        return [];
    }).catch((error) => {
        console.error("获取目录失败:", error);
        return [];
    });
}

export const addCatalog = (data: { cataName: string, parentId?: number }): Promise<boolean> => {

    return httpInstance.post(catalogApi + "/add", data).then((model: any) => {
        return !!model;
    }).catch((error) => {
        console.error("新建目录失败:", error);
        return false;
    })
}

export const getCatalogPath = async (sno: string): Promise<PathVo> => {
    return httpInstance.get(catalogApi + "/path/" + sno).then((model: any) => {
        if (model) {
            return model as PathVo;
        }
        return { path: "/", pathInfo: [] }; // 返回默认对象而不是空字符串
    }).catch((error) => {
        console.error("获取目录失败:", error);
        return { path: "/", pathInfo: [] }; // 返回默认对象而不是空字符串
    });
}

export const updateCatalog = (data: { cateId: number; cataName: string; parentId?: number }) => {
    return httpInstance.post(catalogApi + "/update", data).then((model: any) => {
        return !!model;
    }).catch((error) => {
        console.error("修改/移动目录失败:", error);
        return false;
    })
};

export const deleteCatalog = (data: {cateId: number, newParentId: number, removeType: number}) => {
    return httpInstance.post(catalogApi + "/remove", data).then((model: any) => {
        return !!model;
    }).catch((error) => {
        console.error("删除目录失败:", error);
        return false;
    })
};