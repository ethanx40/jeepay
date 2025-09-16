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
 * 云闪付进件服务实现
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2024/1/1 10:00
 */
@Slf4j
@Service("unionPayApplyService")
public class UnionPayApplyService implements IChannelApplyService {

    @Autowired
    private IsvInfoService isvInfoService;

    @Override
    public String getChannelCode() {
        return "YSF";
    }

    @Override
    public ChannelApplyResult submitToChannel(MchApplyInfo applyInfo) {
        log.info("提交云闪付进件申请: {}", applyInfo.getMchNo());
        
        try {
            // 1. 获取ISV配置信息
            IsvInfo isvInfo = isvInfoService.getById(applyInfo.getIsvNo());
            if (isvInfo == null) {
                return ChannelApplyResult.fail("ISV信息不存在", "");
            }
            
            // 2. 验证必需材料
            String validateResult = validateRequiredMaterials(applyInfo);
            if (StringUtils.hasText(validateResult)) {
                return ChannelApplyResult.fail(validateResult, "");
            }
            
            // 3. 构建云闪付进件请求参数
            JSONObject requestData = buildUnionPayApplyRequest(applyInfo);
            
            // 4. 调用云闪付进件API
            String orderId = callUnionPayApplyApi(isvInfo, requestData);
            
            // 5. 返回结果
            return ChannelApplyResult.success(orderId, "");
            
        } catch (Exception e) {
            log.error("云闪付进件申请失败", e);
            return ChannelApplyResult.fail("进件申请失败: " + e.getMessage(), "");
        }
    }

    @Override
    public ChannelApplyResult queryChannelStatus(String channelApplyId) {
        log.info("查询云闪付进件状态: {}", channelApplyId);
        
        try {
            // 1. 调用云闪付查询API
            JSONObject result = callUnionPayQueryApi(channelApplyId);
            
            // 2. 解析状态
            String status = result.getString("status");
            String subMerchantId = result.getString("sub_merchant_id");
            String rejectReason = result.getString("reject_reason");
            
            // 3. 构建返回结果
            ChannelApplyResult applyResult = new ChannelApplyResult();
            applyResult.setSuccess(true);
            applyResult.setChannelApplyId(channelApplyId);
            applyResult.setChannelState(status);
            applyResult.setChannelStateDesc(getUnionPayStatusDesc(status));
            applyResult.setSubMchid(subMerchantId);
            applyResult.setRejectReason(rejectReason);
            
            // 4. 映射到统一状态
            applyResult.setApplyStatus(mapUnionPayStatusToUnified(status));
            
            return applyResult;
            
        } catch (Exception e) {
            log.error("查询云闪付进件状态失败", e);
            return ChannelApplyResult.fail("状态查询失败: " + e.getMessage(), "");
        }
    }

