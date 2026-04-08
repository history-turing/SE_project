import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import {
  initialAlumniPosts,
  initialCommunityPosts,
  initialConversations,
  initialMyPosts,
  profile,
} from '../data/siteData';
import type { ComposePayload, Conversation, FeedPost, UserProfile } from '../types';

interface AppStateValue {
  communityPosts: FeedPost[];
  alumniPosts: FeedPost[];
  myPosts: FeedPost[];
  likedIds: string[];
  savedIds: string[];
  followedIds: string[];
  conversations: Conversation[];
  activeConversationId: string;
  profile: UserProfile;
  composePost: (payload: ComposePayload) => boolean;
  toggleLike: (postId: string) => void;
  toggleSave: (postId: string) => void;
  toggleFollow: (userId: string) => void;
  selectConversation: (conversationId: string) => void;
  sendMessage: (text: string) => void;
}

interface Snapshot {
  communityPosts: FeedPost[];
  alumniPosts: FeedPost[];
  myPosts: FeedPost[];
  likedIds: string[];
  savedIds: string[];
  followedIds: string[];
  conversations: Conversation[];
  activeConversationId: string;
}

const STORAGE_KEY = 'whu-treehole::state';

const defaultSnapshot: Snapshot = {
  communityPosts: initialCommunityPosts,
  alumniPosts: initialAlumniPosts,
  myPosts: initialMyPosts,
  likedIds: [],
  savedIds: [],
  followedIds: [],
  conversations: initialConversations,
  activeConversationId: initialConversations[0]?.id ?? '',
};

const AppContext = createContext<AppStateValue | null>(null);

function bumpCount(value: number, add: boolean) {
  return add ? value + 1 : Math.max(0, value - 1);
}

function readSnapshot(): Snapshot {
  const raw = window.localStorage.getItem(STORAGE_KEY);

  if (!raw) {
    return defaultSnapshot;
  }

  try {
    const parsed = JSON.parse(raw) as Partial<Snapshot>;

    return {
      communityPosts: parsed.communityPosts ?? defaultSnapshot.communityPosts,
      alumniPosts: parsed.alumniPosts ?? defaultSnapshot.alumniPosts,
      myPosts: parsed.myPosts ?? defaultSnapshot.myPosts,
      likedIds: parsed.likedIds ?? defaultSnapshot.likedIds,
      savedIds: parsed.savedIds ?? defaultSnapshot.savedIds,
      followedIds: parsed.followedIds ?? defaultSnapshot.followedIds,
      conversations: parsed.conversations ?? defaultSnapshot.conversations,
      activeConversationId:
        parsed.activeConversationId ?? defaultSnapshot.activeConversationId,
    };
  } catch {
    return defaultSnapshot;
  }
}

