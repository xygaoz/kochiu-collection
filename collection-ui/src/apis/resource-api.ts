
// 新增上传文件的方法
import httpInstance, { loading } from "@/utils/utils";
import { Module, PageInfo, Resource, Tag } from "@/apis/interface";
import type { AxiosProgressEvent } from 'axios'

const resourceApi = "/resource";
export const uploadFile = async (
    file: File,
    categoryId: number,
    overwrite: string,
    cataId: number | null,
    autoCreate: boolean,
    onProgress?: (progress: number) => void
): Promise<any> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('categoryId', categoryId+"");
    formData.append('overwrite', overwrite);
    if(cataId) {
        formData.append('cataId', cataId + "");
    }
    formData.append('autoCreate', autoCreate + '');

    return new Promise((resolve, reject) => {
        httpInstance.post(resourceApi + "/upload", formData, {
            onUploadProgress: (progressEvent: AxiosProgressEvent) => {
                if (onProgress) {
                    // 计算百分比（确保不除以0）
                    const total = Math.max(progressEvent.total || file.size, 1);
                    const percent = Math.min(99, Math.round(
                        (progressEvent.loaded * 100) / total
                    ));
                    onProgress(percent);  // 只传递计算好的百分比
                }
            }
        })
            .then(response => {
                onProgress?.(100);  // 上传完成时手动触发100%
                resolve(response.data ?? { success: true });
            })
            .catch(error => {
                onProgress?.(100);  // 出错时也触发100%
                reject(error);
            });
    });
};

// 检查分片上传状态
const checkChunkStatus = async (fileId: string, totalChunks: number): Promise<boolean[]> => {
    try {
        return httpInstance.post(resourceApi + "/check-chunks", {
            fileId,
            totalChunks
        }).then((res: any) => {
            if (res) {
                return res as boolean[];
            }
            return [];
        }).catch((error) => {
            console.error('检查分片状态失败:', error);
            return new Array(totalChunks).fill(false);
        });
    } catch (error) {
        console.error('检查分片状态失败:', error);
        return new Array(totalChunks).fill(false);
    }
};

export const uploadLargeFile = async (
    file: File,
    categoryId: number,
    overwrite: string,
    cataId: number | null,
    autoCreate: boolean,
    onProgress?: (progress: number) => void,
    chunkSize = 50 * 1024 * 1024 // 默认50MB分片
): Promise<any> => {
    const totalChunks = Math.ceil(file.size / chunkSize);
    const fileId = Math.random().toString(36).substring(2) + Date.now().toString(36);

    // 检查已上传的分片
    const chunkStatus = await checkChunkStatus(fileId, totalChunks);
    let uploadedSize = chunkStatus.filter(Boolean).length * chunkSize;

    for (let chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
        // 跳过已上传的分片
        if (chunkStatus[chunkIndex]) continue;

        console.log(`上传分片 ${chunkIndex + 1}/${totalChunks}`);
        const start = chunkIndex * chunkSize;
        const end = Math.min(file.size, start + chunkSize);
        console.log(`分割文件 ${start} - ${end}`);
        const chunk = file.slice(start, end);

        const formData = new FormData();
        formData.append('file', chunk);
        formData.append('fileId', fileId);
        formData.append('chunkIndex', chunkIndex.toString());
        formData.append('totalChunks', totalChunks.toString());
        formData.append('originalName', file.name);
        formData.append('categoryId', categoryId.toString());
        formData.append('overwrite', overwrite);
        if(cataId) formData.append('cataId', cataId.toString());
        formData.append('autoCreate', autoCreate.toString());

        try {
            await httpInstance.post(resourceApi + "/upload-chunk", formData);

            uploadedSize += chunk.size;
            const percent = Math.min(99, Math.round((uploadedSize * 100) / file.size));
            console.log(`上传进度: ${percent}%`);
            onProgress?.(percent);
        } catch (error) {
            onProgress?.(100);
            throw error;
        }
    }

    console.log("所有分片已上传完毕");
    // 通知服务器合并分片
    return httpInstance.post(resourceApi + "/merge-chunks", {
        fileId,
        originalName: file.name,
        categoryId,
        overwrite,
        cataId,
        autoCreate
    }).then((res: any) => {
        onProgress?.(100);
        if (res) {
            return res as boolean;
        }
        return false;
    }).catch((error) => {
        console.error('合并分片失败:', error);
    });
};

