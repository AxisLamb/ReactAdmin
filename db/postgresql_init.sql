-- 创建数据库
-- 注意：PostgreSQL 中通常不在此脚本内创建数据库，而是在连接时指定

-- 扩展 UUID 支持（如果需要）
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 文件信息表
CREATE TABLE file_info (
    id BIGSERIAL PRIMARY KEY,
    file_id VARCHAR(64) NOT NULL UNIQUE,
    original_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100),
    bucket_name VARCHAR(100) NOT NULL,
    object_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000),
    service_module VARCHAR(100),
    business_type VARCHAR(100),
    business_id VARCHAR(100),
    status SMALLINT NOT NULL DEFAULT 1,
    created_by BIGINT,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_by BIGINT,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 创建索引
CREATE INDEX idx_business ON file_info(business_type, business_id);
CREATE INDEX idx_service_module ON file_info(service_module);
CREATE INDEX idx_created_time ON file_info(create_time);

-- 添加注释
COMMENT ON TABLE file_info IS '文件信息表';
COMMENT ON COLUMN file_info.id IS '主键ID';
COMMENT ON COLUMN file_info.file_id IS '文件唯一标识';
COMMENT ON COLUMN file_info.original_name IS '原始文件名';
COMMENT ON COLUMN file_info.file_size IS '文件大小(字节)';
COMMENT ON COLUMN file_info.file_type IS '文件类型';
COMMENT ON COLUMN file_info.bucket_name IS '存储桶名称';
COMMENT ON COLUMN file_info.object_name IS '对象名称';
COMMENT ON COLUMN file_info.file_path IS '文件路径';
COMMENT ON COLUMN file_info.service_module IS '服务模块';
COMMENT ON COLUMN file_info.business_type IS '业务类型';
COMMENT ON COLUMN file_info.business_id IS '业务ID';
COMMENT ON COLUMN file_info.status IS '状态: 0-禁用, 1-正常';
COMMENT ON COLUMN file_info.created_by IS '创建人';
COMMENT ON COLUMN file_info.create_time IS '创建时间';
COMMENT ON COLUMN file_info.updated_by IS '更新人';
COMMENT ON COLUMN file_info.update_time IS '更新时间';

-- 系统审计日志表
CREATE TABLE sys_audit_log (
    log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(50),
    method VARCHAR(200),
    params TEXT,
    result TEXT,
    ip VARCHAR(64),
    user_agent VARCHAR(500),
    time BIGINT,
    created_by BIGINT,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_by BIGINT,
    update_time TIMESTAMP WITH TIME ZONE
);

-- 创建索引
CREATE INDEX idx_user_id ON sys_audit_log(user_id);
CREATE INDEX idx_create_time ON sys_audit_log(create_time);

-- 添加注释
COMMENT ON TABLE sys_audit_log IS '系统审计日志';
COMMENT ON COLUMN sys_audit_log.log_id IS '日志ID';
COMMENT ON COLUMN sys_audit_log.user_id IS '用户ID';
COMMENT ON COLUMN sys_audit_log.username IS '用户名';
COMMENT ON COLUMN sys_audit_log.operation IS '用户操作';
COMMENT ON COLUMN sys_audit_log.method IS '请求方法';
COMMENT ON COLUMN sys_audit_log.params IS '请求参数';
COMMENT ON COLUMN sys_audit_log.result IS '执行结果';
COMMENT ON COLUMN sys_audit_log.ip IS 'IP地址';
COMMENT ON COLUMN sys_audit_log.user_agent IS '用户代理';
COMMENT ON COLUMN sys_audit_log.time IS '执行时长(毫秒)';
COMMENT ON COLUMN sys_audit_log.created_by IS '创建者ID';
COMMENT ON COLUMN sys_audit_log.create_time IS '创建时间';
COMMENT ON COLUMN sys_audit_log.updated_by IS '更新者ID';
COMMENT ON COLUMN sys_audit_log.update_time IS '更新时间';

