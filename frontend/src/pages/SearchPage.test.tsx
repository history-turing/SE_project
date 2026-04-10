import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { SearchPage } from './SearchPage';

vi.mock('../services/api', () => ({
  searchAll: vi.fn().mockResolvedValue({
    keyword: '樱花',
    total: 3,
    posts: [
      {
        id: 'home-1',
        topic: '校园日常',
        audience: '首页',
        author: '作者',
        handle: '句柄',
        content: '樱花很美',
        createdAt: '2026-04-11 09:30',
        likes: 1,
        comments: 2,
        saves: 3,
        accent: 'rose',
      },
    ],
    stories: [{ id: 'story-1', title: '樱花季', meta: '校友回忆' }],
    contacts: [{ id: 'wang', name: '王校友', meta: '2010级', focus: '材料', avatar: '/avatar.jpg', followed: false }],
  }),
}));

test('renders grouped search results', async () => {
  renderWithProviders(<SearchPage />, { route: '/search?q=%E6%A8%B1%E8%8A%B1' });

  expect(await screen.findByText('搜索结果')).toBeInTheDocument();
  expect(await screen.findByText('帖子')).toBeInTheDocument();
  expect(await screen.findByText('校友故事')).toBeInTheDocument();
  expect(await screen.findByText('联系人')).toBeInTheDocument();
  expect(await screen.findByText('樱花很美')).toBeInTheDocument();
});
