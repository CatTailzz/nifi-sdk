# NiFi API——集成NiFi Rest API

## 项目介绍
本项目提供了一个与Apache NiFi进行交互的Java API，支持自动化配置、监控和管理NiFi数据流。

## 功能特点
- 连接NiFi实例，获取流程组信息。
- 启动和停止流程组。
- 检查流程组的健康状况。
- 支持自定义主机配置。

## 快速开始
1. 确保您的系统中已安装Java和Maven。
2. 克隆本项目到本地环境：
3. 进入项目目录，使用Maven构建项目：
4. 配置`application.properties`文件，填入NiFi服务器的URL、用户名和密码。

## 配置说明
- `nifi.url`: NiFi服务器的URL地址。
- `nifi.username`: NiFi服务器的用户名。
- `nifi.password`: NiFi服务器的密码。
- `nifi.customHost`: 自定义主机名（可选）。

## API 接口说明

### 获取流程组列表
- **说明**: 获取NiFi实例中所有流程组的列表及其状态信息。
- **返回**: `ResponseVo<List<ProcessGroupInfo>>`，包含流程组的详细信息。

### 启动流程组
- **说明**: 根据流程组ID启动指定的流程组。
- **参数**: `processGroupId` - 流程组的ID。

### 停止流程组
- **说明**: 根据流程组ID停止指定的流程组。
- **参数**: `processGroupId` - 流程组的ID。

### 检查流程组健康状态
- **说明**: 检查指定流程组的健康状态。
- **参数**: `processGroupId` - 流程组的ID。
- **返回**: `ResponseVo<List<StatusDetail>>`，包含健康状态详情。

## 贡献与反馈
我们欢迎任何形式的贡献和反馈。如有任何问题或建议，请提交Issue或Pull Request。

## 版权信息
- 作者：CatTail
- 版权所有：[CatTailzz](https://github.com/CatTailzz)
- 日期：2024年6月27日

