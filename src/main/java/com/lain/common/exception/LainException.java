/**
 * Copyright (c) 2025 LainEx All rights reserved.
 *
 * http://www.7thmist.store
 *
 * 版权所有，侵权必究！
 */

package com.lain.common.exception;

/**
 * 自定义异常
 *
 * @author lain
 */
public class LainException extends RuntimeException {
	private static final long serialVersionUID = 1L;

    private String msg;
    private int code = 500;

    public LainException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public LainException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	public LainException(String msg, int code) {
		super(msg);
		this.msg = msg;
		this.code = code;
	}

	public LainException(String msg, int code, Throwable e) {
		super(msg, e);
		this.msg = msg;
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}


}
