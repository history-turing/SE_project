package com.whu.treehole.infra.mapper;

/* 命令 Mapper 负责发帖、互动、关注和消息写入。 */

import com.whu.treehole.infra.model.FollowStateData;
import com.whu.treehole.infra.model.InteractionStateData;
import com.whu.treehole.infra.model.MessageData;
import com.whu.treehole.infra.model.PostCommentData;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.AuditLogData;
import com.whu.treehole.infra.model.ReportData;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;

public interface PortalCommandMapper {

    Integer countTopicByName(@Param("topicName") String topicName);

    void insertPost(PostData postData);

    PostData selectPostByCode(@Param("postCode") String postCode,
                              @Param("userId") Long userId);

    InteractionStateData selectInteractionState(@Param("userId") Long userId,
                                                @Param("postCode") String postCode);

    void insertInteraction(@Param("userId") Long userId,
                           @Param("postId") Long postId,
                           @Param("liked") boolean liked,
                           @Param("saved") boolean saved);

    void updateInteraction(@Param("userId") Long userId,
                           @Param("postId") Long postId,
                           @Param("liked") boolean liked,
                           @Param("saved") boolean saved);

    void updatePostLikeCount(@Param("postId") Long postId, @Param("delta") int delta);

    void updatePostSaveCount(@Param("postId") Long postId, @Param("delta") int delta);

    void softDeletePost(@Param("postId") Long postId,
                        @Param("deletedBy") Long deletedBy,
                        @Param("deletedAt") LocalDateTime deletedAt);

    void restorePost(@Param("postId") Long postId,
                     @Param("updatedAt") LocalDateTime updatedAt);

    void insertPostComment(PostCommentData postCommentData);

    PostCommentData selectCommentById(@Param("commentId") Long commentId);

    void increasePostCommentCount(@Param("postId") Long postId);

    void decreasePostCommentCount(@Param("postId") Long postId, @Param("delta") int delta);

    Integer countActiveCommentBranch(@Param("rootCommentId") Long rootCommentId);

    void softDeleteComment(@Param("commentId") Long commentId,
                           @Param("deletedBy") Long deletedBy,
                           @Param("deletedAt") LocalDateTime deletedAt);

    void softDeleteCommentBranch(@Param("rootCommentId") Long rootCommentId,
                                 @Param("deletedBy") Long deletedBy,
                                 @Param("deletedAt") LocalDateTime deletedAt);

    void restoreComment(@Param("commentId") Long commentId,
                        @Param("updatedAt") LocalDateTime updatedAt);

    void restoreCommentBranch(@Param("rootCommentId") Long rootCommentId,
                              @Param("updatedAt") LocalDateTime updatedAt);

    FollowStateData selectFollowState(@Param("userId") Long userId,
                                      @Param("contactCode") String contactCode);

    void insertFollowState(@Param("userId") Long userId,
                           @Param("contactId") Long contactId,
                           @Param("followed") boolean followed);

    void updateFollowState(@Param("userId") Long userId,
                           @Param("contactId") Long contactId,
                           @Param("followed") boolean followed);

    Long selectContactId(@Param("contactCode") String contactCode);

    Long selectConversationId(@Param("userId") Long userId,
                              @Param("conversationCode") String conversationCode);

    void insertMessage(MessageData messageData);

    void updateConversationAfterSend(@Param("conversationId") Long conversationId,
                                     @Param("lastMessage") String lastMessage,
                                     @Param("displayTime") String displayTime,
                                     @Param("sortTime") LocalDateTime sortTime);

    void markConversationRead(@Param("userId") Long userId,
                              @Param("conversationCode") String conversationCode);

    void insertAuditLog(AuditLogData auditLogData);

    void insertReport(ReportData reportData);

    void resolveReport(@Param("reportCode") String reportCode,
                       @Param("status") String status,
                       @Param("resolutionCode") String resolutionCode,
                       @Param("resolutionNote") String resolutionNote,
                       @Param("resolvedAt") LocalDateTime resolvedAt,
                       @Param("assignedUserId") Long assignedUserId);
}
