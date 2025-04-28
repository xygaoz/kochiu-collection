const { defineConfig } = require("@vue/cli-service");
const webpack = require("webpack"); // 导入 webpack 模块

module.exports = defineConfig({
    publicPath: process.env.VUE_APP_CONTEXT_PATH + '/',
    transpileDependencies: true,
    devServer: {
        proxy: {
            "/api": {
                target: process.env.VUE_APP_TARGET_URL,
                ws: true, // 是否开启 webSocket 代理
                changeOrigin: true
            },
            // 转发 /resource 开头的请求
            '/resource': {
                target: process.env.VUE_APP_TARGET_URL,
                changeOrigin: true,
                pathRewrite: {
                    '^/resource': '/resource' // 可重写路径
                }
            }
        },
        client: {
          overlay: false
        }
    },
    chainWebpack: config => {
        config.plugin('html').tap(args => {
            args[0].title = 'KoChiu Collection'; // 修改 title
            return args;
        });
    },
    configureWebpack: {
        plugins: [
            new webpack.DefinePlugin({
                __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: 'false',
                // 添加以下配置确保 import.meta.env 可用
                'import.meta.env': JSON.stringify({
                    ...process.env,
                    VUE_WS_TARGET_URL: process.env.VUE_WS_TARGET_URL
                })
            })
        ]
    }
});