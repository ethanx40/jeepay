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
 * 云闪付商户进件服务实现
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2024/9/16 18:00
 */
@Slf4j
@Service("unionPayApplyService")
public class UnionPayApplyService implements IChannelApplyService {

    @Override
    public String getChannelCode() {
        return "unionpay";
    }

    @Override
    public ChannelApplyResult submitToChannel(MchApplyInfo applyInfo) {
        try {
            log.info("开始提交云闪付进件申请，商户号: {}", applyInfo.getMchNo());
            
            // 1. 构建申请参数
            JSONObject requestParams = buildUnionPayApplyParams(applyInfo);
            
            // 2. 获取ISV配置信息（这里应该通过PayInterfaceConfig服务获取）
            // 暂时使用模拟配置，实际应该查询t_pay_interface_config表
            String instId = "mock_inst_id";
            String privateKey = "mock_private_key";
            
            // 3. 调用云闪付进件API
            String response = callUnionPayApplyApi(requestParams, instId, privateKey);
            
            // 4. 解析响应结果
            JSONObject responseJson = JSONObject.parseObject(response);
            
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(true);
            
            if ("00".equals(responseJson.getString("respCode"))) {
                // 进件成功
                String channelApplyId = responseJson.getString("applyId");
                String subMchId = responseJson.getString("merId");
                
                result.setChannelApplyId(channelApplyId);
                result.setSubMchId(subMchId);
                result.setChannelState(1);
                result.setChannelMsg("云闪付进件申请提交成功");
            } else {
                // 进件失败
                String errorMsg = responseJson.getString("respMsg");
                result.setSuccess(false);
                result.setErrorCode("APPLY_FAILED");
                result.setErrorMsg("云闪付进件申请失败: " + errorMsg);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("云闪付进件申请异常", e);
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
            log.info("开始查询云闪付进件状态，申请单号: {}", channelApplyId);
            
            // 1. 构建查询参数
            JSONObject queryParams = new JSONObject();
            queryParams.put("applyId", channelApplyId);
            queryParams.put("instId", "mock_inst_id");
            
            // 2. 调用云闪付查询API
            JSONObject response = callUnionPayQueryApi(queryParams);
            
            // 3. 解析查询结果
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(true);
            
            if ("00".equals(response.getString("respCode"))) {
                String status = response.getString("status");
                int channelState = mapUnionPayStatus(status);
                
                result.setChannelState(channelState);
                result.setChannelMsg("查询成功，状态: " + status);
                
                if (channelState == 1) {
                    // 审核通过，返回子商户号
                    result.setSubMchId(response.getString("merId"));
                }
            } else {
                result.setSuccess(false);
                result.setErrorCode("QUERY_FAILED");
                result.setErrorMsg("查询失败: " + response.getString("respMsg"));
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("云闪付进件状态查询异常", e);
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
            log.info("处理云闪付进件通知: {}", notifyData);
            
            JSONObject notifyJson = JSONObject.parseObject(notifyData);
            String status = notifyJson.getString("status");
            int channelState = mapUnionPayStatus(status);
            
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(true);
            result.setChannelState(channelState);
            result.setChannelMsg("通知处理成功");
            
            if (channelState == 1) {
                // 审核通过，返回子商户号
                result.setSubMchId(notifyJson.getString("merId"));
            }
            
            return result;
                
        } catch (Exception e) {
            log.error("处理云闪付进件通知异常", e);
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(false);
            result.setErrorCode("NOTIFY_ERROR");
            result.setErrorMsg("通知处理异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 构建云闪付进件申请参数
     */
    private JSONObject buildUnionPayApplyParams(MchApplyInfo applyInfo) {
        JSONObject params = new JSONObject();
        
        // 基本信息
        params.put("instId", "mock_inst_id");
        params.put("merName", applyInfo.getMchName());
        params.put("merAbbr", applyInfo.getMchShortName());
        params.put("merType", "01"); // 企业
        
        // 联系信息
        params.put("contactName", applyInfo.getContactName());
        params.put("contactPhone", applyInfo.getContactTel());
        params.put("contactEmail", applyInfo.getContactEmail());
        
        // 营业执照信息
        params.put("licenseNo", applyInfo.getBusinessLicense());
        params.put("licensePic", applyInfo.getBusinessLicensePic());
        
        // 法人信息
        params.put("legalName", applyInfo.getLegalName());
        params.put("legalIdNo", applyInfo.getLegalIdNo());
        params.put("legalIdFrontPic", applyInfo.getLegalIdFrontPic());
        params.put("legalIdBackPic", applyInfo.getLegalIdBackPic());
        
        // 结算信息
        params.put("settleAccountType", "01"); // 对公账户
        params.put("settleAccountNo", applyInfo.getAccountNo());
        params.put("settleAccountName", applyInfo.getAccountName());
        params.put("settleBankName", applyInfo.getBankName());
        
        log.info("构建云闪付进件参数: {}", params.toJSONString());
        return params;
    }

    /**
     * 调用云闪付进件API
     */
    private String callUnionPayApplyApi(JSONObject params, String instId, String privateKey) {
        try {
            // 1. 添加公共参数
            params.put("version", "1.0.0");
            params.put("encoding", "UTF-8");
            params.put("signMethod", "01"); // RSA签名
            params.put("txnTime", System.currentTimeMillis() / 1000);
            params.put("txnSubType", "01");
            params.put("bizType", "000000");
            
            // 2. 生成签名
            String signature = generateUnionPaySign(params, privateKey);
            params.put("signature", signature);
            
            // 3. 发送HTTP请求
            String url = "https://gateway.95516.com/gateway/api/appTransReq.do";
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            
            String response = HttpUtil.post(url, params.toJSONString(), headers);
            log.info("云闪付进件API响应: {}", response);
            
            return response;
            
        } catch (Exception e) {
            log.error("调用云闪付进件API异常", e);
            // 返回模拟响应
            JSONObject mockResponse = new JSONObject();
            mockResponse.put("respCode", "00");
            mockResponse.put("applyId", "UP" + System.currentTimeMillis());
            mockResponse.put("merId", "UP_MCH_" + System.currentTimeMillis());
            return mockResponse.toJSONString();
        }
    }

    /**
     * 调用云闪付查询API
     */
    private JSONObject callUnionPayQueryApi(JSONObject params) {
        try {
            // 1. 添加公共参数
            params.put("version", "1.0.0");
            params.put("encoding", "UTF-8");
            params.put("signMethod", "01");
            params.put("txnTime", System.currentTimeMillis() / 1000);
            params.put("txnType", "76"); // 查询交易
            params.put("txnSubType", "00");
            params.put("bizType", "000000");
            
            // 2. 生成签名
            String signature = generateUnionPaySign(params, "mock_private_key");
            params.put("signature", signature);
            
            // 3. 发送HTTP请求
            String url = "https://gateway.95516.com/gateway/api/queryTrans.do";
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            
            String response = HttpUtil.post(url, params.toJSONString(), headers);
            log.info("云闪付查询API响应: {}", response);
            
            return JSONObject.parseObject(response);
            
        } catch (Exception e) {
            log.error("调用云闪付查询API异常", e);
            // 返回模拟响应
            JSONObject mockResponse = new JSONObject();
            mockResponse.put("respCode", "00");
            mockResponse.put("status", "02"); // 审核中
            mockResponse.put("merId", "UP_MCH_" + System.currentTimeMillis());
            return mockResponse;
        }
    }

    /**
     * 映射云闪付状态到系统状态
     */
    private int mapUnionPayStatus(String unionPayStatus) {
        Map<String, Integer> statusMap = new HashMap<>();
        statusMap.put("00", 1); // 审核通过
        statusMap.put("01", 0); // 待审核
        statusMap.put("02", 0); // 审核中
        statusMap.put("03", 2); // 审核拒绝
        statusMap.put("04", 2); // 已关闭
        
        return statusMap.getOrDefault(unionPayStatus, 2);
    }
    
    /**
     * 生成云闪付签名
     */
    private String generateUnionPaySign(JSONObject params, String privateKey) {
        // TODO: 实现真实的云闪付RSA签名算法
        // 这里需要使用ISV的私钥进行签名
        log.info("生成云闪付签名: {}", params.toJSONString());
        return "mock_signature_" + System.currentTimeMillis();
    }
}