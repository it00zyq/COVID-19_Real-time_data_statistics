package com.it00zyq.demo.handle;

import com.it00zyq.demo.utils.ApiException;
import com.it00zyq.demo.utils.R;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeoutException;


/**
 * 自定义异常统一处理, basePackages指定处理的范围
 * @author IT00ZYQ
 * @date 2021/4/9 21:54
 **/
@ControllerAdvice(basePackages = {"com.zheng.demo"})
public class ApiExceptionHandle {

    /**
     * 处理异常
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public R handle(Exception e) {
        e.printStackTrace();
        if (e instanceof ApiException) {
            // 自定义异常
            ApiException ex = (ApiException) e;
            return R.error().code(ex.getCode()).message(ex.getMessage());
        }else if (e instanceof NullPointerException) {
            // 空指针异常
            return R.error().message("系统繁忙, 请稍后再试！");
        }else if (e instanceof MethodArgumentNotValidException) {
            // 参数校验异常
            return R.error().message(((MethodArgumentNotValidException) e).getBindingResult().getAllErrors().get(0).getDefaultMessage());
        }else if (e instanceof TimeoutException) {
            // 连接超时异常
            return R.error().message("系统繁忙，请给系统一点喘息时间...");
        }else if (e instanceof HttpMessageNotReadableException) {
            // 参数读取异常(如Json格式错误)
            return R.error().message("请检查参数格式");
        }
        // 未识别的异常
        return R.error().message("服务器异常, 请联系管理人员!");
    }
}