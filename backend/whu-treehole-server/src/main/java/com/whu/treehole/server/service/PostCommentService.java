package com.whu.treehole.server.service;

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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostCommentService {

    private final PortalQueryMapper portalQueryMapper;
    private final PortalCommandMapper portalCommandMapper;
    private final PostTimeFormatter postTimeFormatter;
    private final Clock clock;

    public PostCommentService(PortalQueryMapper portalQueryMapper,
                              PortalCommandMapper portalCommandMapper,
                              PostTimeFormatter postTimeFormatter,
                              Clock clock) {
        this.portalQueryMapper = portalQueryMapper;
        this.portalCommandMapper = portalCommandMapper;
        this.postTimeFormatter = postTimeFormatter;
        this.clock = clock;
    }

    @Cacheable(cacheNames = "postComments", key = "#postCode")
    public PostCommentsDto listComments(long userId, String postCode) {
        requirePost(postCode, userId);
        List<PostCommentData> records = portalQueryMapper.selectCommentsByPostCode(postCode);
        Map<Long, PostCommentDto> roots = new LinkedHashMap<>();
        Map<Long, String> commentCodes = new LinkedHashMap<>();
        Map<Long, String> userNames = new LinkedHashMap<>();

        for (PostCommentData record : records) {
            commentCodes.put(record.getId(), record.getCommentCode());
            userNames.put(record.getUserId(), record.getAuthorName());
        }

        for (PostCommentData record : records) {
            if (record.getParentCommentId() == null) {
                roots.put(record.getId(), toCommentDto(record, postCode, null, null, userId));
            }
        }

        for (PostCommentData record : records) {
            if (record.getParentCommentId() == null) {
                continue;
            }
            PostCommentDto parent = roots.get(record.getRootCommentId());
            if (parent == null) {
                continue;
            }
            List<PostCommentDto> replies = new ArrayList<>(parent.replies());
            replies.add(toCommentDto(
                    record,
                    postCode,
                    commentCodes.get(record.getParentCommentId()),
                    userNames.get(record.getReplyToUserId()),
                    userId));
            roots.put(record.getRootCommentId(), copyWithReplies(parent, replies));
        }

        return new PostCommentsDto(new ArrayList<>(roots.values()), records.size());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "postComments", key = "#postCode"),
            @CacheEvict(cacheNames = "homePage", allEntries = true),
            @CacheEvict(cacheNames = "alumniPage", allEntries = true),
            @CacheEvict(cacheNames = "profilePage", allEntries = true)
    })
    public PostCommentDto createComment(long userId, String postCode, CommentCreateRequest request) {
        PostData post = requirePost(postCode, userId);
        return persistComment(userId, post, null, request);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "postComments", key = "#postCode"),
            @CacheEvict(cacheNames = "homePage", allEntries = true),
            @CacheEvict(cacheNames = "alumniPage", allEntries = true),
            @CacheEvict(cacheNames = "profilePage", allEntries = true)
    })
    public PostCommentDto replyComment(long userId, String postCode, String commentCode, CommentCreateRequest request) {
        PostData post = requirePost(postCode, userId);
        PostCommentData parent = requireComment(postCode, commentCode);
        if (parent.getParentCommentId() != null) {
            throw new BusinessException(4005, "当前仅支持两级评论");
        }
        return persistComment(userId, post, parent, request);
    }

    private PostCommentDto persistComment(long userId,
                                          PostData post,
                                          PostCommentData parent,
                                          CommentCreateRequest request) {
        UserProfileData profile = portalQueryMapper.selectUserProfile(userId);
        if (profile == null) {
            throw new BusinessException(4040, "未找到演示用户资料");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        PostCommentData comment = new PostCommentData();
        comment.setCommentCode("comment-" + System.currentTimeMillis());
        comment.setPostId(post.getId());
        comment.setUserId(userId);
        comment.setParentCommentId(parent == null ? null : parent.getId());
        comment.setRootCommentId(parent == null ? null : parent.getId());
        comment.setReplyToUserId(parent == null ? null : parent.getUserId());
        comment.setAuthorName(profile.getName());
        comment.setAuthorHandle(profile.getCollege() + " · " + profile.getGradeYear());
        comment.setContent(request.content().trim());
        comment.setDeletedFlag(false);
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);
        portalCommandMapper.insertPostComment(comment);
        portalCommandMapper.increasePostCommentCount(post.getId());

        PostCommentData saved = comment.getId() == null ? comment : portalCommandMapper.selectCommentById(comment.getId());
        if (saved == null) {
            saved = comment;
        }
        return toCommentDto(
                saved,
                post.getPostCode(),
                parent == null ? null : parent.getCommentCode(),
                parent == null ? null : parent.getAuthorName(),
                userId);
    }

    private PostData requirePost(String postCode, long userId) {
        PostData postData = portalCommandMapper.selectPostByCode(postCode, userId);
        if (postData == null) {
            throw new BusinessException(4041, "帖子不存在");
        }
        return postData;
    }

    private PostCommentData requireComment(String postCode, String commentCode) {
        PostCommentData commentData = portalQueryMapper.selectCommentByCode(postCode, commentCode);
        if (commentData == null) {
            throw new BusinessException(4044, "评论不存在");
        }
        return commentData;
    }

    private PostCommentDto toCommentDto(PostCommentData data,
                                        String postCode,
                                        String parentCommentCode,
                                        String replyToUserName,
                                        long userId) {
        return new PostCommentDto(
                data.getCommentCode(),
                postCode,
                parentCommentCode,
                data.getAuthorName(),
                data.getAuthorHandle(),
                data.getContent(),
                postTimeFormatter.format(data.getCreatedAt(), null),
                Objects.equals(data.getUserId(), userId),
                replyToUserName,
                new ArrayList<>()
        );
    }

    private PostCommentDto copyWithReplies(PostCommentDto source, List<PostCommentDto> replies) {
        return new PostCommentDto(
                source.id(),
                source.postId(),
                source.parentCommentCode(),
                source.author(),
                source.handle(),
                source.content(),
                source.createdAt(),
                source.mine(),
                source.replyToUserName(),
                replies
        );
    }
}
