interface MessageRecallMenuProps {
  visible: boolean;
  disabled?: boolean;
  onRecall: () => void;
}

export function MessageRecallMenu({ visible, disabled = false, onRecall }: MessageRecallMenuProps) {
  if (!visible) {
    return null;
  }

  return (
    <div className="message-recall-menu">
      <button className="mini-button" type="button" disabled={disabled} onClick={onRecall}>
        撤回消息
      </button>
    </div>
  );
}
