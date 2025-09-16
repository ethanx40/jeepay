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

import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 进件材料上传控制器
 *
 * @author [jeepay]
 * @since 2024-01-01
 */
@Tag(name = "进件材料上传")
@Slf4j
@RestController
@RequestMapping("/api/mchApply/material")
public class MaterialUploadController extends CommonCtrl {

    // 允许的文件类型
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "application/pdf"
    );

    // 文件大小限制 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // 上传目录
    private static final String UPLOAD_DIR = "uploads/materials/";

    /**
     * 上传进件材料
     */
    @Operation(summary = "上传进件材料")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_SUBMIT')")
    @MethodLog(remark = "上传进件材料")
    @PostMapping("/upload")
    public ApiRes<MaterialUploadResult> uploadMaterial(
            @Parameter(description = "材料文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "材料类型") @RequestParam("materialType") String materialType) {

        try {
            // 验证文件
            validateFile(file);

            // 获取当前用户信息
            JeeUserDetails userDetails = getCurrentUser();
            String mchNo = userDetails.getSysUser().getBelongInfoId();

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String fileName = UUID.randomUUID().toString() + "." + fileExtension;

            // 创建上传目录
            String uploadPath = UPLOAD_DIR + mchNo + "/" + materialType + "/";
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 保存文件
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // 构建文件URL (这里简化处理，实际应该根据配置生成完整URL)
            String fileUrl = "/" + uploadPath + fileName;

            MaterialUploadResult result = new MaterialUploadResult();
            result.setFileUrl(fileUrl);
            result.setFileName(originalFilename);
            result.setFileSize(file.getSize());
            result.setFileType(fileExtension);
            result.setMaterialType(materialType);

            log.info("材料上传成功，商户号: {}, 材料类型: {}, 文件名: {}", mchNo, materialType, fileName);

            return ApiRes.ok(result);

        } catch (Exception e) {
            log.error("上传进件材料失败", e);
            return ApiRes.customFail("上传进件材料失败: " + e.getMessage());
        }
    }

    /**
     * 删除进件材料
     */
    @Operation(summary = "删除进件材料")
    @PreAuthorize("hasAuthority('ENT_MCH_APPLY_SUBMIT')")
    @MethodLog(remark = "删除进件材料")
    @DeleteMapping("/delete")
    public ApiRes<?> deleteMaterial(@RequestParam("fileUrl") String fileUrl) {
        try {
            // 获取当前用户信息
            JeeUserDetails userDetails = getCurrentUser();
            String mchNo = userDetails.getSysUser().getBelongInfoId();

            // 验证文件路径是否属于当前商户
            if (!fileUrl.contains("/" + mchNo + "/")) {
                return ApiRes.customFail("无权限删除该文件");
            }

            // 删除文件
            Path filePath = Paths.get(fileUrl.substring(1)); // 去掉开头的 "/"
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("材料删除成功，商户号: {}, 文件路径: {}", mchNo, fileUrl);
            }

            return ApiRes.ok();

        } catch (Exception e) {
            log.error("删除进件材料失败", e);
            return ApiRes.customFail("删除进件材料失败: " + e.getMessage());
        }
    }

    /**
     * 验证上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }

        String contentType = file.getContentType();
        if (!ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持JPG、PNG、PDF格式");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 材料上传结果
     */
    @Data
    public static class MaterialUploadResult {
        @Parameter(description = "文件URL")
        private String fileUrl;

        @Parameter(description = "文件名称")
        private String fileName;

        @Parameter(description = "文件大小")
        private Long fileSize;

        @Parameter(description = "文件类型")
        private String fileType;

        @Parameter(description = "材料类型")
        private String materialType;
    }
}