-- 插入示例数据
INSERT INTO sys_audit_log(log_id, user_id, username, operation, method, params, result, ip, user_agent, time, created_by, create_time, updated_by, update_time)
VALUES (97, 1, NULL, '新增用户', 'com.lain.modules.sys.controller.SysUserController.save()', 'SysUserVO(userId=null, username=telsa, roleId=8, roleName=null, password=123456, realName=telsa, email=null, mobile=null, status=1) ', 'R(code=0, msg=success, data=保存成功)', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0', 79, 1, '2026-01-12 22:17:58+00', 1, '2026-01-12 22:17:58+00');

-- 数据字典表
CREATE TABLE sys_dict (
    dict_id BIGSERIAL PRIMARY KEY,
    dict_name VARCHAR(100) NOT NULL,
    dict_type VARCHAR(100) NOT NULL UNIQUE,
    status SMALLINT DEFAULT 1,
    remark VARCHAR(500),
    created_by BIGINT,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_by BIGINT,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 添加注释
COMMENT ON TABLE sys_dict IS '数据字典表';
COMMENT ON COLUMN sys_dict.dict_id IS '字典ID';
COMMENT ON COLUMN sys_dict.dict_name IS '字典名称';
COMMENT ON COLUMN sys_dict.dict_type IS '字典类型';
COMMENT ON COLUMN sys_dict.status IS '状态（0禁用 1正常）';
COMMENT ON COLUMN sys_dict.remark IS '备注';
COMMENT ON COLUMN sys_dict.created_by IS '创建者';
COMMENT ON COLUMN sys_dict.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict.updated_by IS '更新者';
COMMENT ON COLUMN sys_dict.update_time IS '更新时间';

-- 数据字典项表
CREATE TABLE sys_dict_item (
    item_id BIGSERIAL PRIMARY KEY,
    dict_id BIGINT NOT NULL,
    item_label VARCHAR(100) NOT NULL,
    item_value VARCHAR(100) NOT NULL,
    status SMALLINT DEFAULT 1,
    order_num INTEGER DEFAULT 0,
    remark VARCHAR(500),
    created_by BIGINT,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_by BIGINT,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 创建索引
CREATE INDEX idx_dict_id ON sys_dict_item(dict_id);

-- 添加注释
COMMENT ON TABLE sys_dict_item IS '数据字典项表';
COMMENT ON COLUMN sys_dict_item.item_id IS '字典项ID';
COMMENT ON COLUMN sys_dict_item.dict_id IS '字典ID';
COMMENT ON COLUMN sys_dict_item.item_label IS '字典项标签';
COMMENT ON COLUMN sys_dict_item.item_value IS '字典项值';
COMMENT ON COLUMN sys_dict_item.status IS '状态（0禁用 1正常）';
COMMENT ON COLUMN sys_dict_item.order_num IS '显示顺序';
COMMENT ON COLUMN sys_dict_item.remark IS '备注';
COMMENT ON COLUMN sys_dict_item.created_by IS '创建者';
COMMENT ON COLUMN sys_dict_item.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict_item.updated_by IS '更新者';
COMMENT ON COLUMN sys_dict_item.update_time IS '更新时间';

-- 菜单管理表
CREATE TABLE sys_menu (
    menu_id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT,
    name VARCHAR(50) NOT NULL,
    url VARCHAR(200),
    react_component VARCHAR(50),
    perms VARCHAR(500),
    type INTEGER,
    icon VARCHAR(50),
    order_num INTEGER,
    created_by BIGINT,
    create_time TIMESTAMP WITH TIME ZONE,
    updated_by BIGINT,
    update_time TIMESTAMP WITH TIME ZONE
);

-- 添加注释
COMMENT ON TABLE sys_menu IS '菜单管理';
COMMENT ON COLUMN sys_menu.menu_id IS '菜单ID';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID，一级菜单为0';
COMMENT ON COLUMN sys_menu.name IS '菜单名称';
COMMENT ON COLUMN sys_menu.url IS '菜单URL';
COMMENT ON COLUMN sys_menu.react_component IS '菜单对应的React组件';
COMMENT ON COLUMN sys_menu.perms IS '授权(多个用逗号分隔，如：user:list,user:create)';
COMMENT ON COLUMN sys_menu.type IS '类型   0：目录   1：菜单   2：按钮';
COMMENT ON COLUMN sys_menu.icon IS '菜单图标';
COMMENT ON COLUMN sys_menu.order_num IS '排序';
COMMENT ON COLUMN sys_menu.created_by IS '创建者ID';
COMMENT ON COLUMN sys_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_menu.updated_by IS '更新者ID';
COMMENT ON COLUMN sys_menu.update_time IS '更新时间';

-- 插入菜单数据
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(1, 0, '系统管理', 'sys', NULL, NULL, 0, 'setting', 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(2, 1, '用户管理', 'sys/user', 'UserList', 'sys:user:list', 1, 'user', 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(3, 2, '新增用户', NULL, NULL, 'sys:user:save', 2, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(4, 2, '修改用户', NULL, NULL, 'sys:user:update', 2, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(5, 2, '删除用户', NULL, NULL, 'sys:user:delete', 2, NULL, 2, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(6, 2, '重置密码', NULL, NULL, 'sys:user:reset', 2, NULL, 3, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(7, 1, '角色管理', 'sys/role', 'RoleList', 'sys:role:list', 1, 'team', 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(8, 7, '新增角色', NULL, NULL, 'sys:role:save', 2, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(9, 7, '修改角色', NULL, NULL, 'sys:role:update', 2, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(10, 7, '删除角色', NULL, NULL, 'sys:role:delete', 2, NULL, 2, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(11, 1, '菜单管理', 'sys/menu', 'MenuList', 'sys:menu:list', 1, 'menu', 2, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(12, 11, '新增菜单', NULL, NULL, 'sys:menu:save', 2, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(13, 11, '修改菜单', NULL, NULL, 'sys:menu:update', 2, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(14, 11, '删除菜单', NULL, NULL, 'sys:menu:delete', 2, NULL, 2, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(19, 1, '系统日志', 'sys/log', NULL, 'sys:log:list', 1, 'file-text', 4, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(20, 19, '删除日志', NULL, NULL, 'sys:log:delete', 2, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(22, 0, '工作台', 'workbench', NULL, NULL, 0, 'desktop', 2, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(37, 0, '系统监控', 'monitor', NULL, NULL, 0, 'monitor', 4, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(38, 37, '服务监控', 'server', NULL, 'monitor:server:list', 1, 'fund', 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(39, 37, 'Redis监控', 'redis', NULL, 'monitor:redis:list', 1, 'database', 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(40, 37, '接口文档', 'http://localhost:8888/doc.html', NULL, NULL, 1, 'file-word', 2, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(41, 0, '个人中心', 'profile', NULL, NULL, 0, 'user', 9, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(42, 41, '修改信息', NULL, NULL, 'sys:profile:update', 1, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(43, 41, '修改密码', NULL, NULL, 'sys:profile:password', 1, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(44, 23, '文件管理', 'file', NULL, 'oss:file:list', 1, 'file-image', 3, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(45, 44, '上传文件', NULL, NULL, 'oss:file:upload', 2, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(46, 44, '下载文件', NULL, NULL, 'oss:file:download', 2, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(47, 44, '获取文件链接', NULL, NULL, 'oss:file:url', 2, NULL, 2, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(48, 44, '删除文件', NULL, NULL, 'oss:file:delete', 2, NULL, 3, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(49, 1, '数据字典', 'sys/dict', NULL, 'sys:dict:list', 1, 'book', 5, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(50, 49, '查询字典', NULL, NULL, 'sys:dict:list', 2, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(51, 49, '新增字典', NULL, NULL, 'sys:dict:save', 2, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(52, 49, '修改字典', NULL, NULL, 'sys:dict:update', 2, NULL, 2, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(53, 49, '删除字典', NULL, NULL, 'sys:dict:delete', 2, NULL, 3, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(54, 1, '字典项管理', 'sys/dictitem', NULL, 'sys:dict:item:list', 1, 'unordered-list', 6, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(55, 54, '查询字典项', NULL, NULL, 'sys:dict:item:list', 2, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(56, 54, '新增字典项', NULL, NULL, 'sys:dict:item:save', 2, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(57, 54, '修改字典项', NULL, NULL, 'sys:dict:item:update', 2, NULL, 2, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(58, 54, '删除字典项', NULL, NULL, 'sys:dict:item:delete', 2, NULL, 3, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(59, 2, '用户信息', NULL, NULL, 'sys:user:info', 2, NULL, 5, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(61, 2, '登出接口', NULL, NULL, 'sys:user:logout', 2, NULL, 6, 1, '2026-01-07 15:06:11+00', 1, '2026-01-07 15:06:11+00');

-- 角色表
CREATE TABLE sys_role (
    role_id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_desc VARCHAR(100),
    status SMALLINT DEFAULT 1,
    created_by BIGINT,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_by BIGINT,
    update_time TIMESTAMP WITH TIME ZONE
);

-- 添加注释
COMMENT ON TABLE sys_role IS '角色';
COMMENT ON COLUMN sys_role.role_id IS '角色ID';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_desc IS '角色描述';
COMMENT ON COLUMN sys_role.status IS '状态  0：禁用   1：正常';
COMMENT ON COLUMN sys_role.created_by IS '创建者ID';
COMMENT ON COLUMN sys_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_role.updated_by IS '更新者ID';
COMMENT ON COLUMN sys_role.update_time IS '更新时间';

-- 插入角色数据
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(1, '超级管理员', '拥有系统所有权限，最高权限角色', 1, NULL, '2025-12-02 11:50:30+00', NULL, NULL);
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(2, '系统管理员', '管理系统基础配置和用户', 1, NULL, '2025-12-02 11:50:30+00', NULL, NULL);
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(3, '普通用户', '普通操作员，拥有基本查看权限', 1, NULL, '2025-12-02 11:50:30+00', NULL, NULL);
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(7, 'add user', 'add user', 1, 1, '2026-01-05 16:54:36+00', 1, '2026-01-12 22:17:28+00');
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(8, 'edit user', 'edit user', 1, 1, '2026-01-05 17:00:49+00', 1, '2026-01-12 22:17:32+00');
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(9, 'del user', 'del user', 1, 1, '2026-01-05 17:07:12+00', 1, '2026-01-12 22:17:35+00');

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL
);

-- 添加注释
COMMENT ON TABLE sys_role_menu IS '角色与菜单对应关系';
COMMENT ON COLUMN sys_role_menu.id IS '';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';

-- 插入角色菜单数据
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (1, 1, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (2, 1, 2);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (3, 1, 3);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (4, 1, 4);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (5, 1, 5);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (6, 1, 6);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (7, 1, 7);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (8, 1, 8);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (9, 1, 9);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (10, 1, 10);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (11, 1, 11);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (12, 1, 12);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (13, 1, 13);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (14, 1, 14);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (15, 1, 15);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (16, 1, 16);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (17, 1, 17);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (18, 1, 18);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (19, 1, 19);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (20, 1, 20);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (22, 1, 22);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (23, 1, 23);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (24, 1, 24);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (25, 1, 25);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (26, 1, 26);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (27, 1, 27);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (28, 1, 28);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (29, 1, 29);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (30, 1, 30);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (31, 1, 31);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (32, 1, 32);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (33, 1, 33);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (34, 1, 34);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (35, 1, 35);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (36, 1, 36);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (37, 1, 37);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (38, 1, 38);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (39, 1, 39);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (40, 1, 40);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (41, 1, 41);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (42, 1, 42);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (43, 1, 43);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (44, 2, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (45, 2, 2);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (46, 2, 3);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (47, 2, 4);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (48, 2, 5);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (49, 2, 6);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (50, 2, 7);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (51, 2, 8);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (52, 2, 9);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (53, 2, 10);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (54, 2, 11);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (55, 2, 12);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (56, 2, 13);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (57, 2, 14);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (59, 2, 22);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (60, 2, 23);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (61, 2, 24);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (62, 2, 29);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (63, 2, 33);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (64, 2, 41);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (65, 2, 42);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (66, 2, 43);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (68, 3, 22);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (69, 3, 23);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (70, 3, 24);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (71, 3, 29);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (72, 3, 33);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (73, 3, 41);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (74, 3, 42);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (75, 3, 43);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (76, 1, 44);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (77, 1, 45);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (78, 1, 46);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (79, 1, 47);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (80, 1, 48);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (81, 1, 49);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (82, 1, 50);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (83, 1, 51);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (84, 1, 52);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (85, 1, 53);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (86, 1, 54);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (87, 1, 55);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (88, 1, 56);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (89, 1, 57);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (90, 1, 58);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (91, 2, 49);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (92, 2, 50);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (93, 2, 51);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (94, 2, 52);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (95, 2, 53);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (96, 2, 54);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (97, 2, 55);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (98, 2, 56);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (99, 2, 57);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (100, 2, 58);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (101, 1, 59);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (102, 2, 59);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (103, 3, 59);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (104, 3, 11);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (234, 7, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (235, 7, 2);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (236, 7, 3);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (238, 8, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (239, 8, 2);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (240, 8, 4);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (242, 9, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (243, 9, 2);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (244, 9, 5);

-- 系统用户表
CREATE TABLE sys_user (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    mobile VARCHAR(20),
    status SMALLINT DEFAULT 1,
    created_by BIGINT,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_by BIGINT,
    update_time TIMESTAMP WITH TIME ZONE
);

-- 添加注释
COMMENT ON TABLE sys_user IS '系统用户';
COMMENT ON COLUMN sys_user.user_id IS '用户ID';
COMMENT ON COLUMN sys_user.username IS '用户名';
COMMENT ON COLUMN sys_user.password IS '密码';
COMMENT ON COLUMN sys_user.real_name IS '真实姓名';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.mobile IS '手机号';
COMMENT ON COLUMN sys_user.status IS '状态  0：禁用   1：正常';
COMMENT ON COLUMN sys_user.created_by IS '创建者ID';
COMMENT ON COLUMN sys_user.create_time IS '创建时间';
COMMENT ON COLUMN sys_user.updated_by IS '更新者ID';
COMMENT ON COLUMN sys_user.update_time IS '更新时间';

-- 插入用户数据
INSERT INTO sys_user(user_id, username, password, real_name, email, mobile, status, created_by, create_time, updated_by, update_time) VALUES
(1, 'admin', '$2a$10$bddSw9Plnp9wvu8/XyNGz.EA.EtpSa/Bc2ag399fXWaHpQ93bwxXa', '系统管理员', 'admin@example.com', '13800138000', 1, NULL, '2025-12-02 11:50:46+00', NULL, NULL);
INSERT INTO sys_user(user_id, username, password, real_name, email, mobile, status, created_by, create_time, updated_by, update_time) VALUES
(2, 'sysadmin', '$2a$10$XhTMMAx1jCp/XzpXvNNnzOtI5l44E/XIY98fDZy7Os2Q/VPBZDGLe', '张三', 'zhangsan@example.com', '13800138001', 1, NULL, '2025-12-02 11:50:46+00', NULL, NULL);
INSERT INTO sys_user(user_id, username, password, real_name, email, mobile, status, created_by, create_time, updated_by, update_time) VALUES
(3, 'user', '$2a$10$mrOhEfIDpwpq/i0j1PQeYexHysPVN8DCuG6vNo8rIHZ4C6w3aKQo6', '李四', 'lisi@example.com', '13800138002', 1, NULL, '2025-12-02 11:50:46+00', NULL, NULL);

-- 用户角色关联表
CREATE TABLE sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
);

-- 添加注释
COMMENT ON TABLE sys_user_role IS '用户与角色对应关系';
COMMENT ON COLUMN sys_user_role.id IS '';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';

-- 插入用户角色数据
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (1, 1, 1);
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (2, 2, 2);
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (3, 3, 3);
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (4, 1, 1);
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (5, 2, 2);
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (6, 3, 3);
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (7, 4, 2);
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (8, 4, 3);

-- 创建触发器以实现 MySQL 的 ON UPDATE 功能
-- 对于 file_info 表
CREATE OR REPLACE FUNCTION update_file_info_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_file_info_update_time
    BEFORE UPDATE ON file_info
    FOR EACH ROW
    EXECUTE FUNCTION update_file_info_timestamp();

-- 对于 sys_dict 表
CREATE OR REPLACE FUNCTION update_sys_dict_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_sys_dict_update_time
    BEFORE UPDATE ON sys_dict
    FOR EACH ROW
    EXECUTE FUNCTION update_sys_dict_timestamp();

-- 对于 sys_dict_item 表
CREATE OR REPLACE FUNCTION update_sys_dict_item_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_sys_dict_item_update_time
    BEFORE UPDATE ON sys_dict_item
    FOR EACH ROW
    EXECUTE FUNCTION update_sys_dict_item_timestamp();

-- 对于 sys_menu 表
CREATE OR REPLACE FUNCTION update_sys_menu_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_sys_menu_update_time
    BEFORE UPDATE ON sys_menu
    FOR EACH ROW
    EXECUTE FUNCTION update_sys_menu_timestamp();

-- 对于 sys_role 表
CREATE OR REPLACE FUNCTION update_sys_role_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_sys_role_update_time
    BEFORE UPDATE ON sys_role
    FOR EACH ROW
    EXECUTE FUNCTION update_sys_role_timestamp();

-- 对于 sys_user 表
CREATE OR REPLACE FUNCTION update_sys_user_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_sys_user_update_time
    BEFORE UPDATE ON sys_user
    FOR EACH ROW
    EXECUTE FUNCTION update_sys_user_timestamp();

-- 设置序列起始值以匹配 MySQL 的 AUTO_INCREMENT
SELECT setval('file_info_id_seq', 6);
SELECT setval('sys_audit_log_log_id_seq', 98);
SELECT setval('sys_dict_dict_id_seq', 10);
SELECT setval('sys_dict_item_item_id_seq', 2);
SELECT setval('sys_menu_menu_id_seq', 75);
SELECT setval('sys_role_role_id_seq', 19);
SELECT setval('sys_role_menu_id_seq', 246);
SELECT setval('sys_user_user_id_seq', 21);
SELECT setval('sys_user_role_id_seq', 14);
