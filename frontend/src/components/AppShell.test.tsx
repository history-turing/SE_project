import { screen } from '@testing-library/react';
import { AppShell } from './AppShell';
import { renderWithProviders } from '../test/renderWithProviders';

const appContextState = {
  notificationSummary: {
    messagesUnread: 3,
    interactionsUnread: 0,
    systemUnread: 0,
    totalUnread: 3,
    hasUnread: true,
  },
};

vi.mock('../context/AppContext', () => ({
  useAppContext: () => ({
    composePost: vi.fn(),
    profile: {
      name: 'xiewei',
      avatar: 'https://example.com/avatar.png',
    },
    notificationSummary: appContextState.notificationSummary,
  }),
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
      roles: [{ code: 'SUPER_ADMIN', name: 'Super Admin' }],
      permissions: [{ code: 'report.read.any', name: 'Read Reports' }],
    },
    loading: false,
    isAuthenticated: true,
    logout: vi.fn(),
    hasPermission: (code: string) => code === 'report.read.any',
  }),
}));

vi.mock('../services/api', () => ({
  getAnnouncementPopup: vi.fn().mockResolvedValue(null),
}));

beforeEach(() => {
  appContextState.notificationSummary = {
    messagesUnread: 3,
    interactionsUnread: 0,
    systemUnread: 0,
    totalUnread: 3,
    hasUnread: true,
  };
});

test('renders a single admin entry and unread badge in the shared topbar for admin users', () => {
  renderWithProviders(<AppShell />, { route: '/' });

  expect(screen.getAllByRole('link', { name: /管理台/i })).toHaveLength(1);
  expect(screen.getByText('3')).toBeInTheDocument();
});

test('does not render a badge when total unread is zero even if a stale flag remains true', () => {
  appContextState.notificationSummary = {
    messagesUnread: 0,
    interactionsUnread: 0,
    systemUnread: 0,
    totalUnread: 0,
    hasUnread: true,
  };

  renderWithProviders(<AppShell />, { route: '/' });

  expect(screen.queryByText('0')).not.toBeInTheDocument();
});
