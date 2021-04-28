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
