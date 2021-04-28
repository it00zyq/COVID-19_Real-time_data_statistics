package com.it00zyq.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 返回数据类
 * @author IT00ZYQ
 * @date 2021/4/25 15:23
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataVO {

    /**
     * 编号
     */
    private Integer id;

    /**
     * 省份/州
     */
    private String province;

    /**
     * 国家
     */
    private String country;

    /**
     * 确诊人数
     */
    private Integer total;

    /**
     * 日期
     */
    private List<String> dateList;

    /**
     * 病例数据
     */
    private Map<String, Integer> dataMap;

    /**
     * 每日病例数据
     */
    private List<DateDataVO> dataList;

}
