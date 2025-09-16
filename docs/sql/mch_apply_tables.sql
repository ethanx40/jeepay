-- ========================================
-- 商户进件功能相关表结构
-- ========================================

-- 商户进件申请表
DROP TABLE IF EXISTS `t_mch_apply_record`;
CREATE TABLE `t_mch_apply_record` (
    `apply_id` VARCHAR(32) NOT NULL COMMENT '申请ID',
    `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
    `isv_no` VARCHAR(64) COMMENT '服务商编号',
    `channel_code` VARCHAR(32) NOT NULL COMMENT '渠道代码 WX_PAY-微信支付 ALI_PAY-支付宝 YSF_PAY-云闪付',
    `channel_apply_id` VARCHAR(128) COMMENT '渠道申请单号',
    `apply_status` TINYINT NOT NULL DEFAULT 0 COMMENT '申请状态 0-草稿 1-已提交 2-渠道处理中 3-审核通过 4-审核拒绝 5-已取消',
    `apply_data` TEXT COMMENT '申请数据JSON',
    `audit_info` TEXT COMMENT '审核信息JSON',
    `submit_time` TIMESTAMP(3) COMMENT '提交时间',
    `audit_time` TIMESTAMP(3) COMMENT '审核时间',
    `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`apply_id`),
    KEY `idx_mch_no` (`mch_no`),
    KEY `idx_channel_apply_id` (`channel_apply_id`),
    KEY `idx_apply_status` (`apply_status`),
    KEY `idx_channel_code` (`channel_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户进件申请记录表';

-- 进件材料表
DROP TABLE IF EXISTS `t_mch_apply_material`;
CREATE TABLE `t_mch_apply_material` (
    `material_id` VARCHAR(32) NOT NULL COMMENT '材料ID',
    `apply_id` VARCHAR(32) NOT NULL COMMENT '申请ID',
    `material_type` VARCHAR(32) NOT NULL COMMENT '材料类型 BUSINESS_LICENSE-营业执照 ID_CARD_FRONT-身份证正面 ID_CARD_BACK-身份证反面 BANK_ACCOUNT-银行开户许可证',
    `material_name` VARCHAR(128) NOT NULL COMMENT '材料名称',
    `file_url` VARCHAR(512) NOT NULL COMMENT '文件URL',
    `file_name` VARCHAR(128) COMMENT '文件名称',
    `file_size` BIGINT COMMENT '文件大小',
    `file_type` VARCHAR(32) COMMENT '文件类型 jpg png pdf',
    `is_required` TINYINT NOT NULL DEFAULT 1 COMMENT '是否必需 0-否 1-是',
    `upload_time` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '上传时间',
    PRIMARY KEY (`material_id`),
    KEY `idx_apply_id` (`apply_id`),
    KEY `idx_material_type` (`material_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进件材料表';

-- 进件审核记录表
DROP TABLE IF EXISTS `t_mch_apply_audit_record`;
CREATE TABLE `t_mch_apply_audit_record` (
    `audit_id` VARCHAR(32) NOT NULL COMMENT '审核ID',
    `apply_id` VARCHAR(32) NOT NULL COMMENT '申请ID',
    `audit_type` TINYINT NOT NULL COMMENT '审核类型 1-平台审核 2-渠道审核',
    `audit_status` TINYINT NOT NULL COMMENT '审核状态 1-通过 2-拒绝',
    `audit_opinion` TEXT COMMENT '审核意见',
    `auditor` VARCHAR(64) COMMENT '审核人',
    `audit_time` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '审核时间',
    PRIMARY KEY (`audit_id`),
    KEY `idx_apply_id` (`apply_id`),
    KEY `idx_audit_type` (`audit_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进件审核记录表';

-- 渠道进件配置表
DROP TABLE IF EXISTS `t_channel_apply_config`;
CREATE TABLE `t_channel_apply_config` (
    `config_id` VARCHAR(32) NOT NULL COMMENT '配置ID',
    `channel_code` VARCHAR(32) NOT NULL COMMENT '渠道代码',
    `config_name` VARCHAR(128) NOT NULL COMMENT '配置名称',
    `required_materials` TEXT COMMENT '必需材料JSON',
    `config_params` TEXT COMMENT '配置参数JSON',
    `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用 0-否 1-是',
    `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_channel_code` (`channel_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道进件配置表';

-- 初始化渠道进件配置数据
INSERT INTO `t_channel_apply_config` (`config_id`, `channel_code`, `config_name`, `required_materials`, `config_params`, `is_enabled`) VALUES
('WX_PAY_CONFIG', 'WX_PAY', '微信支付进件配置', 
'[
  {"type":"BUSINESS_LICENSE","name":"营业执照","required":true},
  {"type":"ID_CARD_FRONT","name":"法人身份证正面","required":true},
  {"type":"ID_CARD_BACK","name":"法人身份证反面","required":true},
  {"type":"BANK_ACCOUNT","name":"银行开户许可证","required":true}
]',
'{"apiUrl":"https://api.mch.weixin.qq.com/v3/applyment4sub/applyment","timeout":30000}',
1),
('ALI_PAY_CONFIG', 'ALI_PAY', '支付宝进件配置',
'[
  {"type":"BUSINESS_LICENSE","name":"营业执照","required":true},
  {"type":"ID_CARD_FRONT","name":"法人身份证正面","required":true},
  {"type":"BANK_ACCOUNT","name":"银行开户许可证","required":true}
]',
'{"apiUrl":"https://openapi.alipay.com/gateway.do","timeout":30000}',
1),
('YSF_PAY_CONFIG', 'YSF_PAY', '云闪付进件配置',
'[
  {"type":"BUSINESS_LICENSE","name":"营业执照","required":true},
  {"type":"ID_CARD_FRONT","name":"法人身份证正面","required":true},
  {"type":"BANK_ACCOUNT","name":"银行开户许可证","required":true}
]',
'{"apiUrl":"https://qra.95516.com","timeout":30000}',
1);

-- 新增进件相关权限
INSERT INTO `t_sys_entitlement` (`ent_id`, `ent_name`, `menu_icon`, `menu_uri`, `component_name`, `ent_type`, `quick_jump`, `state`, `pid`, `ent_sort`, `sys_type`) VALUES
-- 运营平台权限
('ENT_MCH_APPLY_LIST', '商户进件管理', 'file-text', '/mchApply', 'MchApplyListPage', 'ML', 0, 1, 'ROOT', 50, 'MGR'),
('ENT_MCH_APPLY_VIEW', '查看进件申请', '', '', '', 'PB', 0, 1, 'ENT_MCH_APPLY_LIST', 0, 'MGR'),
('ENT_MCH_APPLY_AUDIT', '审核进件申请', '', '', '', 'PB', 0, 1, 'ENT_MCH_APPLY_LIST', 0, 'MGR'),
('ENT_MCH_APPLY_CONFIG', '进件配置管理', '', '/mchApply/config', 'MchApplyConfigPage', 'MO', 0, 1, 'ENT_MCH_APPLY_LIST', 0, 'MGR'),

-- 商户系统权限  
('ENT_MCH_APPLY_INFO', '商户进件', 'file-add', '/apply', 'MchApplyPage', 'ML', 0, 1, 'ROOT', 30, 'MCH'),
('ENT_MCH_APPLY_SUBMIT', '提交进件申请', '', '', '', 'PB', 0, 1, 'ENT_MCH_APPLY_INFO', 0, 'MCH'),
('ENT_MCH_APPLY_QUERY', '查询进件状态', '', '', '', 'PB', 0, 1, 'ENT_MCH_APPLY_INFO', 0, 'MCH');