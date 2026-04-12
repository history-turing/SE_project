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
}));

function Probe() {
  const { topicRankings, profile } = useAppContext();

  return (
    <div>
      <span data-testid="first-ranking">{topicRankings[0]?.label ?? 'none'}</span>
      <span data-testid="profile-name">{profile.name}</span>
    </div>
  );
}

function createFeedPost(id: string): FeedPost {
  return {
    id,
    title: `${id}-title`,
    content: `${id}-content`,
    author: 'xiewei',
    authorUserCode: 'user-7',
    handle: '信管院 · 2022',
    topic: '校园日常',
    audience: '首页',
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
      rankings: [{ id: 'rank-1', label: '#真实热议', heat: '100 热度' }],
    });
    apiMocks.getAlumniPage.mockResolvedValue({
      stories: [],
      contacts: [],
      posts: [createFeedPost('alumni-1')],
    });
    apiMocks.getProfilePage.mockResolvedValue({
      profile: {
        userCode: 'user-7',
        name: '真实用户',
        tagline: '真实签名',
        college: '信管院',
        year: '2022',
        bio: '真实简介',
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
      status: 'ACTIVE',
      peer: {
        userCode: 'user-9',
        name: '测试用户',
        subtitle: '武汉大学',
        avatar: '',
      },
      lastMessage: 'hello',
      displayTime: '21:45',
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

  test('keeps successful sections when home page request fails', async () => {
    render(
      <AppProvider>
        <Probe />
      </AppProvider>,
    );

    await waitFor(() => {
      expect(screen.getByTestId('first-ranking')).toHaveTextContent('#真实热议');
      expect(screen.getByTestId('profile-name')).toHaveTextContent('真实用户');
    });
  });
});
