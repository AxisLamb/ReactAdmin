/*
SQLyog Community v13.3.0 (64 bit)
MySQL - 8.3.0 : Database - lain_db
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`lain_db` /*!40100 DEFAULT CHARACTER SET utf8mb3 */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `lain_db`;

/*Table structure for table `file_info` */

DROP TABLE IF EXISTS `file_info`;

CREATE TABLE `file_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件唯一标识',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件类型',
  `bucket_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储桶名称',
  `object_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对象名称',
  `file_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件路径',
  `service_module` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务模块',
  `business_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务类型',
  `business_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用, 1-正常',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_id` (`file_id`),
  KEY `idx_business` (`business_type`,`business_id`),
  KEY `idx_service_module` (`service_module`),
  KEY `idx_created_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

/*Data for the table `file_info` */

/*Table structure for table `sys_audit_log` */

DROP TABLE IF EXISTS `sys_audit_log`;

CREATE TABLE `sys_audit_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `operation` varchar(50) DEFAULT NULL COMMENT '用户操作',
  `method` varchar(200) DEFAULT NULL COMMENT '请求方法',
  `params` text COMMENT '请求参数',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '执行结果',
  `ip` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `time` bigint DEFAULT NULL COMMENT '执行时长(毫秒)',
  `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统审计日志';

/*Data for the table `sys_audit_log` */

insert  into `sys_audit_log`(`log_id`,`user_id`,`username`,`operation`,`method`,`params`,`result`,`ip`,`user_agent`,`time`,`created_by`,`create_time`,`updated_by`,`update_time`) values
(97,1,NULL,'新增用户','com.lain.modules.sys.controller.SysUserController.save()','SysUserVO(userId=null, username=telsa, roleId=8, roleName=null, password=123456, realName=telsa, email=null, mobile=null, status=1) ','R(code=0, msg=success, data=保存成功)','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0',79,1,'2026-01-12 22:17:58',1,'2026-01-12 22:17:58');

/*Table structure for table `sys_dict` */

DROP TABLE IF EXISTS `sys_dict`;

CREATE TABLE `sys_dict` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典ID',
  `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
  `dict_type` varchar(100) NOT NULL COMMENT '字典类型',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态（0禁用 1正常）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据字典表';

/*Data for the table `sys_dict` */

/*Table structure for table `sys_dict_item` */

DROP TABLE IF EXISTS `sys_dict_item`;

CREATE TABLE `sys_dict_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典项ID',
  `dict_id` bigint NOT NULL COMMENT '字典ID',
  `item_label` varchar(100) NOT NULL COMMENT '字典项标签',
  `item_value` varchar(100) NOT NULL COMMENT '字典项值',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态（0禁用 1正常）',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`item_id`),
  KEY `idx_dict_id` (`dict_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据字典项表';

/*Data for the table `sys_dict_item` */

/*Table structure for table `sys_menu` */

DROP TABLE IF EXISTS `sys_menu`;

CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父菜单ID，一级菜单为0',
  `name` varchar(50) NOT NULL COMMENT '菜单名称',
  `url` varchar(200) DEFAULT NULL COMMENT '菜单URL',
  `react_component` varchar(50) DEFAULT NULL COMMENT '菜单对应的React组件',
  `perms` varchar(500) DEFAULT NULL COMMENT '授权(多个用逗号分隔，如：user:list,user:create)',
  `type` int DEFAULT NULL COMMENT '类型   0：目录   1：菜单   2：按钮',
  `icon` varchar(50) DEFAULT NULL COMMENT '菜单图标',
  `order_num` int DEFAULT NULL COMMENT '排序',
  `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单管理';

/*Data for the table `sys_menu` */

insert  into `sys_menu`(`menu_id`,`parent_id`,`name`,`url`,`react_component`,`perms`,`type`,`icon`,`order_num`,`created_by`,`create_time`,`updated_by`,`update_time`) values
(1,0,'系统管理','sys',NULL,NULL,0,'setting',0,NULL,NULL,NULL,NULL),
(2,1,'用户管理','sys/user','UserList','sys:user:list',1,'user',0,NULL,NULL,NULL,NULL),
(3,2,'新增用户',NULL,NULL,'sys:user:save',2,NULL,0,NULL,NULL,NULL,NULL),
(4,2,'修改用户',NULL,NULL,'sys:user:update',2,NULL,1,NULL,NULL,NULL,NULL),
(5,2,'删除用户',NULL,NULL,'sys:user:delete',2,NULL,2,NULL,NULL,NULL,NULL),
(6,2,'重置密码',NULL,NULL,'sys:user:reset',2,NULL,3,NULL,NULL,NULL,NULL),
(7,1,'角色管理','sys/role','RoleList','sys:role:list',1,'team',1,NULL,NULL,NULL,NULL),
(8,7,'新增角色',NULL,NULL,'sys:role:save',2,NULL,0,NULL,NULL,NULL,NULL),
(9,7,'修改角色',NULL,NULL,'sys:role:update',2,NULL,1,NULL,NULL,NULL,NULL),
(10,7,'删除角色',NULL,NULL,'sys:role:delete',2,NULL,2,NULL,NULL,NULL,NULL),
(11,1,'菜单管理','sys/menu','MenuList','sys:menu:list',1,'menu',2,NULL,NULL,NULL,NULL),
(12,11,'新增菜单',NULL,NULL,'sys:menu:save',2,NULL,0,NULL,NULL,NULL,NULL),
(13,11,'修改菜单',NULL,NULL,'sys:menu:update',2,NULL,1,NULL,NULL,NULL,NULL),
(14,11,'删除菜单',NULL,NULL,'sys:menu:delete',2,NULL,2,NULL,NULL,NULL,NULL),
(19,1,'系统日志','sys/log',NULL,'sys:log:list',1,'file-text',4,NULL,NULL,NULL,NULL),
(20,19,'删除日志',NULL,NULL,'sys:log:delete',2,NULL,0,NULL,NULL,NULL,NULL),
(21,0,'仪表盘','dashboard','Dashboard',NULL,0,'dashboard',1,NULL,NULL,NULL,NULL),
(22,0,'工作台','workbench',NULL,NULL,0,'desktop',2,NULL,NULL,NULL,NULL),
(37,0,'系统监控','monitor',NULL,NULL,0,'monitor',4,NULL,NULL,NULL,NULL),
(38,37,'服务监控','server',NULL,'monitor:server:list',1,'fund',0,NULL,NULL,NULL,NULL),
(39,37,'Redis监控','redis',NULL,'monitor:redis:list',1,'database',1,NULL,NULL,NULL,NULL),
(40,37,'接口文档','http://localhost:8888/doc.html',NULL,NULL,1,'file-word',2,NULL,NULL,NULL,NULL),
(41,0,'个人中心','profile',NULL,NULL,0,'user',9,NULL,NULL,NULL,NULL),
(42,41,'修改信息',NULL,NULL,'sys:profile:update',1,NULL,0,NULL,NULL,NULL,NULL),
(43,41,'修改密码',NULL,NULL,'sys:profile:password',1,NULL,1,NULL,NULL,NULL,NULL),
(44,23,'文件管理','file',NULL,'oss:file:list',1,'file-image',3,NULL,NULL,NULL,NULL),
(45,44,'上传文件',NULL,NULL,'oss:file:upload',2,NULL,0,NULL,NULL,NULL,NULL),
(46,44,'下载文件',NULL,NULL,'oss:file:download',2,NULL,1,NULL,NULL,NULL,NULL),
(47,44,'获取文件链接',NULL,NULL,'oss:file:url',2,NULL,2,NULL,NULL,NULL,NULL),
(48,44,'删除文件',NULL,NULL,'oss:file:delete',2,NULL,3,NULL,NULL,NULL,NULL),
(49,1,'数据字典','sys/dict',NULL,'sys:dict:list',1,'book',5,NULL,NULL,NULL,NULL),
(50,49,'查询字典',NULL,NULL,'sys:dict:list',2,NULL,0,NULL,NULL,NULL,NULL),
(51,49,'新增字典',NULL,NULL,'sys:dict:save',2,NULL,1,NULL,NULL,NULL,NULL),
(52,49,'修改字典',NULL,NULL,'sys:dict:update',2,NULL,2,NULL,NULL,NULL,NULL),
(53,49,'删除字典',NULL,NULL,'sys:dict:delete',2,NULL,3,NULL,NULL,NULL,NULL),
(54,1,'字典项管理','sys/dictitem',NULL,'sys:dict:item:list',1,'unordered-list',6,NULL,NULL,NULL,NULL),
(55,54,'查询字典项',NULL,NULL,'sys:dict:item:list',2,NULL,0,NULL,NULL,NULL,NULL),
(56,54,'新增字典项',NULL,NULL,'sys:dict:item:save',2,NULL,1,NULL,NULL,NULL,NULL),
(57,54,'修改字典项',NULL,NULL,'sys:dict:item:update',2,NULL,2,NULL,NULL,NULL,NULL),
(58,54,'删除字典项',NULL,NULL,'sys:dict:item:delete',2,NULL,3,NULL,NULL,NULL,NULL),
(59,2,'用户信息',NULL,NULL,'sys:user:info',2,NULL,5,NULL,NULL,NULL,NULL),
(61,2,'登出接口',NULL,NULL,'sys:user:logout',2,NULL,6,1,'2026-01-07 15:06:11',1,'2026-01-07 15:06:11');

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_desc` varchar(100) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态  0：禁用   1：正常',
  `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色';

/*Data for the table `sys_role` */

insert  into `sys_role`(`role_id`,`role_name`,`role_desc`,`status`,`created_by`,`create_time`,`updated_by`,`update_time`) values
(1,'超级管理员','拥有系统所有权限，最高权限角色',1,NULL,'2025-12-02 11:50:30',NULL,NULL),
(2,'系统管理员','管理系统基础配置和用户',1,NULL,'2025-12-02 11:50:30',NULL,NULL),
(3,'普通用户','普通操作员，拥有基本查看权限',1,NULL,'2025-12-02 11:50:30',NULL,NULL),
(7,'add user','add user',1,1,'2026-01-05 16:54:36',1,'2026-01-12 22:17:28'),
(8,'edit user','edit user',1,1,'2026-01-05 17:00:49',1,'2026-01-12 22:17:32'),
(9,'del user','del user',1,1,'2026-01-05 17:07:12',1,'2026-01-12 22:17:35');

/*Table structure for table `sys_role_menu` */

DROP TABLE IF EXISTS `sys_role_menu`;

CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=246 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色与菜单对应关系';

/*Data for the table `sys_role_menu` */

insert  into `sys_role_menu`(`id`,`role_id`,`menu_id`) values
(1,1,1),
(2,1,2),
(3,1,3),
(4,1,4),
(5,1,5),
(6,1,6),
(7,1,7),
(8,1,8),
(9,1,9),
(10,1,10),
(11,1,11),
(12,1,12),
(13,1,13),
(14,1,14),
(15,1,15),
(16,1,16),
(17,1,17),
(18,1,18),
(19,1,19),
(20,1,20),
(21,1,21),
(22,1,22),
(23,1,23),
(24,1,24),
(25,1,25),
(26,1,26),
(27,1,27),
(28,1,28),
(29,1,29),
(30,1,30),
(31,1,31),
(32,1,32),
(33,1,33),
(34,1,34),
(35,1,35),
(36,1,36),
(37,1,37),
(38,1,38),
(39,1,39),
(40,1,40),
(41,1,41),
(42,1,42),
(43,1,43),
(44,2,1),
(45,2,2),
(46,2,3),
(47,2,4),
(48,2,5),
(49,2,6),
(50,2,7),
(51,2,8),
(52,2,9),
(53,2,10),
(54,2,11),
(55,2,12),
(56,2,13),
(57,2,14),
(58,2,21),
(59,2,22),
(60,2,23),
(61,2,24),
(62,2,29),
(63,2,33),
(64,2,41),
(65,2,42),
(66,2,43),
(67,3,21),
(68,3,22),
(69,3,23),
(70,3,24),
(71,3,29),
(72,3,33),
(73,3,41),
(74,3,42),
(75,3,43),
(76,1,44),
(77,1,45),
(78,1,46),
(79,1,47),
(80,1,48),
(81,1,49),
(82,1,50),
(83,1,51),
(84,1,52),
(85,1,53),
(86,1,54),
(87,1,55),
(88,1,56),
(89,1,57),
(90,1,58),
(91,2,49),
(92,2,50),
(93,2,51),
(94,2,52),
(95,2,53),
(96,2,54),
(97,2,55),
(98,2,56),
(99,2,57),
(100,2,58),
(101,1,59),
(102,2,59),
(103,3,59),
(104,3,11),
(234,7,1),
(235,7,2),
(236,7,3),
(237,7,21),
(238,8,1),
(239,8,2),
(240,8,4),
(241,8,21),
(242,9,1),
(243,9,2),
(244,9,5),
(245,9,21);

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态  0：禁用   1：正常',
  `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户';

