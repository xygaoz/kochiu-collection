module.exports = {
    root: true,
    env: {
        browser: true,
        node: true,
        es2021: true
    },
    extends: [
        'plugin:vue/vue3-recommended', // 确保包含 Vue 3 的推荐规则
        'eslint:recommended',
        '@vue/typescript/recommended'
    ],
    parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        parser: '@typescript-eslint/parser'
    },
    plugins: [
        'vue', // 确保包含 Vue 插件
        '@typescript-eslint'
    ],
    rules: {
        // 根据项目需求自定义规则
        'vue/multi-word-component-names': 'off', // 示例规则：关闭多单词组件名的要求
        '@typescript-eslint/no-explicit-any': 'off' // 示例规则：允许使用 any 类型
    }
};