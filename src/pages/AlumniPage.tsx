import { useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { alumniContacts, alumniStories } from '../data/siteData';
import { useAppContext } from '../context/AppContext';
import { PostCard } from '../components/PostCard';

const alumniFilters = ['全部动态', '职场内推', '生活闲聊', '校友故事'];

export function AlumniPage() {
  const { alumniPosts, followedIds, toggleFollow } = useAppContext();
  const [keyword, setKeyword] = useState('');
  const [activeFilter, setActiveFilter] = useState('全部动态');
  const [searchParams] = useSearchParams();
  const topicFromQuery = searchParams.get('topic');

  const visiblePosts = useMemo(() => {
    return alumniPosts.filter((post) => {
      const matchesFilter =
        activeFilter === '全部动态'
          ? true
          : post.topic === activeFilter || (topicFromQuery ? post.topic === topicFromQuery : false);

      const matchesQuery =
        !keyword ||
        `${post.title ?? ''}${post.content}${post.author}${post.topic}`
          .toLowerCase()
          .includes(keyword.toLowerCase());

      const matchesTopic = !topicFromQuery || post.topic === topicFromQuery || activeFilter !== '全部动态';

      return matchesFilter && matchesQuery && matchesTopic;
    });
  }, [activeFilter, alumniPosts, keyword, topicFromQuery]);

  return (
    <div className="container page-stack">
      <section className="hero hero--alumni">
        <div className="hero__content">
          <p className="eyebrow">校友圈</p>
          <h1>把武大人的轨迹重新连成网，让经验和机会都流动起来。</h1>
          <p className="hero__description">
            校友圈承接原有校友页面内容，加入真实路由、筛选和关注交互。这里既能看返校记忆，也能看内推信息和生活近况。
          </p>
        </div>
      </section>

      <div className="page-grid page-grid--alumni">
        <aside className="sidebar-column">
          <section className="surface-card">
            <div className="section-head">
              <h2>热门校友故事</h2>
            </div>
            <div className="list-stack">
              {alumniStories.map((story) => (
                <article key={story.id} className="mini-panel">
                  <strong>{story.title}</strong>
                  <span>{story.meta}</span>
                </article>
              ))}
            </div>
          </section>
        </aside>

        <section className="content-column">
          <div className="toolbar-card">
            <label className="search-bar search-bar--wide">
              <input
                value={keyword}
                onChange={(event) => setKeyword(event.target.value)}
                placeholder="搜索校友动态、岗位或城市..."
              />
            </label>
            <div className="filter-row">
              {alumniFilters.map((filter) => (
                <button
                  key={filter}
                  className={`filter-chip${activeFilter === filter ? ' is-active' : ''}`}
                  type="button"
                  onClick={() => setActiveFilter(filter)}
                >
                  {filter}
                </button>
              ))}
            </div>
          </div>

          <div className="post-stack">
            {visiblePosts.map((post) => (
              <PostCard key={post.id} post={post} />
            ))}
          </div>
        </section>

        <aside className="sidebar-column">
          <section className="surface-card tone-jade">
            <div className="section-head">
              <h2>人脉网络</h2>
            </div>
            <div className="list-stack">
              {alumniContacts.map((contact) => {
                const followed = followedIds.includes(contact.id);

                return (
                  <article key={contact.id} className="contact-card">
                    <img src={contact.avatar} alt={contact.name} />
                    <div>
                      <strong>{contact.name}</strong>
                      <span>{contact.meta}</span>
                      <small>{contact.focus}</small>
                    </div>
                    <button
                      className={`mini-button${followed ? ' is-active' : ''}`}
                      type="button"
                      onClick={() => toggleFollow(contact.id)}
                    >
                      {followed ? '已关注' : '关注'}
                    </button>
                  </article>
                );
              })}
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
}
