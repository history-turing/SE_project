import { useEffect, useMemo, useState } from 'react';
import { useAuthContext } from '../context/AuthContext';
import {
  assignUserRole,
  banUser,
  createAnnouncement,
  deleteComment,
  deletePost,
  getAnnouncementDetail,
  getAdminAnnouncements,
  getAdminReports,
  getAdminRoles,
  getAdminTrendingTopics,
  getAdminUsers,
  getAuditLogs,
  offlineAnnouncement,
  publishAnnouncement,
  resolveReport,
  restoreComment,
  restorePost,
  saveTrendingTopicRule,
  unbanUser,
  updateAnnouncement,
} from '../services/api';
import type {
  AdminUser,
  AnnouncementSavePayload,
  AnnouncementSummary,
  AuditLog,
  ReportSummary,
  Role,
  TrendingTopicAdmin,
  TrendingTopicRulePayload,
} from '../types';

type AdminTab = 'reports' | 'users' | 'roles' | 'audit' | 'trending' | 'announcements';

function parseCommentTarget(targetCode: string) {
  const [postCode, commentCode] = targetCode.split(':');
  if (!postCode || !commentCode) {
    return null;
  }
  return { postCode, commentCode };
}

function createEmptyRuleForm(): TrendingTopicRulePayload {
  return {
    topicKey: '',
    displayName: '',
    mergeTargetKey: '',
    hidden: false,
    pinned: false,
    sortOrder: 0,
  };
}

function createEmptyAnnouncementForm(): AnnouncementSavePayload {
  return {
    title: '',
    summary: '',
    content: '',
    category: '校园公告',
    pinned: false,
    popupEnabled: false,
    popupOncePerSession: true,
    publishedAt: '',
    expireAt: '',
  };
}

function normalizeDateTime(value?: string | null) {
  return value && value.trim() ? value : null;
}

