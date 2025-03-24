const { defineConfig } = require("@vue/cli-service");

module.exports = defineConfig({
    transpileDependencies: true,
    devServer: {
        proxy: {
            "/api": {
                target: process.env.VUE_APP_TARGET_URL,
                ws: true, // 是否开启 webSocket 代理
                changeOrigin: true
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
    }
});
