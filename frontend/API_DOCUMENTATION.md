# AI Hub Community 前端接口文档

> 版本：v1.0  
> 更新时间：2026-05-16  
> Base URL: `http://localhost:8080/api`

---

## 目录

- [1. 通用说明](#1-通用说明)
- [2. 认证模块 (/auth)](#2-认证模块-auth)
- [3. 用户模块 (/user)](#3-用户模块-user)
- [4. 文件上传模块 (/file)](#4-文件上传模块-file)
- [5. 帖子模块 (/post)](#5-帖子模块-post)
- [6. 评论模块 (/comment)](#6-评论模块-comment)
- [7. AI聊天模块 (/ai)](#7-ai聊天模块-ai)
- [8. 通知模块 (/notification)](#8-通知模块-notification)
- [9. 管理员模块 (/admin)](#9-管理员模块-admin)
- [10. 错误码说明](#10-错误码说明)

---

## 1. 通用说明

### 1.1 请求头规范

所有需要认证的接口都需要在请求头中携带 JWT Token：

```
Authorization: Bearer <your_jwt_token>
```

### 1.2 统一响应格式

所有接口都返回统一的 JSON 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1716534600000
}
```

**字段说明：**
- `code`: 状态码（200 表示成功，其他表示失败）
- `message`: 响应消息
- `data`: 响应数据（可能为 null、对象或数组）
- `timestamp`: 时间戳

### 1.3 分页响应格式

分页接口返回的数据结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1716534600000
}
```

**字段说明：**
- `records`: 当前页的数据列表
- `total`: 总记录数
- `size`: 每页大小
- `current`: 当前页码
- `pages`: 总页数

---

## 2. 认证模块 (/auth)

### 2.1 用户注册

**接口地址：** `POST /api/auth/register`

**是否需要登录：** ❌ 否

**请求体：**
```json
{
  "username": "张三",
  "email": "zhangsan@example.com",
  "password": "123456"
}
```

**字段说明：**
- `username`: 用户名（必填，2-20个字符）
- `email`: 邮箱（必填，有效的邮箱格式）
- `password`: 密码（必填，至少6个字符）

**响应示例：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "timestamp": 1716534600000
}
```

---

### 2.2 用户登录

**接口地址：** `POST /api/auth/login`

**是否需要登录：** ❌ 否

**请求体：**
```json
{
  "username": "张三",
  "password": "123456"
}
```

**字段说明：**
- `username`: 用户名或邮箱（必填）
- `password`: 密码（必填）

**响应示例：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "timestamp": 1716534600000
}
```

**注意：** 登录成功后，将返回的 token 保存到本地存储，后续请求需要在 Authorization 头中携带。

---

### 2.3 退出登录

**接口地址：** `POST /api/auth/logout`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null,
  "timestamp": 1716534600000
}
```

---

## 3. 用户模块 (/user)

### 3.1 获取当前用户信息

**接口地址：** `GET /api/user/me`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "张三",
    "email": "zhangsan@example.com",
    "phoneNumber": null,
    "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/xxx.jpg",
    "role": "USER",
    "status": 1,
    "createTime": "2026-05-16T10:00:00"
  },
  "timestamp": 1716534600000
}
```

**字段说明：**
- `id`: 用户ID
- `username`: 用户名
- `email`: 邮箱
- `phoneNumber`: 手机号（可能为null）
- `avatar`: 头像URL
- `role`: 角色（USER/ADMIN）
- `status`: 状态（1正常，0禁用）
- `createTime`: 注册时间

---

### 3.2 更新用户信息

**接口地址：** `PUT /api/user/me`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
```

**请求体：**
```json
{
  "username": "李四",
  "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/yyy.jpg",
  "phoneNumber": "13800138000"
}
```

**字段说明：**
- `username`: 新用户名（可选）
- `avatar`: 新头像URL（可选）
- `phoneNumber`: 新手机号（可选）

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "李四",
    "email": "zhangsan@example.com",
    "phoneNumber": "13800138000",
    "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/yyy.jpg",
    "role": "USER",
    "status": 1,
    "createTime": "2026-05-16T10:00:00"
  },
  "timestamp": 1716534600000
}
```

---

### 3.3 修改密码

**接口地址：** `PUT /api/user/me/password`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
```

**请求体：**
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

**字段说明：**
- `oldPassword`: 原密码（必填）
- `newPassword`: 新密码（必填，至少6个字符）

**响应示例：**
```json
{
  "code": 200,
  "message": "密码修改成功，请重新登录",
  "data": null,
  "timestamp": 1716534600000
}
```

---

## 4. 文件上传模块 (/file)

### 4.1 上传头像

**接口地址：** `POST /api/file/avatar`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**请求体：** FormData 格式
```
file: <图片文件>
```

**字段说明：**
- `file`: 头像图片文件（必填，支持 jpg/png/gif，最大5MB）

**响应示例：**
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/1234567890.jpg"
  },
  "timestamp": 1716534600000
}
```

**使用流程：**
1. 调用此接口上传图片
2. 获取返回的 URL
3. 调用"更新用户信息"接口，将 URL 设置为 avatar 字段

---

## 5. 帖子模块 (/post)

### 5.1 发布帖子

**接口地址：** `POST /api/post`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**
```json
{
  "title": "我的第一篇帖子",
  "content": "这是帖子的内容...",
  "category": "技术分享",
  "tags": ["Java", "Spring Boot"]
}
```

**字段说明：**
- `title`: 标题（必填，1-100个字符）
- `content`: 内容（必填）
- `category`: 分类（必填）
- `tags`: 标签数组（可选）

**响应示例：**
```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": 1,
    "title": "我的第一篇帖子",
    "content": "这是帖子的内容...",
    "category": "技术分享",
    "tags": ["Java", "Spring Boot"],
    "viewCount": 0,
    "likeCount": 0,
    "commentCount": 0,
    "isSticky": 0,
    "isEssence": 0,
    "status": 1,
    "createTime": "2026-05-16T10:00:00",
    "updateTime": "2026-05-16T10:00:00"
  },
  "timestamp": 1716534600000
}
```

---

### 5.2 获取帖子列表

**接口地址：** `GET /api/post/list`

**是否需要登录：** ❌ 否

**查询参数：**
- `page`: 页码（可选，默认1）
- `size`: 每页条数（可选，默认10）
- `category`: 分类筛选（可选）
- `sortBy`: 排序方式（可选，默认time，可选值：time/hot）

**请求示例：**
```
GET /api/post/list?page=1&size=10&category=技术分享&sortBy=hot
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "我的第一篇帖子",
        "category": "技术分享",
        "tags": ["Java", "Spring Boot"],
        "viewCount": 100,
        "likeCount": 10,
        "commentCount": 5,
        "createTime": "2026-05-16T10:00:00",
        "user": {
          "id": 1,
          "username": "张三",
          "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/xxx.jpg"
        }
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  },
  "timestamp": 1716534600000
}
```

**字段说明：**
- `records`: 帖子列表
  - `id`: 帖子ID
  - `title`: 标题
  - `category`: 分类
  - `tags`: 标签数组
  - `viewCount`: 浏览数
  - `likeCount`: 点赞数
  - `commentCount`: 评论数
  - `createTime`: 创建时间
  - `user`: 作者信息
    - `id`: 用户ID
    - `username`: 用户名
    - `avatar`: 头像URL

---

### 5.3 获取帖子详情

**接口地址：** `GET /api/post/{postId}`

**是否需要登录：** ❌ 否（登录后可以看到更多交互状态）

**路径参数：**
- `postId`: 帖子ID

**请求头（可选）：**
```
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "我的第一篇帖子",
    "content": "这是帖子的详细内容...",
    "category": "技术分享",
    "tags": ["Java", "Spring Boot"],
    "viewCount": 101,
    "likeCount": 10,
    "commentCount": 5,
    "isSticky": 0,
    "isEssence": 0,
    "status": 1,
    "createTime": "2026-05-16T10:00:00",
    "updateTime": "2026-05-16T10:00:00",
    "user": {
      "id": 1,
      "username": "张三",
      "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/xxx.jpg"
    },
    "isLiked": true,
    "isCollected": false
  },
  "timestamp": 1716534600000
}
```

**字段说明：**
- 基础字段同帖子列表
- `content`: 帖子详细内容
- `isSticky`: 是否置顶（0否，1是）
- `isEssence`: 是否精华（0否，1是）
- `user`: 作者信息
- `isLiked`: 当前用户是否已点赞（未登录时为false）
- `isCollected`: 当前用户是否已收藏（未登录时为false）

---

### 5.4 更新帖子

**接口地址：** `PUT /api/post/{postId}`

**是否需要登录：** ✅ 是（仅帖主或管理员可操作）

**路径参数：**
- `postId`: 帖子ID

**请求头：**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**
```json
{
  "title": "更新后的标题",
  "content": "更新后的内容...",
  "category": "新技术",
  "tags": ["Vue", "React"]
}
```

**字段说明：** 所有字段都是可选的，只更新提供的字段

**响应示例：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "title": "更新后的标题",
    "content": "更新后的内容...",
    "category": "新技术",
    "tags": ["Vue", "React"],
    "viewCount": 101,
    "likeCount": 10,
    "commentCount": 5,
    "isSticky": 0,
    "isEssence": 0,
    "status": 1,
    "createTime": "2026-05-16T10:00:00",
    "updateTime": "2026-05-16T11:00:00",
    "user": {
      "id": 1,
      "username": "张三",
      "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/xxx.jpg"
    },
    "isLiked": true,
    "isCollected": false
  },
  "timestamp": 1716534600000
}
```

---

### 5.5 删除帖子

**接口地址：** `DELETE /api/post/{postId}`

**是否需要登录：** ✅ 是（仅帖主或管理员可操作）

**路径参数：**
- `postId`: 帖子ID

**请求头：**
```
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1716534600000
}
```

**注意：** 这是软删除，帖子不会真正从数据库中删除，只是标记为已删除状态。

---

### 5.6 点赞/取消点赞帖子

**接口地址：** `POST /api/post/{postId}/like`

**是否需要登录：** ✅ 是

**路径参数：**
- `postId`: 帖子ID

**请求头：**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**
```json
{
  "action": "like"
}
```

**字段说明：**
- `action`: 操作类型（必填，可选值：like/unlike）
  - `like`: 点赞
  - `unlike`: 取消点赞

**响应示例（点赞）：**
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": {
    "likeCount": 11,
    "isLiked": true
  },
  "timestamp": 1716534600000
}
```

**响应示例（取消点赞）：**
```json
{
  "code": 200,
  "message": "取消点赞成功",
  "data": {
    "likeCount": 10,
    "isLiked": false
  },
  "timestamp": 1716534600000
}
```

---

### 5.7 收藏/取消收藏帖子

**接口地址：** `POST /api/post/{postId}/collect`

**是否需要登录：** ✅ 是

**路径参数：**
- `postId`: 帖子ID

**请求头：**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**
```json
{
  "action": "collect"
}
```

**字段说明：**
- `action`: 操作类型（必填，可选值：collect/uncollect）
  - `collect`: 收藏
  - `uncollect`: 取消收藏

**响应示例（收藏）：**
```json
{
  "code": 200,
  "message": "收藏成功",
  "data": {
    "collectCount": 6,
    "isCollected": true
  },
  "timestamp": 1716534600000
}
```

**响应示例（取消收藏）：**
```json
{
  "code": 200,
  "message": "取消收藏成功",
  "data": {
    "collectCount": 5,
    "isCollected": false
  },
  "timestamp": 1716534600000
}
```

---

## 6. 评论模块 (/comment)

### 6.1 发表评论（或回复评论）

**接口地址：** `POST /api/comment`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**
```json
{
  "postId": 1,
  "parentId": 0,
  "content": "非常棒的文章！"
}
```

**字段说明：**
- `postId`: 帖子ID（必填）
- `parentId`: 父评论ID（必填，0表示顶层评论，非0表示回复某条评论）
- `content`: 评论内容（必填，最多500个字符）

**响应示例：**
```json
{
  "code": 200,
  "message": "评论成功",
  "data": {
    "id": 501,
    "content": "非常棒的文章！",
    "user": {
      "id": 1,
      "username": "张三",
      "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/xxx.jpg"
    },
    "createTime": "2026-05-16T10:30:00"
  },
  "timestamp": 1716534600000
}
```

---

### 6.2 获取帖子的评论树

**接口地址：** `GET /api/comment/list/{postId}`

**是否需要登录：** ❌ 否

**路径参数：**
- `postId`: 帖子ID

**查询参数：**
- `page`: 页码（可选，默认1）
- `size`: 每页条数（可选，默认20）

**请求示例：**
```
GET /api/comment/list/1?page=1&size=20
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 501,
        "content": "好文",
        "user": {
          "id": 1,
          "username": "张三",
          "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/xxx.jpg"
        },
        "likeCount": 5,
        "createTime": "2026-05-16T10:30:00",
        "replies": [
          {
            "id": 502,
            "content": "谢谢支持",
            "user": {
              "id": 2,
              "username": "李四",
              "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/yyy.jpg"
            },
            "likeCount": 2,
            "createTime": "2026-05-16T10:35:00",
            "replies": []
          }
        ]
      }
    ],
    "total": 10,
    "size": 20,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1716534600000
}
```

**字段说明：**
- `records`: 顶层评论列表
  - `id`: 评论ID
  - `content`: 评论内容
  - `user`: 评论者信息
    - `id`: 用户ID
    - `username`: 用户名
    - `avatar`: 头像URL
  - `likeCount`: 点赞数
  - `createTime`: 创建时间
  - `replies`: 回复列表（子评论数组）
    - 结构与顶层评论相同，但 replies 为空数组

**注意：** 
- 只对顶层评论进行分页
- 每个顶层评论的所有回复都会一次性返回
- 回复不再包含子回复（避免递归过深）

---

### 6.3 删除评论

**接口地址：** `DELETE /api/comment/{commentId}`

**是否需要登录：** ✅ 是（仅评论作者或管理员可操作）

**路径参数：**
- `commentId`: 评论ID

**请求头：**
```
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1716534600000
}
```

**注意：** 这是软删除，评论不会真正从数据库中删除。

---

### 6.4 点赞/取消点赞评论

**接口地址：** `POST /api/comment/{commentId}/like`

**是否需要登录：** ✅ 是

**路径参数：**
- `commentId`: 评论ID

**请求头：**
```
Authorization: Bearer <token>
```

**响应示例（点赞）：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "likeCount": 6
  },
  "timestamp": 1716534600000
}
```

**响应示例（取消点赞）：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "likeCount": 5
  },
  "timestamp": 1716534600000
}
```

**注意：** 该接口自动判断当前状态，第一次调用为点赞，再次调用为取消点赞。

---

## 7. AI聊天模块 (/ai)

### 7.1 与AI对话

**接口地址：** `POST /api/ai/chat`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**
```json
{
  "message": "你好，请介绍一下自己",
  "sessionId": "session_123456" // 可选，不传则自动创建新会话
}
```

**字段说明：**
- `message`: 用户消息内容（必填）
- `sessionId`: 会话ID（可选，不传则自动创建新会话）

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reply": "你好！我是AI助手，很高兴为您服务。我可以帮助您解答问题、提供建议等。",
    "sessionId": "session_123456",
    "timestamp": "2026-05-17T10:00:00"
  },
  "timestamp": 1716534600000
}
```

---

### 7.2 获取用户的AI会话列表

**接口地址：** `GET /api/ai/sessions`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "sessionId": "session_123456",
      "title": "关于Java开发的讨论",
      "lastUpdate": "2026-05-17T10:00:00"
    },
    {
      "sessionId": "session_789012",
      "title": "Spring Boot相关问题",
      "lastUpdate": "2026-05-16T15:30:00"
    }
  ],
  "timestamp": 1716534600000
}
```

