import { ApiError, loginWithPassword } from './api';

describe('loginWithPassword', () => {
  const originalFetch = global.fetch;

  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterAll(() => {
    global.fetch = originalFetch;
  });

  test('wraps network failures as ApiError', async () => {
    global.fetch = vi.fn().mockRejectedValue(new TypeError('Failed to fetch'));

    await expect(loginWithPassword({ username: 'codex-super', password: 'codex123' })).rejects.toMatchObject({
      name: 'ApiError',
      message: '网络异常，请检查连接后重试',
      status: 0,
      code: -1,
    } satisfies Partial<ApiError>);
  });

  test('wraps invalid json success responses as ApiError', async () => {
    global.fetch = vi.fn().mockResolvedValue(
      new Response('<html>gateway</html>', {
        status: 200,
        headers: {
          'Content-Type': 'text/html',
        },
      }),
    );

    await expect(loginWithPassword({ username: 'codex-super', password: 'codex123' })).rejects.toMatchObject({
      name: 'ApiError',
      message: '服务响应异常，请稍后再试',
      status: 200,
      code: -1,
    } satisfies Partial<ApiError>);
  });

  test('wraps invalid json gateway errors as ApiError', async () => {
    global.fetch = vi.fn().mockResolvedValue(
      new Response('Bad Gateway', {
        status: 502,
        statusText: 'Bad Gateway',
        headers: {
          'Content-Type': 'text/plain',
        },
      }),
    );

    await expect(loginWithPassword({ username: 'codex-super', password: 'codex123' })).rejects.toMatchObject({
      name: 'ApiError',
      message: '服务暂时不可用，请稍后再试',
      status: 502,
      code: -1,
    } satisfies Partial<ApiError>);
  });
});
