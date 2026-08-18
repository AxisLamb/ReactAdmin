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
  `user_id` bigint DEFAULT NULL COMMENT '上传用户ID',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件类型',
  `bucket_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储桶名称',
  `object_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对象名称',
  `file_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件路径',
  `service_module` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务模块',
  `business_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务类型',
  `business_table` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务表名',
  `business_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用, 1-正常',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_id` (`file_id`),
  KEY `idx_user_id` (`user_id`),
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据字典表';

/*Data for the table `sys_dict` */

insert  into `sys_dict`(`dict_name`,`dict_type`,`status`,`remark`,`created_by`,`create_time`,`updated_by`,`update_time`) values
('image','image',1,'图片定义',1,NOW(),1,NOW());

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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据字典项表';

/*Data for the table `sys_dict_item` */

insert  into `sys_dict_item`(`dict_id`,`item_label`,`item_value`,`status`,`order_num`,`remark`,`created_by`,`create_time`,`updated_by`,`update_time`) values
(1,'avatar','user',1,0,'module',1,NOW(),1,NOW()),
(1,'avatar','sys_user',1,1,'业务表',1,NOW(),1,NOW());

/*Table structure for table `sys_menu` */

DROP TABLE IF EXISTS `sys_menu`;

CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父菜单ID，一级菜单为0',
  `name` varchar(50) NOT NULL COMMENT '菜单名称',
  `url` varchar(200) DEFAULT NULL COMMENT '菜单URL',
  `react_component` varchar(50) DEFAULT NULL COMMENT '菜单对应的React组件',
  `perms` varchar(500) DEFAULT NULL COMMENT '授权(多个用逗号分隔，如：user:list,user:create)',
  `type` int DEFAULT NULL COMMENT '类型   0：目录   1：菜单   2：按钮   3：接口目录   4：接口业务   5：具体接口',
  `icon` varchar(50) DEFAULT NULL COMMENT '菜单图标',
  `order_num` int DEFAULT NULL COMMENT '排序',
  `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单管理';

/*Data for the table `sys_menu` */

insert  into `sys_menu`(`menu_id`,`parent_id`,`name`,`url`,`react_component`,`perms`,`type`,`icon`,`order_num`,`created_by`,`create_time`,`updated_by`,`update_time`) values
(1,0,'系统管理','sys',NULL,NULL,0,'setting',1,NULL,NULL,1,'2026-08-18 16:08:18'),
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
(22,64,'首页统计','','',NULL,4,'bar-chart',1,NULL,NULL,1,'2026-08-18 16:13:13'),
(37,0,'系统监控','monitor',NULL,NULL,0,'dashboard',2,NULL,NULL,1,'2026-08-18 16:08:11'),
(38,37,'服务监控','server',NULL,'monitor:server:list',1,'safety',0,NULL,NULL,1,'2026-08-18 14:41:49'),
(39,37,'Redis监控','redis',NULL,'monitor:redis:list',1,'database',1,NULL,NULL,NULL,NULL),
(40,37,'接口文档','',NULL,NULL,1,'database',2,NULL,NULL,1,'2026-08-18 14:42:19'),
(41,64,'个人中心','','',NULL,4,'user',2,NULL,NULL,1,'2026-08-18 17:02:43'),
(42,41,'修改信息',NULL,NULL,'sys:profile:update',5,NULL,0,NULL,NULL,1,'2026-08-18 14:45:11'),
(43,41,'修改密码',NULL,NULL,'sys:profile:password',5,NULL,1,NULL,NULL,1,'2026-08-18 14:45:18'),
(44,1,'文件管理','sys/file','FileList','oss:file:list',1,'folder',3,NULL,NULL,1,'2026-08-18 14:40:40'),
(46,44,'下载文件',NULL,NULL,'oss:file:download',2,NULL,1,NULL,NULL,NULL,NULL),
(47,44,'获取文件链接',NULL,NULL,'oss:file:url',2,NULL,2,NULL,NULL,NULL,NULL),
(48,44,'删除文件',NULL,NULL,'oss:file:delete',2,NULL,3,NULL,NULL,NULL,NULL),
(49,1,'数据字典','sys/dict','DictList','sys:dict:list',1,'book',5,NULL,NULL,NULL,NULL),
(50,49,'查询字典',NULL,NULL,'sys:dict:list',2,NULL,0,NULL,NULL,NULL,NULL),
(51,49,'新增字典',NULL,NULL,'sys:dict:save',2,NULL,1,NULL,NULL,NULL,NULL),
(52,49,'修改字典',NULL,NULL,'sys:dict:update',2,NULL,2,NULL,NULL,NULL,NULL),
(53,49,'删除字典',NULL,NULL,'sys:dict:delete',2,NULL,3,NULL,NULL,NULL,NULL),
(55,49,'查询字典项',NULL,NULL,'sys:dict:item:list',2,NULL,4,NULL,NULL,NULL,NULL),
(56,49,'新增字典项',NULL,NULL,'sys:dict:item:save',2,NULL,5,NULL,NULL,NULL,NULL),
(57,49,'修改字典项',NULL,NULL,'sys:dict:item:update',2,NULL,6,NULL,NULL,NULL,NULL),
(58,49,'删除字典项',NULL,NULL,'sys:dict:item:delete',2,NULL,7,NULL,NULL,NULL,NULL),
(59,2,'用户信息',NULL,NULL,'sys:user:info',2,NULL,5,NULL,NULL,NULL,NULL),
(61,41,'登出接口',NULL,NULL,'sys:user:logout',5,NULL,6,1,'2026-01-07 15:06:11',1,'2026-01-07 15:06:11'),
(63,22,'数据统计',NULL,NULL,'sys:dashboard:list',5,NULL,0,NULL,NULL,NULL,NULL),
(64,0,'通用接口',NULL,NULL,NULL,3,'star',3,1,'2026-08-18 14:43:23',1,'2026-08-18 16:13:03'),
(65,64,'文件',NULL,NULL,NULL,4,'folder',3,1,'2026-08-18 17:02:16',1,'2026-08-18 17:02:23'),
(66,65,'文件上传',NULL,NULL,'oss:file:upload',5,NULL,1,1,'2026-08-18 17:04:10',1,'2026-08-18 17:04:10'),
(67,65,'文件链接',NULL,NULL,'oss:file:url',5,NULL,2,1,'2026-08-18 17:04:54',1,'2026-08-18 17:04:59'),
(68,41,'上传头像',NULL,NULL,'sys:user:upload',5,NULL,3,1,'2026-08-18 18:23:39',1,'2026-08-18 18:23:39');

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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色';

/*Data for the table `sys_role` */

insert  into `sys_role`(`role_id`,`role_name`,`role_desc`,`status`,`created_by`,`create_time`,`updated_by`,`update_time`) values
(1,'超级管理员','拥有系统所有权限，最高权限角色',1,NULL,'2025-12-02 11:50:30',1,'2026-08-18 18:24:14'),
(2,'系统管理员','管理系统基础配置和用户',1,NULL,'2025-12-02 11:50:30',1,'2026-08-18 17:23:34'),
(3,'普通用户','普通操作员，拥有基本查看权限',1,NULL,'2025-12-02 11:50:30',NULL,NULL),
(7,'add user','add user',1,1,'2026-01-05 16:54:36',1,'2026-08-18 17:23:45'),
(8,'edit user','edit user',1,1,'2026-01-05 17:00:49',1,'2026-01-13 11:55:44'),
(9,'del user','del user',1,1,'2026-01-05 17:07:12',1,'2026-01-13 11:55:57'),
(10,'list user','list user',1,1,'2026-01-05 17:07:48',1,'2026-01-13 11:56:02'),
(11,'接口测试员','接口测试员',1,1,'2026-08-18 16:49:05',1,'2026-08-18 18:27:45');

/*Table structure for table `sys_role_menu` */

DROP TABLE IF EXISTS `sys_role_menu`;

CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=513 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色与菜单对应关系';

/*Data for the table `sys_role_menu` */

insert  into `sys_role_menu`(`id`,`role_id`,`menu_id`) values
(68,3,22),
(69,3,23),
(70,3,24),
(71,3,29),
(72,3,33),
(73,3,41),
(74,3,42),
(75,3,43),
(103,3,59),
(104,3,11),
(210,8,1),
(211,8,2),
(212,8,4),
(218,9,1),
(219,9,2),
(220,9,5),
(222,10,1),
(223,10,2),
(403,2,64),
(404,2,1),
(405,2,65),
(406,2,2),
(407,2,66),
(408,2,3),
(409,2,67),
(410,2,4),
(411,2,5),
(412,2,6),
(413,2,7),
(414,2,8),
(415,2,9),
(416,2,10),
(417,2,11),
(418,2,12),
(419,2,13),
(420,2,14),
(421,2,41),
(422,2,42),
(423,2,43),
(424,2,44),
(425,2,46),
(426,2,47),
(427,2,48),
(428,2,49),
(429,2,50),
(430,2,51),
(431,2,52),
(432,2,53),
(433,2,55),
(434,2,56),
(435,2,57),
(436,2,58),
(437,2,59),
(438,7,64),
(439,7,1),
(440,7,65),
(441,7,2),
(442,7,3),
(443,7,67),
(445,1,64),
(446,1,1),
(447,1,65),
(448,1,2),
(449,1,66),
(450,1,3),
(451,1,67),
(452,1,4),
(453,1,68),
(454,1,5),
(455,1,6),
(456,1,7),
(457,1,8),
(458,1,9),
(459,1,10),
(460,1,11),
(461,1,12),
(462,1,13),
(463,1,14),
(464,1,19),
(465,1,20),
(466,1,22),
(467,1,37),
(468,1,38),
(469,1,39),
(470,1,40),
(471,1,41),
(472,1,42),
(473,1,43),
(474,1,44),
(475,1,46),
(476,1,47),
(477,1,48),
(478,1,49),
(479,1,50),
(480,1,51),
(481,1,52),
(482,1,53),
(483,1,55),
(484,1,56),
(485,1,57),
(486,1,58),
(487,1,59),
(488,1,61),
(489,1,63),
(500,11,64),
(501,11,1),
(502,11,68),
(503,11,41),
(504,11,49),
(505,11,50),
(506,11,51),
(507,11,52),
(508,11,53),
(509,11,55),
(510,11,56),
(511,11,57),
(512,11,58);

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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户';

/*Data for the table `sys_user` */

insert  into `sys_user`(`user_id`,`username`,`password`,`real_name`,`email`,`mobile`,`status`,`created_by`,`create_time`,`updated_by`,`update_time`) values
(1,'admin','$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6','超级管理员','admin@example.com','13800138000',1,NULL,'2025-12-02 11:50:46',1,'2026-08-18 17:20:09'),
(2,'sysadmin','$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6','系统管理员','zhangsan@example.com','13800138001',1,NULL,'2025-12-02 11:50:46',1,'2026-08-18 17:20:18'),
(3,'user','$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6','张三','lisi@example.com','13800138002',1,NULL,'2025-12-02 11:50:46',1,'2026-08-18 17:20:28'),
(5,'laoqian','$2a$10$6/KAus4VcvtyXfwVM9scKezhGg4YZ1bynx2IZZ4HkYvGUqNGcNGg6','老千',NULL,NULL,1,1,'2026-08-18 18:25:21',1,'2026-08-18 18:25:21');

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与角色对应关系';

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`id`,`user_id`,`role_id`) values
(11,1,1),
(12,2,2),
(13,3,3),
(14,5,11);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
