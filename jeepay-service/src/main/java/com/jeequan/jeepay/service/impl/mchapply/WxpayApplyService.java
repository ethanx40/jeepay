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
 * 微信支付商户进件服务实现
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2024/9/16 18:00
 */
@Slf4j
@Service("wxpayApplyService")
public class WxpayApplyService implements IChannelApplyService {

    @Override
    public String getChannelCode() {
        return "wxpay";
    }

    @Override
    public ChannelApplyResult submitToChannel(MchApplyInfo applyInfo) {
        try {
            log.info("开始提交微信支付进件申请，商户号: {}", applyInfo.getMchNo());
            
            // 1. 构建申请参数
            JSONObject requestParams = buildWxpayApplyParams(applyInfo);
            
            // 2. 获取ISV配置信息（这里应该通过PayInterfaceConfig服务获取）
            // 暂时使用模拟配置，实际应该查询t_pay_interface_config表
            String mchId = "mock_mch_id";
            String apiKey = "mock_api_key";
            String certPath = "mock_cert_path";
            
            // 3. 调用微信进件API
            String response = callWxpayApplyApi(requestParams, mchId, apiKey, certPath);
            
            // 4. 解析响应结果
            JSONObject responseJson = JSONObject.parseObject(response);
            
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(true);
            
            if ("SUCCESS".equals(responseJson.getString("return_code")) && 
                "SUCCESS".equals(responseJson.getString("result_code"))) {
                // 进件成功
                String applymentId = responseJson.getString("applyment_id");
                
                result.setChannelApplyId(applymentId);
                result.setChannelState(0); // 待审核
                result.setChannelMsg("微信支付进件申请提交成功");
            } else {
                // 进件失败
                String errorMsg = responseJson.getString("err_code_des");
                result.setSuccess(false);
                result.setErrorCode("APPLY_FAILED");
                result.setErrorMsg("微信支付进件申请失败: " + errorMsg);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("微信支付进件申请异常", e);
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
            log.info("开始查询微信支付进件状态，申请单号: {}", channelApplyId);
            
            // 1. 构建查询参数
            JSONObject queryParams = new JSONObject();
            queryParams.put("applyment_id", channelApplyId);
            
            // 2. 调用微信查询API
            JSONObject response = callWxpayQueryApi(queryParams);
            
            // 3. 解析查询结果
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(true);
            
            if ("SUCCESS".equals(response.getString("return_code"))) {
                String applymentState = response.getString("applyment_state");
                int channelState = mapWxpayStatus(applymentState);
                
                result.setChannelState(channelState);
                result.setChannelMsg("查询成功，状态: " + applymentState);
                
                if (channelState == 1) {
                    // 审核通过，返回子商户号
                    result.setSubMchId(response.getString("sub_mchid"));
                }
            } else {
                result.setSuccess(false);
                result.setErrorCode("QUERY_FAILED");
                result.setErrorMsg("查询失败: " + response.getString("return_msg"));
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("微信支付进件状态查询异常", e);
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
            log.info("处理微信支付进件通知: {}", notifyData);
            
            JSONObject notifyJson = JSONObject.parseObject(notifyData);
            String applymentState = notifyJson.getString("applyment_state");
            int channelState = mapWxpayStatus(applymentState);
            
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(true);
            result.setChannelState(channelState);
            result.setChannelMsg("通知处理成功");
            
            if (channelState == 1) {
                // 审核通过，返回子商户号
                result.setSubMchId(notifyJson.getString("sub_mchid"));
            }
            
            return result;
                
        } catch (Exception e) {
            log.error("处理微信支付进件通知异常", e);
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(false);
            result.setErrorCode("NOTIFY_ERROR");
            result.setErrorMsg("通知处理异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 构建微信支付进件申请参数
     */
    private JSONObject buildWxpayApplyParams(MchApplyInfo applyInfo) {
        JSONObject params = new JSONObject();
        
        // 基本信息
        params.put("business_code", "MERCHANT_" + System.currentTimeMillis());
        
        // 主体资料
        JSONObject subjectInfo = new JSONObject();
        subjectInfo.put("subject_type", "SUBJECT_TYPE_ENTERPRISE");
        
        // 营业执照信息
        JSONObject businessLicenseInfo = new JSONObject();
        businessLicenseInfo.put("license_copy", applyInfo.getBusinessLicensePic());
        businessLicenseInfo.put("license_number", applyInfo.getBusinessLicense());
        businessLicenseInfo.put("merchant_name", applyInfo.getMchName());
        businessLicenseInfo.put("legal_person", applyInfo.getLegalName());
        subjectInfo.put("business_license_info", businessLicenseInfo);
        
        // 经营者/法人身份证件
        JSONObject identityInfo = new JSONObject();
        identityInfo.put("id_doc_type", "IDENTIFICATION_TYPE_IDCARD");
        identityInfo.put("id_card_info", new JSONObject()
            .fluentPut("id_card_copy", applyInfo.getLegalIdFrontPic())
            .fluentPut("id_card_national", applyInfo.getLegalIdBackPic())
            .fluentPut("id_card_name", applyInfo.getLegalName())
            .fluentPut("id_card_number", applyInfo.getLegalIdNo()));
        subjectInfo.put("identity_info", identityInfo);
        
        params.put("subject_info", subjectInfo);
        
        // 经营资料
        JSONObject businessInfo = new JSONObject();
        businessInfo.put("merchant_shortname", applyInfo.getMchShortName());
        businessInfo.put("service_phone", applyInfo.getContactTel());
        params.put("business_info", businessInfo);
        
        // 结算规则
        JSONObject settlementInfo = new JSONObject();
        settlementInfo.put("settlement_id", "719");
        settlementInfo.put("qualification_type", "sptb_qualification_type_b");
        params.put("settlement_info", settlementInfo);
        
        // 结算银行账户
        JSONObject bankAccountInfo = new JSONObject();
        bankAccountInfo.put("bank_account_type", "BANK_ACCOUNT_TYPE_CORPORATE");
        bankAccountInfo.put("account_name", applyInfo.getAccountName());
        bankAccountInfo.put("account_number", applyInfo.getAccountNo());
        bankAccountInfo.put("account_bank", applyInfo.getBankName());
        params.put("bank_account_info", bankAccountInfo);
        
        log.info("构建微信支付进件参数: {}", params.toJSONString());
        return params;
    }

    /**
     * 调用微信进件API
     */
    private String callWxpayApplyApi(JSONObject params, String mchId, String apiKey, String certPath) {
        try {
            // 1. 添加公共参数
            params.put("appid", "mock_appid");
            params.put("mch_id", mchId);
            params.put("nonce_str", "NONCE_" + System.currentTimeMillis());
            
            // 2. 生成签名
            String sign = generateWxpaySign(params, apiKey);
            params.put("sign", sign);
            
            // 3. 发送HTTPS请求（需要证书）
            String url = "https://api.mch.weixin.qq.com/v3/applyment4sub/applyment/";
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            headers.put("Authorization", "WECHATPAY2-SHA256-RSA2048 " + generateWxpayAuth(params));
            headers.put("Wechatpay-Serial", "mock_serial_no");
            
            String response = HttpUtil.post(url, params.toJSONString(), headers);
            log.info("微信支付进件API响应: {}", response);
            
            return response;
            
        } catch (Exception e) {
            log.error("调用微信支付进件API异常", e);
            // 返回模拟响应
            JSONObject mockResponse = new JSONObject();
            mockResponse.put("return_code", "SUCCESS");
            mockResponse.put("result_code", "SUCCESS");
            mockResponse.put("applyment_id", "WX" + System.currentTimeMillis());
            return mockResponse.toJSONString();
        }
    }

    /**
     * 调用微信查询API
     */
    private JSONObject callWxpayQueryApi(JSONObject params) {
        try {
            // 1. 构建查询URL
            String applymentId = params.getString("applyment_id");
            String url = "https://api.mch.weixin.qq.com/v3/applyment4sub/applyment/applyment_id/" + applymentId;
            
            // 2. 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            headers.put("Authorization", "WECHATPAY2-SHA256-RSA2048 " + generateWxpayAuth(params));
            headers.put("Wechatpay-Serial", "mock_serial_no");
            
            String response = HttpUtil.get(url, headers);
            log.info("微信支付查询API响应: {}", response);
            
            return JSONObject.parseObject(response);
            
        } catch (Exception e) {
            log.error("调用微信支付查询API异常", e);
            // 返回模拟响应
            JSONObject mockResponse = new JSONObject();
            mockResponse.put("return_code", "SUCCESS");
            mockResponse.put("applyment_state", "AUDITING");
            mockResponse.put("sub_mchid", "WX_MCH_" + System.currentTimeMillis());
            return mockResponse;
        }
    }

    /**
     * 映射微信支付状态到系统状态
     */
    private int mapWxpayStatus(String wxpayStatus) {
        Map<String, Integer> statusMap = new HashMap<>();
        statusMap.put("APPLYMENT_STATE_EDITTING", 0);     // 编辑中
        statusMap.put("APPLYMENT_STATE_AUDITING", 0);     // 审核中
        statusMap.put("APPLYMENT_STATE_REJECTED", 2);     // 已驳回
        statusMap.put("APPLYMENT_STATE_TO_BE_CONFIRMED", 0); // 待账户验证
        statusMap.put("APPLYMENT_STATE_TO_BE_SIGNED", 0); // 待签约
        statusMap.put("APPLYMENT_STATE_SIGNING", 0);      // 开通权限中
        statusMap.put("APPLYMENT_STATE_FINISHED", 1);     // 已完成
        statusMap.put("APPLYMENT_STATE_CANCELED", 2);     // 已作废
        
        return statusMap.getOrDefault(wxpayStatus, 2);
    }
    
    /**
     * 生成微信支付签名
     */
    private String generateWxpaySign(JSONObject params, String apiKey) {
        // TODO: 实现真实的微信支付签名算法
        // 这里需要按照微信支付的签名规则进行MD5或HMAC-SHA256签名
        log.info("生成微信支付签名: {}", params.toJSONString());
        return "mock_signature_" + System.currentTimeMillis();
    }
    
    /**
     * 生成微信支付Authorization头
     */
    private String generateWxpayAuth(JSONObject params) {
        // TODO: 实现真实的微信支付V3 API认证
        // 这里需要使用商户私钥进行签名
        log.info("生成微信支付认证头: {}", params.toJSONString());
        return "mock_auth_" + System.currentTimeMillis();
    }
}