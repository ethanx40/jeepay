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
package com.jeequan.jeepay.core.model.params.mchapply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商户进件申请信息
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Schema(description = "商户进件申请信息")
@Data
public class MchApplyInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商户号
     */
    @Schema(description = "商户号")
    private String mchNo;

    /**
     * 服务商编号
     */
    @Schema(description = "服务商编号")
    private String isvNo;

    /**
     * 渠道代码
     */
    @Schema(description = "渠道代码")
    private String channelCode;

    /**
     * 商户基本信息
     */
    @Schema(description = "商户基本信息")
    private MerchantInfo merchantInfo;

    /**
     * 联系人信息
     */
    @Schema(description = "联系人信息")
    private ContactInfo contactInfo;

    /**
     * 主体信息
     */
    @Schema(description = "主体信息")
    private SubjectInfo subjectInfo;

    /**
     * 经营信息
     */
    @Schema(description = "经营信息")
    private BusinessInfo businessInfo;

    /**
     * 结算信息
     */
    @Schema(description = "结算信息")
    private SettlementInfo settlementInfo;

    /**
     * 申请材料列表
     */
    @Schema(description = "申请材料列表")
    private List<MaterialInfo> materials;

    // 便捷方法，用于向后兼容
    public String getApplyId() {
        return this.mchNo + "_" + System.currentTimeMillis();
    }

    public String getMerchantName() {
        return merchantInfo != null ? merchantInfo.getMerchantName() : null;
    }

    public String getMerchantShortName() {
        return merchantInfo != null ? merchantInfo.getMerchantShortName() : null;
    }

    public String getBusinessLicenseNumber() {
        return subjectInfo != null ? subjectInfo.getLicenseNumber() : null;
    }

    public String getLegalPerson() {
        return subjectInfo != null ? subjectInfo.getLegalPersonName() : null;
    }

    public String getLegalIdNumber() {
        return subjectInfo != null ? subjectInfo.getLegalPersonIdNumber() : null;
    }

    public String getContactName() {
        return contactInfo != null ? contactInfo.getContactName() : null;
    }

    public String getContactPhone() {
        return contactInfo != null ? contactInfo.getContactMobile() : null;
    }

    public String getContactEmail() {
        return contactInfo != null ? contactInfo.getContactEmail() : null;
    }

    public String getContactIdNumber() {
        return contactInfo != null ? contactInfo.getContactIdNumber() : null;
    }

    public String getServicePhone() {
        return businessInfo != null ? businessInfo.getServicePhone() : null;
    }

    public String getStoreAddress() {
        return businessInfo != null ? businessInfo.getBusinessAddress() : null;
    }

    public String getStoreAddressCode() {
        return "110100"; // 默认北京市，实际应该从地址解析
    }

    public String getStoreName() {
        return getMerchantName();
    }

    public String getBankAccountName() {
        return settlementInfo != null ? settlementInfo.getAccountName() : null;
    }

    public String getBankName() {
        return settlementInfo != null ? settlementInfo.getBankName() : null;
    }

    public String getBankAccountNumber() {
        return settlementInfo != null ? settlementInfo.getBankAccount() : null;
    }

    public String getBankBranchName() {
        return settlementInfo != null ? settlementInfo.getBankCode() : null;
    }

    public String getBankAddressCode() {
        return getStoreAddressCode();
    }

    public String getSettleAccountNo() {
        return getBankAccountNumber();
    }

    public String getSettlementId() {
        return "DEFAULT";
    }

    public String getQualificationType() {
        return "GENERAL";
    }

    // 材料相关便捷方法
    public String getBusinessLicensePic() {
        return getMaterialUrl("BUSINESS_LICENSE");
    }

    public String getLegalIdFrontPic() {
        return getMaterialUrl("LEGAL_ID_FRONT");
    }

    public String getLegalIdBackPic() {
        return getMaterialUrl("LEGAL_ID_BACK");
    }

    public String getStoreFrontPic() {
        return getMaterialUrl("STORE_FRONT");
    }

    public String getStoreIndoorPic() {
        return getMaterialUrl("STORE_INDOOR");
    }

    public String getBankAccountLicensePic() {
        return getMaterialUrl("BANK_ACCOUNT_LICENSE");
    }

    public String getLegalIdValidStart() {
        return "2020-01-01";
    }

    public String getLegalIdValidEnd() {
        return "2030-12-31";
    }

    public String getBusinessLicenseValidEnd() {
        return "2030-12-31";
    }

    private String getMaterialUrl(String materialType) {
        if (materials == null) return null;
        return materials.stream()
                .filter(m -> materialType.equals(m.getMaterialType()))
                .map(MaterialInfo::getFileUrl)
                .findFirst()
                .orElse(null);
    }

    // 添加兼容性getter方法
    public String getMchName() {
        return getMerchantName();
    }

    public String getMchShortName() {
        return getMerchantShortName();
    }

    public String getContactTel() {
        return getContactPhone();
    }

    public String getBusinessLicense() {
        return getBusinessLicenseNumber();
    }

    public String getLegalName() {
        return getLegalPerson();
    }

    public String getLegalIdNo() {
        return getLegalIdNumber();
    }

    public String getAccountNo() {
        return getBankAccountNumber();
    }

    public String getAccountName() {
        return getBankAccountName();
    }

    /**
     * 商户基本信息
     */
    @Data
    @Schema(description = "商户基本信息")
    public static class MerchantInfo implements Serializable {
        @Schema(description = "商户名称")
        private String merchantName;

        @Schema(description = "商户简称")
        private String merchantShortName;

        @Schema(description = "商户类型 1-企业 2-个体工商户 3-小微商户")
        private Integer merchantType;

        @Schema(description = "营业执照号")
        private String businessLicense;

        @Schema(description = "统一社会信用代码")
        private String creditCode;
    }

    /**
     * 联系人信息
     */
    @Data
    @Schema(description = "联系人信息")
    public static class ContactInfo implements Serializable {
        @Schema(description = "联系人姓名")
        private String contactName;

        @Schema(description = "联系人手机号")
        private String contactMobile;

        @Schema(description = "联系人邮箱")
        private String contactEmail;

        @Schema(description = "联系人身份证号")
        private String contactIdNumber;
    }

    /**
     * 主体信息
     */
    @Data
    @Schema(description = "主体信息")
    public static class SubjectInfo implements Serializable {
        @Schema(description = "主体类型 1-企业 2-个体工商户 3-小微商户")
        private Integer subjectType;

        @Schema(description = "营业执照/登记证书编号")
        private String licenseNumber;

        @Schema(description = "商户名称")
        private String merchantName;

        @Schema(description = "法定代表人/经营者姓名")
        private String legalPersonName;

        @Schema(description = "法定代表人/经营者身份证号")
        private String legalPersonIdNumber;

        @Schema(description = "注册地址")
        private String registeredAddress;
    }

    /**
     * 经营信息
     */
    @Data
    @Schema(description = "经营信息")
    public static class BusinessInfo implements Serializable {
        @Schema(description = "经营场所")
        private String businessAddress;

        @Schema(description = "经营范围")
        private String businessScope;

        @Schema(description = "行业类别")
        private String industryCategory;

        @Schema(description = "客服电话")
        private String servicePhone;

        @Schema(description = "网站URL")
        private String websiteUrl;
    }

    /**
     * 结算信息
     */
    @Data
    @Schema(description = "结算信息")
    public static class SettlementInfo implements Serializable {
        @Schema(description = "结算账户类型 1-对公账户 2-对私账户")
        private Integer accountType;

        @Schema(description = "开户银行")
        private String bankName;

        @Schema(description = "银行账号")
        private String bankAccount;

        @Schema(description = "开户名")
        private String accountName;

        @Schema(description = "开户行联行号")
        private String bankCode;
    }

    /**
     * 材料信息
     */
    @Data
    @Schema(description = "材料信息")
    public static class MaterialInfo implements Serializable {
        @Schema(description = "材料类型")
        private String materialType;

        @Schema(description = "材料名称")
        private String materialName;

        @Schema(description = "文件URL")
        private String fileUrl;

        @Schema(description = "文件名称")
        private String fileName;

        @Schema(description = "是否必需")
        private Boolean isRequired;
    }
}