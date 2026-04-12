import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { ProfilePage } from './ProfilePage';

const recallMessage = vi.fn().mockResolvedValue(undefined);

vi.mock('../context/AppContext', () => ({
  useAppContext: () => ({
    activeConversationCode: 'dm-1001',
    activeConversation: {
      conversationCode: 'dm-1001',
      status: 'ACTIVE',
      peer: {
        userCode: 'user-9',
        name: '测试用户',
        subtitle: '武汉大学',
        avatar: '',
      },
      lastMessage: 'hello',
      displayTime: '21:45',
      unreadCount: 0,
      messages: [
        {
          id: 'msg-1',
          sender: 'me',
          text: 'hello',
          time: '21:45',
          messageType: 'TEXT',
          status: 'SENT',
          recalled: false,
          recalledAt: null,
          canRecall: true,
        },
      ],
    },
    alumniPosts: [],
    communityPosts: [],
    conversations: [
      {
        conversationCode: 'dm-1001',
        peer: {
          userCode: 'user-9',
          name: '测试用户',
          subtitle: '武汉大学',
          avatar: '',
        },
        lastMessage: 'hello',
        displayTime: '21:45',
        unreadCount: 0,
      },
    ],
    messagesLoading: false,
    myPosts: [],
    profile: {
      name: 'xiewei',
      tagline: 'hello',
      college: 'WHU',
      year: '2022',
      bio: 'bio',
      avatar: '',
      badges: [],
      stats: [],
    },
    savedIds: [],
    selectConversation: vi.fn().mockResolvedValue(undefined),
    sendMessage: vi.fn().mockResolvedValue(undefined),
    recallMessage,
  }),
}));

test('recalls message from profile messaging center', async () => {
  const user = userEvent.setup();

  renderWithProviders(<ProfilePage />, {
    route: '/profile?tab=messages&conversation=dm-1001',
    path: '/profile',
  });

  await user.click(screen.getByRole('button', { name: '撤回消息' }));

  expect(recallMessage).toHaveBeenCalledWith('msg-1');
});
