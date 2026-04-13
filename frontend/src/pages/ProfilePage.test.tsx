import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { ProfilePage } from './ProfilePage';

const recallMessage = vi.fn().mockResolvedValue(undefined);
const selectConversation = vi.fn().mockResolvedValue(undefined);
const appContextState = {
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
        sender: 'me' as const,
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
};

vi.mock('../context/AppContext', () => ({
  useAppContext: () => ({
    activeConversationCode: appContextState.activeConversationCode,
    activeConversation: appContextState.activeConversation,
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
    selectConversation,
    sendMessage: vi.fn().mockResolvedValue(undefined),
    recallMessage,
  }),
}));

beforeEach(() => {
  appContextState.activeConversationCode = 'dm-1001';
  appContextState.activeConversation = {
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
  };
  recallMessage.mockClear();
  selectConversation.mockClear();
});

test('recalls message from profile messaging center', async () => {
  const user = userEvent.setup();

  renderWithProviders(<ProfilePage />, {
    route: '/profile?tab=messages&conversation=dm-1001',
    path: '/profile',
  });

  await user.click(screen.getByRole('button', { name: '撤回消息' }));

  expect(recallMessage).toHaveBeenCalledWith('msg-1');
});

test('selects the first conversation when opening messages tab without a conversation query', async () => {
  appContextState.activeConversationCode = '';
  appContextState.activeConversation = null;

  renderWithProviders(<ProfilePage />, {
    route: '/profile?tab=messages',
    path: '/profile',
  });

  await waitFor(() => {
    expect(selectConversation).toHaveBeenCalledWith('dm-1001');
  });
});
