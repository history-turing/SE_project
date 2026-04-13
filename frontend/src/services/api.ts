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
  DmConversationDetail,
  DmConversationSummary,
  DmRealtimeEvent,
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
import {
  mapConversationDetail,
  mapConversationSummary,
  mapRealtimeEvent,
  type DmConversationDetailResponse,
  type DmConversationListItemResponse,
  type MessageRealtimeEventResponse,
} from '../mappers/messageViewModels';

interface ApiEnvelope<T> {
  code: number;
  message: string;
  data: T;
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
  conversations: DmConversationSummary[];
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
const API_UNKNOWN_ERROR_CODE = -1;

function readStoredToken() {
  if (typeof window === 'undefined') {
    return '';
  }
  return window.localStorage.getItem(AUTH_TOKEN_STORAGE_KEY) ?? '';
}

function getDefaultErrorMessage(status: number) {
  if (status === 0) {
    return '网络异常，请检查连接后重试';
  }
  if (status === 502 || status === 503 || status === 504) {
    return '服务暂时不可用，请稍后再试';
  }
  if (status >= 500) {
    return '服务开小差了，请稍后再试';
  }
  return '请求失败，请稍后再试';
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

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers,
    });
  } catch {
    throw new ApiError(getDefaultErrorMessage(0), 0, API_UNKNOWN_ERROR_CODE);
  }

  const rawBody = await response.text();
  let payload: ApiEnvelope<T> | null = null;

  if (rawBody.trim()) {
    try {
      payload = JSON.parse(rawBody) as ApiEnvelope<T>;
    } catch {
      throw new ApiError(
        response.ok ? '服务响应异常，请稍后再试' : getDefaultErrorMessage(response.status),
        response.status,
        API_UNKNOWN_ERROR_CODE,
      );
    }
  }

  if (!payload || typeof payload.code !== 'number') {
    throw new ApiError(
      response.ok ? '服务响应异常，请稍后再试' : getDefaultErrorMessage(response.status),
      response.status,
      API_UNKNOWN_ERROR_CODE,
    );
  }

  if (!response.ok || payload.code !== 0) {
    throw new ApiError(
      payload.message || getDefaultErrorMessage(response.status),
      response.status,
      payload.code ?? API_UNKNOWN_ERROR_CODE,
    );
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
    items.map(mapConversationSummary),
  );
}

export function getDmConversationDetail(conversationCode: string) {
  return request<DmConversationDetailResponse>(`/dm/conversations/${conversationCode}`).then(
    mapConversationDetail,
  );
}

export function createDirectConversation(payload: {
  peerUserCode: string;
  sourcePostCode?: string;
  anonymousEntry?: boolean;
}) {
  return request<string>('/dm/conversations/direct', {
    method: 'POST',
    body: JSON.stringify(payload),
  }).then((conversationCode) => ({ conversationCode }));
}

export function parseDmRealtimeEvent(raw: string): DmRealtimeEvent {
  return mapRealtimeEvent(JSON.parse(raw) as MessageRealtimeEventResponse);
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
