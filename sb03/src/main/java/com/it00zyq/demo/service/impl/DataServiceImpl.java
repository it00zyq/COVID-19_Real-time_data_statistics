package com.it00zyq.demo.service.impl;

import com.it00zyq.demo.config.TimeTask;
import com.it00zyq.demo.service.DataService;
import com.it00zyq.demo.vo.DataListVO;
import com.it00zyq.demo.vo.DataVO;
import com.it00zyq.demo.vo.DateDataVO;
import com.it00zyq.demo.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author IT00ZYQ
 * @date 2021/4/25 15:20
 **/
@Service
@Slf4j
public class DataServiceImpl implements DataService {

    private final TimeTask timeTask;
    public DataServiceImpl(TimeTask timeTask) {
        this.timeTask = timeTask;
    }

    private List<DataVO> getData() {
        if (timeTask.getDataList().size() == 0) {
            try {
                timeTask.pullData();
            } catch (IOException e) {
                log.error("数据拉取失败");
            }
        }
        return timeTask.getDataList();
    }

    @Override
    public PageVO getData(Integer pageNum, Integer pageSize, String country) {
        List<DataListVO> items;
        int fromIndex = (pageNum - 1) * pageSize;
        int endIndex = fromIndex + pageSize;
        // 判断是否需要进行国家筛选
        if (country != null && country.length() != 0) {
            items = getData()
                    .stream()
                    .filter(e -> country.equals(e.getCountry()))
                    .map(DataServiceImpl::convert)
                    .collect(Collectors.toList());
        } else {
            items = getData()
                    .stream()
                    .map(DataServiceImpl::convert)
                    .collect(Collectors.toList());
        }

        // 计算总页数与总数据数
        int pageTotal = items.size() != 0 ? items.size() / pageSize + 1 : 0;
        int itemTotal = items.size();
        // 分页
        items = items.subList(Math.min(fromIndex, items.size()), Math.min(endIndex, items.size()));

        return PageVO.builder()
                .items(items)
                .itemTotal(itemTotal)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pageTotal(pageTotal)
                .build();
    }

    @Override
    public List<DateDataVO> detail(Integer id) {
        // 根据id获取指定的地区的数据
        List<DataVO> vos = getData().stream().filter(e -> e.getId().equals(id)).collect(Collectors.toList());
        if (vos.size() != 0) {
            return vos.get(0).getDataList();
        } else {
            return new ArrayList<>(0);
        }
    }

    /**
     * DataVO -> DataListVO
     * @param e DataVO
     * @return DataListVO
     */
    private static DataListVO convert(DataVO e) {
        Integer todayCount = 0;
        // 新增确诊病例
        int newCount = 0;
        if (e.getDataList().size() >= 1) {
            todayCount = e.getDataList().get(0).getCount();
            newCount = todayCount;
        }
        // 计算新增确诊病例
        if (e.getDataList().size() >= 2) {
            newCount = e.getDataList().get(0).getCount() - e.getDataList().get(1).getCount();
        }
        return DataListVO.builder()
                .country(e.getCountry())
                .province(e.getProvince())
                .total(todayCount)
                .newCount(newCount)
                .id(e.getId())
                .build();
    }
}
