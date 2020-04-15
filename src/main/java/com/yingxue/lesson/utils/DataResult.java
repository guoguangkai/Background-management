package com.yingxue.lesson.utils;

import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.exception.code.ResponseCodeInterface;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: DataResult
 */
@Data
public class DataResult <T>{



    /**
     * 请求响应code， 0表示请求成功 其它表示失败
     */
    @ApiModelProperty(value = "请求响应code，0为成功 其他为失败")
    private int code;

    /**
     * 响应客户端的提示
     */
    @ApiModelProperty(value = "响应异常码详细信息")
    private String msg;

    /**
     * 响应客户端内容
     */
    @ApiModelProperty(value = "响应客户端内容")
    private T data;

    public DataResult(int code, T data) {
        this.code = code;
        this.data = data;
        this.msg=null;
    }

    public DataResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public DataResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data=null;
    }

    public DataResult() {
        this.code=BaseResponseCode.SUCCESS.getCode();
        this.msg=BaseResponseCode.SUCCESS.getMsg();
        this.data=null;
    }

    public DataResult(T data) {
        this.data = data;
        this.code=BaseResponseCode.SUCCESS.getCode();
        this.msg=BaseResponseCode.SUCCESS.getMsg();
    }

    public DataResult(ResponseCodeInterface responseCodeInterface) {
        this.data = null;
        this.code = responseCodeInterface.getCode();
        this.msg = responseCodeInterface.getMsg();
    }

    public DataResult(ResponseCodeInterface responseCodeInterface, T data) {
        this.data = data;
        this.code = responseCodeInterface.getCode();
        this.msg = responseCodeInterface.getMsg();
    }
    /**
     * 操作成功 data为null
     */
    public static DataResult success(){
        return new DataResult();
    }
    /**
     * 操作成功 data 不为null
     */
    public static <T>DataResult success(T data){
        return new DataResult(data);
    }
    /**
     * 自定义 返回操作 data 可控
     */
    public static <T>DataResult getResult(int code,String msg,T data){
        return new DataResult(code,msg,data);
    }
    /**
     *  自定义返回  data为null
     */
    public static DataResult getResult(int code,String msg){
        return new DataResult(code,msg);
    }
    /**
     * 自定义返回 入参一般是异常code枚举 data为空
     */
    public static DataResult getResult(BaseResponseCode responseCode){
        return new DataResult(responseCode);
    }
    /**
     * 自定义返回 入参一般是异常code枚举 data 可控
     */
    public static <T>DataResult getResult(BaseResponseCode responseCode, T data){

        return new DataResult(responseCode,data);
    }

}
