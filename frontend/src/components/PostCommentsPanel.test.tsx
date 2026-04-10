import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { PostCommentsPanel } from './PostCommentsPanel';
import { createPostComment, getPostComments } from '../services/api';

vi.mock('../services/api', () => ({
  getPostComments: vi.fn().mockResolvedValue({ total: 1, comments: [] }),
  createPostComment: vi.fn().mockResolvedValue({
    id: 'comment-1',
    postId: 'home-1',
    parentCommentCode: null,
    author: '测试用户',
    handle: '信管 · 2022',
    content: '第一条评论',
    createdAt: '2026-04-11 10:00',
    mine: true,
    replyToUserName: null,
    replies: [],
  }),
  createCommentReply: vi.fn(),
}));

test('submits a new root comment and updates visible count', async () => {
  const user = userEvent.setup();
  const onCountChange = vi.fn();

  renderWithProviders(<PostCommentsPanel postId="home-1" initialCount={0} onCountChange={onCountChange} />);

  expect(await screen.findByPlaceholderText('写下你的评论...')).toBeInTheDocument();
  await user.type(screen.getByPlaceholderText('写下你的评论...'), '第一条评论');
  await user.click(screen.getByRole('button', { name: '发送评论' }));

  expect(await screen.findByText('第一条评论')).toBeInTheDocument();
  expect(onCountChange).toHaveBeenCalledWith(1);
  expect(getPostComments).toHaveBeenCalledWith('home-1');
  expect(createPostComment).toHaveBeenCalledWith('home-1', '第一条评论');
});
