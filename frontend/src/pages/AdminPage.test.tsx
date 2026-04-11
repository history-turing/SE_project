import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { AdminPage } from './AdminPage';

vi.mock('../context/AuthContext', () => ({
  useAuthContext: () => ({
    user: {
      id: 7,
      username: 'xiewei',
      email: 'xiewei@whu.edu.cn',
      name: 'xiewei',
      avatar: 'avatar',
      accountStatus: 'ACTIVE',
      roles: [{ code: 'SUPER_ADMIN', name: 'Super Admin' }],
      permissions: [
        { code: 'report.read.any', name: 'Read Reports' },
        { code: 'user.ban', name: 'Ban User' },
        { code: 'role.assign.admin', name: 'Assign Admin' },
        { code: 'audit.read.all', name: 'Read All Audit' },
      ],
    },
    loading: false,
    isAuthenticated: true,
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    hasPermission: (code: string) =>
      ['report.read.any', 'user.ban', 'role.assign.admin', 'audit.read.all'].includes(code),
    hasRole: (code: string) => code === 'SUPER_ADMIN',
  }),
}));

vi.mock('../services/api', () => ({
  getAdminReports: vi.fn().mockResolvedValue([]),
  getAdminUsers: vi.fn().mockResolvedValue([]),
  getAdminRoles: vi.fn().mockResolvedValue([]),
  getAuditLogs: vi.fn().mockResolvedValue([]),
  assignUserRole: vi.fn(),
  banUser: vi.fn(),
  unbanUser: vi.fn(),
  restorePost: vi.fn(),
  restoreComment: vi.fn(),
}));

test('renders moderation tabs for super admin', async () => {
  renderWithProviders(<AdminPage />, { route: '/admin' });

  expect(await screen.findByText('角色管理')).toBeInTheDocument();
  expect(screen.getByText('举报处理')).toBeInTheDocument();
  expect(screen.getByText('用户管理')).toBeInTheDocument();
  expect(screen.getByText('审计日志')).toBeInTheDocument();
});
