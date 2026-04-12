import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTrendingTopics } from '../services/api';
import type { RankingItem } from '../types';

export function TrendingTopicsPage() {
  const [topics, setTopics] = useState<RankingItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      setError('');
      try {
        const data = await getTrendingTopics();
        if (!cancelled) {
          setTopics(data);
        }
      } catch (loadError) {
        console.error('加载热议话题失败。', loadError);
        if (!cancelled) {
          setError('热议话题加载失败，请稍后刷新重试。');
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
  }, []);

  function openTopic(topicLabel: string) {
    navigate(`/?topic=${encodeURIComponent(topicLabel.replace(/^#/, ''))}`);
  }

  return (
    <div className="container page-stack">
      <section className="hero hero--topics">
        <div className="hero__content">
          <p className="eyebrow">Trending</p>
          <h1>热议话题会根据真实帖子实时抽取，并结合互动热度持续更新。</h1>
          <p className="hero__description">
            这里不是预设文案，而是从近 72 小时真实帖子中提炼出的讨论主题，再叠加评论、点赞、收藏、作者广度和运营规则排序。
          </p>
        </div>
      </section>

      {error ? <p className="auth-error">{error}</p> : null}
      {loading ? <p className="search-empty">热议话题加载中...</p> : null}

      {!loading ? (
        <section className="surface-card">
          <div className="section-head">
            <h2>热议榜单</h2>
            <span className="eyebrow">72h Rolling</span>
          </div>
          {!topics.length ? <p className="search-empty">当前还没有形成热议话题。</p> : null}
          <div className="ranking-grid">
            {topics.map((item, index) => (
              <button
                key={item.id}
                className="ranking-panel"
                type="button"
                onClick={() => openTopic(item.label)}
              >
                <strong>{String(index + 1).padStart(2, '0')}</strong>
                <div>
                  <p>{item.label}</p>
                  <span>{item.heat}</span>
                </div>
              </button>
            ))}
          </div>
        </section>
      ) : null}
    </div>
  );
}
