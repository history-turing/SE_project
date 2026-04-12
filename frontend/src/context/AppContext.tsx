import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import {
  alumniContacts as defaultAlumniContacts,
  alumniStories as defaultAlumniStories,
  campusNotices as defaultCampusNotices,
  initialAlumniPosts,
  initialCommunityPosts,
  initialConversations,
  initialMyPosts,
  profile as defaultProfile,
  topicGroups as defaultTopicGroups,
  topicRankings as defaultTopicRankings,
} from '../data/siteData';
import {
  createPost as createPostRequest,
  getAlumniPage,
  getDmConversationDetail,
  getDmConversations,
  getHomePage,
  getProfilePage,
  getTopicsPage,
  sendDmMessage,
  toggleFollow as toggleFollowRequest,
  toggleLike as toggleLikeRequest,
  toggleSave as toggleSaveRequest,
} from '../services/api';
import type {
  AlumniContact,
  ComposePayload,
  Conversation,
  DmConversationDetail,
  DmConversationSummary,
  FeedPost,
  HomeStats,
  Message,
  NoticeItem,
  RankingItem,
  StoryCard,
  TopicGroup,
  UserProfile,
} from '../types';

interface AppStateValue {
  communityPosts: FeedPost[];
  alumniPosts: FeedPost[];
  myPosts: FeedPost[];
  topicGroups: TopicGroup[];
  topicRankings: RankingItem[];
  campusNotices: NoticeItem[];
  homeStats: HomeStats;
  alumniStories: StoryCard[];
  alumniContacts: AlumniContact[];
  likedIds: string[];
  savedIds: string[];
  followedIds: string[];
  conversations: DmConversationSummary[];
  activeConversationCode: string;
  activeConversation: DmConversationDetail | null;
  messagesLoading: boolean;
  profile: UserProfile;
  refreshHomeStats: () => Promise<void>;
  composePost: (payload: ComposePayload) => Promise<boolean>;
  toggleLike: (postId: string) => Promise<void>;
  toggleSave: (postId: string) => Promise<void>;
  removePost: (postId: string) => void;
  setPostCommentCount: (postId: string, count: number) => void;
  toggleFollow: (userId: string) => Promise<void>;
  selectConversation: (conversationCode: string) => Promise<void>;
  sendMessage: (text: string) => Promise<void>;
}

const AppContext = createContext<AppStateValue | null>(null);
const defaultHomeStats: HomeStats = {
  treeholeUpdates: String(initialCommunityPosts.length),
  hotTopics: String(defaultTopicGroups.length),
  alumniPosts: String(initialAlumniPosts.length),
};

function bumpCount(value: number, add: boolean) {
  return add ? value + 1 : Math.max(0, value - 1);
}

function uniquePosts(posts: FeedPost[]) {
  const map = new Map<string, FeedPost>();
  posts.forEach((post) => {
    if (!map.has(post.id)) {
      map.set(post.id, post);
    }
  });
  return Array.from(map.values());
}

function extractIds(posts: FeedPost[], matcher: (post: FeedPost) => boolean) {
  return Array.from(new Set(posts.filter(matcher).map((post) => post.id)));
}

function incrementStat(value: string) {
  const parsed = Number.parseInt(value, 10);
  return String(Number.isNaN(parsed) ? 1 : parsed + 1);
}

function toDmSummary(conversation: Conversation): DmConversationSummary {
  return {
    conversationCode: conversation.id,
    peer: {
      userCode: '',
      name: conversation.name,
      subtitle: conversation.subtitle,
      avatar: conversation.avatar,
    },
    lastMessage: conversation.lastMessage,
    displayTime: conversation.time,
    unreadCount: conversation.unreadCount,
  };
}

function toDmDetail(conversation: Conversation): DmConversationDetail {
  return {
    ...toDmSummary(conversation),
    status: 'ACTIVE',
    messages: conversation.messages,
  };
}

function mergeConversationSummary(
  current: DmConversationSummary[],
  nextConversation: DmConversationSummary,
) {
  const withoutCurrent = current.filter(
    (conversation) => conversation.conversationCode !== nextConversation.conversationCode,
  );
  return [nextConversation, ...withoutCurrent];
}

function updateConversationListFromDetail(
  current: DmConversationSummary[],
  detail: DmConversationDetail,
): DmConversationSummary[] {
  return mergeConversationSummary(current, {
    conversationCode: detail.conversationCode,
    peer: detail.peer,
    lastMessage: detail.lastMessage,
    displayTime: detail.displayTime,
    unreadCount: detail.unreadCount,
  });
}

