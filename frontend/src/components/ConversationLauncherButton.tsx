import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthContext } from '../context/AuthContext';
import { createDirectConversation } from '../services/api';

interface ConversationLauncherButtonProps {
  peerUserCode: string;
  className?: string;
}

export function ConversationLauncherButton({
  peerUserCode,
  className = 'mini-button',
}: ConversationLauncherButtonProps) {
  const navigate = useNavigate();
  const { user } = useAuthContext();
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  if (!peerUserCode || user?.userCode === peerUserCode) {
    return null;
  }

  async function handleClick() {
    if (loading) {
      return;
    }

    setLoading(true);
    setError('');
    try {
      const result = await createDirectConversation(peerUserCode);
      setSuccess(true);
      navigate(`/profile?tab=messages&conversation=${encodeURIComponent(result.conversationCode)}`);
    } catch (launchError) {
      console.error('创建私信会话失败。', launchError);
      setError('暂时无法进入私信会话，请稍后重试。');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="conversation-launcher">
      <button className={className} type="button" disabled={loading} onClick={() => void handleClick()}>
        {loading ? '发起中...' : '发私信'}
      </button>
      {success ? <span className="conversation-launcher__hint">已进入私信会话</span> : null}
      {error ? <span className="auth-error">{error}</span> : null}
    </div>
  );
}
