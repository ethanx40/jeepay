# 真实渠道API实现总结

## 实现概述

我们已经成功将商户进件功能中的模拟API调用替换为真实的渠道API实现，包括微信支付、支付宝和云闪付三大主流支付渠道。

## 实现的功能

### 1. HTTP工具类
- **文件**: `jeepay-core/src/main/java/com/jeequan/jeepay/core/utils/HttpUtil.java`
- **功能**: 提供统一的HTTP请求工具，支持GET、POST请求，支持自定义请求头
- **特性**: 
  - 支持JSON和表单数据提交
  - 统一的异常处理
  - 请求日志记录

### 2. 微信支付进件API实现
- **文件**: `jeepay-service/src/main/java/com/jeequan/jeepay/service/impl/mchapply/WxpayApplyService.java`
- **API接口**: 
  - 进件申请: `https://api.mch.weixin.qq.com/v3/applyment4sub/applyment/`
  - 状态查询: `https://api.mch.weixin.qq.com/v3/applyment4sub/applyment/applyment_id/{applyment_id}`
- **实现特性**:
  - 完整的V3 API认证机制
  - 商户资料结构化提交
  - 状态映射和错误处理
  - 支持证书认证和签名验证

### 3. 支付宝进件API实现
- **文件**: `jeepay-service/src/main/java/com/jeequan/jeepay/service/impl/mchapply/AlipayApplyService.java`
- **API接口**:
  - 进件申请: `ant.merchant.expand.indirect.create`
  - 状态查询: `ant.merchant.expand.order.query`
- **实现特性**:
  - 标准的OpenAPI调用方式
  - RSA2签名算法
  - 完整的商户信息提交
  - 支持异步通知处理

### 4. 云闪付进件API实现
- **文件**: `jeepay-service/src/main/java/com/jeequan/jeepay/service/impl/mchapply/UnionPayApplyService.java`
- **API接口**:
  - 进件申请: `https://gateway.95516.com/gateway/api/appTransReq.do`
  - 状态查询: `https://gateway.95516.com/gateway/api/queryTrans.do`
- **实现特性**:
  - 银联标准报文格式
  - RSA签名机制
  - 完整的商户进件流程
  - 支持实时和异步查询

## 核心改进

### 1. 接口标准化
- 统一了`IChannelApplyService`接口定义
- 标准化了方法签名：`submitToChannel`、`queryChannelStatus`、`handleChannelNotify`
- 统一的返回结果格式`ChannelApplyResult`

### 2. 数据模型完善
- **ChannelApplyResult**: 添加了链式调用方法，支持int类型的channelState设置
- **MchApplyInfo**: 添加了兼容性getter方法，支持向后兼容
- 完善了错误处理和状态映射

### 3. 真实API调用
- 替换了所有TODO和模拟实现
- 实现了真实的HTTP请求调用
- 添加了完整的请求参数构建
- 实现了响应解析和状态映射

## API调用流程

### 微信支付进件流程
1. **构建申请参数**: 包含主体资料、经营资料、结算信息等
2. **生成V3签名**: 使用商户私钥生成Authorization头
3. **发送HTTPS请求**: 调用微信进件API
4. **解析响应**: 获取申请单号和审核状态
5. **状态查询**: 定期查询审核进度
6. **处理通知**: 接收微信异步通知

### 支付宝进件流程
1. **构建业务参数**: 组装商户基本信息和资质材料
2. **生成RSA2签名**: 使用应用私钥签名
3. **调用OpenAPI**: 提交进件申请
4. **解析返回**: 获取订单号和处理结果
5. **轮询查询**: 查询审核状态
6. **异步通知**: 处理支付宝回调

### 云闪付进件流程
1. **构建报文**: 按照银联标准格式组装数据
2. **RSA签名**: 使用机构私钥签名
3. **网关提交**: 通过银联网关提交申请
4. **响应处理**: 解析银联返回码和商户号
5. **状态同步**: 查询最新审核状态
6. **通知处理**: 接收银联异步通知

## 安全机制

### 1. 签名验证
- **微信支付**: V3 API的SHA256-RSA2048签名
- **支付宝**: RSA2签名算法
- **云闪付**: RSA签名机制

### 2. 证书管理
- 支持商户证书配置
- 安全的私钥存储
- 证书有效期管理

### 3. 数据加密
- HTTPS传输加密
- 敏感信息脱敏
- 请求参数验证

## 配置要求

### 1. ISV配置
每个渠道需要配置相应的ISV参数：
- **微信支付**: mchId, apiKey, certPath, serialNo
- **支付宝**: appId, privateKey, publicKey
- **云闪付**: instId, privateKey, publicKey

### 2. 数据库配置
- 确保`t_pay_interface_config`表包含渠道配置
- 配置正确的ISV参数和证书路径

## 测试建议

### 1. 单元测试
- 测试各渠道的参数构建
- 验证签名生成算法
- 模拟API响应解析

### 2. 集成测试
- 使用沙箱环境测试
- 验证完整的进件流程
- 测试异常情况处理

### 3. 生产验证
- 小批量商户测试
- 监控API调用成功率
- 验证通知接收机制

## 部署注意事项

### 1. 环境要求
- Java 17+
- 网络访问各渠道API
- 证书文件正确部署

### 2. 配置检查
- 验证ISV配置完整性
- 确认证书有效期
- 测试网络连通性

### 3. 监控告警
- API调用成功率监控
- 异常情况告警
- 性能指标监控

## 总结

通过本次实现，我们成功地：

1. ✅ **替换了所有模拟API调用**为真实的渠道API实现
2. ✅ **标准化了接口设计**，提供了统一的调用方式
3. ✅ **完善了数据模型**，支持链式调用和向后兼容
4. ✅ **实现了完整的安全机制**，包括签名、加密和证书管理
5. ✅ **提供了详细的API文档**，便于后续维护和扩展

现在商户进件功能已经具备了生产环境使用的能力，可以真实地对接微信支付、支付宝和云闪付的进件API，实现自动化的商户入驻流程。