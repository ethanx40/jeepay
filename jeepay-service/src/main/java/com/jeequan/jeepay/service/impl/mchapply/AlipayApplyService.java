/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com) & 如来神掌工作室 (https://github.com/jeequan)
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.service.impl.mchapply;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.entity.IsvInfo;
import com.jeequan.jeepay.core.model.params.mchapply.ChannelApplyResult;
import com.jeequan.jeepay.core.model.params.mchapply.MchApplyInfo;
import com.jeequan.jeepay.service.impl.IsvInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝进件服务实现
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2024/1/1 10:00
 */
@Slf4j
@Service("alipayApplyService")
public class AlipayApplyService implements IChannelApplyService {

    @Autowired
    private IsvInfoService isvInfoService;

    @Override
    public String getChannelCode() {
        return "ALI_PAY";
    }

    @Override
    public ChannelApplyResult submitToChannel(MchApplyInfo applyInfo) {
        log.info("提交支付宝进件申请: {}", applyInfo.getMchNo());
        
        try {
            // 1. 获取ISV配置信息
            IsvInfo isvInfo = isvInfoService.getById(applyInfo.getIsvNo());
            if (isvInfo == null) {
                return ChannelApplyResult.fail("ISV信息不存在");
            }
            
            // 2. 验证必需材料
            String validateResult = validateRequiredMaterials(applyInfo);
            if (StringUtils.hasText(validateResult)) {
                return ChannelApplyResult.fail(validateResult);
            }
            
            // 3. 构建支付宝进件请求参数
            JSONObject bizContent = buildAlipayApplyRequest(applyInfo);
            
            // 4. 调用支付宝进件API
            String orderId = callAlipayApplyApi(isvInfo, bizContent);
            
            // 5. 返回结果
            return ChannelApplyResult.success(orderId);
            
        } catch (Exception e) {
            log.error("支付宝进件申请失败", e);
            return ChannelApplyResult.fail("SYSTEM_ERROR", "进件申请失败: " + e.getMessage());
        }
    }

    @Override
    public ChannelApplyResult queryChannelStatus(String channelApplyId) {
        log.info("查询支付宝进件状态: {}", channelApplyId);
        
        try {
            // 1. 调用支付宝查询API
            JSONObject result = callAlipayQueryApi(channelApplyId);
            
            // 2. 解析状态
            String status = result.getString("status");
            String subMerchantId = result.getString("sub_merchant_id");
            String rejectReason = result.getString("reject_reason");
            
            // 3. 构建返回结果
            ChannelApplyResult applyResult = new ChannelApplyResult();
            applyResult.setSuccess(true);
            applyResult.setChannelState(status);
            applyResult.setChannelStateDesc(getAlipayStatusDesc(status));
            applyResult.setSubMchid(subMerchantId);
            applyResult.setRejectReason(rejectReason);
            
            // 4. 映射到统一状态
            applyResult.setApplyStatus(mapAlipayStatusToUnified(status));
            
            return applyResult;
            
        } catch (Exception e) {
            log.error("查询支付宝进件状态失败", e);
            return ChannelApplyResult.fail("SYSTEM_ERROR", "状态查询失败: " + e.getMessage());
        }
    }

    @Override
    public ChannelApplyResult handleChannelNotify(String notifyData) {
        log.info("处理支付宝进件回调通知: {}", notifyData);
        
        try {
            // 1. 验证回调签名
            if (!verifyAlipayNotify(notifyData)) {
                return ChannelApplyResult.fail("签名验证失败");
            }
            
            // 2. 解析回调数据
            JSONObject notifyJson = JSONObject.parseObject(notifyData);
            String orderId = notifyJson.getString("order_id");
            String status = notifyJson.getString("status");
            
            // 3. 构建返回结果
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(true);
            result.setChannelApplyId(orderId);
            result.setChannelState(status);
            result.setApplyStatus(mapAlipayStatusToUnified(status));
            
            return result;
            
        } catch (Exception e) {
            log.error("处理支付宝进件回调失败", e);
            return ChannelApplyResult.fail("回调处理失败: " + e.getMessage());
        }
    }

    /**
     * 验证必需材料
     */
    private String validateRequiredMaterials(MchApplyInfo applyInfo) {
        if (!StringUtils.hasText(applyInfo.getBusinessLicensePic())) {
            return "营业执照照片不能为空";
        }
        if (!StringUtils.hasText(applyInfo.getLegalIdFrontPic())) {
            return "法人身份证照片不能为空";
        }
        if (!StringUtils.hasText(applyInfo.getBankAccountLicensePic())) {
            return "银行开户许可证不能为空";
        }
        return null;
    }

    /**
     * 构建支付宝进件请求参数
     */
    private JSONObject buildAlipayApplyRequest(MchApplyInfo applyInfo) {
        JSONObject bizContent = new JSONObject();
        
        // 外部商户ID
        bizContent.put("external_id", applyInfo.getApplyId());
        
        // 商户名称
        bizContent.put("name", applyInfo.getMerchantName());
        bizContent.put("alias_name", applyInfo.getMerchantShortName());
        bizContent.put("service_phone", applyInfo.getServicePhone());
        
        // 联系人信息
        JSONArray contactInfo = new JSONArray();
        JSONObject contact = new JSONObject();
        contact.put("contact_name", applyInfo.getContactName());
        contact.put("contact_mobile", applyInfo.getContactPhone());
        contact.put("contact_email", applyInfo.getContactEmail());
        contact.put("contact_type", "LEGAL_PERSON");
        contactInfo.add(contact);
        bizContent.put("contact_info", contactInfo);
        
        // 地址信息
        JSONObject addressInfo = new JSONObject();
        addressInfo.put("city_code", applyInfo.getStoreAddressCode());
        addressInfo.put("district_code", applyInfo.getStoreAddressCode());
        addressInfo.put("address", applyInfo.getStoreAddress());
        bizContent.put("address_info", addressInfo);
        
        // 营业执照信息
        JSONObject businessLicense = new JSONObject();
        businessLicense.put("business_license_no", applyInfo.getBusinessLicenseNumber());
        businessLicense.put("business_license_pic", applyInfo.getBusinessLicensePic());
        businessLicense.put("legal_name", applyInfo.getLegalPerson());
        businessLicense.put("legal_cert_no", applyInfo.getLegalIdNumber());
        if (StringUtils.hasText(applyInfo.getBusinessLicenseValidEnd())) {
            businessLicense.put("expire_date", applyInfo.getBusinessLicenseValidEnd());
        }
        bizContent.put("business_license", businessLicense);
        
        // 门店信息
        JSONArray shopInfo = new JSONArray();
        JSONObject shop = new JSONObject();
        shop.put("shop_name", applyInfo.getStoreName());
        shop.put("shop_type", "OFFLINE_PAYMENT");
        shop.put("store_id", applyInfo.getMchNo());
        shopInfo.add(shop);
        bizContent.put("shop_info", shopInfo);
        
        // 结算信息
        JSONObject settleInfo = new JSONObject();
        settleInfo.put("settle_account_type", "ALIPAY_BALANCE");
        settleInfo.put("settle_account_no", applyInfo.getSettleAccountNo());
        bizContent.put("settle_info", settleInfo);
        
        return bizContent;
    }
    
    /**
     * 调用支付宝进件API
     */
    private String callAlipayApplyApi(IsvInfo isvInfo, JSONObject bizContent) {
        // TODO: 实现支付宝API调用
        // 这里需要使用支付宝SDK调用ant.merchant.expand.indirect.create接口
        
        log.info("调用支付宝进件API，ISV: {}, 请求数据: {}", isvInfo.getIsvNo(), bizContent.toJSONString());
        
        // 模拟返回订单号
        return "ALI_ORDER_" + System.currentTimeMillis();
    }
    
    /**
     * 调用支付宝查询API
     */
    private JSONObject callAlipayQueryApi(String orderId) {
        // TODO: 实现支付宝查询API调用
        // 调用ant.merchant.expand.indirect.query接口
        
        log.info("调用支付宝查询API，订单号: {}", orderId);
        
        // 模拟返回查询结果
        JSONObject result = new JSONObject();
        result.put("code", "10000");
        result.put("msg", "Success");
        result.put("order_id", orderId);
        result.put("status", "UNDER_REVIEW");
        result.put("sub_merchant_id", "2088000000000001");
        result.put("reject_reason", "");
        
        return result;
    }
    
    /**
     * 验证支付宝回调签名
     */
    private boolean verifyAlipayNotify(String notifyData) {
        // TODO: 实现支付宝回调签名验证
        log.info("验证支付宝回调签名: {}", notifyData);
        return true;
    }
    
    /**
     * 获取支付宝状态描述
     */
    private String getAlipayStatusDesc(String status) {
        Map<String, String> statusDescMap = new HashMap<>();
        statusDescMap.put("UNDER_REVIEW", "审核中");
        statusDescMap.put("APPROVED", "审核通过");
        statusDescMap.put("REJECTED", "审核拒绝");
        statusDescMap.put("NEED_UPLOAD", "需要补充材料");
        
        return statusDescMap.getOrDefault(status, "未知状态");
    }
    
    /**
     * 映射支付宝状态到统一状态
     */
    private Integer mapAlipayStatusToUnified(String alipayStatus) {
        Map<String, Integer> statusMap = new HashMap<>();
        statusMap.put("UNDER_REVIEW", 2);       // 渠道处理中
        statusMap.put("APPROVED", 3);           // 审核通过
        statusMap.put("REJECTED", 4);           // 审核拒绝
        statusMap.put("NEED_UPLOAD", 2);        // 渠道处理中
        
        return statusMap.getOrDefault(alipayStatus, 2);
    }
}