import type { PostComment, PostCommentsData } from '../types';

const LOCAL_COMMENT_KEY = 'whu-treehole-local-comments';

interface LocalCommentMap {
  [postId: string]: PostComment[];
}

function readAllComments(): LocalCommentMap {
  if (typeof window === 'undefined') {
    return {};
  }

  const raw = window.localStorage.getItem(LOCAL_COMMENT_KEY);
  if (!raw) {
    return {};
  }

  try {
    return JSON.parse(raw) as LocalCommentMap;
  } catch {
    return {};
  }
}

function writeAllComments(data: LocalCommentMap) {
  if (typeof window === 'undefined') {
    return;
  }

  window.localStorage.setItem(LOCAL_COMMENT_KEY, JSON.stringify(data));
}

function createLocalComment(content: string, postId: string): PostComment {
  const timestamp = Date.now();

  return {
    id: `local-comment-${timestamp}`,
    postId,
    parentCommentCode: null,
    author: '我',
    handle: '本地评论',
    content,
    createdAt: new Date(timestamp).toLocaleString('zh-CN', {
      hour12: false,
    }),
    mine: true,
    replyToUserName: null,
    replies: [],
  };
}

export function getLocalPostComments(postId: string): PostCommentsData {
  const comments = readAllComments()[postId] ?? [];
  return {
    comments,
    total: comments.reduce((count, comment) => count + 1 + comment.replies.length, 0),
  };
}

export function saveLocalRootComment(postId: string, content: string): PostComment {
  const store = readAllComments();
  const nextComment = createLocalComment(content, postId);
  const current = store[postId] ?? [];
  store[postId] = [...current, nextComment];
  writeAllComments(store);
  return nextComment;
}

export function saveLocalReply(postId: string, rootCommentId: string, content: string): PostComment | null {
  const store = readAllComments();
  const current = store[postId] ?? [];
  let createdReply: PostComment | null = null;

  store[postId] = current.map((comment) => {
    if (comment.id !== rootCommentId) {
      return comment;
    }

    createdReply = {
      ...createLocalComment(content, postId),
      parentCommentCode: rootCommentId,
      replyToUserName: comment.author,
    };

    return {
      ...comment,
      replies: [...comment.replies, createdReply],
    };
  });

  writeAllComments(store);
  return createdReply;
}

export function clearLocalPostComments() {
  if (typeof window === 'undefined') {
    return;
  }

  window.localStorage.removeItem(LOCAL_COMMENT_KEY);
}
