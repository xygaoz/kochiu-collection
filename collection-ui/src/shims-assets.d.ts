declare module '*.gif' {
    const src: string;
    export default src;
}

declare module '*.jpg' {
    const src: string;
    export default src;
}

declare module '*.png' {
    const src: string;
    export default src;
}

interface ImportMetaEnv {
    VUE_APP_CONTEXT_PATH?: string;
    VUE_WS_TARGET_URL?: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}