    @Override
    public ChannelApplyResult handleChannelNotify(String notifyData) {
        log.info("处理云闪付进件回调通知: {}", notifyData);
        
        try {
            // 1. 验证回调签名
            if (!verifyUnionPayNotify(notifyData)) {
                return ChannelApplyResult.fail("签名验证失败", "");
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
            result.setApplyStatus(mapUnionPayStatusToUnified(status));
            
            return result;
            
        } catch (Exception e) {
            log.error("处理云闪付进件回调失败", e);
            return ChannelApplyResult.fail("回调处理失败: " + e.getMessage(), "");
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
            return "法人身份证背面照片不能为空";
        }
        if (!StringUtils.hasText(applyInfo.getBankAccountLicensePic())) {
            return "银行开户许可证不能为空";
        }
        return null;
    }

    /**
     * 构建云闪付进件请求参数
     */
    private JSONObject buildUnionPayApplyRequest(MchApplyInfo applyInfo) {
        JSONObject requestData = new JSONObject();
        
        // 基本信息
        requestData.put("version", "5.1.0");
        requestData.put("encoding", "UTF-8");
        requestData.put("bizType", "000000");
        requestData.put("txnType", "79");
        requestData.put("txnSubType", "00");
        requestData.put("accessType", "0");
        requestData.put("orderId", applyInfo.getApplyId());
        
        // 商户信息
        JSONObject merchantInfo = new JSONObject();
        merchantInfo.put("merId", applyInfo.getMchNo());
        merchantInfo.put("merName", applyInfo.getMerchantName());
        merchantInfo.put("merAbbr", applyInfo.getMerchantShortName());
        merchantInfo.put("merCat", "5999"); // 商户类别码
        merchantInfo.put("merType", "01"); // 商户类型
        
        // 联系人信息
        merchantInfo.put("contactName", applyInfo.getContactName());
        merchantInfo.put("contactPhone", applyInfo.getContactPhone());
        merchantInfo.put("contactEmail", applyInfo.getContactEmail());
        
        // 地址信息
        merchantInfo.put("province", applyInfo.getStoreAddressCode().substring(0, 2));
        merchantInfo.put("city", applyInfo.getStoreAddressCode().substring(0, 4));
        merchantInfo.put("district", applyInfo.getStoreAddressCode());
        merchantInfo.put("merAddr", applyInfo.getStoreAddress());
        
        // 营业执照信息
        merchantInfo.put("licenseNo", applyInfo.getBusinessLicenseNumber());
        merchantInfo.put("licensePic", applyInfo.getBusinessLicensePic());
        merchantInfo.put("legalName", applyInfo.getLegalPerson());
        merchantInfo.put("legalIdNo", applyInfo.getLegalIdNumber());
        merchantInfo.put("legalIdFrontPic", applyInfo.getLegalIdFrontPic());
        merchantInfo.put("legalIdBackPic", applyInfo.getLegalIdBackPic());
        
        // 结算信息
        JSONObject settleInfo = new JSONObject();
        settleInfo.put("settleAcctType", "01"); // 结算账户类型
        settleInfo.put("settleAcctNo", applyInfo.getSettleAccountNo());
        settleInfo.put("settleAcctName", applyInfo.getBankAccountName());
        settleInfo.put("settleBankName", applyInfo.getBankName());
        settleInfo.put("settleBankNo", applyInfo.getBankBranchName());
        
        requestData.put("merchantInfo", merchantInfo);
        requestData.put("settleInfo", settleInfo);
        
        return requestData;
    }
    
    /**
     * 调用云闪付进件API
     */
    private String callUnionPayApplyApi(IsvInfo isvInfo, JSONObject requestData) {
        // TODO: 实现云闪付API调用
        // 这里需要使用云闪付SDK调用商户进件接口
        
        log.info("调用云闪付进件API，ISV: {}, 请求数据: {}", isvInfo.getIsvNo(), requestData.toJSONString());
        
        // 模拟返回订单号
        return "UNI_ORDER_" + System.currentTimeMillis();
    }
    
    /**
     * 调用云闪付查询API
     */
    private JSONObject callUnionPayQueryApi(String orderId) {
        // TODO: 实现云闪付查询API调用
        // 调用商户进件状态查询接口
        
        log.info("调用云闪付查询API，订单号: {}", orderId);
        
        // 模拟返回查询结果
        JSONObject result = new JSONObject();
        result.put("respCode", "00");
        result.put("respMsg", "成功");
        result.put("order_id", orderId);
        result.put("status", "02"); // 02-审核中
        result.put("sub_merchant_id", "898000000000001");
        result.put("reject_reason", "");
        
        return result;
    }
    
    /**
     * 验证云闪付回调签名
     */
    private boolean verifyUnionPayNotify(String notifyData) {
        // TODO: 实现云闪付回调签名验证
        log.info("验证云闪付回调签名: {}", notifyData);
        return true;
    }
    
    /**
     * 获取云闪付状态描述
     */
    private String getUnionPayStatusDesc(String status) {
        Map<String, String> statusDescMap = new HashMap<>();
        statusDescMap.put("01", "待审核");
        statusDescMap.put("02", "审核中");
        statusDescMap.put("03", "审核通过");
        statusDescMap.put("04", "审核拒绝");
        statusDescMap.put("05", "需要补充材料");
        
        return statusDescMap.getOrDefault(status, "未知状态");
    }
    
    /**
     * 映射云闪付状态到统一状态
     */
    private Integer mapUnionPayStatusToUnified(String unionPayStatus) {
        Map<String, Integer> statusMap = new HashMap<>();
        statusMap.put("01", 1);       // 待提交
        statusMap.put("02", 2);       // 渠道处理中
        statusMap.put("03", 3);       // 审核通过
        statusMap.put("04", 4);       // 审核拒绝
        statusMap.put("05", 2);       // 渠道处理中
        
        return statusMap.getOrDefault(unionPayStatus, 2);
    }
}