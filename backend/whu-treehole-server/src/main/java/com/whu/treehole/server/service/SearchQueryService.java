package com.whu.treehole.server.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.AlumniContactDto;
import com.whu.treehole.domain.dto.PostCardDto;
import com.whu.treehole.domain.dto.SearchResultDto;
import com.whu.treehole.domain.dto.StoryCardDto;
import com.whu.treehole.domain.enums.AudienceType;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.ContactData;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.StoryData;
import com.whu.treehole.server.support.PostTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SearchQueryService {

    private final PortalQueryMapper portalQueryMapper;
    private final PostTimeFormatter postTimeFormatter;

    public SearchQueryService(PortalQueryMapper portalQueryMapper, PostTimeFormatter postTimeFormatter) {
        this.portalQueryMapper = portalQueryMapper;
        this.postTimeFormatter = postTimeFormatter;
    }

    @Cacheable(cacheNames = "searchResults", key = "#userId + ':' + (#keyword == null ? '' : #keyword.trim().toLowerCase())")
    public SearchResultDto search(long userId, String keyword) {
        String normalized = keyword == null ? null : keyword.trim();
        if (normalized == null || normalized.isBlank()) {
            throw new BusinessException(4005, "搜索关键词不能为空");
        }

        List<PostCardDto> posts = portalQueryMapper.searchPosts(normalized, userId).stream()
                .map(this::toPostCard)
                .toList();
        List<StoryCardDto> stories = portalQueryMapper.searchStories(normalized).stream()
                .map(this::toStoryCard)
                .toList();
        List<AlumniContactDto> contacts = portalQueryMapper.searchContacts(normalized, userId).stream()
                .map(this::toContactCard)
                .toList();

        return new SearchResultDto(normalized, posts.size() + stories.size() + contacts.size(), posts, stories, contacts);
    }

    private PostCardDto toPostCard(PostData postData) {
        String audience = AudienceType.ALUMNI.code().equals(postData.getAudienceType())
                ? AudienceType.ALUMNI.label()
                : AudienceType.HOME.label();

        return new PostCardDto(
                postData.getPostCode(),
                postData.getTitle(),
                postData.getContent(),
                postData.getAuthorName(),
                postData.getAuthorHandle(),
                postData.getTopicName(),
                audience,
                postTimeFormatter.format(postData.getCreatedAt(), postData.getDisplayTime()),
                Objects.requireNonNullElse(postData.getLikeCount(), 0),
                Objects.requireNonNullElse(postData.getCommentCount(), 0),
                Objects.requireNonNullElse(postData.getSaveCount(), 0),
                postData.getAccentTone(),
                postData.getBadge(),
                postData.getImageUrl(),
                Boolean.TRUE.equals(postData.getAnonymousFlag()),
                postData.getLocation(),
                Boolean.TRUE.equals(postData.getLiked()),
                Boolean.TRUE.equals(postData.getSaved())
        );
    }

    private StoryCardDto toStoryCard(StoryData storyData) {
        return new StoryCardDto(storyData.getStoryCode(), storyData.getTitle(), storyData.getMeta());
    }

    private AlumniContactDto toContactCard(ContactData contactData) {
        return new AlumniContactDto(
                contactData.getContactCode(),
                contactData.getName(),
                contactData.getMeta(),
                contactData.getFocus(),
                contactData.getAvatarUrl(),
                Boolean.TRUE.equals(contactData.getFollowed())
        );
    }
}
