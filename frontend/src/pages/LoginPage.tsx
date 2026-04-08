import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthContext } from '../context/AuthContext';
import { ApiError } from '../services/api';

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuthContext();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      await login(username.trim(), password);
      navigate('/', { replace: true });
    } catch (err) {
      const message = err instanceof ApiError ? err.message : '登录失败，请稍后再试';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-layout">
        <section className="auth-side">
          <p className="eyebrow">Whu Treehole</p>
          <h1>先完成登录，再进入武大树洞。</h1>
          <p className="auth-copy">
            首次使用需要用武大教育邮箱完成注册。注册成功后，后续只需用户名和密码即可登录。
          </p>
          <ul className="auth-feature-list">
            <li>仅允许 @whu.edu.cn 教育邮箱注册</li>
            <li>验证码校验通过后才能创建账号</li>
            <li>帖子、收藏和互动都会绑定到你的真实账号</li>
          </ul>
        </section>

        <section className="auth-card">
          <p className="eyebrow">账号登录</p>
          <h2>欢迎回来</h2>
          <p className="auth-copy">输入用户名和密码，继续使用你的武大树洞账号。</p>

          <form className="auth-form" onSubmit={handleSubmit}>
            <label className="field">
              <span>用户名</span>
              <input
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                placeholder="例如：whu_2026"
                autoComplete="username"
              />
            </label>

            <label className="field">
              <span>密码</span>
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="请输入登录密码"
                autoComplete="current-password"
              />
            </label>

            {error ? <p className="auth-error">{error}</p> : null}

            <button className="primary-button auth-submit" type="submit" disabled={submitting}>
              {submitting ? '登录中...' : '登录'}
            </button>
          </form>

          <p className="auth-switch">
            还没有账号？<Link to="/register">去注册</Link>
          </p>
        </section>
      </div>
    </div>
  );
}
