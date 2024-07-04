package com.quanzhi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quanzhi.utils.ProcessGroupStateSerializer;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/7/4
 * @Copyright: https://github.com/CatTailzz
 */
//@JsonSerialize(using = ProcessGroupStateSerializer.class)
public enum ProcessGroupState {
    EXECUTION_EXCEPTION("执行异常"),
    CONFIGURATION_ERROR("配置错误"),
    STOPPED("停止状态"),
    NORMAL_RUNNING("正常运行"),
    PARTIAL_COMPONENT_STOP("部分组件停止"),
    UNKNOWN("未知错误");

    private final String state;

    ProcessGroupState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return state;
    }

    public static ProcessGroupState fromString(String state) {
        for (ProcessGroupState s : ProcessGroupState.values()) {
            if (s.state.equalsIgnoreCase(state)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No matching enum for state: " + state);
    }

}
