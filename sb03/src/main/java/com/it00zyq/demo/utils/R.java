package com.it00zyq.demo.utils;


import lombok.Data;

import java.util.HashMap;
import java.util.Map;


/**
 * 同一返回类
 * @author IT00ZYQ
 * @date 2021/4/9 19:32
 **/
@Data
public class R {

    private Boolean success;
    private Integer code;
    private String message;
    private Map<String, Object> data = new HashMap<String, Object>();

    private R(){}

    /**
     * 成功静态方法
     */
    public static R ok(){
        R r = new R();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage("成功");
        return r;
    }

    /**
     * 失败静态方法
     */
    public static R error() {
        R r = new R();
        r.setSuccess(false);
        r.setCode(500);
        r.setMessage("失败");
        return r;
    }

    /**
     * 下面四个方法返回this指针是为了实现链式编程
     */

    public R message(String message){
        this.setMessage(message);
        return this;
    }

    public R code(Integer code){
        this.setCode(code);
        return this;
    }

    public R data(String key, Object value){
        this.data.put(key, value);
        return this;
    }

    public R data(Map<String, Object> map){
        this.setData(map);
        return this;
    }


}
