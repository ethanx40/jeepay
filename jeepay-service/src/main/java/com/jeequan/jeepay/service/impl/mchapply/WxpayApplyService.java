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
 * 微信支付进件服务实现
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2024/1/1 10:00
 */
@Slf4j
@Service
public class WxpayApplyService implements IChannelApplyService {

    @Autowired
    private IsvInfoService isvInfoService;

    @Override
    public String getChannelCode() {
        return "WX_PAY";
    }

    @Override
    public ChannelApplyResult submitToChannel(MchApplyInfo applyInfo) {
        log.info("提交微信支付进件申请: {}", applyInfo.getMchNo());
        
        try {
            // 1. 获取服务商配置信息
            IsvInfo isvInfo = isvInfoService.getById(applyInfo.getIsvNo());
            if (isvInfo == null) {
                return ChannelApplyResult.fail("PARAM_ERROR", "服务商信息不存在");
            }
            
            // 2. 验证必需材料
            String validateResult = validateRequiredMaterials(applyInfo);
            if (StringUtils.hasText(validateResult)) {
                return ChannelApplyResult.fail("PARAM_ERROR", validateResult);
            }
            
            // 3. 构建微信进件请求参数
            JSONObject requestData = buildWxApplyRequest(applyInfo);
            
            // 4. 调用微信进件API
            String applymentId = callWxApplyApi(isvInfo, requestData);
            
            // 5. 返回结果
            return ChannelApplyResult.success(applymentId);
            
        } catch (Exception e) {
            log.error("微信支付进件申请失败", e);
            return ChannelApplyResult.fail("SYSTEM_ERROR", "进件申请失败: " + e.getMessage());
        }
    }

    @Override
    public ChannelApplyResult queryChannelStatus(String channelApplyId) {
        log.info("查询微信支付进件状态: {}", channelApplyId);
        
        try {
            // 1. 调用微信查询API
            JSONObject result = callWxQueryApi(channelApplyId);
            
            // 2. 解析状态
            String signState = result.getString("sign_state");
            String signStateDesc = result.getString("sign_state_desc");
            String subMchid = result.getString("sub_mchid");
            
            // 3. 构建返回结果
            ChannelApplyResult applyResult = new ChannelApplyResult();
            applyResult.setSuccess(true);
            applyResult.setChannelState(signState);
            applyResult.setChannelStateDesc(signStateDesc);
            applyResult.setSubMchid(subMchid);
            
            // 4. 映射到统一状态
            applyResult.setApplyStatus(mapWxStatusToUnified(signState));
            
            return applyResult;
            
        } catch (Exception e) {
            log.error("查询微信支付进件状态失败", e);
            return ChannelApplyResult.fail("SYSTEM_ERROR", "状态查询失败: " + e.getMessage());
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
            return "法人身份证正面照片不能为空";
        }
        if (!StringUtils.hasText(applyInfo.getLegalIdBackPic())) {
            return "法人身份证反面照片不能为空";
        }
        if (!StringUtils.hasText(applyInfo.getStoreFrontPic())) {
            return "门店门头照片不能为空";
        }
        if (!StringUtils.hasText(applyInfo.getStoreIndoorPic())) {
            return "门店内景照片不能为空";
        }
        return null;
    }

    /**
     * 构建微信进件请求参数
     */
    private JSONObject buildWxApplyRequest(MchApplyInfo applyInfo) {
        JSONObject request = new JSONObject();
        
        // 业务申请编号
        request.put("business_code", applyInfo.getApplyId());
        
        // 联系人信息
        JSONObject contactInfo = new JSONObject();
        contactInfo.put("contact_name", applyInfo.getContactName());
        contactInfo.put("contact_id_number", applyInfo.getContactIdNumber());
        contactInfo.put("mobile_phone", applyInfo.getContactPhone());
        contactInfo.put("contact_email", applyInfo.getContactEmail());
        request.put("contact_info", contactInfo);
        
        // 主体信息
        JSONObject subjectInfo = buildSubjectInfo(applyInfo);
        request.put("subject_info", subjectInfo);
        
        // 经营信息
        JSONObject businessInfo = buildBusinessInfo(applyInfo);
        request.put("business_info", businessInfo);
        
        // 结算信息
        JSONObject settlementInfo = buildSettlementInfo(applyInfo);
        request.put("settlement_info", settlementInfo);
        
        // 银行账户信息
        JSONObject bankAccountInfo = buildBankAccountInfo(applyInfo);
        request.put("bank_account_info", bankAccountInfo);
        
        return request;
    }
    
