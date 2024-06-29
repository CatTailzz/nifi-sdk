package com.quanzhi.vo;

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
@NoArgsConstructor
@AllArgsConstructor
public class ProcessGroupInfo {

    private String id;

    private String name;

    private String state;

    private String read;

    private String written;

}