**字段说明：**
- `sessionId`: 会话ID
- `title`: 会话标题
- `lastUpdate`: 最后更新时间

---

### 7.3 删除AI会话

**接口地址：** `DELETE /api/ai/session/{sessionId}`

**是否需要登录：** ✅ 是

**路径参数：**
- `sessionId`: 会话ID

**请求头：**
```
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1716534600000
}
```

---

## 8. 通知模块 (/notification)

### 8.1 获取当前用户的通知列表（分页）

**接口地址：** `GET /api/notification/list`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
```

**查询参数：**
- `page`: 页码（可选，默认1）
- `size`: 每页条数（可选，默认10）
- `isRead`: 是否已读筛选（可选，0: 未读, 1: 已读）

**请求示例：**
```
GET /api/notification/list?page=1&size=10&isRead=0
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "type": "COMMENT",
        "content": "张三评论了您的帖子",
        "sourceUser": {
          "id": 2,
          "username": "张三",
          "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/xxx.jpg"
        },
        "postId": 1,
        "isRead": false,
        "createTime": "2026-05-17T10:00:00"
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1716534600000
}
```

**字段说明：**
- `records`: 通知列表
  - `id`: 通知ID
  - `type`: 通知类型 (COMMENT, LIKE, FOLLOW, SYSTEM)
  - `content`: 通知内容
  - `sourceUser`: 来源用户信息
    - `id`: 用户ID
    - `username`: 用户名
    - `avatar`: 头像URL
  - `postId`: 相关帖子ID（可选）
  - `isRead`: 是否已读
  - `createTime`: 创建时间

---

### 8.2 标记单个通知为已读

**接口地址：** `PUT /api/notification/read/{notificationId}`

**是否需要登录：** ✅ 是

**路径参数：**
- `notificationId`: 通知ID

**请求头：**
```
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "标记成功",
  "data": null,
  "timestamp": 1716534600000
}
```

---

### 8.3 标记所有通知为已读

**接口地址：** `PUT /api/notification/read-all`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "全部标记成功",
  "data": null,
  "timestamp": 1716534600000
}
```

