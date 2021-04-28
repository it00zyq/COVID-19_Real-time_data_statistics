package com.it00zyq.demo.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author IT00ZYQ
 * @date 2021/4/25 15:19
 **/
@Data
@ConfigurationProperties(prefix = "data")
public class DataProperties {

    private String url;

    private String cron;

}
