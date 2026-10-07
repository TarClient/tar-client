# Tar Client 1.0 module guide

Open **Client modules** in the launcher or press **Right Shift** in Minecraft.
Search by name, toggle a module and open its settings. **Edit HUD** lets you drag
visible HUD panels. Potion status is anchored to the top-right corner icons and has its own scale, padding, color and background controls. Other Tar HUD panels have a **Show background** switch, background
color/opacity, rounded/solid/gradient/outline style, second gradient color, roundness, text color, position and scale. Backgrounds are off by
default, including when upgrading old settings. Potion icons keep the vanilla background by default. Visual and utility modules that
do not draw a HUD have no panel background to configure.

## Crosshair and potion icons

**Custom crosshair:** Use original Minecraft crosshair is enabled by default, including upgrades. Turn it off to use the arm-size settings or a grid design. Open Draw crosshair in either launcher or in-game settings, paint/erase the 15x15 grid, and Save. Saving selects grid mode automatically. Clear makes a blank design; + Preset gives a simple white-style plus. Escape/Cancel discards editor changes. Color, outline and pixel scale apply to the saved drawing. Profiles keep the drawing.

**Potion status:** Countdown timers sit below the effect icons in the top-right corner; higher levels appear on the icons. Beneficial and harmful effects have separate rows, wrapping when necessary. Configure timer visibility, effect level, beneficial/harmful filters, scale, padding, normal/expiring text colors and vanilla icon background. The standalone names panel has been removed. Turn the module off to restore vanilla rendering. Inventory screens that show their own effects retain Minecraft's normal behavior.

**3D skins** has been removed; old configuration entries are discarded and installed Skin Layers JARs are backed up at the next launcher preparation.

## New built-in modules

| Module | Use and customization |
| --- | --- |
| Armor status | Use the Direction button for Right, Down, Left or Up. Horizontal or vertical strip. Reverse direction puts the first armor slot at the bottom/right. Set icon size, spacing, durability text visibility, bar visibility/width, optional durability colors, percentage/points and empty slots. Existing background, scale, position and warning-sound controls remain. |
| Shulker box tooltips | 9-column contents grid on hover, optional Shift requirement and grid background. Minecraft still draws its outer tooltip frame. Only item contents supplied by the game are shown. |
| Zoom | Enable, then hold **C**. Configure multiplier, smooth transition and scroll step; rebind in Minecraft Controls. While held, scroll to adjust zoom temporarily. Release to reset to your configured multiplier. |
| Freelook | Enable, then hold **Left Alt**. The camera enters third person and rotates independently while the player's aim stays unchanged. Releasing restores your prior perspective. Configure sensitivity and rebind in Controls. |
| Time changer | Enable, set the tick value, then press **Apply time** in the in-game module settings. Sends the normal `/time set` command. Requires server OP/command permission or singleplayer cheats; never changes time automatically. |
| Clock | Local computer time in AM/PM format, with optional seconds. |
| Inventory HUD | Main inventory grid, optional hotbar row and item count/durability overlays. |
| Saturation | Exact singleplayer saturation. Multiplayer is labeled as an estimate because vanilla does not continuously synchronize the actual value. |
| Hit color | Change the damage tint on player models. |
| Streamer mode | Random fake coordinates shared by Tar Coordinates, vanilla F3 and BetterF3. Use Randomize again or a bound key to choose a fresh fake position. Does not affect actual movement or hide coordinates in chat, maps or unrelated mods. |
| Coordinates | Block X/Y/Z and optional dimension label. Enable Show background for a coordinate box. |
| Reach display | Distance from your eyes to the target hitbox at the last local attack, with display timeout and player-only filter. Does not change reach or confirm that the server accepted damage. |
| Server address | Connected server address, optional server name and server-list image. A placeholder is used if Minecraft has no icon. |
| Profiles | Save and load all Tar module settings. Bedwars and SMP presets are created once and never overwrite your saved versions. Open Profiles directly in either sidebar. Enter any valid custom name and select Create profile to copy the current setup. Load restores it; Update replaces it after confirmation. Delete moves it to the recoverable deleted folder; deleted presets stay deleted. Names use 1-48 letters, numbers, spaces, underscores or hyphens, starting with a letter or number. Keyboard bindings remain global in Minecraft options. |
| Smart disconnect | A Leave/Cancel confirmation before disconnecting through the pause menu. Preserves Minecraft's draft-report flow. Does not prevent server kicks or closing the operating-system window. |
| Limit unfocused FPS | Set a limit from 5 to 120 FPS while the game is unfocused. Respects any lower vanilla limit. |

## Integrated mods

These three module cards use compatible upstream mods. Enable the card before
launching; Tar downloads it and its required dependencies from Modrinth. If you
change enabled state in game, **close and relaunch Minecraft through Tar** to
apply it. Motion blur can toggle live once installed. Profiles store enabled states and the Tar motion-blur settings; other detailed upstream settings remain global.

