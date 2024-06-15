package com.quanzhi.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NifiClient extends AbstractNifiClient{

    public NifiClient(@Value("${nifi.url}") String nifiUrl,
                      @Value("${nifi.username}") String username,
                      @Value("${nifi.password}") String password) throws Exception {
        super(nifiUrl, username, password);
    }


    // 获取根流程组ID
    public String getRootProcessGroupId() throws Exception {
        String url = getNifiUrl() + PROCESS_GROUPS_ENDPOINT + "root";
        HttpGet get = new HttpGet(url);

        return executeRequest(get, rootNode ->
                rootNode.get("processGroupFlow").get("id").asText());
    }

    // 获取所有子流程组
    public List<JsonNode> getProcessGroups(String parentGroupId) throws Exception {
        String url = getNifiUrl() + PROCESS_GROUPS_ENDPOINT + parentGroupId;
        HttpGet get = new HttpGet(url);

        return executeRequest(get, rootNode -> {
            List<JsonNode> processGroups = new ArrayList<>();
            rootNode.get("processGroupFlow").get("flow").get("processGroups").forEach(processGroups::add);
            return processGroups;
        });
    }

    // 获取流程组中的处理器
    private List<JsonNode> getProcessors(String processGroupId) throws Exception {
        String url = getNifiUrl() + PROCESS_GROUPS_ENDPOINT + processGroupId;
        HttpGet get = new HttpGet(url);

        return executeRequest(get, rootNode -> {
            List<JsonNode> processors = new ArrayList<>();
            rootNode.get("processGroupFlow").get("flow").get("processors").forEach(processors::add);
            return processors;
        });
    }

    // 启动单个处理器
    public void startProcessor(String processorId) throws Exception {
        updateProcessorState(processorId, "RUNNING");
    }

    // 关闭单个处理器
    public void stopProcessor(String processorId) throws Exception {
        updateProcessorState(processorId, "STOPPED");
    }

    // 修改处理器状态
    private void updateProcessorState(String processorId, String state) throws Exception {
        String url = getNifiUrl() + PROCESSORS_ENDPOINT + processorId + "/run-status";
        HttpPut put = new HttpPut(url);
        put.setHeader("Authorization", "Bearer " + getAccessToken());
        put.setHeader("Content-Type", "application/json");

        int currentVersion = getCurrentProcessorVersion(processorId);

        String json = "{\"revision\":{\"version\":" + currentVersion + "},\"state\":\"" + state + "\"}";
        put.setEntity(new StringEntity(json));


        try (CloseableHttpResponse response = getHttpClient().execute(put)) {
            int statusCode = response.getStatusLine().getStatusCode();
            // Check response status
            if (statusCode != 200) {
                throw new RuntimeException("Failed to update processor state: " + response.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception occurred during updating processor state", e);
        }
//        executeRequest(put, rootNode -> {
//            System.out.println("Processor state updated successfully");
//            return null;
//        });
    }

    // 获取处理器版本号
    private int getCurrentProcessorVersion(String processorId) throws Exception {
        String url = getNifiUrl() + PROCESSORS_ENDPOINT + processorId;
        HttpGet get = new HttpGet(url);
//        get.setHeader("Authorization", "Bearer " + getAccessToken());

        return executeRequest(get, rootNode ->
                rootNode.get("revision").get("version").asInt());
    }

    public void startProcessGroup(String processGroupId) throws Exception {
        // 先启动子流程组
        List<JsonNode> childProcessGroups = getProcessGroups(processGroupId);
        for (JsonNode childProcessGroup : childProcessGroups) {
            String childProcessGroupId = childProcessGroup.get("id").asText();
            startProcessGroup(childProcessGroupId);
        }

        // 启动当前流程组中的处理器
        List<JsonNode> processors = getProcessors(processGroupId);
        for (JsonNode processor : processors) {
            String processorId = processor.get("id").asText();
            System.out.println("Starting processor: " + processorId);
            startProcessor(processorId);
        }
    }

    public void stopProcessGroup(String processGroupId) throws Exception {
        // 先停止子流程组
        List<JsonNode> childProcessGroups = getProcessGroups(processGroupId);
        for (JsonNode childProcessGroup : childProcessGroups) {
            String childProcessGroupId = childProcessGroup.get("id").asText();
            stopProcessGroup(childProcessGroupId);
        }

        // 停止当前流程组中的处理器
        List<JsonNode> processors = getProcessors(processGroupId);
        for (JsonNode processor : processors) {
            String processorId = processor.get("id").asText();
            stopProcessor(processorId);
        }
    }



}
