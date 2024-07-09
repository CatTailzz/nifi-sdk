package com.quanzhi.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/6/26
 * @Copyright: https://github.com/CatTailzz
 */
@Data
public class ProcessGroupInfo {

    private String id;

    private String name;

    private ProcessGroupState state;

    private String comments;


    private String read;

    private String written;

    public ProcessGroupInfo(String id, String name, ProcessGroupState state, String comments, String read, String written) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.comments = comments;
        this.read = read;
        this.written = written;
    }

    public ProcessGroupInfo(){

    }



    @JsonProperty("isFinished")
    public boolean isFinished() {
        return "0 bytes".equals(read) && "0 bytes".equals(written);
    }

    @JsonProperty("runningState")
    public RunningState getRunningState() {
        if (state == ProcessGroupState.STOPPED) {
            return RunningState.NOT_RUNNING;
        } else if (state == ProcessGroupState.NORMAL_RUNNING) {
            if ("0 bytes".equals(read) && "0 bytes".equals(written)) {
                return RunningState.NORMAL_END;
            } else {
                return RunningState.RUNNING;
            }
        } else if (state == ProcessGroupState.EXECUTION_EXCEPTION || state == ProcessGroupState.CONFIGURATION_ERROR) {
            return RunningState.ABNORMAL_END;
        }
        return RunningState.ABNORMAL_END; // 默认异常结束状态
    }
}
