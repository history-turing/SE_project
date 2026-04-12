# Remote Deploy Baseline

This file records the stable operational facts for this project so future changes do not spend time rediscovering the same deployment and verification details.

## Target Environment

- Local development workspace: `d:\Desktop\SE_task\.worktrees\feature-rbac-moderation`
- Active feature branch: `feature-rbac-moderation`
- Remote server access path: `wsl.exe -d Ubuntu-22.04 --user xiewei -- ssh root@43.134.116.122`
- Remote feature deployment directory: `/root/SE_project_feature-rbac-moderation`
- Remote compose project name: `se_project`
- Remote env file: `/root/SE_project_feature-rbac-moderation/.env.production`

## Deployment Contract

- Never keep real MySQL, Redis, or SMTP secrets in tracked repository files.
- Repository templates stay in `.env.example`; real values live only in remote `.env.production`.
- Standard remote deployment entrypoint is:

```bash
cd /root/SE_project_feature-rbac-moderation
export TREEHOLE_DEPLOY_ENV_FILE=.env.production
export TREEHOLE_COMPOSE_PROJECT=se_project
bash scripts/deploy.sh
```

- `scripts/deploy.sh` now does the following in order:
  1. Starts `mysql` and `redis`
  2. Waits for both services to become healthy
  3. Applies `schema.sql` and `data.sql`
  4. Starts `backend` and `frontend`
  5. Waits for both services to become healthy

## Verification Baseline

- Primary remote health check:

```bash
cd /root/SE_project_feature-rbac-moderation
docker compose --env-file .env.production -p se_project ps
curl -sI http://127.0.0.1/ | head -n 1
```

- API smoke checks:

```bash
curl -s -H 'Authorization:Bearer verify-trending-announcement' http://127.0.0.1/api/v1/pages/home
curl -s -H 'Authorization:Bearer verify-trending-announcement' http://127.0.0.1/api/v1/topics/trending
curl -s -H 'Authorization:Bearer verify-trending-announcement' http://127.0.0.1/api/v1/admin/trending-topics
curl -s -H 'Authorization:Bearer verify-trending-announcement' http://127.0.0.1/api/v1/announcements
curl -s -H 'Authorization:Bearer verify-trending-announcement' http://127.0.0.1/api/v1/announcements/popup
```

- Temporary verification session bootstrap:

```bash
source .env.production
docker exec -i se_project_redis_1 redis-cli -a "$REDIS_PASSWORD" SETEX auth:session:verify-trending-announcement 604800 7
```

- Current verification user mapping:
  - username: `xiewei`
  - remote user id: `7`

## Known Pitfalls

- Do not verify backend health from the host with `http://127.0.0.1:8080`; backend is not published to the host and is only reachable inside the compose network.
- Use `docker compose ps` health status as the source of truth for container readiness.
- Remote server currently emits noisy WSL NAT warnings during SSH command execution. Treat them as transport noise unless the command exit code is non-zero.
- `frontend/dist`, `frontend/node_modules`, `frontend/*.tsbuildinfo`, and `GenHash.*` are local build artifacts or helper files and should not be tracked.
- `frontend/vite.config.js` is source code. Do not clean or revert it unless there is a real source diff to review.

## Local Verification Baseline

- Backend tests:

```bash
mvn -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-server -am test
```

- Frontend build:

```bash
npm run build
```

- Compose configuration check:

```bash
docker compose --env-file .env.example -f docker-compose.yml config
```
