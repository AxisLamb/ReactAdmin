-- 检查并创建数据库 lain_db
-- 需要在SSMS执行，在DBeaber执行会失败
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'lain_db')
BEGIN
    CREATE DATABASE lain_db;
END
GO

USE [lain_db]
GO
/****** Object:  Table [dbo].[file_info]    Script Date: 2026/8/20 18:18:46 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[file_info](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[file_id] [nvarchar](64) NOT NULL,
	[user_id] [bigint] NULL,
	[original_name] [nvarchar](255) NOT NULL,
	[file_size] [bigint] NOT NULL,
	[file_type] [nvarchar](100) NULL,
	[bucket_name] [nvarchar](100) NOT NULL,
	[object_name] [nvarchar](500) NOT NULL,
	[file_path] [nvarchar](1000) NULL,
	[service_module] [nvarchar](100) NULL,
	[business_type] [nvarchar](100) NULL,
	[business_table] [nvarchar](100) NULL,
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
/****** Object:  Table [dbo].[sys_audit_log]    Script Date: 2026/8/20 18:18:47 ******/
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
/****** Object:  Table [dbo].[sys_dict]    Script Date: 2026/8/20 18:18:47 ******/
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
/****** Object:  Table [dbo].[sys_dict_item]    Script Date: 2026/8/20 18:18:47 ******/
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
/****** Object:  Table [dbo].[sys_menu]    Script Date: 2026/8/20 18:18:47 ******/
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
/****** Object:  Table [dbo].[sys_role]    Script Date: 2026/8/20 18:18:47 ******/
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
/****** Object:  Table [dbo].[sys_role_menu]    Script Date: 2026/8/20 18:18:47 ******/
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
/****** Object:  Table [dbo].[sys_user]    Script Date: 2026/8/20 18:18:47 ******/
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
/****** Object:  Table [dbo].[sys_user_role]    Script Date: 2026/8/20 18:18:47 ******/
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
SET IDENTITY_INSERT [dbo].[file_info] ON

INSERT [dbo].[file_info] ([id], [file_id], [user_id], [original_name], [file_size], [file_type], [bucket_name], [object_name], [file_path], [service_module], [business_type], [business_table], [business_id], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (3, N'bcde8d3790c447c5b822ef82495971d9', 1, N'5e0a1c137d28af7fff028bc0fff4415b.jpg', 584104, N'image/jpeg', N'lain-sys', N'loginpage/202608/5d0b6d4650c449a380e8f7a148a42116_1.jpg', N'http://localhost:8888/file/lain-sys/loginpage/202608/5d0b6d4650c449a380e8f7a148a42116_1.jpg?satoken=7ed9ec5f-42b0-4cd1-ac76-d4073f2066ca', N'sys', N'loginPage', N'file_info', N'1', 1, 1, CAST(N'2026-08-20T18:07:31.6216246' AS DateTime2), 1, CAST(N'2026-08-20T18:07:31.6216246' AS DateTime2))
INSERT [dbo].[file_info] ([id], [file_id], [user_id], [original_name], [file_size], [file_type], [bucket_name], [object_name], [file_path], [service_module], [business_type], [business_table], [business_id], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (4, N'9382396d31ed40038b5166142f6ca6b9', 1, N'1000g008295m415sfo05g5nsmtssg8ae34dncaj8.webp', 261358, N'image/webp', N'lain-sys', N'loginpage/202608/a36307fac93c4f259b7ff72744c053f3_1.webp', N'http://localhost:8888/file/lain-sys/loginpage/202608/a36307fac93c4f259b7ff72744c053f3_1.webp?satoken=7ed9ec5f-42b0-4cd1-ac76-d4073f2066ca', N'sys', N'loginPage', N'file_info', N'1', 1, 1, CAST(N'2026-08-20T18:07:31.6149531' AS DateTime2), 1, CAST(N'2026-08-20T18:07:31.6149531' AS DateTime2))
SET IDENTITY_INSERT [dbo].[file_info] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_dict] ON

INSERT [dbo].[sys_dict] ([dict_id], [dict_name], [dict_type], [status], [remark], [created_by], [create_time], [updated_by], [update_time]) VALUES (1, N'image', N'image', 1, N'图片定义', 1, CAST(N'2026-08-18T11:42:48.5633333' AS DateTime2), 1, CAST(N'2026-08-18T11:42:48.5633333' AS DateTime2))
SET IDENTITY_INSERT [dbo].[sys_dict] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_dict_item] ON

