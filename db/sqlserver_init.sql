-- 文件信息表
IF OBJECT_ID('file_info', 'U') IS NOT NULL
    DROP TABLE file_info;

CREATE TABLE file_info (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    file_id NVARCHAR(64) NOT NULL,
    original_name NVARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type NVARCHAR(100),
    bucket_name NVARCHAR(100) NOT NULL,
    object_name NVARCHAR(500) NOT NULL,
    file_path NVARCHAR(1000),
    service_module NVARCHAR(100),
    business_type NVARCHAR(100),
    business_id NVARCHAR(100),
    status BIT NOT NULL DEFAULT 1,
    created_by BIGINT,
    create_time DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_by BIGINT,
    update_time DATETIME2 NOT NULL DEFAULT GETDATE()
);

CREATE UNIQUE INDEX uk_file_id ON file_info(file_id);
CREATE INDEX idx_business ON file_info(business_type, business_id);
CREATE INDEX idx_service_module ON file_info(service_module);
CREATE INDEX idx_created_time ON file_info(create_time);

-- 系统审计日志表
IF OBJECT_ID('sys_audit_log', 'U') IS NOT NULL
    DROP TABLE sys_audit_log;

CREATE TABLE sys_audit_log (
    log_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT,
    username NVARCHAR(50),
    operation NVARCHAR(50),
    method NVARCHAR(200),
    params NTEXT,
    result NTEXT,
    ip NVARCHAR(64),
    user_agent NVARCHAR(500),
    time BIGINT,
    created_by BIGINT,
    create_time DATETIME2 DEFAULT GETDATE(),
    updated_by BIGINT,
    update_time DATETIME2
);

CREATE INDEX idx_user_id ON sys_audit_log(user_id);
CREATE INDEX idx_create_time ON sys_audit_log(create_time);

-- 数据字典表
IF OBJECT_ID('sys_dict', 'U') IS NOT NULL
    DROP TABLE sys_dict;

CREATE TABLE sys_dict (
    dict_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    dict_name NVARCHAR(100) NOT NULL,
    dict_type NVARCHAR(100) NOT NULL,
    status BIT DEFAULT 1,
    remark NVARCHAR(500),
    created_by BIGINT,
    create_time DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_by BIGINT,
    update_time DATETIME2 NOT NULL DEFAULT GETDATE()
);

CREATE UNIQUE INDEX uk_dict_type ON sys_dict(dict_type);

-- 数据字典项表
IF OBJECT_ID('sys_dict_item', 'U') IS NOT NULL
    DROP TABLE sys_dict_item;

CREATE TABLE sys_dict_item (
    item_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    dict_id BIGINT NOT NULL,
    item_label NVARCHAR(100) NOT NULL,
    item_value NVARCHAR(100) NOT NULL,
    status BIT DEFAULT 1,
    order_num INT DEFAULT 0,
    remark NVARCHAR(500),
    created_by BIGINT,
    create_time DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_by BIGINT,
    update_time DATETIME2 NOT NULL DEFAULT GETDATE()
);

CREATE INDEX idx_dict_id ON sys_dict_item(dict_id);

-- 菜单管理表
IF OBJECT_ID('sys_menu', 'U') IS NOT NULL
    DROP TABLE sys_menu;

CREATE TABLE sys_menu (
    menu_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    parent_id BIGINT,
    name NVARCHAR(50) NOT NULL,
    url NVARCHAR(200),
    react_component NVARCHAR(50),
    perms NVARCHAR(500),
    type INT,
    icon NVARCHAR(50),
    order_num INT,
    created_by BIGINT,
    create_time DATETIME2,
    updated_by BIGINT,
    update_time DATETIME2
);

-- 插入数据
SET IDENTITY_INSERT sys_menu ON;

