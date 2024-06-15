package com.quanzhi.client;

import org.springframework.beans.factory.annotation.Value;

public class NifiClientTest {

    public static void main(String[] args) {
        try {
            // 从配置文件或者直接指定这些值
            String nifiUrl = "https://nifi-app-0:8443/nifi-api"; // 替换为你的 NiFi 实例 URL
            String username = "sysadmin"; // 替换为你的 NiFi 用户名
            String password = "qzkj@1001@1001"; // 替换为你的 NiFi 密码
            String processGroupId = "c7d3ca37-85ea-30ce-e8c5-2b5634307f3a"; // 替换为你想启动的流程组的 ID

            // 创建 NifiClient 实例
            NifiClient nifiClient = new NifiClient(nifiUrl, username, password);

            // 获取根流程组 ID 并打印
            String rootProcessGroupId = nifiClient.getRootProcessGroupId();
            System.out.println("Root Process Group ID: " + rootProcessGroupId);

            // 启动指定的流程组
            System.out.println("Starting process group: " + processGroupId);
            nifiClient.startProcessGroup(processGroupId);
            System.out.println("Process group started successfully.");

            // 获取某个 Processor 的 ID 并启动
            // 你需要提供你想要启动的 Processor 的 ID
//            String processorId = "YOUR_PROCESSOR_ID";
//            System.out.println("Starting processor: " + processorId);
//            nifiClient.startProcessor(processorId);
//            System.out.println("Processor started successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error occurred while starting the processor or process group.");
        }
    }
}
