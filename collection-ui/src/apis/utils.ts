import axios from "axios";
import { h, render } from 'vue'
import {ElMessage, ElLoading} from "element-plus";
import qs from 'qs';
import router from "@/apis/base-routes"; // 引入qs库来处理form-data
import { tokenStore } from "@/apis/services"; // 引入tokenStore

const httpInstance = axios.create({
    baseURL: process.env.VUE_APP_BASE_API,
    headers: {
        'Content-Type': 'application/json;charset=UTF-8'
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
httpInstance.interceptors.request.use(config => {
    // 获取token并设置到Authorization header中
    const token = tokenStore.getToken();
    if (token) {
        config.headers['Authorization'] = token;
    }
    if (config.data && config.headers['Content-Type'] === 'application/x-www-form-urlencoded') {
        config.data = qs.stringify(config.data);
    }
    return config;
}, error => {
    return Promise.reject(error);
});

// 配置axios的响应拦截器
httpInstance.interceptors.response.use(res=>{
    if(res.data && !res.data.success){
        ElMessage({type:'error',message:!res.data.message?'系统错误':res.data.message})
    }
    return res.data && res.data.model ? res.data.model : null
},e=>{
    debugger
    if(e.response) {
        if (e.response.status == 401) {
            ElMessage.error("请先登录")
            // 跳转到登录页面
            router.push({ name: 'LoginUI' });
        } else {
            ElMessage({type: 'error', message: '系统错误' + e})
        }
    }
    else{
        ElMessage({type: 'error', message: e.message})
    }
    return Promise.reject(e)
})

export default httpInstance

export function initInstance(component:object, container:any, option:object) {
    const vNode = h(component, option);
    render(vNode, container);
    document.body.appendChild(container.firstElementChild);
    return vNode.component;
}

export function getContainer() {
    return document.createElement('div');
}

export function loading(msg:string) {
    return ElLoading.service({
      lock: true,
      text: msg,
      background: 'rgba(0, 0, 0, 0.7)',
    })
}

export const downloadFile = (url: string, data: any) => {
    return axios({
        baseURL: process.env.VUE_APP_BASE_API,
        url,
        method:'POST',
        data,
        responseType: 'blob',
        headers: {
            'X-Token': localStorage.getItem('token') || '',
        },
    })
        .then(res => {
            debugger
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
                const downUrl = window.URL.createObjectURL(blob)
                dom.href = downUrl
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