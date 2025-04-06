import httpInstance from "@/apis/utils"; // 导入httpInstance
import { Category, PageInfo, Resource } from "@/apis/interface"; // 导入Category接口
import { loading } from "@/apis/utils";

export const tokenStore = {
    setToken(token: string, expirySeconds: number) {
        const item = {
            value: token,
            expiry: Date.now() + expirySeconds * 1000
        };
        localStorage.setItem('token', JSON.stringify(item));
    },
    getToken(): string | null {
        const key = 'token';
        const itemStr = localStorage.getItem(key);
        if (!itemStr) return null;

        const item = JSON.parse(itemStr);
        if (Date.now() > item.expiry) {
            localStorage.removeItem(key); // 过期删除
            return null;
        }
        return item.value;
    },
    removeToken() {
        localStorage.removeItem('token');
    }
};

// 创建一个方法来获取公钥
export const getPublicKey = () => {
    return httpInstance.get("/publicKey").then((model) => {
        if (model) {
            console.log("获取公钥成功:", model);
            return model;
        }
    }).catch((error) => {
        console.error("获取公钥失败:", error);
        throw error; // 抛出错误以便调用者处理
    });
};

// 登录
export const loginService = (loginForm: any) => {
    const ld = loading("登录中");
    return httpInstance.post("/login", loginForm).then((model) => {
        if (model) {
            console.log("登录成功，获取token:", model);
            return model;
        }
    }).catch((error) => {
        console.error("登录失败:", error);
        throw error; // 抛出错误以便调用者处理
    }).finally(() => {
        ld.close();
    });
};

export const listCategory = (): Promise<Category[]> => {
    return httpInstance.get("/category/list").then((model: any) => {
        if (model) {
            return model as Category[];
        }
        return [];
    }).catch((error) => {
        console.error("获取分类失败:", error);
        return [];
    });
}

// 新增上传文件的方法
export const uploadFile = (file: File, categorySno: string, overwrite: string): Promise<any> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('categoryId', categorySno);
    formData.append('overwrite', overwrite);

    const ld = loading("上传中")
    return httpInstance.post("/upload", formData).then((model: any) => {
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

export const listCategoryFiles = (cateId: string, page: number, size: number): Promise<PageInfo<Resource>> => {
    const ld = loading("加载中")
    return httpInstance.post("/resource/" + cateId, {
        params: {
            pageNum: page,
            pageSize: size
        }
    }).then((model: any) => {
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