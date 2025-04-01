// 将16进制模数和指数转换为PEM格式的公钥
export const constructPublicKeyPEM = (modulusHex: string, exponentHex: string) => {
    // 1. 将16进制转换为字节数组
    const modulusBytes = hexToBytes(modulusHex);
    const exponentBytes = hexToBytes(exponentHex);

    // 2. 构造ASN.1编码
    const asn1 = [
        // 序列头
        0x30, 0x82, 0x01, 0x22, // SEQUENCE (长格式)

        // 算法标识符
        0x30, 0x0D,             // SEQUENCE
        0x06, 0x09, 0x2A, 0x86, 0x48, 0x86, 0xF7, 0x0D, 0x01, 0x01, 0x01, // RSA OID
        0x05, 0x00,             // NULL

        // 公钥数据
        0x03, 0x82, 0x01, 0x0F, // BIT STRING (长格式)
        0x00,                   // 无用的位数

        // 内部序列
        0x30, 0x82, 0x01, 0x0A, // SEQUENCE (长格式)

        // 模数
        ...encodeASN1Integer(modulusBytes),

        // 指数
        ...encodeASN1Integer(exponentBytes)
    ];

    // 3. 转换为Base64并添加PEM头尾
    const base64 = bytesToBase64(new Uint8Array(asn1));
    return `-----BEGIN PUBLIC KEY-----\n${formatBase64(base64)}\n-----END PUBLIC KEY-----`;
};

// 16进制字符串转字节数组
const hexToBytes = (hexString: string) => {
    if (hexString.length % 2 !== 0) {
        hexString = '0' + hexString; // 补齐偶数长度
    }

    const bytes = [];
    for (let i = 0; i < hexString.length; i += 2) {
        bytes.push(parseInt(hexString.substr(i, 2), 16));
    }
    return bytes;
};

// 字节数组转Base64
const bytesToBase64 = (bytes: Uint8Array) => {
    let binary = '';
    bytes.forEach((byte) => {
        binary += String.fromCharCode(byte);
    });
    return btoa(binary);
};

// 格式化Base64字符串（每64字符换行）
const formatBase64 = (base64: string) => {
    return base64.match(/.{1,64}/g)?.join('\n') || base64; // 处理可能的null情况
};

// 编码ASN.1 INTEGER
const encodeASN1Integer = (bytes: number[]) => {
    // 如果第一个字节的最高位是1，需要添加0x00前缀
    if (bytes[0] & 0x80) {
        bytes = [0x00, ...bytes];
    }

    // 计算长度字节
    let lengthBytes;
    const length = bytes.length;

    if (length < 128) {
        lengthBytes = [length];
    } else if (length <= 255) {
        lengthBytes = [0x81, length];
    } else if (length <= 65535) {
        lengthBytes = [0x82, length >> 8, length & 0xFF];
    } else {
        lengthBytes = [0x83, length >> 16, (length >> 8) & 0xFF, length & 0xFF];
    }

    return [0x02, ...lengthBytes, ...bytes];
};