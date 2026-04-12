import { Icon } from './Icon';

interface AnnouncementPopupModalProps {
  open: boolean;
  title: string;
  content: string;
  onClose: () => void;
}

export function AnnouncementPopupModal({ open, title, content, onClose }: AnnouncementPopupModalProps) {
  if (!open) {
    return null;
  }

  return (
    <div className="modal-backdrop" role="presentation" onClick={onClose}>
      <section
        className="composer-modal announcement-popup"
        role="dialog"
        aria-modal="true"
        aria-label="校园公告弹窗"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="composer-modal__header">
          <div>
            <p className="eyebrow">Campus Notice</p>
            <h2>{title}</h2>
          </div>
          <button className="ghost-button" type="button" onClick={onClose} aria-label="关闭公告弹窗">
            <Icon name="close" className="icon" />
          </button>
        </div>
        <div className="announcement-popup__content">
          {content.split('\n').map((paragraph) => (
            <p key={paragraph}>{paragraph}</p>
          ))}
        </div>
        <div className="composer-modal__footer">
          <button className="primary-button" type="button" onClick={onClose}>
            我知道了
          </button>
        </div>
      </section>
    </div>
  );
}
