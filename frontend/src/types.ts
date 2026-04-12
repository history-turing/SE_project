export type AccentTone = 'rose' | 'jade' | 'gold' | 'ink';

export type Audience = '首页' | '校友圈';

export interface NavItem {
  path: string;
  label: string;
  icon: 'home' | 'topics' | 'alumni' | 'profile';
}

export interface FeedPost {
  id: string;
  title?: string;
  content: string;
  author: string;
  authorUserCode?: string | null;
  handle: string;
  topic: string;
  audience: Audience;
  createdAt: string;
  likes: number;
  comments: number;
  saves: number;
  accent: AccentTone;
  badge?: string;
  image?: string;
  anonymous?: boolean;
  location?: string;
  mine?: boolean;
  canDelete?: boolean;
  liked?: boolean;
  saved?: boolean;
}

export interface PostComment {
  id: string;
  postId: string;
  parentCommentCode?: string | null;
  author: string;
  authorUserCode?: string | null;
  handle: string;
  content: string;
  createdAt: string;
  mine: boolean;
  canDelete?: boolean;
  replyToUserName?: string | null;
  replies: PostComment[];
}

export interface PostCommentsData {
  comments: PostComment[];
  total: number;
}

export interface HomeStats {
  treeholeUpdates: string;
  hotTopics: string;
  alumniPosts: string;
}

export interface TopicGroup {
  id: string;
  name: string;
  description: string;
  heat: string;
  destination: '/' | '/alumni';
  accent: AccentTone;
  tags: string[];
  emoji: string;
}

export interface RankingItem {
  id: string;
  label: string;
  heat: string;
}

export interface NoticeItem {
  id: string;
  title: string;
  meta: string;
}

export interface AnnouncementSummary {
  code: string;
  title: string;
  summary: string;
  category: string;
  pinned: boolean;
  popupEnabled: boolean;
  popupOncePerSession: boolean;
  status: string;
  publishedAt: string;
  expireAt: string;
}

export interface AnnouncementDetail extends AnnouncementSummary {
  content: string;
}

export interface AnnouncementPopup {
  code: string;
  title: string;
  content: string;
  popupOncePerSession: boolean;
}

export interface AnnouncementSavePayload {
  title: string;
  summary: string;
  content: string;
  category: string;
  pinned: boolean;
  popupEnabled: boolean;
  popupOncePerSession: boolean;
  publishedAt?: string | null;
  expireAt?: string | null;
}

export interface AlumniContact {
  id: string;
  name: string;
  meta: string;
  focus: string;
  avatar: string;
  followed?: boolean;
}

export interface StoryCard {
  id: string;
  title: string;
  meta: string;
}

export interface SearchResult {
  keyword: string;
  total: number;
  posts: FeedPost[];
  stories: StoryCard[];
  contacts: AlumniContact[];
}

export interface ProfileStat {
  label: string;
  value: string;
}

export interface UserProfile {
  userCode?: string;
  name: string;
  tagline: string;
  college: string;
  year: string;
  bio: string;
  avatar: string;
  badges: string[];
  stats: ProfileStat[];
}

export interface Role {
  id?: number;
  code: string;
  name: string;
}

export interface Permission {
  id?: number;
  code: string;
  name: string;
}

export interface AuthUser {
  id: number;
  userCode: string;
  username: string;
  email: string;
  name: string;
  avatar: string;
  accountStatus: string;
  roles: Role[];
  permissions: Permission[];
}

export interface ReportSummary {
  reportCode: string;
  targetType: 'POST' | 'COMMENT' | 'USER';
  targetCode: string;
  reasonCode: string;
  reasonDetail: string | null;
  status: 'OPEN' | 'RESOLVED' | 'REJECTED';
  resolutionCode: string | null;
  resolutionNote: string | null;
  createdAt: string;
}

export interface AdminUser {
  id: number;
  userCode: string;
  username: string;
  name: string;
  accountStatus: string;
  roles: Role[];
}

export interface AuditLog {
  id: number;
  actorUserId: number | null;
  actorRoleSnapshot: string;
  actionType: string;
  targetType: string;
  targetId: number | null;
  targetCode: string;
  createdAt: string;
}

export interface Message {
  id: string;
  sender: 'me' | 'them';
  text: string;
  time: string;
  messageType?: string;
  status?: string;
  recalled?: boolean;
  recalledAt?: string | null;
  canRecall?: boolean;
}

export interface Conversation {
  id: string;
  name: string;
  subtitle: string;
  avatar: string;
  lastMessage: string;
  time: string;
  unreadCount: number;
  messages: Message[];
}

export interface DmConversationPeer {
  userCode: string;
  name: string;
  subtitle: string;
  avatar: string;
}

export interface DmConversationSummary {
  conversationCode: string;
  peer: DmConversationPeer;
  lastMessage: string | null;
  displayTime: string | null;
  unreadCount: number;
}

export interface DmConversationDetail extends DmConversationSummary {
  status: string;
  messages: Message[];
}

export interface ComposePayload {
  title: string;
  content: string;
  topic: string;
  audience: Audience;
  anonymous: boolean;
}

export interface TrendingTopicAdmin {
  topicKey: string;
  displayName: string;
  mergeTargetKey: string | null;
  hidden: boolean;
  pinned: boolean;
  sortOrder: number;
  postCount: number;
  interactionCount: number;
  uniqueAuthorCount: number;
  score: number;
}

export interface TrendingTopicRulePayload {
  topicKey: string;
  displayName?: string;
  mergeTargetKey?: string;
  hidden?: boolean;
  pinned?: boolean;
  sortOrder?: number;
}
