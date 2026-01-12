package com.lain.common.vo;

import lombok.Data;

/**
 * @Author: liugy03
 * @Description: 标准返回类 兼容swagger
 * @Date: 2020/12/28 10:13
 * @Version: 1.0
 */
@Data
public class R<T> {

    public static final String MSG_SUCCESS = "success";

    /** 返回状态码 0 成功 **/
    private int code;

    /** 返回消息 **/
    private String msg;

    /** 返回数据 **/
    private T data;


    public R(int code) {
        this.code = code;
    }

    public R(T data) {
        this.data = data;
    }

    public R(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 返回成功提示（不含数据）
     * @return
     */
    public static R ok() {
        return new R(HttpStatus.OK, MSG_SUCCESS);
    }

    /**
     * 返回成功提示（含数据）
     * @return
     */
    public static <T> R<T> ok(T data) {
        return new R<>(HttpStatus.OK, MSG_SUCCESS, data);
    }

    /**
     * 返回错误码及提示
     * @param code
     * @param msg
     * @return
     */
    public static R error(int code, String msg) {
        return new R(code, msg);
    }

    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }
}
