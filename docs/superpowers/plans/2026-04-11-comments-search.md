# Comments And Search Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为树洞项目补齐两级评论能力和统一搜索结果页，并保持现有前端视觉风格不变。

**Architecture:** 后端继续以 MySQL 作为评论与搜索的真源，Redis 只承担评论列表和搜索结果缓存；前端保留现有页面结构，新增独立搜索页，并在帖子卡片内联展开评论区。实现顺序先后端接口和缓存，再前端页面与交互，最后做联调和回归验证。

**Tech Stack:** Spring Boot 3, MyBatis, MySQL, Redis, React 18, TypeScript, Vite, Vitest, React Testing Library

---

### Task 1: Backend Comments API

**Files:**
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/CommentCreateRequest.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/PostCommentDto.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/PostCommentsDto.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/PostCommentData.java`
- Create: `backend/whu-treehole-server/src/main/java/com/whu/treehole/server/service/PostCommentService.java`
- Create: `backend/whu-treehole-server/src/test/java/com/whu/treehole/server/service/PostCommentServiceTest.java`
- Modify: `backend/whu-treehole-server/src/main/resources/db/schema.sql`
- Modify: `backend/whu-treehole-server/src/main/resources/db/data.sql`
- Modify: `backend/whu-treehole-server/src/main/java/com/whu/treehole/server/controller/PostController.java`
- Modify: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/PortalQueryMapper.java`
- Modify: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/PortalCommandMapper.java`
- Modify: `backend/whu-treehole-infra/src/main/resources/mapper/PortalQueryMapper.xml`
- Modify: `backend/whu-treehole-infra/src/main/resources/mapper/PortalCommandMapper.xml`
- Test: `backend/whu-treehole-server/src/test/java/com/whu/treehole/server/service/PostCommentServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.CommentCreateRequest;
import com.whu.treehole.domain.dto.PostCommentDto;
import com.whu.treehole.domain.dto.PostCommentsDto;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.PostCommentData;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.UserProfileData;
import com.whu.treehole.server.support.PostTimeFormatter;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    @Mock
    private PortalQueryMapper portalQueryMapper;

    @Mock
    private PortalCommandMapper portalCommandMapper;

    @Captor
    private ArgumentCaptor<PostCommentData> commentCaptor;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-11T01:30:00Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    void shouldCreateReplyAndRefreshCommentCount() {
        PostCommentService service = new PostCommentService(
                portalQueryMapper,
                portalCommandMapper,
                new PostTimeFormatter(),
                clock);

        PostData post = new PostData();
        post.setId(8L);
        post.setPostCode("home-1");

        UserProfileData user = new UserProfileData();
        user.setId(1L);
        user.setName("测试用户");
        user.setCollege("信管");
        user.setGradeYear("2022");

        PostCommentData root = new PostCommentData();
        root.setId(20L);
        root.setCommentCode("comment-root");
        root.setPostId(8L);
        root.setUserId(2L);
        root.setAuthorName("原评论人");
        root.setParentCommentId(null);
        root.setRootCommentId(20L);

        when(portalCommandMapper.selectPostByCode("home-1", 1L)).thenReturn(post);
        when(portalQueryMapper.selectUserProfile(1L)).thenReturn(user);
        when(portalQueryMapper.selectCommentByCode("home-1", "comment-root")).thenReturn(root);
        when(portalQueryMapper.selectCommentsByPostCode("home-1")).thenReturn(List.of());

        PostCommentDto created = service.replyComment(1L, "home-1", "comment-root", new CommentCreateRequest("收到，谢谢"));
        PostCommentsDto comments = service.listComments(1L, "home-1");

        verify(portalCommandMapper).insertPostComment(commentCaptor.capture());
        verify(portalCommandMapper).increasePostCommentCount(8L);
        assertEquals("comment-root", created.parentCommentCode());
        assertEquals("原评论人", created.replyToUserName());
        assertEquals(0, comments.comments().size());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -s settings.xml -pl whu-treehole-server -am -Dtest=PostCommentServiceTest test`

Expected: FAIL，提示 `PostCommentService`、`CommentCreateRequest`、`PostCommentDto` 或 Mapper 方法不存在。

- [ ] **Step 3: Write minimal implementation**

```java
// backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/CommentCreateRequest.java
public record CommentCreateRequest(
        @NotBlank(message = "评论内容不能为空")
        @Size(max = 1000, message = "评论内容不能超过1000字符")
        String content
) {
}

// backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/PostCommentDto.java
public record PostCommentDto(
        String id,
        String postId,
        String parentCommentCode,
        String author,
        String handle,
        String content,
        String createdAt,
        Boolean mine,
        String replyToUserName,
        List<PostCommentDto> replies
) {
}

// backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/PostCommentsDto.java
public record PostCommentsDto(List<PostCommentDto> comments, Integer total) {
}
```

```java
// backend/whu-treehole-server/src/main/java/com/whu/treehole/server/service/PostCommentService.java
@Service
public class PostCommentService {

    private final PortalQueryMapper portalQueryMapper;
    private final PortalCommandMapper portalCommandMapper;
    private final PostTimeFormatter postTimeFormatter;
    private final Clock clock;

    public PostCommentService(PortalQueryMapper portalQueryMapper,
                              PortalCommandMapper portalCommandMapper,
                              PostTimeFormatter postTimeFormatter,
                              Clock clock) {
        this.portalQueryMapper = portalQueryMapper;
        this.portalCommandMapper = portalCommandMapper;
        this.postTimeFormatter = postTimeFormatter;
        this.clock = clock;
    }

    @Cacheable(cacheNames = "postComments", key = "#postCode")
    public PostCommentsDto listComments(long userId, String postCode) {
        return new PostCommentsDto(List.of(), 0);
    }

    @Transactional
    @CacheEvict(cacheNames = "postComments", key = "#postCode")
    public PostCommentDto createComment(long userId, String postCode, CommentCreateRequest request) {
        return persistComment(userId, requirePost(postCode, userId), null, request);
    }

    @Transactional
    @CacheEvict(cacheNames = "postComments", key = "#postCode")
    public PostCommentDto replyComment(long userId, String postCode, String commentCode, CommentCreateRequest request) {
        return persistComment(userId, requirePost(postCode, userId), requireComment(postCode, commentCode), request);
    }
}
```

```sql
-- backend/whu-treehole-server/src/main/resources/db/schema.sql
CREATE TABLE IF NOT EXISTS post_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment_code VARCHAR(64) NOT NULL UNIQUE,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_comment_id BIGINT NULL,
    root_comment_id BIGINT NOT NULL,
    reply_to_user_id BIGINT NULL,
    author_name VARCHAR(64) NOT NULL,
    author_handle VARCHAR(128) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    deleted_flag TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -s settings.xml -pl whu-treehole-server -am -Dtest=PostCommentServiceTest test`

Expected: PASS，`PostCommentServiceTest` 通过。

- [ ] **Step 5: Commit**

```bash
git add backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/CommentCreateRequest.java \
  backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/PostCommentDto.java \
  backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/PostCommentsDto.java \
  backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/PostCommentData.java \
  backend/whu-treehole-server/src/main/java/com/whu/treehole/server/service/PostCommentService.java \
  backend/whu-treehole-server/src/main/java/com/whu/treehole/server/controller/PostController.java \
  backend/whu-treehole-server/src/main/resources/db/schema.sql \
  backend/whu-treehole-server/src/main/resources/db/data.sql \
  backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/PortalQueryMapper.java \
  backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/PortalCommandMapper.java \
  backend/whu-treehole-infra/src/main/resources/mapper/PortalQueryMapper.xml \
  backend/whu-treehole-infra/src/main/resources/mapper/PortalCommandMapper.xml \
  backend/whu-treehole-server/src/test/java/com/whu/treehole/server/service/PostCommentServiceTest.java
git commit -m "feat: add post comments api"
```

### Task 2: Backend Search API And Cache

**Files:**
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/SearchResultDto.java`
- Create: `backend/whu-treehole-server/src/main/java/com/whu/treehole/server/controller/SearchController.java`
- Create: `backend/whu-treehole-server/src/main/java/com/whu/treehole/server/service/SearchQueryService.java`
- Create: `backend/whu-treehole-server/src/test/java/com/whu/treehole/server/service/SearchQueryServiceTest.java`
- Modify: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/PortalQueryMapper.java`
- Modify: `backend/whu-treehole-infra/src/main/resources/mapper/PortalQueryMapper.xml`
- Test: `backend/whu-treehole-server/src/test/java/com/whu/treehole/server/service/SearchQueryServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.SearchResultDto;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.ContactData;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.StoryData;
import com.whu.treehole.server.support.PostTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchQueryServiceTest {

    @Mock
    private PortalQueryMapper portalQueryMapper;

    @Test
    void shouldAggregatePostsStoriesAndContacts() {
        SearchQueryService service = new SearchQueryService(portalQueryMapper, new PostTimeFormatter());

        when(portalQueryMapper.searchPosts(eq("樱花"), eq(1L))).thenReturn(List.of(new PostData()));
        when(portalQueryMapper.searchStories("樱花")).thenReturn(List.of(new StoryData()));
        when(portalQueryMapper.searchContacts(eq("樱花"), eq(1L))).thenReturn(List.of(new ContactData()));

        SearchResultDto result = service.search(1L, " 樱花 ");

        assertEquals("樱花", result.keyword());
        assertEquals(1, result.posts().size());
        assertEquals(1, result.stories().size());
        assertEquals(1, result.contacts().size());
        assertEquals(3, result.total());
    }

    @Test
    void shouldRejectBlankKeyword() {
        SearchQueryService service = new SearchQueryService(portalQueryMapper, new PostTimeFormatter());

        assertThrows(BusinessException.class, () -> service.search(1L, "   "));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -s settings.xml -pl whu-treehole-server -am -Dtest=SearchQueryServiceTest test`

Expected: FAIL，提示 `SearchQueryService`、`SearchResultDto` 或 `searchPosts/searchStories/searchContacts` 不存在。

- [ ] **Step 3: Write minimal implementation**

```java
// backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/SearchResultDto.java
public record SearchResultDto(
        String keyword,
        Integer total,
        List<PostCardDto> posts,
        List<StoryCardDto> stories,
        List<AlumniContactDto> contacts
) {
}

// backend/whu-treehole-server/src/main/java/com/whu/treehole/server/service/SearchQueryService.java
@Service
public class SearchQueryService {

    private final PortalQueryMapper portalQueryMapper;
    private final PostTimeFormatter postTimeFormatter;

    public SearchQueryService(PortalQueryMapper portalQueryMapper, PostTimeFormatter postTimeFormatter) {
        this.portalQueryMapper = portalQueryMapper;
        this.postTimeFormatter = postTimeFormatter;
    }

    @Cacheable(cacheNames = "searchResults", key = "#userId + ':' + #keyword.trim().toLowerCase()")
    public SearchResultDto search(long userId, String keyword) {
        String normalized = keyword == null ? null : keyword.trim();
        if (normalized == null || normalized.isBlank()) {
            throw new BusinessException(4005, "搜索关键词不能为空");
        }
        List<PostCardDto> posts = portalQueryMapper.searchPosts(normalized, userId).stream().map(this::toPostCard).toList();
        List<StoryCardDto> stories = portalQueryMapper.searchStories(normalized).stream()
                .map(item -> new StoryCardDto(item.getStoryCode(), item.getTitle(), item.getMeta()))
                .toList();
        List<AlumniContactDto> contacts = portalQueryMapper.searchContacts(normalized, userId).stream()
                .map(item -> new AlumniContactDto(item.getContactCode(), item.getName(), item.getMeta(), item.getFocus(), item.getAvatarUrl(), Boolean.TRUE.equals(item.getFollowed())))
                .toList();
        return new SearchResultDto(normalized, posts.size() + stories.size() + contacts.size(), posts, stories, contacts);
    }
}

// backend/whu-treehole-server/src/main/java/com/whu/treehole/server/controller/SearchController.java
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchQueryService searchQueryService;

    public SearchController(SearchQueryService searchQueryService) {
        this.searchQueryService = searchQueryService;
    }

    @GetMapping
    public ApiResponse<SearchResultDto> search(@RequestParam("q") String keyword) {
        return ApiResponse.success(searchQueryService.search(AuthContextHolder.currentUserId(), keyword));
    }
}
```

```xml
<!-- backend/whu-treehole-infra/src/main/resources/mapper/PortalQueryMapper.xml -->
<select id="searchPosts" resultType="com.whu.treehole.infra.model.PostData">
    SELECT <include refid="PostProjection"/>
    FROM posts p
             LEFT JOIN post_interactions i ON i.post_id = p.id AND i.user_id = #{userId}
    WHERE p.title LIKE CONCAT('%', #{keyword}, '%')
       OR p.content LIKE CONCAT('%', #{keyword}, '%')
       OR p.author_name LIKE CONCAT('%', #{keyword}, '%')
       OR p.topic_name LIKE CONCAT('%', #{keyword}, '%')
    ORDER BY p.created_at DESC, p.id DESC
</select>
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -s settings.xml -pl whu-treehole-server -am -Dtest=SearchQueryServiceTest test`

Expected: PASS，`SearchQueryServiceTest` 通过。

- [ ] **Step 5: Commit**

```bash
git add backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/SearchResultDto.java \
  backend/whu-treehole-server/src/main/java/com/whu/treehole/server/controller/SearchController.java \
  backend/whu-treehole-server/src/main/java/com/whu/treehole/server/service/SearchQueryService.java \
  backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/PortalQueryMapper.java \
  backend/whu-treehole-infra/src/main/resources/mapper/PortalQueryMapper.xml \
  backend/whu-treehole-server/src/test/java/com/whu/treehole/server/service/SearchQueryServiceTest.java
git commit -m "feat: add unified search api"
```

### Task 3: Frontend Search Page And Test Harness

**Files:**
- Create: `frontend/vitest.config.ts`
- Create: `frontend/src/test/setup.ts`
- Create: `frontend/src/test/renderWithProviders.tsx`
- Create: `frontend/src/pages/SearchPage.tsx`
- Create: `frontend/src/pages/SearchPage.test.tsx`
- Modify: `frontend/package.json`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/components/AppShell.tsx`
- Modify: `frontend/src/pages/AlumniPage.tsx`
- Modify: `frontend/src/services/api.ts`
- Modify: `frontend/src/types.ts`
- Modify: `frontend/src/styles.css`
- Test: `frontend/src/pages/SearchPage.test.tsx`

- [ ] **Step 1: Write the failing test**

```tsx
import { screen } from '@testing-library/react';
import { SearchPage } from './SearchPage';
import { renderWithProviders } from '../test/renderWithProviders';

vi.mock('../services/api', () => ({
  searchAll: vi.fn().mockResolvedValue({
    keyword: '樱花',
    total: 3,
    posts: [{ id: 'home-1', topic: '校园日常', audience: '首页', author: '作者', handle: '句柄', content: '樱花很美', createdAt: '2026-04-11 09:30', likes: 1, comments: 2, saves: 3, accent: 'rose' }],
    stories: [{ id: 'story-1', title: '樱花季', meta: '校友回忆' }],
    contacts: [{ id: 'wang', name: '王校友', meta: '2010级', focus: '材料', avatar: '/avatar.jpg' }],
  }),
}));

test('renders grouped search results', async () => {
  renderWithProviders(<SearchPage />, { route: '/search?q=%E6%A8%B1%E8%8A%B1' });

  expect(await screen.findByText('搜索结果')).toBeInTheDocument();
  expect(await screen.findByText('帖子')).toBeInTheDocument();
  expect(await screen.findByText('校友故事')).toBeInTheDocument();
  expect(await screen.findByText('人脉联系人')).toBeInTheDocument();
  expect(await screen.findByText('樱花很美')).toBeInTheDocument();
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd frontend && npm install && npm run test -- SearchPage`

Expected: FAIL，提示 `vitest`、`SearchPage`、`renderWithProviders` 或 `searchAll` 不存在。

- [ ] **Step 3: Write minimal implementation**

```json
// frontend/package.json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "test": "vitest run"
  },
  "devDependencies": {
    "@testing-library/jest-dom": "^6.6.3",
    "@testing-library/react": "^16.1.0",
    "@testing-library/user-event": "^14.5.2",
    "jsdom": "^25.0.1",
    "vitest": "^2.1.8"
  }
}
```

```tsx
// frontend/src/pages/SearchPage.tsx
export function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const keyword = (searchParams.get('q') ?? '').trim();
  const [data, setData] = useState<SearchResult | null>(null);

  useEffect(() => {
    if (!keyword) return;
    void searchAll(keyword).then(setData);
  }, [keyword]);

  return (
    <div className="container page-stack">
      <section className="surface-card">
        <div className="section-head">
          <h2>搜索结果</h2>
          <span className="eyebrow">{data?.total ?? 0} 条匹配</span>
        </div>
        <label className="search-bar search-bar--wide">
          <input
            defaultValue={keyword}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                const next = event.currentTarget.value.trim();
                setSearchParams(next ? { q: next } : {});
              }
            }}
          />
        </label>
      </section>
      <section className="surface-card"><div className="section-head"><h2>帖子</h2></div></section>
      <section className="surface-card"><div className="section-head"><h2>校友故事</h2></div></section>
      <section className="surface-card"><div className="section-head"><h2>人脉联系人</h2></div></section>
    </div>
  );
}

// frontend/src/services/api.ts
export interface SearchResult {
  keyword: string;
  total: number;
  posts: FeedPost[];
  stories: StoryCard[];
  contacts: AlumniContact[];
}

export function searchAll(keyword: string) {
  return request<SearchResult>(`/search?q=${encodeURIComponent(keyword)}`);
}

// frontend/src/App.tsx
<Route path="/search" element={<SearchPage />} />
```

```tsx
// frontend/src/components/AppShell.tsx
const navigate = useNavigate();
const [searchKeyword, setSearchKeyword] = useState('');

function submitSearch() {
  const next = searchKeyword.trim();
  if (!next) return;
  navigate(`/search?q=${encodeURIComponent(next)}`);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd frontend && npm run test -- SearchPage && npm run build`

Expected: PASS，`SearchPage.test.tsx` 通过，Vite 构建通过。

- [ ] **Step 5: Commit**

```bash
git add frontend/package.json frontend/vitest.config.ts frontend/src/test/setup.ts \
  frontend/src/test/renderWithProviders.tsx frontend/src/pages/SearchPage.tsx \
  frontend/src/pages/SearchPage.test.tsx frontend/src/App.tsx \
  frontend/src/components/AppShell.tsx frontend/src/pages/AlumniPage.tsx \
  frontend/src/services/api.ts frontend/src/types.ts frontend/src/styles.css
git commit -m "feat: add search results page"
```

### Task 4: Frontend Comments Panel And Comment Count Sync

**Files:**
- Create: `frontend/src/components/PostCommentsPanel.tsx`
- Create: `frontend/src/components/PostCommentsPanel.test.tsx`
- Modify: `frontend/src/context/AppContext.tsx`
- Modify: `frontend/src/components/PostCard.tsx`
- Modify: `frontend/src/services/api.ts`
- Modify: `frontend/src/types.ts`
- Modify: `frontend/src/styles.css`
- Test: `frontend/src/components/PostCommentsPanel.test.tsx`

- [ ] **Step 1: Write the failing test**

```tsx
import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { PostCommentsPanel } from './PostCommentsPanel';
import { renderWithProviders } from '../test/renderWithProviders';

vi.mock('../services/api', () => ({
  getPostComments: vi.fn().mockResolvedValue({ total: 1, comments: [] }),
  createPostComment: vi.fn().mockResolvedValue({
    id: 'comment-1',
    postId: 'home-1',
    parentCommentCode: null,
    author: '测试用户',
    handle: '信管 · 2022',
    content: '第一条评论',
    createdAt: '2026-04-11 10:00',
    mine: true,
    replyToUserName: null,
    replies: [],
  }),
  createCommentReply: vi.fn(),
}));

test('submits a new root comment and updates visible count', async () => {
  const user = userEvent.setup();
  renderWithProviders(<PostCommentsPanel postId="home-1" initialCount={0} onCountChange={vi.fn()} />);

  expect(await screen.findByPlaceholderText('写下你的评论...')).toBeInTheDocument();
  await user.type(screen.getByPlaceholderText('写下你的评论...'), '第一条评论');
  await user.click(screen.getByRole('button', { name: '发送评论' }));

  expect(await screen.findByText('第一条评论')).toBeInTheDocument();
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd frontend && npm run test -- PostCommentsPanel`

Expected: FAIL，提示 `PostCommentsPanel`、评论 API 或评论类型不存在。

- [ ] **Step 3: Write minimal implementation**

```ts
// frontend/src/types.ts
export interface PostComment {
  id: string;
  postId: string;
  parentCommentCode?: string | null;
  author: string;
  handle: string;
  content: string;
  createdAt: string;
  mine: boolean;
  replyToUserName?: string | null;
  replies: PostComment[];
}
```

```tsx
// frontend/src/components/PostCommentsPanel.tsx
export function PostCommentsPanel({ postId, initialCount, onCountChange }: Props) {
  const [comments, setComments] = useState<PostComment[]>([]);
  const [content, setContent] = useState('');

  useEffect(() => {
    void getPostComments(postId).then((data) => setComments(data.comments));
  }, [postId]);

  async function submitRootComment() {
    const next = content.trim();
    if (!next) return;
    const created = await createPostComment(postId, next);
    setComments((current) => [...current, created]);
    onCountChange(initialCount + 1);
    setContent('');
  }

  return (
    <section className="post-comments">
      <div className="post-comments__composer">
        <textarea
          placeholder="写下你的评论..."
          value={content}
          onChange={(event) => setContent(event.target.value)}
        />
        <button className="mini-button" type="button" onClick={() => void submitRootComment()}>
          发送评论
        </button>
      </div>
      <div className="post-comments__list">
        {comments.map((comment) => (
          <article key={comment.id} className="post-comment">
            <strong>{comment.author}</strong>
            <span>{comment.content}</span>
            <small>{comment.createdAt}</small>
          </article>
        ))}
      </div>
    </section>
  );
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd frontend && npm run test -- PostCommentsPanel && npm run build`

Expected: PASS，评论面板测试通过，前端构建通过。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/components/PostCommentsPanel.tsx \
  frontend/src/components/PostCommentsPanel.test.tsx \
  frontend/src/context/AppContext.tsx frontend/src/components/PostCard.tsx \
  frontend/src/services/api.ts frontend/src/types.ts frontend/src/styles.css
git commit -m "feat: add post comments panel"
```

### Task 5: Full Regression And Deployment Verification

**Files:**
- Modify: `backend/whu-treehole-server/src/main/resources/db/data.sql`
- Modify: `docs/superpowers/specs/2026-04-11-comments-search-design.md` (仅在实现偏差时回填)
- Test: `backend/whu-treehole-server/src/test/java/com/whu/treehole/server/service/PostCommentServiceTest.java`
- Test: `backend/whu-treehole-server/src/test/java/com/whu/treehole/server/service/SearchQueryServiceTest.java`
- Test: `frontend/src/pages/SearchPage.test.tsx`
- Test: `frontend/src/components/PostCommentsPanel.test.tsx`

- [ ] **Step 1: Write the failing regression checklist**

```text
1. 首页帖子卡片点击评论数后可以展开评论区
2. 新增一级评论后评论数立即 +1
3. 在一级评论下回复后，回复显示在该评论下方
4. 顶部搜索框输入“樱花”后跳转 /search?q=樱花
5. 搜索页同时展示帖子、校友故事、联系人分组
6. 构建、测试、CI 全部通过
```

- [ ] **Step 2: Run full verification before收口**

Run: `mvn -q -s settings.xml -pl whu-treehole-server -am test`

Expected: PASS，后端评论与搜索测试通过。

Run: `cd frontend && npm run test && npm run build`

Expected: PASS，搜索页与评论面板测试通过，前端构建通过。

Run: `docker compose config`

Expected: PASS，Compose 配置可以正常解析。

- [ ] **Step 3: Execute manual smoke verification**

```text
1. 登录站点
2. 打开首页任意帖子，展开评论区，发布一条一级评论
3. 对该评论点击“回复”，发布二级回复
4. 确认帖子卡片评论数同步变化
5. 在顶部搜索框输入“樱花”并回车
6. 确认搜索页包含帖子、校友故事、联系人三个分组
7. 在校友圈页使用同样关键词重复一次搜索入口验证
```

- [ ] **Step 4: Push and verify CI/CD**

Run: `git push origin HEAD:main`

Expected: GitHub Actions `CI/CD` 成功，`backend-ci`、`frontend-ci`、`deploy` 全绿。

Run: `gh run list -R history-turing/SE_project --limit 5`

Expected: 最新 run 为 `completed success`。

- [ ] **Step 5: Commit**

```bash
git add backend/whu-treehole-server/src/main/resources/db/data.sql \
  docs/superpowers/specs/2026-04-11-comments-search-design.md
git commit -m "chore: verify comments and search rollout"
```

## Self-Review

**Spec coverage:**

- 评论表、评论接口、两级回复结构：Task 1
- 搜索接口、分组结果、Redis 缓存：Task 2
- 独立搜索页、搜索入口跳转、现有风格延续：Task 3
- 帖子卡片评论区、评论数同步、前端交互：Task 4
- 回归测试、联调、CI/CD 验证：Task 5

**Placeholder scan:**

- 已去除 `TODO`、`TBD`、`适当处理` 一类占位描述。
- 每个任务都给出明确文件路径、测试命令、提交命令和关键代码片段。

**Type consistency:**

- 评论相关统一使用 `CommentCreateRequest`、`PostCommentDto`、`PostCommentsDto`
- 搜索相关统一使用 `SearchResultDto` 和前端 `SearchResult`
- 前端评论类型统一使用 `PostComment`