---

### 8.4 获取未读通知数量

**接口地址：** `GET /api/notification/unread-count`

**是否需要登录：** ✅ 是

**请求头：**
```
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 5
  },
  "timestamp": 1716534600000
}
```

---

## 9. 管理员模块 (/admin)

### 9.1 分页查询用户列表

**接口地址：** `GET /api/admin/users`

**是否需要登录：** ✅ 是（需要ADMIN角色）

**请求头：**
```
Authorization: Bearer <token>
```

**查询参数：**
- `page`: 页码（可选，默认1）
- `size`: 每页条数（可选，默认10）
- `username`: 用户名（模糊搜索，可选）
- `role`: 角色筛选（可选）
- `status`: 状态筛选（可选）

**请求示例：**
```
GET /api/admin/users?page=1&size=10&username=张&role=USER&status=1
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "张三",
        "email": "zhangsan@example.com",
        "phoneNumber": "13800138000",
        "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/xxx.jpg",
        "role": "USER",
        "status": 1,
        "createTime": "2026-05-16T10:00:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  },
  "timestamp": 1716534600000
}
```

---

### 9.2 封禁/解封用户

**接口地址：** `PUT /api/admin/users/{userId}/status`

**是否需要登录：** ✅ 是（需要ADMIN角色）

**路径参数：**
- `userId`: 用户ID