    /**
     * 构建主体信息
     */
    private JSONObject buildSubjectInfo(MchApplyInfo applyInfo) {
        JSONObject subjectInfo = new JSONObject();
        subjectInfo.put("subject_type", "SUBJECT_TYPE_ENTERPRISE");
        
        // 营业执照信息
        JSONObject businessLicenseInfo = new JSONObject();
        businessLicenseInfo.put("license_copy", applyInfo.getBusinessLicensePic());
        businessLicenseInfo.put("license_number", applyInfo.getBusinessLicenseNumber());
        businessLicenseInfo.put("merchant_name", applyInfo.getMerchantName());
        businessLicenseInfo.put("legal_person", applyInfo.getLegalPerson());
        subjectInfo.put("business_license_info", businessLicenseInfo);
        
        // 身份证信息
        JSONObject identityInfo = new JSONObject();
        JSONObject idCardInfo = new JSONObject();
        idCardInfo.put("id_card_copy", applyInfo.getLegalIdFrontPic());
        idCardInfo.put("id_card_national", applyInfo.getLegalIdBackPic());
        idCardInfo.put("id_card_name", applyInfo.getLegalPerson());
        idCardInfo.put("id_card_number", applyInfo.getLegalIdNumber());
        if (StringUtils.hasText(applyInfo.getLegalIdValidStart())) {
            idCardInfo.put("card_period_begin", applyInfo.getLegalIdValidStart());
        }
        if (StringUtils.hasText(applyInfo.getLegalIdValidEnd())) {
            idCardInfo.put("card_period_end", applyInfo.getLegalIdValidEnd());
        }
        identityInfo.put("id_card_info", idCardInfo);
        subjectInfo.put("identity_info", identityInfo);
        
        return subjectInfo;
    }
    
    /**
     * 构建经营信息
     */
    private JSONObject buildBusinessInfo(MchApplyInfo applyInfo) {
        JSONObject businessInfo = new JSONObject();
        businessInfo.put("merchant_shortname", applyInfo.getMerchantShortName());
        businessInfo.put("service_phone", applyInfo.getServicePhone());
        
        // 经营场景信息
        JSONObject salesInfo = new JSONObject();
        JSONArray salesScenesType = new JSONArray();
        salesScenesType.add("SALES_SCENES_STORE");
        salesInfo.put("sales_scenes_type", salesScenesType);
        
        // 线下门店信息
        JSONObject bizStoreInfo = new JSONObject();
        bizStoreInfo.put("biz_store_name", applyInfo.getStoreName());
        bizStoreInfo.put("biz_address_code", applyInfo.getStoreAddressCode());
        bizStoreInfo.put("biz_store_address", applyInfo.getStoreAddress());
        
        JSONArray storeEntrancePic = new JSONArray();
        storeEntrancePic.add(applyInfo.getStoreFrontPic());
        bizStoreInfo.put("store_entrance_pic", storeEntrancePic);
        
        JSONArray indoorPic = new JSONArray();
        indoorPic.add(applyInfo.getStoreIndoorPic());
        bizStoreInfo.put("indoor_pic", indoorPic);
        
        salesInfo.put("biz_store_info", bizStoreInfo);
        businessInfo.put("sales_info", salesInfo);
        
        return businessInfo;
    }
    
