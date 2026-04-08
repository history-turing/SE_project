import type {
  AlumniContact,
  Audience,
  AuthUser,
  ComposePayload,
  Conversation,
  FeedPost,
  NoticeItem,
  RankingItem,
  StoryCard,
  TopicGroup,
  UserProfile,
} from '../types';

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
  stats: {
    treeholeUpdates: string;
    hotTopics: string;
    alumniPosts: string;
  };
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

export function getAlumniPage() {
  return request<AlumniPageData>('/pages/alumni');
}

export function getProfilePage() {
  return request<ProfilePageData>('/pages/profile');
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
