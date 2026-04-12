import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ConversationLauncherButton } from '../components/ConversationLauncherButton';
import { getUserProfile } from '../services/api';
import type { UserProfile } from '../types';

export function UserProfilePage() {
  const { userCode = '' } = useParams();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;

    async function loadProfile() {
      if (!userCode) {
        setError('用户不存在');
        setLoading(false);
        return;
      }

      setLoading(true);
      setError('');
      try {
        const nextProfile = await getUserProfile(userCode);
        if (!cancelled) {
          setProfile(nextProfile);
        }
      } catch (loadError) {
        console.error('加载用户主页失败。', loadError);
        if (!cancelled) {
          setError('用户主页暂时不可用，请稍后重试。');
          setProfile(null);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void loadProfile();

    return () => {
      cancelled = true;
    };
  }, [userCode]);

  if (loading) {
    return (
      <div className="container page-stack">
        <section className="surface-card empty-card">
          <h1>正在加载用户主页</h1>
          <p>请稍候，系统正在同步这位同学的公开资料。</p>
        </section>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="container page-stack">
        <section className="surface-card empty-card">
          <h1>用户主页不可用</h1>
          <p>{error || '未找到对应的用户资料。'}</p>
          <Link className="secondary-button" to="/">
            返回首页
          </Link>
        </section>
      </div>
    );
  }

  return (
    <div className="container page-stack">
      <section className="profile-hero profile-hero--public">
        <div className="profile-hero__info">
          <img className="profile-hero__avatar" src={profile.avatar} alt={profile.name} />
          <div>
            <p className="eyebrow">用户主页</p>
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

        <div className="profile-hero__aside">
          <p className="profile-hero__tagline">{profile.tagline}</p>
          <ConversationLauncherButton peerUserCode={userCode} className="primary-button" />
        </div>
      </section>

      {profile.stats.length ? (
        <div className="stat-grid">
          {profile.stats.map((item) => (
            <article key={item.label} className="stat-card">
              <span>{item.label}</span>
              <strong>{item.value}</strong>
            </article>
          ))}
        </div>
      ) : null}
    </div>
  );
}
