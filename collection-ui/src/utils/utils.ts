import axios from "axios";
import { ref } from "vue";
import { ElLoading, ElMessage } from "element-plus";
import qs from "qs";
import router from "@/apis/base-routes"; // 引入qs库来处理form-data
import { tokenStore } from "@/apis/system-api";
import Cookies from "js-cookie"; // 引入tokenStore
import { JSEncrypt } from "jsencrypt";
import { logout } from "@/apis/user-api";
import * as FingerprintJS from "@fingerprintjs/fingerprintjs";

// 初始化指纹
const initFingerprint = async () => {
    const fp = await FingerprintJS.load();
    const { visitorId } = await fp.get();
    localStorage.setItem('deviceFingerprint', visitorId);
    return visitorId;
};

const httpInstance = axios.create({
    baseURL: process.env.VUE_APP_BASE_API,
    headers: {
        'Content-Type': 'multipart/form-data'
    },
    // 根据不同的模式设置不同的配置
    ...(process.env.NODE_ENV === 'development' ? {
        // 开发模式配置
        validateStatus: function () {
            return true; // 允许所有状态码通过
        }
    } : {
        // 生产模式配置
        validateStatus: function (status) {
            return status >= 200 && status < 300; // 只允许2xx状态码通过
        }
    })
});

// 配置axios的请求拦截器，将application/json改为form-data
httpInstance.interceptors.request.use(async config => {
    // 获取token并设置到Authorization header中
    const token = tokenStore.getToken();
    if (token) {
        config.headers['Authorization'] = token;
        config.headers['X-Device-Fingerprint'] = localStorage.getItem('deviceFingerprint') || await initFingerprint();
    }
    if (config.data && config.headers['Content-Type'] === 'application/x-www-form-urlencoded') {
        config.data = qs.stringify(config.data, {
            allowDots: false,  // 禁用 `params.pageNum` 变成 `params[pageNum]`
            arrayFormat: 'repeat',  // 数组变成 `types=IMAGE&types=VIDEO`
            encode: false,  // 不额外编码（可选）
        });
    }
    return config;
}, error => {
    return Promise.reject(error);
});
httpInstance.defaults.timeout = 3600000; // 1小时超时

let isRefreshing = false; // 是否正在刷新Token
let failedQueue: any[] = []; // 存储刷新Token期间的失败请求

