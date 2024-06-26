package com.quanzhi.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/6/26
 * @Copyright: https://github.com/CatTailzz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusDetail {

    private String processorId;

    private String status;

    private List<String> errorMessages;
}
