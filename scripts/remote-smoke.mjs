import { mkdir } from "node:fs/promises";
import path from "node:path";
import process from "node:process";
import { pathToFileURL } from "node:url";
import {
  parseCliArgs,
  pickAnonymousPost,
  validateAnonymousConversation,
  validateUnreadFlow,
} from "./remote-smoke-lib.mjs";

const AUTH_TOKEN_STORAGE_KEY = "whu-treehole-token";

function getConfig() {
  return {
    admin: {
      username: process.env.TREEHOLE_SMOKE_ADMIN_USERNAME ?? "xiewei",
      password: process.env.TREEHOLE_SMOKE_ADMIN_PASSWORD ?? "xiewei123",
    },
    superAdmin: {
      username: process.env.TREEHOLE_SMOKE_SUPER_USERNAME ?? "codex-super",
      password: process.env.TREEHOLE_SMOKE_SUPER_PASSWORD ?? "codex123",
    },
    normalUser: {
      username: process.env.TREEHOLE_SMOKE_USER_USERNAME ?? "codex-user",
      password: process.env.TREEHOLE_SMOKE_USER_PASSWORD ?? "codex123",
    },
    outputDir: process.env.TREEHOLE_SMOKE_OUTPUT_DIR ?? ".smoke-artifacts",
  };
}

async function requestJson(baseUrl, route, init = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(init.headers ?? {}),
  };
  const response = await fetch(`${baseUrl}${route}`, {
    ...init,
    headers,
  });
  const rawBody = await response.text();
  let payload = null;
  if (rawBody.trim()) {
    try {
      payload = JSON.parse(rawBody);
    } catch (error) {
      throw new Error(`Failed to parse JSON from ${route}: ${rawBody}`);
    }
  }

  if (!response.ok) {
    const message = payload?.message ?? `${response.status} ${response.statusText}`;
    throw new Error(`Request failed for ${route}: ${message}`);
  }

  if (!payload || payload.code !== 0) {
    throw new Error(`Business error for ${route}: ${payload?.message ?? "unknown error"}`);
  }

  return payload.data;
}

async function login(baseUrl, account) {
  return requestJson(baseUrl, "/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify({
      username: account.username,
      password: account.password,
    }),
  });
}