export const listCategoryFiles = async (cateId: string, page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
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

export const updateResource = async (resourceId: number, params: any): Promise<boolean> => {
    const ld = loading("修改中")
    params["resourceId"] = resourceId;
    return httpInstance.post(resourceApi + "/updateInfo", params).then((model: any) => {
        console.log("修改成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const bacthUpdateResource = async (resourceIds: number[], params: any): Promise<boolean> => {
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/batchUpdate", params).then((model: any) => {
        console.log("修改成功:", model);
        return model;
    })
}

export const addResourceTag = async (resourceId: number, params: any): Promise<Tag> => {
    params["resourceId"] = resourceId;
    return httpInstance.post(resourceApi + "/addTag", params).then((model: any) => {
        return model;
    });
}

export const removeResourceTag = async (resourceId: number, params: any): Promise<any> => {
    params["resourceId"] = resourceId;
    return httpInstance.post(resourceApi + "/removeTag", params).then((model: any) => {
        return model;
    });
}


export const batchAddTag = async (resourceIds: number[], params: any): Promise<any> => {
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/batchAddTag", params).then((model: any) => {
        return model;
    });
}

export const batchRemoveTag = async (resourceIds: number[], params: any): Promise<any> => {
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/batchRemoveTag", params).then((model: any) => {
        return model;
    });
}

export const listTagFiles = async (tagId: string, page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
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

export const listTypeFiles = async (typeName: string, page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
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

export const moveToCategory = async (resourceIds: number[], params: any): Promise<boolean> => {
    const ld = loading("修改中")
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/moveToCategory", params).then((model: any) => {
        console.log("修改成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const moveToRecycle = async (resourceIds: number[], params: any): Promise<boolean> => {
    const ld = loading("移动中")
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/moveToRecycle", params).then((model: any) => {
        console.log("移动成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const restoreFormRecycle = async (resourceIds: number[], params: any): Promise<boolean> => {
    const ld = loading("恢复中")
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/restore", params).then((model: any) => {
        console.log("恢复成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const listRecycleFiles = async (page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
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

export const listAllCateFiles = async (page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
    const ld = loading("加载中")
    // 合并分页参数和其他参数
    const requestParams: { [key: string]: any } = {
        pageNum: page,
        pageSize: size
    };
    Object.keys(params).forEach(key => {
        requestParams[key] = params[key];
    });
    return httpInstance.post(resourceApi + "/all-category", requestParams).then((model: any) => {
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

export const listCatalogFiles = async (cataId: string, page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
    const ld = loading("加载中")
    // 合并分页参数和其他参数
    const requestParams: { [key: string]: any } = {
        pageNum: page,
        pageSize: size
    };
    Object.keys(params).forEach(key => {
        requestParams[key] = params[key];
    });
    return httpInstance.post(resourceApi + "/catalog/" + cataId, requestParams).then((model: any) => {
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

export const moveToCatalog = async (resourceIds: number[], params: any): Promise<boolean> => {
    const ld = loading("修改中")
    params["resourceIds"] = resourceIds;
    return httpInstance.post(resourceApi + "/moveToCatalog", params).then((model: any) => {
        console.log("修改成功:", model);
        return model;
    }).finally(() => {
        ld.close();
    });
}

export const startBatchImport = async (params: any): Promise<string | null> => {
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

export const cancelBatchImport = async (taskId: string): Promise<boolean> => {
    return httpInstance.get(resourceApi + "/cancelImport/" + taskId).then((model: any) => {
        return model as boolean;
    });
}

export const listPublicFiles = async (page: number, size: number, params: any): Promise<PageInfo<Resource>> => {
    const ld = loading("加载中")
    // 合并分页参数和其他参数
    const requestParams: { [key: string]: any } = {
        pageNum: page,
        pageSize: size
    };
    Object.keys(params).forEach(key => {
        requestParams[key] = params[key];
    });
    return httpInstance.post(resourceApi + "/public", requestParams).then((model: any) => {
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

export const setResourcePublic = async (resourceId: number, isPublic: boolean): Promise<boolean> => {
    return httpInstance.post(resourceApi + "/set-public/" + resourceId, {
        isPublic: isPublic
    }).then((model: any) => {
        return model as boolean;
    });
}

export const batchShareResources = async (resourceIds: number[], isPublic: boolean): Promise<boolean> => {
    return httpInstance.post(resourceApi + "/batch-share", {
        resourceIds: resourceIds,
        share: isPublic
    }).then((model: any) => {
        return model as boolean;
    });
}

export const getAllowedTypes = async (): Promise<string[]> => {
    return httpInstance.get(resourceApi + "/allowedTypes").then((model: any) => {
        return model as string[];
    });
}

export const checkFileExist = async (md5: string): Promise<boolean> => {
    return httpInstance.post(resourceApi + "/check-file-exist",  {
        md5: md5
    }).then((model: any) => {
        return model as boolean;
    });
}