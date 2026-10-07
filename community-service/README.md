# Tar community service

Deployment: https://tar-client-community.prutprut2003.workers.dev

Cloudflare Worker + one SQLite Durable Object, compatible with the free plan. No billing upgrade is performed. Free request/CPU/storage quotas apply; exceeding them makes badges unavailable. Minecraft can still run.

Only the verified Minecraft UUID `ccb2c06282bc4afb86a71058b176dbef` (Tarrecool at deployment) may assign/revoke ranks. Names are resolved by Mojang before assignment. Never add a client-provided owner flag, username-only authorization or shared admin secret. Admin is only a cosmetic badge.

Proof flow: challenge → client sends its token to Mojang `/join` → backend checks Mojang `/hasJoined` → six-hour community token. The backend stores only its SHA-256 hash. Challenges are one-use, including concurrent requests. Heartbeats refresh only the authenticated UUID for 120 seconds; rank reads return active players only. No Minecraft token is sent to Cloudflare.

`node --test worker.test.mjs` requires Node 24 (built-in SQLite). Tests use synthetic Minecraft identities, not live credentials. Deploy with `npx wrangler deploy` after authenticating to your own Cloudflare account. `wrangler.jsonc` declares the SQLite migration; keep the migration tag stable for code-only redeploys. The public client endpoint is in `CommunityService.java`.

Operational code is public; Cloudflare credentials are never committed. Observability request logging is disabled. See ../PRIVACY.md for retention and information sent by clients. A modified open-source client can implement this protocol; presence is not binary attestation. Cloudflare account access controls are separate from Minecraft rank management.
