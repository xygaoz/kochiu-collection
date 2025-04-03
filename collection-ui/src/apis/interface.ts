export interface Category {
    sno: string;
    cateName: string;
}

export interface PageInfo<T> {
    pageNum: number;
    pageSize: number;
    total: number;
    pages: number;
    list: T[];
}

export interface Resource {
    resourceId: number;
    thumbnailUrl: string;
    sourceFileName: string;
    title: string;
    description: string;
    resolutionRatio: string;
    size: number;
    isPublic: number;
    star: number;
    tags: string[];
    createTime: string;
    updateTime: string;
    width: number;
    height: number;
    fileType: string;
}