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
import com.jeequan.jeepay.core.entity.ChannelApplyConfig;
import com.jeequan.jeepay.service.mapper.ChannelApplyConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 渠道进件配置表 服务实现类
 * </p>
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Service
public class ChannelApplyConfigService extends ServiceImpl<ChannelApplyConfigMapper, ChannelApplyConfig> {

    /**
     * 根据渠道代码查询配置
     */
    public ChannelApplyConfig getByChannelCode(String channelCode) {
        return getOne(ChannelApplyConfig.gw()
                .eq(ChannelApplyConfig::getChannelCode, channelCode)
                .eq(ChannelApplyConfig::getIsEnabled, 1));
    }

    /**
     * 查询所有启用的配置
     */
    public List<ChannelApplyConfig> listEnabled() {
        return list(ChannelApplyConfig.gw()
                .eq(ChannelApplyConfig::getIsEnabled, 1)
                .orderByAsc(ChannelApplyConfig::getChannelCode));
    }

    /**
     * 启用/禁用配置
     */
    public boolean updateEnabled(String configId, Byte isEnabled) {
        ChannelApplyConfig config = new ChannelApplyConfig();
        config.setConfigId(configId);
        config.setIsEnabled(isEnabled);
        return updateById(config);
    }
}