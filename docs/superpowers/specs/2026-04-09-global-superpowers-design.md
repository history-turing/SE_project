# Global Superpowers Switch Design

## Goal

Replace the user's global `HelloAGENTS` configuration with a `superpowers`-only setup.

## Scope

- Update global entry file: `C:\Users\xiewei\.codex\AGENTS.md`
- Remove global `helloagents` skill and data directories:
  - `C:\Users\xiewei\.codex\skills\helloagents`
  - `C:\Users\xiewei\.codex\helloagents`
- Remove `helloagents`-specific rules from:
  - `C:\Users\xiewei\.codex\rules\default.rules`
- Leave project-local `helloagents/` directories unchanged in repositories unless explicitly requested later.

## Desired End State

- Global behavior is guided by `superpowers` skills instead of `HelloAGENTS`
- No global `.codex` entrypoint references `helloagents`
- Existing global non-`helloagents` rules remain intact
- A timestamped backup exists for rollback

## Approach

### Option A

Keep legacy `default.rules` entries and only rewrite `AGENTS.md`.

- Pros: minimal edits
- Cons: leaves stale global `helloagents` references behind

### Option B

Rewrite `AGENTS.md`, remove global `helloagents` directories, and clean `helloagents`-specific rules.

- Pros: clean switch with bounded risk
- Cons: requires destructive cleanup after backup

### Option C

Also delete historical session traces and temp references under `.codex`.

- Pros: most thorough
- Cons: higher risk, more likely to hit locks, low practical value

## Chosen Option

Option B.

## Implementation Notes

1. Create a timestamped backup directory under `C:\Users\xiewei\.codex\backup\`
2. Copy global files/directories slated for change into the backup
3. Replace `AGENTS.md` with a minimal global policy that:
   - keeps Chinese output preference
   - keeps pragmatic coding style
   - states that applicable `superpowers` skills should be used
   - removes all `HelloAGENTS` routing/output workflow
4. Delete the two global `helloagents` directories
5. Remove only `helloagents`-specific lines from `default.rules`
6. Re-scan `.codex` and verify the key entrypoints no longer reference `helloagents`

## Error Handling

- If a target is missing, continue and record it in the final summary
- If a file is locked, stop before partial destructive cleanup and report the blocker
- If backup creation fails, do not continue with deletion

## Rollback

Restore the modified/deleted targets from the timestamped backup directory.
