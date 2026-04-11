import { useState } from 'react';
import { useAppContext } from '../context/AppContext';
import { deletePost } from '../services/api';
import type { FeedPost } from '../types';
import { Icon } from './Icon';
import { PostCommentsPanel } from './PostCommentsPanel';
import { ReportDialog } from './ReportDialog';

interface PostCardProps {
  post: FeedPost;
}

export function PostCard({ post }: PostCardProps) {
  const { likedIds, removePost, savedIds, toggleLike, toggleSave, setPostCommentCount } = useAppContext();
  const liked = likedIds.includes(post.id);
  const saved = savedIds.includes(post.id);
  const [commentsOpen, setCommentsOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');
  const [reportOpen, setReportOpen] = useState(false);

  async function handleDeletePost() {
    if (deleting) {
      return;
    }

    setDeleting(true);
    setDeleteError('');
    try {
      await deletePost(post.id);
      removePost(post.id);
    } catch (error) {
      console.error('删除帖子失败。', error);
      setDeleteError('删除帖子失败，请稍后重试。');
    } finally {
      setDeleting(false);
    }
  }

  return (
    <article className={`post-card tone-${post.accent}`}>
      <div className="post-card__meta">
        <div>
          <span className="post-card__topic">{post.topic}</span>
          {post.badge ? <span className="post-card__badge">{post.badge}</span> : null}
        </div>
        <span className="post-card__time">
          {post.location ? `${post.location} · ` : ''}
          {post.createdAt}
        </span>
      </div>

      <div className="post-card__header">
        <div className="post-card__avatar">{post.author.slice(0, 1)}</div>
        <div>
          <p className="post-card__author">{post.author}</p>
          <p className="post-card__handle">{post.handle}</p>
        </div>
      </div>

      {post.title ? <h3 className="post-card__title">{post.title}</h3> : null}
      <p className="post-card__content">{post.content}</p>

      {post.image ? (
        <div className="post-card__image-wrap">
          <img className="post-card__image" src={post.image} alt={post.title ?? post.topic} />
        </div>
      ) : null}

      {deleteError ? <p className="auth-error">{deleteError}</p> : null}

      <div className="post-card__actions">
        <button
          className={`icon-button ${liked ? 'is-active' : ''}`}
          type="button"
          onClick={() => void toggleLike(post.id)}
        >
          <Icon name="heart" className="icon" />
          <span>{post.likes}</span>
        </button>
        <button
          className={`icon-button ${commentsOpen ? 'is-active' : ''}`}
          type="button"
          onClick={() => setCommentsOpen((current) => !current)}
        >
          <Icon name="chat" className="icon" />
          <span>{post.comments}</span>
        </button>
        <button
          className={`icon-button ${saved ? 'is-active' : ''}`}
          type="button"
          onClick={() => void toggleSave(post.id)}
        >
          <Icon name="bookmark" className="icon" />
          <span>{post.saves}</span>
        </button>
      </div>

      <div className="post-card__moderation">
        {!post.mine ? (
          <button className="mini-button" type="button" onClick={() => setReportOpen(true)}>
            举报帖子
          </button>
        ) : null}
        {post.canDelete ? (
          <button className="mini-button" type="button" disabled={deleting} onClick={() => void handleDeletePost()}>
            {deleting ? '删除中...' : '删除帖子'}
          </button>
        ) : null}
      </div>

      {commentsOpen ? (
        <PostCommentsPanel
          postId={post.id}
          initialCount={post.comments}
          onCountChange={(count) => setPostCommentCount(post.id, count)}
        />
      ) : null}

      <ReportDialog
        open={reportOpen}
        targetType="POST"
        targetCode={post.id}
        onClose={() => setReportOpen(false)}
      />
    </article>
  );
}
