import { Catalog } from "@/apis/interface";
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

export const getCatalogPath = async (sno: string): Promise<string> => {
    return httpInstance.get("/catalog/path/" + sno).then((model: any) => {
        if (model) {
            return model as string;
        }
        return "";
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return "";
    });
}
