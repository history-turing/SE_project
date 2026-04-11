package com.whu.treehole.server.service;

/* 帖子命令服务负责发帖、点赞和收藏写入，并同步清理页面缓存。 */

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.PostCardDto;
import com.whu.treehole.domain.dto.PostCreateRequest;
import com.whu.treehole.domain.dto.ToggleResponse;
import com.whu.treehole.domain.enums.AudienceType;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.InteractionStateData;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.UserProfileData;
import com.whu.treehole.server.support.PostTimeFormatter;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostCommandService {

    private final PortalCommandMapper portalCommandMapper;
    private final PortalQueryMapper portalQueryMapper;
    private final PostTimeFormatter postTimeFormatter;
    private final AuthorizationService authorizationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public PostCommandService(PortalCommandMapper portalCommandMapper,
                              PortalQueryMapper portalQueryMapper,
                              PostTimeFormatter postTimeFormatter,
                              AuthorizationService authorizationService,
                              AuditLogService auditLogService,
                              Clock clock) {
        this.portalCommandMapper = portalCommandMapper;
        this.portalQueryMapper = portalQueryMapper;
        this.postTimeFormatter = postTimeFormatter;
        this.authorizationService = authorizationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "homePage", allEntries = true),
            @CacheEvict(cacheNames = "alumniPage", allEntries = true),
            @CacheEvict(cacheNames = "profilePage", allEntries = true)
    })
    public PostCardDto createPost(long userId, PostCreateRequest request) {
        authorizationService.assertCanWrite(userId, "post.create");
        if (portalCommandMapper.countTopicByName(request.topic()) == 0) {
            throw new BusinessException(4004, "发布话题不存在");
        }

        UserProfileData profile = portalQueryMapper.selectUserProfile(userId);
        if (profile == null) {
            throw new BusinessException(4040, "未找到演示用户资料");
        }

        AudienceType audienceType = AudienceType.fromLabel(request.audience());
        PostData postData = new PostData();
        postData.setPostCode("post-" + System.currentTimeMillis());
        postData.setCreatorUserId(userId);
        postData.setTitle(blankToNull(request.title()));
        postData.setContent(request.content().trim());
        postData.setAuthorName(Boolean.TRUE.equals(request.anonymous()) ? "匿名珞珈人" : profile.getName());
        postData.setAuthorHandle(Boolean.TRUE.equals(request.anonymous())
                ? "低语模式"
                : profile.getCollege() + " · " + profile.getGradeYear());
        postData.setTopicName(request.topic().trim());
        postData.setAudienceType(audienceType.code());
        postData.setCreatedAt(LocalDateTime.now(clock));
        postData.setDisplayTime(postTimeFormatter.format(postData.getCreatedAt(), "刚刚"));
        postData.setLikeCount(0);
        postData.setCommentCount(0);
        postData.setSaveCount(0);
        postData.setAccentTone(audienceType == AudienceType.ALUMNI ? "jade" : "rose");
        postData.setBadge(audienceType == AudienceType.ALUMNI ? "新发布" : null);
        postData.setAnonymousFlag(request.anonymous());
        portalCommandMapper.insertPost(postData);

        return toPostCard(requirePost(postData.getPostCode(), userId));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "homePage", allEntries = true),
            @CacheEvict(cacheNames = "alumniPage", allEntries = true),
            @CacheEvict(cacheNames = "profilePage", allEntries = true)
    })
    public ToggleResponse toggleLike(long userId, String postCode) {
        authorizationService.assertActiveUser(userId);
        InteractionStateData state = portalCommandMapper.selectInteractionState(userId, postCode);
        PostData postData = requirePost(postCode, userId);

        boolean nextLiked = !Boolean.TRUE.equals(state == null ? null : state.getLiked());
        boolean saved = Boolean.TRUE.equals(state == null ? null : state.getSaved());
        if (state == null) {
            portalCommandMapper.insertInteraction(userId, postData.getId(), nextLiked, saved);
        } else {
            portalCommandMapper.updateInteraction(userId, postData.getId(), nextLiked, saved);
        }
        portalCommandMapper.updatePostLikeCount(postData.getId(), nextLiked ? 1 : -1);
        PostData latest = requirePost(postCode, userId);
        return new ToggleResponse(postCode, nextLiked, latest.getLikeCount());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "homePage", allEntries = true),
            @CacheEvict(cacheNames = "alumniPage", allEntries = true),
            @CacheEvict(cacheNames = "profilePage", allEntries = true)
    })
    public ToggleResponse toggleSave(long userId, String postCode) {
        authorizationService.assertActiveUser(userId);
        InteractionStateData state = portalCommandMapper.selectInteractionState(userId, postCode);
        PostData postData = requirePost(postCode, userId);

        boolean liked = Boolean.TRUE.equals(state == null ? null : state.getLiked());
        boolean nextSaved = !Boolean.TRUE.equals(state == null ? null : state.getSaved());
        if (state == null) {
            portalCommandMapper.insertInteraction(userId, postData.getId(), liked, nextSaved);
        } else {
            portalCommandMapper.updateInteraction(userId, postData.getId(), liked, nextSaved);
        }
        portalCommandMapper.updatePostSaveCount(postData.getId(), nextSaved ? 1 : -1);
        PostData latest = requirePost(postCode, userId);
        return new ToggleResponse(postCode, nextSaved, latest.getSaveCount());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "homePage", allEntries = true),
            @CacheEvict(cacheNames = "alumniPage", allEntries = true),
            @CacheEvict(cacheNames = "profilePage", allEntries = true)
    })
    public void deletePost(long userId, String postCode) {
        PostData post = requirePost(postCode, userId);
        String permissionCode = userId == post.getCreatorUserId() ? "post.delete.own" : "post.delete.any";
        authorizationService.assertCanWrite(userId, permissionCode);

        portalCommandMapper.softDeletePost(post.getId(), userId, LocalDateTime.now(clock));
        auditLogService.record("DELETE_POST", userId, "POST", post.getId(), post.getPostCode());
    }

    private PostData requirePost(String postCode, long userId) {
        PostData postData = portalCommandMapper.selectPostByCode(postCode, userId);
        if (postData == null) {
            throw new BusinessException(4041, "帖子不存在");
        }
        return postData;
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
                postData.getLikeCount(),
                postData.getCommentCount(),
                postData.getSaveCount(),
                postData.getAccentTone(),
                postData.getBadge(),
                postData.getImageUrl(),
                Boolean.TRUE.equals(postData.getAnonymousFlag()),
                postData.getLocation(),
                Boolean.TRUE.equals(postData.getLiked()),
                Boolean.TRUE.equals(postData.getSaved())
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