**请求头：**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**
```json
{
  "status": 0  // 0: 封禁, 1: 解封
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "用户已封禁",
  "data": null,
  "timestamp": 1716534600000
}
```

---

### 9.3 分页查询所有帖子（含待审核）

**接口地址：** `GET /api/admin/posts`

**是否需要登录：** ✅ 是（需要ADMIN角色）

**请求头：**
```
Authorization: Bearer <token>
```

**查询参数：**
- `page`: 页码（可选，默认1）
- `size`: 每页条数（可选，默认10）
- `status`: 状态筛选（可选）
- `userId`: 用户ID筛选（可选）

**请求示例：**
```
GET /api/admin/posts?page=1&size=10&status=1&userId=1
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "我的第一篇帖子",
        "category": "技术分享",
        "tags": ["Java", "Spring Boot"],
        "viewCount": 100,
        "likeCount": 10,
        "commentCount": 5,
        "status": 1,
        "createTime": "2026-05-16T10:00:00",
        "user": {
          "id": 1,
          "username": "张三",
          "avatar": "https://ai-hub-com.oss-cn-beijing.aliyuncs.com/avatars/xxx.jpg"
        }
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  },
  "timestamp": 1716534600000
}
```

---

### 9.4 审核帖子（通过/驳回/删除）

