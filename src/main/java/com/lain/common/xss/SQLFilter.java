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
//import com.lain.common.exception.LainException;
//
///**
// * SQL过滤
// *
// * @author lain
// */
//public class SQLFilter {
//
//    /**
//     * SQL注入过滤
//     * @param str  待验证的字符串
//     */
//    public static String sqlInject(String str){
//        if(StrUtils.isBlank(str)){
//            return null;
//        }
//        //去掉'|"|;|\字符
//        str = StringUtils.replace(str, "'", "");
//        str = StringUtils.replace(str, "\"", "");
//        str = StringUtils.replace(str, ";", "");
//        str = StringUtils.replace(str, "\\", "");
//
//        //转换成小写
//        str = str.toLowerCase();
//
//        //非法字符
//        String[] keywords = {"master", "truncate", "insert", "select", "delete", "update", "declare", "alter", "drop"};
//
//        //判断是否包含非法字符
//        for(String keyword : keywords){
//            if(str.indexOf(keyword) != -1){
//                throw new LainException("包含非法字符");
//            }
//        }
//
//        return str;
//    }
//}
