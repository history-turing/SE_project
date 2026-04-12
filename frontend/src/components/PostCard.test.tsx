import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { PostCard } from './PostCard';

vi.mock('../context/AppContext', () => ({
  useAppContext: () => ({
    likedIds: [],
    savedIds: [],
    removePost: vi.fn(),
    toggleLike: vi.fn(),
    toggleSave: vi.fn(),
    setPostCommentCount: vi.fn(),
  }),
}));

vi.mock('./PostCommentsPanel', () => ({
  PostCommentsPanel: () => null,
}));

vi.mock('./ReportDialog', () => ({
  ReportDialog: () => null,
}));

test('renders author entry to the public user profile when author user code exists', () => {
  renderWithProviders(
    <PostCard
      post={{
        id: 'post-1',
        title: '帖子标题',
        content: '帖子内容',
        author: '测试用户',
        authorUserCode: 'user-9',
        handle: '信管 · 2022',
        topic: '校园日常',
        audience: '首页',
        createdAt: '2026-04-12 10:00',
        likes: 1,
        comments: 0,
        saves: 0,
        accent: 'rose',
      }}
    />,
  );

  expect(screen.getByRole('link', { name: /测试用户/ })).toHaveAttribute('href', '/users/user-9');
});
