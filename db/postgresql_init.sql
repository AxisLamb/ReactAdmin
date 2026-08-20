-- 创建数据库
-- 注意：PostgreSQL 中通常不在此脚本内创建数据库，而是在连接时指定

-- 扩展 UUID 支持（如果需要）
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 文件信息表
CREATE TABLE file_info (
    id BIGSERIAL PRIMARY KEY,
    file_id VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT,
    original_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100),
    bucket_name VARCHAR(100) NOT NULL,
    object_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000),
    service_module VARCHAR(100),
    business_type VARCHAR(100),
    business_table VARCHAR(100),
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
COMMENT ON COLUMN file_info.user_id IS '上传用户ID';
COMMENT ON COLUMN file_info.original_name IS '原始文件名';
COMMENT ON COLUMN file_info.file_size IS '文件大小(字节)';
COMMENT ON COLUMN file_info.file_type IS '文件类型';
COMMENT ON COLUMN file_info.bucket_name IS '存储桶名称';
COMMENT ON COLUMN file_info.object_name IS '对象名称';
COMMENT ON COLUMN file_info.file_path IS '文件路径';
COMMENT ON COLUMN file_info.service_module IS '服务模块';
COMMENT ON COLUMN file_info.business_type IS '业务类型';
COMMENT ON COLUMN file_info.business_table IS '业务表名';
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

INSERT INTO sys_dict (dict_name,dict_type,status,remark,created_by,create_time,updated_by,update_time) VALUES
	 ('image','image',1,'图片定义',1, NOW(),1, NOW());

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

INSERT INTO public.sys_dict_item (dict_id,item_label,item_value,status,order_num,remark,created_by,create_time,updated_by,update_time) VALUES
	 (1,'avatar','user',1,0,'module',1,NOW(),1,NOW()),
	 (1,'avatar','sys_user',1,1,'业务表',1,NOW(),1,NOW()),
	 (1,'loginPage','sys',1,2,'module',1,NOW(),1,NOW()),
	 (1,'loginPage','file_info',1,3,'业务表',1,NOW(),1,NOW());


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
COMMENT ON COLUMN sys_menu.type IS '类型   0：目录   1：菜单   2：按钮   3：接口目录   4：接口业务   5：具体接口';
COMMENT ON COLUMN sys_menu.icon IS '菜单图标';
COMMENT ON COLUMN sys_menu.order_num IS '排序';
COMMENT ON COLUMN sys_menu.created_by IS '创建者ID';
COMMENT ON COLUMN sys_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_menu.updated_by IS '更新者ID';
COMMENT ON COLUMN sys_menu.update_time IS '更新时间';

