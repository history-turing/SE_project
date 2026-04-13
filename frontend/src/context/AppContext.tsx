import { createContext, useContext, useEffect, useRef, useState, type ReactNode } from 'react';
import {
  alumniContacts as defaultAlumniContacts,
  alumniStories as defaultAlumniStories,
  campusNotices as defaultCampusNotices,
  initialAlumniPosts,
  initialCommunityPosts,
  initialMyPosts,
  profile as defaultProfile,
  topicGroups as defaultTopicGroups,
  topicRankings as defaultTopicRankings,
} from '../data/siteData';
import { useAuthContext } from './AuthContext';
import {
  AUTH_TOKEN_STORAGE_KEY,
  createPost as createPostRequest,
  getAlumniPage,
  getDmConversationDetail,
  getDmConversations,
  getHomePage,
  getProfilePage,
  getTopicsPage,
  markDmConversationRead,
  recallDmMessage,
  sendDmMessage,
  toggleFollow as toggleFollowRequest,
  toggleLike as toggleLikeRequest,
  toggleSave as toggleSaveRequest,
} from '../services/api';
import {
  createEmptyNotificationSummary,
} from '../mappers/messageViewModels';
import { createMessageRealtimeClient } from '../realtime/messageRealtimeClient';
import type {
  AlumniContact,
  ComposePayload,
  DmConversationDetail,
  DmConversationSummary,
  FeedPost,
  HomeStats,
  Message,
  NoticeItem,
  NotificationSummary,
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
  notificationSummary: NotificationSummary;
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
  recallMessage: (messageId: string) => Promise<void>;
}

const HOME_AUDIENCE = '首页';
const ALUMNI_AUDIENCE = '校友圈';

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
    conversationType: detail.conversationType,
    peer: detail.peer,
    lastMessage: detail.lastMessage,
    displayTime: detail.displayTime,
    unreadCount: detail.unreadCount,
  });
}

function upsertMessage(messages: Message[], nextMessage: Message) {
  const existingIndex = messages.findIndex((message) => message.id === nextMessage.id);
  if (existingIndex === -1) {
    return [...messages, nextMessage];
  }

  const nextMessages = [...messages];
  nextMessages[existingIndex] = {
    ...nextMessages[existingIndex],
    ...nextMessage,
  };
  return nextMessages;
}

function buildNotificationSummaryFromConversations(
  conversations: DmConversationSummary[],
): NotificationSummary {
  const messagesUnread = conversations.reduce(
    (total, conversation) => total + conversation.unreadCount,
    0,
  );

  return {
    messagesUnread,
    interactionsUnread: 0,
    systemUnread: 0,
    totalUnread: messagesUnread,
    hasUnread: messagesUnread > 0,
  };
}

function decrementNotificationSummary(
  summary: NotificationSummary,
  readCount: number,
): NotificationSummary {
  const messagesUnread = Math.max(0, summary.messagesUnread - readCount);
  return {
    ...summary,
    messagesUnread,
    totalUnread:
      messagesUnread + summary.interactionsUnread + summary.systemUnread,
    hasUnread:
      messagesUnread + summary.interactionsUnread + summary.systemUnread > 0,
  };
}

function syncFeedState(args: {
  homePosts?: FeedPost[];
  alumniPosts?: FeedPost[];
  myPosts?: FeedPost[];
  savedPosts?: FeedPost[];
}) {
  const myPosts = args.myPosts ?? [];
  const savedPosts = args.savedPosts ?? [];
  const communityPosts = uniquePosts([
    ...(args.homePosts ?? []),
    ...myPosts.filter((post) => post.audience === HOME_AUDIENCE),
    ...savedPosts.filter((post) => post.audience === HOME_AUDIENCE),
  ]);
  const alumniPosts = uniquePosts([
    ...(args.alumniPosts ?? []),
    ...myPosts.filter((post) => post.audience === ALUMNI_AUDIENCE),
    ...savedPosts.filter((post) => post.audience === ALUMNI_AUDIENCE),
  ]);
  const interactionSource = uniquePosts([...communityPosts, ...alumniPosts, ...myPosts, ...savedPosts]);

  return {
    communityPosts,
    alumniPosts,
    likedIds: extractIds(
      interactionSource,
      (post) => Boolean((post as FeedPost & { liked?: boolean }).liked),
    ),
    savedIds: extractIds(
      interactionSource,
      (post) => Boolean((post as FeedPost & { saved?: boolean }).saved),
    ),
  };
}