export function AppProvider({ children }: { children: ReactNode }) {
  const [communityPosts, setCommunityPosts] = useState(initialCommunityPosts);
  const [alumniPosts, setAlumniPosts] = useState(initialAlumniPosts);
  const [myPosts, setMyPosts] = useState(initialMyPosts);
  const [topicGroups, setTopicGroups] = useState(defaultTopicGroups);
  const [topicRankings, setTopicRankings] = useState(defaultTopicRankings);
  const [campusNotices, setCampusNotices] = useState(defaultCampusNotices);
  const [homeStats, setHomeStats] = useState(defaultHomeStats);
  const [alumniStories, setAlumniStories] = useState(defaultAlumniStories);
  const [alumniContacts, setAlumniContacts] = useState(defaultAlumniContacts);
  const [likedIds, setLikedIds] = useState<string[]>([]);
  const [savedIds, setSavedIds] = useState<string[]>([]);
  const [followedIds, setFollowedIds] = useState<string[]>([]);
  const [conversations, setConversations] = useState<DmConversationSummary[]>(
    initialConversations.map(toDmSummary),
  );
  const [activeConversationCode, setActiveConversationCode] = useState(
    initialConversations[0]?.id ?? '',
  );
  const [activeConversation, setActiveConversation] = useState<DmConversationDetail | null>(
    initialConversations[0] ? toDmDetail(initialConversations[0]) : null,
  );
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [profile, setProfile] = useState(defaultProfile);

  useEffect(() => {
    let cancelled = false;

    async function bootstrap() {
      try {
        const [homePage, topicsPage, alumniPage, profilePage, dmConversations] = await Promise.all([
          getHomePage(),
          getTopicsPage(),
          getAlumniPage(),
          getProfilePage(),
          getDmConversations().catch(() => []),
        ]);

        if (cancelled) {
          return;
        }

        const mergedCommunityPosts = uniquePosts([
          ...homePage.posts,
          ...profilePage.myPosts.filter((post) => post.audience === '首页'),
          ...profilePage.savedPosts.filter((post) => post.audience === '首页'),
        ]);
        const mergedAlumniPosts = uniquePosts([
          ...alumniPage.posts,
          ...profilePage.myPosts.filter((post) => post.audience === '校友圈'),
          ...profilePage.savedPosts.filter((post) => post.audience === '校友圈'),
        ]);
        const interactionSource = uniquePosts([
          ...mergedCommunityPosts,
          ...mergedAlumniPosts,
          ...profilePage.myPosts,
          ...profilePage.savedPosts,
        ]);

        setCommunityPosts(mergedCommunityPosts);
        setAlumniPosts(mergedAlumniPosts);
        setMyPosts(profilePage.myPosts);
        setTopicGroups(topicsPage.topics);
        setTopicRankings(topicsPage.rankings);
        setCampusNotices(homePage.notices);
        setHomeStats(homePage.stats);
        setAlumniStories(alumniPage.stories);
        setAlumniContacts(alumniPage.contacts);
        setLikedIds(
          extractIds(interactionSource, (post) => Boolean((post as FeedPost & { liked?: boolean }).liked)),
        );
        setSavedIds(
          extractIds(interactionSource, (post) => Boolean((post as FeedPost & { saved?: boolean }).saved)),
        );
        setFollowedIds(
          alumniPage.contacts
            .filter((contact) => Boolean((contact as AlumniContact & { followed?: boolean }).followed))
            .map((contact) => contact.id),
        );
        setProfile(profilePage.profile);

        if (dmConversations.length) {
          const firstConversation = dmConversations[0];
          setConversations(dmConversations);
          setActiveConversationCode(firstConversation.conversationCode);
        } else {
          setConversations([]);
          setActiveConversationCode('');
          setActiveConversation(null);
        }
      } catch (error) {
        console.error('加载后端数据失败，当前继续使用前端默认数据。', error);
      }
    }

    void bootstrap();

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function loadConversationDetail() {
      if (!activeConversationCode) {
        setActiveConversation(null);
        return;
      }

      const fallbackConversation = initialConversations.find(
        (conversation) => conversation.id === activeConversationCode,
      );

      setMessagesLoading(true);
      try {
        const detail = await getDmConversationDetail(activeConversationCode);
        if (cancelled) {
          return;
        }
        setActiveConversation(detail);
        setConversations((current) => updateConversationListFromDetail(current, detail));
      } catch (error) {
        console.error('加载私信会话详情失败。', error);
        if (!cancelled && fallbackConversation) {
          setActiveConversation(toDmDetail(fallbackConversation));
        }
      } finally {
        if (!cancelled) {
          setMessagesLoading(false);
        }
      }
    }

    void loadConversationDetail();

    return () => {
      cancelled = true;
    };
  }, [activeConversationCode]);

  function updatePostEverywhere(postId: string, updater: (post: FeedPost) => FeedPost) {
    setCommunityPosts((current) => current.map((post) => (post.id === postId ? updater(post) : post)));
    setAlumniPosts((current) => current.map((post) => (post.id === postId ? updater(post) : post)));
    setMyPosts((current) => current.map((post) => (post.id === postId ? updater(post) : post)));
  }

  async function refreshHomeStats() {
    try {
      const page = await getHomePage();
      setHomeStats(page.stats);
    } catch (error) {
      console.error('首页统计刷新失败。', error);
    }
  }

  async function toggleLike(postId: string) {
    try {
      const result = await toggleLikeRequest(postId);
      setLikedIds((current) =>
        result.active ? [...new Set([...current, postId])] : current.filter((id) => id !== postId),
      );
      updatePostEverywhere(postId, (post) => ({
        ...post,
        likes: result.count ?? bumpCount(post.likes, result.active),
      }));
    } catch (error) {
      console.error('点赞操作失败。', error);
    }
  }

  async function toggleSave(postId: string) {
    try {
      const result = await toggleSaveRequest(postId);
      setSavedIds((current) =>
        result.active ? [...new Set([...current, postId])] : current.filter((id) => id !== postId),
      );
      updatePostEverywhere(postId, (post) => ({
        ...post,
        saves: result.count ?? bumpCount(post.saves, result.active),
      }));
    } catch (error) {
      console.error('收藏操作失败。', error);
    }
  }

  async function toggleFollow(userId: string) {
    try {
      const result = await toggleFollowRequest(userId);
      setFollowedIds((current) =>
        result.active ? [...new Set([...current, userId])] : current.filter((id) => id !== userId),
      );
      setAlumniContacts((current) =>
        current.map((contact) =>
          contact.id === userId ? { ...contact, followed: result.active } : contact,
        ),
      );
    } catch (error) {
      console.error('关注操作失败。', error);
    }
  }

  function setPostCommentCount(postId: string, count: number) {
    updatePostEverywhere(postId, (post) => ({
      ...post,
      comments: count,
    }));
  }

  function removePost(postId: string) {
    setCommunityPosts((current) => current.filter((post) => post.id !== postId));
    setAlumniPosts((current) => current.filter((post) => post.id !== postId));
    setMyPosts((current) => current.filter((post) => post.id !== postId));
  }

  async function composePost(payload: ComposePayload) {
    if (!payload.content.trim()) {
      return false;
    }

    try {
      const nextPost = await createPostRequest(payload);
      if (payload.audience === '首页') {
        setCommunityPosts((current) => [nextPost, ...current]);
        setHomeStats((current) => ({
          ...current,
          treeholeUpdates: incrementStat(current.treeholeUpdates),
        }));
      } else {
        setAlumniPosts((current) => [nextPost, ...current]);
        setHomeStats((current) => ({
          ...current,
          alumniPosts: incrementStat(current.alumniPosts),
        }));
      }
      setMyPosts((current) => [nextPost, ...current]);
      return true;
    } catch (error) {
      console.error('发布树洞失败。', error);
      return false;
    }
  }

  async function selectConversation(conversationCode: string) {
    setActiveConversationCode(conversationCode);
    setConversations((current) =>
      current.map((conversation) =>
        conversation.conversationCode === conversationCode
          ? { ...conversation, unreadCount: 0 }
          : conversation,
      ),
    );
  }

  async function sendMessage(text: string) {
    const trimmed = text.trim();
    if (!trimmed || !activeConversationCode) {
      return;
    }

    try {
      const message = await sendDmMessage(activeConversationCode, trimmed);
      const nextMessages: Message[] = [...(activeConversation?.messages ?? []), message];
      const nextDetail: DmConversationDetail = {
        conversationCode: activeConversationCode,
        peer:
          activeConversation?.peer ??
          conversations.find((conversation) => conversation.conversationCode === activeConversationCode)?.peer ?? {
            userCode: '',
            name: '私信会话',
            subtitle: '',
            avatar: '',
          },
        status: activeConversation?.status ?? 'ACTIVE',
        messages: nextMessages,
        lastMessage: message.text,
        displayTime: message.time,
        unreadCount: 0,
      };
      setActiveConversation(nextDetail);
      setConversations((current) => updateConversationListFromDetail(current, nextDetail));
    } catch (error) {
      console.error('发送消息失败。', error);
    }
  }

  return (
    <AppContext.Provider
      value={{
        communityPosts,
        alumniPosts,
        myPosts,
        topicGroups,
        topicRankings,
        campusNotices,
        homeStats,
        alumniStories,
        alumniContacts,
        likedIds,
        savedIds,
        followedIds,
        conversations,
        activeConversationCode,
        activeConversation,
        messagesLoading,
        profile,
        refreshHomeStats,
        composePost,
        toggleLike,
        toggleSave,
        removePost,
        setPostCommentCount,
        toggleFollow,
        selectConversation,
        sendMessage,
      }}
    >
      {children}
    </AppContext.Provider>
  );
}

export function useAppContext() {
  const context = useContext(AppContext);

  if (!context) {
    throw new Error('useAppContext 必须在 AppProvider 内使用');
  }

  return context;
}
