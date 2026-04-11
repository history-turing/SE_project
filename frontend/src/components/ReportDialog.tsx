import { useEffect, useState } from 'react';
import { createReport } from '../services/api';
import { Icon } from './Icon';

interface ReportDialogProps {
  open: boolean;
  targetType: 'POST' | 'COMMENT' | 'USER';
  targetCode: string;
  onClose: () => void;
  onSubmitted?: () => void;
}

const REPORT_REASONS = [
  { value: 'SPAM', label: '垃圾广告' },
  { value: 'HARASSMENT', label: '辱骂攻击' },
  { value: 'RUMOR', label: '造谣不实' },
  { value: 'PRIVACY', label: '侵犯隐私' },
  { value: 'OTHER', label: '其他原因' },
];

export function ReportDialog({ open, targetType, targetCode, onClose, onSubmitted }: ReportDialogProps) {
  const [reasonCode, setReasonCode] = useState(REPORT_REASONS[0].value);
  const [reasonDetail, setReasonDetail] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!open) {
      setReasonCode(REPORT_REASONS[0].value);
      setReasonDetail('');
      setError('');
      setSubmitting(false);
    }
  }, [open]);

  if (!open) {
    return null;
  }

  async function handleSubmit() {
    if (submitting || !targetCode) {
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      await createReport({
        targetType,
        targetCode,
        reasonCode,
        reasonDetail: reasonDetail.trim(),
      });
      onSubmitted?.();
      onClose();
    } catch (submitError) {
      console.error('提交举报失败。', submitError);
      setError('举报提交失败，请稍后重试。');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="modal-backdrop" role="presentation" onClick={onClose}>
      <section
        className="composer-modal report-dialog"
        role="dialog"
        aria-modal="true"
        aria-label="举报内容"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="composer-modal__header">
          <div>
            <p className="eyebrow">内容治理</p>
            <h2>提交举报</h2>
          </div>
          <button className="ghost-button" type="button" onClick={onClose}>
            <Icon name="close" className="icon" />
          </button>
        </div>

        <label className="field">
          <span>举报原因</span>
          <select aria-label="举报原因" value={reasonCode} onChange={(event) => setReasonCode(event.target.value)}>
            {REPORT_REASONS.map((reason) => (
              <option key={reason.value} value={reason.value}>
                {reason.label}
              </option>
            ))}
          </select>
        </label>

        <label className="field">
          <span>补充说明</span>
          <textarea
            aria-label="补充说明"
            rows={4}
            value={reasonDetail}
            onChange={(event) => setReasonDetail(event.target.value)}
            placeholder="补充上下文，便于管理员快速处理。"
          />
        </label>

        {error ? <p className="auth-error">{error}</p> : null}

        <div className="composer-modal__footer">
          <button className="secondary-button" type="button" onClick={onClose}>
            取消
          </button>
          <button className="primary-button" type="button" disabled={submitting} onClick={() => void handleSubmit()}>
            {submitting ? '提交中...' : '提交举报'}
          </button>
        </div>
      </section>
    </div>
  );
}
