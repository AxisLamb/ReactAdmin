package com.lain.modules.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lain.common.vo.R;
import com.lain.modules.sys.entity.SysUser;
import com.lain.modules.sys.vo.SysUserVO;
import com.lain.modules.sys.vo.UserInfoVO;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     */
    Page<SysUserVO> pageList(SysUserVO userVO);

    /**
     * 根据用户名查找用户
     */
    SysUser findByUsername(String username);

    /**
     * 新增用户
     */
    R saveUser(SysUserVO userVO);

    /**
     * 修改用户
     */
    R updateUser(SysUserVO userVO);

    /**
     * 删除用户
     */
    R deleteUserByIds(List<Long> list);

    /**
     * 获取用户信息
     */
    UserInfoVO getUserInfo(String userId);

}