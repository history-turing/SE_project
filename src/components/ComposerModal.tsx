import { useState } from 'react';
import { publishTopics } from '../data/siteData';
import type { Audience } from '../types';
import { Icon } from './Icon';

interface ComposerModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (payload: {
    title: string;
    content: string;
    topic: string;
    audience: Audience;
    anonymous: boolean;
  }) => boolean;
}

export function ComposerModal({ open, onClose, onSubmit }: ComposerModalProps) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [topic, setTopic] = useState(publishTopics[0]);
  const [audience, setAudience] = useState<Audience>('首页');
  const [anonymous, setAnonymous] = useState(true);

  if (!open) {
    return null;
  }

  function handleSubmit() {
    const ok = onSubmit({ title, content, topic, audience, anonymous });

    if (!ok) {
      return;
    }

    setTitle('');
    setContent('');
    setTopic(publishTopics[0]);
    setAudience('首页');
    setAnonymous(true);
    onClose();
  }

  return (
    <div className="modal-backdrop" role="presentation" onClick={onClose}>
      <section
        className="composer-modal"
        role="dialog"
        aria-modal="true"
        aria-label="发布树洞"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="composer-modal__header">
          <div>
            <p className="eyebrow">发布树洞</p>
            <h2>写下此刻想说的话</h2>
          </div>
          <button className="ghost-button" type="button" onClick={onClose}>
            <Icon name="close" className="icon" />
          </button>
        </div>

        <div className="field-grid">
          <label className="field">
            <span>发布到</span>
            <select value={audience} onChange={(event) => setAudience(event.target.value as Audience)}>
              <option value="首页">首页树洞</option>
              <option value="校友圈">校友圈</option>
            </select>
          </label>

          <label className="field">
            <span>话题</span>
            <select value={topic} onChange={(event) => setTopic(event.target.value)}>
              {publishTopics.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>
        </div>

        <label className="field">
          <span>标题</span>
          <input
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            placeholder="给这条树洞起一个名字（可选）"
          />
        </label>

        <label className="field">
          <span>正文</span>
          <textarea
            value={content}
            onChange={(event) => setContent(event.target.value)}
            placeholder="今天你想把什么留在武大树洞？"
            rows={6}
          />
        </label>

        <label className="checkbox-field">
          <input
            checked={anonymous}
            type="checkbox"
            onChange={(event) => setAnonymous(event.target.checked)}
          />
          <span>匿名发布，保留树洞感</span>
        </label>

        <div className="composer-modal__footer">
          <button className="secondary-button" type="button" onClick={onClose}>
            取消
          </button>
          <button className="primary-button" type="button" onClick={handleSubmit}>
            发布
          </button>
        </div>
      </section>
    </div>
  );
}