**接口地址：** `PUT /api/admin/posts/{postId}/audit`

**是否需要登录：** ✅ 是（需要ADMIN角色）

**路径参数：**
- `postId`: 帖子ID

**请求头：**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**
```json
{
  "status": 1,  // 1: 通过, 2: 驳回, 3: 删除
  "reason": "内容不符合规范"  // 可选，驳回原因
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "审核完成",
  "data": null,
  "timestamp": 1716534600000
}
```

---

### 9.5 置顶/取消置顶帖子

**接口地址：** `PUT /api/admin/posts/{postId}/sticky`

**是否需要登录：** ✅ 是（需要ADMIN角色）

**路径参数：**
- `postId`: 帖子ID

**请求头：**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**
```json
{
  "sticky": 1  // 1: 置顶, 0: 取消置顶
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1716534600000
}
```

---

### 9.6 加精/取消加精帖子

**接口地址：** `PUT /api/admin/posts/{postId}/essence`

**是否需要登录：** ✅ 是（需要ADMIN角色）

**路径参数：**
- `postId`: 帖子ID

**请求头：**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**
```json
{
  "essence": 1  // 1: 加精, 0: 取消加精
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1716534600000
}
```

---

### 9.7 删除任何评论

**接口地址：** `DELETE /api/admin/comments/{commentId}`