-- 插入数据
INSERT INTO sys_menu(menu_id, parent_id, name, url, react_component, perms, type, icon, order_num, created_by, create_time, updated_by, update_time)
VALUES
(1, 0, N'系统管理', N'sys', NULL, NULL, 0, N'setting', 0, NULL, NULL, NULL, NULL),
(2, 1, N'用户管理', N'sys/user', N'UserList', N'sys:user:list', 1, N'user', 0, NULL, NULL, NULL, NULL),
(3, 2, N'新增用户', NULL, NULL, N'sys:user:save', 2, NULL, 0, NULL, NULL, NULL, NULL),
(4, 2, N'修改用户', NULL, NULL, N'sys:user:update', 2, NULL, 1, NULL, NULL, NULL, NULL),
(5, 2, N'删除用户', NULL, NULL, N'sys:user:delete', 2, NULL, 2, NULL, NULL, NULL, NULL),
(6, 2, N'重置密码', NULL, NULL, N'sys:user:reset', 2, NULL, 3, NULL, NULL, NULL, NULL),
(7, 1, N'角色管理', N'sys/role', N'RoleList', N'sys:role:list', 1, N'team', 1, NULL, NULL, NULL, NULL),
(8, 7, N'新增角色', NULL, NULL, N'sys:role:save', 2, NULL, 0, NULL, NULL, NULL, NULL),
(9, 7, N'修改角色', NULL, NULL, N'sys:role:update', 2, NULL, 1, NULL, NULL, NULL, NULL),
(10, 7, N'删除角色', NULL, NULL, N'sys:role:delete', 2, NULL, 2, NULL, NULL, NULL, NULL),
(11, 1, N'菜单管理', N'sys/menu', N'MenuList', N'sys:menu:list', 1, N'menu', 2, NULL, NULL, NULL, NULL),
(12, 11, N'新增菜单', NULL, NULL, N'sys:menu:save', 2, NULL, 0, NULL, NULL, NULL, NULL),
(13, 11, N'修改菜单', NULL, NULL, N'sys:menu:update', 2, NULL, 1, NULL, NULL, NULL, NULL),
(14, 11, N'删除菜单', NULL, NULL, N'sys:menu:delete', 2, NULL, 2, NULL, NULL, NULL, NULL),
(15, 1, N'部门管理', N'sys/dept', NULL, N'sys:dept:list', 1, N'cluster', 3, NULL, NULL, NULL, NULL),
(16, 15, N'新增部门', NULL, NULL, N'sys:dept:save', 2, NULL, 0, NULL, NULL, NULL, NULL),
(17, 15, N'修改部门', NULL, NULL, N'sys:dept:update', 2, NULL, 1, NULL, NULL, NULL, NULL),
(18, 15, N'删除部门', NULL, NULL, N'sys:dept:delete', 2, NULL, 2, NULL, NULL, NULL, NULL),
(19, 1, N'系统日志', N'sys/log', NULL, N'sys:log:list', 1, N'file-text', 4, NULL, NULL, NULL, NULL),
(20, 19, N'删除日志', NULL, NULL, N'sys:log:delete', 2, NULL, 0, NULL, NULL, NULL, NULL),
(21, 0, N'仪表盘', N'dashboard', N'Dashboard', NULL, 0, N'dashboard', 1, NULL, NULL, NULL, NULL),
(22, 0, N'工作台', N'workbench', NULL, NULL, 0, N'desktop', 2, NULL, NULL, NULL, NULL),
(23, 0, N'内容管理', N'content', NULL, NULL, 0, N'container', 3, NULL, NULL, NULL, NULL),
(24, 23, N'文章管理', N'article', NULL, N'content:article:list', 1, N'file', 0, NULL, NULL, NULL, NULL),
(25, 24, N'新增文章', NULL, NULL, N'content:article:save', 2, NULL, 0, NULL, NULL, NULL, NULL),
(26, 24, N'修改文章', NULL, NULL, N'content:article:update', 2, NULL, 1, NULL, NULL, NULL, NULL),
(27, 24, N'删除文章', NULL, NULL, N'content:article:delete', 2, NULL, 2, NULL, NULL, NULL, NULL),
(28, 24, N'发布文章', NULL, NULL, N'content:article:publish', 2, NULL, 3, NULL, NULL, NULL, NULL),
(29, 23, N'分类管理', N'category', NULL, N'content:category:list', 1, N'folder', 1, NULL, NULL, NULL, NULL),
(30, 29, N'新增分类', NULL, NULL, N'content:category:save', 2, NULL, 0, NULL, NULL, NULL, NULL),
(31, 29, N'修改分类', NULL, NULL, N'content:category:update', 2, NULL, 1, NULL, NULL, NULL, NULL),
(32, 29, N'删除分类', NULL, NULL, N'content:category:delete', 2, NULL, 2, NULL, NULL, NULL, NULL),
(33, 23, N'标签管理', N'tag', NULL, N'content:tag:list', 1, N'tags', 2, NULL, NULL, NULL, NULL),
(34, 33, N'新增标签', NULL, NULL, N'content:tag:save', 2, NULL, 0, NULL, NULL, NULL, NULL),
(35, 33, N'修改标签', NULL, NULL, N'content:tag:update', 2, NULL, 1, NULL, NULL, NULL, NULL),
(36, 33, N'删除标签', NULL, NULL, N'content:tag:delete', 2, NULL, 2, NULL, NULL, NULL, NULL),
(37, 0, N'系统监控', N'monitor', NULL, NULL, 0, N'monitor', 4, NULL, NULL, NULL, NULL),
(38, 37, N'服务监控', N'server', NULL, N'monitor:server:list', 1, N'fund', 0, NULL, NULL, NULL, NULL),
(39, 37, N'Redis监控', N'redis', NULL, N'monitor:redis:list', 1, N'database', 1, NULL, NULL, NULL, NULL),
(40, 37, N'接口文档', N'http://localhost:8888/doc.html', NULL, NULL, 1, N'file-word', 2, NULL, NULL, NULL, NULL),
(41, 0, N'个人中心', N'profile', NULL, NULL, 0, N'user', 9, NULL, NULL, NULL, NULL),
(42, 41, N'修改信息', NULL, NULL, N'sys:profile:update', 1, NULL, 0, NULL, NULL, NULL, NULL),
(43, 41, N'修改密码', NULL, NULL, N'sys:profile:password', 1, NULL, 1, NULL, NULL, NULL, NULL),
(44, 23, N'文件管理', N'file', NULL, N'oss:file:list', 1, N'file-image', 3, NULL, NULL, NULL, NULL),
(45, 44, N'上传文件', NULL, NULL, N'oss:file:upload', 2, NULL, 0, NULL, NULL, NULL, NULL),
(46, 44, N'下载文件', NULL, NULL, N'oss:file:download', 2, NULL, 1, NULL, NULL, NULL, NULL),
(47, 44, N'获取文件链接', NULL, NULL, N'oss:file:url', 2, NULL, 2, NULL, NULL, NULL, NULL),
(48, 44, N'删除文件', NULL, NULL, N'oss:file:delete', 2, NULL, 3, NULL, NULL, NULL, NULL),
(49, 1, N'数据字典', N'sys/dict', NULL, N'sys:dict:list', 1, N'book', 5, NULL, NULL, NULL, NULL),
(50, 49, N'查询字典', NULL, NULL, N'sys:dict:list', 2, NULL, 0, NULL, NULL, NULL, NULL),
(51, 49, N'新增字典', NULL, NULL, N'sys:dict:save', 2, NULL, 1, NULL, NULL, NULL, NULL),
(52, 49, N'修改字典', NULL, NULL, N'sys:dict:update', 2, NULL, 2, NULL, NULL, NULL, NULL),
(53, 49, N'删除字典', NULL, NULL, N'sys:dict:delete', 2, NULL, 3, NULL, NULL, NULL, NULL),
(54, 1, N'字典项管理', N'sys/dictitem', NULL, N'sys:dict:item:list', 1, N'unordered-list', 6, NULL, NULL, NULL, NULL),
(55, 54, N'查询字典项', NULL, NULL, N'sys:dict:item:list', 2, NULL, 0, NULL, NULL, NULL, NULL),
(56, 54, N'新增字典项', NULL, NULL, N'sys:dict:item:save', 2, NULL, 1, NULL, NULL, NULL, NULL),
(57, 54, N'修改字典项', NULL, NULL, N'sys:dict:item:update', 2, NULL, 2, NULL, NULL, NULL, NULL),
(58, 54, N'删除字典项', NULL, NULL, N'sys:dict:item:delete', 2, NULL, 3, NULL, NULL, NULL, NULL),
(59, 2, N'用户信息', NULL, NULL, N'sys:user:info', 2, NULL, 5, NULL, NULL, NULL, NULL),
(61, 2, N'登出接口', NULL, NULL, N'sys:user:logout', 2, NULL, 6, 1, '2026-01-07 15:06:11', 1, '2026-01-07 15:06:11');

