package com.ai_hub.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    
    /**
     * 上传头像到OSS
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像URL
     */
    String uploadAvatar(MultipartFile file, Long userId);
    
    /**
     * 删除OSS上的文件
     *
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
}
