import { Tag } from "@/apis/interface";
import httpInstance from "@/utils/utils";

const tagApi = "/tag";
export const listTag = async (): Promise<Tag[]> => {
    return httpInstance.get(tagApi + "/list").then((model: any) => {
        if (model) {
            return model as Tag[];
        }
        return [];
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return [];
    });
}

export const getAllTag = async (): Promise<Tag[]> => {
    return httpInstance.get(tagApi + "/all").then((model: any) => {
        if (model) {
            return model as Tag[];
        }
        return [];
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return [];
    });
}

export const getTag = async (tagId: string): Promise<Tag> => {
    return httpInstance.get(tagApi + "/get/" + tagId).then((model: any) => {
        if (model) {
            return model as Tag;
        }
        return {tagId: 0, tagName: ""};
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return {tagId: 0, tagName: ""};
    });
}