// 处理队列中的失败请求
const processQueue = (error: any, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

// 配置axios的响应拦截器
httpInstance.interceptors.response.use(
    res => {
        if (res.data && !res.data.success) {
            ElMessage({
                type: 'error',
                message: !res.data.message ? '系统错误' : res.data.message
            });
        }
        return res.data?.model ?? null;
    },
    async error => {
        if (!error.response) {
            ElMessage({ type: 'error', message: error.message });
            return Promise.reject(error);
        }

        const originalRequest = error.config;
        const status = error.response.status;

        // 401处理逻辑（Token过期）
        if (status === 401) {
            if (originalRequest.url.includes('/api/v1/sys/refresh')) {
                // 刷新Token接口也返回401，说明refreshToken过期
                ElMessage.error('登录已过期，请重新登录');
                clearAuthData();
                await router.push({ name: 'LoginUI' });
                return Promise.reject(error);
            }

            // 非刷新请求的401处理
            if (!isRefreshing) {
                isRefreshing = true;

                try {
                    // 尝试用refreshToken获取新accessToken
                    const newToken = await refreshAccessToken();
                    // 重试所有积压的请求
                    processQueue(null, newToken);

                    // 重试当前失败请求
                    originalRequest.headers.Authorization = newToken;
                    return httpInstance(originalRequest);
                } catch (refreshError) {
                    // 刷新失败，清空认证数据并跳转登录
                    processQueue(refreshError);
                    clearAuthData();
                    await router.push({ name: 'LoginUI' });
                    return Promise.reject(refreshError);
                } finally {
                    isRefreshing = false;
                }
            } else {
                // 正在刷新Token，将请求加入队列
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then(token => {
                    originalRequest.headers.Authorization = token;
                    return httpInstance(originalRequest);
                }).catch(err => {
                    return Promise.reject(err);
                });
            }
        }

        // 其他错误处理
        ElMessage({
            type: 'error',
            message: `系统错误 ${error.response?.data?.message || ''}`
        });
        return Promise.reject(error);
    }
);

// 刷新Token的函数
export const refreshAccessToken = async () => {
    try {
        const refreshToken = getRefreshToken(); // 从HttpOnly Cookie获取
        if (refreshToken.value === '') {
            return null;
        }
        const response = await axios.post(process.env.VUE_APP_CONTEXT_PATH + '/api/v1/sys/refresh', {}, {
            headers: {
                'Authorization': refreshToken.value // 提取ref的值
            }
        });

        // 更新存储的accessToken
        setAccessToken(response.data.token, response.data.expirySeconds);
        //设置Cookie
        Cookies.set('refresh_token', response.data.refreshToken, { expires: 7, path: '/' });

        return response.data.token; // 假设返回 { token: 'xxx', expiresIn: 1800 }
    } catch (err) {
        console.error('刷新Token失败');
        logout();
    }
};

// 工具函数
const setAccessToken = (token: string, expirySeconds: number) => {
    // 存到内存或短期存储（避免XSS）
    tokenStore.setToken(token, expirySeconds)
};

const getRefreshToken = () => {
    // 从Cookie解析refreshToken（需配合后端设置HttpOnly）
    return ref(Cookies.get('refresh_token') || '');
};

const clearAuthData = () => {
    delete axios.defaults.headers.common['Authorization'];
    Cookies.remove('Cookies.remove(\'user_token\');');
};

export default httpInstance

export function loading(msg:string) {
    return ElLoading.service({
      lock: true,
      text: msg,
      background: 'rgba(0, 0, 0, 0.7)',
    })
}

export const downloadFile = async (url: string, data: any) => {
    const token = tokenStore.getToken();
    return axios({
        baseURL: process.env.VUE_RESOURCE_BASE_API,
        url,
        method:'POST',
        data,
        responseType: 'blob',
        headers: {
            'Authorization': token || '',
        },
    }).then(res => {
            const str = res.headers['content-disposition']
            if (!res || !str) {
                ElMessage.error('下载失败！')
                return
            }
            if (res && res.status === 200 && res.data) {
                const { data, headers } = res
                let fileName
                if (headers['content-disposition']) {
                    fileName = headers['content-disposition'].replace(/\w+;filename=(.*)/, '$1')
                } else {
                    fileName = data.fileName
                }
                const blob = new Blob([data], { type: headers['content-type'] })
                const dom = document.createElement('a')
                dom.href = window.URL.createObjectURL(blob)
                dom.download = decodeURIComponent(fileName)
                dom.style.display = 'none'
                document.body.appendChild(dom)
                dom.click()
                dom.parentNode!.removeChild(dom)
                window.URL.revokeObjectURL(url)
            } else {
                ElMessage.error('下载失败！')
            }
        })
        .catch(err => {
            ElMessage.error(err || '下载失败！')
        })
}

// 加密密码的方法
export const encryptPassword = (publicKey: string, password: string) : undefined | string => {

    // 创建JSEncrypt实例
    const encryptor = new JSEncrypt();
    encryptor.setPublicKey(publicKey);
    // 验证公钥是否设置成功
    if (!encryptor.getPublicKey()) {
        ElMessage.error('公钥设置失败');
        return
    }
    // 同步加密（JSEncrypt默认是同步操作）
    const encrypted = encryptor.encrypt(password);
    if (!encrypted){
        ElMessage.error('加密失败');
        return
    }

    // 执行加密
    return encrypted;
};

export const convertToBytes = (value: any) : number => {
    if (typeof value === 'number') return value

    const units: Record<string, number> = {
        B: 1,
        KB: 1024,
        MB: 1024 ** 2,
        GB: 1024 ** 3,
        TB: 1024 ** 4,
        PB: 1024 ** 5
    }

    const match = String(value).trim().match(/^([\d.]+)\s*([KMGTP]?B)$/i)
    if (!match) return parseFloat(value) || 0

    const num = parseFloat(match[1])
    const unit = match[2].toUpperCase()

    return num * (units[unit] || 1)
}