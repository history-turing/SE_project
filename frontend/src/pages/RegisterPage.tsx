import { useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthContext } from '../context/AuthContext';
import { ApiError, sendEmailCode } from '../services/api';

const SEND_COOLDOWN_SECONDS = 60;

export function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuthContext();
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [info, setInfo] = useState('');
  const [sendingCode, setSendingCode] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [countdown, setCountdown] = useState(0);

  useEffect(() => {
    if (!countdown) {
      return undefined;
    }

    const timer = window.setTimeout(() => setCountdown((value) => Math.max(0, value - 1)), 1000);
    return () => window.clearTimeout(timer);
  }, [countdown]);

  async function handleSendCode() {
    setError('');
    setInfo('');
    setSendingCode(true);

    try {
      await sendEmailCode(email.trim());
      setInfo('验证码已发送，请到武大教育邮箱查收。');
      setCountdown(SEND_COOLDOWN_SECONDS);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : '验证码发送失败，请稍后再试';
      setError(message);
    } finally {
      setSendingCode(false);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setInfo('');
    setSubmitting(true);

    try {
      await register({
        email: email.trim(),
        code: code.trim(),
        username: username.trim(),
        password,
      });
      navigate('/', { replace: true });
    } catch (err) {
      const message = err instanceof ApiError ? err.message : '注册失败，请稍后再试';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-layout">
        <section className="auth-card">
          <p className="eyebrow">账号注册</p>
          <h2>创建你的树洞账号</h2>
          <p className="auth-copy">注册成功后，系统会自动登录并进入树洞首页。</p>

          <form className="auth-form" onSubmit={handleSubmit}>
            <label className="field">
              <span>武大教育邮箱</span>
              <div className="auth-inline-field">
                <input
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  placeholder="例如：2024xxxx@whu.edu.cn"
                  autoComplete="email"
                />
                <button
                  className="secondary-button"
                  type="button"
                  disabled={sendingCode || countdown > 0 || !email.trim()}
                  onClick={() => void handleSendCode()}
                >
                  {countdown > 0 ? `${countdown}s` : sendingCode ? '发送中...' : '发送验证码'}
                </button>
              </div>
            </label>

            <label className="field">
              <span>邮箱验证码</span>
              <input
                value={code}
                onChange={(event) => setCode(event.target.value)}
                placeholder="请输入 6 位验证码"
                inputMode="numeric"
              />
            </label>

            <label className="field">
              <span>用户名</span>
              <input
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                placeholder="3-20 位字母、数字或下划线"
                autoComplete="username"
              />
            </label>

            <label className="field">
              <span>密码</span>
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="至少 8 位密码"
                autoComplete="new-password"
              />
            </label>

            {info ? <p className="auth-info">{info}</p> : null}
            {error ? <p className="auth-error">{error}</p> : null}

            <button className="primary-button auth-submit" type="submit" disabled={submitting}>
              {submitting ? '注册中...' : '完成注册'}
            </button>
          </form>

          <p className="auth-switch">
            已有账号？<Link to="/login">去登录</Link>
          </p>
        </section>

        <section className="auth-side auth-side--rose">
          <p className="eyebrow">武大教育邮箱认证</p>
          <h1>首次注册必须先通过邮箱验证码。</h1>
          <p className="auth-copy">
            使用 @whu.edu.cn 教育邮箱获取验证码。验证通过后，再设置用户名和密码，后续登录就不需要再收验证码。
          </p>
          <div className="auth-note-card">
            <strong>注册流程</strong>
            <span>1. 填写武大邮箱并发送验证码</span>
            <span>2. 输入收到的 6 位验证码</span>
            <span>3. 设置用户名和密码并完成注册</span>
          </div>
        </section>
      </div>
    </div>
  );
}