**是否需要登录：** ✅ 是（需要ADMIN角色）

**路径参数：**
- `commentId`: 评论ID

**请求头：**
```
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1716534600000
}
```

---

## 10. 错误码说明

### 7.1 常见错误码

| 错误码 | 消息 | 说明 |
|--------|------|------|
| 200 | success | 成功 |
| 400 | 参数错误 | 请求参数不合法 |
| 401 | 未授权 | Token无效或已过期 |
| 403 | 禁止访问 | 没有权限执行此操作 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 500 | 服务器内部错误 | 服务器异常 |

### 7.2 业务错误码

| 错误码 | 消息 | 说明 |
|--------|------|------|
| 10001 | 用户名已存在 | 注册时用户名已被占用 |
| 10002 | 邮箱已存在 | 注册时邮箱已被占用 |
| 10003 | 用户不存在 | 用户ID对应的用户不存在 |
| 10004 | 账号已被禁用 | 用户账号被禁用 |
| 10005 | 原密码错误 | 修改密码时原密码不正确 |
| 20001 | 帖子不存在 | 帖子ID对应的帖子不存在 |
| 20002 | 没有权限更新该帖子 | 非帖主或管理员尝试更新帖子 |
| 20003 | 没有权限删除该帖子 | 非帖主或管理员尝试删除帖子 |
| 30001 | 评论不存在 | 评论ID对应的评论不存在 |
| 30002 | 没有权限删除该评论 | 非评论作者或管理员尝试删除评论 |
| 30003 | 父评论不存在 | 回复评论时父评论不存在 |
| 30004 | 父评论不属于该帖子 | 回复评论时父评论不属于当前帖子 |

### 7.3 错误响应示例

```json
{
  "code": 401,
  "message": "Token无效或已过期",
  "data": null,
  "timestamp": 1716534600000
}
```

---

## 8. 开发建议

### 8.1 Token 管理

1. **登录成功后**：将 token 保存到 localStorage 或 sessionStorage
2. **每次请求**：从存储中读取 token，添加到 Authorization 头
3. **Token 过期**：捕获 401 错误，跳转到登录页
4. **退出登录**：清除本地存储的 token

### 8.2 文件上传

1. 使用 FormData 格式上传文件
2. 设置 Content-Type 为 `multipart/form-data`
3. 上传成功后获取 URL，再调用更新用户信息接口

### 8.3 评论树展示

1. 先获取顶层评论列表（分页）
2. 每个顶层评论的 replies 数组包含所有回复
3. 回复的 replies 为空数组，不需要递归渲染
4. 建议使用两层嵌套循环渲染

