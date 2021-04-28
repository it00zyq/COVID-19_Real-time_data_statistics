package com.it00zyq.demo.controller;

import com.it00zyq.demo.service.DataService;
import com.it00zyq.demo.utils.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author IT00ZYQ
 * @date 2021/4/27 14:55
 **/
@RestController
public class DataController {

    private final DataService dataService;
    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    /**
     * 获取数据
     * @param pageNum 页码
     * @param pageSize 页面容量
     * @param country 国家筛选
     * @return R
     */
    @GetMapping("data")
    public R getData(@RequestParam Integer pageNum,
                     @RequestParam Integer pageSize,
                     @RequestParam(required = false) String country) {
        return R.ok().data("data", dataService.getData(pageNum, pageSize, country));
    }

    /**
     * 获取详情数据
     * @param id 数据ID
     * @return R
     */
    @GetMapping("detail")
    public R detail(@RequestParam Integer id) {
        return R.ok().data("data", dataService.detail(id));
    }


}