export function AppProvider({ children }: { children: ReactNode }) {
  const [communityPosts, setCommunityPosts] = useState(defaultSnapshot.communityPosts);
  const [alumniPosts, setAlumniPosts] = useState(defaultSnapshot.alumniPosts);
  const [myPosts, setMyPosts] = useState(defaultSnapshot.myPosts);
  const [likedIds, setLikedIds] = useState(defaultSnapshot.likedIds);
  const [savedIds, setSavedIds] = useState(defaultSnapshot.savedIds);
  const [followedIds, setFollowedIds] = useState(defaultSnapshot.followedIds);
  const [conversations, setConversations] = useState(defaultSnapshot.conversations);
  const [activeConversationId, setActiveConversationId] = useState(
    defaultSnapshot.activeConversationId,
  );
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    const snapshot = readSnapshot();
    setCommunityPosts(snapshot.communityPosts);
    setAlumniPosts(snapshot.alumniPosts);
    setMyPosts(snapshot.myPosts);
    setLikedIds(snapshot.likedIds);
    setSavedIds(snapshot.savedIds);
    setFollowedIds(snapshot.followedIds);
    setConversations(snapshot.conversations);
    setActiveConversationId(snapshot.activeConversationId);
    setHydrated(true);
  }, []);

  useEffect(() => {
    if (!hydrated) {
      return;
    }

    const snapshot: Snapshot = {
      communityPosts,
      alumniPosts,
      myPosts,
      likedIds,
      savedIds,
      followedIds,
      conversations,
      activeConversationId,
    };

    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(snapshot));
  }, [
    activeConversationId,
    alumniPosts,
    communityPosts,
    conversations,
    followedIds,
    hydrated,
    likedIds,
    myPosts,
    savedIds,
  ]);

  function updatePostEverywhere(postId: string, updater: (post: FeedPost) => FeedPost) {
    setCommunityPosts((current) =>
      current.map((post) => (post.id === postId ? updater(post) : post)),
    );
    setAlumniPosts((current) =>
      current.map((post) => (post.id === postId ? updater(post) : post)),
    );
    setMyPosts((current) =>
      current.map((post) => (post.id === postId ? updater(post) : post)),
    );
  }

  function toggleLike(postId: string) {
    const willLike = !likedIds.includes(postId);

    setLikedIds((current) =>
      willLike ? [...current, postId] : current.filter((id) => id !== postId),
    );
    updatePostEverywhere(postId, (post) => ({
      ...post,
      likes: bumpCount(post.likes, willLike),
    }));
  }

  function toggleSave(postId: string) {
    const willSave = !savedIds.includes(postId);

    setSavedIds((current) =>
      willSave ? [...current, postId] : current.filter((id) => id !== postId),
    );
    updatePostEverywhere(postId, (post) => ({
      ...post,
      saves: bumpCount(post.saves, willSave),
    }));
  }

  function toggleFollow(userId: string) {
    setFollowedIds((current) =>
      current.includes(userId)
        ? current.filter((id) => id !== userId)
        : [...current, userId],
    );
  }

  function composePost(payload: ComposePayload) {
    if (!payload.content.trim()) {
      return false;
    }

    const nextPost: FeedPost = {
      id: `post-${Date.now()}`,
      title: payload.title.trim() || undefined,
      content: payload.content.trim(),
      author: payload.anonymous ? '匿名珞珈人' : profile.name,
      handle: payload.anonymous ? '低语模式' : `${profile.college} · ${profile.year}`,
      topic: payload.topic,
      audience: payload.audience,
      createdAt: '刚刚',
      likes: 0,
      comments: 0,
      saves: 0,
      accent: payload.audience === '校友圈' ? 'jade' : 'rose',
      badge: payload.audience === '校友圈' ? '新发布' : undefined,
      anonymous: payload.anonymous,
    };

    setCommunityPosts((current) => [nextPost, ...current]);
    if (payload.audience === '校友圈') {
      setAlumniPosts((current) => [nextPost, ...current]);
    }
    setMyPosts((current) => [nextPost, ...current]);

    return true;
  }

  function selectConversation(conversationId: string) {
    setActiveConversationId(conversationId);
    setConversations((current) =>
      current.map((conversation) =>
        conversation.id === conversationId
          ? { ...conversation, unreadCount: 0 }
          : conversation,
      ),
    );
  }

  function sendMessage(text: string) {
    const trimmed = text.trim();

    if (!trimmed || !activeConversationId) {
      return;
    }

    const time = new Date().toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
    });

    setConversations((current) =>
      current.map((conversation) => {
        if (conversation.id !== activeConversationId) {
          return conversation;
        }

        return {
          ...conversation,
          time,
          lastMessage: trimmed,
          messages: [
            ...conversation.messages,
            {
              id: `${conversation.id}-${Date.now()}`,
              sender: 'me',
              text: trimmed,
              time,
            },
          ],
        };
      }),
    );
  }

  return (
    <AppContext.Provider
      value={{
        communityPosts,
        alumniPosts,
        myPosts,
        likedIds,
        savedIds,
        followedIds,
        conversations,
        activeConversationId,
        profile,
        composePost,
        toggleLike,
        toggleSave,
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
