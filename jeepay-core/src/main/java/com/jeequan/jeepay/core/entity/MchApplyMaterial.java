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
 * 进件材料表
 * </p>
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Schema(description = "进件材料表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_apply_material")
public class MchApplyMaterial extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<MchApplyMaterial> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    /**
     * 材料ID
     */
    @Schema(description = "材料ID")
    @TableId
    private String materialId;

    /**
     * 申请ID
     */
    @Schema(description = "申请ID")
    private String applyId;

    /**
     * 材料类型
     */
    @Schema(description = "材料类型")
    private String materialType;

    /**
     * 材料名称
     */
    @Schema(description = "材料名称")
    private String materialName;

    /**
     * 文件URL
     */
    @Schema(description = "文件URL")
    private String fileUrl;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 文件大小
     */
    @Schema(description = "文件大小")
    private Long fileSize;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型")
    private String fileType;

    /**
     * 是否必需 0-否 1-是
     */
    @Schema(description = "是否必需")
    private Byte isRequired;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    private Date uploadTime;

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
     * 材料类型常量
     */
    public interface MaterialType {
        String BUSINESS_LICENSE = "BUSINESS_LICENSE";  // 营业执照
        String ID_CARD_FRONT = "ID_CARD_FRONT";       // 身份证正面
        String ID_CARD_BACK = "ID_CARD_BACK";         // 身份证反面
        String BANK_ACCOUNT = "BANK_ACCOUNT";         // 银行开户许可证
        String ORGANIZATION_CODE = "ORGANIZATION_CODE"; // 组织机构代码证
        String TAX_REGISTRATION = "TAX_REGISTRATION";   // 税务登记证
        String LEGAL_PERSON_PHOTO = "LEGAL_PERSON_PHOTO"; // 法人照片
        String STORE_PHOTO = "STORE_PHOTO";           // 门店照片
    }

    /**
     * 文件类型常量
     */
    public interface FileType {
        String JPG = "jpg";
        String PNG = "png";
        String PDF = "pdf";
        String JPEG = "jpeg";
    }
}