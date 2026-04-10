# Global Superpowers Switch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the global HelloAGENTS setup under `C:\Users\xiewei\.codex` with a superpowers-only configuration while keeping a rollback backup.

**Architecture:** Back up the current global `.codex` targets first, then replace the global `AGENTS.md` entrypoint, remove global `helloagents` directories, and prune only `helloagents`-specific rules from `default.rules`. Finish with a verification scan of the same global entrypoints.

**Tech Stack:** Codex CLI, PowerShell 5.1, Markdown, `.rules` config

---

### Task 1: Back Up Global Targets

**Files:**
- Create: `C:\Users\xiewei\.codex\backup\<timestamp>\`
- Copy: `C:\Users\xiewei\.codex\AGENTS.md`
- Copy: `C:\Users\xiewei\.codex\skills\helloagents\`
- Copy: `C:\Users\xiewei\.codex\helloagents\`
- Copy: `C:\Users\xiewei\.codex\rules\default.rules`

- [ ] **Step 1: Create the timestamped backup directory**

```powershell
$ts = Get-Date -Format "yyyyMMddHHmmss"
$backupRoot = Join-Path $env:USERPROFILE ".codex\backup\$ts"
New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null
```

- [ ] **Step 2: Copy the global files and directories into the backup**

```powershell
Copy-Item -LiteralPath "$env:USERPROFILE\.codex\AGENTS.md" -Destination (Join-Path $backupRoot "AGENTS.md") -Force
Copy-Item -LiteralPath "$env:USERPROFILE\.codex\rules\default.rules" -Destination (Join-Path $backupRoot "default.rules") -Force
Copy-Item -LiteralPath "$env:USERPROFILE\.codex\skills\helloagents" -Destination (Join-Path $backupRoot "skills-helloagents") -Recurse -Force
Copy-Item -LiteralPath "$env:USERPROFILE\.codex\helloagents" -Destination (Join-Path $backupRoot "helloagents") -Recurse -Force
```

- [ ] **Step 3: Verify the backup exists**

Run: `Get-ChildItem -Path "$env:USERPROFILE\.codex\backup\<timestamp>" -Force`
Expected: backup directory contains `AGENTS.md`, `default.rules`, and the copied `helloagents` directories

### Task 2: Replace Global AGENTS Entrypoint

**Files:**
- Modify: `C:\Users\xiewei\.codex\AGENTS.md`

- [ ] **Step 1: Write the new minimal global AGENTS content**

```markdown
<!-- bootstrap: lang=zh-CN; encoding=UTF-8 -->

# Global Codex Preferences

## Language

- 默认使用简体中文回复
- 代码标识符、API 名称和必要技术术语保持原文

## Working Style

- 保持直接、务实、面向交付
- 先检查上下文，再修改代码或配置
- 对破坏性操作先备份，再执行
- 需要时优先使用已安装的 `superpowers` 技能

## Skills

- 当前全局配置不再注入 `HelloAGENTS`
- 如任务匹配 `superpowers` 技能，按技能说明执行
```

- [ ] **Step 2: Re-read the file to verify the old HelloAGENTS header is gone**

Run: `Get-Content -Path "$env:USERPROFILE\.codex\AGENTS.md" -Encoding UTF8`
Expected: contains `# Global Codex Preferences` and no `HelloAGENTS`

### Task 3: Remove Global Helloagents Directories

**Files:**
- Delete: `C:\Users\xiewei\.codex\skills\helloagents\`
- Delete: `C:\Users\xiewei\.codex\helloagents\`

- [ ] **Step 1: Confirm the target directories exist before deletion**

Run: `Get-ChildItem -LiteralPath "$env:USERPROFILE\.codex\skills\helloagents","$env:USERPROFILE\.codex\helloagents" -Force`
Expected: both targets resolve successfully

- [ ] **Step 2: Delete the backed-up helloagents directories**

```powershell
Remove-Item -LiteralPath "$env:USERPROFILE\.codex\skills\helloagents" -Recurse -Force
Remove-Item -LiteralPath "$env:USERPROFILE\.codex\helloagents" -Recurse -Force
```

- [ ] **Step 3: Verify the directories are gone**

Run: `Test-Path -LiteralPath "$env:USERPROFILE\.codex\skills\helloagents"; Test-Path -LiteralPath "$env:USERPROFILE\.codex\helloagents"`
Expected: both commands return `False`

### Task 4: Prune Helloagents-Specific Rules

**Files:**
- Modify: `C:\Users\xiewei\.codex\rules\default.rules`

- [ ] **Step 1: Remove only rules that contain `helloagents`**

```powershell
$rules = Get-Content -Path "$env:USERPROFILE\.codex\rules\default.rules" -Encoding UTF8
$filtered = $rules | Where-Object { $_ -notmatch 'helloagents' }
Set-Content -Path "$env:USERPROFILE\.codex\rules\default.rules" -Value $filtered -Encoding UTF8
```

- [ ] **Step 2: Verify no `helloagents` lines remain in `default.rules`**

Run: `Select-String -Path "$env:USERPROFILE\.codex\rules\default.rules" -Pattern "helloagents"`
Expected: no output

### Task 5: Verify Global Switch State

**Files:**
- Read: `C:\Users\xiewei\.codex\AGENTS.md`
- Read: `C:\Users\xiewei\.codex\rules\default.rules`

- [ ] **Step 1: Scan the key global entrypoints for leftover references**

Run: `rg -n "helloagents|HelloAGENTS" "$env:USERPROFILE\.codex\AGENTS.md" "$env:USERPROFILE\.codex\rules\default.rules"`
Expected: no matches

- [ ] **Step 2: Confirm the backup location for rollback**

Run: `Get-ChildItem -Path "$env:USERPROFILE\.codex\backup\<timestamp>" -Force`
Expected: backup contents are still present for rollback
