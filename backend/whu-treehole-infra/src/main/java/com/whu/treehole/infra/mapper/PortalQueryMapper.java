package com.whu.treehole.infra.mapper;

/* 查询 Mapper 统一负责页面聚合所需的只读 SQL。 */

import com.whu.treehole.infra.model.ContactData;
import com.whu.treehole.infra.model.ConversationData;
import com.whu.treehole.infra.model.MessageData;
import com.whu.treehole.infra.model.NoticeData;
import com.whu.treehole.infra.model.PostCommentData;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.ProfileStatData;
import com.whu.treehole.infra.model.RankingData;
import com.whu.treehole.infra.model.StoryData;
import com.whu.treehole.infra.model.TopicData;
import com.whu.treehole.infra.model.TopicTagData;
import com.whu.treehole.infra.model.UserBadgeData;
import com.whu.treehole.infra.model.UserProfileData;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PortalQueryMapper {

    List<TopicData> selectTopics(@Param("scope") String scope);

    List<TopicTagData> selectTopicTags(@Param("scope") String scope);

    List<RankingData> selectRankings();

    List<NoticeData> selectNotices();

    List<PostData> selectPosts(@Param("audience") String audience,
                               @Param("topic") String topic,
                               @Param("keyword") String keyword,
                               @Param("userId") Long userId);

    UserProfileData selectUserProfile(@Param("userId") Long userId);

    List<UserBadgeData> selectUserBadges(@Param("userId") Long userId);

    List<ProfileStatData> selectUserStats(@Param("userId") Long userId);

    List<PostData> selectMyPosts(@Param("userId") Long userId);

    List<PostData> selectSavedPosts(@Param("userId") Long userId);

    List<StoryData> selectStories();

    List<ContactData> selectContacts(@Param("userId") Long userId);

    List<PostCommentData> selectCommentsByPostCode(@Param("postCode") String postCode);

    List<PostCommentData> selectCommentsByPostId(@Param("postId") Long postId);

    PostCommentData selectCommentByCode(@Param("postCode") String postCode,
                                        @Param("commentCode") String commentCode);

    List<PostData> searchPosts(@Param("keyword") String keyword, @Param("userId") Long userId);

    List<StoryData> searchStories(@Param("keyword") String keyword);

    List<ContactData> searchContacts(@Param("keyword") String keyword, @Param("userId") Long userId);

    List<ConversationData> selectConversations(@Param("userId") Long userId);

    List<MessageData> selectMessages(@Param("conversationCodes") List<String> conversationCodes);

    ConversationData selectConversation(@Param("userId") Long userId,
                                        @Param("conversationCode") String conversationCode);

    List<MessageData> selectConversationMessages(@Param("conversationCode") String conversationCode,
                                                 @Param("userId") Long userId);

    Integer countPostsByAudience(@Param("audience") String audience);

    Integer countTopics();
}
