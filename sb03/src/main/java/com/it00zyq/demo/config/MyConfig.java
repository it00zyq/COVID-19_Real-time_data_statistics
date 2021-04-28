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
