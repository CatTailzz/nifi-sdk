package com.quanzhi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quanzhi.vo.ApiDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/7/17
 * @Copyright: https://github.com/CatTailzz
 */
public class ApiDocumentUtils {

    public ApiDocument parseConfigProcessor(JsonNode processorNode) throws JsonProcessingException {
        ApiDocument apiDocument = new ApiDocument();
        JsonNode properties = processorNode.path("component").path("config").path("properties");

        if (properties != null) {
            apiDocument.setMethod(properties.path("method").asText(""));
            apiDocument.setUrl(properties.path("url").asText(""));
            apiDocument.setDescription(properties.path("description").asText(""));
            apiDocument.setRequestExample(properties.path("requestExample").asText(""));
            apiDocument.setResponseExample(properties.path("responseExample").asText(""));

            // Parse request parameters
            List<ApiDocument.Parameter> requestParameters = new ArrayList<>();
            String requestParamsString = properties.path("requestParameters").asText("[]");
            if (requestParamsString != null && !requestParamsString.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode requestParamsNode = objectMapper.readTree(requestParamsString);
                if (requestParamsNode.isArray()) {
                    for (JsonNode paramNode : requestParamsNode) {
                        ApiDocument.Parameter param = new ApiDocument.Parameter();
                        param.setName(paramNode.path("name").asText("Unnamed Parameter"));
                        param.setType(paramNode.path("type").asText("String"));
                        param.setDescription(paramNode.path("description").asText("No description"));
                        param.setRequired(paramNode.path("required").asBoolean(false));
                        requestParameters.add(param);
                    }
                }
            }
            apiDocument.setRequestParameters(requestParameters);

            // Parse response parameters
            List<ApiDocument.Parameter> responseParameters = new ArrayList<>();
            String responseParamsString = properties.path("responseParameters").asText("[]");
            if (responseParamsString != null && !responseParamsString.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode responseParamsNode = objectMapper.readTree(responseParamsString);
                if (responseParamsNode.isArray()) {
                    for (JsonNode paramNode : responseParamsNode) {
                        ApiDocument.Parameter param = new ApiDocument.Parameter();
                        param.setName(paramNode.path("name").asText("Unnamed Parameter"));
                        param.setType(paramNode.path("type").asText("String"));
                        param.setDescription(paramNode.path("description").asText("No description"));
                        param.setRequired(paramNode.path("required").asBoolean(false));
                        responseParameters.add(param);
                    }
                }
            }
            apiDocument.setResponseParameters(responseParameters);
        }

        return apiDocument;
    }

    // Convert ApiDocument to JSON
    public JsonNode toJson(ApiDocument apiDocument) {
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put("method", apiDocument.getMethod());
        jsonNode.put("url", apiDocument.getUrl());
        jsonNode.put("description", apiDocument.getDescription());
        jsonNode.put("requestExample", apiDocument.getRequestExample());
        jsonNode.put("responseExample", apiDocument.getResponseExample());

        // Request parameters
        ArrayNode requestParamsArray = JsonNodeFactory.instance.arrayNode();
        apiDocument.getRequestParameters().forEach(param -> {
            ObjectNode paramNode = JsonNodeFactory.instance.objectNode();
            paramNode.put("name", param.getName());
            paramNode.put("type", param.getType());
            paramNode.put("description", param.getDescription());
            paramNode.put("required", param.isRequired());
            requestParamsArray.add(paramNode);
        });
        jsonNode.set("requestParameters", requestParamsArray);

        // Response parameters
        ArrayNode responseParamsArray = JsonNodeFactory.instance.arrayNode();
        apiDocument.getResponseParameters().forEach(param -> {
            ObjectNode paramNode = JsonNodeFactory.instance.objectNode();
            paramNode.put("name", param.getName());
            paramNode.put("type", param.getType());
            paramNode.put("description", param.getDescription());
            paramNode.put("required", param.isRequired());
            responseParamsArray.add(paramNode);
        });
        jsonNode.set("responseParameters", responseParamsArray);

        return jsonNode;
    }
}