async function fetchHomePage(baseUrl, token) {
  return requestJson(baseUrl, "/api/v1/pages/home", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

async function fetchConversationList(baseUrl, token) {
  return requestJson(baseUrl, "/api/v1/dm/conversations", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

async function fetchConversationDetail(baseUrl, token, conversationCode) {
  return requestJson(baseUrl, `/api/v1/dm/conversations/${conversationCode}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

async function createConversation(baseUrl, token, payload) {
  return requestJson(baseUrl, "/api/v1/dm/conversations/direct", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(payload),
  });
}

async function sendMessage(baseUrl, token, conversationCode, content) {
  return requestJson(baseUrl, `/api/v1/dm/conversations/${conversationCode}/messages`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      clientMessageId: `remote-smoke-${Date.now()}`,
      content,
    }),
  });
}

async function markConversationRead(baseUrl, token, conversationCode) {
  return requestJson(baseUrl, `/api/v1/dm/conversations/${conversationCode}/read`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

async function runAnonymousConversationSmoke(baseUrl, sessions) {
  const homePage = await fetchHomePage(baseUrl, sessions.admin.token);
  const anonymousPost = pickAnonymousPost(homePage.posts);
  const conversationCode = await createConversation(baseUrl, sessions.admin.token, {
    peerUserCode: anonymousPost.authorUserCode,
    sourcePostCode: anonymousPost.id,
    anonymousEntry: true,
  });
  const list = await fetchConversationList(baseUrl, sessions.admin.token);
  const detail = await fetchConversationDetail(baseUrl, sessions.admin.token, conversationCode);
  const conversationListItem = list.find((item) => item.conversationCode === conversationCode);

  return {
    selectedPost: anonymousPost,
    conversationCode,
    conversationListItem,
    conversationDetail: detail,
    validation: validateAnonymousConversation({
      conversationCode,
      conversationListItem,
      conversationDetail: detail,
    }),
  };
}

async function runUnreadFlowSmoke(baseUrl, sessions, options = {}) {
  const conversationCode = await createConversation(baseUrl, sessions.superAdmin.token, {
    peerUserCode: sessions.normalUser.user.userCode,
    anonymousEntry: false,
  });
  const content = options.messageText ?? `remote smoke message ${Date.now()}`;
  const sentMessage = await sendMessage(baseUrl, sessions.superAdmin.token, conversationCode, content);
  const recipientBeforeReadList = await fetchConversationList(baseUrl, sessions.normalUser.token);
  const recipientBeforeRead = recipientBeforeReadList.find(
    (item) => item.conversationCode === conversationCode,
  );
  const recipientDetailBeforeRead = await fetchConversationDetail(
    baseUrl,
    sessions.normalUser.token,
    conversationCode,
  );

  let recipientAfterRead = null;
  if (options.markRead !== false) {
    await markConversationRead(baseUrl, sessions.normalUser.token, conversationCode);
    const recipientAfterReadList = await fetchConversationList(baseUrl, sessions.normalUser.token);
    recipientAfterRead = recipientAfterReadList.find(
      (item) => item.conversationCode === conversationCode,
    );
  }

  const validation =
    options.markRead === false
      ? null
      : validateUnreadFlow({
          conversationCode,
          sentMessage,
          recipientBeforeRead,
          recipientDetailBeforeRead,
          recipientAfterRead,
        });

  return {
    conversationCode,
    sentMessage,
    recipientBeforeRead,
    recipientDetailBeforeRead,
    recipientAfterRead,
    validation,
  };
}

async function ensurePlaywright() {
  try {
    const playwrightEntry = path.resolve(
      process.cwd(),
      "frontend/node_modules/playwright/index.mjs",
    );
    return await import(pathToFileURL(playwrightEntry).href);
  } catch (error) {
    throw new Error(
      "Browser smoke requires Playwright. Run `npm --prefix frontend install -D playwright` and `npx playwright install chromium` first.",
    );
  }
}

async function dismissAnnouncementPopup(page) {
  const modal = page.locator(".announcement-popup");
  if ((await modal.count()) === 0) {
    return;
  }
  const closeButton = modal.locator("button").first();
  if ((await closeButton.count()) === 0) {
    return;
  }
  await closeButton.click().catch(() => {});
}

async function createAuthenticatedPage(browser, baseUrl, token) {
  const page = await browser.newPage();
  await page.addInitScript(
    ([storageKey, storageValue]) => {
      window.localStorage.setItem(storageKey, storageValue);
    },
    [AUTH_TOKEN_STORAGE_KEY, token],
  );
  await page.goto(baseUrl, { waitUntil: "networkidle" });
  await dismissAnnouncementPopup(page);
  return page;
}

async function runBrowserSmoke(baseUrl, options) {
  const config = getConfig();
  const sessions = await bootstrapSessions(baseUrl, config);
  const anonymous = await runAnonymousConversationSmoke(baseUrl, sessions);
  const unread = await runUnreadFlowSmoke(baseUrl, sessions, { markRead: false });

  const playwright = await ensurePlaywright();
  const browser = await playwright.chromium.launch({
    headless: options.headless,
  });

  try {
    await mkdir(config.outputDir, { recursive: true });

    const adminPage = await createAuthenticatedPage(browser, baseUrl, sessions.admin.token);
    await adminPage.goto(
      `${baseUrl}/profile?tab=messages&conversation=${encodeURIComponent(anonymous.conversationCode)}`,
      { waitUntil: "networkidle" },
    );
    await dismissAnnouncementPopup(adminPage);
    await adminPage.waitForSelector(".message-panel h2");
    const anonymousHeader = (await adminPage.locator(".message-panel h2").textContent())?.trim() ?? "";
    if (anonymousHeader !== anonymous.validation.peerName) {
      throw new Error(`Anonymous browser smoke expected header ${anonymous.validation.peerName} but got ${anonymousHeader}`);
    }
    const adminContent = await adminPage.content();
    if (adminContent.includes(anonymous.selectedPost.authorUserCode)) {
      throw new Error("Anonymous browser smoke detected a leaked real user code in the rendered page.");
    }
    const anonymousScreenshot = path.join(config.outputDir, "remote-anonymous-conversation.png");
    await adminPage.screenshot({ path: anonymousScreenshot, fullPage: true });
    await adminPage.close();

    const userPage = await createAuthenticatedPage(browser, baseUrl, sessions.normalUser.token);
    await userPage.waitForSelector(".topbar__actions");
    const topbarBadge = userPage.locator(".topbar-badge");
    await topbarBadge.waitFor({ state: "visible" });
    const unreadBadgeBeforeOpen = (await topbarBadge.textContent())?.trim() ?? "";
    if (!unreadBadgeBeforeOpen) {
      throw new Error("Browser smoke did not find a visible unread badge before opening the conversation.");
    }
    await userPage.goto(
      `${baseUrl}/profile?tab=messages&conversation=${encodeURIComponent(unread.conversationCode)}`,
      { waitUntil: "networkidle" },
    );
    await dismissAnnouncementPopup(userPage);
    await userPage.waitForSelector(".message-thread .bubble");
    const directHeader = (await userPage.locator(".message-panel h2").textContent())?.trim() ?? "";
    const renderedMessage = (await userPage.locator(".message-thread .bubble p").last().textContent())?.trim() ?? "";
    if (directHeader !== unread.recipientBeforeRead.peerName) {
      throw new Error(`Browser smoke expected direct conversation header ${unread.recipientBeforeRead.peerName} but got ${directHeader}`);
    }
    if (renderedMessage !== unread.sentMessage.text) {
      throw new Error(`Browser smoke expected message text ${unread.sentMessage.text} but got ${renderedMessage}`);
    }
    const unreadBadgeAfterOpen = await topbarBadge.count() === 0
      ? ""
      : ((await topbarBadge.textContent())?.trim() ?? "");
    const directScreenshot = path.join(config.outputDir, "remote-direct-conversation.png");
    await userPage.screenshot({ path: directScreenshot, fullPage: true });
    await userPage.close();

    await markConversationRead(baseUrl, sessions.normalUser.token, unread.conversationCode);

    return {
      anonymousConversationCode: anonymous.conversationCode,
      unreadConversationCode: unread.conversationCode,
      unreadBadgeBeforeOpen,
      unreadBadgeAfterOpen,
      anonymousScreenshot,
      directScreenshot,
    };
  } finally {
    await browser.close();
  }
}

async function bootstrapSessions(baseUrl, config) {
  const admin = await login(baseUrl, config.admin);
  const superAdmin = await login(baseUrl, config.superAdmin);
  const normalUser = await login(baseUrl, config.normalUser);

  return {
    admin,
    superAdmin,
    normalUser,
  };
}

async function runApiSmoke(baseUrl) {
  const config = getConfig();
  const sessions = await bootstrapSessions(baseUrl, config);
  const anonymousConversation = await runAnonymousConversationSmoke(baseUrl, sessions);
  const unreadFlow = await runUnreadFlowSmoke(baseUrl, sessions);

  return {
    baseUrl,
    accounts: {
      admin: sessions.admin.user.username,
      superAdmin: sessions.superAdmin.user.username,
      normalUser: sessions.normalUser.user.username,
    },
    anonymousConversation,
    unreadFlow,
  };
}

async function main() {
  const options = parseCliArgs(process.argv.slice(2));

  if (options.mode === "api") {
    console.log(JSON.stringify(await runApiSmoke(options.baseUrl), null, 2));
    return;
  }

  if (options.mode === "browser") {
    console.log(JSON.stringify(await runBrowserSmoke(options.baseUrl, options), null, 2));
    return;
  }

  const apiResult = await runApiSmoke(options.baseUrl);
  const browserResult = await runBrowserSmoke(options.baseUrl, options);
  console.log(
    JSON.stringify(
      {
        api: apiResult,
        browser: browserResult,
      },
      null,
      2,
    ),
  );
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : error);
  process.exitCode = 1;
});
