package com.quanzhi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quanzhi.client.NifiClient;
import com.quanzhi.exception.NifiClientException;
import com.quanzhi.vo.ProcessGroupInfo;
import com.quanzhi.vo.ResponseVo;
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

    // 获取ProcessGroups列表以及信息
    public ResponseVo getProcessGroups() {
        try {
            String rootGroupId = nifiClient.getRootProcessGroupId();
            List<JsonNode> processGroups = nifiClient.getProcessGroups(rootGroupId);

            List<ProcessGroupInfo> processGroupInfos = processGroups.stream().map(group -> {
                String id = group.path("id").asText();
                String name = group.path("component").path("name").asText();
                int activeThreadCount = group.path("status").path("aggregateSnapshot").path("activeThreadCount").asInt();
                String state = activeThreadCount > 0 ? "RUNNING" : "STOPPED";
                return new ProcessGroupInfo(id, name, state);
            }).collect(Collectors.toList());

            return ResponseVo.ok(200, processGroupInfos, "获取成功");

        } catch (Exception e) {
            return ResponseVo.error(500, "获取失败");
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

    // 按id查询processGroup的健康详情
    public ResponseVo ProcessGroupHealth(String processGroupId) throws Exception {
        try {
            return nifiClient.checkProcessGroupStatus(processGroupId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVo.error(500, "无法获取状态");
        }
    }



}
