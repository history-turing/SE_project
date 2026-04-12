import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAppContext } from '../context/AppContext';
import { MessageRecallMenu } from '../components/MessageRecallMenu';
import { Icon } from '../components/Icon';
import { PostCard } from '../components/PostCard';

type ProfileTab = 'posts' | 'saved' | 'messages';

function resolveTab(value: string | null): ProfileTab {
  if (value === 'saved' || value === 'messages') {
    return value;
  }
  return 'posts';
}

export function ProfilePage() {
  const {
    activeConversation,
    activeConversationCode,
    alumniPosts,
    communityPosts,
    conversations,
    messagesLoading,
    myPosts,
    profile,
    recallMessage,
    savedIds,
    selectConversation,
    sendMessage,
  } = useAppContext();
  const [searchParams, setSearchParams] = useSearchParams();
  const [tab, setTab] = useState<ProfileTab>(() => resolveTab(searchParams.get('tab')));
  const [draft, setDraft] = useState('');

  useEffect(() => {
    const nextTab = resolveTab(searchParams.get('tab'));
    if (nextTab !== tab) {
      setTab(nextTab);
    }
  }, [searchParams, tab]);

  useEffect(() => {
    const requestedConversation = searchParams.get('conversation');
    if (tab === 'messages' && requestedConversation && requestedConversation !== activeConversationCode) {
      void selectConversation(requestedConversation);
    }
  }, [activeConversationCode, searchParams, selectConversation, tab]);

  const savedPosts = useMemo(() => {
    const unique = new Map<string, (typeof communityPosts)[number]>();

    [...communityPosts, ...alumniPosts, ...myPosts].forEach((post) => {
      if (savedIds.includes(post.id)) {
        unique.set(post.id, post);
      }
    });

    return Array.from(unique.values());
  }, [alumniPosts, communityPosts, myPosts, savedIds]);

  function updateQuery(nextTab: ProfileTab, conversationCode?: string) {
    const nextParams = new URLSearchParams(searchParams);
    if (nextTab === 'posts') {
      nextParams.delete('tab');
      nextParams.delete('conversation');
    } else {
      nextParams.set('tab', nextTab);
      if (conversationCode) {
        nextParams.set('conversation', conversationCode);
      } else if (nextTab !== 'messages') {
        nextParams.delete('conversation');
      }
    }
    setSearchParams(nextParams, { replace: true });
  }

  async function handleSend() {
    const nextDraft = draft.trim();
    if (!nextDraft) {
      return;
    }

    await sendMessage(nextDraft);
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
              {profile.college} 路 {profile.year}
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
          onClick={() => {
            setTab('posts');
            updateQuery('posts');
          }}
        >
          我的树洞
        </button>
        <button
          className={`filter-chip${tab === 'saved' ? ' is-active' : ''}`}
          type="button"
          onClick={() => {
            setTab('saved');
            updateQuery('saved');
          }}
        >
          我的收藏
        </button>
        <button
          className={`filter-chip${tab === 'messages' ? ' is-active' : ''}`}
          type="button"
          onClick={() => {
            setTab('messages');
            updateQuery('messages', activeConversationCode || conversations[0]?.conversationCode);
          }}
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
            {conversations.length ? (
              conversations.map((conversation) => (
                <button
                  key={conversation.conversationCode}
                  className={`conversation-item${
                    conversation.conversationCode === activeConversationCode ? ' is-active' : ''
                  }`}
                  type="button"
                  onClick={() => {
                    void selectConversation(conversation.conversationCode);
                    updateQuery('messages', conversation.conversationCode);
                  }}
                >
                  {conversation.peer.avatar ? (
                    <img src={conversation.peer.avatar} alt={conversation.peer.name} />
                  ) : (
                    <div className="conversation-item__avatar">{conversation.peer.name.slice(0, 1)}</div>
                  )}
                  <div>
                    <strong>{conversation.peer.name}</strong>
                    <span>{conversation.peer.subtitle}</span>
                    <small>{conversation.lastMessage || '还没有消息'}</small>
                  </div>
                  <div className="conversation-item__meta">
                    <span>{conversation.displayTime || ''}</span>
                    {conversation.unreadCount ? <b>{conversation.unreadCount}</b> : null}
                  </div>
                </button>
              ))
            ) : (
              <section className="surface-card empty-card">
                <h3>还没有私信会话</h3>
                <p>从帖子作者、评论作者或用户主页进入私信后，这里会实时显示新的会话。</p>
              </section>
            )}
          </aside>

          <div className="message-panel">
            {activeConversation ? (
              <>
                <div className="message-panel__head">
                  <div>
                    <h2>{activeConversation.peer.name}</h2>
                    <p>{activeConversation.peer.subtitle}</p>
                  </div>
                </div>

                <div className="message-thread">
                  {messagesLoading ? <p className="search-empty">会话加载中...</p> : null}
                  {!messagesLoading && !activeConversation.messages.length ? (
                    <p className="search-empty">还没有消息，先打个招呼吧。</p>
                  ) : null}
                  {activeConversation.messages.map((message) => (
                    <article key={message.id} className={`bubble${message.sender === 'me' ? ' bubble--me' : ''}`}>
                      <div className="bubble__content">
                        <p>{message.text}</p>
                        <MessageRecallMenu
                          visible={Boolean(message.canRecall)}
                          onRecall={() => {
                            void recallMessage(message.id);
                          }}
                        />
                      </div>
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
                  <button className="primary-button" type="button" onClick={() => void handleSend()}>
                    <Icon name="send" className="icon" />
                    发送
                  </button>
                </div>
              </>
            ) : (
              <section className="surface-card empty-card">
                <h2>选择一个会话</h2>
                <p>左侧显示的是你的真实私信会话列表，点开后即可查看完整消息记录。</p>
              </section>
            )}
          </div>
        </section>
      ) : null}
    </div>
  );
}
