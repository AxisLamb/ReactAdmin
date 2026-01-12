/**
 * Copyright (c) 2025 LainEx All rights reserved.
 *
 * http://www.7thmist.store
 *
 * 版权所有，侵权必究！
 */
package com.lain.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import com.lain.common.vo.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 异常处理器
 *
 * @author lain
 */
@RestControllerAdvice
public class LainExceptionHandler {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 处理自定义异常
	 */
	@ExceptionHandler(LainException.class)
	public R handleRRException(LainException e){
		return R.error(e.getCode(), e.getMsg());
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public R handlerNoFoundException(Exception e) {
		logger.error(e.getMessage(), e);
		return R.error(404, "路径不存在，请检查路径是否正确");
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public R handleDuplicateKeyException(DuplicateKeyException e){
		logger.error(e.getMessage(), e);
		return R.error("数据库中已存在该记录");
	}

	/**
	 * 处理Sa-Token权限异常
	 */
	@ExceptionHandler({SaTokenException.class, NotLoginException.class, NotPermissionException.class, NotRoleException.class})
	public R handleSaTokenException(Exception e) {
		logger.error(e.getMessage(), e);

		if (e instanceof NotLoginException) {
			return R.error(401, "未登录或登录已过期，请重新登录");
		} else if (e instanceof NotPermissionException) {
			return R.error(403, "没有权限访问该资源:[" + ((NotPermissionException) e).getPermission() + "]");
		} else if (e instanceof NotRoleException) {
			return R.error(403, "没有角色权限:[" + ((NotRoleException) e).getRole() + "]");
		} else {
			return R.error(403, "权限验证失败:" + e.getMessage());
		}
	}


	@ExceptionHandler(Exception.class)
	public R handleException(Exception e){
		logger.error(e.getMessage(), e);
		return R.error();
	}
}
