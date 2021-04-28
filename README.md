```
东莞理工学院网络空间安全学院
实验名称	全球新型冠状病毒实时数据统计应用程序的设计与实现
```

# 一、	实验目标：
本实验项目要求学生综合运用课堂学习的Spring MVC、Spring内置Http同步客户端RestTemplate工具、Spring计划任务功能的知识内容，
设计实现一个全球新型冠状病毒实时数据统计应用程序。 实 验内容包括Http同步客户端爬取Github仓库的CSV文件数据 ，
并使用Angular、React或Vue等的前 端框架开发一个数据展示仪表板的应用程序。应用程序可以每天自动更新数据，
用户通过本实验实 现的应用程序可以实时了解到全球新型冠状病毒确诊数据等。
# 二、	实验条件：
## 2.1 硬件条件：
CPU:i3以上；
内存：8g内存以上
## 2.2 软件条件：
IntelliJ IDEA 2021版本，
Mysql 5.6 以上
JDK 1.8以上
# 三、	实验内容：
（1）使用IDEA构建spring boot项目
（2）利用定时任务实现每天自动抓取github等网站的csv数据。
参考：
https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_time_series 
https://gitee.com/IOP_tech/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_time_series
（4）利用第三方包解析csv数据，并将有用的数据保存到数据库。
（5）利用前端表图如echarts等实现界面可视化
（6）要求界面尽可能的美观，代码有注释。
# 四、	实验步骤(可与实验内容合并描述)：
## 导入maven依赖
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- 用于解析CSV文件的工具 -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-csv</artifactId>
        <version>1.8</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## 配置yml文件
```
server:
  port: 9002

data:
  #url: https://github.com/CSSEGISandData/COVID-19/blob/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv
  #url: https://gitee.com/dgut-sai/COVID-19/raw/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv
  # 获取数据的地址
  url: https://raw.fastgit.org/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv
  # 每天8点重新拉取数据
  cron: 0 0 8 * * *
```


## 跨域配置与其他配置
```java
package com.it00zyq.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 * @author IT00ZYQ
 * @date 2021/4/9 21:31
 **/
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    /**
     * 跨域问题解决
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*");
    }

}
```

```java
package com.it00zyq.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 配置类
 * @author IT00ZYQ
 * @date 2021/4/27 14:40
 **/
@Configuration
public class MyConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.setReadTimeout(Duration.ofSeconds(60))
                .setConnectTimeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * 项目启动时拉取数据
     * @param timeTask 定时任务
     * @return CommandLineRunner
     */
    @Bean
    public CommandLineRunner commandLineRunner(TimeTask timeTask){
        return args -> timeTask.pullData();
    }

}
```
## 爬取数据定时任务
```java
package com.it00zyq.demo.config;

import com.it00zyq.demo.vo.DataVO;
import com.it00zyq.demo.vo.DateDataVO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author IT00ZYQ
 * @date 2021/4/25 15:29
 **/
@Configuration
@EnableConfigurationProperties(DataProperties.class)
@EnableScheduling
public class TimeTask {

    private List<DataVO> dataList = new ArrayList<>();
    private final RestTemplate restTemplate;
    private final DataProperties properties;

    public TimeTask(RestTemplate restTemplate, DataProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Scheduled(cron = "${data.cron}")
    public void pullData() throws IOException {
        // 构造请求体
        RequestEntity<Void> requestEntity = RequestEntity.get(URI.create(properties.getUrl()))
                .headers(httpHeaders -> httpHeaders.add("User-Agent", "Mozilla/5.0"))
                .build();
        // 发起请求，拉取数据
        ResponseEntity<Resource> responseEntity = restTemplate.exchange(requestEntity, Resource.class);
        // 获取响应数据
        Resource resource = responseEntity.getBody();

        if (resource == null) {
            throw new RuntimeException("数据拉取失败");
        }

        //读取CSV文件
        Reader in = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);

        boolean isHeader = true;
        int index = 1;
        // 日期列表
        List<String> dateList = new ArrayList<>();
        for (CSVRecord record : records) {
            //获取表头
            if (isHeader) {
                isHeader = false;
                //从第5列开始是日期/病例
                for (int i = record.size() - 1; i >= 4; i--) {
                    String[] split = record.get(i).split("/");
                    dateList.add("20"+split[2]+"-"+split[0]+"-"+split[1]);
                }
            } else {
                //获取数据
                DataVO data = new DataVO();
                Map<String,Integer> map = new LinkedHashMap<>();
                List<DateDataVO> list = new ArrayList<>();
                //从第5列开始是日期/病例
                for (int i = record.size() - 1, j = 0; i >= 4; i--, j++) {
                    map.put(dateList.get(j), Integer.valueOf(record.get(i)));
                    list.add(DateDataVO.builder()
                            .date(dateList.get(j))
                            .count(Integer.valueOf(record.get(i)))
                            .build());
                }
                data.setId(index ++);
                data.setDataMap(map);
                if (record.size() > 1) {
                    data.setCountry(record.get(1));
                }
                data.setProvince(record.get(0));
                data.setDataList(list);
                dataList.add(data);
            }
        }

    }

    /**
     * 获取数据
     * @return 数据
     */
    public List<DataVO> getDataList() {
        return dataList;
    }
}
```
## 控制层

