export interface Category {
    sno: string;
    cateName: string;
}

export interface Resource {
    resourceId: number;
    thumbnailUrl: string;
    sourceFileName: string;
    title: string;
    description: string;
    resolutionRatio: string;
    size: string;
    isPublic: number;
    star: number;
    tags: string[];
    createdTime: string;
    updateTime: string;
}