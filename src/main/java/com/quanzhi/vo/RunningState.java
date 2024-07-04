package com.quanzhi.vo;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/7/4
 * @Copyright: https://github.com/CatTailzz
 */
public enum RunningState {
    NOT_RUNNING("未运行"),
    RUNNING("运行中"),
    ABNORMAL_END("异常结束"),
    NORMAL_END("正常结束");

    private final String description;

    RunningState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
