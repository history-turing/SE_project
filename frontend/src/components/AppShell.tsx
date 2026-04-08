import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAppContext } from '../context/AppContext';
import { useAuthContext } from '../context/AuthContext';
import { navItems } from '../data/siteData';
import { ComposerModal } from './ComposerModal';
import { Icon } from './Icon';

export function AppShell() {
  const { composePost, profile } = useAppContext();
  const { logout, user } = useAuthContext();
  const [openComposer, setOpenComposer] = useState(false);

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="container topbar__inner">
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
          </nav>

          <div className="topbar__actions">
            <label className="search-bar">
              <Icon name="search" className="icon" />
              <input placeholder="搜索树洞里的关键词..." />
            </label>
            <button className="ghost-button" type="button" aria-label="通知">
              <Icon name="bell" className="icon" />
            </button>
            <button className="primary-button" type="button" onClick={() => setOpenComposer(true)}>
              <Icon name="plus" className="icon" />
              发布
            </button>
            <button className="secondary-button secondary-button--compact" type="button" onClick={() => void logout()}>
              退出
            </button>
            <NavLink className="profile-chip" to="/profile">
              <img src={profile.avatar} alt={profile.name} />
              <span>{user?.name ?? profile.name}</span>
            </NavLink>
          </div>
        </div>
      </header>

      <main className="main-shell">
        <Outlet />
      </main>

      <footer className="site-footer">
        <div className="container site-footer__inner">
          <div>
            <p className="site-footer__title">武大树洞</p>
            <p className="site-footer__copy">
              一个把校园生活、成长烦恼与校友记忆连成一张网的树洞式社区。
            </p>
          </div>
          <div className="site-footer__links">
            <a href="#rules">使用守则</a>
            <a href="#feedback">反馈建议</a>
            <a href="#about">关于项目</a>
          </div>
        </div>
      </footer>

      <nav className="mobile-nav" aria-label="移动导航">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => `mobile-nav__item${isActive ? ' is-active' : ''}`}
          >
            <Icon name={item.icon} className="icon" />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <ComposerModal
        open={openComposer}
        onClose={() => setOpenComposer(false)}
        onSubmit={composePost}
      />
    </div>
  );
}
