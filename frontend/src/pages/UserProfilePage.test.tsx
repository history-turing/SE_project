import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { UserProfilePage } from './UserProfilePage';

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
  }),
}));

vi.mock('../services/api', () => ({
  getUserProfile: vi.fn().mockResolvedValue({
    userCode: 'user-9',
    name: '测试用户',
    tagline: '武汉大学',
    college: '信管',
    year: '2022',
    bio: '测试简介',
    avatar: 'https://example.com/avatar.png',
    badges: ['热心校友'],
    stats: [],
  }),
}));

test('renders user profile page with dm entry', async () => {
  renderWithProviders(<UserProfilePage />, { route: '/users/user-9', path: '/users/:userCode' });

  expect(await screen.findByRole('button', { name: '发私信' })).toBeInTheDocument();
});
