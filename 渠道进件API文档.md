# 渠道进件API对接文档

## 1. 微信支付进件API

### 1.1 API概述
微信支付提供了特约商户进件API，支持服务商为子商户提交进件申请。

### 1.2 接入准备
- **服务商资质**：需要先申请成为微信支付服务商
- **API权限**：开通特约商户进件权限
- **证书配置**：配置API证书用于接口调用

### 1.3 核心API接口

#### 1.3.1 提交申请单API
```
POST https://api.mch.weixin.qq.com/v3/applyment4sub/applyment
```

**请求参数**：
```json
{
  "business_code": "CALLBACK_URL_NOT_CONFIGURED",
  "contact_info": {
    "contact_name": "张三",
    "contact_id_number": "320311770706001",
    "mobile_phone": "13888888888",
    "contact_email": "test@test.com"
  },
  "subject_info": {
    "subject_type": "SUBJECT_TYPE_ENTERPRISE",
    "business_license_info": {
      "license_copy": "47ZC6GC-vnrbEny__Ie_An5-tCpqxucuxi-vByf3Gjm7KE53JXvGy9tqZm2XAUf-4KGprrKhpVBDIUv0OF4wFNIO4kqg05InE4d2I6_H7I4",
      "license_number": "123456789012345678",
      "merchant_name": "腾讯科技有限公司",
      "legal_person": "张三"
    },
    "certificate_info": {
      "cert_copy": "47ZC6GC-vnrbEny__Ie_An5-tCpqxucuxi-vByf3Gjm7KE53JXvGy9tqZm2XAUf-4KGprrKhpVBDIUv0OF4wFNIO4kqg05InE4d2I6_H7I4",
      "cert_type": "CERTIFICATE_TYPE_2388",
      "cert_number": "111",
      "merchant_name": "腾讯科技有限公司",
      "company_address": "深圳市南山区科技园",
      "legal_person": "张三"
    },
    "identity_info": {
      "id_doc_copy": "47ZC6GC-vnrbEny__Ie_An5-tCpqxucuxi-vByf3Gjm7KE53JXvGy9tqZm2XAUf-4KGprrKhpVBDIUv0OF4wFNIO4kqg05InE4d2I6_H7I4",
      "id_card_info": {
        "id_card_copy": "47ZC6GC-vnrbEny__Ie_An5-tCpqxucuxi-vByf3Gjm7KE53JXvGy9tqZm2XAUf-4KGprrKhpVBDIUv0OF4wFNIO4kqg05InE4d2I6_H7I4",
        "id_card_national": "47ZC6GC-vnrbEny__Ie_An5-tCpqxucuxi-vByf3Gjm7KE53JXvGy9tqZm2XAUf-4KGprrKhpVBDIUv0OF4wFNIO4kqg05InE4d2I6_H7I4",
        "id_card_name": "张三",
        "id_card_number": "320311770706001",
        "card_period_begin": "2026-06-06",
        "card_period_end": "2026-06-06"
      }
    }
  },
  "business_info": {
    "merchant_shortname": "张三餐厅",
    "service_phone": "0758XXXXX",
    "sales_info": {
      "sales_scenes_type": ["SALES_SCENES_STORE"],
      "biz_store_info": {
        "biz_store_name": "张三餐厅",
        "biz_address_code": "440305",
        "biz_store_address": "南山区xx大厦x层xxxx室",
        "store_entrance_pic": ["47ZC6GC-vnrbEny__Ie_An5-tCpqxucuxi-vByf3Gjm7KE53JXvGy9tqZm2XAUf-4KGprrKhpVBDIUv0OF4wFNIO4kqg05InE4d2I6_H7I4"],
        "indoor_pic": ["47ZC6GC-vnrbEny__Ie_An5-tCpqxucuxi-vByf3Gjm7KE53JXvGy9tqZm2XAUf-4KGprrKhpVBDIUv0OF4wFNIO4kqg05InE4d2I6_H7I4"]
      }
    }
  },
  "settlement_info": {
    "settlement_id": "719",
    "qualification_type": "餐饮"
  },
  "bank_account_info": {
    "bank_account_type": "BANK_ACCOUNT_TYPE_CORPORATE",
    "account_name": "腾讯科技有限公司",
    "account_bank": "工商银行",
    "bank_address_code": "110000",
    "bank_name": "施秉县农村信用合作联社城关信用社",
    "account_number": "d+xT+MQCvrLHUVDWv/8MR/dB7TkXLVfSrUxMPZy6jWWYqDxi7m5BUqmTd+SM2OMKhEiZVtCFd0aVf0fwD4z4SXjCzZZbdJIgWdCCdd8LdS4="
  }
}
```

**响应参数**：
```json
{
  "applyment_id": 2000002124775691
}
```

#### 1.3.2 查询申请单状态API
```
GET https://api.mch.weixin.qq.com/v3/applyment4sub/applyment/business_code/{business_code}
```

