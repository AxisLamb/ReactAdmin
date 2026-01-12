package com.lain.modules.sys.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lain.common.utils.SecurityUtil;
import com.lain.common.vo.R;
import com.lain.modules.sys.dao.SysUserMapper;
import com.lain.modules.sys.dao.SysUserRoleMapper;
import com.lain.modules.sys.entity.SysRole;
import com.lain.modules.sys.entity.SysUser;
import com.lain.modules.sys.entity.SysUserRole;
import com.lain.modules.sys.service.SysRoleService;
import com.lain.modules.sys.service.SysUserService;
import com.lain.modules.sys.vo.SysRoleVO;
import com.lain.modules.sys.vo.SysUserVO;
import com.lain.modules.sys.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public Page<SysUserVO> pageList(SysUserVO userVO) {

        Page<SysUser> pageParam = new Page<>(userVO.getCurrent(), userVO.getSize());
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(userVO.getUsername())) {
            queryWrapper.like(SysUser::getUsername, userVO.getUsername());
        }

        queryWrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = page(pageParam, queryWrapper);

        // 清除密码信息
        List<SysUserVO> records = result.getRecords().stream().map(user -> {
            SysUserVO vo = new SysUserVO();
            BeanUtils.copyProperties(user, vo);
            List<SysRole> roles = sysRoleService.findRolesByUserId(user.getUserId());
            if (CollectionUtil.isNotEmpty(roles)) {
                vo.setRoleId(roles.getFirst().getRoleId());
                vo.setRoleName(roles.getFirst().getRoleName());
            }
            vo.setPassword(null);
            return vo;
        }).collect(Collectors.toList());

        // 构造返回结果
        Page<SysUserVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R saveUser(SysUserVO userVO) {
        // 校验必填字段
        if (userVO == null) {
            return R.error("用户信息不能为空");
        }
        if (StrUtil.isBlank(userVO.getUsername())) {
            return R.error("用户名不能为空");
        }
        if (StrUtil.isBlank(userVO.getPassword())) {
            return R.error("密码不能为空");
        }
        if (userVO.getRoleId() == null) {
            return R.error("角色不能为空");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, userVO.getUsername());
        if (getOne(queryWrapper) != null) {
            return R.error("用户名已存在");
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(userVO, user);
        user.setPassword(SecurityUtil.encryptPassword(userVO.getPassword()));

        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        save(user);
        // save user role
        if (userVO.getRoleId() != null) {
            sysUserRoleMapper.insert(new SysUserRole(null, user.getUserId(), userVO.getRoleId()));
        }

        return R.ok("保存成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateUser(SysUserVO userVO) {
        if (userVO.getUserId() == null) {
            return R.error("用户ID不能为空");
        }
        // 判断用户是否存在
        SysUser user = getById(userVO.getUserId());
        if (user == null) {
            return R.error("用户不存在");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, userVO.getUsername())
                .ne(SysUser::getUserId, user.getUserId());
        if (getOne(queryWrapper) != null) {
            return R.error("用户名已存在");
        }

//        SysUser user = new SysUser();
        BeanUtils.copyProperties(userVO, user);
        updateById(user);
        // 需要先删除旧的用户角色绑定关系
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getUserId()));
        // save user role
        if (userVO.getRoleId() != null) {
            sysUserRoleMapper.insert(new SysUserRole(null, user.getUserId(), userVO.getRoleId()));
        }
        return R.ok("修改成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteUserByIds(List<Long> list) {
        if (list.size() <= 0) {
            return R.error("请选择要删除的用户");
        }

        // 不允许删除超级管理员
        if (list.contains(1L)) {
            return R.error("系统管理员不能删除");
        }

        List<SysUser> users = this.listByIds(list);
        if (CollectionUtil.isEmpty(users)) {
            return R.error("用户不存在");
        }
        // 检查是否存在角色绑定
//        if (sysRoleService.getRoleCountByUserIds(list) > 0) {
//            return R.error("用户已绑定角色，不允许删除");
//        }
        // 删除user - role 关联表数据
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, list));

        removeBatchByIds(list);
        return R.ok("删除成功");
    }

    @Override
    public UserInfoVO getUserInfo(String userId) {
        SysUser user = getById(userId);
        if (user == null) {
            return null;
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(user.getUserId());
        userInfoVO.setUserName(user.getUsername());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setEmail(user.getEmail());
        userInfoVO.setMobile(user.getMobile());
        userInfoVO.setStatus(user.getStatus());
        userInfoVO.setCreateTime(user.getCreateTime().toString());

        List<SysRole> roles = sysRoleService.findRolesByUserId(Long.valueOf(userId));
        if (CollectionUtil.isNotEmpty(roles)) {
            SysRole sysRole = roles.getFirst();
            SysRoleVO role = new SysRoleVO();
            role.setRoleId(sysRole.getRoleId());
            role.setRoleName(sysRole.getRoleName());
            role.setRoleDesc(sysRole.getRoleDesc());
            role.setStatus(sysRole.getStatus());
            role.setCreateTime(sysRole.getCreateTime());
            role.setMenuIds(sysRoleService.findRolesPermissions(
                    roles.stream().map(SysRole::getRoleId).toList()));
            userInfoVO.setRole(role);
        }

        return userInfoVO;
    }

    @Override
    public SysUser findByUsername(String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        return this.getOne(queryWrapper);
    }


}
