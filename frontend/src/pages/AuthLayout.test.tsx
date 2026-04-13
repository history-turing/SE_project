import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { LoginPage } from './LoginPage';
import { RegisterPage } from './RegisterPage';

vi.mock('../context/AuthContext', () => ({
  useAuthContext: () => ({
    login: vi.fn().mockResolvedValue(undefined),
    register: vi.fn().mockResolvedValue(undefined),
  }),
}));

vi.mock('../services/api', async () => {
  const actual = await vi.importActual<typeof import('../services/api')>('../services/api');
  return {
    ...actual,
    sendEmailCode: vi.fn().mockResolvedValue(undefined),
  };
});

test('renders the login form card before the marketing panel in DOM order for narrow-screen workflows', () => {
  const { container } = renderWithProviders(<LoginPage />, { route: '/login' });

  const layout = container.querySelector('.auth-layout');
  expect(layout?.firstElementChild).toHaveClass('auth-card');
  expect(screen.getByRole('heading', { name: '欢迎回来' })).toBeInTheDocument();
});

test('renders the register form card before the guidance panel in DOM order for narrow-screen workflows', () => {
  const { container } = renderWithProviders(<RegisterPage />, { route: '/register' });

  const layout = container.querySelector('.auth-layout');
  expect(layout?.firstElementChild).toHaveClass('auth-card');
  expect(screen.getByRole('heading', { name: '创建你的树洞账号' })).toBeInTheDocument();
});
