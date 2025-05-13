// src/stores/userStore.ts
import { defineStore } from 'pinia';
import { computed, ref } from "vue";

interface UserSettings {
    include_sub_dir: boolean;
    username: string;
    userid: number;
    permissions?: string[]; // 权限数组
    canDel?: number;
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
                username: '',
                userid: 0,
                permissions: [], // 默认空权限数组
                canDel: 1
            }
    )

    const clearCurrentUser = () => {
        currentUserCode.value = ''
    }

    // Actions
    const initializeUser = (userCode: string, username: string,
                            userid: number, permissions?: string[],
                            canDel?: number) => {
        currentUserCode.value = userCode;
        userSettingsMap.value[userCode] = {
            ...userSettingsMap.value[userCode], // 保留现有设置
            username,
            userid,
            permissions: permissions || [], // 初始化权限
            canDel: canDel || 1
        };
    };

    // 新增：设置权限
    const setPermissions = (permissions: string[]) => {
        if (currentUserCode.value) {
            if (!userSettingsMap.value[currentUserCode.value]) {
                userSettingsMap.value[currentUserCode.value] = {
                    include_sub_dir: false,
                    username: '',
                    userid: 0,
                    permissions: [],
                    canDel: 1
                };
            }
            userSettingsMap.value[currentUserCode.value].permissions = permissions;
        }
    };

    const setCanDel = (calDel: number) => {
        if (currentUserCode.value) {
            if (!userSettingsMap.value[currentUserCode.value]) {
                userSettingsMap.value[currentUserCode.value] = {
                    include_sub_dir: false,
                    username: '',
                    userid: 0,
                    permissions: [],
                    canDel: 1
                };
            }
            userSettingsMap.value[currentUserCode.value].canDel = calDel;
        }
    };

    const getCanDel = () => {
        return currentSettings.value.canDel;
    };

    // 新增：检查权限
    const hasPermission = (permission: string) => {
        console.log("检查权限：", currentSettings.value.permissions);
        return currentSettings.value.permissions?.includes(permission) || false;
    };

    // 新增：获取所有权限
    const getPermissions = () => {
        return currentSettings.value.permissions || [];
    };

    const getUsername = () => {
        return currentSettings.value.username;
    };

    const setUsername = (username: string) => {
        if (currentUserCode.value) {
            userSettingsMap.value[currentUserCode.value].username = username;
        }
    };

    const getUserid = () => {
        return currentSettings.value.userid;
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
        getUsername,
        setUsername,
        getUserid,
        setPermissions, // 新增
        hasPermission, // 新增
        getPermissions, // 新增
        setCanDel,
        getCanDel,
        include_sub_dir: computed({
            get: () => currentSettings.value.include_sub_dir,
            set: (val) => setIncludeSubDir(val)
        }),
        username: computed({
            get: () => currentSettings.value.username,
            set: (val) => setUsername(val)
        }),
        userid: computed(() => currentSettings.value.userid),
        permissions: computed(() => currentSettings.value.permissions), // 新增计算属性
        canDel: computed(() => currentSettings.value.canDel)
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