**响应参数**：
```json
{
  "business_code": "CALLBACK_URL_NOT_CONFIGURED",
  "applyment_id": 2000002124775691,
  "sub_mchid": "1900013511",
  "sign_state": "ACCOUNT_NEED_VERIFY",
  "sign_state_desc": "系统已受理，请联系微信支付客服（95017）提供开户意愿确认函等相关资料，详情请查看指引",
  "audit_detail": [
    {
      "field": "id_card_copy",
      "field_name": "身份证人像面照片",
      "reject_reason": "身份证人像面照片：请提供清晰完整的身份证人像面照片"
    }
  ]
}
```

### 1.4 必需材料清单
1. **营业执照**：license_copy
2. **法人身份证**：id_card_copy, id_card_national  
3. **开户许可证**：account_license_copy（对公账户）
4. **门店照片**：store_entrance_pic, indoor_pic
5. **其他资质**：根据行业要求提供

### 1.5 状态说明
- `CHECKING`：资料校验中
- `ACCOUNT_NEED_VERIFY`：待账户验证
- `AUDITING`：审核中
- `REJECTED`：已驳回
- `NEED_SIGN`：待签约
- `FINISH`：完成

---

## 2. 支付宝进件API

### 2.1 API概述
支付宝提供了间连商户进件API，支持ISV为商户提交进件申请。

### 2.2 接入准备
- **ISV资质**：需要先申请成为支付宝ISV
- **应用创建**：在开放平台创建应用
- **API权限**：开通商户进件相关权限

### 2.3 核心API接口

#### 2.3.1 商户进件申请API
```
POST https://openapi.alipay.com/gateway.do
Method: ant.merchant.expand.indirect.create
```

**请求参数**：
```json
{
  "method": "ant.merchant.expand.indirect.create",
  "app_id": "2021000000000000",
  "charset": "UTF-8",
  "sign_type": "RSA2",
  "timestamp": "2024-01-01 12:00:00",
  "version": "1.0",
  "biz_content": {
    "external_id": "20240101001",
    "name": "张三餐厅",
    "alias_name": "张三餐厅",
    "service_phone": "0571-88888888",
    "contact_info": [
      {
        "contact_name": "张三",
        "contact_mobile": "13888888888",
        "contact_email": "zhangsan@example.com",
        "contact_type": "LEGAL_PERSON"
      }
    ],
    "address_info": {
      "city_code": "330100",
      "district_code": "330106",
      "address": "杭州市西湖区文三路xxx号"
    },
    "business_license": {
      "business_license_no": "91330100000000000X",
      "business_license_pic": "https://example.com/license.jpg",
      "legal_name": "张三",
      "legal_cert_no": "330106199001010001",
      "expire_date": "2030-12-31"
    },
    "shop_info": [
      {
        "shop_name": "张三餐厅",
        "shop_type": "OFFLINE_PAYMENT",
        "store_id": "STORE001"
      }
    ],
    "settle_info": {
      "settle_account_type": "ALIPAY_BALANCE",
      "settle_account_no": "zhangsan@example.com"
    }
  }
}
```

**响应参数**：
```json
{
  "code": "10000",
  "msg": "Success",
  "order_id": "20240101001",
  "apply_id": "2024010100000001"
}
```

#### 2.3.2 查询进件状态API
```
POST https://openapi.alipay.com/gateway.do
Method: ant.merchant.expand.indirect.query
```

**请求参数**：
```json
{
  "method": "ant.merchant.expand.indirect.query",
  "app_id": "2021000000000000",
  "biz_content": {
    "order_id": "20240101001"
  }
}
```

**响应参数**：
```json
{
  "code": "10000",
  "msg": "Success",
  "order_id": "20240101001",
  "status": "UNDER_REVIEW",
  "sub_merchant_id": "2088000000000001",
  "reject_reason": ""
}
```

### 2.4 必需材料清单
1. **营业执照**：business_license_pic
2. **法人身份证**：legal_cert_pic
3. **门店照片**：shop_pic
4. **银行开户许可证**：bank_cert_pic（企业商户）
5. **特殊行业资质**：根据行业要求

### 2.5 状态说明
- `UNDER_REVIEW`：审核中
- `APPROVED`：审核通过
- `REJECTED`：审核拒绝
- `NEED_UPLOAD`：需要补充材料

---

## 3. 云闪付进件API

### 3.1 API概述
云闪付（银联商务）提供了商户进件API，支持收单机构为商户提交进件申请。

### 3.2 接入准备
- **收单资质**：需要获得银联商务收单资质
- **机构代码**：分配的收单机构代码
- **API密钥**：用于接口签名的密钥

### 3.3 核心API接口

#### 3.3.1 商户进件申请API
```
POST https://open.95516.com/open/access/1.0/merchant.register
```

