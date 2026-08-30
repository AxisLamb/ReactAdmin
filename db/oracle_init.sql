-- 创建数据库（Oracle中通常创建表空间）
CREATE TABLESPACE LAIN_TBSP
DATAFILE 'lain_data.dbf' SIZE 100M
AUTOEXTEND ON NEXT 10M MAXSIZE UNLIMITED;

-- 创建用户并分配表空间
CREATE USER lain_user IDENTIFIED BY password DEFAULT TABLESPACE LAIN_TBSP;
GRANT CONNECT, RESOURCE TO lain_user;
GRANT CREATE SESSION TO lain_user;

-- 切换到新建的用户
CONNECT lain_user/password;

-- 文件信息表
CREATE TABLE file_info (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    file_id VARCHAR2(64) NOT NULL,
    user_id NUMBER,
    original_name VARCHAR2(255) NOT NULL,
    file_size NUMBER NOT NULL,
    file_type VARCHAR2(100),
    bucket_name VARCHAR2(100) NOT NULL,
    object_name VARCHAR2(500) NOT NULL,
    file_path VARCHAR2(1000),
    service_module VARCHAR2(100),
    business_type VARCHAR2(100),
    business_table VARCHAR2(100),
    business_id VARCHAR2(100),
    status NUMBER(1) DEFAULT 1 NOT NULL,
    created_by NUMBER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by NUMBER,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 创建索引
CREATE UNIQUE INDEX uk_file_id ON file_info(file_id);
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
    log_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER,
    username VARCHAR2(50),
    operation VARCHAR2(50),
    method VARCHAR2(200),
    params CLOB,
    result CLOB,
    ip VARCHAR2(64),
    user_agent VARCHAR2(500),
    time NUMBER,
    created_by NUMBER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by NUMBER,
    update_time TIMESTAMP
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
    dict_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dict_name VARCHAR2(100) NOT NULL,
    dict_type VARCHAR2(100) NOT NULL,
    status NUMBER(1) DEFAULT 1,
    remark VARCHAR2(500),
    created_by NUMBER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by NUMBER,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 创建索引
CREATE UNIQUE INDEX uk_dict_type ON sys_dict(dict_type);

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

-- 插入字典数据
INSERT INTO sys_dict(dict_name, dict_type, status, remark, created_by, create_time, updated_by, update_time) VALUES
('image', 'image', 1, '图片定义', 1, SYSDATE, 1, SYSDATE);

-- 数据字典项表
CREATE TABLE sys_dict_item (
    item_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dict_id NUMBER NOT NULL,
    item_label VARCHAR2(100) NOT NULL,
    item_value VARCHAR2(100) NOT NULL,
    status NUMBER(1) DEFAULT 1,
    order_num NUMBER DEFAULT 0,
    remark VARCHAR2(500),
    created_by NUMBER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by NUMBER,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
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

-- 插入字典项数据
INSERT INTO sys_dict_item(dict_id, item_label, item_value, status, order_num, remark, created_by, create_time, updated_by, update_time) VALUES
(1, 'avatar', 'user', 1, 0, 'module', 1, SYSDATE, 1, SYSDATE);
INSERT INTO sys_dict_item(dict_id, item_label, item_value, status, order_num, remark, created_by, create_time, updated_by, update_time) VALUES
(1, 'avatar', 'sys_user', 1, 1, '业务表', 1, SYSDATE, 1, SYSDATE);
INSERT INTO sys_dict_item(dict_id, item_label, item_value, status, order_num, remark, created_by, create_time, updated_by, update_time) VALUES
(1, 'loginPage', 'sys', 1, 2, 'module', 1, SYSDATE, 1, SYSDATE);
INSERT INTO sys_dict_item(dict_id, item_label, item_value, status, order_num, remark, created_by, create_time, updated_by, update_time) VALUES
(1, 'loginPage', 'file_info', 1, 3, '业务表', 1, SYSDATE, 1, SYSDATE);

-- 菜单管理表
CREATE TABLE sys_menu (
    menu_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id NUMBER,
    name VARCHAR2(50) NOT NULL,
    url VARCHAR2(200),
    react_component VARCHAR2(50),
    perms VARCHAR2(500),
    type NUMBER,
    icon VARCHAR2(50),
    order_num NUMBER,
    created_by NUMBER,
    create_time TIMESTAMP,
    updated_by NUMBER,
    update_time TIMESTAMP
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
(1, 0, '系统管理', 'sys', NULL, NULL, 0, 'setting', 1, NULL, NULL, 1, TO_DATE('2026-08-18 16:08:18', 'YYYY-MM-DD HH24:MI:SS'));
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
(22, 64, '首页统计', NULL, NULL, NULL, 4, 'bar-chart', 1, NULL, NULL, 1, TO_DATE('2026-08-18 16:13:13', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(37, 0, '系统监控', 'monitor', NULL, NULL, 0, 'dashboard', 2, NULL, NULL, 1, TO_DATE('2026-08-18 16:08:11', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(38, 37, '服务监控', 'server', NULL, 'monitor:server:list', 1, 'safety', 0, NULL, NULL, 1, TO_DATE('2026-08-18 14:41:49', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(39, 37, 'Redis监控', 'redis', NULL, 'monitor:redis:list', 1, 'database', 1, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(40, 37, '接口文档', NULL, NULL, NULL, 1, 'database', 2, NULL, NULL, 1, TO_DATE('2026-08-18 14:42:19', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(41, 64, '个人中心', NULL, NULL, NULL, 4, 'user', 2, NULL, NULL, 1, TO_DATE('2026-08-18 17:02:43', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(42, 41, '修改信息', NULL, NULL, 'sys:user:update', 5, NULL, 0, NULL, NULL, 1, TO_DATE('2026-08-18 14:45:11', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(43, 41, '修改密码', NULL, NULL, 'sys:user:update', 5, NULL, 1, NULL, NULL, 1, TO_DATE('2026-08-18 14:45:18', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(44, 1, '文件管理', 'sys/file', 'FileList', 'oss:file:list', 1, 'folder', 3, NULL, NULL, 1, TO_DATE('2026-08-18 14:40:40', 'YYYY-MM-DD HH24:MI:SS'));
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
(61, 41, '登出接口', NULL, NULL, 'sys:user:logout', 5, NULL, 6, 1, TO_DATE('2026-01-07 15:06:11', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-01-07 15:06:11', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(63, 22, '数据统计', NULL, NULL, 'sys:dashboard:list', 5, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(64, 0, '通用接口', NULL, NULL, NULL, 3, 'star', 3, 1, TO_DATE('2026-08-18 14:43:23', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 16:13:03', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(65, 64, '文件', NULL, NULL, NULL, 4, 'folder', 3, 1, TO_DATE('2026-08-18 17:02:16', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 17:02:23', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(66, 65, '文件上传', NULL, NULL, 'oss:file:upload', 5, NULL, 1, 1, TO_DATE('2026-08-18 17:04:10', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 17:04:10', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(67, 65, '文件链接', NULL, NULL, 'oss:file:url', 5, NULL, 2, 1, TO_DATE('2026-08-18 17:04:54', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 17:04:59', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(68, 41, '上传头像', NULL, NULL, 'sys:user:upload', 5, NULL, 3, 1, TO_DATE('2026-08-18 18:23:39', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 18:23:39', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(69, 1, '系统配置', 'sys/config', 'SysConfig', NULL, 1, 'setting', 10, NULL, NULL, NULL, NULL);
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time) VALUES
(70, 69, '上传图片', NULL, NULL, 'sys:config:loginPageUpload', 2, NULL, 7, NULL, NULL, NULL, NULL);

-- 角色表
CREATE TABLE sys_role (
    role_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name VARCHAR2(50) NOT NULL,
    role_desc VARCHAR2(100),
    status NUMBER(1) DEFAULT 1,
    created_by NUMBER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by NUMBER,
    update_time TIMESTAMP
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
(1, '超级管理员', '拥有系统所有权限，最高权限角色', 1, NULL, TO_DATE('2025-12-02 11:50:30', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 18:24:14', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(2, '系统管理员', '管理系统基础配置和用户', 1, NULL, TO_DATE('2025-12-02 11:50:30', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 17:23:34', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(3, '普通用户', '普通操作员，拥有基本查看权限', 1, NULL, TO_DATE('2025-12-02 11:50:30', 'YYYY-MM-DD HH24:MI:SS'), NULL, NULL);
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(7, 'add user', 'add user', 1, 1, TO_DATE('2026-01-05 16:54:36', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 17:23:45', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(8, 'edit user', 'edit user', 1, 1, TO_DATE('2026-01-05 17:00:49', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-01-13 11:55:44', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(9, 'del user', 'del user', 1, 1, TO_DATE('2026-01-05 17:07:12', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-01-13 11:55:57', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(10, 'list user', 'list user', 1, 1, TO_DATE('2026-01-05 17:07:48', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-01-13 11:56:02', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time) VALUES
(11, '接口测试员', '接口测试员', 1, 1, TO_DATE('2026-08-18 16:49:05', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 18:27:45', 'YYYY-MM-DD HH24:MI:SS'));

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_id NUMBER NOT NULL,
    menu_id NUMBER NOT NULL
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
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR2(50) NOT NULL,
    password VARCHAR2(100) NOT NULL,
    real_name VARCHAR2(50),
    email VARCHAR2(100),
    mobile VARCHAR2(20),
    status NUMBER(1) DEFAULT 1,
    created_by NUMBER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by NUMBER,
    update_time TIMESTAMP
);

-- 创建唯一索引
CREATE UNIQUE INDEX uk_username ON sys_user(username);

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
(1, 'admin', '$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', '超级管理员', 'admin@example.com', '13800138000', 1, NULL, TO_DATE('2025-12-02 11:50:46', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 17:20:09', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_user(user_id, username, password, real_name, email, mobile, status, created_by, create_time, updated_by, update_time) VALUES
(2, 'sysadmin', '$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', '系统管理员', 'zhangsan@example.com', '13800138001', 1, NULL, TO_DATE('2025-12-02 11:50:46', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 17:20:18', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_user(user_id, username, password, real_name, email, mobile, status, created_by, create_time, updated_by, update_time) VALUES
(3, 'user', '$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', '张三', 'lisi@example.com', '13800138002', 1, NULL, TO_DATE('2025-12-02 11:50:46', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 17:20:28', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO sys_user(user_id, username, password, real_name, email, mobile, status, created_by, create_time, updated_by, update_time) VALUES
(5, 'laoqian', '$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', '老千', NULL, NULL, 1, 1, TO_DATE('2026-08-18 18:25:21', 'YYYY-MM-DD HH24:MI:SS'), 1, TO_DATE('2026-08-18 18:25:21', 'YYYY-MM-DD HH24:MI:SS'));

-- 用户角色关联表
CREATE TABLE sys_user_role (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL,
    role_id NUMBER NOT NULL
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

-- 创建触发器自动更新update_time字段
CREATE OR REPLACE TRIGGER tr_file_info_update_time
    BEFORE UPDATE ON file_info
    FOR EACH ROW
BEGIN
    :NEW.update_time := CURRENT_TIMESTAMP;
END;

CREATE OR REPLACE TRIGGER tr_sys_audit_log_update_time
    BEFORE UPDATE ON sys_audit_log
    FOR EACH ROW
BEGIN
    :NEW.update_time := CURRENT_TIMESTAMP;
END;

CREATE OR REPLACE TRIGGER tr_sys_dict_update_time
    BEFORE UPDATE ON sys_dict
    FOR EACH ROW
BEGIN
    :NEW.update_time := CURRENT_TIMESTAMP;
END;

CREATE OR REPLACE TRIGGER tr_sys_dict_item_update_time
    BEFORE UPDATE ON sys_dict_item
    FOR EACH ROW
BEGIN
    :NEW.update_time := CURRENT_TIMESTAMP;
END;

CREATE OR REPLACE TRIGGER tr_sys_menu_update_time
    BEFORE UPDATE ON sys_menu
    FOR EACH ROW
BEGIN
    :NEW.update_time := CURRENT_TIMESTAMP;
END;

CREATE OR REPLACE TRIGGER tr_sys_role_update_time
    BEFORE UPDATE ON sys_role
    FOR EACH ROW
BEGIN
    :NEW.update_time := CURRENT_TIMESTAMP;
END;

CREATE OR REPLACE TRIGGER tr_sys_user_update_time
    BEFORE UPDATE ON sys_user
    FOR EACH ROW
BEGIN
    :NEW.update_time := CURRENT_TIMESTAMP;
END;

-- 序列创建（虽然使用了IDENTITY，但有时可能需要手动创建序列）
CREATE SEQUENCE seq_file_info START WITH 6 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_sys_audit_log START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_sys_dict START WITH 2 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_sys_dict_item START WITH 5 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_sys_menu START WITH 71 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_sys_role START WITH 12 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_sys_role_menu START WITH 546 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_sys_user START WITH 6 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_sys_user_role START WITH 15 INCREMENT BY 1 NOCACHE;

-- 闲鱼机器人聊天消息表
CREATE TABLE xianyu_chat_message (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_id VARCHAR2(64) NOT NULL,
    user_id VARCHAR2(64) NOT NULL,
    item_id VARCHAR2(64) NOT NULL,
    role VARCHAR2(16) NOT NULL,
    content CLOB NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 创建索引
CREATE INDEX idx_chat_id ON xianyu_chat_message(chat_id);
CREATE INDEX idx_user_item ON xianyu_chat_message(user_id, item_id);
CREATE INDEX idx_create_time ON xianyu_chat_message(create_time);

-- 添加注释
COMMENT ON TABLE xianyu_chat_message IS '闲鱼机器人聊天消息表';
COMMENT ON COLUMN xianyu_chat_message.id IS '主键ID';
COMMENT ON COLUMN xianyu_chat_message.chat_id IS '会话ID';
COMMENT ON COLUMN xianyu_chat_message.user_id IS '用户ID(用户消息存真实user_id，助手消息存卖家ID)';
COMMENT ON COLUMN xianyu_chat_message.item_id IS '商品ID';
COMMENT ON COLUMN xianyu_chat_message.role IS '消息角色: user/assistant/system';
COMMENT ON COLUMN xianyu_chat_message.content IS '消息内容';
COMMENT ON COLUMN xianyu_chat_message.create_time IS '创建时间';

-- 闲鱼会话议价次数表
CREATE TABLE xianyu_chat_bargain (
    chat_id VARCHAR2(64) PRIMARY KEY,
    count NUMBER DEFAULT 0 NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 添加注释
COMMENT ON TABLE xianyu_chat_bargain IS '闲鱼会话议价次数表';
COMMENT ON COLUMN xianyu_chat_bargain.chat_id IS '会话ID';
COMMENT ON COLUMN xianyu_chat_bargain.count IS '议价次数';
COMMENT ON COLUMN xianyu_chat_bargain.last_updated IS '最后更新时间';

-- 闲鱼商品信息缓存表
CREATE TABLE xianyu_item (
    item_id VARCHAR2(64) PRIMARY KEY,
    data CLOB NOT NULL,
    price NUMBER(10,2),
    description CLOB,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 添加注释
COMMENT ON TABLE xianyu_item IS '闲鱼商品信息缓存表';
COMMENT ON COLUMN xianyu_item.item_id IS '商品ID';
COMMENT ON COLUMN xianyu_item.data IS '商品完整数据(JSON)';
COMMENT ON COLUMN xianyu_item.price IS '商品价格(元)';
COMMENT ON COLUMN xianyu_item.description IS '商品描述';
COMMENT ON COLUMN xianyu_item.last_updated IS '最后更新时间';
