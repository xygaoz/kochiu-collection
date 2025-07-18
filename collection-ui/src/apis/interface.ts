export interface LoginInfo{
    userId: number;
    userCode: string
    userName: string
    token: string
    refreshToken: string
    expirySeconds: number
    permissions: string[]
    canDel: number
}

export interface Category {
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
    cataPath: string;
    resolutionRatio: string;
    size: number;
    share: boolean;
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
    cateId?: string;
    keyword?: string;
    types?: string[];
    tags?: string[];
    include?: boolean; // 包含子目录
}

export interface Catalog {
    id: number;
    label: string;
    level: number;
    parentId: number;
    children: Catalog[];
}

export interface PathVo {
    path: string;
    pathInfo: PathInfo[];
}

export interface PathInfo{
    id: number;
    cataName: string;
}

export interface User {
    userId: string
    userCode: string
    userName: string
    password: string
    strategy: string
    key: string
    token: string
    status: number
    theme: string
    canDel: number
    roles: Role[]
}

export interface Role {
    roleId: string
    roleName: string
    canDel: number
    permissions: Permission[]
}

export interface Permission {
    actionId: number;
    moduleName: string;
    actionName: string
}

export interface Strategy {
    strategyId: number;
    strategyCode: string;
    strategyName: string;
    serverUrl: string;
    username: string;
    password: string;
    otherConfig: string;
}

export interface Module {
    moduleId: number;
    moduleName: string;
    moduleCode: string;
    children: Module[];
    actions: Action[];
}

export interface Action {
    actionId: number;
    actionName: string;
    actionCode: string;
    selected: boolean;
}

export interface Menu {
    name: string;
    path: string;
    title: string;
    icon: string;
    iconType: string;
    style: string;
    redirect: string;
    children: Menu[];
}

export interface Meta{
    title: string;
    icon: string;
    iconType: string;
    style: string;
    requiresAuth: boolean;
    group?: string;
}

export interface RouteMenu {
    name: string;
    path: string;
    meta: Meta;
    children: RouteMenu[];
}

export interface Keys {
    publicKey: string;
    privateKey: string;
    commonKey: string;
}

export interface UserProperty {
    listCategoryNum: number;
    listCategoryBy: number;
    resourcePageSize: number;
    listTagNum: number;
    listTagBy: number;
}

export interface SysProperty {
    uploadMaxSize: string
}