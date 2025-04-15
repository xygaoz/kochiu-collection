export interface Category {
    sno: number;
    cateName: string;
    cateId: number
}

export interface PageInfo<T> {
    pageNum: number;
    pageSize: number;
    total: number;
    pages: number;
    list: T[];
}

export interface Tag{
    tagId: number;
    tagName: string;
}

export interface Resource {
    resourceId: number;
    resourceUrl: string;
    thumbnailUrl: string;
    sourceFileName: string;
    title: string;
    description: string;
    cateName: string;
    resolutionRatio: string;
    size: number;
    isPublic: number;
    star: number;
    tags: Tag[];
    createTime: string;
    updateTime: string;
    width: number;
    height: number;
    fileType: string;
    typeName: string;
    mimeType: string;
    previewUrl: string;
}

export interface ResourceType {
    label: string;
    value: string;
}

export interface SearchForm {
    cateId: string;
    keyword: string;
    types: string[];
    tags: string[];
}