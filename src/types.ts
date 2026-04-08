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

export interface AlumniContact {
  id: string;
  name: string;
  meta: string;
  focus: string;
  avatar: string;
}

export interface StoryCard {
  id: string;
  title: string;
  meta: string;
}

export interface ProfileStat {
  label: string;
  value: string;
}

export interface UserProfile {
  name: string;
  tagline: string;
  college: string;
  year: string;
  bio: string;
  avatar: string;
  badges: string[];
  stats: ProfileStat[];
}

export interface Message {
  id: string;
  sender: 'me' | 'them';
  text: string;
  time: string;
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

export interface ComposePayload {
  title: string;
  content: string;
  topic: string;
  audience: Audience;
  anonymous: boolean;
}
