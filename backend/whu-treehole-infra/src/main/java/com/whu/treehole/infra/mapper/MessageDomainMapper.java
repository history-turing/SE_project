package com.whu.treehole.infra.mapper;

import com.whu.treehole.infra.model.ConversationData;
import com.whu.treehole.infra.model.DmConversationData;
import com.whu.treehole.infra.model.DmConversationParticipantData;
import com.whu.treehole.infra.model.DmMessageData;
import com.whu.treehole.infra.model.UserProfileData;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MessageDomainMapper {

    DmConversationData selectSingleConversationBetweenUsers(@Param("userId") Long userId,
                                                            @Param("peerUserId") Long peerUserId);

    void insertConversation(DmConversationData conversationData);

    void insertConversationParticipant(DmConversationParticipantData participantData);

    Long selectConversationIdByCode(@Param("conversationCode") String conversationCode);

    List<ConversationData> selectConversationList(@Param("userId") Long userId);

    DmConversationData selectConversationByCodeAndUserId(@Param("userId") Long userId,
                                                         @Param("conversationCode") String conversationCode);

    DmConversationParticipantData selectConversationParticipant(@Param("userId") Long userId,
                                                                @Param("conversationCode") String conversationCode);

    UserProfileData selectConversationPeer(@Param("userId") Long userId,
                                           @Param("conversationCode") String conversationCode);

    List<DmMessageData> selectConversationMessages(@Param("userId") Long userId,
                                                   @Param("conversationCode") String conversationCode);

    void insertMessage(DmMessageData messageData);

    void updateConversationAfterSend(@Param("conversationId") Long conversationId,
                                     @Param("lastMessageId") Long lastMessageId,
                                     @Param("lastMessageAt") LocalDateTime lastMessageAt);

    void increaseUnreadForPeer(@Param("conversationId") Long conversationId,
                               @Param("senderUserId") Long senderUserId);
}
