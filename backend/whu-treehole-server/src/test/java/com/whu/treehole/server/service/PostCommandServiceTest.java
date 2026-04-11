package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.PostCardDto;
import com.whu.treehole.domain.dto.PostCreateRequest;
import com.whu.treehole.domain.enums.AudienceType;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.UserProfileData;
import com.whu.treehole.server.support.PostTimeFormatter;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-11T01:05:00Z"),
            ZoneId.of("Asia/Shanghai"));

    @Mock
    private PortalCommandMapper portalCommandMapper;

    @Mock
    private PortalQueryMapper portalQueryMapper;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AuditLogService auditLogService;

    @Captor
    private ArgumentCaptor<PostData> postDataCaptor;

    private PostCommandService postCommandService;

    @BeforeEach
    void setUp() {
        postCommandService = new PostCommandService(
                portalCommandMapper,
                portalQueryMapper,
                new PostTimeFormatter(),
                authorizationService,
                auditLogService,
                FIXED_CLOCK);
    }

    @Test
    void shouldPersistAndReturnExactPostTimeWhenCreatingPost() {
        UserProfileData profile = new UserProfileData();
        profile.setId(1L);
        profile.setName("测试用户");
        profile.setCollege("工学部");
        profile.setGradeYear("2026");

        PostData savedPost = new PostData();
        savedPost.setId(11L);
        savedPost.setPostCode("post-11");
        savedPost.setTitle("标题");
        savedPost.setContent("内容");
        savedPost.setAuthorName("测试用户");
        savedPost.setAuthorHandle("工学部 · 2026");
        savedPost.setTopicName("校园日常");
        savedPost.setAudienceType(AudienceType.HOME.code());
        savedPost.setDisplayTime("刚刚");
        savedPost.setCreatedAt(LocalDateTime.of(2026, 4, 11, 9, 5));

        when(portalCommandMapper.countTopicByName("校园日常")).thenReturn(1);
        when(portalQueryMapper.selectUserProfile(1L)).thenReturn(profile);
        when(portalCommandMapper.selectPostByCode(anyString(), eq(1L))).thenReturn(savedPost);

        PostCreateRequest request = new PostCreateRequest("标题", "内容", "校园日常", AudienceType.HOME.label(), false);

        PostCardDto created = postCommandService.createPost(1L, request);

        verify(portalCommandMapper).insertPost(postDataCaptor.capture());
        assertEquals("2026-04-11 09:05", postDataCaptor.getValue().getDisplayTime());
        assertEquals("2026-04-11 09:05", created.createdAt());
    }

    @Test
    void shouldAllowAuthorToSoftDeleteOwnPost() {
        PostData post = new PostData();
        post.setId(8L);
        post.setPostCode("home-1");
        post.setCreatorUserId(1L);

        when(portalCommandMapper.selectPostByCode("home-1", 1L)).thenReturn(post);

        postCommandService.deletePost(1L, "home-1");

        verify(authorizationService).assertCanWrite(1L, "post.delete.own");
        verify(portalCommandMapper).softDeletePost(8L, 1L, LocalDateTime.of(2026, 4, 11, 9, 5));
        verify(auditLogService).record("DELETE_POST", 1L, "POST", 8L, "home-1");
    }
}
