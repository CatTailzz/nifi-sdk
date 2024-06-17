package com.quanzhi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

public class NifiClientTest {
    private static final String NIFI_URL = "https://nifi-app-0:8443/nifi-api";
    private static final String LOGIN_ENDPOINT = "/access/token";
    private static final String PROCESS_GROUPS_ENDPOINT = "/flow/process-groups/";
    private static final String PROCESSORS_ENDPOINT = "/processors/";

    private String accessToken;

    public void login(String username, String password) throws Exception {
        CloseableHttpClient client = HttpClients.custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        HttpPost post = new HttpPost(NIFI_URL + LOGIN_ENDPOINT);

        StringEntity entity = new StringEntity("username=" + username + "&password=" + password);
        entity.setContentType("application/x-www-form-urlencoded");
        post.setEntity(entity);

        try (CloseableHttpResponse response = client.execute(post)) {
            String responseString = EntityUtils.toString(response.getEntity());
            this.accessToken = responseString.trim();
        } finally {
            client.close();
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public JsonNode getProcessGroups(String parentGroupId) throws Exception {
        String url = NIFI_URL + PROCESS_GROUPS_ENDPOINT + parentGroupId;
        CloseableHttpClient client = createClient();
        HttpGet get = new HttpGet(url);
        get.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = client.execute(get)) {
            String responseString = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(responseString);
        } finally {
            client.close();
        }
    }

    public void startProcessor(String processorId) throws Exception {
        updateProcessorState(processorId, "RUNNING");
    }

    public void stopProcessor(String processorId) throws Exception {
        updateProcessorState(processorId, "STOPPED");
    }

    private void updateProcessorState(String processorId, String state) throws Exception {
        String url = NIFI_URL + PROCESSORS_ENDPOINT + processorId + "/run-status";
        CloseableHttpClient client = createClient();
        HttpPut put = new HttpPut(url);
        put.setHeader("Authorization", "Bearer " + accessToken);
        put.setHeader("Content-Type", "application/json");

        int currentVersion = getCurrentProcessorVersion(processorId);

        String json = "{\"revision\":{\"version\":" + currentVersion + "},\"state\":\"" + state + "\"}";
        StringEntity entity = new StringEntity(json);
        put.setEntity(entity);

        try (CloseableHttpResponse response = client.execute(put)) {
            // Check response status
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed to update processor state: " + response.getStatusLine().getReasonPhrase());
            }
        } finally {
            client.close();
        }
    }

    private int getCurrentProcessorVersion(String processorId) throws Exception {
        String url = NIFI_URL + PROCESSORS_ENDPOINT + processorId;
        CloseableHttpClient client = createClient();
        HttpGet get = new HttpGet(url);
        get.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = client.execute(get)) {
            String responseString = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseString);

            // Extract version from JSON response
            int version = rootNode.get("revision").get("version").asInt();
            return version;
        } finally {
            client.close();
        }
    }

    private CloseableHttpClient createClient() throws Exception {
        return HttpClients.custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }

    public void startProcessGroup(String processGroupId) throws Exception {
        // Start all processors in the process group
        JsonNode processGroup = getProcessGroups(processGroupId);
        JsonNode processors = processGroup.get("processGroupFlow").get("flow").get("processors");

        if (processors != null) {
            for (JsonNode processor : processors) {
                String processorId = processor.get("id").asText();
                startProcessor(processorId);
            }
        }

        // Start all child process groups recursively
        JsonNode childProcessGroups = processGroup.get("processGroupFlow").get("flow").get("processGroups");
        if (childProcessGroups != null) {
            for (JsonNode childProcessGroup : childProcessGroups) {
                String childProcessGroupId = childProcessGroup.get("id").asText();
                startProcessGroup(childProcessGroupId);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        NifiClientTest client = new NifiClientTest();
        client.login("sysadmin", "qzkj@1001@1001");
        System.out.println("Token: " + client.getAccessToken());

        // 获取任务流
        JsonNode processGroups = client.getProcessGroups("root");
        System.out.println("Process Groups: " + processGroups);

        // 启动整个任务组
        client.startProcessGroup("c7d3ca37-85ea-30ce-e8c5-2b5634307f3a");

        // 关闭任务
        // client.stopProcessor("1030bd87-0190-1000-54f0-f60a1a08c6f6");
    }
}
