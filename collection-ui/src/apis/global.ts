// src/stores/userStore.ts
import { defineStore } from 'pinia';
import { computed, ref } from "vue";

interface UserSettings {
    include_sub_dir: boolean;
    username: string;
}

// 添加类型导出（方便其他文件使用）
export type { UserSettings }

export const useUserStore = defineStore('userStore', () => {
    // State
    const userSettingsMap = ref<Record<string, UserSettings>>({});
    const currentUserCode = ref('');

    const currentSettings = computed(() =>
            userSettingsMap.value[currentUserCode.value] || {
                include_sub_dir: false,
                username: ''
            }
    )

    const clearCurrentUser = () => {
        currentUserCode.value = ''
    }

    // Actions
    const initializeUser = (userCode: string, username: string) => {
        currentUserCode.value = userCode;
        if (!userSettingsMap.value[userCode]) {
            userSettingsMap.value[userCode] = { include_sub_dir: false, username };
        }
    };

    const setIncludeSubDir = (value: boolean) => {
        if (currentUserCode.value) {
            userSettingsMap.value[currentUserCode.value].include_sub_dir = value;
        }
    };

    // 暴露状态和方法
    return {
        userSettingsMap,
        currentUserCode,
        currentSettings,
        initializeUser,
        clearCurrentUser,
        setIncludeSubDir,
        include_sub_dir: computed({
            get: () => currentSettings.value.include_sub_dir,
            set: (val) => setIncludeSubDir(val)
        })
    };
}, {
    persist: {
        // 新版插件使用 storage 替代 paths
        storage: localStorage,
        serializer: {
            serialize: JSON.stringify,
            deserialize: JSON.parse
        }
    }
});