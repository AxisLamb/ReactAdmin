-- 检查并创建数据库 lain_db
-- 需要在SSMS执行，在DBeaber执行会失败
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'lain_db')
BEGIN
    CREATE DATABASE lain_db;
END
GO

USE [lain_db]
GO
/****** Object:  Table [dbo].[file_info]    Script Date: 2026/1/13 11:58:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[file_info](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[file_id] [nvarchar](64) NOT NULL,
	[original_name] [nvarchar](255) NOT NULL,
	[file_size] [bigint] NOT NULL,
	[file_type] [nvarchar](100) NULL,
	[bucket_name] [nvarchar](100) NOT NULL,
	[object_name] [nvarchar](500) NOT NULL,
	[file_path] [nvarchar](1000) NULL,
	[service_module] [nvarchar](100) NULL,
	[business_type] [nvarchar](100) NULL,
	[business_id] [nvarchar](100) NULL,
	[status] [bit] NOT NULL,
	[created_by] [bigint] NULL,
	[create_time] [datetime2](7) NOT NULL,
	[updated_by] [bigint] NULL,
	[update_time] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[sys_audit_log]    Script Date: 2026/1/13 11:58:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sys_audit_log](
	[log_id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_id] [bigint] NULL,
	[username] [nvarchar](50) NULL,
	[operation] [nvarchar](50) NULL,
	[method] [nvarchar](200) NULL,
	[params] [ntext] NULL,
	[result] [ntext] NULL,
	[ip] [nvarchar](64) NULL,
	[user_agent] [nvarchar](500) NULL,
	[time] [bigint] NULL,
	[created_by] [bigint] NULL,
	[create_time] [datetime2](7) NULL,
	[updated_by] [bigint] NULL,
	[update_time] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[log_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[sys_dict]    Script Date: 2026/1/13 11:58:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sys_dict](
	[dict_id] [bigint] IDENTITY(1,1) NOT NULL,
	[dict_name] [nvarchar](100) NOT NULL,
	[dict_type] [nvarchar](100) NOT NULL,
	[status] [bit] NULL,
	[remark] [nvarchar](500) NULL,
	[created_by] [bigint] NULL,
	[create_time] [datetime2](7) NOT NULL,
	[updated_by] [bigint] NULL,
	[update_time] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[dict_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[sys_dict_item]    Script Date: 2026/1/13 11:58:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sys_dict_item](
	[item_id] [bigint] IDENTITY(1,1) NOT NULL,
	[dict_id] [bigint] NOT NULL,
	[item_label] [nvarchar](100) NOT NULL,
	[item_value] [nvarchar](100) NOT NULL,
	[status] [bit] NULL,
	[order_num] [int] NULL,
	[remark] [nvarchar](500) NULL,
	[created_by] [bigint] NULL,
	[create_time] [datetime2](7) NOT NULL,
	[updated_by] [bigint] NULL,
	[update_time] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[item_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[sys_menu]    Script Date: 2026/1/13 11:58:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sys_menu](
	[menu_id] [bigint] IDENTITY(1,1) NOT NULL,
	[parent_id] [bigint] NULL,
	[name] [nvarchar](50) NOT NULL,
	[url] [nvarchar](200) NULL,
	[react_component] [nvarchar](50) NULL,
	[perms] [nvarchar](500) NULL,
	[type] [int] NULL,
	[icon] [nvarchar](50) NULL,
	[order_num] [int] NULL,
	[created_by] [bigint] NULL,
	[create_time] [datetime2](7) NULL,
	[updated_by] [bigint] NULL,
	[update_time] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[menu_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[sys_role]    Script Date: 2026/1/13 11:58:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sys_role](
	[role_id] [bigint] IDENTITY(1,1) NOT NULL,
	[role_name] [nvarchar](50) NOT NULL,
	[role_desc] [nvarchar](100) NULL,
	[status] [bit] NULL,
	[created_by] [bigint] NULL,
	[create_time] [datetime2](7) NULL,
	[updated_by] [bigint] NULL,
	[update_time] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[sys_role_menu]    Script Date: 2026/1/13 11:58:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sys_role_menu](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[role_id] [bigint] NOT NULL,
	[menu_id] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[sys_user]    Script Date: 2026/1/13 11:58:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sys_user](
	[user_id] [bigint] IDENTITY(1,1) NOT NULL,
	[username] [nvarchar](50) NOT NULL,
	[password] [nvarchar](100) NOT NULL,
	[real_name] [nvarchar](50) NULL,
	[email] [nvarchar](100) NULL,
	[mobile] [nvarchar](20) NULL,
	[status] [bit] NULL,
	[created_by] [bigint] NULL,
	[create_time] [datetime2](7) NULL,
	[updated_by] [bigint] NULL,
	[update_time] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[sys_user_role]    Script Date: 2026/1/13 11:58:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sys_user_role](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_id] [bigint] NOT NULL,
	[role_id] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[sys_audit_log] ON

INSERT [dbo].[sys_audit_log] ([log_id], [user_id], [username], [operation], [method], [params], [result], [ip], [user_agent], [time], [created_by], [create_time], [updated_by], [update_time]) VALUES (1, 1, NULL, N'新增用户', N'com.lain.modules.sys.controller.SysUserController.save()', N'SysUserVO(userId=null, username=lain, roleId=9, roleName=null, password=123456, realName=lain, email=null, mobile=null, status=1) ', N'R(code=0, msg=success, data=保存成功)', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36', 183, 1, CAST(N'2026-01-13T11:51:00.0843034' AS DateTime2), 1, CAST(N'2026-01-13T11:51:00.0843034' AS DateTime2))
SET IDENTITY_INSERT [dbo].[sys_audit_log] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_menu] ON

INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (1, 0, N'系统管理', N'sys', NULL, NULL, 0, N'setting', 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (2, 1, N'用户管理', N'sys/user', N'UserList', N'sys:user:list', 1, N'user', 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (3, 2, N'新增用户', NULL, NULL, N'sys:user:save', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (4, 2, N'修改用户', NULL, NULL, N'sys:user:update', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (5, 2, N'删除用户', NULL, NULL, N'sys:user:delete', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (6, 2, N'重置密码', NULL, NULL, N'sys:user:reset', 2, NULL, 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (7, 1, N'角色管理', N'sys/role', N'RoleList', N'sys:role:list', 1, N'team', 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (8, 7, N'新增角色', NULL, NULL, N'sys:role:save', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (9, 7, N'修改角色', NULL, NULL, N'sys:role:update', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (10, 7, N'删除角色', NULL, NULL, N'sys:role:delete', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (11, 1, N'菜单管理', N'sys/menu', N'MenuList', N'sys:menu:list', 1, N'menu', 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (12, 11, N'新增菜单', NULL, NULL, N'sys:menu:save', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (13, 11, N'修改菜单', NULL, NULL, N'sys:menu:update', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (14, 11, N'删除菜单', NULL, NULL, N'sys:menu:delete', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (15, 1, N'部门管理', N'sys/dept', NULL, N'sys:dept:list', 1, N'cluster', 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (16, 15, N'新增部门', NULL, NULL, N'sys:dept:save', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (17, 15, N'修改部门', NULL, NULL, N'sys:dept:update', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (18, 15, N'删除部门', NULL, NULL, N'sys:dept:delete', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (19, 1, N'系统日志', N'sys/log', NULL, N'sys:log:list', 1, N'file-text', 4, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (20, 19, N'删除日志', NULL, NULL, N'sys:log:delete', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (22, 0, N'工作台', N'workbench', NULL, NULL, 0, N'desktop', 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (23, 0, N'内容管理', N'content', NULL, NULL, 0, N'container', 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (24, 23, N'文章管理', N'article', NULL, N'content:article:list', 1, N'file', 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (25, 24, N'新增文章', NULL, NULL, N'content:article:save', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (26, 24, N'修改文章', NULL, NULL, N'content:article:update', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (27, 24, N'删除文章', NULL, NULL, N'content:article:delete', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (28, 24, N'发布文章', NULL, NULL, N'content:article:publish', 2, NULL, 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (29, 23, N'分类管理', N'category', NULL, N'content:category:list', 1, N'folder', 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (30, 29, N'新增分类', NULL, NULL, N'content:category:save', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (31, 29, N'修改分类', NULL, NULL, N'content:category:update', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (32, 29, N'删除分类', NULL, NULL, N'content:category:delete', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (33, 23, N'标签管理', N'tag', NULL, N'content:tag:list', 1, N'tags', 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (34, 33, N'新增标签', NULL, NULL, N'content:tag:save', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (35, 33, N'修改标签', NULL, NULL, N'content:tag:update', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (36, 33, N'删除标签', NULL, NULL, N'content:tag:delete', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (37, 0, N'系统监控', N'monitor', NULL, NULL, 0, N'monitor', 4, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (38, 37, N'服务监控', N'server', NULL, N'monitor:server:list', 1, N'fund', 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (39, 37, N'Redis监控', N'redis', NULL, N'monitor:redis:list', 1, N'database', 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (40, 37, N'接口文档', N'http://localhost:8888/doc.html', NULL, NULL, 1, N'file-word', 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (41, 0, N'个人中心', N'profile', NULL, NULL, 0, N'user', 9, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (42, 41, N'修改信息', NULL, NULL, N'sys:profile:update', 1, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (43, 41, N'修改密码', NULL, NULL, N'sys:profile:password', 1, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (44, 23, N'文件管理', N'file', NULL, N'oss:file:list', 1, N'file-image', 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (45, 44, N'上传文件', NULL, NULL, N'oss:file:upload', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (46, 44, N'下载文件', NULL, NULL, N'oss:file:download', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (47, 44, N'获取文件链接', NULL, NULL, N'oss:file:url', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (48, 44, N'删除文件', NULL, NULL, N'oss:file:delete', 2, NULL, 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (49, 1, N'数据字典', N'sys/dict', NULL, N'sys:dict:list', 1, N'book', 5, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (50, 49, N'查询字典', NULL, NULL, N'sys:dict:list', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (51, 49, N'新增字典', NULL, NULL, N'sys:dict:save', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (52, 49, N'修改字典', NULL, NULL, N'sys:dict:update', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (53, 49, N'删除字典', NULL, NULL, N'sys:dict:delete', 2, NULL, 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (54, 1, N'字典项管理', N'sys/dictitem', NULL, N'sys:dict:item:list', 1, N'unordered-list', 6, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (55, 54, N'查询字典项', NULL, NULL, N'sys:dict:item:list', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (56, 54, N'新增字典项', NULL, NULL, N'sys:dict:item:save', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (57, 54, N'修改字典项', NULL, NULL, N'sys:dict:item:update', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (58, 54, N'删除字典项', NULL, NULL, N'sys:dict:item:delete', 2, NULL, 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (59, 2, N'用户信息', NULL, NULL, N'sys:user:info', 2, NULL, 5, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (61, 2, N'登出接口', NULL, NULL, N'sys:user:logout', 2, NULL, 6, 1, CAST(N'2026-01-07T15:06:11.0000000' AS DateTime2), 1, CAST(N'2026-01-07T15:06:11.0000000' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (62, 2, N'用户列表', NULL, NULL, N'sys:user:list', 2, NULL, NULL, 1, CAST(N'2026-01-13T11:50:20.3982879' AS DateTime2), 1, CAST(N'2026-01-13T11:50:20.3982879' AS DateTime2))
SET IDENTITY_INSERT [dbo].[sys_menu] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_role] ON

INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (1, N'超级管理员', N'拥有系统所有权限，最高权限角色', 1, NULL, CAST(N'2025-12-02T11:50:30.0000000' AS DateTime2), NULL, NULL)
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (2, N'系统管理员', N'管理系统基础配置和用户', 1, NULL, CAST(N'2025-12-02T11:50:30.0000000' AS DateTime2), NULL, NULL)
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (3, N'普通用户', N'普通操作员，拥有基本查看权限', 1, NULL, CAST(N'2025-12-02T11:50:30.0000000' AS DateTime2), NULL, NULL)
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (7, N'add user', N'add user', 1, 1, CAST(N'2026-01-05T16:54:36.0000000' AS DateTime2), 1, CAST(N'2026-01-13T11:55:53.8289147' AS DateTime2))
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (8, N'edit user', N'edit user', 1, 1, CAST(N'2026-01-05T17:00:49.0000000' AS DateTime2), 1, CAST(N'2026-01-13T11:55:44.7030094' AS DateTime2))
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (9, N'del user', N'del user', 1, 1, CAST(N'2026-01-05T17:07:12.0000000' AS DateTime2), 1, CAST(N'2026-01-13T11:55:57.2101194' AS DateTime2))
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (10, N'list user', N'list user', 1, 1, CAST(N'2026-01-05T17:07:48.0000000' AS DateTime2), 1, CAST(N'2026-01-13T11:56:02.3048120' AS DateTime2))
SET IDENTITY_INSERT [dbo].[sys_role] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_role_menu] ON

INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (1, 1, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (2, 1, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (3, 1, 3)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (4, 1, 4)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (5, 1, 5)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (6, 1, 6)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (7, 1, 7)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (8, 1, 8)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (9, 1, 9)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (10, 1, 10)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (11, 1, 11)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (12, 1, 12)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (13, 1, 13)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (14, 1, 14)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (15, 1, 15)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (16, 1, 16)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (17, 1, 17)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (18, 1, 18)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (19, 1, 19)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (20, 1, 20)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (22, 1, 22)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (23, 1, 23)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (24, 1, 24)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (25, 1, 25)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (26, 1, 26)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (27, 1, 27)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (28, 1, 28)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (29, 1, 29)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (30, 1, 30)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (31, 1, 31)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (32, 1, 32)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (33, 1, 33)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (34, 1, 34)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (35, 1, 35)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (36, 1, 36)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (37, 1, 37)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (38, 1, 38)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (39, 1, 39)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (40, 1, 40)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (41, 1, 41)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (42, 1, 42)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (43, 1, 43)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (44, 2, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (45, 2, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (46, 2, 3)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (47, 2, 4)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (48, 2, 5)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (49, 2, 6)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (50, 2, 7)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (51, 2, 8)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (52, 2, 9)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (53, 2, 10)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (54, 2, 11)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (55, 2, 12)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (56, 2, 13)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (57, 2, 14)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (59, 2, 22)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (60, 2, 23)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (61, 2, 24)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (62, 2, 29)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (63, 2, 33)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (64, 2, 41)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (65, 2, 42)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (66, 2, 43)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (68, 3, 22)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (69, 3, 23)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (70, 3, 24)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (71, 3, 29)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (72, 3, 33)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (73, 3, 41)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (74, 3, 42)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (75, 3, 43)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (76, 1, 44)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (77, 1, 45)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (78, 1, 46)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (79, 1, 47)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (80, 1, 48)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (81, 1, 49)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (82, 1, 50)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (83, 1, 51)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (84, 1, 52)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (85, 1, 53)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (86, 1, 54)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (87, 1, 55)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (88, 1, 56)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (89, 1, 57)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (90, 1, 58)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (91, 2, 49)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (92, 2, 50)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (93, 2, 51)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (94, 2, 52)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (95, 2, 53)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (96, 2, 54)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (97, 2, 55)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (98, 2, 56)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (99, 2, 57)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (100, 2, 58)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (101, 1, 59)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (102, 2, 59)
GO
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (103, 3, 59)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (104, 3, 11)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (210, 8, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (211, 8, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (212, 8, 4)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (213, 8, 62)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (214, 7, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (215, 7, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (216, 7, 3)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (217, 7, 62)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (218, 9, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (219, 9, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (220, 9, 5)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (221, 9, 62)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (222, 10, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (223, 10, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (224, 10, 62)
SET IDENTITY_INSERT [dbo].[sys_role_menu] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_user] ON

INSERT [dbo].[sys_user] ([user_id], [username], [password], [real_name], [email], [mobile], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (1, N'admin', N'$2a$10$bddSw9Plnp9wvu8/XyNGz.EA.EtpSa/Bc2ag399fXWaHpQ93bwxXa', N'系统管理员', N'admin@example.com', N'13800138000', 1, NULL, CAST(N'2025-12-02T11:50:46.0000000' AS DateTime2), NULL, NULL)
INSERT [dbo].[sys_user] ([user_id], [username], [password], [real_name], [email], [mobile], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (2, N'sysadmin', N'$2a$10$XhTMMAx1jCp/XzpXvNNnzOtI5l44E/XIY98fDZy7Os2Q/VPBZDGLe', N'张三', N'zhangsan@example.com', N'13800138001', 1, NULL, CAST(N'2025-12-02T11:50:46.0000000' AS DateTime2), NULL, NULL)
INSERT [dbo].[sys_user] ([user_id], [username], [password], [real_name], [email], [mobile], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (3, N'user', N'$2a$10$mrOhEfIDpwpq/i0j1PQeYexHysPVN8DCuG6vNo8rIHZ4C6w3aKQo6', N'李四', N'lisi@example.com', N'13800138002', 1, NULL, CAST(N'2025-12-02T11:50:46.0000000' AS DateTime2), NULL, NULL)
SET IDENTITY_INSERT [dbo].[sys_user] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_user_role] ON

INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (1, 1, 1)
INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (2, 2, 2)
INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (3, 3, 3)
INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (4, 1, 1)
INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (5, 2, 2)
INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (6, 3, 3)
INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (7, 4, 2)
INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (8, 4, 3)
SET IDENTITY_INSERT [dbo].[sys_user_role] OFF
GO
ALTER TABLE [dbo].[file_info] ADD  DEFAULT ((1)) FOR [status]
GO
ALTER TABLE [dbo].[file_info] ADD  DEFAULT (getdate()) FOR [create_time]
GO
ALTER TABLE [dbo].[file_info] ADD  DEFAULT (getdate()) FOR [update_time]
GO
ALTER TABLE [dbo].[sys_audit_log] ADD  DEFAULT (getdate()) FOR [create_time]
GO
ALTER TABLE [dbo].[sys_dict] ADD  DEFAULT ((1)) FOR [status]
GO
ALTER TABLE [dbo].[sys_dict] ADD  DEFAULT (getdate()) FOR [create_time]
GO
ALTER TABLE [dbo].[sys_dict] ADD  DEFAULT (getdate()) FOR [update_time]
GO
ALTER TABLE [dbo].[sys_dict_item] ADD  DEFAULT ((1)) FOR [status]
GO
ALTER TABLE [dbo].[sys_dict_item] ADD  DEFAULT ((0)) FOR [order_num]
GO
ALTER TABLE [dbo].[sys_dict_item] ADD  DEFAULT (getdate()) FOR [create_time]
GO
ALTER TABLE [dbo].[sys_dict_item] ADD  DEFAULT (getdate()) FOR [update_time]
GO
ALTER TABLE [dbo].[sys_role] ADD  DEFAULT ((1)) FOR [status]
GO
ALTER TABLE [dbo].[sys_role] ADD  DEFAULT (getdate()) FOR [create_time]
GO
ALTER TABLE [dbo].[sys_user] ADD  DEFAULT ((1)) FOR [status]
GO
ALTER TABLE [dbo].[sys_user] ADD  DEFAULT (getdate()) FOR [create_time]
GO
