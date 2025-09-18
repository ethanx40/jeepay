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

/**
 * 渠道进件结果
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Schema(description = "渠道进件结果")
@Data
public class ChannelApplyResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * 渠道申请单号
     */
    @Schema(description = "渠道申请单号")
    private String channelApplyId;

    /**
     * 子商户号（审核通过后返回）
     */
    @Schema(description = "子商户号")
    private String subMchId;

    /**
     * 子商户号（兼容字段）
     */
    @Schema(description = "子商户号")
    private String subMchid;

    /**
     * 渠道状态
     */
    @Schema(description = "渠道状态")
    private String channelState;

    /**
     * 渠道状态描述
     */
    @Schema(description = "渠道状态描述")
    private String channelStateDesc;

    /**
     * 申请状态（统一状态）
     */
    @Schema(description = "申请状态")
    private Integer applyStatus;

    /**
     * 拒绝原因
     */
    @Schema(description = "拒绝原因")
    private String rejectReason;

    /**
     * 错误码
     */
    @Schema(description = "错误码")
    private String errorCode;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 渠道返回的原始数据
     */
    @Schema(description = "渠道返回的原始数据")
    private String channelData;

    // Getter和Setter方法
    public String getSubMchid() {
        return subMchid;
    }

    public void setSubMchid(String subMchid) {
        this.subMchid = subMchid;
    }

    public String getChannelState() {
        return channelState;
    }

    public void setChannelState(String channelState) {
        this.channelState = channelState;
    }

    public String getChannelStateDesc() {
        return channelStateDesc;
    }

    public void setChannelStateDesc(String channelStateDesc) {
        this.channelStateDesc = channelStateDesc;
    }

    public Integer getApplyStatus() {
        return applyStatus;
    }

    public void setApplyStatus(Integer applyStatus) {
        this.applyStatus = applyStatus;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getSubMchId() {
        return subMchId;
    }

    /**
     * 创建成功结果
     */
    public static ChannelApplyResult success(String channelApplyId) {
        ChannelApplyResult result = new ChannelApplyResult();
        result.setSuccess(true);
        result.setChannelApplyId(channelApplyId);
        return result;
    }

    /**
     * 创建成功结果（带子商户号）
     */
    public static ChannelApplyResult success(String channelApplyId, String subMchId) {
        ChannelApplyResult result = new ChannelApplyResult();
        result.setSuccess(true);
        result.setChannelApplyId(channelApplyId);
        result.setSubMchId(subMchId);
        return result;
    }

    /**
     * 创建失败结果
     */
    public static ChannelApplyResult fail(String errorCode, String errorMsg) {
        ChannelApplyResult result = new ChannelApplyResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMsg(errorMsg);
        return result;
    }

    /**
     * 创建失败结果（单参数）
     */
    public static ChannelApplyResult fail(String errorMsg) {
        ChannelApplyResult result = new ChannelApplyResult();
        result.setSuccess(false);
        result.setErrorMsg(errorMsg);
        return result;
    }

    /**
     * 设置渠道状态（支持int类型）
     */
    public ChannelApplyResult setChannelState(int channelState) {
        this.channelState = String.valueOf(channelState);
        return this;
    }

    /**
     * 设置渠道消息
     */
    public ChannelApplyResult setChannelMsg(String channelMsg) {
        this.channelStateDesc = channelMsg;
        return this;
    }

    /**
     * 设置子商户号（链式调用）
     */
    public ChannelApplyResult setSubMchId(String subMchId) {
        this.subMchId = subMchId;
        this.subMchid = subMchId; // 同时设置兼容字段
        return this;
    }

    /**
     * 设置渠道申请ID（链式调用）
     */
    public ChannelApplyResult setChannelApplyId(String channelApplyId) {
        this.channelApplyId = channelApplyId;
        return this;
    }

    /**
     * 设置成功状态（链式调用）
     */
    public ChannelApplyResult setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    /**
     * 设置错误码（链式调用）
     */
    public ChannelApplyResult setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    /**
     * 设置错误消息（链式调用）
     */
    public ChannelApplyResult setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }
}