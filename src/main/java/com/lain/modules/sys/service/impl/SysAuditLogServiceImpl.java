package com.lain.modules.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lain.modules.sys.entity.SysAuditLog;
import com.lain.modules.sys.dao.SysAuditLogMapper;
import com.lain.modules.sys.service.SysAuditLogService;
import org.springframework.stereotype.Service;

@Service
public class SysAuditLogServiceImpl extends ServiceImpl<SysAuditLogMapper, SysAuditLog> implements SysAuditLogService {
}
