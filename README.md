# Tar Client 1.0.1

A free, open-source Windows launcher for **Minecraft Java 1.21.11 / Fabric 0.19.5**, with the bundled Tar Client cosmetic and HUD mod. Published by **Tarre Industries**, a project name rather than a registered company or verified Windows publisher.

## Download and open

Download **TarClient-1.0.1.exe** from [Releases](https://github.com/prutprut2003-creator/tar-client/releases/tag/v1.0.1). You can put this file on your desktop and double-click it. It contains the full launcher and Java runtime, installs them under `%LOCALAPPDATA%/Programs/Tar Client`, and creates a **Tar Client** desktop shortcut. No administrator access is needed. Subsequent starts reuse the installed files. Previous versions are retained for rollback.

The optional **TarClient-1.0.1-Windows.zip** contains the same application. Extract the whole ZIP and keep its `app` and `runtime` folders beside the inner `Tar Client.exe`. The small EXE inside that ZIP is not the self-contained EXE above.

**Windows signing is still unfinished.** These downloads are unsigned. Smart App Control may block them, and a Microsoft review of an older file does not automatically approve a new build. See [WINDOWS-SIGNING.md](WINDOWS-SIGNING.md). Renaming files, moving them or using a shortcut does not establish publisher trust.

Open Accounts and sign in through Microsoft's official page. The public application ID is included: there is nothing to paste. New sign-ins open a fresh private Edge/Chrome profile so another browser session does not choose your main account. Enter the email belonging to the account you want. If automatic browser opening is unavailable, use the copied URL in a fresh private window.

Windows encrypts remembered account sessions and refresh tokens for the current Windows user. Reopening Tar restores the selected account when Microsoft accepts its refresh token. Sign out/Forget removes that account from Tar's saved list. Expired, revoked or restricted accounts can still require sign-in. See [SIGN-IN-SETUP.md](SIGN-IN-SETUP.md) and [PRIVACY.md](PRIVACY.md).

Choose **Install / verify files**, then **Play Minecraft**. Minecraft, Fabric, assets and required mods download automatically. Minecraft Java ownership or a qualifying subscription is required for full play; **Try demo** starts Minecraft's own restricted demo. Java 21 is bundled. Default game memory is **8 GB**, adjustable in Settings; existing chosen memory settings are preserved.

## New in 1.0

- Fullbright blends the lightmap toward uniform white light, including unlit areas. Strength is adjustable; server light levels do not change.
- Your Tar logo appears in the launcher/icon and beside verified active Tar users in nametags and the player list.
- Owner-controlled Normal, Partner, Mod and Admin badges: white, light purple, light blue and light red.
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

Sign in to Minecraft as **Tarrecool**. Open **Right Shift > Tar player badges > Settings > Manage ranks**. Enter the recipient's Minecraft Java username and select Partner, Mod or Admin. Select Normal to remove their assigned rank. You retain the red owner badge. Rank changes appear after the next presence refresh, normally within 30 seconds.

The service verifies Minecraft identity through Mojang. Only owner UUID `ccb2c06282bc4afb86a71058b176dbef` can manage ranks; an Admin badge does not grant this permission. Ranks follow UUIDs, not changeable names. The service is deployed on the owner's free Cloudflare Workers account; no paid plan is required or enabled by the build.

Every signed-in Tar player automatically receives a white Normal badge while in a world; no rank request or separate signup is needed. Assigned ranks replace white with their rank color. The module is on by default; turning it off only hides badges on your own screen, and other Tar users can still see yours. Badge lookup covers the full player list in batches of 100. Presence normally refreshes within 30 seconds and expires after disconnect. Both players need Tar running, valid Minecraft sessions, and access to the community service. Only Tar users can see these badges. Existing server nametag visibility rules remain intact. An open-source client cannot cryptographically prove that its binary is unmodified; this is verified account presence, not anti-cheat attestation. Outages or exhausted free quotas temporarily hide badges without blocking Minecraft.

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

## 1.0.1 changes

**Right Shift > Visual > Totem Pop Size > Settings** controls your totem activation animation from 10% to 300%, in 5% steps. Enable the module to apply it; 100% is vanilla. Translation, spin and duration remain vanilla. Held totems and pop particles are unchanged. The separate Item Size module can still multiply the rendered item scale when enabled. Settings are saved in profiles.

Badge sharing now works independently of the local display toggle. Normal badges need no action from the owner, and players beyond the first 100 entries are included.
