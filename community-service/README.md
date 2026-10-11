# Tar community service

Deployment: https://tar-client-community.prutprut2003.workers.dev

## Authentication incident — 11 October 2026

Live diagnosis found Mojang `/join` returning HTTP 204 and `/hasJoined` returning HTTP 200 with a matching identity from the player's computer, while `/hasJoined` returned HTTP 403 to the Cloudflare Worker. The precise reason for Mojang's refusal is unconfirmed. `/health` being successful does not verify this outbound dependency. Badges and rank assignment remain affected; this is not resolved by the error-reporting change below.

The backend now distinguishes an upstream refusal/outage (HTTP 502, `mojang_lookup_failed`, with its numeric upstream status) from an absent proof (HTTP 401). Older clients report the 502 generically instead of wrongly describing it as an expired sign-in. Profile lookups similarly distinguish upstream failure from an unknown username. Requests identify Tar Client, accept JSON and refuse redirects. No alternate verification host, proxy, identity bypass or rank change was introduced. A valid Mojang proof remains mandatory, and only the verified owner may manage ranks.

Nine backend tests cover proof rejection/replay, owner-only ranks, presence, and upstream failures. Restoring authenticated service requires a supported server-side verification path that Mojang accepts. No new launcher binary is needed to receive these backend error-classification changes.

Cloudflare Worker + one SQLite Durable Object, compatible with the free plan. No billing upgrade is performed. Free request/CPU/storage quotas apply; exceeding them makes badges unavailable. Minecraft can still run.

Only the verified Minecraft UUID `ccb2c06282bc4afb86a71058b176dbef` (Tarrecool at deployment) may assign/revoke ranks. Names are resolved by Mojang before assignment. Never add a client-provided owner flag, username-only authorization or shared admin secret. Admin is only a cosmetic badge.

Proof flow: challenge → client sends its token to Mojang `/join` → backend checks Mojang `/hasJoined` → six-hour community token. The backend stores only its SHA-256 hash. Challenges are one-use, including concurrent requests. Heartbeats refresh only the authenticated UUID for 120 seconds; rank reads return active players only. No Minecraft token is sent to Cloudflare.

`node --test worker.test.mjs` requires Node 24 (built-in SQLite). Tests use synthetic Minecraft identities, not live credentials. Deploy with `npx wrangler deploy` after authenticating to your own Cloudflare account. `wrangler.jsonc` declares the SQLite migration; keep the migration tag stable for code-only redeploys. The public client endpoint is in `CommunityService.java`.

Operational code is public; Cloudflare credentials are never committed. Observability request logging is disabled. See ../PRIVACY.md for retention and information sent by clients. A modified open-source client can implement this protocol; presence is not binary attestation. Cloudflare account access controls are separate from Minecraft rank management.
