import type { ReactElement } from 'react';
import { render } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

export function renderWithProviders(ui: ReactElement, options?: { route?: string; path?: string }) {
  return render(
    <MemoryRouter initialEntries={[options?.route ?? '/']}>
      {options?.path ? (
        <Routes>
          <Route path={options.path} element={ui} />
        </Routes>
      ) : (
        ui
      )}
    </MemoryRouter>,
  );
}
