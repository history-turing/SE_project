package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.CommentCreateRequest;
import com.whu.treehole.domain.dto.PostCommentDto;
import com.whu.treehole.domain.dto.PostCommentsDto;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.PostCommentData;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.UserProfileData;
import com.whu.treehole.server.support.PostTimeFormatter;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-11T01:30:00Z"),
            ZoneId.of("Asia/Shanghai"));

    @Mock
    private PortalQueryMapper portalQueryMapper;

    @Mock
    private PortalCommandMapper portalCommandMapper;

    @Captor
    private ArgumentCaptor<PostCommentData> commentCaptor;

    @Test
    void shouldCreateRootCommentAndIncreaseCount() {
        PostCommentService service = new PostCommentService(
                portalQueryMapper,
                portalCommandMapper,
                new PostTimeFormatter(),
                FIXED_CLOCK);

        PostData post = new PostData();
        post.setId(8L);
        post.setPostCode("home-1");

        UserProfileData user = new UserProfileData();
        user.setId(1L);
        user.setName("测试用户");
        user.setCollege("信管");
        user.setGradeYear("2022");

        PostCommentData saved = new PostCommentData();
        saved.setId(30L);
        saved.setCommentCode("comment-root");
        saved.setPostId(8L);
        saved.setUserId(1L);
        saved.setParentCommentId(null);
        saved.setRootCommentId(null);
        saved.setReplyToUserId(null);
        saved.setAuthorName("测试用户");
        saved.setAuthorHandle("信管 路 2022");
        saved.setContent("新的评论");
        saved.setCreatedAt(LocalDateTime.of(2026, 4, 11, 9, 30));

        when(portalCommandMapper.selectPostByCode("home-1", 1L)).thenReturn(post);
        when(portalQueryMapper.selectUserProfile(1L)).thenReturn(user);
        doAnswer(invocation -> {
            PostCommentData inserted = invocation.getArgument(0);
            inserted.setId(30L);
            return null;
        }).when(portalCommandMapper).insertPostComment(any(PostCommentData.class));
        when(portalCommandMapper.selectCommentById(30L)).thenReturn(saved);

        PostCommentDto created = service.createComment(1L, "home-1", new CommentCreateRequest("新的评论"));

        verify(portalCommandMapper).insertPostComment(commentCaptor.capture());
        verify(portalCommandMapper).increasePostCommentCount(8L);
        assertEquals("comment-root", created.id());
        assertNull(created.parentCommentCode());
        assertEquals("新的评论", created.content());
        assertEquals(0, created.replies().size());
        assertNull(commentCaptor.getValue().getParentCommentId());
        assertNull(commentCaptor.getValue().getRootCommentId());
    }

    @Test
    void shouldCreateReplyAndReturnNestedComments() {
        PostCommentService service = new PostCommentService(
                portalQueryMapper,
                portalCommandMapper,
                new PostTimeFormatter(),
                FIXED_CLOCK);

        PostData post = new PostData();
        post.setId(8L);
        post.setPostCode("home-1");

        UserProfileData user = new UserProfileData();
        user.setId(1L);
        user.setName("测试用户");
        user.setCollege("信管");
        user.setGradeYear("2022");

        PostCommentData root = new PostCommentData();
        root.setId(20L);
        root.setCommentCode("comment-root");
        root.setPostId(8L);
        root.setUserId(2L);
        root.setParentCommentId(null);
        root.setRootCommentId(20L);
        root.setReplyToUserId(null);
        root.setAuthorName("原评论人");
        root.setAuthorHandle("计院 路 2020");
        root.setContent("第一条评论");
        root.setCreatedAt(LocalDateTime.of(2026, 4, 11, 9, 20));

        PostCommentData reply = new PostCommentData();
        reply.setId(21L);
        reply.setCommentCode("comment-reply");
        reply.setPostId(8L);
        reply.setUserId(1L);
        reply.setParentCommentId(20L);
        reply.setRootCommentId(20L);
        reply.setReplyToUserId(2L);
        reply.setAuthorName("测试用户");
        reply.setAuthorHandle("信管 路 2022");
        reply.setContent("收到，谢谢");
        reply.setCreatedAt(LocalDateTime.of(2026, 4, 11, 9, 30));

        when(portalCommandMapper.selectPostByCode("home-1", 1L)).thenReturn(post);
        when(portalQueryMapper.selectUserProfile(1L)).thenReturn(user);
        when(portalQueryMapper.selectCommentByCode("home-1", "comment-root")).thenReturn(root);
        doAnswer(invocation -> {
            PostCommentData inserted = invocation.getArgument(0);
            inserted.setId(21L);
            return null;
        }).when(portalCommandMapper).insertPostComment(any(PostCommentData.class));
        when(portalCommandMapper.selectCommentById(21L)).thenReturn(reply);
        when(portalQueryMapper.selectCommentsByPostCode("home-1")).thenReturn(List.of(root, reply));

        PostCommentDto created = service.replyComment(
                1L,
                "home-1",
                "comment-root",
                new CommentCreateRequest("收到，谢谢"));
        PostCommentsDto comments = service.listComments(1L, "home-1");

        verify(portalCommandMapper).insertPostComment(commentCaptor.capture());
        verify(portalCommandMapper).increasePostCommentCount(8L);
        assertEquals("comment-root", created.parentCommentCode());
        assertEquals("原评论人", created.replyToUserName());
        assertEquals("comment-root", comments.comments().get(0).id());
        assertEquals(1, comments.comments().get(0).replies().size());
        assertEquals("comment-reply", comments.comments().get(0).replies().get(0).id());
        assertEquals(2, comments.total());
    }

    @Test
    void shouldRejectReplyToSecondLevelComment() {
        PostCommentService service = new PostCommentService(
                portalQueryMapper,
                portalCommandMapper,
                new PostTimeFormatter(),
                FIXED_CLOCK);

        PostData post = new PostData();
        post.setId(8L);
        post.setPostCode("home-1");

        PostCommentData reply = new PostCommentData();
        reply.setId(21L);
        reply.setCommentCode("comment-reply");
        reply.setPostId(8L);
        reply.setParentCommentId(20L);
        reply.setRootCommentId(20L);

        when(portalCommandMapper.selectPostByCode("home-1", 1L)).thenReturn(post);
        when(portalQueryMapper.selectCommentByCode("home-1", "comment-reply")).thenReturn(reply);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.replyComment(1L, "home-1", "comment-reply", new CommentCreateRequest("不允许三级回复")));

        assertEquals(4005, exception.getCode());
    }
}
