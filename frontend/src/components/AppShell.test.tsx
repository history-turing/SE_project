import { screen } from '@testing-library/react';
import { AppShell } from './AppShell';
import { renderWithProviders } from '../test/renderWithProviders';

vi.mock('../context/AppContext', () => ({
  useAppContext: () => ({
    composePost: vi.fn(),
    profile: {
      name: 'xiewei',
      avatar: 'https://example.com/avatar.png',
    },
  }),
}));

vi.mock('../context/AuthContext', () => ({
  useAuthContext: () => ({
    user: {
      id: 7,
      username: 'xiewei',
      email: 'xiewei@whu.edu.cn',
      name: 'xiewei',
      avatar: 'https://example.com/avatar.png',
      accountStatus: 'ACTIVE',
      roles: [{ code: 'SUPER_ADMIN', name: 'Super Admin' }],
      permissions: [{ code: 'report.read.any', name: 'Read Reports' }],
    },
    loading: false,
    isAuthenticated: true,
    logout: vi.fn(),
    hasPermission: (code: string) => code === 'report.read.any',
  }),
}));

test('renders a single admin entry in the shared topbar for admin users', () => {
  renderWithProviders(<AppShell />, { route: '/' });

  expect(screen.getAllByRole('link', { name: '管理台' })).toHaveLength(1);
});
