# Responsive Web Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the current Web app usable on mobile browsers while preserving desktop usability, full feature coverage, and the existing UI tone.

**Architecture:** Refactor responsiveness from the shell inward. First stabilize shared layout primitives and shell behavior, then update page-level layouts for home, auth, profile/messages, and post/comment surfaces. Keep all behavior in the same routes and components instead of forking a separate mobile app.

**Tech Stack:** React 18, TypeScript, React Router, Vitest, CSS media queries, Vite

---

## File Structure

- Modify: `frontend/src/components/AppShell.tsx`
- Modify: `frontend/src/components/AppShell.test.tsx`
- Modify: `frontend/src/components/PostCard.tsx`
- Modify: `frontend/src/components/PostCard.test.tsx`
- Modify: `frontend/src/pages/HomePage.tsx`
- Modify: `frontend/src/pages/LoginPage.tsx`
- Modify: `frontend/src/pages/RegisterPage.tsx`
- Modify: `frontend/src/pages/ProfilePage.tsx`
- Modify: `frontend/src/pages/ProfilePage.test.tsx`
- Modify: `frontend/src/styles.css`

---

### Task 1: Stabilize Shared Shell Responsiveness

**Files:**
- Modify: `frontend/src/components/AppShell.tsx`
- Modify: `frontend/src/components/AppShell.test.tsx`
- Modify: `frontend/src/styles.css`

- [ ] **Step 1: Write failing shell tests for mobile-friendly navigation surfaces**

Add tests that assert:
- the desktop admin entry still renders only once for admin users
- the unread badge still disappears when `totalUnread` is `0`
- the mobile navigation renders the same primary destinations

- [ ] **Step 2: Run the shell tests to verify at least one new assertion fails**

Run: `npm --prefix frontend test -- src/components/AppShell.test.tsx`

Expected: FAIL because the current test file does not yet verify the new mobile navigation contract.

- [ ] **Step 3: Implement minimal AppShell markup and CSS changes**

Update `AppShell.tsx` and `styles.css` so that:
- topbar structure can wrap safely on tablet widths
- the search bar and action buttons shrink without collapsing the whole header
- mobile nav remains the primary nav on small screens
- main content and footer spacing account for the fixed mobile nav

- [ ] **Step 4: Run the shell tests again**

Run: `npm --prefix frontend test -- src/components/AppShell.test.tsx`

Expected: PASS

### Task 2: Make The Home Page Work On Mobile Without Hiding Features

**Files:**
- Modify: `frontend/src/pages/HomePage.tsx`
- Modify: `frontend/src/styles.css`

- [ ] **Step 1: Write a failing home-page rendering test or extend an existing component test**

Cover these expectations:
- hero stats still render on small screens
- hot topics and announcements sections still exist
- filter chips remain reachable

- [ ] **Step 2: Run the targeted tests to verify failure**

Run: `npm --prefix frontend test -- src/components/PostCard.test.tsx src/components/AppShell.test.tsx`

Expected: FAIL or expose missing layout-oriented expectations before implementation.

- [ ] **Step 3: Implement homepage layout reordering and responsive card rules**

Update `HomePage.tsx` and `styles.css` so that:
- the main feed column becomes first on tablet/mobile
- side modules collapse below the feed in a controlled order
- hero, stat cards, quote card, filter row, and topic strip remain readable and tappable

- [ ] **Step 4: Re-run the affected frontend tests**

Run: `npm --prefix frontend test -- src/components/AppShell.test.tsx src/components/PostCard.test.tsx`

Expected: PASS

### Task 3: Tighten Auth Page Small-Screen Behavior

**Files:**
- Modify: `frontend/src/pages/LoginPage.tsx`
- Modify: `frontend/src/pages/RegisterPage.tsx`
- Modify: `frontend/src/styles.css`

- [ ] **Step 1: Add a failing auth-page test or reuse existing rendering tests**

Verify that auth pages keep their form visible and do not rely on side-by-side layout below the mobile breakpoint.

- [ ] **Step 2: Run the relevant tests to verify failure**

Run: `npm --prefix frontend test -- src/components/AppShell.test.tsx`

Expected: FAIL after adding auth expectations.

- [ ] **Step 3: Implement compact auth spacing and stacking**

Update auth layout rules so that:
- the form card always appears first on narrow screens
- headings, spacing, and inline form rows compress cleanly
- the informational side panel remains visible below the form instead of being removed

- [ ] **Step 4: Re-run the affected tests**

Run: `npm --prefix frontend test -- src/components/AppShell.test.tsx`

Expected: PASS

### Task 4: Adapt Profile And Messaging For Mobile Workflows

**Files:**
- Modify: `frontend/src/pages/ProfilePage.tsx`
- Modify: `frontend/src/pages/ProfilePage.test.tsx`
- Modify: `frontend/src/styles.css`

- [ ] **Step 1: Extend the profile messaging tests with a mobile workflow expectation**

Add a test that confirms the message tab can still select a conversation and expose the active thread controls when rendered in a narrow viewport setup.

- [ ] **Step 2: Run the profile test to verify the new expectation fails**

Run: `npm --prefix frontend test -- src/pages/ProfilePage.test.tsx`

Expected: FAIL before the responsive message-panel behavior is implemented.

- [ ] **Step 3: Implement responsive profile hero and message layout**

Update `ProfilePage.tsx` and `styles.css` so that:
- profile hero content stacks vertically on narrow screens
- message list and active thread can be used in a single-column flow
- compose area, recall actions, and conversation switching remain accessible

- [ ] **Step 4: Re-run the profile tests**

Run: `npm --prefix frontend test -- src/pages/ProfilePage.test.tsx`

Expected: PASS

### Task 5: Finish Post Cards, Comments, And End-To-End Verification

**Files:**
- Modify: `frontend/src/components/PostCard.tsx`
- Modify: `frontend/src/components/PostCard.test.tsx`
- Modify: `frontend/src/styles.css`

- [ ] **Step 1: Add a failing post-card responsiveness test**

Verify that the post action area and comments area still render together and can wrap on small-screen conditions.

- [ ] **Step 2: Run the post-card test to verify failure**

Run: `npm --prefix frontend test -- src/components/PostCard.test.tsx`

Expected: FAIL before layout-supporting markup and classes are in place.

- [ ] **Step 3: Implement wrapping action rows, comment spacing, and modal-friendly sizing**

Update the post-card related markup and shared CSS so that:
- action buttons wrap instead of overflowing
- comment composer and reply areas use full width on narrow screens
- modal bodies stay within the viewport

- [ ] **Step 4: Run the full frontend verification set**

Run: `npm --prefix frontend test`
Expected: PASS

Run: `npm --prefix frontend run build`
Expected: build succeeds

- [ ] **Step 5: Verify in the real remote Docker environment**

Use the deployed site with a mobile viewport to validate:
- login works
- home page layout remains stable
- posts and comments stay operable
- profile messages remain usable
