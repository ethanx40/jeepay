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
 * 渠道进件配置表
 * </p>
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Schema(description = "渠道进件配置表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_channel_apply_config")
public class ChannelApplyConfig extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<ChannelApplyConfig> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @Schema(description = "配置ID")
    @TableId
    private String configId;

    /**
     * 渠道代码
     */
    @Schema(description = "渠道代码")
    private String channelCode;

    /**
     * 配置名称
     */
    @Schema(description = "配置名称")
    private String configName;

    /**
     * 必需材料JSON
     */
    @Schema(description = "必需材料JSON")
    private String requiredMaterials;

    /**
     * 配置参数JSON
     */
    @Schema(description = "配置参数JSON")
    private String configParams;

    /**
     * 是否启用 0-否 1-是
     */
    @Schema(description = "是否启用")
    private Byte isEnabled;

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
}