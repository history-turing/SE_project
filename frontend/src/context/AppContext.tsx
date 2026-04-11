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
  getHomePage,
  getProfilePage,
  getTopicsPage,
  markConversationRead,
  sendMessage as sendMessageRequest,
  toggleFollow as toggleFollowRequest,
  toggleLike as toggleLikeRequest,
  toggleSave as toggleSaveRequest,
} from '../services/api';
import type {
  AlumniContact,
  ComposePayload,
  Conversation,
  FeedPost,
  HomeStats,
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
  conversations: Conversation[];
  activeConversationId: string;
  profile: UserProfile;
  refreshHomeStats: () => Promise<void>;
  composePost: (payload: ComposePayload) => Promise<boolean>;
  toggleLike: (postId: string) => Promise<void>;
  toggleSave: (postId: string) => Promise<void>;
  removePost: (postId: string) => void;
  setPostCommentCount: (postId: string, count: number) => void;
  toggleFollow: (userId: string) => Promise<void>;
  selectConversation: (conversationId: string) => Promise<void>;
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
  const [conversations, setConversations] = useState(initialConversations);
  const [activeConversationId, setActiveConversationId] = useState(initialConversations[0]?.id ?? '');
  const [profile, setProfile] = useState(defaultProfile);

  useEffect(() => {
    let cancelled = false;

    async function bootstrap() {
      try {
        const [homePage, topicsPage, alumniPage, profilePage] = await Promise.all([
          getHomePage(),
          getTopicsPage(),
          getAlumniPage(),
          getProfilePage(),
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
        setLikedIds(extractIds(interactionSource, (post) => Boolean((post as FeedPost & { liked?: boolean }).liked)));
        setSavedIds(extractIds(interactionSource, (post) => Boolean((post as FeedPost & { saved?: boolean }).saved)));
        setFollowedIds(alumniPage.contacts.filter((contact) => Boolean((contact as AlumniContact & { followed?: boolean }).followed)).map((contact) => contact.id));
        setConversations(profilePage.conversations);
        setActiveConversationId(profilePage.activeConversationId);
        setProfile(profilePage.profile);
      } catch (error) {
        console.error('加载后端数据失败，当前继续使用前端默认数据。', error);
      }
    }

    void bootstrap();

    return () => {
      cancelled = true;
    };
  }, []);

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

  async function selectConversation(conversationId: string) {
    setActiveConversationId(conversationId);
    setConversations((current) =>
      current.map((conversation) =>
        conversation.id === conversationId ? { ...conversation, unreadCount: 0 } : conversation,
      ),
    );

    try {
      await markConversationRead(conversationId);
    } catch (error) {
      console.error('会话已读回写失败。', error);
    }
  }

  async function sendMessage(text: string) {
    const trimmed = text.trim();
    if (!trimmed || !activeConversationId) {
      return;
    }

    try {
      const message = await sendMessageRequest(activeConversationId, trimmed);
      setConversations((current) =>
        current.map((conversation) => {
          if (conversation.id !== activeConversationId) {
            return conversation;
          }

          return {
            ...conversation,
            time: message.time,
            lastMessage: message.text,
            unreadCount: 0,
            messages: [...conversation.messages, message],
          };
        }),
      );
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
        activeConversationId,
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
