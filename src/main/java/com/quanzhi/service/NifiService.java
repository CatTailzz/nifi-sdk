package com.quanzhi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quanzhi.client.NifiClient;
import com.quanzhi.exception.NifiClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/6/14
 * @Copyright: https://github.com/CatTailzz
 */
@Service
public class NifiService {

    @Autowired
    private NifiClient nifiClient;

    // 获取ProcessGroups列表
    public List<Map<String, Object>> getProcessGroups() {
        try {
            String rootGroupId = nifiClient.getRootProcessGroupId();
            List<JsonNode> processGroups = nifiClient.getProcessGroups(rootGroupId);
            return processGroups.stream().map(group -> {
                Map<String, Object> groupMap = new ConcurrentHashMap<>();
                groupMap.put("id", group.get("id").asText());
                groupMap.put("name", group.get("component").get("name").asText());
                groupMap.put("state", group.get("status").get("aggregateSnapshot").get("activeThreadCount").asInt() > 0 ? "RUNNING" : "STOPPED");
                groupMap.put("lastRunTime", group.get("status").get("statsLastRefreshed").asText());
                return groupMap;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new NifiClientException("Failed to get process groups", e);
        }
    }

    // 按id启动processGroup
    public void startProcessGroup(String processGroupId) throws Exception {
        nifiClient.startProcessGroup(processGroupId);
    }

    // 按id停止processGroup
    public void stopProcessGroup(String processGroupId) throws Exception {
        nifiClient.stopProcessGroup(processGroupId);
    }

    // 按id查询processGroupId详情
    public void ProcessGroupDetail(String processGroupId) throws Exception {
//        nifiClient
    }



}
