import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { PostCard } from '../components/PostCard';
import { useAppContext } from '../context/AppContext';
import { campusMoodQuotes, encouragementQuotes } from '../data/homeQuotes';

const homeFilters = ['全部', '校园日常', '学业交流', '生活闲聊', '表白墙', '失物招领'];
const ROTATION_INTERVAL_MS = 2 * 60 * 1000;

function getRotationIndex(length: number, offset = 0) {
  return Math.floor(Date.now() / ROTATION_INTERVAL_MS + offset) % length;
}

export function HomePage() {
  const { communityPosts, topicGroups, topicRankings, campusNotices, homeStats, refreshHomeStats } =
    useAppContext();
  const refreshHomeStatsRef = useRef(refreshHomeStats);
  const [searchParams, setSearchParams] = useSearchParams();
  const [campusQuoteIndex, setCampusQuoteIndex] = useState(() => getRotationIndex(campusMoodQuotes.length));
  const [encouragementIndex, setEncouragementIndex] = useState(() =>
    getRotationIndex(encouragementQuotes.length, 1),
  );
  const activeTopic = searchParams.get('topic') ?? '全部';
  const keyword = searchParams.get('q') ?? '';
  const campusQuote = campusMoodQuotes[campusQuoteIndex];
  const encouragement = encouragementQuotes[encouragementIndex];

  useEffect(() => {
    refreshHomeStatsRef.current = refreshHomeStats;
  }, [refreshHomeStats]);

  useEffect(() => {
    void refreshHomeStatsRef.current();

    const timer = window.setInterval(() => {
      setCampusQuoteIndex(getRotationIndex(campusMoodQuotes.length));
      setEncouragementIndex(getRotationIndex(encouragementQuotes.length, 1));
      void refreshHomeStatsRef.current();
    }, ROTATION_INTERVAL_MS);

    return () => window.clearInterval(timer);
  }, []);

  const filteredPosts = communityPosts.filter((post) => {
    const matchesTopic = activeTopic === '全部' || post.topic === activeTopic;
    const matchesKeyword =
      !keyword ||
      `${post.title ?? ''}${post.content}${post.author}${post.topic}`
        .toLowerCase()
        .includes(keyword.toLowerCase());

    return matchesTopic && matchesKeyword;
  });

  function setTopic(topic: string) {
    if (topic === '全部') {
      setSearchParams(keyword ? { q: keyword } : {});
      return;
    }

    const next = new URLSearchParams(searchParams);
    next.set('topic', topic);
    setSearchParams(next);
  }

  return (
    <div className="container page-stack">
      <section className="hero hero--home">
        <div className="hero__content">
          <p className="eyebrow">武大树洞</p>
          <h1>把今天想说的话，轻轻放进珞珈山的风里。</h1>
          <p className="hero__description">
            首页承接树洞主信息流，聚合校园见闻、匿名低语、学业互助和日常碎片。你可以按话题切换内容，也可以从这里进入更细的专题页面。
          </p>
          <div className="hero__cta-row">
            <Link className="primary-button" to="/topics">
              浏览全部话题
            </Link>
            <Link className="secondary-button" to="/alumni">
              看看校友圈
            </Link>
          </div>
        </div>

        <div className="hero-panel">
          <p className="hero-panel__title">今日树洞温度</p>
          <div className="stat-grid">
            <article className="stat-card">
              <span>今日树洞更新</span>
              <strong>{homeStats.treeholeUpdates}</strong>
            </article>
            <article className="stat-card">
              <span>热议话题</span>
              <strong>{homeStats.hotTopics}</strong>
            </article>
            <article className="stat-card">
              <span>今日校友新帖</span>
              <strong>{homeStats.alumniPosts}</strong>
            </article>
          </div>
          <div className="quote-card">
            <p>“{campusQuote.text}”</p>
            <span>{encouragement.text}</span>
            <small>{campusQuote.source}</small>
            <small>{encouragement.source}</small>
          </div>
        </div>
      </section>

      <section className="topic-strip">
        {topicGroups.slice(0, 4).map((group) => (
          <Link
            key={group.id}
            className={`topic-chip tone-${group.accent}`}
            to={group.destination === '/' ? `/?topic=${group.name}` : `/topics`}
          >
            <span>{group.emoji}</span>
            <strong>{group.name}</strong>
            <small>{group.heat}</small>
          </Link>
        ))}
      </section>

      <div className="page-grid page-grid--home">
        <aside className="sidebar-column">
          <section className="surface-card">
            <div className="section-head">
              <h2>热议话题</h2>
              <Link className="secondary-button secondary-button--compact" to="/topics/trending">
                查看更多
              </Link>
            </div>
            <div className="list-stack">
              {topicRankings.slice(0, 5).map((item) => (
                <Link key={item.id} className="rank-item" to={`/?topic=${encodeURIComponent(item.label.slice(1))}`}>
                  <strong>{item.label}</strong>
                  <span>{item.heat}</span>
                </Link>
              ))}
            </div>
          </section>

          <section className="surface-card">
            <div className="section-head">
              <h2>校园公告</h2>
              <Link className="secondary-button secondary-button--compact" to="/announcements">
                查看更多
              </Link>
            </div>
            <div className="list-stack">
              {campusNotices.map((notice) => (
                <Link key={notice.id} className="notice-item" to={`/announcements?code=${encodeURIComponent(notice.id)}`}>
                  <strong>{notice.title}</strong>
                  <span>{notice.meta}</span>
                </Link>
              ))}
            </div>
          </section>
        </aside>

        <section className="content-column">
          <div className="filter-row">
            {homeFilters.map((filter) => (
              <button
                key={filter}
                className={`filter-chip${activeTopic === filter ? ' is-active' : ''}`}
                type="button"
                onClick={() => setTopic(filter)}
              >
                {filter}
              </button>
            ))}
          </div>

          <div className="post-stack">
            {filteredPosts.map((post) => (
              <PostCard key={post.id} post={post} />
            ))}
          </div>
        </section>

        <aside className="sidebar-column">
          <section className="surface-card tone-rose">
            <div className="section-head">
              <h2>今日推荐路径</h2>
            </div>
            <div className="list-stack">
              <Link className="mini-panel" to="/topics">
                <strong>先逛话题页</strong>
                <span>按分区快速找到和你情绪同频的入口</span>
              </Link>
              <Link className="mini-panel" to="/alumni">
                <strong>再看校友圈</strong>
                <span>求职、返校、成长故事都集中在这里</span>
              </Link>
              <Link className="mini-panel" to="/profile">
                <strong>最后回到个人页</strong>
                <span>整理自己的树洞、收藏和消息</span>
              </Link>
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
}
