package com.whu.treehole.server.service;

import com.whu.treehole.domain.dto.RankingItemDto;
import com.whu.treehole.domain.dto.TrendingTopicAdminDto;
import com.whu.treehole.domain.dto.TrendingTopicRuleRequest;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.TrendingTopicRuleData;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class TrendingTopicService {

    private static final int HOME_LIMIT = 5;
    private static final int WINDOW_HOURS = 72;
    private static final Pattern HASH_TAG_PATTERN = Pattern.compile("#([^#\\s]{2,24})");
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[\\s,，。！？!?.；;：:\\-—/\\\\]+");
    private static final Pattern CLEAN_PATTERN = Pattern.compile("[^\\p{IsHan}A-Za-z0-9]");
    private static final List<String> COMMON_PREFIXES = List.of("有没有", "求问", "请问", "想问一下", "想问", "求助", "关于", "终于");
    private static final Set<String> CHANNEL_NAMES = Set.of("表白墙", "失物招领", "学业交流", "校园日常", "职场内推", "校友故事", "生活闲聊");

    private final PortalQueryMapper portalQueryMapper;
    private final PortalCommandMapper portalCommandMapper;
    private final AuthorizationService authorizationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public TrendingTopicService(PortalQueryMapper portalQueryMapper,
                                PortalCommandMapper portalCommandMapper,
                                AuthorizationService authorizationService,
                                AuditLogService auditLogService,
                                Clock clock) {
        this.portalQueryMapper = portalQueryMapper;
        this.portalCommandMapper = portalCommandMapper;
        this.authorizationService = authorizationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    TrendingTopicService(PortalQueryMapper portalQueryMapper, Clock clock) {
        this(portalQueryMapper, null, null, null, clock);
    }

    public List<RankingItemDto> listHomeRankings() {
        return listAllRankings().stream().limit(HOME_LIMIT).toList();
    }

    public List<RankingItemDto> listAllRankings() {
        return buildRankings().stream()
                .map(topic -> new RankingItemDto(topic.topicKey, "#" + topic.displayName, topic.heatText()))
                .toList();
    }

    public List<TrendingTopicAdminDto> listAdminTopics(long actorUserId) {
        authorizationService.assertCanWrite(actorUserId, "trending.read.any");
        return buildRankings().stream()
                .map(topic -> new TrendingTopicAdminDto(
                        topic.topicKey,
                        topic.displayName,
                        topic.mergeTargetKey,
                        topic.hidden,
                        topic.pinned,
                        topic.sortOrder,
                        topic.postCount,
                        topic.interactionCount,
                        topic.uniqueAuthorCount,
                        topic.score
                ))
                .toList();
    }

    public void saveRule(long actorUserId, TrendingTopicRuleRequest request) {
        if (Boolean.TRUE.equals(request.hidden())) {
            authorizationService.assertCanWrite(actorUserId, "trending.hide");
        } else {
            authorizationService.assertCanWrite(actorUserId, "trending.curate");
        }

        TrendingTopicRuleData data = new TrendingTopicRuleData();
        data.setTopicKey(normalizeKey(request.topicKey()));
        data.setDisplayName(defaultDisplayName(request.topicKey(), request.displayName()));
        data.setMergeTargetKey(normalizeNullable(request.mergeTargetKey()));
        data.setHiddenFlag(Boolean.TRUE.equals(request.hidden()));
        data.setPinnedFlag(Boolean.TRUE.equals(request.pinned()));
        data.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        data.setUpdatedBy(actorUserId);
        data.setUpdatedAt(LocalDateTime.now(clock));
        portalCommandMapper.upsertTrendingTopicRule(data);
        auditLogService.record("UPSERT_TRENDING_RULE", actorUserId, "TRENDING_TOPIC", null, data.getTopicKey());
    }

    private List<TrendingTopicSnapshot> buildRankings() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<PostData> posts = portalQueryMapper.selectRecentPostsForTrending(now.minusHours(WINDOW_HOURS));
        Map<String, TrendingTopicRuleData> rulesByKey = new LinkedHashMap<>();
        for (TrendingTopicRuleData rule : portalQueryMapper.selectTrendingTopicRules()) {
            rulesByKey.put(normalizeKey(rule.getTopicKey()), rule);
        }

        Map<String, TrendingAccumulator> accumulators = new LinkedHashMap<>();
        for (PostData post : posts) {
            List<String> candidates = extractCandidates(post);
            for (String candidate : candidates) {
                String candidateKey = normalizeKey(candidate);
                if (candidateKey.isBlank()) {
                    continue;
                }
                TrendingTopicRuleData candidateRule = rulesByKey.get(candidateKey);
                if (candidateRule != null && Boolean.TRUE.equals(candidateRule.getHiddenFlag())) {
                    continue;
                }
                String canonicalKey = candidateRule != null && candidateRule.getMergeTargetKey() != null
                        ? normalizeKey(candidateRule.getMergeTargetKey())
                        : candidateKey;
                TrendingTopicRuleData canonicalRule = rulesByKey.getOrDefault(canonicalKey, candidateRule);
                if (canonicalRule != null && Boolean.TRUE.equals(canonicalRule.getHiddenFlag())) {
                    continue;
                }

                TrendingAccumulator accumulator = accumulators.computeIfAbsent(
                        canonicalKey,
                        ignored -> new TrendingAccumulator(canonicalKey)
                );
                accumulator.displayName = resolveDisplayName(candidate, candidateRule, canonicalRule);
                accumulator.mergeTargetKey = canonicalRule == null ? null : normalizeNullable(canonicalRule.getMergeTargetKey());
                accumulator.hidden = canonicalRule != null && Boolean.TRUE.equals(canonicalRule.getHiddenFlag());
                accumulator.pinned = canonicalRule != null && Boolean.TRUE.equals(canonicalRule.getPinnedFlag());
                accumulator.sortOrder = canonicalRule == null || canonicalRule.getSortOrder() == null ? 0 : canonicalRule.getSortOrder();
                accumulator.postCount += 1;
                accumulator.interactionCount += interactionCount(post);
                accumulator.uniqueAuthors.add(post.getCreatorUserId());
                accumulator.score += weightedScore(post, now);
            }
        }

        return accumulators.values().stream()
                .filter(accumulator -> !accumulator.hidden)
                .map(TrendingAccumulator::snapshot)
                .sorted(Comparator
                        .comparing(TrendingTopicSnapshot::pinned).reversed()
                        .thenComparing(TrendingTopicSnapshot::sortOrder)
                        .thenComparing(TrendingTopicSnapshot::score, Comparator.reverseOrder())
                        .thenComparing(TrendingTopicSnapshot::displayName))
                .toList();
    }

    private int interactionCount(PostData post) {
        return defaultInt(post.getLikeCount()) + defaultInt(post.getCommentCount()) + defaultInt(post.getSaveCount());
    }

    private int weightedScore(PostData post, LocalDateTime now) {
        int base = 5 + defaultInt(post.getCommentCount()) * 3 + defaultInt(post.getLikeCount()) + defaultInt(post.getSaveCount()) * 2;
        double factor = timeDecayFactor(post.getCreatedAt(), now);
        return (int) Math.round(base * factor);
    }

    private double timeDecayFactor(LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null) {
            return 0.35D;
        }
        long hours = Math.max(0, Duration.between(createdAt, now).toHours());
        if (hours < 6) {
            return 1.0D;
        }
        if (hours < 24) {
            return 0.8D;
        }
        if (hours < 48) {
            return 0.55D;
        }
        return 0.35D;
    }

    private List<String> extractCandidates(PostData post) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        addCandidates(candidates, post.getTitle());
        addCandidates(candidates, firstSentence(post.getContent()));
        return candidates.stream().limit(3).toList();
    }

    private void addCandidates(Set<String> target, String source) {
        if (source == null || source.isBlank()) {
            return;
        }
        Matcher matcher = HASH_TAG_PATTERN.matcher(source);
        while (matcher.find()) {
            String candidate = normalizeCandidate(matcher.group(1));
            if (isValidCandidate(candidate)) {
                target.add(candidate);
            }
        }
        for (String segment : SPLIT_PATTERN.split(source)) {
            String candidate = normalizeCandidate(segment);
            if (isValidCandidate(candidate)) {
                target.add(candidate);
            }
        }
    }

    private String firstSentence(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String[] parts = SPLIT_PATTERN.split(content);
        return parts.length == 0 ? content : parts[0];
    }

    private String normalizeCandidate(String raw) {
        if (raw == null) {
            return "";
        }
        String candidate = CLEAN_PATTERN.matcher(raw.trim()).replaceAll("");
        for (String prefix : COMMON_PREFIXES) {
            if (candidate.startsWith(prefix) && candidate.length() > prefix.length()) {
                candidate = candidate.substring(prefix.length());
            }
        }
        candidate = candidate.replace("求助", "").replace("前辈", "").replace("怎么办", "").replace("了吗", "").replace("吗", "");
        return candidate.trim();
    }

    private boolean isValidCandidate(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        if (candidate.length() < 2 || candidate.length() > 12) {
            return false;
        }
        return !CHANNEL_NAMES.contains(candidate);
    }

    private String resolveDisplayName(String candidate,
                                      TrendingTopicRuleData candidateRule,
                                      TrendingTopicRuleData canonicalRule) {
        if (candidateRule != null
                && candidateRule.getMergeTargetKey() != null
                && !candidateRule.getMergeTargetKey().isBlank()
                && (canonicalRule == null
                || normalizeKey(canonicalRule.getTopicKey()).equals(normalizeKey(candidateRule.getTopicKey())))) {
            return candidateRule.getMergeTargetKey();
        }
        if (canonicalRule != null && canonicalRule.getDisplayName() != null && !canonicalRule.getDisplayName().isBlank()) {
            return canonicalRule.getDisplayName();
        }
        if (candidateRule != null && candidateRule.getDisplayName() != null && !candidateRule.getDisplayName().isBlank()) {
            return candidateRule.getDisplayName();
        }
        return candidate;
    }

    private String defaultDisplayName(String topicKey, String displayName) {
        if (displayName != null && !displayName.isBlank()) {
            return displayName.trim();
        }
        return topicKey.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return normalizeKey(value);
    }

    private String normalizeKey(String value) {
        return value == null ? "" : CLEAN_PATTERN.matcher(value.trim().toLowerCase(Locale.ROOT)).replaceAll("");
    }

    private int defaultInt(Integer value) {
        return Objects.requireNonNullElse(value, 0);
    }

    private static final class TrendingAccumulator {
        private final String topicKey;
        private String displayName;
        private String mergeTargetKey;
        private boolean hidden;
        private boolean pinned;
        private int sortOrder;
        private int postCount;
        private int interactionCount;
        private int score;
        private final Set<Long> uniqueAuthors = new LinkedHashSet<>();

        private TrendingAccumulator(String topicKey) {
            this.topicKey = topicKey;
            this.displayName = topicKey;
        }

        private TrendingTopicSnapshot snapshot() {
            return new TrendingTopicSnapshot(
                    topicKey,
                    displayName,
                    mergeTargetKey,
                    hidden,
                    pinned,
                    sortOrder,
                    postCount,
                    interactionCount,
                    uniqueAuthors.size(),
                    score + uniqueAuthors.size() * 4
            );
        }
    }

    private record TrendingTopicSnapshot(
            String topicKey,
            String displayName,
            String mergeTargetKey,
            boolean hidden,
            boolean pinned,
            int sortOrder,
            int postCount,
            int interactionCount,
            int uniqueAuthorCount,
            int score
    ) {
        private String heatText() {
            return score + " 热度";
        }
    }
}
