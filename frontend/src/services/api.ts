import type {
  AdminUser,
  AlumniContact,
  AnnouncementDetail,
  AnnouncementPopup,
  AnnouncementSavePayload,
  AnnouncementSummary,
  Audience,
  AuditLog,
  AuthUser,
  ComposePayload,
  Conversation,
  DmConversationDetail,
  DmConversationSummary,
  FeedPost,
  HomeStats,
  NoticeItem,
  PostComment,
  PostCommentsData,
  RankingItem,
  ReportSummary,
  Role,
  SearchResult,
  StoryCard,
  TrendingTopicAdmin,
  TrendingTopicRulePayload,
  TopicGroup,
  UserProfile,
} from '../types';

interface ApiEnvelope<T> {
  code: number;
  message: string;
  data: T;
}

interface DmConversationListItemResponse {
  conversationCode: string;
  peerName: string;
  peerSubtitle: string;
  peerAvatarUrl: string;
  lastMessage: string | null;
  displayTime: string | null;
  unreadCount: number | null;
}

interface DmConversationPeerResponse {
  userCode: string;
  name: string;
  subtitle: string;
  avatarUrl: string;
}

interface DmConversationDetailResponse {
  conversationCode: string;
  status: string;
  peer: DmConversationPeerResponse | null;
  lastMessage: string | null;
  lastMessageTime: string | null;
  unreadCount: number | null;
  messages: {
    id: string;
    sender: 'me' | 'them';
    text: string;
    time: string;
    messageType?: string;
    status?: string;
    recalled?: boolean;
    recalledAt?: string | null;
    canRecall?: boolean;
  }[];
}

export class ApiError extends Error {
  status: number;
  code: number;

