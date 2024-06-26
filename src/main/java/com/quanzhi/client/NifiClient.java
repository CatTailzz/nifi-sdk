package com.quanzhi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.quanzhi.vo.ResponseVo;
import com.quanzhi.vo.StatusDetail;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // 获取 Processor 的状态
    public Map<String, String> getProcessorStatuses(String processGroupId) throws Exception {
        List<JsonNode> processors = getProcessors(processGroupId);
        Map<String, String> statuses = new HashMap<>();
        for (JsonNode processor : processors) {
            String processorId = processor.get("id").asText();
            String runStatus = processor.get("status").get("runStatus").asText();
            statuses.put(processorId, runStatus);
        }
        return statuses;
    }

    // 获取 Processor 的 Bulletins
    public Map<String, List<String>> getProcessorBulletins(String processGroupId) throws Exception {
        String url = getNifiUrl() + PROCESS_GROUPS_ENDPOINT + processGroupId;
        HttpGet get = new HttpGet(url);

        return executeRequest(get, rootNode -> {
            Map<String, List<String>> bulletinsMap = new HashMap<>();
            if (rootNode.has("processGroupFlow")) {
                JsonNode processGroupFlow = rootNode.get("processGroupFlow");
                if (processGroupFlow.has("flow") && processGroupFlow.get("flow").has("processors")) {
                    processGroupFlow.get("flow").get("processors").forEach(processor -> {
                        if (processor.has("bulletins")) {
                            String processorId = processor.get("id").asText();
                            processor.get("bulletins").forEach(bulletin -> {
                                if (bulletin.has("bulletin")) {
                                    JsonNode bulletinNode = bulletin.get("bulletin");
                                    if (bulletinNode.has("sourceId") && bulletinNode.has("message")) {
                                        String message = bulletinNode.get("message").asText();
                                        bulletinsMap.computeIfAbsent(processorId, k -> new ArrayList<>()).add(message);
                                    }
                                }
                            });
                        }
                    });
                }
            }
            return bulletinsMap;
        });
    }

    public ResponseVo checkProcessGroupStatus(String processGroupId) throws Exception {
        Map<String, String> processorStatuses = getProcessorStatuses(processGroupId);
        Map<String, List<String>> bulletins = getProcessorBulletins(processGroupId);

        boolean allRunning = processorStatuses.values().stream().allMatch(status -> status.equals("Running"));
        boolean allStopped = processorStatuses.values().stream().allMatch(status -> status.equals("Stopped"));
        boolean hasInvalid = processorStatuses.values().stream().anyMatch(status -> status.equals("Invalid"));
        boolean hasErrors = bulletins.values().stream().anyMatch(messages -> !messages.isEmpty());

        List<StatusDetail> details = new ArrayList<>();

        if (hasErrors) {
            for (Map.Entry<String, List<String>> entry : bulletins.entrySet()) {
                String processorId = entry.getKey();
                List<String> messages = entry.getValue();
                details.add(new StatusDetail(processorId, processorStatuses.get(processorId), messages));
            }
            return ResponseVo.ok(500, details, "执行异常");
        } else if (hasInvalid) {
            for (Map.Entry<String, String> entry : processorStatuses.entrySet()) {
                String processorId = entry.getKey();
                String status = entry.getValue();
                if (status.equals("Invalid")) {
                    details.add(new StatusDetail(processorId, status, bulletins.getOrDefault(processorId, new ArrayList<>())));
                }
            }
            return ResponseVo.ok(400, details, "配置错误");
        } else if (allStopped) {
            return ResponseVo.ok(201, details, "停止状态");
        } else if (allRunning) {
            return ResponseVo.ok(200, details, "正常运行");
        } else {
            return ResponseVo.ok(202, details, "未知状态，请检查控制面板");
        }
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

        executeRequest(put, rootNode -> {
            System.out.println("Processor state updated successfully");
            return null;
        });
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
