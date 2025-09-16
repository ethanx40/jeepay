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
package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 商户进件申请记录表
 * </p>
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Schema(description = "商户进件申请记录表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_apply_record")
public class MchApplyRecord extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<MchApplyRecord> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    /**
     * 申请ID
     */
    @Schema(description = "申请ID")
    @TableId
    private String applyId;

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
     * 渠道申请单号
     */
    @Schema(description = "渠道申请单号")
    private String channelApplyId;

    /**
     * 申请状态 0-草稿 1-已提交 2-渠道处理中 3-审核通过 4-审核拒绝 5-已取消
     */
    @Schema(description = "申请状态")
    private Byte applyStatus;

    /**
     * 申请数据JSON
     */
    @Schema(description = "申请数据JSON")
    private String applyData;

    /**
     * 审核信息JSON
     */
    @Schema(description = "审核信息JSON")
    private String auditInfo;

    /**
     * 提交时间
     */
    @Schema(description = "提交时间")
    private Date submitTime;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private Date auditTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updatedAt;

    /**
     * 申请状态枚举
     */
    public interface ApplyStatus {
        byte DRAFT = 0;              // 草稿
        byte SUBMITTED = 1;          // 已提交
        byte CHANNEL_PROCESSING = 2; // 渠道处理中
        byte APPROVED = 3;           // 审核通过
        byte REJECTED = 4;           // 审核拒绝
        byte CANCELLED = 5;          // 已取消
    }

    /**
     * 渠道代码常量
     */
    public interface ChannelCode {
        String WX_PAY = "WX_PAY";     // 微信支付
        String ALI_PAY = "ALI_PAY";   // 支付宝
        String YSF_PAY = "YSF_PAY";   // 云闪付
    }
}