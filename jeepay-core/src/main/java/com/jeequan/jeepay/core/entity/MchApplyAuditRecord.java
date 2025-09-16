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
 * 进件审核记录表
 * </p>
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Schema(description = "进件审核记录表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_apply_audit_record")
public class MchApplyAuditRecord extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<MchApplyAuditRecord> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    /**
     * 审核ID
     */
    @Schema(description = "审核ID")
    @TableId
    private String auditId;

    /**
     * 申请ID
     */
    @Schema(description = "申请ID")
    private String applyId;

    /**
     * 审核类型 1-平台审核 2-渠道审核
     */
    @Schema(description = "审核类型")
    private Byte auditType;

    /**
     * 审核状态 1-通过 2-拒绝
     */
    @Schema(description = "审核状态")
    private Byte auditStatus;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String auditOpinion;

    /**
     * 审核人
     */
    @Schema(description = "审核人")
    private String auditor;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private Date auditTime;

    /**
     * 审核类型常量
     */
    public interface AuditType {
        byte PLATFORM = 1;  // 平台审核
        byte CHANNEL = 2;   // 渠道审核
    }

    /**
     * 审核状态常量
     */
    public interface AuditStatus {
        byte APPROVED = 1;  // 通过
        byte REJECTED = 2;  // 拒绝
    }
}