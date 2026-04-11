import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { createReport } from '../services/api';
import { renderWithProviders } from '../test/renderWithProviders';
import { ReportDialog } from './ReportDialog';

vi.mock('../services/api', () => ({
  createReport: vi.fn(),
}));

test('submits report payload and closes dialog', async () => {
  const user = userEvent.setup();
  const onClose = vi.fn();

  vi.mocked(createReport).mockResolvedValue({
    reportCode: 'report-1',
    targetType: 'POST',
    targetCode: 'home-1',
    reasonCode: 'SPAM',
    reasonDetail: '广告',
    status: 'OPEN',
    resolutionCode: null,
    resolutionNote: null,
    createdAt: '2026-04-12T10:00:00',
  });

  renderWithProviders(
    <ReportDialog open targetType="POST" targetCode="home-1" onClose={onClose} />,
  );

  await user.selectOptions(screen.getByLabelText('举报原因'), 'SPAM');
  await user.type(screen.getByLabelText('补充说明'), '广告');
  await user.click(screen.getByRole('button', { name: '提交举报' }));

  expect(createReport).toHaveBeenCalledWith({
    targetType: 'POST',
    targetCode: 'home-1',
    reasonCode: 'SPAM',
    reasonDetail: '广告',
  });
  expect(onClose).toHaveBeenCalled();
});
