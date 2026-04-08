import { useAppContext } from '../context/AppContext';
import type { FeedPost } from '../types';
import { Icon } from './Icon';

interface PostCardProps {
  post: FeedPost;
}

export function PostCard({ post }: PostCardProps) {
  const { likedIds, savedIds, toggleLike, toggleSave } = useAppContext();
  const liked = likedIds.includes(post.id);
  const saved = savedIds.includes(post.id);

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

      <div className="post-card__actions">
        <button
          className={`icon-button ${liked ? 'is-active' : ''}`}
          type="button"
          onClick={() => void toggleLike(post.id)}
        >
          <Icon name="heart" className="icon" />
          <span>{post.likes}</span>
        </button>
        <span className="icon-button is-static">
          <Icon name="chat" className="icon" />
          <span>{post.comments}</span>
        </span>
        <button
          className={`icon-button ${saved ? 'is-active' : ''}`}
          type="button"
          onClick={() => void toggleSave(post.id)}
        >
          <Icon name="bookmark" className="icon" />
          <span>{post.saves}</span>
        </button>
      </div>
    </article>
  );
}