INSERT [dbo].[sys_dict_item] ([item_id], [dict_id], [item_label], [item_value], [status], [order_num], [remark], [created_by], [create_time], [updated_by], [update_time]) VALUES (1, 1, N'avatar', N'user', 1, 0, N'module', 1, CAST(N'2026-08-18T11:42:48.6200000' AS DateTime2), 1, CAST(N'2026-08-18T11:42:48.6200000' AS DateTime2))
INSERT [dbo].[sys_dict_item] ([item_id], [dict_id], [item_label], [item_value], [status], [order_num], [remark], [created_by], [create_time], [updated_by], [update_time]) VALUES (2, 1, N'avatar', N'sys_user', 1, 1, N'业务表', 1, CAST(N'2026-08-18T11:42:48.6200000' AS DateTime2), 1, CAST(N'2026-08-18T11:42:48.6200000' AS DateTime2))
INSERT [dbo].[sys_dict_item] ([item_id], [dict_id], [item_label], [item_value], [status], [order_num], [remark], [created_by], [create_time], [updated_by], [update_time]) VALUES (3, 1, N'loginPage', N'sys', 1, 2, N'module', 1, CAST(N'2026-08-18T11:42:48.6200000' AS DateTime2), 1, CAST(N'2026-08-18T11:42:48.6200000' AS DateTime2))
INSERT [dbo].[sys_dict_item] ([item_id], [dict_id], [item_label], [item_value], [status], [order_num], [remark], [created_by], [create_time], [updated_by], [update_time]) VALUES (4, 1, N'loginPage', N'file_info', 1, 3, N'业务表', 1, CAST(N'2026-08-18T11:42:48.6200000' AS DateTime2), 1, CAST(N'2026-08-18T11:42:48.6200000' AS DateTime2))
SET IDENTITY_INSERT [dbo].[sys_dict_item] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_menu] ON

INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (1, 0, N'系统管理', N'sys', NULL, NULL, 0, N'setting', 1, NULL, NULL, 1, CAST(N'2026-08-18T16:08:18.5167017' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (2, 1, N'用户管理', N'sys/user', N'UserList', N'sys:user:list', 1, N'user', 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (3, 2, N'新增用户', NULL, NULL, N'sys:user:save', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (4, 2, N'修改用户', NULL, NULL, N'sys:user:update', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (5, 2, N'删除用户', NULL, NULL, N'sys:user:delete', 2, NULL, 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (6, 2, N'重置密码', NULL, NULL, N'sys:user:reset', 2, NULL, 4, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (7, 1, N'角色管理', N'sys/role', N'RoleList', N'sys:role:list', 1, N'team', 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (8, 7, N'新增角色', NULL, NULL, N'sys:role:save', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (9, 7, N'修改角色', NULL, NULL, N'sys:role:update', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (10, 7, N'删除角色', NULL, NULL, N'sys:role:delete', 2, NULL, 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (11, 1, N'菜单管理', N'sys/menu', N'MenuList', N'sys:menu:list', 1, N'menu', 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (12, 11, N'新增菜单', NULL, NULL, N'sys:menu:save', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (13, 11, N'修改菜单', NULL, NULL, N'sys:menu:update', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (14, 11, N'删除菜单', NULL, NULL, N'sys:menu:delete', 2, NULL, 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (19, 1, N'系统日志', N'sys/log', NULL, N'sys:log:list', 1, N'file-text', 4, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (20, 19, N'删除日志', NULL, NULL, N'sys:log:delete', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (22, 64, N'首页统计', N'', N'', NULL, 4, N'bar-chart', 1, NULL, NULL, 1, CAST(N'2026-08-18T16:13:13.7973999' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (37, 0, N'系统监控', N'monitor', NULL, NULL, 0, N'dashboard', 2, NULL, NULL, 1, CAST(N'2026-08-18T16:08:11.6361942' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (38, 37, N'服务监控', N'/monitor', NULL, N'outer', 1, N'safety', 1, NULL, NULL, 1, CAST(N'2026-08-20T17:53:11.4985823' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (40, 37, N'接口文档', N'/doc.html', NULL, N'outer', 1, N'database', 2, NULL, NULL, 1, CAST(N'2026-08-18T14:42:19.6748396' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (41, 64, N'个人中心', N'', N'', NULL, 4, N'user', 2, NULL, NULL, 1, CAST(N'2026-08-18T17:02:43.0560909' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (42, 41, N'修改信息', NULL, NULL, N'sys:profile:update', 5, NULL, 0, NULL, NULL, 1, CAST(N'2026-08-18T14:45:11.4025301' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (43, 41, N'修改密码', NULL, NULL, N'sys:profile:password', 5, NULL, 1, NULL, NULL, 1, CAST(N'2026-08-18T14:45:18.7236975' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (44, 1, N'文件管理', N'sys/file', N'FileList', N'oss:file:list', 1, N'folder', 3, NULL, NULL, 1, CAST(N'2026-08-18T14:40:40.4931818' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (46, 44, N'下载文件', NULL, NULL, N'oss:file:download', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (47, 44, N'获取文件链接', NULL, NULL, N'oss:file:url', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (48, 44, N'删除文件', NULL, NULL, N'oss:file:delete', 2, NULL, 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (49, 1, N'数据字典', N'sys/dict', N'DictList', N'sys:dict:list', 1, N'book', 5, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (50, 49, N'查询字典', NULL, NULL, N'sys:dict:list', 2, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (51, 49, N'新增字典', NULL, NULL, N'sys:dict:save', 2, NULL, 1, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (52, 49, N'修改字典', NULL, NULL, N'sys:dict:update', 2, NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (53, 49, N'删除字典', NULL, NULL, N'sys:dict:delete', 2, NULL, 3, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (55, 49, N'查询字典项', NULL, NULL, N'sys:dict:item:list', 2, NULL, 4, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (56, 49, N'新增字典项', NULL, NULL, N'sys:dict:item:save', 2, NULL, 5, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (57, 49, N'修改字典项', NULL, NULL, N'sys:dict:item:update', 2, NULL, 6, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (58, 49, N'删除字典项', NULL, NULL, N'sys:dict:item:delete', 2, NULL, 7, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (59, 2, N'用户信息', NULL, NULL, N'sys:user:info', 2, NULL, 5, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (63, 22, N'数据统计', NULL, NULL, N'sys:dashboard:list', 5, NULL, 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (64, 0, N'通用接口', NULL, NULL, NULL, 3, N'star', 3, 1, CAST(N'2026-08-18T14:43:23.7727826' AS DateTime2), 1, CAST(N'2026-08-18T16:13:03.7224170' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (65, 64, N'文件', NULL, NULL, NULL, 4, N'folder', 3, 1, CAST(N'2026-08-18T17:02:16.2096924' AS DateTime2), 1, CAST(N'2026-08-18T17:02:23.0298749' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (66, 65, N'文件上传', NULL, NULL, N'oss:file:upload', 5, NULL, 1, 1, CAST(N'2026-08-18T17:04:10.2748154' AS DateTime2), 1, CAST(N'2026-08-18T17:04:10.2748154' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (67, 65, N'文件链接', NULL, NULL, N'oss:file:url', 5, NULL, 2, 1, CAST(N'2026-08-18T17:04:54.1635794' AS DateTime2), 1, CAST(N'2026-08-18T17:04:59.7311582' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (68, 41, N'上传头像', NULL, NULL, N'sys:user:upload', 5, NULL, 3, 1, CAST(N'2026-08-18T18:23:39.4193711' AS DateTime2), 1, CAST(N'2026-08-18T18:23:39.4193711' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (69, 1, N'系统配置', N'sys/config', N'SysConfig', NULL, 1, N'setting', 10, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (70, 69, N'上传图片', NULL, NULL, N'sys:config:loginPageUpload', 2, NULL, 7, NULL, NULL, NULL, NULL)
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (71, 37, N'数据库监控', N'/druid', NULL, N'outer', 1, N'database', 3, 1, CAST(N'2026-08-20T18:00:37.5650702' AS DateTime2), 1, CAST(N'2026-08-20T18:00:37.5650702' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (72, 2, N'查询用户', NULL, NULL, N'sys:user:list', 2, NULL, 0, 1, CAST(N'2026-08-20T18:02:17.5748082' AS DateTime2), 1, CAST(N'2026-08-20T18:02:17.5748082' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (73, 7, N'查询角色', NULL, NULL, N'sys:role:list', 2, NULL, 0, 1, CAST(N'2026-08-20T18:02:31.1314259' AS DateTime2), 1, CAST(N'2026-08-20T18:02:31.1314259' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (74, 11, N'查询菜单', NULL, NULL, N'sys:menu:list', 2, NULL, 0, 1, CAST(N'2026-08-20T18:02:44.1300893' AS DateTime2), 1, CAST(N'2026-08-20T18:02:44.1300893' AS DateTime2))
INSERT [dbo].[sys_menu] ([menu_id], [parent_id], [name], [url], [react_component], [perms], [type], [icon], [order_num], [created_by], [create_time], [updated_by], [update_time]) VALUES (75, 44, N'查看文件', NULL, NULL, N'oss:file:list', 2, NULL, 0, 1, CAST(N'2026-08-20T18:03:07.5194635' AS DateTime2), 1, CAST(N'2026-08-20T18:03:07.5194635' AS DateTime2))
SET IDENTITY_INSERT [dbo].[sys_menu] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_role] ON

INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (1, N'超级管理员', N'拥有系统所有权限，最高权限角色', 1, NULL, CAST(N'2025-12-02T11:50:30.0000000' AS DateTime2), 1, CAST(N'2026-08-20T18:05:40.1367807' AS DateTime2))
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (2, N'系统管理员', N'管理系统基础配置和用户', 1, NULL, CAST(N'2025-12-02T11:50:30.0000000' AS DateTime2), 1, CAST(N'2026-08-18T17:23:34.3858641' AS DateTime2))
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (3, N'普通用户', N'普通操作员，拥有基本查看权限', 1, NULL, CAST(N'2025-12-02T11:50:30.0000000' AS DateTime2), 1, CAST(N'2026-08-20T18:04:23.7515596' AS DateTime2))
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (7, N'add user', N'add user', 1, 1, CAST(N'2026-01-05T16:54:36.0000000' AS DateTime2), 1, CAST(N'2026-08-18T17:23:45.5575616' AS DateTime2))
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (8, N'edit user', N'edit user', 1, 1, CAST(N'2026-01-05T17:00:49.0000000' AS DateTime2), 1, CAST(N'2026-01-13T11:55:44.7030094' AS DateTime2))
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (9, N'del user', N'del user', 1, 1, CAST(N'2026-01-05T17:07:12.0000000' AS DateTime2), 1, CAST(N'2026-01-13T11:55:57.2101194' AS DateTime2))
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (10, N'list user', N'list user', 1, 1, CAST(N'2026-01-05T17:07:48.0000000' AS DateTime2), 1, CAST(N'2026-08-20T18:06:38.3583818' AS DateTime2))
INSERT [dbo].[sys_role] ([role_id], [role_name], [role_desc], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (11, N'接口测试员', N'接口测试员', 1, 1, CAST(N'2026-08-18T16:49:05.4963419' AS DateTime2), 1, CAST(N'2026-08-20T18:06:55.1869390' AS DateTime2))
SET IDENTITY_INSERT [dbo].[sys_role] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_role_menu] ON

INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (210, 8, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (211, 8, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (212, 8, 4)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (218, 9, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (219, 9, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (220, 9, 5)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (403, 2, 64)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (404, 2, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (405, 2, 65)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (406, 2, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (407, 2, 66)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (408, 2, 3)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (409, 2, 67)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (410, 2, 4)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (411, 2, 5)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (412, 2, 6)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (413, 2, 7)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (414, 2, 8)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (415, 2, 9)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (416, 2, 10)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (417, 2, 11)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (418, 2, 12)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (419, 2, 13)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (420, 2, 14)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (421, 2, 41)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (422, 2, 42)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (423, 2, 43)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (424, 2, 44)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (425, 2, 46)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (426, 2, 47)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (427, 2, 48)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (428, 2, 49)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (429, 2, 50)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (430, 2, 51)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (431, 2, 52)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (432, 2, 53)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (433, 2, 55)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (434, 2, 56)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (435, 2, 57)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (436, 2, 58)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (437, 2, 59)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (438, 7, 64)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (439, 7, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (440, 7, 65)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (441, 7, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (442, 7, 3)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (443, 7, 67)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (563, 3, 64)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (564, 3, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (565, 3, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (566, 3, 7)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (567, 3, 72)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (568, 3, 73)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (569, 3, 41)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (570, 3, 74)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (571, 3, 11)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (572, 3, 75)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (573, 3, 43)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (574, 3, 44)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (575, 3, 49)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (576, 3, 50)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (577, 3, 55)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (578, 3, 59)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (579, 1, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (580, 1, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (581, 1, 3)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (582, 1, 4)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (583, 1, 5)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (584, 1, 6)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (585, 1, 7)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (586, 1, 8)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (587, 1, 9)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (588, 1, 10)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (589, 1, 11)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (590, 1, 12)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (591, 1, 13)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (592, 1, 14)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (593, 1, 19)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (594, 1, 20)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (595, 1, 22)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (596, 1, 37)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (597, 1, 38)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (598, 1, 40)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (599, 1, 41)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (600, 1, 42)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (601, 1, 43)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (602, 1, 44)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (603, 1, 46)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (604, 1, 47)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (605, 1, 48)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (606, 1, 49)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (607, 1, 50)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (608, 1, 51)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (609, 1, 52)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (610, 1, 53)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (611, 1, 55)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (612, 1, 56)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (613, 1, 57)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (614, 1, 58)
GO
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (615, 1, 59)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (616, 1, 63)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (617, 1, 64)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (618, 1, 65)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (619, 1, 66)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (620, 1, 67)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (621, 1, 68)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (622, 1, 69)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (623, 1, 70)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (624, 1, 71)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (625, 1, 72)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (626, 1, 73)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (627, 1, 74)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (628, 1, 75)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (629, 10, 64)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (630, 10, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (631, 10, 2)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (632, 10, 72)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (633, 10, 41)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (634, 10, 43)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (635, 11, 64)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (636, 11, 1)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (637, 11, 65)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (638, 11, 66)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (639, 11, 67)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (640, 11, 68)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (641, 11, 41)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (642, 11, 42)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (643, 11, 43)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (644, 11, 49)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (645, 11, 50)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (646, 11, 22)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (647, 11, 55)
INSERT [dbo].[sys_role_menu] ([id], [role_id], [menu_id]) VALUES (648, 11, 63)
SET IDENTITY_INSERT [dbo].[sys_role_menu] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_user] ON

INSERT [dbo].[sys_user] ([user_id], [username], [password], [real_name], [email], [mobile], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (1, N'admin', N'$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', N'超级管理员', N'admin@example.com', N'13800138000', 1, NULL, CAST(N'2025-12-02T11:50:46.0000000' AS DateTime2), 1, CAST(N'2026-08-18T17:20:09.2482751' AS DateTime2))
INSERT [dbo].[sys_user] ([user_id], [username], [password], [real_name], [email], [mobile], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (2, N'sysadmin', N'$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', N'系统管理员', N'zhangsan@example.com', N'13800138001', 1, NULL, CAST(N'2025-12-02T11:50:46.0000000' AS DateTime2), 1, CAST(N'2026-08-18T17:20:18.7628490' AS DateTime2))
INSERT [dbo].[sys_user] ([user_id], [username], [password], [real_name], [email], [mobile], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (3, N'user', N'$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', N'张三', N'lisi@example.com', N'13800138002', 1, NULL, CAST(N'2025-12-02T11:50:46.0000000' AS DateTime2), 1, CAST(N'2026-08-18T17:20:28.7832086' AS DateTime2))
INSERT [dbo].[sys_user] ([user_id], [username], [password], [real_name], [email], [mobile], [status], [created_by], [create_time], [updated_by], [update_time]) VALUES (5, N'laoqian', N'$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6', N'老千', NULL, NULL, 1, 1, CAST(N'2026-08-18T18:25:21.5762095' AS DateTime2), 1, CAST(N'2026-08-18T18:25:21.5762095' AS DateTime2))
SET IDENTITY_INSERT [dbo].[sys_user] OFF
GO
SET IDENTITY_INSERT [dbo].[sys_user_role] ON

INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (11, 1, 1)
INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (12, 2, 2)
INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (13, 3, 3)
INSERT [dbo].[sys_user_role] ([id], [user_id], [role_id]) VALUES (14, 5, 11)
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
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'类型0, 1, 2都是会展示在系统页面的元素
0 一级菜单 1 页面 2 页面上的按钮

类型3, 4, 5 表示不会展示在系统页面，表示接口以及其所属业务分类
3 接口目录 4 接口业务 5 具体接口

都可以分配给角色' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'sys_menu', @level2type=N'COLUMN',@level2name=N'type'
GO
