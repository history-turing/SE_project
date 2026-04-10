import type { ReactElement } from 'react';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

export function renderWithProviders(ui: ReactElement, options?: { route?: string }) {
  return render(
    <MemoryRouter initialEntries={[options?.route ?? '/']}>
      {ui}
    </MemoryRouter>,
  );
}
