import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { ConversationLauncherButton } from './ConversationLauncherButton';

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
  createDirectConversation: vi.fn().mockResolvedValue({ conversationCode: 'dm-1001' }),
}));

test('launches a direct conversation from author entry', async () => {
  const user = userEvent.setup();

  renderWithProviders(<ConversationLauncherButton peerUserCode="user-9" />);

  await user.click(screen.getByRole('button', { name: '发私信' }));

  expect(await screen.findByText('已进入私信会话')).toBeInTheDocument();
});
