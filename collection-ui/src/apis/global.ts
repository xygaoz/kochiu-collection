import { defineStore } from 'pinia'

export const useGlobalStore = defineStore('global', {
    state: () => ({
        include_sub_dir: false,
    })
})