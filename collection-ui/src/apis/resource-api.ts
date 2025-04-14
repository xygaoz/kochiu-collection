
// 新增上传文件的方法
import httpInstance, { loading } from "@/apis/utils";
import { PageInfo, Resource, Tag } from "@/apis/interface";

export const uploadFile = (file: File, categorySno: string, overwrite: string): Promise<any> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('categoryId', categorySno);
    formData.append('overwrite', overwrite);

    const ld = loading("上传中")
    return httpInstance.post("/resource/upload", formData).then((model: any) => {
        if (model) {
            console.log("文件上传成功:", model);
            return model;
        }
        return null;
    }).catch((error) => {
        console.error("文件上传失败:", error);
        throw error; // 抛出错误以便调用者处理
    }).finally(() => {
        ld.close();
    });
}

export const listCategoryFiles = (cateId: string, page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
    const ld = loading("加载中")
    // 合并分页参数和其他参数
    const requestParams: { [key: string]: any } = {
        pageNum: page,
        pageSize: size
    };
    Object.keys(params).forEach(key => {
        requestParams[key] = params[key];
    });
    return httpInstance.post("/resource/category/" + cateId, requestParams).then((model: any) => {
        if (model) {
            console.log("获取文件列表成功:", model);
            return model as PageInfo<Resource>;
        }
        return { pageNum: 0, pageSize: 0, total: 0, pages: 0, list: [] };
    }).catch((error) => {
        console.error("获取文件列表失败:", error);
        return { pageNum: 0, pageSize: 0, total: 0, pages: 0, list: [] };
    }).finally(() => {
        ld.close();
    });
}

export const updateResource = (resourceId: number, params: any): Promise<boolean> => {
    const ld = loading("修改中")
    params["resourceId"] = resourceId;
    return httpInstance.post("/resource/updateInfo", params).then((model: any) => {
        console.log("修改成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const bacthUpdateResource = (resourceIds: number[], params: any): Promise<boolean> => {
    params["resourceIds"] = resourceIds;
    return httpInstance.post("/resource/batchUpdate", params).then((model: any) => {
        console.log("修改成功:", model);
        return model;
    })
}

export const addResourceTag = (resourceId: number, params: any): Promise<Tag> => {
    params["resourceId"] = resourceId;
    return httpInstance.post("/resource/addTag", params).then((model: any) => {
        return model;
    });
}

export const removeResourceTag = (resourceId: number, params: any): Promise<any> => {
    params["resourceId"] = resourceId;
    return httpInstance.post("/resource/removeTag", params).then((model: any) => {
        return model;
    });
}


export const batchAddTag = (resourceIds: number[], params: any): Promise<any> => {
    params["resourceIds"] = resourceIds;
    return httpInstance.post("/resource/batchAddTag", params).then((model: any) => {
        return model;
    });
}

export const batchRemoveTag = (resourceIds: number[], params: any): Promise<any> => {
    params["resourceIds"] = resourceIds;
    return httpInstance.post("/resource/batchRemoveTag", params).then((model: any) => {
        return model;
    });
}

export const listTagFiles = (tagId: string, page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
    const ld = loading("加载中")
    // 合并分页参数和其他参数
    const requestParams: { [key: string]: any } = {
        pageNum: page,
        pageSize: size
    };
    Object.keys(params).forEach(key => {
        requestParams[key] = params[key];
    });
    return httpInstance.post("/resource/tag/" + tagId, requestParams).then((model: any) => {
        if (model) {
            console.log("获取文件列表成功:", model);
            return model as PageInfo<Resource>;
        }
        return { pageNum: 0, pageSize: 0, total: 0, pages: 0, list: [] };
    }).catch((error) => {
        console.error("获取文件列表失败:", error);
        return { pageNum: 0, pageSize: 0, total: 0, pages: 0, list: [] };
    }).finally(() => {
        ld.close();
    });
}

export const listTypeFiles = (typeName: string, page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
    const ld = loading("加载中")
    // 合并分页参数和其他参数
    const requestParams: { [key: string]: any } = {
        pageNum: page,
        pageSize: size
    };
    Object.keys(params).forEach(key => {
        requestParams[key] = params[key];
    });
    return httpInstance.post("/resource/type/" + typeName, requestParams).then((model: any) => {
        if (model) {
            console.log("获取文件列表成功:", model);
            return model as PageInfo<Resource>;
        }
        return { pageNum: 0, pageSize: 0, total: 0, pages: 0, list: [] };
    }).catch((error) => {
        console.error("获取文件列表失败:", error);
        return { pageNum: 0, pageSize: 0, total: 0, pages: 0, list: [] };
    }).finally(() => {
        ld.close();
    });
}

export const moveToCategory = (resourceIds: number[], params: any): Promise<boolean> => {
    const ld = loading("修改中")
    params["resourceIds"] = resourceIds;
    return httpInstance.post("/resource/moveToCategory", params).then((model: any) => {
        console.log("修改成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const moveToRecycle = (resourceIds: number[], params: any): Promise<boolean> => {
    const ld = loading("移动中")
    params["resourceIds"] = resourceIds;
    return httpInstance.post("/resource/moveToRecycle", params).then((model: any) => {
        console.log("移动成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const restoreFormRecycle = (resourceIds: number[], params: any): Promise<boolean> => {
    const ld = loading("恢复中")
    params["resourceIds"] = resourceIds;
    return httpInstance.post("/resource/restore", params).then((model: any) => {
        console.log("恢复成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const listRecycleFiles = (page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
    const ld = loading("加载中")
    // 合并分页参数和其他参数
    const requestParams: { [key: string]: any } = {
        pageNum: page,
        pageSize: size
    };
    Object.keys(params).forEach(key => {
        requestParams[key] = params[key];
    });
    return httpInstance.post("/resource/recycle", requestParams).then((model: any) => {
        if (model) {
            console.log("获取文件列表成功:", model);
            return model as PageInfo<Resource>;
        }
        return { pageNum: 0, pageSize: 0, total: 0, pages: 0, list: [] };
    }).catch((error) => {
        console.error("获取文件列表失败:", error);
        return { pageNum: 0, pageSize: 0, total: 0, pages: 0, list: [] };
    }).finally(() => {
        ld.close();
    });
}