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
package com.jeequan.jeepay.mch.ctrl.apply;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.ChannelApplyConfig;
import com.jeequan.jeepay.core.entity.MchApplyRecord;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.params.mchapply.MchApplyInfo;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.ChannelApplyConfigService;
import com.jeequan.jeepay.service.impl.MchApplyRecordService;
import com.jeequan.jeepay.service.impl.MchApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户进件申请控制器
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Tag(name = "商户进件管理")
@Slf4j
@RestController
@RequestMapping("/api/mchApply")
public class MchApplyController extends CommonCtrl {

    @Autowired
    private MchApplyService mchApplyService;

    @Autowired
    private MchApplyRecordService mchApplyRecordService;

    @Autowired
    private ChannelApplyConfigService channelApplyConfigService;

    /**
     * 获取支持的渠道配置列表
     */
    @Operation(summary = "获取支持的渠道配置列表")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_QUERY')")
    @GetMapping("/channels")
    public ApiRes<List<ChannelApplyConfig>> listChannels() {
        List<ChannelApplyConfig> configs = channelApplyConfigService.listEnabled();
        return ApiRes.ok(configs);
    }

    /**
     * 提交进件申请
     */
    @Operation(summary = "提交进件申请")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_SUBMIT')")
    @MethodLog(remark = "提交进件申请")
    @PostMapping("/submit")
    public ApiRes<String> submitApply(@RequestBody MchApplyInfo applyInfo) {
        try {
            // 设置当前商户号
            JeeUserDetails userDetails = getCurrentUser();
            applyInfo.setMchNo(userDetails.getSysUser().getBelongInfoId());
            
            String applyId = mchApplyService.submitApply(applyInfo);
            return ApiRes.ok(applyId);
        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());
        } catch (Exception e) {
            log.error("提交进件申请失败", e);
            return ApiRes.customFail("提交进件申请失败");
        }
    }

    /**
     * 查询进件申请列表
     */
    @Operation(summary = "查询进件申请列表")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_QUERY')")
    @GetMapping("/list")
    public ApiRes<IPage<MchApplyRecord>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNumber,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "渠道代码") @RequestParam(required = false) String channelCode,
            @Parameter(description = "申请状态") @RequestParam(required = false) Byte applyStatus) {

        // 获取当前商户号
        JeeUserDetails userDetails = getCurrentUser();
        String mchNo = userDetails.getSysUser().getBelongInfoId();

        IPage<MchApplyRecord> pages = mchApplyRecordService.selectPage(
                new Page<>(pageNumber, pageSize), mchNo, channelCode, applyStatus);

        return ApiRes.ok(pages);
    }

    /**
     * 查询进件申请详情
     */
    @Operation(summary = "查询进件申请详情")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_QUERY')")
    @GetMapping("/{applyId}")
    public ApiRes<MchApplyRecord> detail(@PathVariable String applyId) {
        try {
            MchApplyRecord record = mchApplyService.queryApplyStatus(applyId);
            
            // 验证是否为当前商户的申请
            JeeUserDetails userDetails = getCurrentUser();
            String mchNo = userDetails.getSysUser().getBelongInfoId();
            if (!mchNo.equals(record.getMchNo())) {
                return ApiRes.customFail("无权限查看该申请");
            }
            
            return ApiRes.ok(record);
        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());
        } catch (Exception e) {
            log.error("查询进件申请详情失败", e);
            return ApiRes.customFail("查询进件申请详情失败");
        }
    }

    /**
     * 取消进件申请
     */
    @Operation(summary = "取消进件申请")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_SUBMIT')")
    @MethodLog(remark = "取消进件申请")
    @PutMapping("/{applyId}/cancel")
    public ApiRes<?> cancelApply(@PathVariable String applyId) {
        try {
            MchApplyRecord record = mchApplyRecordService.getById(applyId);
            if (record == null) {
                return ApiRes.customFail("进件申请不存在");
            }

            // 验证是否为当前商户的申请
            JeeUserDetails userDetails = getCurrentUser();
            String mchNo = userDetails.getSysUser().getBelongInfoId();
            if (!mchNo.equals(record.getMchNo())) {
                return ApiRes.customFail("无权限操作该申请");
            }

            // 只有草稿和已提交状态可以取消
            if (record.getApplyStatus() != MchApplyRecord.ApplyStatus.DRAFT 
                && record.getApplyStatus() != MchApplyRecord.ApplyStatus.SUBMITTED) {
                return ApiRes.customFail("当前状态不允许取消");
            }

            boolean success = mchApplyRecordService.updateApplyStatus(applyId, MchApplyRecord.ApplyStatus.CANCELLED);
            if (success) {
                return ApiRes.ok();
            } else {
                return ApiRes.customFail("取消进件申请失败");
            }
        } catch (Exception e) {
            log.error("取消进件申请失败", e);
            return ApiRes.customFail("取消进件申请失败");
        }
    }
}