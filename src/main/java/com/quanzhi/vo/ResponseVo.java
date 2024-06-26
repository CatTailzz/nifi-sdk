package com.quanzhi.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/6/26
 * @Copyright: https://github.com/CatTailzz
 */
@Data
public class ResponseVo<T> {
    // 成功标记
    private boolean success;
    // 提示信息
    private String msg;
    // 标识码
    private int code;
    // 错误码
    private int errorCode;
    // 返回的具体数据
    private T data;

    /**
     * 异常栈信息
     */
    private String errStatck;

    /**
     * 额外数据，为兼容data字段被占用的问题
     */
    private Object extraData;

    public static final <T> ResponseVo ok() {
        return ok(null);
    }

    public static <T> ResponseVo<T> ok(T data) {
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setSuccess(true);
        responseVo.setData(data);
        return responseVo;
    }
    public static <T> ResponseVo<T> ok(T data, String msg) {
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setSuccess(true);
        responseVo.setMsg(msg);
        responseVo.setData(data);
        return responseVo;
    }

    public static <T> ResponseVo<T> ok(int code, T data, String msg) {
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setSuccess(true);
        responseVo.setCode(code);
        responseVo.setMsg(msg);
        responseVo.setData(data);
        return responseVo;
    }

    public static final <T> ResponseVo ok(Boolean flag, String msg) {
        ResponseVo responseVo = new ResponseVo();
        responseVo.setSuccess(flag);
        responseVo.setData(msg);
        return responseVo;
    }

    public static final <T> ResponseVo error(String msg, Integer errorCode) {
        ResponseVo responseVo = new ResponseVo();
        responseVo.setSuccess(false);
        responseVo.setMsg(msg);
        responseVo.setErrorCode(errorCode);
        return responseVo;
    }

    public static final <T> ResponseVo error(String msg, Integer errorCode, T data) {
        ResponseVo responseVo = new ResponseVo();
        responseVo.setSuccess(false);
        responseVo.setMsg(msg);
        responseVo.setErrorCode(errorCode);
        responseVo.setData(data);
        return responseVo;
    }

    public static <T> ResponseVo<T> error(String msg) {
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setSuccess(false);
        responseVo.setMsg(msg);
        responseVo.setErrorCode(500);
        return responseVo;
    }

    public static final <T> ResponseVo error(int errorCode, String msg) {
        ResponseVo responseVo = new ResponseVo();
        responseVo.setSuccess(false);
        responseVo.setMsg(msg);
        responseVo.setErrorCode(errorCode);
        return responseVo;
    }

    @Override
    public String toString(){
        return JSONObject.toJSONString(this);
    }
}
