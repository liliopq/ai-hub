package com.ai_hub.service;

import com.ai_hub.dto.request.CreatePostRequest;
import com.ai_hub.dto.request.PostListRequest;
import com.ai_hub.dto.response.CreatePostResponse;
import com.ai_hub.dto.response.PageResult;
import com.ai_hub.dto.response.PostListItemResponse;
import com.ai_hub.entity.Post;
import com.ai_hub.mapper.PostMapper;
import com.ai_hub.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 帖子服务测试类
 */
@SpringBootTest
@Transactional // 每个测试方法执行后自动回滚，不影响数据库
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostMapper postMapper;

    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        // 清理测试数据 - 删除所有帖子
        postMapper.delete(null);
    }

    @Test
    void testCreatePost() {
        // 准备测试数据
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("测试帖子标题");
        request.setContent("这是测试帖子的内容");
        request.setCategory("技术分享");
        request.setTags(new String[]{"Java", "Spring Boot"});

        // 执行测试
        CreatePostResponse response = postService.createPost(testUserId, request);

        // 验证结果
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("测试帖子标题", response.getTitle());
        assertNotNull(response.getCreateTime());

        // 验证数据库中是否有记录
        Post savedPost = postMapper.selectById(response.getId());
        assertNotNull(savedPost);
        assertEquals("测试帖子标题", savedPost.getTitle());
        assertEquals("这是测试帖子的内容", savedPost.getContent());
        assertEquals("技术分享", savedPost.getCategory());
    }

    @Test
    void testGetPostList() {
        // 准备测试数据：创建多个帖子
        for (int i = 1; i <= 15; i++) {
            CreatePostRequest request = new CreatePostRequest();
            request.setTitle("测试帖子" + i);
            request.setContent("内容" + i);
            request.setCategory(i % 2 == 0 ? "技术分享" : "问答");
            postService.createPost(testUserId, request);
        }

        // 执行测试：获取第一页
        PostListRequest listRequest = new PostListRequest();
        listRequest.setPage(1);
        listRequest.setSize(10);
        
        PageResult<PostListItemResponse> result = postService.getPostList(listRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(10, result.getRecords().size());
        assertEquals(15L, result.getTotal());
        assertEquals(1L, result.getCurrent());
        assertEquals(10L, result.getSize());

        // 验证第二页
        listRequest.setPage(2);
        result = postService.getPostList(listRequest);
        assertEquals(5, result.getRecords().size());
    }

    @Test
    void testGetPostListWithCategoryFilter() {
        // 准备测试数据
        CreatePostRequest request1 = new CreatePostRequest();
        request1.setTitle("技术帖子1");
        request1.setContent("内容1");
        request1.setCategory("技术分享");
        postService.createPost(testUserId, request1);

        CreatePostRequest request2 = new CreatePostRequest();
        request2.setTitle("问答帖子1");
        request2.setContent("内容2");
        request2.setCategory("问答");
        postService.createPost(testUserId, request2);

        // 执行测试：按分类筛选
        PostListRequest listRequest = new PostListRequest();
        listRequest.setPage(1);
        listRequest.setSize(10);
        listRequest.setCategory("技术分享");
        
        PageResult<PostListItemResponse> result = postService.getPostList(listRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("技术帖子1", result.getRecords().get(0).getTitle());
    }

    @Test
    void testGetPostDetail() {
        // 准备测试数据
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("测试详情帖子");
        request.setContent("详细内容");
        request.setCategory("技术分享");
        request.setTags(new String[]{"测试"});
        
        CreatePostResponse createResponse = postService.createPost(testUserId, request);
        Long postId = createResponse.getId();

        // 执行测试
        var detail = postService.getPostDetail(postId, null);

        // 验证结果
        assertNotNull(detail);
        assertEquals("测试详情帖子", detail.getTitle());
        assertEquals("详细内容", detail.getContent());
        assertEquals("技术分享", detail.getCategory());
        assertNotNull(detail.getTags());
        assertEquals(1, detail.getTags().size());
        assertEquals("测试", detail.getTags().get(0));
    }

    @Test
    void testUpdatePost() {
        // 准备测试数据
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("原始标题");
        createRequest.setContent("原始内容");
        createRequest.setCategory("技术分享");
        
        CreatePostResponse createResponse = postService.createPost(testUserId, createRequest);
        Long postId = createResponse.getId();

        // 执行更新
        com.ai_hub.dto.request.UpdatePostRequest updateRequest = new com.ai_hub.dto.request.UpdatePostRequest();
        updateRequest.setTitle("更新后的标题");
        updateRequest.setContent("更新后的内容");
        
        var updatedDetail = postService.updatePost(postId, testUserId, "USER", updateRequest);

        // 验证结果
        assertNotNull(updatedDetail);
        assertEquals("更新后的标题", updatedDetail.getTitle());
        assertEquals("更新后的内容", updatedDetail.getContent());
    }

    @Test
    void testDeletePost() {
        // 准备测试数据
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("待删除帖子");
        request.setContent("内容");
        request.setCategory("技术分享");
        
        CreatePostResponse createResponse = postService.createPost(testUserId, request);
        Long postId = createResponse.getId();

        // 执行删除
        postService.deletePost(postId, testUserId, "USER");

        // 验证删除（软删除，应该查询不到）
        Post deletedPost = postMapper.selectById(postId);
        assertNull(deletedPost); // MyBatis-Plus 逻辑删除后 selectById 返回 null
    }
}
