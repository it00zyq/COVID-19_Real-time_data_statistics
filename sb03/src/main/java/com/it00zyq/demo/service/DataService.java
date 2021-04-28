package com.it00zyq.demo.service;

import com.it00zyq.demo.vo.DataVO;
import com.it00zyq.demo.vo.DateDataVO;
import com.it00zyq.demo.vo.PageVO;

import java.util.List;
import java.util.Map;

/**
 * @author IT00ZYQ
 * @date 2021/4/25 15:20
 **/
public interface DataService {

    /**
     * 获取数据
     * @param pageNum 页码
     * @param pageSize 页面容量
     * @param country 根据国家获取
     * @return PageVO
     */
    PageVO getData(Integer pageNum, Integer pageSize, String country);

    /**
     * 获取详情数据
     * @param id id
     * @return Map
     */
    List<DateDataVO> detail(Integer id);

}
