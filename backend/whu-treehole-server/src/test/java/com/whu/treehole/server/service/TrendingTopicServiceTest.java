package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.RankingItemDto;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.TrendingTopicRuleData;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrendingTopicServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-12T03:00:00Z"),
            ZoneId.of("Asia/Shanghai"));

    @Mock
    private PortalQueryMapper portalQueryMapper;

    @Test
    void shouldBuildDynamicRankingsFromRecentPostsAndHonorRules() {
        TrendingTopicService service = new TrendingTopicService(portalQueryMapper, FIXED_CLOCK);

        when(portalQueryMapper.selectRecentPostsForTrending(any())).thenReturn(List.of(
                post("post-1", "樱花季预约", 10L, 18, 12, 8, LocalDateTime.of(2026, 4, 12, 8, 0)),
                post("post-2", "樱花预约", 11L, 6, 5, 2, LocalDateTime.of(2026, 4, 12, 9, 0)),
                post("post-3", "图书馆占座", 12L, 20, 4, 1, LocalDateTime.of(2026, 4, 12, 7, 0)),
                post("post-4", "梅园食堂新品", 13L, 30, 10, 5, LocalDateTime.of(2026, 4, 12, 6, 0))
        ));

        when(portalQueryMapper.selectTrendingTopicRules()).thenReturn(List.of(
                rule("樱花预约", "樱花预约", "樱花季预约", false, false, 0),
                rule("图书馆占座", "图书馆占座", null, false, true, 1),
                rule("梅园食堂新品", "梅园食堂新品", null, true, false, 0)
        ));

        List<RankingItemDto> rankings = service.listHomeRankings();

        assertEquals(2, rankings.size());
        assertEquals("#图书馆占座", rankings.get(0).label());
        assertEquals("#樱花季预约", rankings.get(1).label());
        assertTrue(rankings.get(0).heat().contains("热度"));
    }

    private static PostData post(String code,
                                 String title,
                                 Long userId,
                                 int likes,
                                 int comments,
                                 int saves,
                                 LocalDateTime createdAt) {
        PostData data = new PostData();
        data.setPostCode(code);
        data.setTitle(title);
        data.setContent(title + " 相关讨论持续升温");
        data.setCreatorUserId(userId);
        data.setLikeCount(likes);
        data.setCommentCount(comments);
        data.setSaveCount(saves);
        data.setCreatedAt(createdAt);
        return data;
    }

    private static TrendingTopicRuleData rule(String topicKey,
                                              String displayName,
                                              String mergeTargetKey,
                                              boolean hidden,
                                              boolean pinned,
                                              int sortOrder) {
        TrendingTopicRuleData data = new TrendingTopicRuleData();
        data.setTopicKey(topicKey);
        data.setDisplayName(displayName);
        data.setMergeTargetKey(mergeTargetKey);
        data.setHiddenFlag(hidden);
        data.setPinnedFlag(pinned);
        data.setSortOrder(sortOrder);
        return data;
    }
}