### 8.4 点赞/收藏状态

1. 获取帖子详情时，会返回 `isLiked` 和 `isCollected` 字段
2. 根据这两个字段显示按钮状态
3. 点赞/收藏后，接口会返回最新的状态和数量
4. 前端需要立即更新 UI，无需重新请求

### 8.5 分页处理

1. 首次加载：请求 page=1
2. 加载更多：page+1
3. 判断是否有更多：`current < pages`
4. 显示总数：使用 `total` 字段

---

## 9. 接口清单汇总

| 模块 | 接口 | 方法 | 路径 | 需要登录 |
|------|------|------|------|----------|
| 认证 | 注册 | POST | /api/auth/register | ❌ |
| 认证 | 登录 | POST | /api/auth/login | ❌ |
| 认证 | 退出 | POST | /api/auth/logout | ✅ |
| 用户 | 获取当前用户 | GET | /api/user/me | ✅ |
| 用户 | 更新用户 | PUT | /api/user/me | ✅ |
| 用户 | 修改密码 | PUT | /api/user/me/password | ✅ |
| 文件 | 上传头像 | POST | /api/file/avatar | ✅ |
| 帖子 | 发布帖子 | POST | /api/post | ✅ |
| 帖子 | 帖子列表 | GET | /api/post/list | ❌ |
| 帖子 | 帖子详情 | GET | /api/post/{postId} | ❌ |
| 帖子 | 更新帖子 | PUT | /api/post/{postId} | ✅ |
| 帖子 | 删除帖子 | DELETE | /api/post/{postId} | ✅ |
| 帖子 | 点赞帖子 | POST | /api/post/{postId}/like | ✅ |
| 帖子 | 收藏帖子 | POST | /api/post/{postId}/collect | ✅ |
| 评论 | 发表评论 | POST | /api/comment | ✅ |
| 评论 | 评论树 | GET | /api/comment/list/{postId} | ❌ |
| 评论 | 删除评论 | DELETE | /api/comment/{commentId} | ✅ |
| 评论 | 点赞评论 | POST | /api/comment/{commentId}/like | ✅ |
| AI聊天 | 与AI对话 | POST | /api/ai/chat | ✅ |
| AI聊天 | 获取会话列表 | GET | /api/ai/sessions | ✅ |
| AI聊天 | 删除会话 | DELETE | /api/ai/session/{sessionId} | ✅ |
| 通知 | 获取通知列表 | GET | /api/notification/list | ✅ |
| 通知 | 标记单个通知为已读 | PUT | /api/notification/read/{notificationId} | ✅ |
| 通知 | 标记所有通知为已读 | PUT | /api/notification/read-all | ✅ |
| 通知 | 获取未读通知数量 | GET | /api/notification/unread-count | ✅ |
| 管理员 | 查询用户列表 | GET | /api/admin/users | ✅(ADMIN) |
| 管理员 | 封禁/解封用户 | PUT | /api/admin/users/{userId}/status | ✅(ADMIN) |
| 管理员 | 查询帖子列表 | GET | /api/admin/posts | ✅(ADMIN) |
| 管理员 | 审核帖子 | PUT | /api/admin/posts/{postId}/audit | ✅(ADMIN) |
| 管理员 | 置顶/取消置顶 | PUT | /api/admin/posts/{postId}/sticky | ✅(ADMIN) |
| 管理员 | 加精/取消加精 | PUT | /api/admin/posts/{postId}/essence | ✅(ADMIN) |
| 管理员 | 删除评论 | DELETE | /api/admin/comments/{commentId} | ✅(ADMIN) |

---

## 10. 更新日志

### v1.1 (2026-05-17)
- 新增AI聊天模块
- 新增通知模块
- 新增管理员模块
- 完善API文档结构

### v1.0 (2026-05-16)
- 初始版本
- 完成用户认证模块
- 完成用户信息管理模块
- 完成文件上传模块
- 完成帖子 CRUD 模块
- 完成帖子点赞/收藏模块
- 完成评论 CRUD 模块
- 完成评论点赞模块

---

**文档结束**

如有问题，请联系后端开发团队。
