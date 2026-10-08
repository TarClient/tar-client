# Privacy — Tar Client 1.0.3 signing candidate

Tar has no advertising, analytics SDK or automatic crash-report upload. Version 1.0 adds encrypted remembered accounts and a shared player-badge service.

## Microsoft accounts and local files

Microsoft, Xbox and Minecraft services handle sign-in, entitlement checks, profile restrictions and refresh tokens. Tar never asks for a Microsoft password. New sign-ins open an isolated private browser profile; normal browser cookies are not read or cleared. Temporary browser profiles are removed after that browser process exits when Windows permits it; an interrupted cleanup can leave a temporary `tar-microsoft-signin-*` folder.

Minecraft sessions and Microsoft refresh tokens are saved together in `accounts.dpapi`, encrypted by Windows DPAPI for the current Windows user. Preferences JSON and profiles contain no tokens. Launcher reopening refreshes the selected account online. Launcher and in-game account lists share the encrypted store. Forget/Sign out removes an entry; deleting the data folder removes all saved Tar accounts. Software running as the same Windows user may be able to decrypt DPAPI data, so it is not protection from malware already running as you.

Minecraft receives an access token in a temporary `.launch-*.args` file. Cleanup is attempted shortly after starting and on game exit; crashes can leave it behind. Do not share raw instance folders, account files or launch argument files. Minecraft manages its own profile-key cache, telemetry and network behavior. Account switching leaves the current world after confirmation and refreshes Minecraft's account services.

## Player badges and ranks

Before installation, the standalone EXE displays this policy and an optional **Share my Minecraft identity for Tar player badges** checkbox, checked by default. The ZIP/inner-launcher route displays the same policy and choice before normal startup. Cancel stops setup. Existing installations are asked once when they first run this version. Unattended installation does not enable sharing; the first interactive launch asks for a choice.

Change the choice at **Launcher Settings > Privacy and player badges**. The choice is stored outside module profiles in `badge-sharing.txt`, so loading a profile cannot undo an opt-out. Missing or invalid choices disable community requests. Turning sharing off stops new badge requests; an already-running request may finish, and a cleanup request can remove prior presence. Old presence expires within two minutes if cleanup fails. An opt-out also disables badge lookup and community rank-management requests until sharing is enabled again.

With sharing enabled and a signed-in player in a world, Tar sends the public Minecraft UUID/name and current player-list UUIDs, in batches of up to 100 IDs, to `tar-client-community.prutprut2003.workers.dev` on the maintainer's Cloudflare account. It does not send server addresses, coordinates, chat, inventory, passwords, Microsoft refresh tokens or Minecraft access tokens to that service. The **Tar player badges** module toggle only controls local display; use the separate privacy choice to disable sharing.


Minecraft identity is proven through Mojang's session service: the Minecraft access token goes only to Mojang's `/join` endpoint. The backend receives a public one-time challenge and checks it with Mojang. Community bearer tokens stay in game memory; only their hashes are stored by the backend. Tokens expire after six hours, challenges after one minute, and presence after two minutes. Expired records are cleaned on subsequent service requests. Leaving a world requests immediate presence removal; network failures fall back to expiry.

Active presence and assigned badge colors are visible to other Tar users requesting those UUIDs. Permanent rank records contain UUID, last assigned name, rank and update time until the owner removes them. Only Tarrecool's verified UUID can change ranks. The server retains no custom request logs. Cloudflare receives ordinary network metadata, including IP addresses; short-lived in-memory counters limit abuse. Cloudflare may retain its own operational/security data under its [privacy policy](https://www.cloudflare.com/privacypolicy/).

## Downloads and other network activity

Installation and launching contact Mojang/Microsoft and FabricMC for game metadata, libraries and assets. Mod discovery sends query text and compatibility filters to Modrinth and fetches icons from its CDN. Required-dependency resolution queries Modrinth using local JAR SHA-512 hashes, not the contents of local JARs. Downloads use upstream URLs and hash verification.

AppleSkin is automatically downloaded from Modrinth as the hunger-bar renderer. It can exchange hunger/saturation synchronization packets with compatible Minecraft servers; Tar adds no separate food-statistics service. Its display settings are controlled in memory from Tar profiles.

Optional integrations contact their own services; TierTagger uses its tier-list providers. Multiplayer servers and other mods have independent behavior. Provider policies: [Microsoft](https://privacy.microsoft.com/privacystatement), [Modrinth](https://modrinth.com/legal/privacy), and [FabricMC](https://fabricmc.net/) (metadata/download provider).

Game data defaults to `%LOCALAPPDATA%/TarClient`; app files install under `%LOCALAPPDATA%/Programs/Tar Client`. Profiles store module settings in the instance's `config/tar-profiles`; deleted profiles are retained in its `deleted` folder. The Server Address HUD can expose an address in screenshots. Streamer mode only masks supported coordinate displays, not chat, maps or unrelated mods. See README for uninstall and world-backup instructions.
