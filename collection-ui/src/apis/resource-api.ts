
// 新增上传文件的方法
import httpInstance, { loading } from "@/apis/utils";
import { PageInfo, Resource, Tag } from "@/apis/interface";
import type { AxiosProgressEvent } from 'axios'

const resourceApi = "/resource";
export const uploadFile = (
    file: File,
    categoryId: number,
    overwrite: string,
    cataId: number | null,
    autoCreate: boolean,
    onProgress?: (progressEvent: AxiosProgressEvent) => void // 使用 AxiosProgressEvent
): Promise<any> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('categoryId', categoryId+"");
    formData.append('overwrite', overwrite);
    if(cataId) {
        formData.append('cataId', cataId + "");
    }
    formData.append('autoCreate', autoCreate + '');

    return httpInstance.post(resourceApi + "/upload", formData, {
        onUploadProgress: (progressEvent: AxiosProgressEvent) => { // 使用 AxiosProgressEvent
            if (onProgress) {
                onProgress(progressEvent); // 触发回调
            }
        }
    }).then((model: any) => {
        if (model) {
            console.log("文件上传成功:", model);
            return model;
        }
        return null;
    }).catch((error) => {
        console.error("文件上传失败:", error);
        throw error;
    });
};

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
    return httpInstance.post(resourceApi + "/category/" + cateId, requestParams).then((model: any) => {
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
    return httpInstance.post(resourceApi + "/updateInfo", params).then((model: any) => {
        console.log("修改成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const bacthUpdateResource = (resourceIds: number[], params: any): Promise<boolean> => {
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/batchUpdate", params).then((model: any) => {
        console.log("修改成功:", model);
        return model;
    })
}

export const addResourceTag = (resourceId: number, params: any): Promise<Tag> => {
    params["resourceId"] = resourceId;
    return httpInstance.post(resourceApi + "/addTag", params).then((model: any) => {
        return model;
    });
}

export const removeResourceTag = (resourceId: number, params: any): Promise<any> => {
    params["resourceId"] = resourceId;
    return httpInstance.post(resourceApi + "/removeTag", params).then((model: any) => {
        return model;
    });
}


export const batchAddTag = (resourceIds: number[], params: any): Promise<any> => {
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/batchAddTag", params).then((model: any) => {
        return model;
    });
}

export const batchRemoveTag = (resourceIds: number[], params: any): Promise<any> => {
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/batchRemoveTag", params).then((model: any) => {
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
    return httpInstance.post(resourceApi + "/tag/" + tagId, requestParams).then((model: any) => {
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
    return httpInstance.post(resourceApi + "/type/" + typeName, requestParams).then((model: any) => {
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
    return httpInstance.post(resourceApi + "/moveToCategory", params).then((model: any) => {
        console.log("修改成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const moveToRecycle = (resourceIds: number[], params: any): Promise<boolean> => {
    const ld = loading("移动中")
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/moveToRecycle", params).then((model: any) => {
        console.log("移动成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const restoreFormRecycle = (resourceIds: number[], params: any): Promise<boolean> => {
    const ld = loading("恢复中")
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/restore", params).then((model: any) => {
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
    return httpInstance.post(resourceApi + "/recycle", requestParams).then((model: any) => {
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

export const listAllFiles = (page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
    const ld = loading("加载中")
    // 合并分页参数和其他参数
    const requestParams: { [key: string]: any } = {
        pageNum: page,
        pageSize: size
    };
    Object.keys(params).forEach(key => {
        requestParams[key] = params[key];
    });
    return httpInstance.post(resourceApi + "/all", requestParams).then((model: any) => {
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

export const listCatalogFiles = (sno: string, page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
    const ld = loading("加载中")
    // 合并分页参数和其他参数
    const requestParams: { [key: string]: any } = {
        pageNum: page,
        pageSize: size
    };
    Object.keys(params).forEach(key => {
        requestParams[key] = params[key];
    });
    return httpInstance.post(resourceApi + "/catalog/" + sno, requestParams).then((model: any) => {
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

export const moveToCatalog = (resourceIds: number[], params: any): Promise<boolean> => {
    const ld = loading("修改中")
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/moveToCatalog", params).then((model: any) => {
        console.log("修改成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const startBatchImport = (params: any): Promise<string | null> => {
    const ld = loading("开始导入")
    return httpInstance.post(resourceApi + "/startBatchImport", params).then((model: any) => {
        if(model) {
            return model as string;
        }
        return null;
    }).finally(() => {
        ld.close();
    });
}

export const cancelBatchImport = (taskId: string): Promise<boolean> => {
    return httpInstance.get(resourceApi + "/cancelImport/" + taskId).then((model: any) => {
        return model as boolean;
    });
}
