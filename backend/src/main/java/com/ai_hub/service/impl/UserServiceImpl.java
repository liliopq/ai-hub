package com.ai_hub.service.impl;

import com.ai_hub.dto.request.ChangePasswordRequest;
import com.ai_hub.dto.request.LoginRequest;
import com.ai_hub.dto.request.RegisterRequest;
import com.ai_hub.dto.request.UpdateUserRequest;
import com.ai_hub.dto.response.RegisterResponse;
import com.ai_hub.dto.response.UserResponse;
import com.ai_hub.entity.User;
import com.ai_hub.enums.ErrorCode;
import com.ai_hub.mapper.UserMapper;
import com.ai_hub.service.FileUploadService;
import com.ai_hub.service.UserService;
import com.ai_hub.utils.JwtTokenProvider;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;        // 密码编码器
    private final JwtTokenProvider jwtTokenProvider;      // JWT Token 提供者
    private final StringRedisTemplate redisTemplate;      // Redis 操作
    private final FileUploadService fileUploadService;    // 文件上传服务

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册响应（包含 JWT Token）
     */
    @Override
    public RegisterResponse register(RegisterRequest request) {
        log.info("用户注册，用户名: {}", request.getUsername());

        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User existingUser = userMapper.selectOne(queryWrapper);
        if (existingUser != null) {
            throw new RuntimeException(ErrorCode.USER_ALREADY_EXISTS.getMessage());
        }

        // 2. 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 密码加密
        user.setRole("USER"); // 默认角色为 USER
        user.setStatus(1); // 默认状态为正常

        // 3. 保存用户（createTime 和 updateTime 会自动填充）
        userMapper.insert(user);

        log.info("用户注册成功，用户ID: {}", user.getId());

        // 4. 生成 JWT Token（注册后自动登录）
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        // 5. 构建响应
        RegisterResponse response = new RegisterResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setCreateTime(user.getCreateTime());
        response.setToken(token); // 设置 Token

        return response;
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return JWT Token
     */
    @Override
    public String login(LoginRequest request) {
        log.info("用户登录，用户名: {}", request.getUsername());

        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();   // 查询条件
        queryWrapper.eq(User::getUsername, request.getUsername());            // 用户名条件
        User user = userMapper.selectOne(queryWrapper);                       // 查询用户

        // 2. 检查用户是否存在
        if (user == null) {
            throw new RuntimeException(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        // 3. 检查用户状态是否正常
        if (user.getStatus() == 0) {
            throw new RuntimeException(ErrorCode.ACCOUNT_DISABLED.getMessage());
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException(ErrorCode.USERNAME_OR_PASSWORD_ERROR.getMessage());
        }

        // 5. 生成 JWT Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        log.info("用户登录成功，用户ID: {}", user.getId());

        return token;
    }

    /**
     * 用户退出登录（将 Token 加入黑名单）
     *
     * @param token JWT Token
     */
    @Override
    public void logout(String token) {
        log.info("用户退出登录");

        // 1. 从 Token 中获取用户ID和过期时间
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        long expirationTime = jwtTokenProvider.getExpirationFromToken(token).getTime();
        long currentTime = System.currentTimeMillis();
        long remainingTime = expirationTime - currentTime;
        
        // 2. 将 Token 加入 Redis 黑名单，设置过期时间为 Token 的剩余有效期
        String blacklistKey = "jwt:blacklist:" + token;
        long ttl = Math.max(remainingTime, 60000); // 至少保留1分钟
        redisTemplate.opsForValue().set(blacklistKey, userId.toString(), ttl, TimeUnit.MILLISECONDS);
        
        log.info("Token 已加入黑名单，用户ID: {}, 剩余有效期: {}ms", userId, ttl);
    }

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public UserResponse getCurrentUser(Long userId) {
        log.info("获取用户信息，用户ID: {}", userId);

        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        // 2. 检查用户状态是否正常
        if (user.getStatus() == 0) {
            throw new RuntimeException(ErrorCode.ACCOUNT_DISABLED.getMessage());
        }

        // 3. 构建响应
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());

        return response;
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        log.info("更新用户信息，用户ID: {}", userId);

        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        // 2. 检查用户状态是否正常
        if (user.getStatus() == 0) {
            throw new RuntimeException(ErrorCode.ACCOUNT_DISABLED.getMessage());
        }

        // 3. 部分更新用户信息（只更新非空字段）
        if (StringUtils.hasText(request.getUsername())) {
            // 检查新用户名是否已被其他用户使用
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, request.getUsername())
                    .ne(User::getId, userId);
            User existingUser = userMapper.selectOne(queryWrapper);
            if (existingUser != null) {
                throw new RuntimeException(ErrorCode.USER_ALREADY_EXISTS.getMessage());
            }
            user.setUsername(request.getUsername());
        }

        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (StringUtils.hasText(request.getAvatar())) {
            // 删除旧头像
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                fileUploadService.deleteFile(user.getAvatar());
            }
            user.setAvatar(request.getAvatar());
        }

        // 4. 保存更新（updateTime 会自动填充）
        userMapper.updateById(user);

        log.info("用户信息更新成功，用户ID: {}", userId);

        // 5. 构建响应
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());

        return response;
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param request 修改密码请求
     */
    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("修改密码，用户ID: {}", userId);

        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        // 2. 检查用户状态是否正常
        if (user.getStatus() == 0) {
            throw new RuntimeException(ErrorCode.ACCOUNT_DISABLED.getMessage());
        }

        // 3. 验证原密码是否正确
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException(ErrorCode.OLD_PASSWORD_ERROR.getMessage());
        }

        // 4. 检查新密码是否与原密码相同
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        // 5. 加密并更新新密码（updateTime 会自动填充）
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        log.info("密码修改成功，用户ID: {}", userId);
    }

    /**
     * 获取指定用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public UserResponse getUserById(Long userId) {
        log.info("获取用户信息，用户ID: {}", userId);

        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        // 2. 检查用户状态是否正常
        if (user.getStatus() == 0) {
            throw new RuntimeException(ErrorCode.ACCOUNT_DISABLED.getMessage());
        }

        // 3. 构建响应（只返回公开信息）
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setCreateTime(user.getCreateTime());

        return response;
    }
}
