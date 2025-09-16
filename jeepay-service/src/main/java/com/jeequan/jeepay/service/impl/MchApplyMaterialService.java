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
package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.MchApplyMaterial;
import com.jeequan.jeepay.service.mapper.MchApplyMaterialMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 进件材料表 服务实现类
 * </p>
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Service
public class MchApplyMaterialService extends ServiceImpl<MchApplyMaterialMapper, MchApplyMaterial> {

    /**
     * 根据申请ID查询材料列表
     */
    public List<MchApplyMaterial> listByApplyId(String applyId) {
        return list(MchApplyMaterial.gw()
                .eq(MchApplyMaterial::getApplyId, applyId)
                .orderByAsc(MchApplyMaterial::getUploadTime));
    }

    /**
     * 根据申请ID和材料类型查询材料
     */
    public MchApplyMaterial getByApplyIdAndType(String applyId, String materialType) {
        return getOne(MchApplyMaterial.gw()
                .eq(MchApplyMaterial::getApplyId, applyId)
                .eq(MchApplyMaterial::getMaterialType, materialType));
    }

    /**
     * 删除申请的所有材料
     */
    public boolean removeByApplyId(String applyId) {
        return remove(MchApplyMaterial.gw()
                .eq(MchApplyMaterial::getApplyId, applyId));
    }

    /**
     * 批量保存材料
     */
    public boolean saveBatch(List<MchApplyMaterial> materials) {
        return super.saveBatch(materials);
    }
}