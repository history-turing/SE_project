import { useEffect, useMemo, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAppContext } from '../context/AppContext';
import { useAuthContext } from '../context/AuthContext';
import { navItems } from '../data/siteData';
import { getAnnouncementPopup } from '../services/api';
import { AnnouncementPopupModal } from './AnnouncementPopupModal';
import { ComposerModal } from './ComposerModal';
import { Icon } from './Icon';

export function AppShell() {
  const { composePost, notificationSummary, profile } = useAppContext();
  const { hasPermission, logout, user } = useAuthContext();
  const [openComposer, setOpenComposer] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [mobileSearchOpen, setMobileSearchOpen] = useState(false);
  const [popupAnnouncement, setPopupAnnouncement] = useState<{ code: string; title: string; content: string } | null>(
    null,
  );
  const navigate = useNavigate();
  const location = useLocation();
  const canAccessAdmin =
    hasPermission('report.read.any') ||
    hasPermission('user.ban') ||
    hasPermission('role.assign.admin') ||
    hasPermission('trending.read.any') ||
    hasPermission('announcement.read.any') ||
    hasPermission('audit.read.moderation') ||
    hasPermission('audit.read.all');
  const mobileNavItems = useMemo(() => {
    if (!canAccessAdmin) {
      return navItems;
    }

    return [...navItems, { path: '/admin', label: '管理台' }];
  }, [canAccessAdmin]);

  useEffect(() => {
    const keyword = new URLSearchParams(location.search).get('q') ?? '';
    setSearchKeyword(location.pathname === '/search' ? keyword : '');
    setMobileSearchOpen(false);
  }, [location.pathname, location.search]);

  useEffect(() => {
    let cancelled = false;

    async function loadPopup() {
      try {
        const popup = await getAnnouncementPopup();
        if (!popup || cancelled) {
          return;
        }
        const storageKey = `announcement-popup-dismissed:${popup.code}`;
        if (popup.popupOncePerSession && window.sessionStorage.getItem(storageKey) === '1') {
          return;
        }
        setPopupAnnouncement({
          code: popup.code,
          title: popup.title,
          content: popup.content,
        });
      } catch (error) {
        console.error('load announcement popup failed', error);
      }
    }

    void loadPopup();

    return () => {
      cancelled = true;
    };
  }, []);

  function closePopupAnnouncement() {
    if (popupAnnouncement) {
      window.sessionStorage.setItem(`announcement-popup-dismissed:${popupAnnouncement.code}`, '1');
    }
    setPopupAnnouncement(null);
  }

  function submitSearch() {
    const next = searchKeyword.trim();
    if (!next) {
      return;
    }
    setMobileSearchOpen(false);
    navigate(`/search?q=${encodeURIComponent(next)}`);
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="container topbar__stack">
          <div className="topbar__inner">
            <div className="brand-block">
              <NavLink className="brand" to="/">
                武大树洞
              </NavLink>
              <p className="brand-copy">把武大的日常、心事与校友故事，安静地留在这里。</p>
            </div>

            <nav className="desktop-nav" aria-label="主导航">
              {navItems.map((item) => (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={({ isActive }) => `nav-link${isActive ? ' is-active' : ''}`}
                >
                  {item.label}
                </NavLink>
              ))}
              {canAccessAdmin ? (
                <NavLink to="/admin" className={({ isActive }) => `nav-link${isActive ? ' is-active' : ''}`}>
                  管理台
                </NavLink>
              ) : null}
            </nav>

            <div className="topbar__actions">
              <label className="search-bar search-bar--desktop">
                <Icon name="search" className="icon" />
                <input
                  type="search"
                  value={searchKeyword}
                  placeholder="搜索树洞里的关键词..."
                  onChange={(event) => setSearchKeyword(event.target.value)}
                  onKeyDown={(event) => {
                    if (event.key === 'Enter') {
                      submitSearch();
                    }
                  }}
                />
              </label>
              <button
                className="ghost-button topbar__mobile-search-toggle"
                type="button"
                aria-label="打开搜索"
                aria-expanded={mobileSearchOpen}
                onClick={() => setMobileSearchOpen((current) => !current)}
              >
                <Icon name={mobileSearchOpen ? 'close' : 'search'} className="icon" />
              </button>
              <button
                className="ghost-button"
                type="button"
                aria-label="通知"
                onClick={() => navigate('/profile?tab=messages')}
              >
                <Icon name="bell" className="icon" />
                {notificationSummary.totalUnread > 0 ? (
                  <span className="topbar-badge">{notificationSummary.totalUnread}</span>
                ) : null}
              </button>
              <button className="primary-button" type="button" onClick={() => setOpenComposer(true)}>
                <Icon name="plus" className="icon" />
                <span className="topbar__primary-action-text">发布</span>
              </button>
              <button
                className="secondary-button secondary-button--compact topbar__logout"
                type="button"
                onClick={() => void logout()}
              >
                退出
              </button>
              <NavLink className="profile-chip" to="/profile">
                <img src={profile.avatar} alt={profile.name} />
                <span>{user?.name ?? profile.name}</span>
              </NavLink>
            </div>
          </div>

          {mobileSearchOpen ? (
            <div className="topbar__mobile-search">
              <label className="search-bar search-bar--wide search-bar--mobile">
                <Icon name="search" className="icon" />
                <input
                  type="search"
                  aria-label="移动端搜索关键词"
                  value={searchKeyword}
                  placeholder="搜索树洞里的关键词..."
                  onChange={(event) => setSearchKeyword(event.target.value)}
                  onKeyDown={(event) => {
                    if (event.key === 'Enter') {
                      submitSearch();
                    }
                  }}
                />
              </label>
              <button className="secondary-button secondary-button--compact" type="button" onClick={submitSearch}>
                搜索
              </button>
            </div>
          ) : null}
        </div>
      </header>

      <main className="main-shell">
        <Outlet />
      </main>

      <footer className="site-footer">
        <div className="container site-footer__inner">
          <div>
            <p className="site-footer__title">武大树洞</p>
            <p className="site-footer__copy">一个把校园生活、成长情绪与校友记忆连成一张网的树洞式社区。</p>
          </div>
          <div className="site-footer__links">
            <a href="#rules">使用守则</a>
            <a href="#feedback">反馈建议</a>
            <a href="#about">关于项目</a>
          </div>
        </div>
      </footer>

      <nav className="mobile-nav" aria-label="移动导航">
        {mobileNavItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => `mobile-nav__item${isActive ? ' is-active' : ''}`}
          >
            <Icon
              name={
                item.path === '/admin'
                  ? 'spark'
                  : navItems.find((navItem) => navItem.path === item.path)?.icon ?? 'profile'
              }
              className="icon"
            />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <ComposerModal open={openComposer} onClose={() => setOpenComposer(false)} onSubmit={composePost} />
      <AnnouncementPopupModal
        open={Boolean(popupAnnouncement)}
        title={popupAnnouncement?.title ?? ''}
        content={popupAnnouncement?.content ?? ''}
        onClose={closePopupAnnouncement}
      />
    </div>
  );
}
