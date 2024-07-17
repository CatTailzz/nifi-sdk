package com.quanzhi.vo;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/7/17
 * @Copyright: https://github.com/CatTailzz
 */
@Data
public class ApiDocument {
    private String method;
    private String url;
    private String description;
    private List<Parameter> requestParameters;
    private List<Parameter> responseParameters;
    private String requestExample;
    private String responseExample;

    @Data
    public static class Parameter {
        private String name;
        private String type;
        private String description;
        private boolean required;

    }
}
