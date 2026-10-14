# Tar community service

Deployment: https://tar-client-community.prutprut2003.workers.dev

## Authentication restored — 14 October 2026

On 11 October, Mojang `/join` returned HTTP 204 and `/hasJoined` returned HTTP 200 with a matching identity from the player's computer, while `/hasJoined` returned HTTP 403 to the Cloudflare Worker. The precise reason for that refusal remains unconfirmed. `/health` alone does not verify this outbound dependency.

The two Mojang lookups now run on the maintainer's Netlify Free function at `https://tar-client-verifier.netlify.app/.netlify/functions/mojang`. Cloudflare retains the existing database, public API, challenges and owner checks. A secret authenticates backend-to-verifier requests; it grants no player or rank authority. The verifier only accepts fixed session/profile operations and still requires Mojang's answer. It accepts no Microsoft/Minecraft access tokens and cannot fetch arbitrary URLs. Neither a missing secret nor an upstream failure creates a player session.

Live checks on 14 October passed: a real Minecraft session matched the selected UUID, community confirmation returned 200, owner rank-list access returned 200, and heartbeat returned the owner's badge. Test presence was removed afterward. No player's rank was modified. Sixteen automated tests cover owner-only writes, proof replay/mismatch, two-player badge presence, verifier authentication and failure cases. Rendering in two live game clients still needs an in-game check. Existing launchers use the fix automatically; no new binary is required.

The backend distinguishes upstream failures (502) from absent proofs (401). Older clients display a generic 502 rather than incorrectly reporting an expired sign-in. For rollback, remove `VERIFIER_URL` and redeploy; this restores direct Mojang lookups but may restore the original Cloudflare 403 failure. Keep the Durable Object and `v1` migration intact to preserve ranks.

Cloudflare Worker + one SQLite Durable Object + Netlify Free Function. No paid plan or billing upgrade is required. Free quotas apply; exceeding them makes badges unavailable. Netlify Free has a hard credit limit, not paid overages. Minecraft can still run.

Only the verified Minecraft UUID `ccb2c06282bc4afb86a71058b176dbef` (Tarrecool at deployment) may assign/revoke ranks. Names are resolved by Mojang before assignment. Never add a client-provided owner flag, username-only authorization or shared admin secret. Admin is only a cosmetic badge.

Proof flow: challenge → client sends its token to Mojang `/join` → backend checks Mojang `/hasJoined` → six-hour community token. The backend stores only its SHA-256 hash. Challenges are one-use, including concurrent requests. Heartbeats refresh only the authenticated UUID for 120 seconds; rank reads return active players only. No Minecraft token is sent to Cloudflare.

`node --test worker.test.mjs verifier/verifier.test.mjs` requires Node 24 (built-in SQLite). Tests use synthetic identities. Follow `verifier/README.md` to deploy Netlify, then set the same `TAR_VERIFIER_SECRET` using `wrangler secret put TAR_VERIFIER_SECRET` and deploy the worker with `npx wrangler deploy`. Never put the secret in `wrangler.jsonc` or the client. Keep migration tag `v1` stable for code-only redeploys. The public client endpoint is in `CommunityService.java`.

Operational code is public; Cloudflare credentials are never committed. Observability request logging is disabled. See ../PRIVACY.md for retention and information sent by clients. A modified open-source client can implement this protocol; presence is not binary attestation. Cloudflare account access controls are separate from Minecraft rank management.