```java
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
```
## 服务层
```java
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
```

```java
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
```
## 前端主要页面代码
```
<template>
  <div class="hello">
    <div class="container">
        <div class="search">
          <el-input style="display: inline-block; width: 250px; position: absolute" v-model="country" size="mini" placeholder="请输入国家名称"></el-input>
          <el-button style="display: inline-block; position: absolute; right: 0px" type="primary" size="mini" @click="getDataList()">搜索</el-button>
        </div>
      <template>
        <template>
          <el-table
            fit
            border
            :data="dataList"
            highlight-current-row >
            <el-table-column
              prop="id"
              label="序号"
              width="120">
            </el-table-column>
            <el-table-column
              prop="country"
              label="国家/地区"
              width="250">
            </el-table-column>
            <el-table-column
              prop="province"
              label="省份/州"
              width="250">
            </el-table-column>
            <el-table-column
              prop="total"
              label="累计确诊病例"
              width="180">
            </el-table-column>
            <el-table-column
              prop="newCount"
              label="新增确诊病例"
              width="180">
            </el-table-column>
            <el-table-column
              label="修改"
              width="200">
              <template slot-scope="detail">
                <el-button type="success" size="mini" @click="detailData(detail.row.id)">查看详情</el-button>
                <template>
                  <el-dialog :visible="showDialog" :before-close="closeDialog">
                    <el-table
                      fit
                      border
                      :data="details">
                      <el-table-column
                        prop="date"
                        label="统计日期"
                        width="380">
                      </el-table-column>
                      <el-table-column
                        prop="count"
                        label="累计确诊人数"
                        width="200">
                      </el-table-column>
                    </el-table>
                  </el-dialog>
                </template>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[5, 10, 20, 40]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="itemTotal">
          </el-pagination>
        </template>
      </template>
    </div>
  </div>
</template>

<script>
  import InfiniteScroll from 'vue-infinite-scroll'
  import {request} from "../../network/request";
  export default {
    name: "Index",
    data() {
      return {
        country: "",
        pageNum: 1,
        pageSize: 10,
        dataList: [],
        itemTotal: 0,
        pageTotal: 0,
        showDialog: false,
        details: []
      }
    },
    methods: {
      getDataList(){
        request({
          url: "/data/",
          method: "get",
          params: {
            pageNum: this.pageNum,
            pageSize: this.pageSize,
            country: this.country
          }
        }).then(res => {
          console.log(res)
          console.log(res.data.data.data)
          if (parseInt(res.data.code) === 200){
            this.dataList = res.data.data.data.items
            this.pageNum = res.data.data.data.pageNum
            this.pageSize = res.data.data.data.pageSize
            this.pageTotal = res.data.data.data.pageTotal
            this.itemTotal = res.data.data.data.itemTotal
          }else {
            this.$message({type: 'error', center: true, message: res.data.message})
          }
        }).catch(res => {
          this.$message({type: 'error', center: true, message: "网络繁忙，请稍后重试"})
        })
      },
      handleSizeChange(size) {
        this.pageSize = size
        this.getDataList()
      },
      handleCurrentChange(num) {
        this.pageNum = num
        this.getDataList()
      },
      closeDialog() {
        this.showDialog = false
      },
      detailData(id) {
        request({
          url: "/detail/",
          method: "get",
          params: {
            id: id
          }
        }).then(res => {
          if (parseInt(res.data.code) === 200){
            this.showDialog = true
            this.details = res.data.data.data
          }else {
            this.$message({type: 'error', center: true, message: res.data.message})
          }
        }).catch(res => {
          this.$message({type: 'error', center: true, message: "网络繁忙，请稍后重试"})
        })
      }
    },
    activated() {
      this.getDataList()
    },
    computed: {},
    components: {
      InfiniteScroll
    }
  }
</script>

<style scoped>
  .search {
    height: 40px;
    width: 660px;
    display: block;
    position: relative;
  }
  .hello {
    text-align: center;
  }
</style>
```
# 五、	实验结果(及分析)：
## 可通过国家名称搜索数据，默认显示全部国家
![image](https://user-images.githubusercontent.com/58462525/116366294-ba2bce00-a838-11eb-9251-f022f43c4419.png)

## 查看详情可查看指定地区每日累计确诊人数
![image](https://user-images.githubusercontent.com/58462525/116366317-c152dc00-a838-11eb-8076-f8f86bf35523.png)

