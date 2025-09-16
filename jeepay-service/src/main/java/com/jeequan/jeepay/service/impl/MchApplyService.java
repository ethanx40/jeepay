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

import com.alibaba.fastjson.JSON;
import com.jeequan.jeepay.core.entity.ChannelApplyConfig;
import com.jeequan.jeepay.core.entity.MchApplyMaterial;
import com.jeequan.jeepay.core.entity.MchApplyRecord;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.mchapply.MchApplyInfo;
import com.jeequan.jeepay.core.utils.SeqKit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商户进件业务服务
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Slf4j
@Service
public class MchApplyService {

    @Autowired
    private MchApplyRecordService mchApplyRecordService;

    @Autowired
    private MchApplyMaterialService mchApplyMaterialService;

    @Autowired
    private ChannelApplyConfigService channelApplyConfigService;

    /**
     * 提交进件申请
     */
    @Transactional(rollbackFor = Exception.class)
    public String submitApply(MchApplyInfo applyInfo) {
        // 1. 验证渠道配置
        ChannelApplyConfig config = channelApplyConfigService.getByChannelCode(applyInfo.getChannelCode());
        if (config == null) {
            throw new BizException("不支持的支付渠道");
        }

        // 2. 验证必要材料
        validateRequiredMaterials(applyInfo, config);

        // 3. 检查是否已存在进件申请
        MchApplyRecord existRecord = mchApplyRecordService.getByMchNoAndChannel(
                applyInfo.getMchNo(), applyInfo.getChannelCode());
        if (existRecord != null && existRecord.getApplyStatus() != MchApplyRecord.ApplyStatus.REJECTED) {
            throw new BizException("该渠道已存在进件申请，请勿重复提交");
        }

        // 4. 创建进件申请记录
        String applyId = SeqKit.genApplyId();
        MchApplyRecord applyRecord = new MchApplyRecord();
        applyRecord.setApplyId(applyId);
        applyRecord.setMchNo(applyInfo.getMchNo());
        applyRecord.setIsvNo(applyInfo.getIsvNo());
        applyRecord.setChannelCode(applyInfo.getChannelCode());
        applyRecord.setApplyStatus(MchApplyRecord.ApplyStatus.SUBMITTED);
        applyRecord.setApplyData(JSON.toJSONString(applyInfo));
        applyRecord.setSubmitTime(new Date());

        if (!mchApplyRecordService.save(applyRecord)) {
            throw new BizException("保存进件申请失败");
        }

        // 5. 保存申请材料
        saveMaterials(applyId, applyInfo.getMaterials());

        // 6. 提交到渠道 (暂时跳过，后续实现)
        log.info("进件申请提交成功，申请ID: {}", applyId);

        return applyId;
    }

    /**
     * 查询进件状态
     */
    public MchApplyRecord queryApplyStatus(String applyId) {
        MchApplyRecord record = mchApplyRecordService.getById(applyId);
        if (record == null) {
            throw new BizException("进件申请不存在");
        }
        return record;
    }

    /**
     * 验证必要材料
     */
    private void validateRequiredMaterials(MchApplyInfo applyInfo, ChannelApplyConfig config) {
        if (applyInfo.getMaterials() == null || applyInfo.getMaterials().isEmpty()) {
            throw new BizException("请上传必要的申请材料");
        }
    }

    /**
     * 保存申请材料
     */
    private void saveMaterials(String applyId, List<MchApplyInfo.MaterialInfo> materials) {
        if (materials == null || materials.isEmpty()) {
            return;
        }

        List<MchApplyMaterial> materialList = materials.stream().map(material -> {
            MchApplyMaterial entity = new MchApplyMaterial();
            entity.setMaterialId(SeqKit.genMaterialId());
            entity.setApplyId(applyId);
            entity.setMaterialType(material.getMaterialType());
            entity.setMaterialName(material.getMaterialName());
            entity.setFileUrl(material.getFileUrl());
            entity.setFileName(material.getFileName());
            entity.setIsRequired(material.getIsRequired() ? (byte) 1 : (byte) 0);
            entity.setUploadTime(new Date());
            return entity;
        }).collect(Collectors.toList());

        mchApplyMaterialService.saveBatch(materialList);
    }
}