export function AdminPage() {
  const { hasPermission, hasRole } = useAuthContext();
  const [reports, setReports] = useState<ReportSummary[]>([]);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [trendingTopics, setTrendingTopics] = useState<TrendingTopicAdmin[]>([]);
  const [announcements, setAnnouncements] = useState<AnnouncementSummary[]>([]);
  const [ruleForm, setRuleForm] = useState<TrendingTopicRulePayload>(createEmptyRuleForm());
  const [announcementForm, setAnnouncementForm] = useState<AnnouncementSavePayload>(createEmptyAnnouncementForm());
  const [editingAnnouncementCode, setEditingAnnouncementCode] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionKey, setActionKey] = useState('');

  const canReadTrending = hasPermission('trending.read.any');
  const canManageTrending = hasPermission('trending.curate') || hasPermission('trending.hide');
  const canReadAnnouncements = hasPermission('announcement.read.any');
  const canManageAnnouncements = hasPermission('announcement.create');
  const canPublishAnnouncements = hasPermission('announcement.publish');

  const tabs = useMemo(
    () =>
      [
        { key: 'reports' as const, label: '举报处理', visible: hasPermission('report.read.any') },
        { key: 'users' as const, label: '用户管理', visible: hasPermission('user.ban') || hasPermission('user.unban') },
        { key: 'roles' as const, label: '角色管理', visible: hasPermission('role.assign.admin') },
        {
          key: 'audit' as const,
          label: '审计日志',
          visible: hasPermission('audit.read.moderation') || hasPermission('audit.read.all'),
        },
        { key: 'trending' as const, label: '热议运营', visible: canReadTrending || canManageTrending },
        { key: 'announcements' as const, label: '公告管理', visible: canReadAnnouncements || canManageAnnouncements },
      ].filter((tab) => tab.visible),
    [canManageAnnouncements, canManageTrending, canReadAnnouncements, canReadTrending, hasPermission],
  );
  const [activeTab, setActiveTab] = useState<AdminTab>(tabs[0]?.key ?? 'reports');

  useEffect(() => {
    if (!tabs.some((tab) => tab.key === activeTab)) {
      setActiveTab(tabs[0]?.key ?? 'reports');
    }
  }, [activeTab, tabs]);

  async function loadData() {
    setLoading(true);
    setError('');
    try {
      const [
        nextReports,
        nextUsers,
        nextRoles,
        nextAuditLogs,
        nextTrendingTopics,
        nextAnnouncements,
      ] = await Promise.all([
        hasPermission('report.read.any') ? getAdminReports() : Promise.resolve([]),
        hasPermission('user.ban') || hasPermission('user.unban') ? getAdminUsers() : Promise.resolve([]),
        hasPermission('role.assign.admin') ? getAdminRoles() : Promise.resolve([]),
        hasPermission('audit.read.moderation') || hasPermission('audit.read.all')
          ? getAuditLogs()
          : Promise.resolve([]),
        canReadTrending || canManageTrending ? getAdminTrendingTopics() : Promise.resolve([]),
        canReadAnnouncements || canManageAnnouncements ? getAdminAnnouncements() : Promise.resolve([]),
      ]);

      setReports(nextReports);
      setUsers(nextUsers);
      setRoles(nextRoles);
      setAuditLogs(nextAuditLogs);
      setTrendingTopics(nextTrendingTopics);
      setAnnouncements(nextAnnouncements);
    } catch (loadError) {
      console.error('加载管理台数据失败。', loadError);
      setError('管理台数据加载失败，请稍后刷新重试。');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadData();
  }, []);

  async function runAction(key: string, action: () => Promise<void>) {
    if (actionKey) {
      return;
    }

    setActionKey(key);
    setError('');
    try {
      await action();
      await loadData();
    } catch (actionError) {
      console.error('管理操作失败。', actionError);
      setError('管理操作失败，请稍后重试。');
    } finally {
      setActionKey('');
    }
  }

  function isBusy(key: string) {
    return actionKey === key;
  }

  async function startEditAnnouncement(item: AnnouncementSummary) {
    setError('');
    try {
      const detail = await getAnnouncementDetail(item.code);
      setEditingAnnouncementCode(item.code);
      setAnnouncementForm({
        title: detail.title,
        summary: detail.summary,
        content: detail.content,
        category: detail.category,
        pinned: detail.pinned,
        popupEnabled: detail.popupEnabled,
        popupOncePerSession: detail.popupOncePerSession,
        publishedAt: detail.publishedAt ? detail.publishedAt.replace(' ', 'T') : '',
        expireAt: detail.expireAt ? detail.expireAt.replace(' ', 'T') : '',
      });
    } catch (loadError) {
      console.error('加载公告详情失败。', loadError);
      setError('加载公告详情失败，请稍后重试。');
    }
  }

  function resetAnnouncementEditor() {
    setEditingAnnouncementCode('');
    setAnnouncementForm(createEmptyAnnouncementForm());
  }

  async function submitTrendingRule() {
    if (!ruleForm.topicKey?.trim()) {
      setError('请先填写话题键。');
      return;
    }
    await runAction('save-trending-rule', async () => {
      await saveTrendingTopicRule({
        ...ruleForm,
        topicKey: ruleForm.topicKey.trim(),
        displayName: ruleForm.displayName?.trim() || undefined,
        mergeTargetKey: ruleForm.mergeTargetKey?.trim() || undefined,
        sortOrder: Number(ruleForm.sortOrder ?? 0),
      });
      setRuleForm(createEmptyRuleForm());
    });
  }

  async function submitAnnouncement() {
    if (!announcementForm.title.trim() || !announcementForm.summary.trim() || !announcementForm.content.trim()) {
      setError('请填写完整的公告标题、摘要和正文。');
      return;
    }

    const payload: AnnouncementSavePayload = {
      ...announcementForm,
      title: announcementForm.title.trim(),
      summary: announcementForm.summary.trim(),
      content: announcementForm.content.trim(),
      category: announcementForm.category.trim(),
      publishedAt: normalizeDateTime(announcementForm.publishedAt),
      expireAt: normalizeDateTime(announcementForm.expireAt),
    };

    await runAction(editingAnnouncementCode || 'create-announcement', async () => {
      if (editingAnnouncementCode) {
        await updateAnnouncement(editingAnnouncementCode, payload);
      } else {
        await createAnnouncement(payload);
      }
      resetAnnouncementEditor();
    });
  }

  function renderReports() {
    return (
      <div className="admin-section">
        <div className="admin-section__header">
          <div>
            <p className="eyebrow">Moderation</p>
            <h2>举报工单</h2>
          </div>
          <button className="secondary-button secondary-button--compact" type="button" onClick={() => void loadData()}>
            刷新
          </button>
        </div>

        {!reports.length ? <p className="search-empty">当前没有举报记录。</p> : null}

        <div className="admin-list">
          {reports.map((report) => {
            const commentTarget = report.targetType === 'COMMENT' ? parseCommentTarget(report.targetCode) : null;
            return (
              <article key={report.reportCode} className="admin-card">
                <div className="admin-card__header">
                  <div>
                    <strong>{report.reportCode}</strong>
                    <span>
                      {report.targetType} · {report.targetCode}
                    </span>
                  </div>
                  <span className={`admin-badge admin-badge--${report.status.toLowerCase()}`}>{report.status}</span>
                </div>
                <p>
                  原因：{report.reasonCode}
                  {report.reasonDetail ? ` / ${report.reasonDetail}` : ''}
                </p>
                <p>创建时间：{report.createdAt}</p>
                {report.resolutionCode ? <p>处理结果：{report.resolutionCode}</p> : null}

                {report.status === 'OPEN' ? (
                  <div className="admin-card__actions">
                    {report.targetType === 'POST' ? (
                      <>
                        <button
                          className="mini-button"
                          type="button"
                          disabled={isBusy(`${report.reportCode}-delete-post`)}
                          onClick={() =>
                            void runAction(`${report.reportCode}-delete-post`, async () => {
                              await deletePost(report.targetCode);
                              await resolveReport(report.reportCode, {
                                resolutionCode: 'DELETE_POST',
                                resolutionNote: '管理员删除帖子并结案',
                              });
                            })
                          }
                        >
                          删除帖子
                        </button>
                        <button
                          className="mini-button"
                          type="button"
                          disabled={isBusy(`${report.reportCode}-restore-post`)}
                          onClick={() =>
                            void runAction(`${report.reportCode}-restore-post`, async () => {
                              await restorePost(report.targetCode);
                              await resolveReport(report.reportCode, {
                                resolutionCode: 'RESTORE_POST',
                                resolutionNote: '管理员恢复帖子并结案',
                              });
                            })
                          }
                        >
                          恢复帖子
                        </button>
                      </>
                    ) : null}

                    {report.targetType === 'COMMENT' && commentTarget ? (
                      <>
                        <button
                          className="mini-button"
                          type="button"
                          disabled={isBusy(`${report.reportCode}-delete-comment`)}
                          onClick={() =>
                            void runAction(`${report.reportCode}-delete-comment`, async () => {
                              await deleteComment(commentTarget.postCode, commentTarget.commentCode);
                              await resolveReport(report.reportCode, {
                                resolutionCode: 'DELETE_COMMENT',
                                resolutionNote: '管理员删除评论并结案',
                              });
                            })
                          }
                        >
                          删除评论
                        </button>
                        <button
                          className="mini-button"
                          type="button"
                          disabled={isBusy(`${report.reportCode}-restore-comment`)}
                          onClick={() =>
                            void runAction(`${report.reportCode}-restore-comment`, async () => {
                              await restoreComment(commentTarget.postCode, commentTarget.commentCode);
                              await resolveReport(report.reportCode, {
                                resolutionCode: 'RESTORE_COMMENT',
                                resolutionNote: '管理员恢复评论并结案',
                              });
                            })
                          }
                        >
                          恢复评论
                        </button>
                      </>
                    ) : null}

                    <button
                      className="mini-button"
                      type="button"
                      disabled={isBusy(`${report.reportCode}-no-action`)}
                      onClick={() =>
                        void runAction(`${report.reportCode}-no-action`, async () => {
                          await resolveReport(report.reportCode, {
                            resolutionCode: 'NO_ACTION',
                            resolutionNote: '经审核暂不处理',
                          });
                        })
                      }
                    >
                      标记无违规
                    </button>
                  </div>
                ) : null}
              </article>
            );
          })}
        </div>
      </div>
    );
  }

  function renderUsers() {
    return (
      <div className="admin-section">
        <div className="admin-section__header">
          <div>
            <p className="eyebrow">Accounts</p>
            <h2>用户管理</h2>
          </div>
        </div>

        <div className="admin-list">
          {users.map((user) => (
            <article key={user.userCode} className="admin-card">
              <div className="admin-card__header">
                <div>
                  <strong>{user.name}</strong>
                  <span>
                    {user.username} · {user.userCode}
                  </span>
                </div>
                <span className={`admin-badge admin-badge--${user.accountStatus.toLowerCase()}`}>{user.accountStatus}</span>
              </div>
              <p>角色：{user.roles.length ? user.roles.map((role) => role.code).join('、') : 'USER'}</p>
              <div className="admin-card__actions">
                {hasRole('SUPER_ADMIN') && !user.roles.some((role) => role.code === 'SUPER_ADMIN') ? (
                  <button
                    className="mini-button"
                    type="button"
                    disabled={isBusy(`${user.userCode}-assign-admin`)}
                    onClick={() =>
                      void runAction(`${user.userCode}-assign-admin`, async () => {
                        await assignUserRole(user.userCode, 'ADMIN');
                      })
                    }
                  >
                    设为管理员
                  </button>
                ) : null}
                {user.accountStatus === 'BANNED' ? (
                  <button
                    className="mini-button"
                    type="button"
                    disabled={isBusy(`${user.userCode}-unban`)}
                    onClick={() =>
                      void runAction(`${user.userCode}-unban`, async () => {
                        await unbanUser(user.userCode);
                      })
                    }
                  >
                    解封
                  </button>
                ) : (
                  <button
                    className="mini-button"
                    type="button"
                    disabled={isBusy(`${user.userCode}-ban`)}
                    onClick={() =>
                      void runAction(`${user.userCode}-ban`, async () => {
                        await banUser(user.userCode, '管理员封禁');
                      })
                    }
                  >
                    封禁
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>
      </div>
    );
  }

  function renderRoles() {
    return (
      <div className="admin-section">
        <div className="admin-section__header">
          <div>
            <p className="eyebrow">Roles</p>
            <h2>角色管理</h2>
          </div>
        </div>

        {!roles.length ? <p className="search-empty">当前没有可展示的角色。</p> : null}

        <div className="admin-list admin-list--compact">
          {roles.map((role) => (
            <article key={role.code} className="admin-card">
              <div className="admin-card__header">
                <div>
                  <strong>{role.name}</strong>
                  <span>{role.code}</span>
                </div>
              </div>
            </article>
          ))}
        </div>
      </div>
    );
  }

  function renderAudit() {
    return (
      <div className="admin-section">
        <div className="admin-section__header">
          <div>
            <p className="eyebrow">Audit</p>
            <h2>审计日志</h2>
          </div>
        </div>

        {!auditLogs.length ? <p className="search-empty">当前没有审计记录。</p> : null}

        <div className="admin-list">
          {auditLogs.map((log) => (
            <article key={log.id} className="admin-card">
              <div className="admin-card__header">
                <div>
                  <strong>{log.actionType}</strong>
                  <span>
                    {log.targetType} · {log.targetCode}
                  </span>
                </div>
                <span>{log.createdAt}</span>
              </div>
              <p>操作者：{log.actorUserId ?? '系统'} / 角色快照：{log.actorRoleSnapshot || 'N/A'}</p>
            </article>
          ))}
        </div>
      </div>
    );
  }

  function renderTrending() {
    return (
      <div className="admin-section">
        <div className="admin-section__header">
          <div>
            <p className="eyebrow">Trending Ops</p>
            <h2>热议话题运营</h2>
          </div>
          <button className="secondary-button secondary-button--compact" type="button" onClick={() => void loadData()}>
            刷新
          </button>
        </div>

        {canManageTrending ? (
          <section className="admin-card">
            <div className="admin-card__header">
              <div>
                <strong>规则配置</strong>
                <span>支持归并、隐藏、置顶和手动排序。</span>
              </div>
            </div>
            <div className="field-grid">
              <label className="field">
                <span>话题键</span>
                <input
                  value={ruleForm.topicKey ?? ''}
                  onChange={(event) => setRuleForm((current) => ({ ...current, topicKey: event.target.value }))}
                  placeholder="例如：樱花预约"
                />
              </label>
              <label className="field">
                <span>展示名</span>
                <input
                  value={ruleForm.displayName ?? ''}
                  onChange={(event) => setRuleForm((current) => ({ ...current, displayName: event.target.value }))}
                  placeholder="为空则默认沿用话题键"
                />
              </label>
              <label className="field">
                <span>归并到</span>
                <input
                  value={ruleForm.mergeTargetKey ?? ''}
                  onChange={(event) => setRuleForm((current) => ({ ...current, mergeTargetKey: event.target.value }))}
                  placeholder="例如：樱花季预约"
                />
              </label>
              <label className="field">
                <span>排序值</span>
                <input
                  type="number"
                  value={String(ruleForm.sortOrder ?? 0)}
                  onChange={(event) =>
                    setRuleForm((current) => ({ ...current, sortOrder: Number(event.target.value || 0) }))
                  }
                />
              </label>
            </div>
            <label className="checkbox-field">
              <input
                type="checkbox"
                checked={Boolean(ruleForm.hidden)}
                onChange={(event) => setRuleForm((current) => ({ ...current, hidden: event.target.checked }))}
              />
              <span>隐藏该话题</span>
            </label>
            <label className="checkbox-field">
              <input
                type="checkbox"
                checked={Boolean(ruleForm.pinned)}
                onChange={(event) => setRuleForm((current) => ({ ...current, pinned: event.target.checked }))}
              />
              <span>将该话题置顶</span>
            </label>
            <div className="admin-card__actions">
              <button
                className="primary-button"
                type="button"
                disabled={isBusy('save-trending-rule')}
                onClick={() => void submitTrendingRule()}
              >
                保存规则
              </button>
            </div>
          </section>
        ) : null}

        {!trendingTopics.length ? <p className="search-empty">当前还没有可管理的热议数据。</p> : null}

        <div className="admin-list">
          {trendingTopics.map((topic) => (
            <article key={topic.topicKey} className="admin-card">
              <div className="admin-card__header">
                <div>
                  <strong>#{topic.displayName}</strong>
                  <span>{topic.topicKey}</span>
                </div>
                <span className={`admin-badge admin-badge--${topic.hidden ? 'banned' : 'active'}`}>
                  {topic.hidden ? 'HIDDEN' : 'LIVE'}
                </span>
              </div>
              <p>
                帖子数 {topic.postCount} · 互动 {topic.interactionCount} · 作者数 {topic.uniqueAuthorCount} · 分数{' '}
                {topic.score}
              </p>
              <p>
                {topic.mergeTargetKey ? `归并目标：${topic.mergeTargetKey}` : '未设置归并目标'}
                {topic.pinned ? ' · 已置顶' : ''}
                {topic.sortOrder ? ` · 排序 ${topic.sortOrder}` : ''}
              </p>
            </article>
          ))}
        </div>
      </div>
    );
  }

  function renderAnnouncements() {
    return (
      <div className="admin-section">
        <div className="admin-section__header">
          <div>
            <p className="eyebrow">Announcements</p>
            <h2>校园公告管理</h2>
          </div>
          <button className="secondary-button secondary-button--compact" type="button" onClick={() => void loadData()}>
            刷新
          </button>
        </div>

        {canManageAnnouncements ? (
          <section className="admin-card">
            <div className="admin-card__header">
              <div>
                <strong>{editingAnnouncementCode ? '编辑公告' : '新建公告'}</strong>
                <span>支持摘要、正文、发布时间、过期时间和登录弹窗策略。</span>
              </div>
            </div>
            <div className="field-grid">
              <label className="field">
                <span>标题</span>
                <input
                  value={announcementForm.title}
                  onChange={(event) => setAnnouncementForm((current) => ({ ...current, title: event.target.value }))}
                />
              </label>
              <label className="field">
                <span>分类</span>
                <input
                  value={announcementForm.category}
                  onChange={(event) =>
                    setAnnouncementForm((current) => ({ ...current, category: event.target.value }))
                  }
                />
              </label>
            </div>
            <label className="field">
              <span>摘要</span>
              <textarea
                rows={3}
                value={announcementForm.summary}
                onChange={(event) => setAnnouncementForm((current) => ({ ...current, summary: event.target.value }))}
              />
            </label>
            <label className="field">
              <span>正文</span>
              <textarea
                rows={6}
                value={announcementForm.content}
                onChange={(event) => setAnnouncementForm((current) => ({ ...current, content: event.target.value }))}
              />
            </label>
            <div className="field-grid">
              <label className="field">
                <span>发布时间</span>
                <input
                  type="datetime-local"
                  value={announcementForm.publishedAt ?? ''}
                  onChange={(event) =>
                    setAnnouncementForm((current) => ({ ...current, publishedAt: event.target.value }))
                  }
                />
              </label>
              <label className="field">
                <span>结束时间</span>
                <input
                  type="datetime-local"
                  value={announcementForm.expireAt ?? ''}
                  onChange={(event) => setAnnouncementForm((current) => ({ ...current, expireAt: event.target.value }))}
                />
              </label>
            </div>
            <label className="checkbox-field">
              <input
                type="checkbox"
                checked={announcementForm.pinned}
                onChange={(event) => setAnnouncementForm((current) => ({ ...current, pinned: event.target.checked }))}
              />
              <span>首页置顶展示</span>
            </label>
            <label className="checkbox-field">
              <input
                type="checkbox"
                checked={announcementForm.popupEnabled}
                onChange={(event) =>
                  setAnnouncementForm((current) => ({ ...current, popupEnabled: event.target.checked }))
                }
              />
              <span>登录后弹窗投放</span>
            </label>
            <label className="checkbox-field">
              <input
                type="checkbox"
                checked={announcementForm.popupOncePerSession}
                onChange={(event) =>
                  setAnnouncementForm((current) => ({ ...current, popupOncePerSession: event.target.checked }))
                }
              />
              <span>每个会话仅展示一次</span>
            </label>
            <div className="admin-card__actions">
              <button
                className="primary-button"
                type="button"
                disabled={isBusy(editingAnnouncementCode || 'create-announcement')}
                onClick={() => void submitAnnouncement()}
              >
                {editingAnnouncementCode ? '保存修改' : '创建公告'}
              </button>
              {editingAnnouncementCode ? (
                <button className="secondary-button" type="button" onClick={resetAnnouncementEditor}>
                  取消编辑
                </button>
              ) : null}
            </div>
          </section>
        ) : null}

        {!announcements.length ? <p className="search-empty">当前还没有公告数据。</p> : null}

        <div className="admin-list">
          {announcements.map((item) => (
            <article key={item.code} className="admin-card">
              <div className="admin-card__header">
                <div>
                  <strong>{item.title}</strong>
                  <span>
                    {item.category} · {item.code}
                  </span>
                </div>
                <span className={`admin-badge admin-badge--${item.status.toLowerCase()}`}>{item.status}</span>
              </div>
              <p>{item.summary}</p>
              <p>
                {item.pinned ? '已置顶' : '普通公告'}
                {item.popupEnabled ? ' · 已开启弹窗' : ''}
                {item.popupOncePerSession ? ' · 单次会话投放' : ''}
              </p>
              <p>
                {item.publishedAt ? `发布时间：${item.publishedAt}` : '未设置发布时间'}
                {item.expireAt ? ` · 截止：${item.expireAt}` : ''}
              </p>
              <div className="admin-card__actions">
                {canManageAnnouncements ? (
                  <button className="mini-button" type="button" onClick={() => void startEditAnnouncement(item)}>
                    编辑
                  </button>
                ) : null}
                {canPublishAnnouncements && item.status !== 'PUBLISHED' ? (
                  <button
                    className="mini-button"
                    type="button"
                    disabled={isBusy(`${item.code}-publish`)}
                    onClick={() =>
                      void runAction(`${item.code}-publish`, async () => {
                        await publishAnnouncement(item.code);
                      })
                    }
                  >
                    发布
                  </button>
                ) : null}
                {canPublishAnnouncements && item.status === 'PUBLISHED' ? (
                  <button
                    className="mini-button"
                    type="button"
                    disabled={isBusy(`${item.code}-offline`)}
                    onClick={() =>
                      void runAction(`${item.code}-offline`, async () => {
                        await offlineAnnouncement(item.code);
                      })
                    }
                  >
                    下线
                  </button>
                ) : null}
              </div>
            </article>
          ))}
        </div>
      </div>
    );
  }

  return (
    <section className="container page-stack admin-page">
      <div className="admin-hero">
        <div>
          <p className="eyebrow">Admin Console</p>
          <h1>内容治理管理台</h1>
          <p className="auth-copy">这里集中处理举报、审计、用户封禁、角色分配、热议运营与校园公告投放。</p>
        </div>
      </div>

      <div className="admin-tabs" role="tablist" aria-label="管理台标签">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            className={`mini-button${activeTab === tab.key ? ' is-active' : ''}`}
            type="button"
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {error ? <p className="auth-error">{error}</p> : null}
      {loading ? <p className="search-empty">管理台数据加载中...</p> : null}

      {!loading && activeTab === 'reports' ? renderReports() : null}
      {!loading && activeTab === 'users' ? renderUsers() : null}
      {!loading && activeTab === 'roles' ? renderRoles() : null}
      {!loading && activeTab === 'audit' ? renderAudit() : null}
      {!loading && activeTab === 'trending' ? renderTrending() : null}
      {!loading && activeTab === 'announcements' ? renderAnnouncements() : null}
    </section>
  );
}
