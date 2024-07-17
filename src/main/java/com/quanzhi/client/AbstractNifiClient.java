package com.quanzhi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.util.Arrays;

/**
 * @description: 管理登录和连接的公共逻辑，包装一些发请求方法
 * @author：CatTail
 * @date: 2024/6/14
 * @Copyright: https://github.com/CatTailzz
 */
public abstract class AbstractNifiClient implements Constants{

    private static final Logger logger = LoggerFactory.getLogger(AbstractNifiClient.class);

    private final String nifiUrl;
    private final String username;
    private final String password;
    private String accessToken;
    private long tokenExpiryTime;
    private final String customHost;

    public AbstractNifiClient(String nifiUrl, String username, String password, String customHost) throws Exception {
        this.nifiUrl = nifiUrl;
        this.username = username;
        this.password = password;
        this.customHost = customHost;
        try {
            login(); // 自动登录
        } catch (Exception e) {
            System.err.println("Failed to login NiFi during initialization: " + e.getMessage());
        }
    }


    private CloseableHttpClient createHttpClient() throws Exception {
        SSLContext sslContext;
        try {
            sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true)
                    .build();
        } catch (Exception e) {
            throw new KeyManagementException("Failed to create SSL context", e);
        }

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE);

        return HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
    }

    private void login() throws Exception {
        HttpPost post = new HttpPost(nifiUrl + LOGIN_ENDPOINT);

        StringEntity entity = new StringEntity("username=" + username + "&password=" + password);
        entity.setContentType("application/x-www-form-urlencoded");
        post.setEntity(entity);
        post.setHeader("Host", customHost);

        logRequestHeaders(post);

        CloseableHttpClient httpClient = createHttpClient();


        try (CloseableHttpResponse response = httpClient.execute(post)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 201) {
                throw new RuntimeException("Failed to login. HTTP Status Code: " + statusCode);
            }
            String responseString = EntityUtils.toString(response.getEntity());
            this.accessToken = responseString.trim();
            this.tokenExpiryTime = System.currentTimeMillis() + 3600 * 1000 * 6; // 假设 token 有效期为 1 小时
        } catch (Exception e) {
            logger.error("Failed to login", e);
            throw new RuntimeException("Failed to login", e);
        } finally {
            httpClient.close();
        }
    }

    protected <T> T executeRequest(HttpUriRequest request, JsonResponseHandler<T> handler) throws Exception {
        try {
            ensureLoggedIn();
            request.setHeader("Authorization", "Bearer " + accessToken);
            request.setHeader("Host", customHost);

            logRequestHeaders(request);

            CloseableHttpClient httpClient = createHttpClient();
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 403) {
                    // 重新登录并重试请求
                    login();
                    request.setHeader("Authorization", "Bearer " + accessToken);
                    request.setHeader("Host", customHost);
                    try (CloseableHttpResponse retryResponse = httpClient.execute(request)) {
                        return handleResponse(retryResponse, handler);
                    }
                } else {
                    return handleResponse(response, handler);
                }
            } finally {
                httpClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception occurred during request execution", e);
        }
    }

    private <T> T handleResponse(CloseableHttpResponse response, JsonResponseHandler<T> handler) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new RuntimeException("Failed to execute request. HTTP Status Code: " + statusCode + " Reason: " + response.getStatusLine().getReasonPhrase());
        }
        String responseString = EntityUtils.toString(response.getEntity());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseString);
        return handler.handle(rootNode);
    }

    private void ensureLoggedIn() throws Exception {
        if (this.accessToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
            try {
                login();
            } catch (Exception e) {
                System.err.println("Failed to re-login NiFI: " + e.getMessage());
                throw new RuntimeException("Failed to re-login NiFi", e);
            }
        }
    }

    private void logRequestHeaders(HttpUriRequest request) {
        logger.info("Request URI: {}", request.getURI());
        Arrays.stream(request.getAllHeaders())
                .forEach(header -> logger.info("Request Header: {}={}", header.getName(), header.getValue()));
    }

    protected String getNifiUrl() {
        return nifiUrl;
    }

    protected String getAccessToken() {
        return accessToken;
    }

    protected interface JsonResponseHandler<T> {
        T handle(JsonNode rootNode) throws IOException;
    }
}
