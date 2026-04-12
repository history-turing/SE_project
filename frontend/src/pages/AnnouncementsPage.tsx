import { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { getAnnouncementDetail, getAnnouncements } from '../services/api';
import type { AnnouncementDetail, AnnouncementSummary } from '../types';

export function AnnouncementsPage() {
  const [announcements, setAnnouncements] = useState<AnnouncementSummary[]>([]);
  const [activeDetail, setActiveDetail] = useState<AnnouncementDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState('');
  const [searchParams, setSearchParams] = useSearchParams();

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      setError('');
      try {
        const data = await getAnnouncements();
        if (!cancelled) {
          setAnnouncements(data);
          const code = searchParams.get('code') ?? data[0]?.code ?? '';
          if (code) {
            setSearchParams({ code }, { replace: true });
          }
        }
      } catch (loadError) {
        console.error('加载公告列表失败。', loadError);
        if (!cancelled) {
          setError('公告加载失败，请稍后刷新重试。');
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

  const activeCode = searchParams.get('code') ?? '';
  const activeSummary = useMemo(
    () => announcements.find((item) => item.code === activeCode) ?? announcements[0] ?? null,
    [activeCode, announcements],
  );

  useEffect(() => {
    let cancelled = false;

    async function loadDetail(code: string) {
      setDetailLoading(true);
      try {
        const detail = await getAnnouncementDetail(code);
        if (!cancelled) {
          setActiveDetail(detail);
        }
      } catch (loadError) {
        console.error('加载公告详情失败。', loadError);
        if (!cancelled) {
          setError('公告详情加载失败，请稍后重试。');
        }
      } finally {
        if (!cancelled) {
          setDetailLoading(false);
        }
      }
    }

    if (activeSummary?.code) {
      void loadDetail(activeSummary.code);
    } else {
      setActiveDetail(null);
    }

    return () => {
      cancelled = true;
    };
  }, [activeSummary?.code]);

  function selectAnnouncement(code: string) {
    setSearchParams({ code });
  }

  return (
    <div className="container page-stack">
      <section className="hero hero--topics">
        <div className="hero__content">
          <p className="eyebrow">Announcements</p>
          <h1>校园公告由后台真实发布，首页只展示精简摘要，完整内容统一沉淀在这里。</h1>
          <p className="hero__description">
            管理员可发布、置顶、下线公告，超级管理员还可以控制登录后的弹窗投放。首页保留原布局，只展示少量重点内容。
          </p>
        </div>
      </section>

      {error ? <p className="auth-error">{error}</p> : null}
      {loading ? <p className="search-empty">公告加载中...</p> : null}

      {!loading ? (
        <div className="page-grid page-grid--home">
          <aside className="sidebar-column">
            <section className="surface-card">
              <div className="section-head">
                <h2>公告列表</h2>
                <span className="eyebrow">{announcements.length} 条</span>
              </div>
              <div className="list-stack">
                {announcements.map((notice) => (
                  <button
                    key={notice.code}
                    className="ranking-panel"
                    type="button"
                    onClick={() => selectAnnouncement(notice.code)}
                  >
                    <div>
                      <p>{notice.title}</p>
                      <span>{notice.category}</span>
                    </div>
                    <strong>{notice.pinned ? '置顶' : '公告'}</strong>
                  </button>
                ))}
              </div>
            </section>
          </aside>

          <section className="content-column">
            <section className="surface-card">
              {detailLoading ? <p className="search-empty">公告详情加载中...</p> : null}
              {!detailLoading && activeDetail ? (
                <div className="announcement-detail">
                  <div className="section-head">
                    <div>
                      <h2>{activeDetail.title}</h2>
                      <p className="search-summary">
                        {activeDetail.category}
                        {activeDetail.publishedAt ? ` · 发布时间 ${activeDetail.publishedAt}` : ''}
                        {activeDetail.expireAt ? ` · 结束时间 ${activeDetail.expireAt}` : ''}
                      </p>
                    </div>
                    <Link className="secondary-button secondary-button--compact" to="/">
                      返回首页
                    </Link>
                  </div>
                  <p className="auth-copy">{activeDetail.summary}</p>
                  <div className="announcement-detail__content">
                    {activeDetail.content.split('\n').map((paragraph) => (
                      <p key={paragraph}>{paragraph}</p>
                    ))}
                  </div>
                </div>
              ) : null}
              {!detailLoading && !activeDetail ? <p className="search-empty">暂无可查看的公告。</p> : null}
            </section>
          </section>

          <aside className="sidebar-column">
            <section className="surface-card tone-gold">
              <div className="section-head">
                <h2>查看建议</h2>
              </div>
              <div className="list-stack">
                <Link className="mini-panel" to="/topics/trending">
                  <strong>去看热议话题</strong>
                  <span>从真实帖子抽象出的讨论趋势，适合快速把握校园当日关注点。</span>
                </Link>
                <Link className="mini-panel" to="/topics">
                  <strong>去话题广场</strong>
                  <span>按板块进入表白墙、失物招领、学业交流等真实主题入口。</span>
                </Link>
              </div>
            </section>
          </aside>
        </div>
      ) : null}
    </div>
  );
}
