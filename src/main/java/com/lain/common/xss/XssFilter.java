///**
// * Copyright (c) 2025 LainEx All rights reserved.
// *
// * http://www.7thmist.store
// *
// * 版权所有，侵权必究！
// */
//
//package com.lain.common.xss;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//
///**
// * XSS过滤
// *
// * @author lain
// */
//public class XssFilter implements Filter {
//
//	@Override
//	public void init(FilterConfig config) throws ServletException {
//	}
//
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//		XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(
//				(HttpServletRequest) request);
//		chain.doFilter(xssRequest, response);
//	}
//
//	@Override
//	public void destroy() {
//	}
//
//}