import { Catalog, PathVo } from "@/apis/interface";
import httpInstance from "@/apis/utils";

export const getCatalogTree = (): Promise<Catalog[]> => {
    return httpInstance.get("/catalog/tree").then((model: any) => {
        if (model) {
            return model as Catalog[];
        }
        return [];
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return [];
    });
}

export const addCatalog = (data: { folderName: string, parentId?: number }) => {
    return httpInstance.post("/catalog", data);
}

export const getCatalogPath = async (sno: string): Promise<PathVo> => {
    return httpInstance.get("/catalog/path/" + sno).then((model: any) => {
        if (model) {
            return model as PathVo;
        }
        return { path: "/", pathInfo: [] }; // 返回默认对象而不是空字符串
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return { path: "/", pathInfo: [] }; // 返回默认对象而不是空字符串
    });
}
