import { useEffect, useMemo, useRef, useState } from 'react';
import { createCommentReply, createPostComment, getPostComments } from '../services/api';
import { getLocalPostComments, saveLocalReply, saveLocalRootComment } from '../services/localCommentStore';
import type { PostComment } from '../types';

interface PostCommentsPanelProps {
  postId: string;
  initialCount: number;
  onCountChange: (count: number) => void;
}

const DEFAULT_VISIBLE_COMMENTS = 3;

function normalizeComment(comment: PostComment): PostComment {
  return {
    ...comment,
    replies: (comment.replies ?? []).map(normalizeComment),
  };
}

function mergeComments(remoteComments: PostComment[], localComments: PostComment[]) {
  const merged = new Map<string, PostComment>();

  [...remoteComments, ...localComments].forEach((comment) => {
    merged.set(comment.id, normalizeComment(comment));
  });

  return Array.from(merged.values());
}

function countVisibleComments(comments: PostComment[]) {
  return comments.reduce((count, comment) => count + 1 + comment.replies.length, 0);
}

export function PostCommentsPanel({ postId, initialCount, onCountChange }: PostCommentsPanelProps) {
  const onCountChangeRef = useRef(onCountChange);
  const [comments, setComments] = useState<PostComment[]>([]);
  const [total, setTotal] = useState(initialCount);
  const [content, setContent] = useState('');
  const [replyTargetId, setReplyTargetId] = useState('');
  const [replyContent, setReplyContent] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [expanded, setExpanded] = useState(false);

  useEffect(() => {
    onCountChangeRef.current = onCountChange;
  }, [onCountChange]);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      try {
        const [remoteData, localData] = await Promise.all([
          getPostComments(postId).catch(() => ({ comments: [], total: 0 })),
          Promise.resolve(getLocalPostComments(postId)),
        ]);

        if (!cancelled) {
          const mergedComments = mergeComments(remoteData.comments, localData.comments);
          const mergedTotal = Math.max(remoteData.total, countVisibleComments(mergedComments));
          setComments(mergedComments);
          setTotal(mergedTotal);
          onCountChangeRef.current(mergedTotal);
          setErrorMessage('');
        }
      } catch (error) {
        console.error('加载评论失败。', error);
        if (!cancelled) {
          const localData = getLocalPostComments(postId);
          setComments(localData.comments.map(normalizeComment));
          setTotal(localData.total);
          onCountChangeRef.current(localData.total);
          setErrorMessage('评论区暂时不可用，已切换到本地评论。');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void load();

    return () => {
      cancelled = true;
    };
  }, [postId]);

  function syncComments(nextComments: PostComment[]) {
    setComments(nextComments);
    const nextTotal = countVisibleComments(nextComments);
    setTotal(nextTotal);
    onCountChangeRef.current(nextTotal);
  }

  async function submitRootComment() {
    const next = content.trim();
    if (!next || submitting) {
      return;
    }

    setSubmitting(true);
    setErrorMessage('');
    try {
      const created = normalizeComment(await createPostComment(postId, next));
      syncComments([...comments, created]);
    } catch (error) {
      console.error('发表评论失败，改用本地持久化。', error);
      const localComment = saveLocalRootComment(postId, next);
      syncComments([...comments, normalizeComment(localComment)]);
      setErrorMessage('评论已保存到当前浏览器，本地环境恢复后会继续使用。');
    } finally {
      setContent('');
      setSubmitting(false);
    }
  }

  async function submitReply(rootCommentId: string) {
    const next = replyContent.trim();
    if (!next || submitting) {
      return;
    }

    setSubmitting(true);
    setErrorMessage('');
    try {
      const created = normalizeComment(await createCommentReply(postId, rootCommentId, next));
      syncComments(
        comments.map((comment) =>
          comment.id === rootCommentId
            ? { ...comment, replies: [...comment.replies, created] }
            : comment,
        ),
      );
    } catch (error) {
      console.error('回复评论失败，改用本地持久化。', error);
      const localReply = saveLocalReply(postId, rootCommentId, next);
      if (!localReply) {
        setErrorMessage('回复发送失败，请刷新后重试。');
        setSubmitting(false);
        return;
      }

      syncComments(
        comments.map((comment) =>
          comment.id === rootCommentId
            ? { ...comment, replies: [...comment.replies, normalizeComment(localReply)] }
            : comment,
        ),
      );
      setErrorMessage('回复已保存到当前浏览器，本地环境恢复后会继续使用。');
    } finally {
      setReplyContent('');
      setReplyTargetId('');
      setSubmitting(false);
    }
  }

  const visibleComments = useMemo(() => {
    if (expanded) {
      return comments;
    }
    return comments.slice(0, DEFAULT_VISIBLE_COMMENTS);
  }, [comments, expanded]);

  const shouldCollapse = comments.length > DEFAULT_VISIBLE_COMMENTS;

  return (
    <section className="post-comments">
      <div className="post-comments__head">
        <strong>评论区</strong>
        <span>{total} 条评论</span>
      </div>

      <div className="post-comments__composer">
        <textarea
          placeholder="写下你的评论..."
          value={content}
          onChange={(event) => {
            setContent(event.target.value);
            if (errorMessage) {
              setErrorMessage('');
            }
          }}
        />
        <button
          className="mini-button"
          type="button"
          disabled={submitting || !content.trim()}
          onClick={() => void submitRootComment()}
        >
          {submitting ? '发送中...' : '发送评论'}
        </button>
      </div>

      {errorMessage ? <p className="auth-error">{errorMessage}</p> : null}
      {loading ? <p className="search-empty">评论加载中...</p> : null}

      {!loading && !comments.length ? <p className="search-empty">还没有评论，来留下第一句话吧。</p> : null}

      <div className="post-comments__list">
        {visibleComments.map((comment) => (
          <article key={comment.id} className="post-comment">
            <div className="post-comment__header">
              <div>
                <strong>{comment.author}</strong>
                <span>{comment.handle}</span>
              </div>
              <small>{comment.createdAt}</small>
            </div>
            <p>{comment.content}</p>
            <button
              className="mini-button"
              type="button"
              onClick={() => setReplyTargetId(replyTargetId === comment.id ? '' : comment.id)}
            >
              回复
            </button>

            {replyTargetId === comment.id ? (
              <div className="post-comment__reply-box">
                <textarea
                  placeholder={`回复 ${comment.author}...`}
                  value={replyContent}
                  onChange={(event) => {
                    setReplyContent(event.target.value);
                    if (errorMessage) {
                      setErrorMessage('');
                    }
                  }}
                />
                <button
                  className="mini-button"
                  type="button"
                  disabled={submitting || !replyContent.trim()}
                  onClick={() => void submitReply(comment.id)}
                >
                  {submitting ? '发送中...' : '发送回复'}
                </button>
              </div>
            ) : null}

            {comment.replies.length ? (
              <div className="post-comment__reply-list">
                {comment.replies.map((reply) => (
                  <article key={reply.id} className="post-comment post-comment--reply">
                    <div className="post-comment__header">
                      <div>
                        <strong>{reply.author}</strong>
                        <span>{reply.handle}</span>
                      </div>
                      <small>{reply.createdAt}</small>
                    </div>
                    <p>
                      {reply.replyToUserName ? <b>@{reply.replyToUserName} </b> : null}
                      {reply.content}
                    </p>
                  </article>
                ))}
              </div>
            ) : null}
          </article>
        ))}
      </div>

      {shouldCollapse ? (
        <button className="mini-button post-comments__toggle" type="button" onClick={() => setExpanded((current) => !current)}>
          {expanded ? '收起评论' : `展开全部 ${comments.length} 条评论`}
        </button>
      ) : null}
    </section>
  );
}
