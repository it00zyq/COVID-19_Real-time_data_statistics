package com.it00zyq.demo;

import com.it00zyq.demo.config.TimeTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private TimeTask timeTask;

    @Test
    void contextLoads() throws IOException {
        timeTask.pullData();
        System.out.println(timeTask.getDataList());
    }

}
