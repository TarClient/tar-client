# 1.0 validation — 6 October 2026

- Launcher and client compile locally with Java 21. Structural checks pass for 28 remapped mixin classes, 36 injection targets and 23 shadow fields against Minecraft 1.21.11 and BetterF3.
- Local Java run: 39 of 41 tests passed; the existing desktop canonical-path test and new Windows DPAPI round-trip fail in this restricted environment. Both remain mandatory on Windows CI; no tests were skipped.
- Five backend tests passed using SQLite and synthetic Mojang proofs: spoofed identities, owner-only grants/revocation, Admin permission isolation, heartbeat identity/expiry, hashed expiring tokens and concurrent proof replay.
- Deployed the community Worker on the owner's free Cloudflare account. Live HTTPS health succeeds; unauthenticated rank/heartbeat requests return 401, invalid identity returns 400, and oversize request returns 413. Positive live Mojang authentication still requires an actual Minecraft account.
- Dark/light launcher pages were rendered and inspected. Logo and 8 GB fresh-install default are present. Backend and login behavior changes have not been verified in live multiplayer.
- Windows CI must pass Java tests, remapping, ZIP/standalone EXE packaging, native desktop copying and two starts of the moved self-contained desktop EXE with extracted file hashes checked.

Required real-machine checks: remembered login across restart; two real entitled accounts with a fresh email prompt; fullbright in unlit caves; new HUD panels and backgrounds at several GUI scales; zoom wheel reset; particle controls; TNT fuse timing; player badges on two clients and Tarrecool's rank changes. No full in-world rendering result is claimed. An unsigned EXE can still be blocked by Smart App Control.

Historical records follow.

# 0.4.1 validation

Added regression coverage for saved grid drawings, configuration migration, recoverable Skin Layers removal, 0.4.0 core upgrades, device-code polling/backoff, a different account profile, ownership rejection, cancellation and error-stage reporting. Authentication tests use synthetic responses, not real accounts. The native crosshair is kept intact when selected. Potion rendering targets the 1.21.11 renderStatusEffectOverlay method and uses Minecraft effect textures.

Local direct compilation succeeds. The remapped mixin checker validates 24 classes, 30 injection targets and 23 shadow fields against Minecraft 1.21.11 and BetterF3 17.0.0. Local DesktopInstaller testing remains subject to the Windows sandbox canonical-path restriction; use the Windows CI result for that check. No test is skipped to hide this restriction.

Required real-machine checks: use two entitled Microsoft accounts, including choosing another account in the browser; verify grid painting and saving in both editors; render beneficial, harmful and infinite potion effects at several GUI scales; confirm vanilla rendering returns when modules are disabled. Full game rendering and live accounts have not been verified in this environment.

The previous version's validation notes below are historical, not claims about the new build.

# 0.4.0 checks â€” 4 October 2026

