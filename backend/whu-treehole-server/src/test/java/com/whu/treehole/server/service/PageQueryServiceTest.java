package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.HomePageDto;
import com.whu.treehole.domain.enums.AudienceType;
import com.whu.treehole.domain.enums.TopicScope;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.server.support.PostTimeFormatter;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PageQueryServiceTest {

    @Mock
    private PortalQueryMapper portalQueryMapper;

    private PageQueryService pageQueryService;

    @BeforeEach
    void setUp() {
        pageQueryService = new PageQueryService(portalQueryMapper, new PostTimeFormatter());
    }

    @Test
    void shouldUseCreatedAtInsteadOfStoredDisplayLabelForPosts() {
        PostData postData = new PostData();
        postData.setPostCode("post-1");
        postData.setTitle("标题");
        postData.setContent("内容");
        postData.setAuthorName("作者");
        postData.setAuthorHandle("工学部 · 2026");
        postData.setTopicName("校园日常");
        postData.setAudienceType(AudienceType.HOME.code());
        postData.setDisplayTime("刚刚");
        postData.setCreatedAt(LocalDateTime.of(2026, 4, 11, 9, 5));

        when(portalQueryMapper.selectTopics(TopicScope.CAMPUS.name())).thenReturn(Collections.emptyList());
        when(portalQueryMapper.selectTopicTags(TopicScope.CAMPUS.name())).thenReturn(Collections.emptyList());
        when(portalQueryMapper.selectPosts(eq(AudienceType.HOME.code()), eq(null), eq(null), eq(1L)))
                .thenReturn(Collections.singletonList(postData));
        when(portalQueryMapper.countPostsByAudience(anyString())).thenReturn(0);
        when(portalQueryMapper.countTopics()).thenReturn(0);
        when(portalQueryMapper.selectRankings()).thenReturn(Collections.emptyList());
        when(portalQueryMapper.selectNotices()).thenReturn(Collections.emptyList());

        HomePageDto homePage = pageQueryService.getHomePage(1L, null, null);

        assertEquals("2026-04-11 09:05", homePage.posts().get(0).createdAt());
    }
}
