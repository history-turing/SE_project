import test from "node:test";
import assert from "node:assert/strict";

import {
  parseCliArgs,
  pickAnonymousPost,
  validateAnonymousBrowserSurface,
  validateAnonymousConversation,
  validateUnreadFlow,
} from "./remote-smoke-lib.mjs";

test("parseCliArgs defaults to api mode and localhost-safe options", () => {
  const parsed = parseCliArgs([]);

  assert.equal(parsed.mode, "api");
  assert.equal(parsed.headless, true);
  assert.equal(parsed.baseUrl, "http://43.134.116.122");
});

test("parseCliArgs supports browser mode and explicit flags", () => {
  const parsed = parseCliArgs([
    "browser",
    "--base-url=http://example.com",
    "--headed",
  ]);

  assert.equal(parsed.mode, "browser");
  assert.equal(parsed.baseUrl, "http://example.com");
  assert.equal(parsed.headless, false);
});

test("pickAnonymousPost chooses an anonymous post with a visible author code", () => {
  const post = pickAnonymousPost([
    { id: "post-1", anonymous: false, authorUserCode: "user-1", mine: false },
    { id: "post-2", anonymous: true, authorUserCode: null, mine: false },
    { id: "post-3", anonymous: true, authorUserCode: "user-3", mine: false },
  ]);

  assert.equal(post.id, "post-3");
});

test("validateAnonymousConversation accepts anonymized peer payloads", () => {
  const result = validateAnonymousConversation({
    conversationCode: "dm-1",
    conversationListItem: {
      conversationType: "ANONYMOUS_POST",
      peerName: "匿名树洞作者",
      peerSubtitle: "匿名私信会话",
      peerAvatarUrl: "/images/avatar-anonymous.svg",
    },
    conversationDetail: {
      conversationType: "ANONYMOUS_POST",
      peer: {
        userCode: null,
        name: "匿名树洞作者",
        subtitle: "匿名私信会话",
        avatarUrl: "/images/avatar-anonymous.svg",
      },
    },
  });

  assert.deepEqual(result, {
    conversationCode: "dm-1",
    conversationType: "ANONYMOUS_POST",
    peerName: "匿名树洞作者",
  });
});

test("validateAnonymousBrowserSurface only checks the active anonymous conversation surfaces", () => {
  const result = validateAnonymousBrowserSurface({
    authorUserCode: "codex-user",
    selectedConversationHtml: "<strong>匿名树洞作者</strong><span>匿名私信会话</span>",
    messagePanelHtml: "<h2>匿名树洞作者</h2><p>匿名私信会话</p>",
  });

  assert.deepEqual(result, {
    authorUserCode: "codex-user",
    surfacesChecked: ["selectedConversation", "messagePanel"],
  });
});

test("validateAnonymousBrowserSurface rejects a leaked real user code in the active surfaces", () => {
  assert.throws(
    () =>
      validateAnonymousBrowserSurface({
        authorUserCode: "codex-user",
        selectedConversationHtml: "<a href=\"/users/codex-user\">Codex User</a>",
        messagePanelHtml: "<h2>匿名树洞作者</h2>",
      }),
    /selected conversation item/,
  );
});

test("validateUnreadFlow enforces unread increment before read and clear after read", () => {
  const result = validateUnreadFlow({
    conversationCode: "dm-2",
    sentMessage: {
      text: "hello",
    },
    recipientBeforeRead: {
      unreadCount: 1,
      lastMessage: "hello",
    },
    recipientDetailBeforeRead: {
      messages: [
        {
          text: "hello",
          sender: "them",
        },
      ],
      unreadCount: 1,
    },
    recipientAfterRead: {
      unreadCount: 0,
    },
  });

  assert.deepEqual(result, {
    conversationCode: "dm-2",
    unreadBeforeRead: 1,
    unreadAfterRead: 0,
    messageText: "hello",
  });
});
