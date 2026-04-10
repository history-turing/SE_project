import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { searchAll } from '../services/api';
import type { SearchResult } from '../types';

export function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const keyword = (searchParams.get('q') ?? '').trim();
  const [draft, setDraft] = useState(keyword);
  const [result, setResult] = useState<SearchResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setDraft(keyword);
  }, [keyword]);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      if (!keyword) {
        setResult(null);
        setError('');
        return;
      }

      setLoading(true);
      setError('');
      try {
        const data = await searchAll(keyword);
        if (!cancelled) {
          setResult(data);
        }
      } catch (loadError) {
        if (!cancelled) {
          setError('搜索暂时失败，请稍后重试。');
          setResult(null);
        }
        console.error('搜索失败。', loadError);
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
  }, [keyword]);

  function submitSearch() {
    const next = draft.trim();
    setSearchParams(next ? { q: next } : {});
  }

  return (
    <div className="container page-stack">
      <section className="surface-card">
        <div className="section-head">
          <div>
            <p className="eyebrow">Search</p>
            <h2>搜索结果</h2>
          </div>
          <span className="search-summary">
            {keyword ? `${result?.total ?? 0} 条匹配` : '输入关键词开始搜索'}
          </span>
        </div>

        <div className="search-page__toolbar">
          <label className="search-bar search-bar--wide">
            <input
              value={draft}
              placeholder="搜索帖子、校友故事或联系人..."
              onChange={(event) => setDraft(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  submitSearch();
                }
              }}
            />
          </label>
          <button className="primary-button" type="button" onClick={submitSearch}>
            搜索
          </button>
        </div>

        {error ? <p className="auth-error">{error}</p> : null}
        {!keyword && !error ? <p className="search-empty">输入一个关键词，查看全站结果。</p> : null}
        {loading ? <p className="search-empty">搜索中...</p> : null}
      </section>

      {!loading && result ? (
        <>
          <section className="surface-card">
            <div className="section-head">
              <h2>帖子</h2>
              <span className="eyebrow">{result.posts.length} 条</span>
            </div>
            <div className="list-stack">
              {result.posts.length ? (
                result.posts.map((post) => (
                  <article key={post.id} className={`post-card tone-${post.accent}`}>
                    <div className="post-card__meta">
                      <div>
                        <span className="post-card__topic">{post.topic}</span>
                        <span className="post-card__badge">{post.audience}</span>
                      </div>
                      <span className="post-card__time">{post.createdAt}</span>
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
                    <div className="search-post-stats">
                      <span>点赞 {post.likes}</span>
                      <span>评论 {post.comments}</span>
                      <span>收藏 {post.saves}</span>
                    </div>
                  </article>
                ))
              ) : (
                <p className="search-empty">没有匹配的帖子。</p>
              )}
            </div>
          </section>

          <section className="surface-card">
            <div className="section-head">
              <h2>校友故事</h2>
              <span className="eyebrow">{result.stories.length} 条</span>
            </div>
            <div className="list-stack">
              {result.stories.length ? (
                result.stories.map((story) => (
                  <article key={story.id} className="mini-panel">
                    <strong>{story.title}</strong>
                    <span>{story.meta}</span>
                  </article>
                ))
              ) : (
                <p className="search-empty">没有匹配的校友故事。</p>
              )}
            </div>
          </section>

          <section className="surface-card">
            <div className="section-head">
              <h2>联系人</h2>
              <span className="eyebrow">{result.contacts.length} 位</span>
            </div>
            <div className="list-stack">
              {result.contacts.length ? (
                result.contacts.map((contact) => (
                  <article key={contact.id} className="contact-card">
                    <img src={contact.avatar} alt={contact.name} />
                    <div>
                      <strong>{contact.name}</strong>
                      <span>{contact.meta}</span>
                      <small>{contact.focus}</small>
                    </div>
                  </article>
                ))
              ) : (
                <p className="search-empty">没有匹配的联系人。</p>
              )}
            </div>
          </section>
        </>
      ) : null}
    </div>
  );
}
