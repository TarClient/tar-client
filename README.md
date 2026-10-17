# Tar Client

> **Latest recommended version: [V1.0 (v1.0.0)](https://github.com/TarClient/tar-client/releases/tag/v1.0.0).**
> Use V1.0 for now. Versions [1.0.1](https://github.com/TarClient/tar-client/releases/tag/v1.0.1), [1.0.2](https://github.com/TarClient/tar-client/releases/tag/v1.0.2) and [1.0.3](https://github.com/TarClient/tar-client/releases/tag/v1.0.3) are available again as optional downloads. They have reported issues and remain unsigned; restoring their downloads does not fix Windows Smart App Control blocks.
>
> This repository also contains newer development work. Features described below as 1.0.1 or later are not included in the recommended V1.0 download.

A free, open-source Windows launcher for **Minecraft Java 1.21.11 / Fabric 0.19.5**, with the bundled Tar Client cosmetic and HUD mod. Published by **Tarre Industries**, a project name rather than a registered company or verified Windows publisher.

## Optional 1.1.3: cape clipping correction

The segmented capes in 1.1.1 and 1.1.2 bent toward the torso, causing the lower artwork to disappear into the skin. This update reverses the root and hinge angles to bend outward and positions the attachment behind the jacket layer. All five Tar cape styles use the corrected motion. Artwork, accounts and preferences are unchanged. Geometry regression tests cover idle and running animation samples; live in-game visual confirmation remains pending.

This remains an unsigned preview. The automatic updater installs only newer stable releases, so install this preview manually. The existing Defender-review warning still applies.

## Optional 1.1.2: automatic launcher updates

The packaged Windows launcher checks GitHub's latest stable release at startup. A newer version downloads automatically, verifies its size and SHA-256 digest from GitHub, installs beside the previous version and restarts. Accounts, worlds, mods and profiles remain in the existing data folder. An offline check or invalid download leaves the current launcher usable. Settings has an **Automatically update on startup** switch (enabled by default) and **Check and install update now**.

Only releases marked stable/latest by the maintainer are eligible: drafts, previews, equal versions and downgrades are excluded. V1.0 is currently the recommended stable release, so 1.1.2 will not downgrade to it. A later stable release above 1.1.2 will update automatically. Install this updater-enabled version manually once; older versions cannot gain the feature by themselves. Use the new desktop shortcut. Standalone EXEs from 1.1.2 onward also remember newer installations when reopened. Development launches and custom data/instance paths do not self-update.

Windows security checks remain in effect. The previous Defender detection remains unresolved by this feature; automatic updates do not make an unsigned release trusted. A blocked installer is not bypassed. Previous app installations are retained for recovery.

## Optional 1.1.1: detailed cosmetics

**Security notice:** The earlier 1.1.0 Windows EXE was detected by Microsoft Defender as Trojan:Win32/Wacatac.B!ml and submitted to Microsoft for review. The cause has not been established. This cosmetic update does not resolve or disprove that detection, and this new build has not been cleared by Microsoft. Leave blocked files quarantined; do not disable protection or use the ZIP to bypass a block. Executables remain unsigned. Build tests and matching checksums do not establish safety.

Higher-resolution cape artwork and 32-frame animations add a perched cherry-blossom bird, large black lightning on white, and an upright obsidian-framed Nether portal. Wings attach farther apart and use four connected animated panels. Ocean scenery, bandanas, horns, halos, caps and swords also have more detail. All cosmetics remain free.

Base decoded cosmetic textures use about 70 MiB if every style is loaded, excluding graphics-driver overhead. Live in-game and two-player visual checks remain pending; this is a prerelease.

## Optional 1.1.0: free Friends, emotes and cosmetics

Open **Friends & style** in the launcher or **Right Shift > Friends & style** in Minecraft. Send a request using a Minecraft Java username; the recipient must accept before either person can send messages. Select a person to accept, decline, remove, block or unblock. Messages are available in both interfaces while that page is open, with up to 100 recent messages per conversation. New messages refresh about every six seconds. Removing or blocking deletes that conversation. Messages expire after 30 days and are not end-to-end encrypted.

All **16 emotes** and **20 cosmetics** are free, including all wings and capes. There are no Tar Coin purchases, payment-provider accounts, bank connections or ID checks. This does not include the Minecraft game license. Donator remains a manually assigned badge.

**Cosmetics** lets you equip/remove one item in each slot: wings, head, face, hip and back. Your choices are saved to your verified Minecraft account. The launcher and game share the same outfit. **Emotes** plays an animation in game; use F5 to see yourself, or Stop emote to end it. Every emote and the Friends menu have configurable shortcuts under **Keybinds**. Emotes last 15 seconds, or two minutes for Sit, Lay and T-Pose. They animate the model only; flying emotes provide no flight or movement advantage. Swimming, crouching and other non-standing poses suppress emotes.

Other nearby Tar users running 1.1.0 see your outfit and emotes when player sharing is enabled in launcher Settings. Appearance synchronization runs about every five seconds for the nearest 99 players plus yourself. Disabled sharing hides your appearance, but you can still open Friends to use messages. Invisible/spectator players do not reveal cosmetics. These features require the free community service; exhausted free quotas or outages can delay/hide them. Existing ranks and normal badges remain unchanged.

The new accessories use original geometric models and procedural pixel-art textures: flapping wings, emissive halos, animated Ocean/Lightning/Blossom/Nether capes, and the free Tar-logo cape. No music is included. This is an optional prerelease; live two-player rendering and gameplay acceptance remain unverified. Windows executables remain unsigned.

## Optional 1.0.6: Theme and gradient color pickers

The **Theme** module controls orange button accents and the near-black menu background. Open **Right Shift > Utility > Theme > Settings**, then **Pick color**. The draggable gradient square chooses saturation/brightness, and the rainbow bar chooses hue. Backgrounds with transparency include an opacity bar and checkerboard preview. Hex entry is optional. **Apply** saves; **Cancel** or Escape preserves the previous setting. **Default** selects the setting's original color for you to apply. Tab and arrow keys work too; Shift increases the adjustment step.

Every configurable module color uses this picker, including hit color, crosshair, hitboxes, keystrokes, HUD text, potion timers, backgrounds and their gradient endpoints. The picker selects a color; existing HUD **gradient** background style uses the two separately selected endpoint colors. Other modules keep their existing rendering behavior.

Disable Theme for light menus. **Reset orange / black** restores the in-game defaults. The existing theme preferences and profiles are preserved under the same saved ID. HUD backgrounds and player badge colors remain separate.

Launcher **Settings** also has gradient pickers for its accent and background; save and restart to apply launcher colors. Launcher **Client modules > Theme** edits the game menu. All module color settings in the launcher use the same HSV picker, with opacity for ARGB settings.

The 1.0.5 search fix is included: filtering keeps the editing field, focus, caret and selection, with visible white text on black.

## Download and open

Download **[TarClient-1.0.0.exe](https://github.com/TarClient/tar-client/releases/download/v1.0.0/TarClient-1.0.0.exe)** from the [V1.0 release](https://github.com/TarClient/tar-client/releases/tag/v1.0.0). You can put this file on your desktop and double-click it. It contains the full launcher and Java runtime, installs them under `%LOCALAPPDATA%/Programs/Tar Client`, and creates a **Tar Client** desktop shortcut. No administrator access is needed. Subsequent starts reuse the installed files. Previous versions are retained for rollback.

The optional **[TarClient-1.0.0-Windows.zip](https://github.com/TarClient/tar-client/releases/download/v1.0.0/TarClient-1.0.0-Windows.zip)** contains the same application. Extract the whole ZIP and keep its `app` and `runtime` folders beside the inner `Tar Client.exe`. The small EXE inside that ZIP is not the self-contained EXE above.

**Windows signing is still unfinished.** These downloads are unsigned. Smart App Control may block them, and a Microsoft review of an older file does not automatically approve a new build. See [WINDOWS-SIGNING.md](WINDOWS-SIGNING.md). Renaming files, moving them or using a shortcut does not establish publisher trust.

Open Accounts and sign in through Microsoft's official page. The public application ID is included: there is nothing to paste. New sign-ins open a fresh private Edge/Chrome profile so another browser session does not choose your main account. Enter the email belonging to the account you want. If automatic browser opening is unavailable, use the copied URL in a fresh private window.

Windows encrypts remembered account sessions and refresh tokens for the current Windows user. Reopening Tar restores the selected account when Microsoft accepts its refresh token. Sign out/Forget removes that account from Tar's saved list. Expired, revoked or restricted accounts can still require sign-in. See [SIGN-IN-SETUP.md](SIGN-IN-SETUP.md) and [PRIVACY.md](PRIVACY.md).

Choose **Install / verify files**, then **Play Minecraft**. Minecraft, Fabric, assets and required mods download automatically. Minecraft Java ownership or a qualifying subscription is required for full play; **Try demo** starts Minecraft's own restricted demo. Java 21 is bundled. Default game memory is **8 GB**, adjustable in Settings; existing chosen memory settings are preserved.

## New in 1.0

- Fullbright blends the lightmap toward uniform white light, including unlit areas. Strength is adjustable; server light levels do not change.
- Your Tar logo appears in the launcher/icon and beside verified active Tar users in nametags and the player list.
- Normal, Partner, Mod, Admin, Donator, Co-Owner and Owner badges: white, light purple, light blue, light red, light green, light orange and orange.
- Dark/light launcher theme, plus a Dark mode module for Tar's game menus.
- TPS estimate, Particles, TNT timer, Mouse tracer, Item counter and custom Text modules.
- HUD panels support rounded, solid, gradient and outline backgrounds, with background visibility, colors and opacity controls. Potion icons can keep vanilla backgrounds.
- White default custom crosshair, vanilla mode and the existing 15×15 drawing editor.
- Scroll while holding Zoom to change magnification temporarily; releasing resets it.
- Item-size filters can include only listed item IDs or exclude them, alongside existing per-item scales.
- Delete profiles in either menu. Deleted settings move to a recoverable `config/tar-profiles/deleted` folder.
- Required Modrinth dependencies install automatically, including dependencies of recognized imported JARs. Unpublished local JARs still need their missing dependencies supplied manually.

The previous armor directions/customization, potion corner timers, streamer coordinates, account switching, motion-blur settings, profiles, keybinds and mod browser remain. Spotify and 3D skins remain removed.

Press **Right Shift** for modules, **Edit HUD** to move panels, and **Keybinds** to change controls. **Accounts** switches Minecraft accounts without restarting the game; switching asks before leaving a world/server. [MODULES.md](MODULES.md) describes every module and its limitations.

## Give ranks (Tarrecool only)

Sign in to Minecraft as **Tarrecool**. Open **Right Shift > Tar player badges > Settings > Manage ranks**. Enter the recipient's Minecraft Java username and select Partner, Mod, Admin, Donator or Co-Owner. Select Normal to remove their assigned rank. Your orange Owner badge is reserved for Tarrecool. Co-Owners also see Manage ranks, but can assign only Partner or Mod and cannot change Admins, other Co-Owners or the Owner. Revoking Co-Owner immediately removes server-side management access. All viewers need 1.0.4 for the new colors; older clients show Donators/Co-Owners in white and the Owner in red. Rank changes appear after the next presence refresh, normally within 30 seconds.

The service verifies Minecraft identity through Mojang. Only owner UUID `ccb2c06282bc4afb86a71058b176dbef` can manage ranks; an Admin badge does not grant this permission. Ranks follow UUIDs, not changeable names. The service is deployed on the owner's free Cloudflare Workers account; no paid plan is required or enabled by the build.

With badge sharing enabled in the installation privacy choice, every signed-in Tar player automatically receives a white Normal badge while in a world; no rank request or separate signup is needed. Assigned ranks replace white with their rank color. The module is on by default; turning it off only hides badges on your own screen, and other Tar users can still see yours. Badge lookup covers the full player list in batches of 100. Presence normally refreshes within 30 seconds and expires after disconnect. Both players need Tar running, valid Minecraft sessions, and access to the community service. Only Tar users can see these badges. Existing server nametag visibility rules remain intact. An open-source client cannot cryptographically prove that its binary is unmodified; this is verified account presence, not anti-cheat attestation. Outages or exhausted free quotas temporarily hide badges without blocking Minecraft.

## Mods and profiles

Discover mods searches Modrinth for Fabric 1.21.11 releases with sorting, filters and pagination. Required dependencies and hashes are checked before installation. Installed mods can import Fabric JARs, toggle, remove or resolve dependencies. Removing a mod moves it to `removed-mods`. Declared incompatibilities block launch. Metadata cannot guarantee that arbitrary third-party mods work together.

Close Minecraft before editing launcher module settings or installed mods. While playing, use the game menu. Profiles save all Tar module settings; Minecraft keybindings and detailed upstream-mod settings remain global. Bedwars and SMP presets are seeded once.

## Data and uninstall

Game data lives in `%LOCALAPPDATA%/TarClient/instance-1.21.11`, separate from the official `.minecraft` folder. It contains saves, screenshots, resource packs, mods and `config/tarclient.json`. Launcher preferences and encrypted `accounts.dpapi` live in `%LOCALAPPDATA%/TarClient`. Review logs before sharing; never share account files or `.launch-*.args` files.

To uninstall, close Minecraft and Tar, remove the desktop EXE/shortcut and `%LOCALAPPDATA%/Programs/Tar Client`. This leaves worlds intact. To remove game data too, first back up your worlds, then remove `%LOCALAPPDATA%/TarClient`. Use your selected locations instead if you configured custom data paths.

## Build and extend

Requires Windows x64, JDK 21, Gradle 9.3.0 and the Windows .NET Framework C# compiler. Run `./build-windows.ps1`. It tests Java code, builds/remaps the Fabric mod, packages Java, and embeds the complete ZIP in the movable EXE. The public-repository Windows workflow tests both desktop installation paths; it does not purchase services or automatically publish releases.

`launcher` is the desktop app, `client-mod` is the Fabric component, `common` contains shared settings/authentication, `bootstrap` builds the self-contained EXE, and `community-service` contains the rank backend. Add module definitions to `ClientConfig.MODULES` and implement their rendering/events in `client-mod`. Third-party JARs require no source changes.

The release JAR embeds the remapped mod at `bundled/tar-client.jar`. For isolated demo diagnostics, use `java -jar tar-launcher.jar --smoke C:/path/to/test-instance --launch`; never point it at a personal instance. `-Dtar.data=...` and `-Dtar.instance=...` override test locations. See [TESTING.md](TESTING.md) for what has actually been verified and the outstanding in-world checks.

Source is MIT licensed; dependencies retain their own licenses. See [THIRD-PARTY.md](THIRD-PARTY.md). Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.

## 1.0.1 changes (optional download; reported issues)

**Right Shift > Visual > Totem Pop Size > Settings** controls your totem activation animation from 10% to 300%, in 5% steps. Enable the module to apply it; 100% is vanilla. Translation, spin and duration remain vanilla. Held totems and pop particles are unchanged. The separate Item Size module can still multiply the rendered item scale when enabled. Settings are saved in profiles.

Badge sharing now works independently of the local display toggle. Normal badges need no action from the owner, and players beyond the first 100 entries are included.

## 1.0.2 changes (optional download; reported issues)

**Saturation** now uses the official [AppleSkin](https://modrinth.com/mod/appleskin) renderer on the vanilla hunger bar instead of a separate text panel. Tar automatically downloads AppleSkin and its required dependencies at launch; it stays loaded so toggles and profile changes work live. Open **Right Shift > HUD > Saturation > Settings** and enable it. Controls include current saturation, held-food hunger/saturation preview, offhand preview, exhaustion, health recovery, food tooltips, vanilla animations and preview opacity. Existing Saturation enabled states are preserved.

The overlay follows Minecraft GUI scale and hunger-bar visibility rather than the draggable HUD editor. Exact multiplayer saturation/exhaustion needs AppleSkin or compatible synchronization on the server; vanilla-only servers may expose incomplete values. Food previews are informational, not a change to hunger or healing mechanics. Tar controls the display settings in memory while retaining the original AppleSkin config file.

## Code signing policy

See [Code signing policy](CODE-SIGNING.md). SignPath Foundation declined the application because the project has not yet established sufficient public recognition. **No approval or signed release is claimed.** The recommended V1.0 download remains unsigned and may still be blocked by Windows. The optional 1.0.3 signing candidate adds a privacy screen and optional badge-sharing switch before installation, plus a persistent opt-out in launcher Settings. That switch is not included in V1.0.

## 1.0.4 badge update

Adds Donator (light green), Co-Owner (light orange), and the exclusive Owner (orange). Co-Owners can grant Partner/Mod only. Install 1.0.4 to see the new badge colors and management controls. This release remains unsigned; the badge update does not resolve Windows Smart App Control blocks.
