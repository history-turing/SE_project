import { useEffect, useRef, useState } from 'react';
import { createCommentReply, createPostComment, getPostComments } from '../services/api';
import type { PostComment } from '../types';

interface PostCommentsPanelProps {
  postId: string;
  initialCount: number;
  onCountChange: (count: number) => void;
}

function normalizeComment(comment: PostComment): PostComment {
  return {
    ...comment,
    replies: comment.replies ?? [],
  };
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

  useEffect(() => {
    onCountChangeRef.current = onCountChange;
  }, [onCountChange]);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      try {
        const data = await getPostComments(postId);
        if (!cancelled) {
          setComments(data.comments.map(normalizeComment));
          setTotal(data.total);
          onCountChangeRef.current(data.total);
          setErrorMessage('');
        }
      } catch (error) {
        console.error('加载评论失败。', error);
        if (!cancelled) {
          setErrorMessage('评论区暂时不可用，请稍后再试。');
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

  function syncTotal(updater: number | ((current: number) => number)) {
    setTotal((current) => {
      const nextTotal = typeof updater === 'function' ? updater(current) : updater;
      onCountChangeRef.current(nextTotal);
      return nextTotal;
    });
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
      setComments((current) => [...current, created]);
      setContent('');
      syncTotal((current) => current + 1);
    } catch (error) {
      console.error('发表评论失败。', error);
      setErrorMessage('评论发送失败，请确认登录状态后重试。');
    } finally {
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
      setComments((current) =>
        current.map((comment) =>
          comment.id === rootCommentId
            ? { ...comment, replies: [...comment.replies, created] }
            : comment,
        ),
      );
      setReplyContent('');
      setReplyTargetId('');
      syncTotal((current) => current + 1);
    } catch (error) {
      console.error('回复评论失败。', error);
      setErrorMessage('回复发送失败，请稍后再试。');
    } finally {
      setSubmitting(false);
    }
  }

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
        {comments.map((comment) => (
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
    </section>
  );
}
