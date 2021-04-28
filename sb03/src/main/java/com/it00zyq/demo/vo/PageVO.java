package com.it00zyq.demo.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 分页VO
 * @author IT00ZYQ
 * @date 2021/4/27 15:09
 **/
@Data
@Builder
public class PageVO {

    private Integer pageNum;
    private Integer pageSize;
    private Integer pageTotal;
    private Integer itemTotal;
    private List<DataListVO> items;

}
