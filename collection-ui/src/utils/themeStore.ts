// themeStore.ts
import { defineStore } from 'pinia';
import themeConfig from '@/utils/theme-config';

export const useThemeStore = defineStore('theme', {
    state: () => ({
        currentTheme: 'light' // 默认值
    }),

    actions: {
        async initTheme() {
            const savedTheme = localStorage.getItem('global-theme');
            if (savedTheme) {
                await this.applyTheme(savedTheme);
            } else {
                await this.applyTheme('light');
            }
        },

        async applyTheme(themeName: string) {
            return new Promise<void>((resolve) => {
                this.currentTheme = themeName;
                localStorage.setItem('global-theme', themeName);

                const theme = themeConfig.themes.find(t => t.name === themeName);
                if (!theme) return resolve();

                const apply = () => {
                    const root = document.documentElement;
                    const body = document.body;

                    root.classList.remove('light', 'dark');
                    body.classList.remove('light', 'dark');

                    root.classList.add(themeName);
                    body.classList.add(themeName);

                    Object.entries(theme.variables).forEach(([key, value]) => {
                        root.style.setProperty(key, value);
                    });

                    resolve();
                };

                if (document.readyState === 'complete') {
                    apply();
                } else {
                    window.addEventListener('load', apply);
                }
            });
        },

        async toggleTheme() {
            alert(this.currentTheme)
            const newTheme = this.currentTheme === 'light' ? 'dark' : 'light';
            await this.applyTheme(newTheme);
        }
    },

    getters: {
        isDark: (state) => state.currentTheme === 'dark'
    }
});