import { renderWithProviders } from '../test/renderWithProviders';
import { HomePage } from './HomePage';

vi.mock('../context/AppContext', () => ({
  useAppContext: () => ({
    communityPosts: [
      {
        id: 'post-1',
        title: 'post-title',
        content: 'post-content',
        author: 'tester',
        authorUserCode: 'user-9',
        handle: '@tester',
        topic: '全部',
        audience: '首页',
        createdAt: '2026-04-13 09:00',
        likes: 3,
        comments: 1,
        saves: 1,
        accent: 'rose',
      },
    ],
    topicGroups: [
      { id: '1', name: '全部', heat: '1', destination: '/', accent: 'rose', emoji: 'A' },
      { id: '2', name: '校园日常', heat: '2', destination: '/', accent: 'jade', emoji: 'B' },
      { id: '3', name: '学业交流', heat: '3', destination: '/', accent: 'gold', emoji: 'C' },
      { id: '4', name: '生活闲聊', heat: '4', destination: '/', accent: 'ink', emoji: 'D' },
    ],
    topicRankings: [{ id: 'rank-1', label: '#topic', heat: '99' }],
    campusNotices: [{ id: 'notice-1', title: 'notice', meta: 'today' }],
    homeStats: { treeholeUpdates: '5', hotTopics: '6', alumniPosts: '7' },
    refreshHomeStats: vi.fn().mockResolvedValue(undefined),
  }),
}));

vi.mock('../components/PostCard', () => ({
  PostCard: ({ post }: { post: { title?: string; content: string } }) => <article>{post.title ?? post.content}</article>,
}));

test('renders the feed column before sidebar content in the DOM so mobile readers hit real content first', () => {
  const { container } = renderWithProviders(<HomePage />, { route: '/' });

  const grid = container.querySelector('.page-grid--home');
  expect(grid?.firstElementChild).toHaveClass('content-column');
});