SET IDENTITY_INSERT sys_menu OFF;
-- 角色表
IF OBJECT_ID('sys_role', 'U') IS NOT NULL
    DROP TABLE sys_role;

CREATE TABLE sys_role (
    role_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    role_name NVARCHAR(50) NOT NULL,
    role_desc NVARCHAR(100),
    status BIT DEFAULT 1,
    created_by BIGINT,
    create_time DATETIME2 DEFAULT GETDATE(),
    updated_by BIGINT,
    update_time DATETIME2
);

-- 插入数据
SET IDENTITY_INSERT sys_role ON;

-- 插入数据
INSERT INTO sys_role(role_id, role_name, role_desc, status, created_by, create_time, updated_by, update_time)
VALUES
(1, N'超级管理员', N'拥有系统所有权限，最高权限角色', 1, NULL, '2025-12-02 11:50:30', NULL, NULL),
(2, N'系统管理员', N'管理系统基础配置和用户', 1, NULL, '2025-12-02 11:50:30', NULL, NULL),
(3, N'普通用户', N'普通操作员，拥有基本查看权限', 1, NULL, '2025-12-02 11:50:30', NULL, NULL),
(7, N'add user', N'add user', 1, 1, '2026-01-05 16:54:36', 1, '2026-01-05 16:54:36'),
(8, N'edit user', N'edit user', 1, 1, '2026-01-05 17:00:49', 1, '2026-01-05 17:00:49'),
(9, N'del user', N'del user', 1, 1, '2026-01-05 17:07:12', 1, '2026-01-05 17:07:12'),
(10, N'Reader', N'Reader', 1, 1, '2026-01-05 17:07:48', 1, '2026-01-05 17:07:48'),
(11, N'111', N'111', 1, 1, '2026-01-05 17:07:54', 1, '2026-01-05 17:07:54'),
(12, N'222', N'222', 1, 1, '2026-01-05 17:08:01', 1, '2026-01-05 17:08:01'),
(13, N'311', N'322', 1, 1, '2026-01-05 17:08:12', 1, '2026-01-05 17:08:12'),
(14, N'123', N'123', 1, 1, '2026-01-05 17:08:20', 1, '2026-01-05 17:09:10'),
(15, N'321', N'321', 1, 1, '2026-01-05 17:12:40', 1, '2026-01-05 17:12:40');

