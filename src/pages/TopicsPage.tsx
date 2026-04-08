import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { topicGroups, topicRankings } from '../data/siteData';

const scopes = ['全部', '校园话题', '校友话题'];

export function TopicsPage() {
  const [scope, setScope] = useState('全部');
  const navigate = useNavigate();

  const visibleTopics = useMemo(() => {
    if (scope === '全部') {
      return topicGroups;
    }

    const destination = scope === '校园话题' ? '/' : '/alumni';
    return topicGroups.filter((group) => group.destination === destination);
  }, [scope]);

  function openTopic(topicName: string, destination: '/' | '/alumni') {
    navigate(`${destination}?topic=${topicName}`);
  }

  return (
    <div className="container page-stack">
      <section className="hero hero--topics">
        <div className="hero__content">
          <p className="eyebrow">话题广场</p>
          <h1>探索珞珈的每一个角落，也探索每一种正在发生的情绪。</h1>
          <p className="hero__description">
            话题页把原先分散的静态内容变成了可跳转、可过滤的入口层。你可以从这里进入首页对应树洞，也可以直接切到校友圈的内推与故事场景。
          </p>
        </div>
      </section>

      <div className="filter-row">
        {scopes.map((item) => (
          <button
            key={item}
            className={`filter-chip${scope === item ? ' is-active' : ''}`}
            type="button"
            onClick={() => setScope(item)}
          >
            {item}
          </button>
        ))}
      </div>

      <section className="topic-grid">
        {visibleTopics.map((topic) => (
          <article key={topic.id} className={`topic-card tone-${topic.accent}`}>
            <div className="topic-card__head">
              <span className="topic-card__emoji">{topic.emoji}</span>
              <span className="topic-card__heat">{topic.heat}</span>
            </div>
            <h2>{topic.name}</h2>
            <p>{topic.description}</p>
            <div className="tag-row">
              {topic.tags.map((tag) => (
                <span key={tag} className="tag">
                  #{tag}
                </span>
              ))}
            </div>
            <button
              className="secondary-button"
              type="button"
              onClick={() => openTopic(topic.name, topic.destination)}
            >
              进入话题
            </button>
          </article>
        ))}
      </section>

      <section className="surface-card">
        <div className="section-head">
          <h2>热门话题排行</h2>
          <span className="eyebrow">实时更新</span>
        </div>
        <div className="ranking-grid">
          {topicRankings.map((item, index) => (
            <button
              key={item.id}
              className="ranking-panel"
              type="button"
              onClick={() => openTopic(item.label.slice(1), '/')}
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
    </div>
  );
}
