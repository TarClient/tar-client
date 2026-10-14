# Tar Minecraft verifier

Live on Netlify **Free** Functions at `https://tar-client-verifier.netlify.app/.netlify/functions/mojang`. A real Mojang proof and the integrated badge flow passed on 14 October 2026. Keep the Free plan; do not add payment details or enable credit recharge. At the plan's hard usage limit, verification becomes unavailable until quota resets.

The existing Cloudflare service retains challenges, sessions, presence and rank records. Only its outbound Mojang lookups move here. This function accepts two tightly validated operations (`session` and `profile`), uses fixed official Mojang endpoints, disables redirects, imposes request/response limits and an eight-second timeout, and returns only UUID/name. It does not accept Minecraft or Microsoft access tokens, URLs, arbitrary headers or rank mutations. No application logging or persistent storage is used here. Netlify can retain its own operational metadata.

Set a random 32-byte hex `TAR_VERIFIER_SECRET` in Netlify's environment (Free uses all scopes). Configure the identical secret in Cloudflare as a secret binding, never in source, a launcher, command arguments or GitHub. Deploy this directory using `netlify.toml`. Production visibility must allow requests without a Netlify team login; previews can stay private. The function itself always requires `Authorization: Bearer <server secret>` over HTTPS. Access without that secret fails closed.

Test with `node --test verifier.test.mjs`. A live test must check an authenticated Mojang proof, not just that a static page loads. Preserve the old endpoint and rank database, and enable the new lookup only after testing. A free-host quota failure must never grant a session or relax the owner check.
