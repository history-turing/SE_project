import { useEffect, useState } from 'react';
import { createCommentReply, createPostComment, getPostComments } from '../services/api';
import type { PostComment } from '../types';

interface PostCommentsPanelProps {
  postId: string;
  initialCount: number;
  onCountChange: (count: number) => void;
}

export function PostCommentsPanel({ postId, initialCount, onCountChange }: PostCommentsPanelProps) {
  const [comments, setComments] = useState<PostComment[]>([]);
  const [total, setTotal] = useState(initialCount);
  const [content, setContent] = useState('');
  const [replyTargetId, setReplyTargetId] = useState('');
  const [replyContent, setReplyContent] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      try {
        const data = await getPostComments(postId);
        if (!cancelled) {
          setComments(data.comments);
          setTotal(data.total);
          onCountChange(data.total);
        }
      } catch (error) {
        console.error('加载评论失败。', error);
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
  }, [onCountChange, postId]);

  function syncTotal(nextTotal: number) {
    setTotal(nextTotal);
    onCountChange(nextTotal);
  }

  async function submitRootComment() {
    const next = content.trim();
    if (!next || submitting) {
      return;
    }

    setSubmitting(true);
    try {
      const created = await createPostComment(postId, next);
      setComments((current) => [...current, created]);
      setContent('');
      syncTotal(total + 1);
    } catch (error) {
      console.error('发表评论失败。', error);
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
    try {
      const created = await createCommentReply(postId, rootCommentId, next);
      setComments((current) =>
        current.map((comment) =>
          comment.id === rootCommentId
            ? { ...comment, replies: [...comment.replies, created] }
            : comment,
        ),
      );
      setReplyContent('');
      setReplyTargetId('');
      syncTotal(total + 1);
    } catch (error) {
      console.error('回复评论失败。', error);
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
          onChange={(event) => setContent(event.target.value)}
        />
        <button className="mini-button" type="button" onClick={() => void submitRootComment()}>
          发送评论
        </button>
      </div>

      {loading ? <p className="search-empty">评论加载中...</p> : null}

      {!loading && !comments.length ? (
        <p className="search-empty">还没有评论，来留下第一句话吧。</p>
      ) : null}

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
                  onChange={(event) => setReplyContent(event.target.value)}
                />
                <button className="mini-button" type="button" onClick={() => void submitReply(comment.id)}>
                  发送回复
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
