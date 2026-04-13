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
        title: 'post-title',
        content: 'post-content',
        author: 'tester',
        authorUserCode: 'user-9',
        handle: 'handle',
        topic: 'topic',
        audience: '首页',
        createdAt: '2026-04-12 10:00',
        likes: 1,
        comments: 0,
        saves: 0,
        accent: 'rose',
      }}
    />,
  );

  expect(screen.getByRole('link', { name: /tester/i })).toHaveAttribute('href', '/users/user-9');
});

test('does not render a public profile link for anonymous posts', () => {
  renderWithProviders(
    <PostCard
      post={{
        id: 'post-2',
        title: 'anonymous-post',
        content: 'anonymous-content',
        author: 'anonymous-author',
        authorUserCode: 'user-9',
        handle: 'anonymous-handle',
        topic: 'topic',
        audience: '首页',
        createdAt: '2026-04-12 10:00',
        likes: 1,
        comments: 0,
        saves: 0,
        accent: 'rose',
        anonymous: true,
      }}
    />,
  );

  expect(screen.queryByRole('link', { name: /anonymous-author/i })).not.toBeInTheDocument();
});
