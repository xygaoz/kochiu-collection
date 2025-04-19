// src/apis/global.ts
import { defineStore } from 'pinia'

export const useGlobalStore = defineStore('global', {
    state: () => ({
        include_sub_dir: false,
    }),
    actions: {
        setIncludeSubDir(value: boolean) {
            this.include_sub_dir = value
        }
    },
    persist: true // 简单启用持久化，使用默认配置
})