/*Data for the table `sys_user` */

insert  into `sys_user`(`user_id`,`username`,`password`,`real_name`,`email`,`mobile`,`status`,`created_by`,`create_time`,`updated_by`,`update_time`) values
(1,'admin','$2a$10$bddSw9Plnp9wvu8/XyNGz.EA.EtpSa/Bc2ag399fXWaHpQ93bwxXa','系统管理员','admin@example.com','13800138000',1,NULL,'2025-12-02 11:50:46',NULL,NULL),
(2,'sysadmin','$2a$10$XhTMMAx1jCp/XzpXvNNnzOtI5l44E/XIY98fDZy7Os2Q/VPBZDGLe','张三','zhangsan@example.com','13800138001',1,NULL,'2025-12-02 11:50:46',NULL,NULL),
(3,'user','$2a$10$mrOhEfIDpwpq/i0j1PQeYexHysPVN8DCuG6vNo8rIHZ4C6w3aKQo6','李四','lisi@example.com','13800138002',1,NULL,'2025-12-02 11:50:46',NULL,NULL);

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与角色对应关系';

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`id`,`user_id`,`role_id`) values
(1,1,1),
(2,2,2),
(3,3,3),
(4,1,1),
(5,2,2),
(6,3,3),
(7,4,2),
(8,4,3);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
