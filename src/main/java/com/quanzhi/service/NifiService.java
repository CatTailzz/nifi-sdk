package com.quanzhi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quanzhi.client.NifiClient;
import com.quanzhi.exception.NifiClientException;
import com.quanzhi.vo.ProcessGroupInfo;
import com.quanzhi.vo.ResponseVo;
import com.quanzhi.vo.StatusDetail;
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

    private final NifiClient nifiClient;

    public NifiService(NifiClient nifiClient) {
        this.nifiClient = nifiClient;
    }

    // 获取ProcessGroups列表以及信息
    public ResponseVo<List<ProcessGroupInfo>> getProcessGroups() {
        try {
            String rootGroupId = nifiClient.getRootProcessGroupId();
            List<JsonNode> processGroups = nifiClient.getProcessGroups(rootGroupId);

            List<ProcessGroupInfo> processGroupInfos = processGroups.stream().map(group -> {
                String id = group.path("id").asText();
                String name = group.path("component").path("name").asText();
                ResponseVo<List<StatusDetail>> responseVo;
                try {
                    responseVo = processGroupHealth(id);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                String state = responseVo.getMsg();
                String read = group.path("status").path("aggregateSnapshot").path("read").asText();
                String written = group.path("status").path("aggregateSnapshot").path("written").asText();

                return new ProcessGroupInfo(id, name, state, read, written);
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
    public ResponseVo<List<StatusDetail>> processGroupHealth(String processGroupId) throws Exception {
        try {
            return nifiClient.checkProcessGroupStatus(processGroupId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVo.error(501, "无法获取状态");
        }
    }



}