**请求参数**：
```json
{
  "version": "1.0.0",
  "encoding": "UTF-8",
  "certId": "69629715588",
  "signature": "signature_value",
  "signMethod": "01",
  "txnTime": "20240101120000",
  "accessType": "0",
  "bizType": "000000",
  "orderId": "20240101001",
  "reqReserved": {
    "orgCode": "ORG001",
    "merchantInfo": {
      "merchantName": "张三餐厅",
      "merchantShortName": "张三餐厅",
      "merchantType": "01",
      "businessLicense": "91330100000000000X",
      "legalPerson": "张三",
      "legalPersonIdCard": "330106199001010001",
      "contactPerson": "张三",
      "contactPhone": "13888888888",
      "merchantAddress": "杭州市西湖区文三路xxx号",
      "businessScope": "餐饮服务"
    },
    "settleInfo": {
      "settleAccountType": "01",
      "settleAccountNo": "6228480000000000001",
      "settleAccountName": "张三",
      "settleBankName": "中国银行",
      "settleBankCode": "104"
    },
    "rateInfo": {
      "debitCardRate": "0.0038",
      "creditCardRate": "0.0060",
      "scanCodeRate": "0.0038"
    },
    "attachments": [
      {
        "attachType": "01",
        "attachName": "营业执照",
        "attachUrl": "https://example.com/license.jpg"
      },
      {
        "attachType": "02", 
        "attachName": "法人身份证正面",
        "attachUrl": "https://example.com/id_front.jpg"
      },
      {
        "attachType": "03",
        "attachName": "法人身份证反面", 
        "attachUrl": "https://example.com/id_back.jpg"
      }
    ]
  }
}
```

**响应参数**：
```json
{
  "version": "1.0.0",
  "encoding": "UTF-8",
  "certId": "69629715588",
  "signature": "signature_value",
  "signMethod": "01",
  "txnTime": "20240101120000",
  "respCode": "00",
  "respMsg": "成功",
  "orderId": "20240101001",
  "queryId": "202401010000000001",
  "merchantId": "898000000000001"
}
```

#### 3.3.2 查询进件状态API
```
POST https://open.95516.com/open/access/1.0/merchant.query
```

**请求参数**：
```json
{
  "version": "1.0.0",
  "encoding": "UTF-8",
  "certId": "69629715588",
  "signature": "signature_value",
  "signMethod": "01",
  "txnTime": "20240101120000",
  "accessType": "0",
  "bizType": "000000",
  "orderId": "20240101001",
  "queryId": "202401010000000001"
}
```

**响应参数**：
```json
{
  "version": "1.0.0",
  "encoding": "UTF-8",
  "respCode": "00",
  "respMsg": "成功",
  "orderId": "20240101001",
  "queryId": "202401010000000001",
  "merchantId": "898000000000001",
  "status": "02",
  "statusDesc": "审核中",
  "auditInfo": {
    "auditResult": "",
    "auditReason": ""
  }
}
```

### 3.4 必需材料清单
1. **营业执照**：attachType=01
2. **法人身份证正面**：attachType=02
3. **法人身份证反面**：attachType=03
4. **银行开户许可证**：attachType=04
5. **门店照片**：attachType=05
6. **特殊行业许可证**：attachType=06

### 3.5 状态说明
- `01`：待审核
- `02`：审核中
- `03`：审核通过
- `04`：审核拒绝
- `05`：待签约
- `06`：已签约

---

## 4. 通用对接要点

### 4.1 文件上传处理
所有渠道都需要先上传文件获取文件ID或URL：

1. **微信支付**：使用图片上传API获取media_id
2. **支付宝**：上传到自己的服务器，提供URL
3. **云闪付**：上传到自己的服务器，提供URL

### 4.2 签名验证
- **微信支付**：使用证书进行签名
- **支付宝**：使用RSA2签名
- **云闪付**：使用银联标准签名

### 4.3 异步通知
所有渠道都支持异步通知进件结果：
- 需要配置回调URL
- 验证通知签名
- 及时响应确认

### 4.4 错误处理
- 统一错误码映射
- 重试机制设计
- 异常情况记录

### 4.5 测试环境
- **微信支付**：https://api.mch.weixin.qq.com（沙箱环境另有地址）
- **支付宝**：https://openapi.alipaydev.com（沙箱环境）
- **云闪付**：提供专门的测试环境地址

---

## 5. 实现建议

### 5.1 统一抽象
```java
public interface IChannelApplyService {
    String getChannelCode();
    ChannelApplyResult submitApply(MchApplyInfo applyInfo);
    ChannelApplyResult queryStatus(String channelApplyId);
    boolean handleNotify(String notifyData);
}
```

### 5.2 配置管理
```yaml
channel:
  wxpay:
    sp_mchid: "服务商商户号"
    sp_appid: "服务商应用ID"
    api_key: "API密钥"
    cert_path: "证书路径"
  alipay:
    app_id: "应用ID"
    private_key: "私钥"
    public_key: "支付宝公钥"
  unionpay:
    org_code: "机构代码"
    cert_id: "证书ID"
    private_key: "私钥"
```

### 5.3 材料映射
建立统一的材料类型到各渠道材料的映射关系：
```java
public enum MaterialType {
    BUSINESS_LICENSE("营业执照"),
    LEGAL_ID_FRONT("法人身份证正面"),
    LEGAL_ID_BACK("法人身份证反面"),
    BANK_ACCOUNT_LICENSE("开户许可证"),
    STORE_FRONT_PIC("门店门头照"),
    STORE_INDOOR_PIC("门店内景照");
}
```

这个文档为后续的具体实现提供了详细的API参考和技术要点。