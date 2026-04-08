package com.whu.treehole.infra.mapper;

/* 命令 Mapper 负责发帖、互动、关注和消息写入。 */

import com.whu.treehole.infra.model.FollowStateData;
import com.whu.treehole.infra.model.InteractionStateData;
import com.whu.treehole.infra.model.MessageData;
import com.whu.treehole.infra.model.PostData;
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
}
