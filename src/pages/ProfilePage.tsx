import { useMemo, useState } from 'react';
import { useAppContext } from '../context/AppContext';
import { Icon } from '../components/Icon';
import { PostCard } from '../components/PostCard';

type ProfileTab = 'posts' | 'saved' | 'messages';

export function ProfilePage() {
  const {
    activeConversationId,
    alumniPosts,
    communityPosts,
    conversations,
    myPosts,
    profile,
    savedIds,
    selectConversation,
    sendMessage,
  } = useAppContext();
  const [tab, setTab] = useState<ProfileTab>('posts');
  const [draft, setDraft] = useState('');

  const savedPosts = useMemo(() => {
    const unique = new Map<string, (typeof communityPosts)[number]>();

    [...communityPosts, ...alumniPosts, ...myPosts].forEach((post) => {
      if (savedIds.includes(post.id)) {
        unique.set(post.id, post);
      }
    });

    return Array.from(unique.values());
  }, [alumniPosts, communityPosts, myPosts, savedIds]);

  const activeConversation =
    conversations.find((conversation) => conversation.id === activeConversationId) ??
    conversations[0];

  function handleSend() {
    sendMessage(draft);
    setDraft('');
  }

  return (
    <div className="container page-stack">
      <section className="profile-hero">
        <div className="profile-hero__info">
          <img className="profile-hero__avatar" src={profile.avatar} alt={profile.name} />
          <div>
            <p className="eyebrow">个人主页</p>
            <h1>{profile.name}</h1>
            <p className="profile-hero__meta">
              {profile.college} · {profile.year}
            </p>
            <p className="hero__description">{profile.bio}</p>
            <div className="tag-row">
              {profile.badges.map((badge) => (
                <span key={badge} className="tag">
                  {badge}
                </span>
              ))}
            </div>
          </div>
        </div>

        <div className="stat-grid">
          {profile.stats.map((item) => (
            <article key={item.label} className="stat-card">
              <span>{item.label}</span>
              <strong>{item.value}</strong>
            </article>
          ))}
        </div>
      </section>

      <div className="filter-row">
        <button
          className={`filter-chip${tab === 'posts' ? ' is-active' : ''}`}
          type="button"
          onClick={() => setTab('posts')}
        >
          我的树洞
        </button>
        <button
          className={`filter-chip${tab === 'saved' ? ' is-active' : ''}`}
          type="button"
          onClick={() => setTab('saved')}
        >
          我的收藏
        </button>
        <button
          className={`filter-chip${tab === 'messages' ? ' is-active' : ''}`}
          type="button"
          onClick={() => setTab('messages')}
        >
          消息中心
        </button>
      </div>

      {tab === 'posts' ? (
        <div className="post-stack">
          {myPosts.map((post) => (
            <PostCard key={post.id} post={post} />
          ))}
        </div>
      ) : null}

      {tab === 'saved' ? (
        <div className="post-stack">
          {savedPosts.length ? (
            savedPosts.map((post) => <PostCard key={post.id} post={post} />)
          ) : (
            <section className="surface-card empty-card">
              <h2>还没有收藏内容</h2>
              <p>在首页或校友圈点一下收藏按钮，喜欢的内容就会回到这里。</p>
            </section>
          )}
        </div>
      ) : null}

      {tab === 'messages' ? (
        <section className="message-layout">
          <aside className="message-list">
            <div className="section-head">
              <h2>消息会话</h2>
            </div>
            {conversations.map((conversation) => (
              <button
                key={conversation.id}
                className={`conversation-item${
                  conversation.id === activeConversation?.id ? ' is-active' : ''
                }`}
                type="button"
                onClick={() => selectConversation(conversation.id)}
              >
                <img src={conversation.avatar} alt={conversation.name} />
                <div>
                  <strong>{conversation.name}</strong>
                  <span>{conversation.subtitle}</span>
                  <small>{conversation.lastMessage}</small>
                </div>
                <div className="conversation-item__meta">
                  <span>{conversation.time}</span>
                  {conversation.unreadCount ? <b>{conversation.unreadCount}</b> : null}
                </div>
              </button>
            ))}
          </aside>

          <div className="message-panel">
            <div className="message-panel__head">
              <div>
                <h2>{activeConversation?.name}</h2>
                <p>{activeConversation?.subtitle}</p>
              </div>
            </div>

            <div className="message-thread">
              {activeConversation?.messages.map((message) => (
                <article
                  key={message.id}
                  className={`bubble${message.sender === 'me' ? ' bubble--me' : ''}`}
                >
                  <p>{message.text}</p>
                  <span>{message.time}</span>
                </article>
              ))}
            </div>

            <div className="message-compose">
              <textarea
                value={draft}
                onChange={(event) => setDraft(event.target.value)}
                placeholder="在这里继续低语..."
                rows={3}
              />
              <button className="primary-button" type="button" onClick={handleSend}>
                <Icon name="send" className="icon" />
                发送
              </button>
            </div>
          </div>
        </section>
      ) : null}
    </div>
  );
}
