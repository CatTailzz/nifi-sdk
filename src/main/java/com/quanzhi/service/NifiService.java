package com.quanzhi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quanzhi.client.NifiClient;
import com.quanzhi.exception.NifiClientException;
import com.quanzhi.vo.ProcessGroupInfo;
import com.quanzhi.vo.ProcessGroupState;
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


    /**
     * 获取ProcessGroups列表以及信息
     * @return 返回ResponseVo，内部封装了ProcessGroupInfo列表，表示根目录下所有的group
     *      每一条ProcessGroupInfo包含了groupId、name、state（健康状态）、comments（用于判断类型）、runningState（运行状态）、isFinished（是否已经停止读写了）、read、written
     *
     *      state是枚举类，枚举值有：STOPPED（停止状态）、NORMAL_RUNNING（正常运行）、PARTIAL_COMPONENT_STOP（部分组件停止）、UNKNOWN（未知错误）、EXECUTION_EXCEPTION（执行异常）、CONFIGURATION_ERROR（配置错误）
     *      isFinished是布尔值，根据read、written是否为0，判断是否已经停止读写了
     *      runningState是枚举类，枚举值有：NOT_RUNNING（未运行）、RUNNING（运行中）、ABNORMAL_END（异常结束）、NORMAL_END（正常结束）。根据state和isFinished综合判断。
     *      comments是String类型，表示该ProcessGroup的类型，用于判断任务类型。
     *      read和written是String类型，表示该ProcessGroup在最近一段时间内读写了多少数据。
     *
     *      code=200表示获取成功，
     *      code=500表示获取失败。
     */
    public ResponseVo<List<ProcessGroupInfo>> getProcessGroups() {
        try {
            String rootGroupId = nifiClient.getRootProcessGroupId();
            List<JsonNode> processGroups = nifiClient.getProcessGroups(rootGroupId);

            List<ProcessGroupInfo> processGroupInfos = processGroups.stream().map(group -> {
                String id = group.path("id").asText();
                String name = group.path("component").path("name").asText();
                String comments = group.path("component").path("comments").asText();

                ResponseVo<List<StatusDetail>> responseVo;
                try {
                    responseVo = processGroupHealth(id);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                // 获取状态信息，并将其转换为 ProcessGroupState 枚举类型
                String stateString = responseVo.getMsg();
                ProcessGroupState state;
                try {
                    state = ProcessGroupState.fromString(stateString);
                } catch (IllegalArgumentException e) {
                    state = ProcessGroupState.UNKNOWN;  // 默认状态或处理无效状态的逻辑
                }

                String read = group.path("status").path("aggregateSnapshot").path("read").asText();
                String written = group.path("status").path("aggregateSnapshot").path("written").asText();

                return new ProcessGroupInfo(id, name, state, comments, read, written);
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


    /**
     * 按id查询processGroup的健康详情
     * @param processGroupId，要检查的流程组的ID
     * @return ResponseVo，内部封装了StatusDetail列表，表示该processGroup下所有组件的健康详情，仅当异常状态和配置错误状态才有值。
     *          msg封装了状态信息，可以不关注，因为getProcessGroups方法里为状态提供了更好的枚举类封装
     *
     *          code=501，无法获取状态，
     *          code=500，执行异常，将返回错误的detail信息，
     *          code=400，配置错误，会返回detail信息，
     *          code=201，停止状态，
     *          code=200，正常运行，
     *          code=202，部分组件停止，出现情况较少。
     * @throws Exception
     */
    public ResponseVo<List<StatusDetail>> processGroupHealth(String processGroupId) throws Exception {
        try {
            return nifiClient.checkProcessGroupStatus(processGroupId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVo.error(501, "无法获取状态");
        }
    }



}
