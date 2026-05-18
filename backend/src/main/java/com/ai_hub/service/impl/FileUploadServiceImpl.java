package com.ai_hub.service.impl;

import com.ai_hub.config.OssConfig;
import com.ai_hub.service.FileUploadService;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {
    
    private final OssConfig ossConfig;
    
    @Autowired(required = false)
    private OSS ossClient;
    
    /**
     * 上传头像
     * 如果配置了OSS则上传到OSS，否则使用本地存储
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像URL
     */
    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        log.info("上传头像，用户ID: {}, 文件名: {}", userId, file.getOriginalFilename());
        
        // 1. 验证文件
        validateFile(file);
        
        // 2. 生成文件名
        String fileName = generateFileName(file.getOriginalFilename(), userId);
        
        // 3. 检查是否配置了OSS
        if (ossClient != null && ossConfig.isConfigured()) {
            return uploadToOss(file, fileName);
        } else {
            // 使用本地存储（开发环境）
            return uploadToLocal(file, fileName);
        }
    }
    
    /**
     * 上传到OSS
     */
    private String uploadToOss(MultipartFile file, String fileName) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            
            ossClient.putObject(
                ossConfig.getBucketName(),
                fileName,
                file.getInputStream(),
                metadata
            );
            
            // 返回文件URL
            String fileUrl = getOssFileUrl(fileName);
            log.info("头像上传到OSS成功，URL: {}", fileUrl);
            
            return fileUrl;
            
        } catch (IOException e) {
            log.error("头像上传到OSS失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }
    
    /**
     * 上传到本地存储（开发环境）
     */
    private String uploadToLocal(MultipartFile file, String fileName) {
        try {
            // 创建存储目录
            Path storagePath = Paths.get("uploads", "avatar");
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }
            
            // 保存文件
            Path filePath = storagePath.resolve(fileName.substring(fileName.lastIndexOf("/") + 1));
            Files.copy(file.getInputStream(), filePath);
            
            // 返回本地URL
            String fileUrl = "/uploads/avatar/" + filePath.getFileName();
            log.info("头像上传到本地成功，URL: {}", fileUrl);
            
            return fileUrl;
            
        } catch (IOException e) {
            log.error("头像上传到本地失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }
    
    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        // 检查是否配置了OSS
        if (ossClient != null && ossConfig.isConfigured()) {
            deleteFromOss(fileUrl);
        } else {
            deleteFromLocal(fileUrl);
        }
    }
    
    /**
     * 从OSS删除文件
     */
    private void deleteFromOss(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName != null && !fileName.isEmpty()) {
                ossClient.deleteObject(ossConfig.getBucketName(), fileName);
                log.info("从OSS删除文件成功: {}", fileName);
            }
        } catch (Exception e) {
            log.error("从OSS删除文件失败: {}", fileUrl, e);
        }
    }
    
    /**
     * 从本地删除文件
     */
    private void deleteFromLocal(String fileUrl) {
        try {
            // 提取文件名
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get("uploads", "avatar", fileName);
            Files.deleteIfExists(filePath);
            log.info("从本地删除文件成功: {}", fileName);
        } catch (Exception e) {
            log.error("从本地删除文件失败: {}", fileUrl, e);
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        // 检查文件大小（限制为5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("文件大小不能超过5MB");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只支持图片格式");
        }
        
        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!isValidImageExtension(extension)) {
                throw new RuntimeException("不支持的图片格式");
            }
        }
    }
    
    /**
     * 检查是否为有效的图片扩展名
     */
    private boolean isValidImageExtension(String extension) {
        return "jpg".equals(extension) || "jpeg".equals(extension) || 
               "png".equals(extension) || "gif".equals(extension) || 
               "webp".equals(extension);
    }
    
    /**
     * 生成文件名
     */
    private String generateFileName(String originalFilename, Long userId) {
        // 获取文件扩展名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 生成唯一文件名：avatar/{userId}/{uuid}.{extension}
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "avatar/" + userId + "/" + uuid + extension;
    }
    
    /**
     * 获取OSS文件URL
     */
    private String getOssFileUrl(String fileName) {
        if (ossConfig.getDomain() != null && !ossConfig.getDomain().isEmpty()) {
            // 使用自定义域名
            return ossConfig.getDomain() + "/" + fileName;
        } else {
            // 使用OSS默认域名
            return "https://" + ossConfig.getBucketName() + "." + 
                   ossConfig.getEndpoint() + "/" + fileName;
        }
    }
    
    /**
     * 从URL中提取文件名
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        // 查找bucket名称后的路径
        String bucketName = ossConfig.getBucketName();
        int bucketIndex = fileUrl.indexOf(bucketName);
        if (bucketIndex != -1) {
            int startIndex = bucketIndex + bucketName.length() + 1;
            if (startIndex < fileUrl.length()) {
                return fileUrl.substring(startIndex);
            }
        }
        
        return null;
    }
}
