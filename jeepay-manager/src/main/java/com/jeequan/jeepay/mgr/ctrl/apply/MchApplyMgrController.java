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
package com.jeequan.jeepay.mgr.ctrl.apply;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.entity.ChannelApplyConfig;
import com.jeequan.jeepay.core.entity.MchApplyAuditRecord;
import com.jeequan.jeepay.core.entity.MchApplyMaterial;
import com.jeequan.jeepay.core.entity.MchApplyRecord;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.ChannelApplyConfigService;
import com.jeequan.jeepay.service.impl.MchApplyMaterialService;
import com.jeequan.jeepay.service.impl.MchApplyRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 商户进件管理控制器（管理端）
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Tag(name = "商户进件管理（管理端）")
@Slf4j
@RestController
@RequestMapping("/api/mchApply")
public class MchApplyMgrController extends CommonCtrl {

    @Autowired
    private MchApplyRecordService mchApplyRecordService;

    @Autowired
    private MchApplyMaterialService mchApplyMaterialService;

    @Autowired
    private ChannelApplyConfigService channelApplyConfigService;

    /**
     * 查询进件申请列表
     */
    @Operation(summary = "查询进件申请列表")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_VIEW')")
    @GetMapping("/list")
    public ApiRes<IPage<MchApplyRecord>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNumber,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "商户号") @RequestParam(required = false) String mchNo,
            @Parameter(description = "渠道代码") @RequestParam(required = false) String channelCode,
            @Parameter(description = "申请状态") @RequestParam(required = false) Byte applyStatus) {

        IPage<MchApplyRecord> pages = mchApplyRecordService.selectPage(
                new Page<>(pageNumber, pageSize), mchNo, channelCode, applyStatus);

        return ApiRes.ok(pages);
    }

    /**
     * 查询进件申请详情
     */
    @Operation(summary = "查询进件申请详情")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_VIEW')")
    @GetMapping("/{applyId}")
    public ApiRes<MchApplyDetailVO> detail(@PathVariable String applyId) {
        try {
            MchApplyRecord record = mchApplyRecordService.getById(applyId);
            if (record == null) {
                return ApiRes.customFail("进件申请不存在");
            }

            // 查询申请材料
            List<MchApplyMaterial> materials = mchApplyMaterialService.listByApplyId(applyId);

            MchApplyDetailVO detailVO = new MchApplyDetailVO();
            detailVO.setApplyRecord(record);
            detailVO.setMaterials(materials);

            return ApiRes.ok(detailVO);
        } catch (Exception e) {
            log.error("查询进件申请详情失败", e);
            return ApiRes.customFail("查询进件申请详情失败");
        }
    }

    /**
     * 审核进件申请
     */
    @Operation(summary = "审核进件申请")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_AUDIT')")
    @MethodLog(remark = "审核进件申请")
    @PostMapping("/{applyId}/audit")
    public ApiRes<?> auditApply(@PathVariable String applyId, @RequestBody AuditRequest request) {
        try {
            MchApplyRecord record = mchApplyRecordService.getById(applyId);
            if (record == null) {
                return ApiRes.customFail("进件申请不存在");
            }

            // 只有已提交状态可以审核
            if (record.getApplyStatus() != MchApplyRecord.ApplyStatus.SUBMITTED) {
                return ApiRes.customFail("当前状态不允许审核");
            }

            // 获取当前用户
            JeeUserDetails userDetails = getCurrentUser();
            String auditor = userDetails.getSysUser().getRealname();

            // 创建审核记录
            MchApplyAuditRecord auditRecord = new MchApplyAuditRecord();
            auditRecord.setAuditId(SeqKit.genApplyId()); // 复用申请ID生成方法
            auditRecord.setApplyId(applyId);
            auditRecord.setAuditType(MchApplyAuditRecord.AuditType.PLATFORM);
            auditRecord.setAuditStatus(request.getAuditStatus());
            auditRecord.setAuditOpinion(request.getAuditOpinion());
            auditRecord.setAuditor(auditor);
            auditRecord.setAuditTime(new Date());

            // 更新申请状态
            Byte newStatus = request.getAuditStatus() == MchApplyAuditRecord.AuditStatus.APPROVED 
                    ? MchApplyRecord.ApplyStatus.APPROVED 
                    : MchApplyRecord.ApplyStatus.REJECTED;

            boolean success = mchApplyRecordService.updateApplyStatus(applyId, newStatus);
            if (success) {
                // TODO: 保存审核记录到数据库
                // auditRecordService.save(auditRecord);
                
                // TODO: 如果审核通过，可以触发渠道进件流程
                if (request.getAuditStatus() == MchApplyAuditRecord.AuditStatus.APPROVED) {
                    log.info("进件申请审核通过，可以开始渠道进件流程，申请ID: {}", applyId);
                }
                
                return ApiRes.ok();
            } else {
                return ApiRes.customFail("审核进件申请失败");
            }
        } catch (Exception e) {
            log.error("审核进件申请失败", e);
            return ApiRes.customFail("审核进件申请失败");
        }
    }

    /**
     * 渠道配置管理
     */
    @Operation(summary = "获取渠道配置列表")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_CONFIG')")
    @GetMapping("/config/list")
    public ApiRes<List<ChannelApplyConfig>> listConfigs() {
        List<ChannelApplyConfig> configs = channelApplyConfigService.list();
        return ApiRes.ok(configs);
    }

    /**
     * 更新渠道配置
     */
    @Operation(summary = "更新渠道配置")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_CONFIG')")
    @MethodLog(remark = "更新渠道配置")
    @PutMapping("/config/{configId}")
    public ApiRes<?> updateConfig(@PathVariable String configId, @RequestBody ChannelApplyConfig config) {
        try {
            config.setConfigId(configId);
            boolean success = channelApplyConfigService.updateById(config);
            if (success) {
                return ApiRes.ok();
            } else {
                return ApiRes.customFail("更新渠道配置失败");
            }
        } catch (Exception e) {
            log.error("更新渠道配置失败", e);
            return ApiRes.customFail("更新渠道配置失败");
        }
    }

    /**
     * 启用/禁用渠道配置
     */
    @Operation(summary = "启用/禁用渠道配置")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_CONFIG')")
    @MethodLog(remark = "启用/禁用渠道配置")
    @PutMapping("/config/{configId}/enabled")
    public ApiRes<?> updateConfigEnabled(@PathVariable String configId, @RequestParam Byte isEnabled) {
        try {
            boolean success = channelApplyConfigService.updateEnabled(configId, isEnabled);
            if (success) {
                return ApiRes.ok();
            } else {
                return ApiRes.customFail("更新渠道配置状态失败");
            }
        } catch (Exception e) {
            log.error("更新渠道配置状态失败", e);
            return ApiRes.customFail("更新渠道配置状态失败");
        }
    }

    /**
     * 进件申请详情VO
     */
    @Data
    public static class MchApplyDetailVO {
        private MchApplyRecord applyRecord;
        private List<MchApplyMaterial> materials;
    }

    /**
     * 审核请求
     */
    @Data
    public static class AuditRequest {
        @Parameter(description = "审核状态 1-通过 2-拒绝")
        private Byte auditStatus;
        
        @Parameter(description = "审核意见")
        private String auditOpinion;
    }
}