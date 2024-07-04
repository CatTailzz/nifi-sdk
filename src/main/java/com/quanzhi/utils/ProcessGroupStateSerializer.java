package com.quanzhi.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.quanzhi.vo.ProcessGroupState;

import java.io.IOException;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/7/4
 * @Copyright: https://github.com/CatTailzz
 */
public class ProcessGroupStateSerializer extends StdSerializer<ProcessGroupState> {

    public ProcessGroupStateSerializer() {
        super(ProcessGroupState.class);
    }

    @Override
    public void serialize(ProcessGroupState state, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(state.getState());
    }
}
