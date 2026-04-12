package com.whu.treehole.server.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.AlumniContactDto;
import com.whu.treehole.domain.dto.AlumniPageDto;
import com.whu.treehole.domain.dto.AnnouncementSummaryDto;
import com.whu.treehole.domain.dto.ConversationDto;
import com.whu.treehole.domain.dto.HomePageDto;
import com.whu.treehole.domain.dto.HomeStatsDto;
import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.domain.dto.NoticeItemDto;
import com.whu.treehole.domain.dto.PostCardDto;
import com.whu.treehole.domain.dto.ProfilePageDto;
import com.whu.treehole.domain.dto.ProfileStatDto;
import com.whu.treehole.domain.dto.RankingItemDto;
import com.whu.treehole.domain.dto.StoryCardDto;
import com.whu.treehole.domain.dto.TopicCardDto;
import com.whu.treehole.domain.dto.TopicsPageDto;
import com.whu.treehole.domain.dto.UserProfileDto;
import com.whu.treehole.domain.enums.AudienceType;
import com.whu.treehole.domain.enums.TopicScope;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.ContactData;
import com.whu.treehole.infra.model.ConversationData;
import com.whu.treehole.infra.model.MessageData;
import com.whu.treehole.infra.model.NoticeData;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.ProfileStatData;
import com.whu.treehole.infra.model.StoryData;
import com.whu.treehole.infra.model.TopicData;
import com.whu.treehole.infra.model.TopicRealtimeStatData;
import com.whu.treehole.infra.model.TopicTagData;
import com.whu.treehole.infra.model.UserBadgeData;
import com.whu.treehole.infra.model.UserProfileData;
import com.whu.treehole.server.support.PostTimeFormatter;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PageQueryService {

    private final PortalQueryMapper portalQueryMapper;
    private final PostTimeFormatter postTimeFormatter;
    private final AuthorizationService authorizationService;
    private final TrendingTopicService trendingTopicService;
    private final AnnouncementService announcementService;
    private final Clock clock;

    public PageQueryService(PortalQueryMapper portalQueryMapper,
                            PostTimeFormatter postTimeFormatter,
                            AuthorizationService authorizationService,
                            TrendingTopicService trendingTopicService,
                            AnnouncementService announcementService,
                            Clock clock) {
        this.portalQueryMapper = portalQueryMapper;
        this.postTimeFormatter = postTimeFormatter;
        this.authorizationService = authorizationService;
        this.trendingTopicService = trendingTopicService;
        this.announcementService = announcementService;
        this.clock = clock;
    }

    PageQueryService(PortalQueryMapper portalQueryMapper,
                     PostTimeFormatter postTimeFormatter,
                     AuthorizationService authorizationService) {
        this(
                portalQueryMapper,
                postTimeFormatter,
                authorizationService,
                new TrendingTopicService(portalQueryMapper, Clock.system(ZoneId.of("Asia/Shanghai"))),
                new AnnouncementService(portalQueryMapper, null, authorizationService, null, Clock.system(ZoneId.of("Asia/Shanghai"))),
                Clock.system(ZoneId.of("Asia/Shanghai"))
        );
    }

    @Cacheable(cacheNames = "homePage", key = "#userId + ':' + (#topic == null ? '' : #topic) + ':' + (#keyword == null ? '' : #keyword)")
    public HomePageDto getHomePage(long userId, String topic, String keyword) {
        List<TopicData> campusTopics = portalQueryMapper.selectTopics(TopicScope.CAMPUS.name());
        Map<String, List<String>> topicTags = buildTopicTagMap(TopicScope.CAMPUS.name());
        Map<String, TopicRealtimeStatData> topicStats = buildTopicStatMap(TopicScope.CAMPUS.name());
        List<PostCardDto> posts = toPostCards(
                portalQueryMapper.selectPosts(AudienceType.HOME.code(), normalize(topic), normalize(keyword), userId),
                userId);
        List<RankingItemDto> rankings = trendingTopicService.listHomeRankings();
        List<AnnouncementSummaryDto> announcements = announcementService.listHomeAnnouncements();

        HomeStatsDto stats = new HomeStatsDto(
                String.valueOf(defaultInt(portalQueryMapper.countTodayPostsByAudience(AudienceType.HOME.code()))),
                String.valueOf(rankings.size()),
                String.valueOf(defaultInt(portalQueryMapper.countTodayPostsByAudience(AudienceType.ALUMNI.code()))));

        return new HomePageDto(
                stats,
                campusTopics.stream().limit(4).map(topicData -> toTopicCard(topicData, topicTags, topicStats)).toList(),
                rankings,
                announcements.stream().map(this::toNoticeItem).toList(),
                posts
        );
    }

    @Cacheable(cacheNames = "topicsPage", key = "#scope.name()")
    public TopicsPageDto getTopicsPage(TopicScope scope) {
        String mapperScope = switch (scope) {
            case ALL -> null;
            case CAMPUS -> TopicScope.CAMPUS.name();
            case ALUMNI -> TopicScope.ALUMNI.name();
        };
        List<TopicData> topics = portalQueryMapper.selectTopics(mapperScope);
        Map<String, List<String>> topicTags = buildTopicTagMap(mapperScope);
        Map<String, TopicRealtimeStatData> topicStats = buildTopicStatMap(mapperScope);
        return new TopicsPageDto(
                topics.stream().map(topicData -> toTopicCard(topicData, topicTags, topicStats)).toList(),
                trendingTopicService.listAllRankings()
        );
    }

    @Cacheable(cacheNames = "alumniPage", key = "#userId + ':' + (#topic == null ? '' : #topic) + ':' + (#keyword == null ? '' : #keyword)")
    public AlumniPageDto getAlumniPage(long userId, String topic, String keyword) {
        return new AlumniPageDto(
                portalQueryMapper.selectStories().stream().map(this::toStoryCard).toList(),
                portalQueryMapper.selectContacts(userId).stream().map(this::toContactCard).toList(),
                toPostCards(portalQueryMapper.selectPosts(
                        AudienceType.ALUMNI.code(), normalize(topic), normalize(keyword), userId), userId)
        );
    }

    @Cacheable(cacheNames = "profilePage", key = "#userId + ':' + (#conversationCode == null ? '' : #conversationCode)")
    public ProfilePageDto getProfilePage(long userId, String conversationCode) {
        UserProfileData userProfile = portalQueryMapper.selectUserProfile(userId);
        if (userProfile == null) {
            throw new BusinessException(4040, "未找到演示用户资料");
        }

        List<ConversationData> conversationData = portalQueryMapper.selectConversations(userId);
        List<String> codes = conversationData.stream().map(ConversationData::getConversationCode).toList();
        List<MessageData> messages = codes.isEmpty() ? Collections.emptyList() : portalQueryMapper.selectMessages(codes);
        List<ConversationDto> conversations = toConversations(conversationData, messages);
        String activeConversationId = resolveActiveConversationCode(conversationCode, conversations);

        return new ProfilePageDto(
                toUserProfile(userProfile),
                toPostCards(portalQueryMapper.selectMyPosts(userId), userId),
                toPostCards(portalQueryMapper.selectSavedPosts(userId), userId),
                conversations,
                activeConversationId
        );
    }

    @Cacheable(cacheNames = "conversationDetail", key = "#userId + ':' + #conversationCode")
    public ConversationDto getConversation(long userId, String conversationCode) {
        ConversationData conversation = portalQueryMapper.selectConversation(userId, conversationCode);
        if (conversation == null) {
            throw new BusinessException(4042, "会话不存在");
        }
        List<MessageDto> messages = portalQueryMapper.selectConversationMessages(conversationCode, userId)
                .stream()
                .map(this::toMessageDto)
                .toList();
        return new ConversationDto(
                conversation.getConversationCode(),
                conversation.getPeerName(),
                conversation.getPeerSubtitle(),
                conversation.getPeerAvatarUrl(),
                conversation.getLastMessage(),
                conversation.getDisplayTime(),
                defaultInt(conversation.getUnreadCount()),
                messages
        );
    }

    private Map<String, List<String>> buildTopicTagMap(String scope) {
        return portalQueryMapper.selectTopicTags(scope).stream()
                .collect(Collectors.groupingBy(
                        TopicTagData::getTopicCode,
                        LinkedHashMap::new,
                        Collectors.mapping(TopicTagData::getTagName, Collectors.toList())
                ));
    }

    private Map<String, TopicRealtimeStatData> buildTopicStatMap(String scope) {
        LocalDateTime now = LocalDateTime.now(clock);
        return portalQueryMapper.selectTopicRealtimeStats(scope, now.minusHours(24), now.toLocalDate().atStartOfDay()).stream()
                .collect(Collectors.toMap(
                        TopicRealtimeStatData::getTopicCode,
                        data -> data,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private TopicCardDto toTopicCard(TopicData topicData,
                                     Map<String, List<String>> topicTags,
                                     Map<String, TopicRealtimeStatData> topicStats) {
        String destination = TopicScope.ALUMNI.name().equals(topicData.getDestinationType())
                ? AudienceType.ALUMNI.destination()
                : AudienceType.HOME.destination();
        TopicRealtimeStatData statData = topicStats.get(topicData.getTopicCode());
        return new TopicCardDto(
                topicData.getTopicCode(),
                topicData.getName(),
                topicData.getDescription(),
                buildTopicHeat(statData),
                destination,
                topicData.getAccentTone(),
                topicTags.getOrDefault(topicData.getTopicCode(), Collections.emptyList()),
                topicData.getEmoji()
        );
    }

    private NoticeItemDto toNoticeItem(NoticeData noticeData) {
        return new NoticeItemDto(noticeData.getNoticeCode(), noticeData.getTitle(), noticeData.getMeta());
    }

    private NoticeItemDto toNoticeItem(AnnouncementSummaryDto announcement) {
        String meta = announcement.pinned() ? "置顶公告" : announcement.category();
        return new NoticeItemDto(announcement.code(), announcement.title(), meta);
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

    private UserProfileDto toUserProfile(UserProfileData userProfileData) {
        List<String> badges = portalQueryMapper.selectUserBadges(userProfileData.getId()).stream()
                .map(UserBadgeData::getBadgeName)
                .toList();
        List<ProfileStatDto> stats = portalQueryMapper.selectUserStats(userProfileData.getId()).stream()
                .map(this::toProfileStat)
                .toList();

        return new UserProfileDto(
                userProfileData.getName(),
                userProfileData.getTagline(),
                userProfileData.getCollege(),
                userProfileData.getGradeYear(),
                userProfileData.getBio(),
                userProfileData.getAvatarUrl(),
                badges,
                stats
        );
    }

    private ProfileStatDto toProfileStat(ProfileStatData data) {
        return new ProfileStatDto(data.getStatLabel(), data.getStatValue());
    }

    private List<PostCardDto> toPostCards(List<PostData> postDataList, long userId) {
        return postDataList.stream().map(post -> toPostCard(post, userId)).toList();
    }

    private PostCardDto toPostCard(PostData postData, long userId) {
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
                defaultInt(postData.getLikeCount()),
                defaultInt(postData.getCommentCount()),
                defaultInt(postData.getSaveCount()),
                postData.getAccentTone(),
                postData.getBadge(),
                postData.getImageUrl(),
                Boolean.TRUE.equals(postData.getAnonymousFlag()),
                postData.getLocation(),
                Objects.equals(postData.getCreatorUserId(), userId),
                Objects.equals(postData.getCreatorUserId(), userId)
                        || authorizationService.hasPermission(userId, "post.delete.any"),
                Boolean.TRUE.equals(postData.getLiked()),
                Boolean.TRUE.equals(postData.getSaved())
        );
    }

    private List<ConversationDto> toConversations(List<ConversationData> conversationData,
                                                  List<MessageData> messageData) {
        Map<String, List<MessageDto>> messagesByConversation = messageData.stream()
                .collect(Collectors.groupingBy(
                        MessageData::getConversationCode,
                        LinkedHashMap::new,
                        Collectors.mapping(this::toMessageDto, Collectors.toList())
                ));

        List<ConversationDto> conversations = new ArrayList<>();
        for (ConversationData item : conversationData) {
            conversations.add(new ConversationDto(
                    item.getConversationCode(),
                    item.getPeerName(),
                    item.getPeerSubtitle(),
                    item.getPeerAvatarUrl(),
                    item.getLastMessage(),
                    item.getDisplayTime(),
                    defaultInt(item.getUnreadCount()),
                    messagesByConversation.getOrDefault(item.getConversationCode(), Collections.emptyList())
            ));
        }
        return conversations;
    }

    private MessageDto toMessageDto(MessageData data) {
        String sender = "ME".equals(data.getSenderType()) ? "me" : "them";
        return new MessageDto(data.getMessageCode(), sender, data.getTextContent(), data.getDisplayTime());
    }

    private String resolveActiveConversationCode(String requestedCode, List<ConversationDto> conversations) {
        if (requestedCode != null && conversations.stream().map(ConversationDto::id).anyMatch(requestedCode::equals)) {
            return requestedCode;
        }
        return conversations.isEmpty() ? "" : conversations.get(0).id();
    }

    private int defaultInt(Integer value) {
        return Objects.requireNonNullElse(value, 0);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String buildTopicHeat(TopicRealtimeStatData statData) {
        if (statData == null) {
            return "今天还没有新帖";
        }
        int todayPosts = defaultInt(statData.getTodayPostCount());
        int interactionCount = defaultInt(statData.getInteractionCount24h());
        if (todayPosts > 0) {
            return "今日 " + todayPosts + " 条新帖 · 24h " + interactionCount + " 次互动";
        }
        if (defaultInt(statData.getTotalPostCount()) > 0) {
            return "今天还没有新帖 · 累计 " + statData.getTotalPostCount() + " 条";
        }
        return "今天还没有新帖";
    }
}
