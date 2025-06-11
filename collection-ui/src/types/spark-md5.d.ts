declare module 'spark-md5' {
    interface SparkMD5 {
        ArrayBuffer: {
            new (): {
                append(chunk: ArrayBuffer): void;
                end(raw?: boolean): string;
                destroy(): void;
                getState(): { buf: string; length: number };
                setState(state: { buf: string; length: number }): void;
                reset(): void;
            };
        };
    }

    const SparkMD5: SparkMD5;
    export = SparkMD5;
}