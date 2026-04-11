import { useEffect, useMemo, useState } from 'react';
import { useAuthContext } from '../context/AuthContext';
import {
  assignUserRole,
  banUser,
  deleteComment,
  deletePost,
  getAdminReports,
  getAdminRoles,
  getAdminUsers,
  getAuditLogs,
  resolveReport,
  restoreComment,
  restorePost,
  unbanUser,
} from '../services/api';
import type { AdminUser, AuditLog, ReportSummary, Role } from '../types';

type AdminTab = 'reports' | 'users' | 'roles' | 'audit';

function parseCommentTarget(targetCode: string) {
  const [postCode, commentCode] = targetCode.split(':');
  if (!postCode || !commentCode) {
    return null;
  }
  return { postCode, commentCode };
}

export function AdminPage() {
  const { hasPermission, hasRole } = useAuthContext();
  const [reports, setReports] = useState<ReportSummary[]>([]);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionKey, setActionKey] = useState('');

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
      ].filter((tab) => tab.visible),
    [hasPermission],
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
      const [nextReports, nextUsers, nextRoles, nextAuditLogs] = await Promise.all([
        hasPermission('report.read.any') ? getAdminReports() : Promise.resolve([]),
        hasPermission('user.ban') || hasPermission('user.unban') ? getAdminUsers() : Promise.resolve([]),
        hasPermission('role.assign.admin') ? getAdminRoles() : Promise.resolve([]),
        hasPermission('audit.read.moderation') || hasPermission('audit.read.all')
          ? getAuditLogs()
          : Promise.resolve([]),
      ]);

      setReports(nextReports);
      setUsers(nextUsers);
      setRoles(nextRoles);
      setAuditLogs(nextAuditLogs);
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
                    <span>{report.targetType} · {report.targetCode}</span>
                  </div>
                  <span className={`admin-badge admin-badge--${report.status.toLowerCase()}`}>{report.status}</span>
                </div>
                <p>原因：{report.reasonCode}{report.reasonDetail ? ` / ${report.reasonDetail}` : ''}</p>
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
                  <span>{user.username} · {user.userCode}</span>
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
                  <span>{log.targetType} · {log.targetCode}</span>
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

  return (
    <section className="container page-stack admin-page">
      <div className="admin-hero">
        <div>
          <p className="eyebrow">Admin Console</p>
          <h1>内容治理管理台</h1>
          <p className="auth-copy">这里集中处理举报、恢复内容、角色授权、封禁解封与审计查询。</p>
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
    </section>
  );
}
