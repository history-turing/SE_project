import userEvent from '@testing-library/user-event';
import { render, screen, waitFor } from '@testing-library/react';
import { AppProvider, useAppContext } from './AppContext';
import type { FeedPost } from '../types';

const apiMocks = vi.hoisted(() => ({
  getHomePage: vi.fn(),
  getTopicsPage: vi.fn(),
  getAlumniPage: vi.fn(),
  getProfilePage: vi.fn(),
  getDmConversations: vi.fn(),
  getDmConversationDetail: vi.fn(),
  markDmConversationRead: vi.fn(),
  sendDmMessage: vi.fn(),
  recallDmMessage: vi.fn(),
  createPost: vi.fn(),
  toggleFollow: vi.fn(),
  toggleLike: vi.fn(),
  toggleSave: vi.fn(),
}));

vi.mock('../services/api', () => ({
  getHomePage: apiMocks.getHomePage,
  getTopicsPage: apiMocks.getTopicsPage,
  getAlumniPage: apiMocks.getAlumniPage,
  getProfilePage: apiMocks.getProfilePage,
  getDmConversations: apiMocks.getDmConversations,
  getDmConversationDetail: apiMocks.getDmConversationDetail,
  markDmConversationRead: apiMocks.markDmConversationRead,
  sendDmMessage: apiMocks.sendDmMessage,
  recallDmMessage: apiMocks.recallDmMessage,
  createPost: apiMocks.createPost,
  toggleFollow: apiMocks.toggleFollow,
  toggleLike: apiMocks.toggleLike,
  toggleSave: apiMocks.toggleSave,
  AUTH_TOKEN_STORAGE_KEY: 'whu-treehole-token',
}));

vi.mock('../context/AuthContext', () => ({
  useAuthContext: () => ({
    user: {
      id: 7,
      userCode: 'user-7',
      username: 'xiewei',
      email: 'xiewei@whu.edu.cn',
      name: 'xiewei',
      avatar: 'https://example.com/avatar.png',
      accountStatus: 'ACTIVE',
      roles: [],
      permissions: [],
    },
    loading: false,
    isAuthenticated: true,
  }),
}));

function Probe() {
  const { topicRankings, profile, conversations, notificationSummary, homeStats, composePost } =
    useAppContext();

  return (
    <div>
      <span data-testid="first-ranking">{topicRankings[0]?.label ?? 'none'}</span>
      <span data-testid="profile-name">{profile.name}</span>
      <span data-testid="conversation-count">{String(conversations.length)}</span>
      <span data-testid="message-unread">{String(notificationSummary.messagesUnread)}</span>
      <span data-testid="treehole-updates">{homeStats.treeholeUpdates}</span>
      <span data-testid="alumni-posts">{homeStats.alumniPosts}</span>
      <button
        type="button"
        onClick={() =>
          void composePost({
            title: 'new-post',
            content: 'new-content',
            topic: 'topic',
            audience: '首页',
            anonymous: true,
          })
        }
      >
        compose-home
      </button>
    </div>
  );
}

function createFeedPost(id: string, audience: FeedPost['audience'] = '首页'): FeedPost {
  return {
    id,
    title: `${id}-title`,
    content: `${id}-content`,
    author: 'xiewei',
    authorUserCode: 'user-7',
    handle: 'handle',
    topic: 'topic',
    audience,
    createdAt: '2026-04-12 10:00',
    likes: 0,
    comments: 0,
    saves: 0,
    accent: 'rose',
  };
}

describe('AppProvider bootstrap', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    apiMocks.getHomePage.mockRejectedValue(new Error('home failed'));
    apiMocks.getTopicsPage.mockResolvedValue({
      topics: [],
      rankings: [{ id: 'rank-1', label: '#real-trending', heat: '100 heat' }],
    });
    apiMocks.getAlumniPage.mockResolvedValue({
      stories: [],
      contacts: [],
      posts: [createFeedPost('alumni-1', '校友圈')],
    });
    apiMocks.getProfilePage.mockResolvedValue({
      profile: {
        userCode: 'user-7',
        name: 'real-user',
        tagline: 'real-tagline',
        college: 'WHU',
        year: '2022',
        bio: 'real-bio',
        avatar: '',
        badges: [],
        stats: [],
      },
      myPosts: [],
      savedPosts: [],
      conversations: [],
      activeConversationId: '',
    });
    apiMocks.getDmConversations.mockResolvedValue([]);
    apiMocks.getDmConversationDetail.mockResolvedValue({
      conversationCode: 'dm-1',
      conversationType: 'DIRECT',
      status: 'ACTIVE',
      peer: {
        userCode: 'user-9',
        name: 'tester',
        subtitle: 'WHU',
        avatarUrl: '',
      },
      lastMessage: 'hello',
      lastMessageTime: '21:45',
      unreadCount: 0,
      messages: [],
    });
    apiMocks.markDmConversationRead.mockResolvedValue(undefined);
    apiMocks.sendDmMessage.mockResolvedValue(undefined);
    apiMocks.recallDmMessage.mockResolvedValue(undefined);
    apiMocks.createPost.mockResolvedValue(createFeedPost('post-1'));
    apiMocks.toggleFollow.mockResolvedValue({ active: true });
    apiMocks.toggleLike.mockResolvedValue({ active: true, count: 1 });
    apiMocks.toggleSave.mockResolvedValue({ active: true, count: 1 });
  });

  test('starts with empty live dm state before api resolves and keeps successful sections when home fails', async () => {
    render(
      <AppProvider>
        <Probe />
      </AppProvider>,
    );

    expect(screen.getByTestId('conversation-count')).toHaveTextContent('0');
    expect(screen.getByTestId('message-unread')).toHaveTextContent('0');

    await waitFor(() => {
      expect(screen.getByTestId('first-ranking')).toHaveTextContent('#real-trending');
      expect(screen.getByTestId('profile-name')).toHaveTextContent('real-user');
    });
  });

  test('treats 首页 posts as home-feed content when composing new posts', async () => {
    const user = userEvent.setup();

    render(
      <AppProvider>
        <Probe />
      </AppProvider>,
    );

    await waitFor(() => {
      expect(screen.getByTestId('profile-name')).toHaveTextContent('real-user');
    });

    const treeholeUpdatesBefore = Number(screen.getByTestId('treehole-updates').textContent);
    const alumniPostsBefore = Number(screen.getByTestId('alumni-posts').textContent);

    await user.click(screen.getByRole('button', { name: 'compose-home' }));

    await waitFor(() => {
      expect(screen.getByTestId('treehole-updates')).toHaveTextContent(
        String(treeholeUpdatesBefore + 1),
      );
      expect(screen.getByTestId('alumni-posts')).toHaveTextContent(String(alumniPostsBefore));
    });
  });
});