-- 插入菜单数据
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(1, 0, '系统管理', 'sys', NULL, NULL, 0, 'setting', 1, NULL, NULL, 1, '2026-08-18 16:08:18+00');
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
(22, 64, '首页统计', NULL, NULL, NULL, 4, 'bar-chart', 1, NULL, NULL, 1, '2026-08-18 16:13:13+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(37, 0, '系统监控', 'monitor', NULL, NULL, 0, 'dashboard', 2, NULL, NULL, 1, '2026-08-18 16:08:11+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(38, 37, '服务监控', 'server', NULL, 'monitor:server:list', 1, 'safety', 0, NULL, NULL, 1, '2026-08-18 14:41:49+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(39, 37, 'Redis监控', 'redis', NULL, 'monitor:redis:list', 1, 'database', 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(40, 37, '接口文档', NULL, NULL, NULL, 1, 'database', 2, NULL, NULL, 1, '2026-08-18 14:42:19+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(41, 64, '个人中心', NULL, NULL, NULL, 4, 'user', 2, NULL, NULL, 1, '2026-08-18 17:02:43+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(42, 41, '修改信息', NULL, NULL, 'sys:profile:update', 5, NULL, 0, NULL, NULL, 1, '2026-08-18 14:45:11+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(43, 41, '修改密码', NULL, NULL, 'sys:profile:password', 5, NULL, 1, NULL, NULL, 1, '2026-08-18 14:45:18+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(44, 1, '文件管理', 'sys/file', 'FileList', 'oss:file:list', 1, 'folder', 3, NULL, NULL, 1, '2026-08-18 14:40:40+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(46, 44, '下载文件', NULL, NULL, 'oss:file:download', 2, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(47, 44, '获取文件链接', NULL, NULL, 'oss:file:url', 2, NULL, 2, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(48, 44, '删除文件', NULL, NULL, 'oss:file:delete', 2, NULL, 3, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(49, 1, '数据字典', 'sys/dict', 'DictList', 'sys:dict:list', 1, 'book', 5, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(50, 49, '查询字典', NULL, NULL, 'sys:dict:list', 2, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(51, 49, '新增字典', NULL, NULL, 'sys:dict:save', 2, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(52, 49, '修改字典', NULL, NULL, 'sys:dict:update', 2, NULL, 2, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(53, 49, '删除字典', NULL, NULL, 'sys:dict:delete', 2, NULL, 3, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(55, 49, '查询字典项', NULL, NULL, 'sys:dict:item:list', 2, NULL, 4, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(56, 49, '新增字典项', NULL, NULL, 'sys:dict:item:save', 2, NULL, 5, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(57, 49, '修改字典项', NULL, NULL, 'sys:dict:item:update', 2, NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(58, 49, '删除字典项', NULL, NULL, 'sys:dict:item:delete', 2, NULL, 7, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(59, 2, '用户信息', NULL, NULL, 'sys:user:info', 2, NULL, 5, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(61, 41, '登出接口', NULL, NULL, 'sys:user:logout', 5, NULL, 6, 1, '2026-01-07 15:06:11+00', 1, '2026-01-07 15:06:11+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(63, 22, '数据统计', NULL, NULL, 'sys:dashboard:list', 5, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(64, 0, '通用接口', NULL, NULL, NULL, 3, 'star', 3, 1, '2026-08-18 14:43:23+00', 1, '2026-08-18 16:13:03+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(65, 64, '文件', NULL, NULL, NULL, 4, 'folder', 3, 1, '2026-08-18 17:02:16+00', 1, '2026-08-18 17:02:23+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(66, 65, '文件上传', NULL, NULL, 'oss:file:upload', 5, NULL, 1, 1, '2026-08-18 17:04:10+00', 1, '2026-08-18 17:04:10+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(67, 65, '文件链接', NULL, NULL, 'oss:file:url', 5, NULL, 2, 1, '2026-08-18 17:04:54+00', 1, '2026-08-18 17:04:59+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(68, 41, '上传头像', NULL, NULL, 'sys:user:upload', 5, NULL, 3, 1, '2026-08-18 18:23:39+00', 1, '2026-08-18 18:23:39+00');
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(69, 1, '系统配置', 'sys/config', 'SysConfig', NULL, 1, 'setting', 10, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(70, 69, '上传图片', NULL, NULL, 'sys:config:loginPageUpload', 2, NULL, 7, NULL, NULL, NULL, NULL);

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
(1, '超级管理员', '拥有系统所有权限，最高权限角色', 1, NULL, '2025-12-02 11:50:30+00', 1, '2026-08-18 18:24:14+00');
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(2, '系统管理员', '管理系统基础配置和用户', 1, NULL, '2025-12-02 11:50:30+00', 1, '2026-08-18 17:23:34+00');
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(3, '普通用户', '普通操作员，拥有基本查看权限', 1, NULL, '2025-12-02 11:50:30+00', NULL, NULL);
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(7, 'add user', 'add user', 1, 1, '2026-01-05 16:54:36+00', 1, '2026-08-18 17:23:45+00');
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(8, 'edit user', 'edit user', 1, 1, '2026-01-05 17:00:49+00', 1, '2026-01-13 11:55:44+00');
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(9, 'del user', 'del user', 1, 1, '2026-01-05 17:07:12+00', 1, '2026-01-13 11:55:57+00');
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(10, 'list user', 'list user', 1, 1, '2026-01-05 17:07:48+00', 1, '2026-01-13 11:56:02+00');
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(11, '接口测试员', '接口测试员', 1, 1, '2026-08-18 16:49:05+00', 1, '2026-08-18 18:27:45+00');

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
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (68, 3, 22);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (69, 3, 23);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (70, 3, 24);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (71, 3, 29);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (72, 3, 33);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (73, 3, 41);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (74, 3, 42);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (75, 3, 43);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (103, 3, 59);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (104, 3, 11);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (210, 8, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (211, 8, 2);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (212, 8, 4);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (218, 9, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (219, 9, 2);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (220, 9, 5);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (222, 10, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (223, 10, 2);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (403, 2, 64);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (404, 2, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (405, 2, 65);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (406, 2, 2);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (407, 2, 66);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (408, 2, 3);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (409, 2, 67);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (410, 2, 4);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (411, 2, 5);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (412, 2, 6);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (413, 2, 7);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (414, 2, 8);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (415, 2, 9);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (416, 2, 10);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (417, 2, 11);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (418, 2, 12);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (419, 2, 13);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (420, 2, 14);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (421, 2, 41);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (422, 2, 42);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (423, 2, 43);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (424, 2, 44);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (425, 2, 46);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (426, 2, 47);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (427, 2, 48);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (428, 2, 49);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (429, 2, 50);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (430, 2, 51);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (431, 2, 52);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (432, 2, 53);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (433, 2, 55);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (434, 2, 56);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (435, 2, 57);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (436, 2, 58);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (437, 2, 59);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (438, 7, 64);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (439, 7, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (440, 7, 65);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (441, 7, 2);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (442, 7, 3);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (443, 7, 67);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (445, 1, 64);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (446, 1, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (447, 1, 65);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (448, 1, 2);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (449, 1, 66);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (450, 1, 3);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (451, 1, 67);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (452, 1, 4);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (453, 1, 68);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (454, 1, 5);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (455, 1, 6);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (456, 1, 7);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (457, 1, 8);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (458, 1, 9);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (459, 1, 10);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (460, 1, 11);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (461, 1, 12);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (462, 1, 13);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (463, 1, 14);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (464, 1, 19);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (465, 1, 20);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (466, 1, 22);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (467, 1, 37);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (468, 1, 38);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (469, 1, 39);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (470, 1, 40);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (471, 1, 41);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (472, 1, 42);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (473, 1, 43);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (474, 1, 44);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (475, 1, 46);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (476, 1, 47);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (477, 1, 48);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (478, 1, 49);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (479, 1, 50);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (480, 1, 51);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (481, 1, 52);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (482, 1, 53);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (483, 1, 55);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (484, 1, 56);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (485, 1, 57);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (486, 1, 58);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (487, 1, 59);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (488, 1, 61);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (489, 1, 63);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (500, 11, 64);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (501, 11, 1);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (502, 11, 68);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (503, 11, 41);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (504, 11, 49);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (505, 11, 50);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (506, 11, 51);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (507, 11, 52);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (508, 11, 53);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (509, 11, 55);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (510, 11, 56);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (511, 11, 57);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (512, 11, 58);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (543, 1, 69);
INSERT INTO sys_role_menu(id, role_id, menu_id) VALUES (545, 1, 70);

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
(1, 'admin', '$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', '超级管理员', 'admin@example.com', '13800138000', 1, NULL, '2025-12-02 11:50:46+00', 1, '2026-08-18 17:20:09+00');
INSERT INTO sys_user(user_id, username, password, real_name, email, mobile, status, created_by, create_time, updated_by, update_time) VALUES
(2, 'sysadmin', '$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', '系统管理员', 'zhangsan@example.com', '13800138001', 1, NULL, '2025-12-02 11:50:46+00', 1, '2026-08-18 17:20:18+00');
INSERT INTO sys_user(user_id, username, password, real_name, email, mobile, status, created_by, create_time, updated_by, update_time) VALUES
(3, 'user', '$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', '张三', 'lisi@example.com', '13800138002', 1, NULL, '2025-12-02 11:50:46+00', 1, '2026-08-18 17:20:28+00');
INSERT INTO sys_user(user_id, username, password, real_name, email, mobile, status, created_by, create_time, updated_by, update_time) VALUES
(5, 'laoqian', '$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', '老千', NULL, NULL, 1, 1, '2026-08-18 18:25:21+00', 1, '2026-08-18 18:25:21+00');

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
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (11, 1, 1);
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (12, 2, 2);
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (13, 3, 3);
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (14, 5, 11);

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
SELECT setval('sys_audit_log_log_id_seq', 1);
SELECT setval('sys_dict_dict_id_seq', 2);
SELECT setval('sys_dict_item_item_id_seq', 5);
SELECT setval('sys_menu_menu_id_seq', 71);
SELECT setval('sys_role_role_id_seq', 12);
SELECT setval('sys_role_menu_id_seq', 546);
SELECT setval('sys_user_user_id_seq', 6);
SELECT setval('sys_user_role_id_seq', 15);