| Module | Provider and settings |
| --- | --- |
| TierTagger | [Official TierTagger](https://modrinth.com/mod/tiertagger), with ukulib. Displays published PvP tiers; use Mod settings / Mods for tier-list and display settings. |
| Motion blur | [Smooth Motion Blur](https://modrinth.com/mod/smooth-motion-blur). Tar Settings provides strength 1-100 and Pause blur in menus. Disable the module for zero blur. Strength uses the upstream display scale (20 = default). Upstream command/shortcut changes are synchronized back into Tar profiles. Its Increase/Decrease keys appear in Keybinds when installed. |
| Pack organizer | [Resource Tree](https://modrinth.com/mod/resource-tree-mod). The Resource Packs screen gains subfolder navigation and folder-management controls. Use the Resource Packs button in its integration screen. |

The three integrations start disabled and are not copied into the Tar ZIP.
Installed-mod toggles for these three are reconciled with their Tar module setting
at the next launch. Disable them through **Client modules** when using Tar.

## Keybinds

Choose **Keybinds** in the Right Shift sidebar. This opens Minecraft's binding
editor, including all loaded mods. Click a binding, press a key or mouse button,
or press Escape to unbind it. Conflicts are marked by Minecraft. Bindings persist
in the game's options and are global rather than per profile.

Tar's menu defaults to Right Shift, zoom to C, and freelook to Left Alt. Optional
module toggles, Open profiles and Apply configured world time start unbound.
Toggles run only while playing and focused, never while typing in chat or menus.
Motion blur's strength shortcuts are provided by its installed mod. Minecraft's
Fullscreen binding controls F11; movement/attack bindings drive the keystrokes HUD.
F3+B is Minecraft's fixed debug shortcut: to use a custom hitbox key, enable
Hitbox outlines > Show without F3+B and assign Toggle Hitbox outlines.

## Verification limits

See [TESTING.md](TESTING.md) for compilation, tests, package and mixin checks.
The local environment has previously blocked Fabric startup during filesystem
path resolution before Tar initializes, so in-world rendering is not yet verified.
Mojang application review is approved; live account and rendering checks are still separate.

## Accounts and desktop installation

Right Shift > Accounts opens the Microsoft account switcher. Open accounts, Toggle Streamer mode and Randomize streamer coordinates have optional bindings in Keybinds. All start unbound. Switching confirms leaving the current world but keeps Minecraft open. In the launcher, Play > Install to desktop creates a desktop shortcut to a complete installed app copy. See README and SIGN-IN-SETUP for details.

## Added in 1.0

| Module / change | Behavior |
| --- | --- |
| Fullbright | At strength 1, blends the game lightmap toward white, including zero-light areas. Lower strengths blend with normal lighting. Does not place torches or change server light levels. Shader/renderer replacements can require compatibility testing. |
| Tar player badges | Verified recent community presence adds your logo to visible nametags and the player list. Normal white, Partner purple, Mod blue, Admin red. Tarrecool sees Manage ranks under Settings. All signed-in players automatically share a white badge while in a world; disabling this module only hides badges locally. The entire player list is queried in batches of 100. |
| Dark mode | Switch Tar's in-game menus between dark and light palettes. Launcher Settings has its own theme switch, applied on restart. |
| TPS | Estimates 0–20 ticks per second from server time packets; choose averaging samples. Network delay affects the estimate. It is not direct server instrumentation. |
| Particles | Adjust overall quantity and crit, potion and other particle percentages independently, including zero. Disable module to restore vanilla particle spawning. Applies to vanilla ParticleManager effect creation. |
| TNT timer | Shows the fuse in seconds while looking at primed TNT within configured range and unobstructed by blocks. It uses the client entity's fuse, so unusual server plugins/lag can affect accuracy. |
| Mouse tracer | Configurable HUD trail of in-game mouse-look movement. Set panel size, sensitivity, trail duration, color and background. It does not collect desktop mouse activity. |
| Item counter | Counts configured item IDs in your inventory, including offhand. Comma-separated IDs, optional hiding zero counts, position, color and background. Up to 24 types. |
| Text | Your own HUD text, up to 12 lines. Type `\n` for a line break. Position, scale, color and panel style are customizable. |
| Item size filters | All items, Only listed or Except listed; use comma-separated IDs such as `minecraft:shield,minecraft:diamond_sword`. Filters take priority over per-item scale overrides. |

New information/visual modules start disabled, except Dark mode and Tar player badges. The crosshair's custom color now defaults to white with no outline; the original green default migrates once, while later custom color choices are preserved.

The movable download is **TarClient-1.0.1.exe**, which contains all app/runtime files. The small inner EXE from the optional ZIP still needs its sibling folders.

## 1.0.1

| Module | Controls and behavior |
|---|---|
| Totem Pop Size | Visual category. Enable to resize the totem activation animation from 10% to 300% in 5% steps; 100% is vanilla. Does not resize held items or particles, or alter animation timing/position. Item Size can additionally multiply the model scale. Saved per profile. |

Normal badges require no owner assignment. Every signed-in Tar player automatically advertises presence while playing, even if they hide badges locally. Both players need Tar running and connectivity; discovery normally takes up to 30 seconds. Existing assigned colors and owner-only rank management are preserved.
