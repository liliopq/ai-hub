package com.ai_hub.controller;

import com.ai_hub.dto.response.Result;
import com.ai_hub.service.FileUploadService;
import com.ai_hub.utils.TokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    
    private final FileUploadService fileUploadService;
    private final TokenValidator tokenValidator;
    
    /**
     * 上传头像
     *
     * @param file 头像文件
     * @param authorization Authorization头
     * @return 上传结果
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorization) {
        
        log.info("上传头像请求");
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 上传头像
        String avatarUrl = fileUploadService.uploadAvatar(file, userId);
        
        // 构建响应
        Map<String, String> data = new HashMap<>();
        data.put("url", avatarUrl);
        
        return Result.success("上传成功", data);
    }
}