    /**
     * 构建结算信息
     */
    private JSONObject buildSettlementInfo(MchApplyInfo applyInfo) {
        JSONObject settlementInfo = new JSONObject();
        settlementInfo.put("settlement_id", applyInfo.getSettlementId());
        settlementInfo.put("qualification_type", applyInfo.getQualificationType());
        return settlementInfo;
    }
    
    /**
     * 构建银行账户信息
     */
    private JSONObject buildBankAccountInfo(MchApplyInfo applyInfo) {
        JSONObject bankAccountInfo = new JSONObject();
        bankAccountInfo.put("bank_account_type", "BANK_ACCOUNT_TYPE_CORPORATE");
        bankAccountInfo.put("account_name", applyInfo.getBankAccountName());
        bankAccountInfo.put("account_bank", applyInfo.getBankName());
        bankAccountInfo.put("bank_address_code", applyInfo.getBankAddressCode());
        bankAccountInfo.put("bank_name", applyInfo.getBankBranchName());
        bankAccountInfo.put("account_number", applyInfo.getBankAccountNumber());
        return bankAccountInfo;
    }
    
    /**
     * 调用微信进件API
     */
    private String callWxApplyApi(IsvInfo isvInfo, JSONObject requestData) {
        // TODO: 实现微信支付API调用
        // 这里需要使用微信支付SDK或HTTP客户端调用API
        
        log.info("调用微信进件API，服务商: {}, 请求数据: {}", isvInfo.getIsvNo(), requestData.toJSONString());
        
        // 模拟返回申请单号
        return "2000002124775691";
    }
    
    /**
     * 调用微信查询API
     */
    private JSONObject callWxQueryApi(String channelApplyId) {
        // TODO: 实现微信支付查询API调用
        
        log.info("调用微信查询API，申请单号: {}", channelApplyId);
        
        // 模拟返回查询结果
        JSONObject result = new JSONObject();
        result.put("business_code", "CALLBACK_URL_NOT_CONFIGURED");
        result.put("applyment_id", channelApplyId);
        result.put("sub_mchid", "1900013511");
        result.put("sign_state", "CHECKING");
        result.put("sign_state_desc", "资料校验中");
        
        return result;
    }
    
    /**
     * 映射微信状态到统一状态
     */
    private Integer mapWxStatusToUnified(String wxStatus) {
        Map<String, Integer> statusMap = new HashMap<>();
        statusMap.put("CHECKING", 2);           // 渠道处理中
        statusMap.put("ACCOUNT_NEED_VERIFY", 2); // 渠道处理中
        statusMap.put("AUDITING", 2);           // 渠道处理中
        statusMap.put("REJECTED", 4);           // 审核拒绝
        statusMap.put("NEED_SIGN", 2);          // 渠道处理中
        statusMap.put("FINISH", 3);             // 审核通过
        
        return statusMap.getOrDefault(wxStatus, 2);
    }

    @Override
    public ChannelApplyResult handleChannelNotify(String notifyData) {
        log.info("处理微信支付进件回调通知: {}", notifyData);
        
        try {
            // TODO: 解析微信回调数据，验证签名等
            JSONObject notifyJson = JSONObject.parseObject(notifyData);
            
            // 构建回调结果
            ChannelApplyResult result = new ChannelApplyResult();
            result.setSuccess(true);
            result.setChannelApplyId(notifyJson.getString("applyment_id"));
            result.setChannelState(notifyJson.getString("sign_state"));
            result.setChannelStateDesc(notifyJson.getString("sign_state_desc"));
            
            // 如果有子商户号，设置子商户号
            if (notifyJson.containsKey("sub_mchid")) {
                result.setSubMchid(notifyJson.getString("sub_mchid"));
            }
            
            // 映射状态
            result.setApplyStatus(mapWxStatusToUnified(notifyJson.getString("sign_state")));
            
            return result;
            
        } catch (Exception e) {
            log.error("处理微信支付进件回调失败", e);
            return ChannelApplyResult.fail("SYSTEM_ERROR", "回调处理失败: " + e.getMessage());
        }
    }
}