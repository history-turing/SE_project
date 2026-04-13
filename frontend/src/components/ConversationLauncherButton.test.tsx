import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { ConversationLauncherButton } from './ConversationLauncherButton';

const apiMocks = vi.hoisted(() => ({
  createDirectConversation: vi.fn().mockResolvedValue({ conversationCode: 'dm-1001' }),
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
      roles: [],
      permissions: [],
    },
  }),
}));

vi.mock('../services/api', () => ({
  createDirectConversation: apiMocks.createDirectConversation,
}));

test('launches an anonymous-safe conversation from anonymous author entry', async () => {
  const user = userEvent.setup();

  renderWithProviders(
    <ConversationLauncherButton peerUserCode="user-9" sourcePostCode="post-1001" anonymousEntry />,
  );

  await user.click(screen.getByRole('button', { name: '发私信' }));

  expect(apiMocks.createDirectConversation).toHaveBeenCalledWith({
    peerUserCode: 'user-9',
    sourcePostCode: 'post-1001',
    anonymousEntry: true,
  });
});
