function fail(message) {
  throw new Error(message);
}

export function parseCliArgs(args) {
  const parsed = {
    mode: "api",
    baseUrl: process.env.TREEHOLE_SMOKE_BASE_URL ?? "http://43.134.116.122",
    headless: process.env.TREEHOLE_SMOKE_HEADLESS !== "false",
  };

  for (const arg of args) {
    if (!arg.startsWith("--")) {
      parsed.mode = arg;
      continue;
    }
    if (arg.startsWith("--base-url=")) {
      parsed.baseUrl = arg.slice("--base-url=".length);
      continue;
    }
    if (arg === "--headed") {
      parsed.headless = false;
      continue;
    }
    if (arg === "--headless") {
      parsed.headless = true;
      continue;
    }
    fail(`Unsupported argument: ${arg}`);
  }

  if (!["api", "browser", "all"].includes(parsed.mode)) {
    fail(`Unsupported smoke mode: ${parsed.mode}`);
  }

  return parsed;
}

export function pickAnonymousPost(posts) {
  const normalizedPosts = Array.isArray(posts) ? posts : [];
  const candidate =
    normalizedPosts.find((post) => post?.anonymous && post?.authorUserCode && post?.mine === false) ??
    normalizedPosts.find((post) => post?.anonymous && post?.authorUserCode);

  if (!candidate) {
    fail("No anonymous post with an author user code is available for smoke validation.");
  }

  return candidate;
}

export function validateAnonymousConversation(result) {
  if (!result?.conversationCode) {
    fail("Anonymous conversation smoke did not return a conversation code.");
  }

  const item = result.conversationListItem;
  const detail = result.conversationDetail;
  if (!item || !detail) {
    fail("Anonymous conversation smoke is missing list or detail payload.");
  }

  if (item.conversationType !== "ANONYMOUS_POST" || detail.conversationType !== "ANONYMOUS_POST") {
    fail("Anonymous conversation type was not anonymized as ANONYMOUS_POST.");
  }

  if (item.peerName !== "匿名树洞作者" || detail.peer?.name !== "匿名树洞作者") {
    fail("Anonymous conversation exposed an unexpected peer name.");
  }

  if (item.peerSubtitle !== "匿名私信会话" || detail.peer?.subtitle !== "匿名私信会话") {
    fail("Anonymous conversation exposed an unexpected peer subtitle.");
  }

  if (detail.peer?.userCode !== null) {
    fail("Anonymous conversation detail leaked a real peer userCode.");
  }

  if (item.peerAvatarUrl !== "/images/avatar-anonymous.svg" || detail.peer?.avatarUrl !== "/images/avatar-anonymous.svg") {
    fail("Anonymous conversation did not use the anonymous avatar contract.");
  }

  return {
    conversationCode: result.conversationCode,
    conversationType: detail.conversationType,
    peerName: detail.peer.name,
  };
}

export function validateAnonymousBrowserSurface(result) {
  const authorUserCode = result?.authorUserCode ?? "";
  const selectedConversationHtml = result?.selectedConversationHtml ?? "";
  const messagePanelHtml = result?.messagePanelHtml ?? "";

  if (!authorUserCode) {
    fail("Anonymous browser smoke did not receive the author user code.");
  }

  if (
    selectedConversationHtml.includes(authorUserCode) ||
    selectedConversationHtml.includes(`/users/${authorUserCode}`)
  ) {
    fail("Anonymous browser smoke detected a leaked real user code in the selected conversation item.");
  }

  if (
    messagePanelHtml.includes(authorUserCode) ||
    messagePanelHtml.includes(`/users/${authorUserCode}`)
  ) {
    fail("Anonymous browser smoke detected a leaked real user code in the message panel.");
  }

  return {
    authorUserCode,
    surfacesChecked: ["selectedConversation", "messagePanel"],
  };
}

export function validateUnreadFlow(result) {
  if (!result?.conversationCode) {
    fail("Unread smoke did not return a conversation code.");
  }
  if (!result.sentMessage?.text) {
    fail("Unread smoke did not return the sent message payload.");
  }
  if (!result.recipientBeforeRead || !result.recipientDetailBeforeRead || !result.recipientAfterRead) {
    fail("Unread smoke is missing recipient list/detail payloads.");
  }

  const beforeUnread = Number(result.recipientBeforeRead.unreadCount ?? -1);
  const detailUnread = Number(result.recipientDetailBeforeRead.unreadCount ?? -1);
  const afterUnread = Number(result.recipientAfterRead.unreadCount ?? -1);
  const detailMessage = result.recipientDetailBeforeRead.messages?.at(-1);

  if (beforeUnread < 1 || detailUnread < 1) {
    fail("Unread smoke did not observe unread count growth before read.");
  }
  if (detailMessage?.text !== result.sentMessage.text || detailMessage?.sender !== "them") {
    fail("Unread smoke did not observe the expected inbound message content.");
  }
  if (afterUnread !== 0) {
    fail("Unread smoke did not clear unread count after marking the conversation read.");
  }

  return {
    conversationCode: result.conversationCode,
    unreadBeforeRead: beforeUnread,
    unreadAfterRead: afterUnread,
    messageText: result.sentMessage.text,
  };
}
