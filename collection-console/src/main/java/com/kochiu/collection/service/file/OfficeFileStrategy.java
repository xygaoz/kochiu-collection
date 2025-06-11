package com.kochiu.collection.service.file;

import com.kochiu.collection.properties.CollectionProperties;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public abstract class OfficeFileStrategy {

    private final CollectionProperties properties;
    protected final RestTemplate restTemplate;

    protected OfficeFileStrategy(CollectionProperties properties) {
        this.properties = properties;
        this.restTemplate = createRestTemplateWithTimeout();
    }

    public void remoteConvertToPdf(File inputFile, File outputFile) throws IOException {
        // 1. 准备请求部件
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("data", new FileSystemResource(inputFile));
        body.add("outputFilename", "converted.pdf");

        // 2. 设置认证头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if(properties.getJodconverter().getRemote().getUsername() != null) {
            headers.setBasicAuth(properties.getJodconverter().getRemote().getUsername(), properties.getJodconverter().getRemote().getPassword()); // Base64自动编码
        }

        // 3. 发送请求
        String host = properties.getJodconverter().getRemote().getApiHost();
        if(host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        ResponseEntity<byte[]> response = restTemplate.exchange(
                host + "/lool/convert-to/pdf",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                byte[].class);

        // 4. 保存结果
        Files.write(outputFile.toPath(), Objects.requireNonNull(response.getBody()));
    }

    private RestTemplate createRestTemplateWithTimeout() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(properties.getJodconverter().getRemote().getTimeout())
                .setSocketTimeout(properties.getJodconverter().getRemote().getTimeout())
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
