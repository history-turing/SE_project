import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { ApiError, createPostComment, getPostComments } from '../services/api';
import { clearLocalPostComments } from '../services/localCommentStore';
import { PostCommentsPanel } from './PostCommentsPanel';

vi.mock('../services/api', () => ({
  ApiError: class extends Error {
    status: number;
    code: number;

    constructor(message: string, status: number, code: number) {
      super(message);
      this.status = status;
      this.code = code;
    }
  },
  getPostComments: vi.fn(),
  createPostComment: vi.fn(),
  createCommentReply: vi.fn(),
}));

function createRootComment(id: string, content: string) {
  return {
    id,
    postId: 'home-1',
    parentCommentCode: null,
    author: '测试用户',
    handle: '信管 · 2022',
    content,
    createdAt: '2026-04-11 10:00',
    mine: true,
    replyToUserName: null,
    replies: [],
  };
}

beforeEach(() => {
  clearLocalPostComments();
  vi.clearAllMocks();
  vi.mocked(getPostComments).mockResolvedValue({ total: 0, comments: [] });
  vi.mocked(createPostComment).mockResolvedValue(createRootComment('comment-1', '第一条评论'));
});

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

test('falls back to local persistence and still renders the comment immediately', async () => {
  const user = userEvent.setup();

  vi.mocked(createPostComment).mockRejectedValueOnce(new ApiError('评论发送失败', 500, 5000));

  renderWithProviders(<PostCommentsPanel postId="home-1" initialCount={0} onCountChange={vi.fn()} />);

  expect(await screen.findByPlaceholderText('写下你的评论...')).toBeInTheDocument();
  await user.type(screen.getByPlaceholderText('写下你的评论...'), '发送失败的评论');
  await user.click(screen.getByRole('button', { name: '发送评论' }));

  expect(await screen.findByText('发送失败的评论')).toBeInTheDocument();
  expect(await screen.findByText('评论已保存到当前浏览器，本地环境恢复后会继续使用。')).toBeInTheDocument();
});

test('collapses root comments after three items and expands on demand', async () => {
  const user = userEvent.setup();

  vi.mocked(getPostComments).mockResolvedValueOnce({
    total: 4,
    comments: [
      createRootComment('comment-1', '评论 1'),
      createRootComment('comment-2', '评论 2'),
      createRootComment('comment-3', '评论 3'),
      createRootComment('comment-4', '评论 4'),
    ],
  });

  renderWithProviders(<PostCommentsPanel postId="home-1" initialCount={4} onCountChange={vi.fn()} />);

  expect(await screen.findByText('评论 1')).toBeInTheDocument();
  expect(screen.getByText('评论 2')).toBeInTheDocument();
  expect(screen.getByText('评论 3')).toBeInTheDocument();
  expect(screen.queryByText('评论 4')).not.toBeInTheDocument();
  expect(screen.getByRole('button', { name: '展开全部 4 条评论' })).toBeInTheDocument();

  await user.click(screen.getByRole('button', { name: '展开全部 4 条评论' }));

  expect(await screen.findByText('评论 4')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: '收起评论' })).toBeInTheDocument();
});
