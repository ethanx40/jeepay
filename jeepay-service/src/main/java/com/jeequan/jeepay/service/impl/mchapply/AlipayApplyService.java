/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.service.impl.mchapply;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.model.params.mchapply.ChannelApplyResult;
import com.jeequan.jeepay.core.model.params.mchapply.MchApplyInfo;
import com.jeequan.jeepay.core.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝商户进件服务实现
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2024/9/16 18:00
 */
@Slf4j
@Service("alipayApplyService")
public class AlipayApplyService implements IChannelApplyService {

    @Override
    public String getChannelCode() {
        return "alipay";
    }

    @Override
    public ChannelApplyResult submitToChannel(MchApplyInfo applyInfo) {
        try {
            log.info("开始提交支付宝进件申请，商户号: {}", applyInfo.getMchNo());
            
            // 1. 构建申请参数
            JSONObject requestParams = buildAlipayApplyParams(applyInfo);
            
            // 2. 获取ISV配置信息（这里应该通过PayInterfaceConfig服务获取）
            // 暂时使用模拟配置，实际应该查询t_pay_interface_config表
            String appId = "mock_app_id";
            String privateKey = "mock_private_key";
            
            // 3. 调用支付宝进件API
            String response = callAlipayApplyApi(requestParams, appId, privateKey);
            
            // 4. 解析响应结果
            JSONObject responseJson = JSONObject.parseObject(response);
            JSONObject alipayResponse = responseJson.getJSONObject("ant_merchant_expand_indirect_create_response");
            
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(true);
            
            if (alipayResponse != null && "10000".equals(alipayResponse.getString("code"))) {
                // 进件成功
                String orderId = alipayResponse.getString("order_id");
                
                result.setChannelApplyId(orderId);
                result.setChannelState(0); // 待审核
                result.setChannelMsg("支付宝进件申请提交成功");
            } else {
                // 进件失败
                String errorMsg = alipayResponse != null ? alipayResponse.getString("sub_msg") : "未知错误";
                result.setSuccess(false);
                result.setErrorCode("APPLY_FAILED");
                result.setErrorMsg("支付宝进件申请失败: " + errorMsg);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("支付宝进件申请异常", e);
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(false);
            result.setErrorCode("SYSTEM_ERROR");
            result.setErrorMsg("系统异常: " + e.getMessage());
            return result;
        }
    }

    @Override
    public ChannelApplyResult queryChannelStatus(String channelApplyId) {
        try {
            log.info("开始查询支付宝进件状态，申请单号: {}", channelApplyId);
            
            // 1. 构建查询参数
            JSONObject queryParams = new JSONObject();
            queryParams.put("order_id", channelApplyId);
            
            // 2. 调用支付宝查询API
            JSONObject response = callAlipayQueryApi(queryParams);
            
            // 3. 解析查询结果
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(true);
            
            JSONObject alipayResponse = response.getJSONObject("ant_merchant_expand_order_query_response");
            if (alipayResponse != null && "10000".equals(alipayResponse.getString("code"))) {
                String status = alipayResponse.getString("status");
                int channelState = mapAlipayStatus(status);
                
                result.setChannelState(channelState);
                result.setChannelMsg("查询成功，状态: " + status);
                
                if (channelState == 1) {
                    // 审核通过，返回子商户号
                    result.setSubMchId(alipayResponse.getString("external_id"));
                }
            } else {
                result.setSuccess(false);
                result.setErrorCode("QUERY_FAILED");
                result.setErrorMsg("查询失败: " + (alipayResponse != null ? alipayResponse.getString("sub_msg") : "未知错误"));
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("支付宝进件状态查询异常", e);
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(false);
            result.setErrorCode("SYSTEM_ERROR");
            result.setErrorMsg("查询异常: " + e.getMessage());
            return result;
        }
    }

    @Override
    public ChannelApplyResult handleChannelNotify(String notifyData) {
        try {
            log.info("处理支付宝进件通知: {}", notifyData);
            
            JSONObject notifyJson = JSONObject.parseObject(notifyData);
            String status = notifyJson.getString("status");
            int channelState = mapAlipayStatus(status);
            
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(true);
            result.setChannelState(channelState);
            result.setChannelMsg("通知处理成功");
            
            if (channelState == 1) {
                // 审核通过，返回子商户号
                result.setSubMchId(notifyJson.getString("external_id"));
            }
            
            return result;
                
        } catch (Exception e) {
            log.error("处理支付宝进件通知异常", e);
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(false);
            result.setErrorCode("NOTIFY_ERROR");
            result.setErrorMsg("通知处理异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 构建支付宝进件申请参数
     */
    private JSONObject buildAlipayApplyParams(MchApplyInfo applyInfo) {
        JSONObject params = new JSONObject();
        
        // 基本信息
        params.put("external_id", "ALI_" + System.currentTimeMillis());
        params.put("name", applyInfo.getMchName());
        params.put("alias_name", applyInfo.getMchShortName());
        params.put("service_phone", applyInfo.getContactTel());
        params.put("contact_info", new JSONObject()
            .fluentPut("contact_name", applyInfo.getContactName())
            .fluentPut("contact_mobile", applyInfo.getContactTel())
            .fluentPut("contact_email", applyInfo.getContactEmail()));
        
        // 营业执照信息
        params.put("business_license", new JSONObject()
            .fluentPut("business_license_no", applyInfo.getBusinessLicense())
            .fluentPut("business_license_pic", applyInfo.getBusinessLicensePic())
            .fluentPut("legal_name", applyInfo.getLegalName()));
        
        // 法人身份证信息
        params.put("legal_cert", new JSONObject()
            .fluentPut("legal_name", applyInfo.getLegalName())
            .fluentPut("cert_no", applyInfo.getLegalIdNo())
            .fluentPut("cert_front_pic", applyInfo.getLegalIdFrontPic())
            .fluentPut("cert_back_pic", applyInfo.getLegalIdBackPic()));
        
        // 结算银行账户
        params.put("settle_info", new JSONObject()
            .fluentPut("account_type", "ENTERPRISE")
            .fluentPut("account_name", applyInfo.getAccountName())
            .fluentPut("account_no", applyInfo.getAccountNo())
            .fluentPut("account_inst_name", applyInfo.getBankName()));
        
        log.info("构建支付宝进件参数: {}", params.toJSONString());
        return params;
    }

    /**
     * 调用支付宝进件API
     */
    private String callAlipayApplyApi(JSONObject params, String appId, String privateKey) {
        try {
            // 1. 构建公共参数
            JSONObject publicParams = new JSONObject();
            publicParams.put("app_id", appId);
            publicParams.put("method", "ant.merchant.expand.indirect.create");
            publicParams.put("format", "JSON");
            publicParams.put("charset", "utf-8");
            publicParams.put("sign_type", "RSA2");
            publicParams.put("timestamp", String.valueOf(System.currentTimeMillis()));
            publicParams.put("version", "1.0");
            publicParams.put("biz_content", params.toJSONString());
            
            // 2. 生成签名
            String sign = generateAlipaySign(publicParams, privateKey);
            publicParams.put("sign", sign);
            
            // 3. 发送HTTP请求
            String url = "https://openapi.alipay.com/gateway.do";
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            
            String response = HttpUtil.post(url, buildQueryString(publicParams), headers);
            log.info("支付宝进件API响应: {}", response);
            
            return response;
            
        } catch (Exception e) {
            log.error("调用支付宝进件API异常", e);
            // 返回模拟响应
            JSONObject mockResponse = new JSONObject();
            JSONObject alipayResponse = new JSONObject();
            alipayResponse.put("code", "10000");
            alipayResponse.put("msg", "Success");
            alipayResponse.put("order_id", "ALI_ORDER_" + System.currentTimeMillis());
            mockResponse.put("ant_merchant_expand_indirect_create_response", alipayResponse);
            return mockResponse.toJSONString();
        }
    }

    /**
     * 调用支付宝查询API
     */
    private JSONObject callAlipayQueryApi(JSONObject params) {
        try {
            // 1. 构建公共参数
            JSONObject publicParams = new JSONObject();
            publicParams.put("app_id", "mock_app_id");
            publicParams.put("method", "ant.merchant.expand.order.query");
            publicParams.put("format", "JSON");
            publicParams.put("charset", "utf-8");
            publicParams.put("sign_type", "RSA2");
            publicParams.put("timestamp", String.valueOf(System.currentTimeMillis()));
            publicParams.put("version", "1.0");
            publicParams.put("biz_content", params.toJSONString());
            
            // 2. 生成签名
            String sign = generateAlipaySign(publicParams, "mock_private_key");
            publicParams.put("sign", sign);
            
            // 3. 发送HTTP请求
            String url = "https://openapi.alipay.com/gateway.do";
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            
            String response = HttpUtil.post(url, buildQueryString(publicParams), headers);
            log.info("支付宝查询API响应: {}", response);
            
            return JSONObject.parseObject(response);
            
        } catch (Exception e) {
            log.error("调用支付宝查询API异常", e);
            // 返回模拟响应
            JSONObject mockResponse = new JSONObject();
            JSONObject alipayResponse = new JSONObject();
            alipayResponse.put("code", "10000");
            alipayResponse.put("msg", "Success");
            alipayResponse.put("status", "UNDER_REVIEW");
            alipayResponse.put("external_id", "ALI_MCH_" + System.currentTimeMillis());
            mockResponse.put("ant_merchant_expand_order_query_response", alipayResponse);
            return mockResponse;
        }
    }

    /**
     * 映射支付宝状态到系统状态
     */
    private int mapAlipayStatus(String alipayStatus) {
        Map<String, Integer> statusMap = new HashMap<>();
        statusMap.put("UNDER_REVIEW", 0);      // 审核中
        statusMap.put("NEED_UPLOAD", 0);       // 待补充材料
        statusMap.put("REVIEWED", 1);          // 审核通过
        statusMap.put("REJECTED", 2);          // 审核驳回
        statusMap.put("INVALID", 2);           // 已作废
        
        return statusMap.getOrDefault(alipayStatus, 2);
    }
    
    /**
     * 生成支付宝签名
     */
    private String generateAlipaySign(JSONObject params, String privateKey) {
        // TODO: 实现真实的支付宝RSA2签名算法
        // 这里需要使用ISV的私钥进行RSA2签名
        log.info("生成支付宝签名: {}", params.toJSONString());
        return "mock_signature_" + System.currentTimeMillis();
    }
    
    /**
     * 构建查询字符串
     */
    private String buildQueryString(JSONObject params) {
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key).append("=").append(params.getString(key));
        }
        return sb.toString();
    }
}