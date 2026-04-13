import type {
  DmConversationDetail,
  DmConversationSummary,
  DmRealtimeEvent,
  Message,
  NotificationSummary,
} from '../types';

export interface DmConversationListItemResponse {
  conversationCode: string;
  conversationType?: string | null;
  peerName: string | null;
  peerSubtitle: string | null;
  peerAvatarUrl: string | null;
  lastMessage: string | null;
  displayTime: string | null;
  unreadCount: number | null;
}

export interface DmConversationPeerResponse {
  userCode: string | null;
  name: string | null;
  subtitle: string | null;
  avatarUrl: string | null;
}

export interface DmConversationDetailResponse {
  conversationCode: string;
  conversationType?: string | null;
  status: string;
  peer: DmConversationPeerResponse | null;
  lastMessage: string | null;
  lastMessageTime: string | null;
  unreadCount: number | null;
  messages: Message[];
}

export interface UnreadNotificationResponse {
  userId?: number | null;
  messagesUnread?: number | null;
  interactionsUnread?: number | null;
  systemUnread?: number | null;
  totalUnread?: number | null;
}

export interface MessageRealtimeRecipientStateResponse {
  userId: number;
  conversation: DmConversationListItemResponse | null;
  message: Message | null;
  unreadNotification: UnreadNotificationResponse | null;
}

export interface MessageRealtimeEventResponse {
  type: string;
  conversationCode: string;
  recipientStates: MessageRealtimeRecipientStateResponse[];
}

export function createEmptyNotificationSummary(): NotificationSummary {
  return {
    messagesUnread: 0,
    interactionsUnread: 0,
    systemUnread: 0,
    totalUnread: 0,
    hasUnread: false,
  };
}

export function mapNotificationSummary(
  notification?: UnreadNotificationResponse | null,
): NotificationSummary {
  const messagesUnread = notification?.messagesUnread ?? 0;
  const interactionsUnread = notification?.interactionsUnread ?? 0;
  const systemUnread = notification?.systemUnread ?? 0;
  const totalUnread =
    notification?.totalUnread ?? messagesUnread + interactionsUnread + systemUnread;

  return {
    messagesUnread,
    interactionsUnread,
    systemUnread,
    totalUnread,
    hasUnread: totalUnread > 0,
  };
}

export function mapConversationSummary(
  dto: DmConversationListItemResponse,
): DmConversationSummary {
  return {
    conversationCode: dto.conversationCode,
    conversationType: dto.conversationType ?? 'DIRECT',
    peer: {
      userCode: '',
      name: dto.peerName ?? '私信会话',
      subtitle: dto.peerSubtitle ?? '',
      avatar: dto.peerAvatarUrl ?? '',
    },
    lastMessage: dto.lastMessage,
    displayTime: dto.displayTime,
    unreadCount: dto.unreadCount ?? 0,
  };
}

export function mapConversationDetail(
  dto: DmConversationDetailResponse,
): DmConversationDetail {
  return {
    conversationCode: dto.conversationCode,
    conversationType: dto.conversationType ?? 'DIRECT',
    status: dto.status,
    peer: {
      userCode: dto.peer?.userCode ?? '',
      name: dto.peer?.name ?? '私信会话',
      subtitle: dto.peer?.subtitle ?? '',
      avatar: dto.peer?.avatarUrl ?? '',
    },
    lastMessage: dto.lastMessage,
    displayTime: dto.lastMessageTime,
    unreadCount: dto.unreadCount ?? 0,
    messages: dto.messages ?? [],
  };
}

export function mapRealtimeEvent(
  event: MessageRealtimeEventResponse,
): DmRealtimeEvent {
  return {
    type: event.type,
    conversationCode: event.conversationCode,
    recipientStates: (event.recipientStates ?? []).map((state) => ({
      userId: state.userId,
      conversation: state.conversation
        ? mapConversationSummary(state.conversation)
        : {
            conversationCode: event.conversationCode,
            conversationType: 'DIRECT',
            peer: {
              userCode: '',
              name: '私信会话',
              subtitle: '',
              avatar: '',
            },
            lastMessage: state.message?.text ?? null,
            displayTime: state.message?.time ?? null,
            unreadCount: state.unreadNotification?.messagesUnread ?? 0,
          },
      message: state.message ?? null,
      unreadNotification: mapNotificationSummary(state.unreadNotification),
    })),
  };
}
