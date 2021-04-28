package com.it00zyq.demo.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author IT00ZYQ
 * @date 2021/4/27 15:26
 **/
@Data
@Builder
public class DataListVO {
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
     * 新增确诊
     */
    private Integer newCount;
}
