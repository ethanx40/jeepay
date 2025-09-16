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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.MchApplyRecord;
import com.jeequan.jeepay.service.mapper.MchApplyRecordMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商户进件申请记录表 服务实现类
 * </p>
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Service
public class MchApplyRecordService extends ServiceImpl<MchApplyRecordMapper, MchApplyRecord> {

    /**
     * 分页查询商户进件申请记录
     */
    public IPage<MchApplyRecord> selectPage(IPage page, String mchNo, String channelCode, Byte applyStatus) {
        LambdaQueryWrapper<MchApplyRecord> wrapper = MchApplyRecord.gw();
        
        if (StringUtils.isNotBlank(mchNo)) {
            wrapper.eq(MchApplyRecord::getMchNo, mchNo);
        }
        
        if (StringUtils.isNotBlank(channelCode)) {
            wrapper.eq(MchApplyRecord::getChannelCode, channelCode);
        }
        
        if (applyStatus != null) {
            wrapper.eq(MchApplyRecord::getApplyStatus, applyStatus);
        }
        
        wrapper.orderByDesc(MchApplyRecord::getCreatedAt);
        
        return page(page, wrapper);
    }

    /**
     * 根据商户号和渠道代码查询进件记录
     */
    public MchApplyRecord getByMchNoAndChannel(String mchNo, String channelCode) {
        return getOne(MchApplyRecord.gw()
                .eq(MchApplyRecord::getMchNo, mchNo)
                .eq(MchApplyRecord::getChannelCode, channelCode)
                .orderByDesc(MchApplyRecord::getCreatedAt)
                .last("LIMIT 1"));
    }

    /**
     * 根据渠道申请单号查询进件记录
     */
    public MchApplyRecord getByChannelApplyId(String channelApplyId) {
        return getOne(MchApplyRecord.gw()
                .eq(MchApplyRecord::getChannelApplyId, channelApplyId));
    }

    /**
     * 更新申请状态
     */
    public boolean updateApplyStatus(String applyId, Byte applyStatus) {
        MchApplyRecord record = new MchApplyRecord();
        record.setApplyId(applyId);
        record.setApplyStatus(applyStatus);
        return updateById(record);
    }

    /**
     * 更新渠道申请单号
     */
    public boolean updateChannelApplyId(String applyId, String channelApplyId) {
        MchApplyRecord record = new MchApplyRecord();
        record.setApplyId(applyId);
        record.setChannelApplyId(channelApplyId);
        record.setApplyStatus(MchApplyRecord.ApplyStatus.CHANNEL_PROCESSING);
        return updateById(record);
    }
}