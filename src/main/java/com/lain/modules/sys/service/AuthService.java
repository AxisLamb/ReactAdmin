package com.lain.modules.sys.service;

import com.lain.common.vo.R;
import com.lain.modules.sys.vo.LoginParam;

import java.util.List;

public interface AuthService {

    /**
     * 登录
     *
     * @param loginParam
     * @return
     */
    R login(LoginParam loginParam);

    /**
     * 获取用户角色列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 获取用户权限列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 测试
     */
    R test();

}