  constructor(message: string, status: number, code: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

export interface HomePageData {
  stats: HomeStats;
  topicHighlights: TopicGroup[];
  rankings: RankingItem[];
  notices: NoticeItem[];
  posts: FeedPost[];
}

export interface TopicsPageData {
  topics: TopicGroup[];
  rankings: RankingItem[];
}

export interface AlumniPageData {
  stories: StoryCard[];
  contacts: AlumniContact[];
  posts: FeedPost[];
}

export interface ProfilePageData {
  profile: UserProfile;
  myPosts: FeedPost[];
  savedPosts: FeedPost[];
  conversations: Conversation[];
  activeConversationId: string;
}

export interface ToggleResult {
  targetId: string;
  active: boolean;
  count: number | null;
}

export interface AuthPayload {
  token: string;
  user: AuthUser;
}

export interface ReportCreatePayload {
  targetType: 'POST' | 'COMMENT' | 'USER';
  targetCode: string;
  reasonCode: string;
  reasonDetail?: string;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';
export const AUTH_TOKEN_STORAGE_KEY = 'whu-treehole-token';

function readStoredToken() {
  if (typeof window === 'undefined') {
    return '';
  }
  return window.localStorage.getItem(AUTH_TOKEN_STORAGE_KEY) ?? '';
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = readStoredToken();
  const headers = new Headers(init?.headers ?? {});

  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
  });

  const payload = (await response.json()) as ApiEnvelope<T>;

  if (!response.ok || payload.code !== 0) {
    throw new ApiError(payload.message || '请求失败', response.status, payload.code ?? -1);
  }

  return payload.data;
}

export function getHomePage() {
  return request<HomePageData>('/pages/home');
}

export function getTopicsPage() {
  return request<TopicsPageData>('/pages/topics?scope=ALL');
}

export function getTrendingTopics() {
  return request<RankingItem[]>('/topics/trending');
}

export function getAlumniPage() {
  return request<AlumniPageData>('/pages/alumni');
}

export function getProfilePage() {
  return request<ProfilePageData>('/pages/profile');
}

export function getUserProfile(userCode: string) {
  return request<UserProfile>(`/users/${userCode}/profile`);
}

export function getAnnouncements() {
  return request<AnnouncementSummary[]>('/announcements');
}

export function getAnnouncementDetail(announcementCode: string) {
  return request<AnnouncementDetail>(`/announcements/${announcementCode}`);
}

export function getAnnouncementPopup() {
  return request<AnnouncementPopup | null>('/announcements/popup');
}

export function searchAll(keyword: string) {
  return request<SearchResult>(`/search?q=${encodeURIComponent(keyword)}`);
}

export function createPost(payload: ComposePayload) {
  return request<FeedPost>('/posts', {
    method: 'POST',
    body: JSON.stringify({
      ...payload,
      audience: normalizeAudience(payload.audience),
    }),
  });
}

export function deletePost(postId: string) {
  return request<void>(`/posts/${postId}`, {
    method: 'DELETE',
  });
}

export function toggleLike(postId: string) {
  return request<ToggleResult>(`/posts/${postId}/likes/toggle`, {
    method: 'POST',
  });
}

export function toggleSave(postId: string) {
  return request<ToggleResult>(`/posts/${postId}/saves/toggle`, {
    method: 'POST',
  });
}

export function getPostComments(postId: string) {
  return request<PostCommentsData>(`/posts/${postId}/comments`);
}

export function createPostComment(postId: string, content: string) {
  return request<PostComment>(`/posts/${postId}/comments`, {
    method: 'POST',
    body: JSON.stringify({ content }),
  });
}

export function createCommentReply(postId: string, commentId: string, content: string) {
  return request<PostComment>(`/posts/${postId}/comments/${commentId}/replies`, {
    method: 'POST',
    body: JSON.stringify({ content }),
  });
}

export function deleteComment(postId: string, commentId: string) {
  return request<void>(`/posts/${postId}/comments/${commentId}`, {
    method: 'DELETE',
  });
}

export function createReport(payload: ReportCreatePayload) {
  return request<ReportSummary>('/reports', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getAdminReports() {
  return request<ReportSummary[]>('/admin/reports');
}

export function resolveReport(reportCode: string, payload: { resolutionCode: string; resolutionNote?: string }) {
  return request<void>(`/admin/reports/${reportCode}/resolve`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getAdminUsers() {
  return request<AdminUser[]>('/admin/users');
}

export function getAdminRoles() {
  return request<Role[]>('/admin/roles');
}

export function getAuditLogs() {
  return request<AuditLog[]>('/admin/audit-logs');
}

export function getAdminTrendingTopics() {
  return request<TrendingTopicAdmin[]>('/admin/trending-topics');
}

export function saveTrendingTopicRule(payload: TrendingTopicRulePayload) {
  return request<void>('/admin/trending-topics/rules', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getAdminAnnouncements() {
  return request<AnnouncementSummary[]>('/admin/announcements');
}

export function getDmConversations() {
  return request<DmConversationListItemResponse[]>('/dm/conversations').then((items) =>
    items.map((item) => ({
      conversationCode: item.conversationCode,
      peer: {
        userCode: '',
        name: item.peerName,
        subtitle: item.peerSubtitle,
        avatar: item.peerAvatarUrl,
      },
      lastMessage: item.lastMessage,
      displayTime: item.displayTime,
      unreadCount: item.unreadCount ?? 0,
    })),
  );
}

export function getDmConversationDetail(conversationCode: string) {
  return request<DmConversationDetailResponse>(`/dm/conversations/${conversationCode}`).then((detail) => ({
    conversationCode: detail.conversationCode,
    status: detail.status,
    peer: {
      userCode: detail.peer?.userCode ?? '',
      name: detail.peer?.name ?? '私信会话',
      subtitle: detail.peer?.subtitle ?? '',
      avatar: detail.peer?.avatarUrl ?? '',
    },
    lastMessage: detail.lastMessage,
    displayTime: detail.lastMessageTime,
    unreadCount: detail.unreadCount ?? 0,
    messages: detail.messages,
  }));
}

export function createDirectConversation(peerUserCode: string) {
  return request<string>('/dm/conversations/direct', {
    method: 'POST',
    body: JSON.stringify({ peerUserCode }),
  }).then((conversationCode) => ({ conversationCode }));
}

export function sendDmMessage(conversationCode: string, content: string) {
  return request<{
    id: string;
    sender: 'me' | 'them';
    text: string;
    time: string;
    messageType?: string;
    status?: string;
    recalled?: boolean;
    recalledAt?: string | null;
    canRecall?: boolean;
  }>(
    `/dm/conversations/${conversationCode}/messages`,
    {
      method: 'POST',
      body: JSON.stringify({
        clientMessageId: `web-${Date.now()}`,
        content,
      }),
    },
  );
}

export function recallDmMessage(conversationCode: string, messageId: string) {
  return request<{
    id: string;
    sender: 'me' | 'them';
    text: string;
    time: string;
    messageType?: string;
    status?: string;
    recalled?: boolean;
    recalledAt?: string | null;
    canRecall?: boolean;
  }>(`/dm/conversations/${conversationCode}/messages/${messageId}/recall`, {
    method: 'POST',
  });
}

export function markDmConversationRead(conversationCode: string) {
  return request<null>(`/dm/conversations/${conversationCode}/read`, {
    method: 'POST',
  });
}

export function createAnnouncement(payload: AnnouncementSavePayload) {
  return request<AnnouncementSummary>('/admin/announcements', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updateAnnouncement(announcementCode: string, payload: AnnouncementSavePayload) {
  return request<AnnouncementSummary>(`/admin/announcements/${announcementCode}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function publishAnnouncement(announcementCode: string) {
  return request<void>(`/admin/announcements/${announcementCode}/publish`, {
    method: 'POST',
  });
}

export function offlineAnnouncement(announcementCode: string) {
  return request<void>(`/admin/announcements/${announcementCode}/offline`, {
    method: 'POST',
  });
}

export function assignUserRole(userCode: string, roleCode: string) {
  return request<void>(`/admin/users/${userCode}/roles`, {
    method: 'POST',
    body: JSON.stringify({ roleCode }),
  });
}

export function banUser(userCode: string, reason: string) {
  return request<void>(`/admin/users/${userCode}/ban`, {
    method: 'POST',
    body: JSON.stringify({ reason }),
  });
}

export function unbanUser(userCode: string) {
  return request<void>(`/admin/users/${userCode}/unban`, {
    method: 'POST',
  });
}

export function restorePost(postCode: string) {
  return request<void>(`/admin/posts/${postCode}/restore`, {
    method: 'POST',
  });
}

export function restoreComment(postCode: string, commentCode: string) {
  return request<void>(`/admin/posts/${postCode}/comments/${commentCode}/restore`, {
    method: 'POST',
  });
}

export function toggleFollow(contactId: string) {
  return request<ToggleResult>(`/alumni/contacts/${contactId}/follow/toggle`, {
    method: 'POST',
  });
}

export function sendMessage(conversationId: string, text: string) {
  return request<{ id: string; sender: 'me' | 'them'; text: string; time: string }>(
    `/conversations/${conversationId}/messages`,
    {
      method: 'POST',
      body: JSON.stringify({ text }),
    },
  );
}

export function markConversationRead(conversationId: string) {
  return request<null>(`/conversations/${conversationId}/read`, {
    method: 'POST',
  });
}

export function sendEmailCode(email: string) {
  return request<string>('/auth/email-code', {
    method: 'POST',
    body: JSON.stringify({ email }),
  });
}

export function registerWithEmail(payload: {
  email: string;
  code: string;
  username: string;
  password: string;
}) {
  return request<AuthPayload>('/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function loginWithPassword(payload: { username: string; password: string }) {
  return request<AuthPayload>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getCurrentUser() {
  return request<AuthUser>('/auth/me');
}

export function logoutRequest() {
  return request<null>('/auth/logout', {
    method: 'POST',
  });
}

export function normalizeAudience(audience: Audience): 'HOME' | 'ALUMNI' {
  return audience === '校友圈' ? 'ALUMNI' : 'HOME';
}