export function AppProvider({ children }: { children: ReactNode }) {
  const { user } = useAuthContext();
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
  const [conversations, setConversations] = useState<DmConversationSummary[]>([]);
  const [activeConversationCode, setActiveConversationCode] = useState('');
  const [activeConversation, setActiveConversation] = useState<DmConversationDetail | null>(null);
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [notificationSummary, setNotificationSummary] = useState<NotificationSummary>(
    createEmptyNotificationSummary(),
  );
  const [profile, setProfile] = useState(defaultProfile);
  const activeConversationCodeRef = useRef('');
  const activeConversationRef = useRef<DmConversationDetail | null>(null);

  useEffect(() => {
    activeConversationCodeRef.current = activeConversationCode;
    activeConversationRef.current = activeConversation;
  }, [activeConversation, activeConversationCode]);

  useEffect(() => {
    let cancelled = false;

    async function bootstrap() {
      const [homePageResult, topicsPageResult, alumniPageResult, profilePageResult, dmConversationsResult] =
        await Promise.allSettled([
          getHomePage(),
          getTopicsPage(),
          getAlumniPage(),
          getProfilePage(),
          getDmConversations(),
        ]);

      if (cancelled) {
        return;
      }

      const homePage = homePageResult.status === 'fulfilled' ? homePageResult.value : null;
      const topicsPage = topicsPageResult.status === 'fulfilled' ? topicsPageResult.value : null;
      const alumniPage = alumniPageResult.status === 'fulfilled' ? alumniPageResult.value : null;
      const profilePage = profilePageResult.status === 'fulfilled' ? profilePageResult.value : null;
      const dmConversations =
        dmConversationsResult.status === 'fulfilled' ? dmConversationsResult.value : null;

      if (topicsPage) {
        setTopicGroups(topicsPage.topics);
        setTopicRankings(topicsPage.rankings);
      }

      if (homePage) {
        setCampusNotices(homePage.notices);
        setHomeStats(homePage.stats);
      }

      if (alumniPage) {
        setAlumniStories(alumniPage.stories);
        setAlumniContacts(alumniPage.contacts);
        setFollowedIds(
          alumniPage.contacts
            .filter((contact) => Boolean((contact as AlumniContact & { followed?: boolean }).followed))
            .map((contact) => contact.id),
        );
      }

      if (profilePage) {
        setMyPosts(profilePage.myPosts);
        setProfile(profilePage.profile);
      }

      if (homePage || alumniPage || profilePage) {
        const nextFeedState = syncFeedState({
          homePosts: homePage?.posts,
          alumniPosts: alumniPage?.posts,
          myPosts: profilePage?.myPosts,
          savedPosts: profilePage?.savedPosts,
        });

        if (nextFeedState.communityPosts.length) {
          setCommunityPosts(nextFeedState.communityPosts);
        }
        if (nextFeedState.alumniPosts.length) {
          setAlumniPosts(nextFeedState.alumniPosts);
        }
        setLikedIds(nextFeedState.likedIds);
        setSavedIds(nextFeedState.savedIds);
      }

      const nextConversations = dmConversations ?? [];
      setConversations(nextConversations);
      setNotificationSummary(buildNotificationSummaryFromConversations(nextConversations));
      setActiveConversationCode('');
      setActiveConversation(null);
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

      setMessagesLoading(true);
      try {
        const detail = await getDmConversationDetail(activeConversationCode);
        if (cancelled) {
          return;
        }

        const unreadBeforeOpen = detail.unreadCount;
        const normalizedDetail =
          unreadBeforeOpen > 0
            ? {
                ...detail,
                unreadCount: 0,
              }
            : detail;

        setActiveConversation(normalizedDetail);
        setConversations((current) => updateConversationListFromDetail(current, normalizedDetail));
        if (unreadBeforeOpen > 0) {
          setNotificationSummary((current) =>
            decrementNotificationSummary(current, unreadBeforeOpen),
          );
          void markDmConversationRead(activeConversationCode).catch((error) => {
            console.error('mark dm conversation read failed', error);
          });
        }
      } catch (error) {
        console.error('load dm conversation detail failed', error);
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

  useEffect(() => {
    const token = window.localStorage.getItem(AUTH_TOKEN_STORAGE_KEY) ?? '';
    if (!user || !token || typeof WebSocket === 'undefined') {
      return;
    }

    const client = createMessageRealtimeClient({
      token,
      onEvent: (event) => {
        const state = event.recipientStates.find((item) => item.userId === user.id);
        if (!state) {
          return;
        }

        setConversations((current) => mergeConversationSummary(current, state.conversation));
        setNotificationSummary(state.unreadNotification);

        if (activeConversationCodeRef.current !== event.conversationCode) {
          return;
        }

        setActiveConversation((current) => {
          const baseConversation = current?.conversationCode === event.conversationCode
            ? current
            : null;
          const nextMessages = state.message
            ? upsertMessage(baseConversation?.messages ?? [], state.message)
            : baseConversation?.messages ?? [];

          return {
            conversationCode: state.conversation.conversationCode,
            conversationType: state.conversation.conversationType,
            peer: state.conversation.peer,
            status: baseConversation?.status ?? 'ACTIVE',
            messages: nextMessages,
            lastMessage: state.conversation.lastMessage,
            displayTime: state.conversation.displayTime,
            unreadCount: state.conversation.unreadCount,
          };
        });
      },
    });

    client.connect();
    return () => {
      client.disconnect();
    };
  }, [user]);

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
      console.error('refresh home stats failed', error);
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
      console.error('toggle like failed', error);
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
      console.error('toggle save failed', error);
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
      console.error('toggle follow failed', error);
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
      if (payload.audience === HOME_AUDIENCE) {
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
      console.error('compose post failed', error);
      return false;
    }
  }

  async function selectConversation(conversationCode: string) {
    let unreadCount = 0;
    setActiveConversationCode(conversationCode);
    setConversations((current) =>
      current.map((conversation) => {
        if (conversation.conversationCode === conversationCode) {
          unreadCount = conversation.unreadCount;
          return { ...conversation, unreadCount: 0 };
        }
        return conversation;
      }),
    );
    if (unreadCount > 0) {
      setNotificationSummary((current) => decrementNotificationSummary(current, unreadCount));
    }
  }

  async function sendMessage(text: string) {
    const trimmed = text.trim();
    if (!trimmed || !activeConversationCode) {
      return;
    }

    try {
      const message = await sendDmMessage(activeConversationCode, trimmed);
      const peer =
        activeConversation?.peer ??
        conversations.find((conversation) => conversation.conversationCode === activeConversationCode)?.peer ?? {
          userCode: '',
          name: '私信会话',
          subtitle: '',
          avatar: '',
        };
      const nextMessages = upsertMessage(activeConversation?.messages ?? [], message);
      const nextDetail: DmConversationDetail = {
        conversationCode: activeConversationCode,
        conversationType:
          activeConversation?.conversationType ??
          conversations.find((conversation) => conversation.conversationCode === activeConversationCode)
            ?.conversationType ??
          'DIRECT',
        peer,
        status: activeConversation?.status ?? 'ACTIVE',
        messages: nextMessages,
        lastMessage: message.text,
        displayTime: message.time,
        unreadCount: 0,
      };
      setActiveConversation(nextDetail);
      setConversations((current) => updateConversationListFromDetail(current, nextDetail));
    } catch (error) {
      console.error('send dm message failed', error);
    }
  }

  async function recallMessage(messageId: string) {
    if (!activeConversationCode) {
      return;
    }

    try {
      const recalledMessage = await recallDmMessage(activeConversationCode, messageId);
      const currentDetail = activeConversation;
      if (!currentDetail || currentDetail.conversationCode !== activeConversationCode) {
        return;
      }

      const nextMessages = upsertMessage(currentDetail.messages, recalledMessage);
      const lastMessage = nextMessages[nextMessages.length - 1];
      const nextDetail: DmConversationDetail = {
        ...currentDetail,
        messages: nextMessages,
        lastMessage: lastMessage?.text ?? currentDetail.lastMessage,
        displayTime: lastMessage?.time ?? currentDetail.displayTime,
      };
      setActiveConversation(nextDetail);
      setConversations((current) => updateConversationListFromDetail(current, nextDetail));
    } catch (error) {
      console.error('recall dm message failed', error);
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
        notificationSummary,
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
        recallMessage,
      }}
    >
      {children}
    </AppContext.Provider>
  );
}

export function useAppContext() {
  const context = useContext(AppContext);

  if (!context) {
    throw new Error('useAppContext must be used inside AppProvider');
  }

  return context;
}
