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
package com.jeequan.jeepay.service.impl.mchapply;

import com.jeequan.jeepay.core.model.params.mchapply.ChannelApplyResult;
import com.jeequan.jeepay.core.model.params.mchapply.MchApplyInfo;

/**
 * 渠道进件服务接口
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
public interface IChannelApplyService {

    /**
     * 获取渠道代码
     */
    String getChannelCode();

    /**
     * 提交进件申请到渠道
     */
    ChannelApplyResult submitToChannel(MchApplyInfo applyInfo);

    /**
     * 查询渠道进件状态
     */
    ChannelApplyResult queryChannelStatus(String channelApplyId);

    /**
     * 处理渠道回调通知
     */
    ChannelApplyResult handleChannelNotify(String notifyData);
}