import { Tag } from "@/apis/interface";
import httpInstance from "@/apis/utils";

export const listTag = (): Promise<Tag[]> => {
    return httpInstance.get("/tag/list").then((model: any) => {
        if (model) {
            return model as Tag[];
        }
        return [];
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return [];
    });
}