SET IDENTITY_INSERT sys_role OFF;

-- 角色与菜单对应关系表
IF OBJECT_ID('sys_role_menu', 'U') IS NOT NULL
    DROP TABLE sys_role_menu;

CREATE TABLE sys_role_menu (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL
);

-- 插入数据
SET IDENTITY_INSERT sys_role_menu ON;

-- 插入数据
INSERT INTO sys_role_menu(id, role_id, menu_id)
VALUES
(1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4), (5, 1, 5), (6, 1, 6), (7, 1, 7), (8, 1, 8), (9, 1, 9), (10, 1, 10),
(11, 1, 11), (12, 1, 12), (13, 1, 13), (14, 1, 14), (15, 1, 15), (16, 1, 16), (17, 1, 17), (18, 1, 18), (19, 1, 19), (20, 1, 20),
(21, 1, 21), (22, 1, 22), (23, 1, 23), (24, 1, 24), (25, 1, 25), (26, 1, 26), (27, 1, 27), (28, 1, 28), (29, 1, 29), (30, 1, 30),
(31, 1, 31), (32, 1, 32), (33, 1, 33), (34, 1, 34), (35, 1, 35), (36, 1, 36), (37, 1, 37), (38, 1, 38), (39, 1, 39), (40, 1, 40),
(41, 1, 41), (42, 1, 42), (43, 1, 43), (44, 2, 1), (45, 2, 2), (46, 2, 3), (47, 2, 4), (48, 2, 5), (49, 2, 6), (50, 2, 7),
(51, 2, 8), (52, 2, 9), (53, 2, 10), (54, 2, 11), (55, 2, 12), (56, 2, 13), (57, 2, 14), (58, 2, 21), (59, 2, 22), (60, 2, 23),
(61, 2, 24), (62, 2, 29), (63, 2, 33), (64, 2, 41), (65, 2, 42), (66, 2, 43), (67, 3, 21), (68, 3, 22), (69, 3, 23), (70, 3, 24),
(71, 3, 29), (72, 3, 33), (73, 3, 41), (74, 3, 42), (75, 3, 43), (76, 1, 44), (77, 1, 45), (78, 1, 46), (79, 1, 47), (80, 1, 48),
(81, 1, 49), (82, 1, 50), (83, 1, 51), (84, 1, 52), (85, 1, 53), (86, 1, 54), (87, 1, 55), (88, 1, 56), (89, 1, 57), (90, 1, 58),
(91, 2, 49), (92, 2, 50), (93, 2, 51), (94, 2, 52), (95, 2, 53), (96, 2, 54), (97, 2, 55), (98, 2, 56), (99, 2, 57), (100, 2, 58),
(101, 1, 59), (102, 2, 59), (103, 3, 59), (104, 3, 11), (105, 7, 3), (106, 8, 4), (107, 9, 5), (108, 10, 9), (109, 11, 9),
(110, 11, 10), (111, 12, 21), (112, 12, 22), (113, 12, 23), (114, 12, 24), (115, 12, 29), (116, 12, 33), (117, 12, 44), (118, 12, 25),
(119, 12, 26), (120, 12, 27), (121, 12, 28), (122, 12, 30), (123, 12, 31), (124, 12, 32), (125, 12, 34), (126, 12, 35), (127, 12, 36),
(128, 12, 45), (129, 12, 46), (130, 12, 47), (131, 12, 48), (132, 13, 21), (133, 13, 37), (134, 13, 38), (135, 13, 39), (136, 13, 40),
(161, 14, 19), (162, 14, 20), (163, 14, 49), (164, 14, 50), (165, 14, 51), (166, 14, 52), (167, 14, 53), (168, 14, 54), (169, 14, 55),
(170, 14, 56), (171, 14, 57), (172, 14, 58), (173, 15, 1), (174, 15, 2), (175, 15, 7), (176, 15, 11), (177, 15, 15), (178, 15, 19),
(179, 15, 49), (180, 15, 3), (181, 15, 4), (182, 15, 5), (183, 15, 6), (184, 15, 59), (185, 15, 8), (186, 15, 9), (187, 15, 10),
(188, 15, 12), (189, 15, 13), (190, 15, 14), (191, 15, 16), (192, 15, 17), (193, 15, 18), (194, 15, 20), (195, 15, 50), (196, 15, 51),
(197, 15, 52), (198, 15, 53), (199, 15, 54), (200, 15, 55), (201, 15, 56), (202, 15, 57), (203, 15, 58);


