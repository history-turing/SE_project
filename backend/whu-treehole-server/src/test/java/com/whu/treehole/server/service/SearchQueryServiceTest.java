package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.SearchResultDto;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.ContactData;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.StoryData;
import com.whu.treehole.server.support.PostTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchQueryServiceTest {

    @Mock
    private PortalQueryMapper portalQueryMapper;

    @Test
    void shouldAggregatePostsStoriesAndContacts() {
        SearchQueryService service = new SearchQueryService(portalQueryMapper, new PostTimeFormatter());

        PostData post = new PostData();
        post.setPostCode("home-1");
        post.setContent("樱花很美");
        post.setAuthorName("作者");
        post.setAuthorHandle("句柄");
        post.setTopicName("校园日常");
        post.setAudienceType("HOME");
        post.setAccentTone("rose");
        post.setLikeCount(1);
        post.setCommentCount(2);
        post.setSaveCount(3);
        post.setCreatedAt(LocalDateTime.of(2026, 4, 11, 9, 30));

        StoryData story = new StoryData();
        story.setStoryCode("story-1");
        story.setTitle("樱花季");
        story.setMeta("校友回忆");

        ContactData contact = new ContactData();
        contact.setContactCode("wang");
        contact.setName("王校友");
        contact.setMeta("2010级");
        contact.setFocus("材料");
        contact.setAvatarUrl("/avatar.jpg");

        when(portalQueryMapper.searchPosts(eq("樱花"), eq(1L))).thenReturn(List.of(post));
        when(portalQueryMapper.searchStories("樱花")).thenReturn(List.of(story));
        when(portalQueryMapper.searchContacts(eq("樱花"), eq(1L))).thenReturn(List.of(contact));

        SearchResultDto result = service.search(1L, " 樱花 ");

        assertEquals("樱花", result.keyword());
        assertEquals(1, result.posts().size());
        assertEquals(1, result.stories().size());
        assertEquals(1, result.contacts().size());
        assertEquals(3, result.total());
    }

    @Test
    void shouldRejectBlankKeyword() {
        SearchQueryService service = new SearchQueryService(portalQueryMapper, new PostTimeFormatter());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.search(1L, "   "));

        assertEquals(4005, exception.getCode());
    }
}
