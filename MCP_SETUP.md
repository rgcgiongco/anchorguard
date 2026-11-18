# Guardify — MCP Server Configuration

This project uses 8 MCP servers configured in `kilo.jsonc` (project) and `~/.config/kilo/kilo.jsonc` (global).

## Active servers (all connected)

| Server | Binary | Purpose | Plan sections |
|---|---|---|---|
| `android-pilot` | `android-pilot-mcp` | Gradle builds, device management, logcat, scaffolding | §1.2, §11.1 |
| `androidbuild` | `androidbuild-mcp` | Gradle builds, emulator management, adb, UI automation | §1.2, §11.1 |
| `firebase` | `firebase mcp` | Firebase projects, Auth, Firestore, FCM | §1.1, §4.8, §5.3, §6.2 |
| `docker` | `docker-mcp` | Container/image/network/volume management | §1.3 |
| `sqlite` | `mcp-sqlite` | SQLite file querying (Room schema validation) | §3.1 |
| `codegraph` | `codegraph serve --mcp` | Cross-language code intelligence graph | codebase exploration |
| `sequential-thinking` | `mcp-server-sequential-thinking` | Structured step-by-step reasoning | planning |
| `linux` | `system-monitoring-mcp-server` | Linux system/process/service monitoring | §1.3, §12 |
| `filesystem` | `mcp-server-filesystem /home/anchor/AnchorGuard` | Filesystem read/write access | general |

## Removed servers

| Server | Reason |
|---|---|
| `postman` | Requires `POSTMAN_API_KEY` — no key available |
| `github` | Requires `GITHUB_PERSONAL_ACCESS_TOKEN` — no token available |
| `postgres` | Requires `POSTGRES_CONNECTION_STRING` — no DB available |

## Setup

```bash
# All servers installed globally
npm install -g android-pilot-mcp @asjackson/androidbuild-mcp firebase-tools \
  @swartdraak/docker-mcp-server @mokei/mcp-sqlite \
  @modelcontextprotocol/server-sequential-thinking \
  @supernova123/system-monitoring-mcp-server mcp-server-filesystem

# codegraph (separate install)
npm install -g @colbymchenry/codegraph
codegraph init   # build index for this project

# Verify
kilo mcp list
```

## Notes

- `filesystem` is restricted to `/home/anchor/AnchorGuard` — cannot access outside the project.
- `sqlite` defaults to `/home/anchor/AnchorGuard/guardify.db`.
- `codegraph` requires `codegraph init` to build the index before `codegraph_explore` returns useful results.
- To re-enable `postman`, `github`, or `postgres`, set the required env vars and change `enabled` to `true` in the config.