Published [v0.4.0-preview](https://github.com/prutprut2003-creator/tar-client/releases/tag/v0.4.0-preview) from commit `0d855ebe923c8c4840d581c1e74053d418aafa3f`.
[Windows CI run 37214352351](https://github.com/prutprut2003-creator/tar-client/actions/runs/37214352351) passed in 2m24s. All **25 tests passed with zero failures and zero skips**. The actual packaged native EXE created a desktop shortcut and copied the application/runtime; the workflow verified its target and installed file hashes. The exact CI ZIP was downloaded, checksum-verified and published (61,980,685 bytes).

SHA-256: `0eb34f021a489551c8ea32a52a9773c985efdfa06d60334f71bcaad5a5018c22`.

The first Windows test found a containment bug involving directory aliases; resolving both source and destination ancestors fixed it. A subsequent native-installer timeout was addressed by isolating PowerShell progress output and explicitly exiting unattended mode. The final workflow passed both checks; neither test was disabled.

The isolated Minecraft smoke run verified 84 libraries and 4,591 assets, then failed before Tar initialization at Fabric LoaderUtil.normalizeExistingPath / WindowsPath.toRealPath with AccessDeniedException. Full gameplay and authenticated account switching remain unverified. The Home and Accounts launcher pages were rendered and inspected.

Windows App Control blocked the new unsigned EXE on the publisher's local machine. The full CI app was copied to a per-user application directory and a desktop shortcut created, with 424 file hashes matching, but the shortcut does not resolve that Windows trust block. No protection was disabled or bypassed.

Local checks performed before the release build:

- Java 21 launcher and Fabric client compilation passed locally.
- Structural bytecode verification passed for 24 mixins/accessors, 29 injection targets and 23 shadow fields against Minecraft 1.21.11 and the actual BetterF3 17.0.0 JAR. Realms/splash setters and the Realms constructor invoker were also checked.
- Local JUnit run: 24/25 tests passed. The desktop-copy test was blocked by the sandbox's WindowsPath.toRealPath AccessDeniedException in its temporary source folder. This is retained as a required test for the unrestricted Windows CI runner; it was not disabled or bypassed.
- Tests cover randomized coordinate formatting and removal of chunk/region positions, preserving unrelated F3 text, streamer profile persistence, rejecting expired/demo sessions, full desktop-copy layout, repeated installations, incomplete-package rejection, safe quoting of paths and 0.3.1 upgrade backup.
- Microsoft allowlist approval was confirmed from the publisher's 21 September review-completion email. No live login, authenticated server reconnection, Realms switch, chat-signing or in-world visual test is claimed.

Earlier release records follow.

# 0.3.1 checks â€” 29 September 2026

Published [v0.3.1-preview](https://github.com/prutprut2003-creator/tar-client/releases/tag/v0.3.1-preview) from commit `d961ba473eeae4849136703b8f641ec65265eb3f`.
[Windows CI run 36562964397](https://github.com/prutprut2003-creator/tar-client/actions/runs/36562964397) passed in 2m30s, including 19 tests with zero failures/skips. The exact CI ZIP was downloaded, checksum-verified and published (61,938,571 bytes).

SHA-256: `27d656d0ce173da55ad7c7c50eb332de8056d28ee590abb9038f88eb9d71961a`.

The fresh isolated game smoke run verified 84 libraries and 4,591 assets, then Fabric failed at `LoaderUtil.normalizeExistingPath` / `WindowsPath.toRealPath` with AccessDeniedException for lz4-java, before Tar initialization. No in-world rendering result is claimed.


- Java 21 launcher and client compilation passed.
- All 19 launcher/config tests passed, including new custom-profile duplicate protection, persistence of armor/motion-blur settings, old Spotify configuration removal, four armor directions and 0.3.0 core upgrade backup.
- All 18 remapped mixins passed target verification (26 injections and 12 shadow fields) against Minecraft 1.21.11.
- Smooth Motion Blur 1.0.0's actual JAR API was inspected: public config enabled/strength/pauseInGuis, save(), renderer reset(). Upstream strength is clamped to 0.05-5.0, corresponding to display values 1-100.
- Six motion-blur bridge state-transition checks passed using an upstream-API fixture: initial apply, idle without repeated writes, upstream shortcut changes, disabling/resetting history, upstream re-enable and profile switching. This checks synchronization logic, not the GPU renderer.
- The actual Swing Profiles page was rendered and visually inspected. Packaged EXE/runtime/JAR contents and complete removal of Spotify classes/resources were verified.
- Full in-world rendering and live GPU blur remain unverified. Earlier sandbox Fabric startup limitations are recorded below.
- Microsoft login approval status has not been rechecked during this update.

Earlier release records follow; Spotify checks below apply only to 0.3.0. The Spotify module and helper have been removed in 0.3.1.

# Verification record â€” updated 20 September 2026

## 0.3.0 local verification (20 September)

- 15 launcher/configuration tests passed with zero failures: profiles, traversal rejection, preset preservation, old-setting migration and prior-core backups are covered alongside the previous tests.
- Client and launcher compile with Java 21 against Minecraft 1.21.11 Yarn mappings. Remapped bytecode validation passed for 18 mixins, 26 injection targets and 12 shadow fields.
- Inspected the camera bytecode and hooked both its normal and minecart paths for freelook. This is a structural check, not a gameplay test.
- All four optional integrations were actually downloaded through the production ModManager, SHA-512 verified and dependency-checked. Disabling and re-enabling all four also passed preflight. Metadata is compatible with Fabric 1.21.11.
- The launcher module page was rendered and inspected at 1180x820.
- The isolated demo installation verified 84 classpath entries and 4,591 asset-index entries. Fabric startup was blocked by an environment AccessDeniedException in WindowsPath.toRealPath for a game library, before Tar initialization. No in-world/rendering/gameplay result is claimed.
- The Windows Spotify helper ran but the local media-session service returned 'specified service does not exist'. Its unavailable state is handled; live song/artwork and shape rendering remain unverified on a normal desktop.
- [Public Windows build 35531439667](https://github.com/prutprut2003-creator/tar-client/actions/runs/35531439667) passed all 15 tests, packaging and the native signature audit (Tar launcher remains unsigned). Source commit: `2e5ac655eaef7d32d0d73569f4ace2757f788d29`.
- The published ZIP is 61,930,901 bytes and matches SHA-256 `50c408d5a698ac9defcbe5886e6fa0036924af7868f54c31fffb007f76dd5329`. Its bundled runtime rendered the launcher successfully; the embedded public app ID, 0.3.0 mod metadata, 18 mixins and Spotify helper resource were verified in the downloaded artifact.
- [0.3.0 preview is published](https://github.com/prutprut2003-creator/tar-client/releases/tag/v0.3.0-preview). Minecraft app approval remains pending separately.

## 0.2.1 local verification (20 September)

- All 12 launcher tests passed, including fallback for missing/empty legacy application IDs, custom ID preservation and upgrading both previous bundled core versions without duplicates.
- The production Microsoft device-code request accepted the built-in Tar Client application ID and returned authorization fields. No account credentials or tokens were printed or saved by this check.
- The Accounts page was rendered and inspected: it reports that the connection is configured and Minecraft approval is pending, with no application ID setup required.
- The publisher's live sign-in attempt reached Minecraft Services but returned HTTP 403. Full sign-in and gameplay with a live session remain unverified. The separate approval request was submitted on 20 September 2026; the confirmation page acknowledged receipt. Approval is pending.

## 0.2.0 public hosted build

[GitHub Actions build 35458397484](https://github.com/prutprut2003-creator/tar-client/actions/runs/35458397484) passed on Windows Server 2022 with Temurin 21.0.10 and Gradle 9.3.0.
Source commit: `3b912a9b9af5b732150047d6c41b9e920e36eacb`.

- All 10 launcher tests passed, with 0 failures and 0 skipped tests.
- The normal Gradle/Loom pipeline built and remapped the client and packaged the Windows launcher.
- Downloaded Windows ZIP SHA-256 matched the build checksum: `2cc4eb6d62f1e70452d733cdb82cc3a5f11101afc5021958641574b221fd77a3`.
- Required runtime, launcher, bundled 0.2.0 core, keyboard hook and WebP decoder files were checked in the actual artifact.
- The bundled runtime and fat JAR passed the WebP fixture test, verifying image-reader service discovery after packaging.
- Full Minecraft gameplay, multiple monitors and Microsoft sign-in remain unverified as described below.

## 0.2.0 local verification (19 September)

- Launcher and Fabric client compile against the exact Minecraft 1.21.11 dependencies.
- All 10 JUnit tests pass, including an idempotent core upgrade that preserves worlds and unrelated mods, and a missing-bundle failure that leaves the previous installation intact.
- Production remapping completed. Bytecode checks pass for 12 mixin classes, 17 injection targets and 12 shadow fields, including the new keyboard hook.
- A native GLFW test invokes the actual WindowMixin transition code with position/size callbacks mutating its fields. Three fullscreen/windowed cycles passed at 3840x2160, checking geometry and decoration restoration. This uses a hidden native test window, not a Minecraft gameplay session; multiple monitors and DPI configurations remain unverified.
- Actual Swing components for Play, Client modules, Accounts and the live Modrinth catalog were rendered and visually inspected.
- Live Modrinth integration checked two distinct result pages, version filters, a performance-category search and WebP icon decoding. An original WebP fixture also passes through the packaged decoder in JUnit.
- Microsoft application registration and account login remain outstanding. This build contains no publisher client ID.
- The local compiler driver leaves its file manager open until process exit because the sandbox denies an extra ZipFS canonical-path traversal during close. Compilation itself succeeds; the hosted build above subsequently passed.

## 0.1.0 public hosted build

[GitHub Actions build 35425079764](https://github.com/prutprut2003-creator/tar-client/actions/runs/35425079764)
completed successfully on Windows Server 2022 with Temurin 21.0.10 and Gradle 9.3.0.
Source commit: `7360ecf14345ed9bb9a9c496846dead52b10487d`.

- The normal Gradle/Loom pipeline compiled the client and launcher, remapped the
  Fabric JAR, and created the Windows application image and ZIP.
- All 7 launcher tests passed; 0 failed and 0 were skipped.
- The downloaded artifact's SHA-256 matched the build-generated checksum:
  `f8b71ab8b51baa7c5417d43d19c5611229bcb210dff73abde95710a9e04a352b`.
- Required launcher/runtime files are present. The launcher EXE remains unsigned.

This hosted build resolves the local Gradle build limitation described below.
It does not test interactive game rendering, account login, or Smart App Control
acceptance. Those limitations remain.

The remaining sections record the earlier local verification of 18 September.

## Passed

- Java 21 compilation of the launcher and client sources against Minecraft Java
  1.21.11 (Yarn `1.21.11+build.6`) and Fabric API `0.141.6+1.21.11`.
- Production remapping of the client mod from named to intermediary mappings,
  including mixin annotations and shadow members.
- Bytecode validation of 11 mixin classes, 16 injection targets and 11 shadow
  fields. The checker verifies target names/descriptors, callback argument types,
  static/instance matching and the lightmap uniform invocation ordinal.
- Seven automated launcher tests: safe download paths; Mojang launch rules;
  configuration round-trip and bounds; Fabric version constraints; local mod
  import/version/duplicate checks; missing-dependency blocking; explicit demo
  sessions and token-safe session formatting.
- Live download/install integration against Mojang, Fabric and Modrinth:
  84 library/classpath entries and over 4,500 assets verified by checksum.
- Fabric API, Mod Menu, BetterF3, Cloth Config and Placeholder API installed for
  1.21.11, including nested-library dependency validation.
- Windows app image generated, containing an executable and OpenJDK 21.0.10
  runtime. The bundled `java.exe` reports the expected version.
- The seven tests also pass using that bundled runtime.
- The real Swing launcher UI was rendered to an image and visually inspected.

## Not verified

- Full Minecraft boot or world gameplay. The smoke test reaches Fabric Loader,
  which exits while canonicalizing a library path because this environment's
  Windows sandbox denies Java's parent-directory traversal. This occurs before
  Tar's mixins are loaded. It does **not** prove that the mods work in game.
- The packaged EXE did not expose a usable native window in this restricted
  environment. The UI image is an offscreen rendering of the actual Swing
  components, not evidence of a successful interactive desktop session.
- Microsoft/Xbox/Minecraft account login: no approved application client ID or
  account was supplied.
- Multiplayer latency against a live server, sound playback, full-screen monitor
  transitions, actual frame rendering and arbitrary extra-mod compatibility.

This is why the artifact is labeled a preview. No tests were disabled to claim a
successful gameplay run. The normal Gradle/Loom build also encountered the same
Windows canonical-path restriction; the delivered binaries were compiled and
remapped through a direct pipeline using the same official dependencies.

## Suggested real-machine acceptance check

1. Extract the ZIP and open Tar Client. Confirm navigation and settings persistence.
2. Run Install / verify, then Minecraft demo. Confirm the title screen and a world.
3. Open Right Shift. Toggle each feature, adjust its values, close and reopen the
   menu, then restart Minecraft to confirm persistence.
4. Drag HUD panels and resize the game window. Check FPS and potion countdowns.
5. In a suitable test world, wear nearly broken armor, verify warning timing and
   volume, then disable the sound and confirm it stops.
6. Check held, dropped, inventory, third-person and item-frame scales, including a
   filled map and a per-item override.
7. Press F3+B with outline customization enabled and disabled; verify collision
   behavior stays vanilla. Check the always-on option.
8. Test F11 on each monitor and return to windowed mode. Test shield, fire,
   fullbright, fog and glint in appropriate scenes.
9. Open Mod Menu and BetterF3 settings. Import a compatible local Fabric JAR,
   toggle it, and try a Modrinth install with dependencies.
10. Configure your registered Microsoft app, sign in yourself, launch the owned
    game and check your ping on a multiplayer server.
