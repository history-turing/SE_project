import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { ComposerModal } from './ComposerModal';

test('shows submit error message and keeps modal open when publishing fails', async () => {
  const user = userEvent.setup();
  const onClose = vi.fn();
  const onSubmit = vi.fn().mockResolvedValue({
    ok: false,
    errorMessage: '没有发布权限',
  });

  renderWithProviders(<ComposerModal open onClose={onClose} onSubmit={onSubmit as never} />);

  await user.type(screen.getByPlaceholderText('今天你想把什么留在武大树洞？'), '测试发布失败');
  await user.click(screen.getByRole('button', { name: '发布' }));

  expect(await screen.findByText('没有发布权限')).toBeInTheDocument();
  expect(screen.getByRole('dialog', { name: '发布树洞' })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: '发布' })).toBeEnabled();
  expect(onClose).not.toHaveBeenCalled();
});
