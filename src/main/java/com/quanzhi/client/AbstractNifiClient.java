package com.quanzhi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;

/**
 * @description: 管理登录和连接的公共逻辑，包装一些发请求方法
 * @author：CatTail
 * @date: 2024/6/14
 * @Copyright: https://github.com/CatTailzz
 */
public abstract class AbstractNifiClient implements Constants{

    private final String nifiUrl;
    private final String username;
    private final String password;
    private String accessToken;
    private long tokenExpiryTime;
    private final CloseableHttpClient httpClient;

    public AbstractNifiClient(String nifiUrl, String username, String password) throws Exception {
        this.nifiUrl = nifiUrl;
        this.username = username;
        this.password = password;
        this.httpClient = createHttpClient();
        login(); // 自动登录
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

        // Create a connection manager with custom SSL context
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE);

        // Create HTTP client with custom SSL context
        return HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
    }

    private void login() throws Exception {
        HttpPost post = new HttpPost(nifiUrl + LOGIN_ENDPOINT);

        StringEntity entity = new StringEntity("username=" + username + "&password=" + password);
        entity.setContentType("application/x-www-form-urlencoded");
        post.setEntity(entity);


        try (CloseableHttpResponse response = httpClient.execute(post)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 201) {
                throw new RuntimeException("Failed to login. HTTP Status Code: " + statusCode);
            }
            String responseString = EntityUtils.toString(response.getEntity());
            this.accessToken = responseString.trim();
            this.tokenExpiryTime = System.currentTimeMillis() + 3600 * 1000 * 6; // 假设 token 有效期为 1 小时
        } catch (Exception e) {
            // handle exception (e.g., log error)
            e.printStackTrace();
            throw new RuntimeException("Failed to login", e);
        }
    }

    protected <T> T executeRequest(HttpUriRequest request, JsonResponseHandler<T> handler) throws Exception {
        try {
            ensureLoggedIn();
            request.setHeader("Authorization", "Bearer " + accessToken);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 403) {
                    // 重新登录并重试请求
                    login();
                    request.setHeader("Authorization", "Bearer " + accessToken);
                    try (CloseableHttpResponse retryResponse = httpClient.execute(request)) {
                        return handleResponse(retryResponse, handler);
                    }
                } else {
                    return handleResponse(response, handler);
                }
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
            login();
        }
    }

    protected String getNifiUrl() {
        return nifiUrl;
    }

    protected String getAccessToken() {
        return accessToken;
    }

    protected CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    protected interface JsonResponseHandler<T> {
        T handle(JsonNode rootNode) throws IOException;
    }
}