SET IDENTITY_INSERT sys_role_menu OFF;

-- 系统用户表
IF OBJECT_ID('sys_user', 'U') IS NOT NULL
    DROP TABLE sys_user;

CREATE TABLE sys_user (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL,
    password NVARCHAR(100) NOT NULL,
    real_name NVARCHAR(50),
    email NVARCHAR(100),
    mobile NVARCHAR(20),
    status BIT DEFAULT 1,
    created_by BIGINT,
    create_time DATETIME2 DEFAULT GETDATE(),
    updated_by BIGINT,
    update_time DATETIME2
);

CREATE UNIQUE INDEX uk_username ON sys_user(username);

-- 插入数据
SET IDENTITY_INSERT sys_user ON;

-- 插入数据
INSERT INTO sys_user(user_id, username, password, real_name, email, mobile, status, created_by, create_time, updated_by, update_time)
VALUES
(1, N'admin', N'$2a$10$bddSw9Plnp9wvu8/XyNGz.EA.EtpSa/Bc2ag399fXWaHpQ93bwxXa', N'系统管理员', N'admin@example.com', N'13800138000', 1, NULL, '2025-12-02 11:50:46', NULL, NULL),
(2, N'sysadmin', N'$2a$10$XhTMMAx1jCp/XzpXvNNnzOtI5l44E/XIY98fDZy7Os2Q/VPBZDGLe', N'张三', N'zhangsan@example.com', N'13800138001', 1, NULL, '2025-12-02 11:50:46', NULL, NULL),
(3, N'user', N'$2a$10$mrOhEfIDpwpq/i0j1PQeYexHysPVN8DCuG6vNo8rIHZ4C6w3aKQo6', N'李四', N'lisi@example.com', N'13800138002', 1, NULL, '2025-12-02 11:50:46', NULL, NULL),
(16, N'CPATEST01', N'$2a$10$S7UWQRsnn7xGKwxkhdhwD.7XxyfJ6tMc2zIq3.AvsWXOSKAm41yZG', N'112', N'CarterTest08@pccw.com', NULL, 1, 1, '2025-12-12 18:37:54', 1, '2025-12-12 18:39:03'),
(17, N'OFFICER2', N'$2a$10$oNBdj.d2pvaDUGkZ69NNE.lDZ3b5re9pw4x.7EwtNtipuxHG.uQnG', N'OFFICER2', N'skypeadmin01@pccwsolutionltd.onmicrosoft.com', NULL, 1, 1, '2025-12-12 18:57:27', 1, '2025-12-12 18:57:27'),
(18, N'CPATEST3', N'$2a$10$EEWwVWqdaZlRf3M.ynOSdek9ROlOdeR3GrSPJ2LD5vcQ3uNGCBL0S', N'CPATEST3', N'CarterTest08@pccw.com', NULL, 0, 1, '2025-12-12 18:59:06', 1, '2025-12-12 18:59:06');

SET IDENTITY_INSERT sys_user OFF;

-- 用户与角色对应关系表
IF OBJECT_ID('sys_user_role', 'U') IS NOT NULL
    DROP TABLE sys_user_role;

CREATE TABLE sys_user_role (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
);

-- 插入数据
SET IDENTITY_INSERT sys_user_role ON;
-- 插入数据
INSERT INTO sys_user_role(id, user_id, role_id)
VALUES
(1, 1, 1), (2, 2, 2), (3, 3, 3), (4, 1, 1), (5, 2, 2), (6, 3, 3), (7, 4, 2), (8, 4, 3), (9, 17, 2), (10, 18, 3);

SET IDENTITY_INSERT sys_user_role OFF;