# Tar Client 0.3.1 module guide

Open **Client modules** in the launcher or press **Right Shift** in Minecraft.
Search by name, toggle a module and open its settings. **Edit HUD** lets you drag
visible HUDs. All Tar HUD panels have a **Show background** switch, background
color/opacity, roundness, text color, position and scale. Backgrounds are off by
default, including when upgrading old settings. Visual and utility modules that
do not draw a HUD have no panel background to configure.

## New built-in modules

| Module | Use and customization |
| --- | --- |
| Armor status | Horizontal or vertical strip. Reverse direction puts the first armor slot at the bottom/right. Set icon size, spacing, durability text visibility, bar visibility/width, optional durability colors, percentage/points and empty slots. Existing background, scale, position and warning-sound controls remain. |
| Shulker box tooltips | 9-column contents grid on hover, optional Shift requirement and grid background. Minecraft still draws its outer tooltip frame. Only item contents supplied by the game are shown. |
| Zoom | Enable, then hold **C**. Configure multiplier and smooth transition; rebind in Minecraft Controls. |
| Freelook | Enable, then hold **Left Alt**. The camera enters third person and rotates independently while the player's aim stays unchanged. Releasing restores your prior perspective. Configure sensitivity and rebind in Controls. |
| Time changer | Enable, set the tick value, then press **Apply time** in the in-game module settings. Sends the normal `/time set` command. Requires server OP/command permission or singleplayer cheats; never changes time automatically. |
| Clock | Local computer time in AM/PM format, with optional seconds. |
| Inventory HUD | Main inventory grid, optional hotbar row and item count/durability overlays. |
| Saturation | Exact singleplayer saturation. Multiplayer is labeled as an estimate because vanilla does not continuously synchronize the actual value. |
| Hit color | Change the damage tint on player models. |
| Coordinates | Block X/Y/Z and optional dimension label. Enable Show background for a coordinate box. |
| Reach display | Distance from your eyes to the target hitbox at the last local attack, with display timeout and player-only filter. Does not change reach or confirm that the server accepted damage. |
| Server address | Connected server address, optional server name and server-list image. A placeholder is used if Minecraft has no icon. |
| Profiles | Save and load all Tar module settings. Bedwars and SMP presets are created once and never overwrite your saved versions. Open Profiles directly in either sidebar. Enter any valid custom name and select Create profile to copy the current setup. Load restores it; Update replaces it after confirmation. Names use 1-48 letters, numbers, spaces, underscores or hyphens, starting with a letter or number. Keyboard bindings remain global in Minecraft options. |
| Smart disconnect | A Leave/Cancel confirmation before disconnecting through the pause menu. Preserves Minecraft's draft-report flow. Does not prevent server kicks or closing the operating-system window. |
| Limit unfocused FPS | Set a limit from 5 to 120 FPS while the game is unfocused. Respects any lower vanilla limit. |

## Integrated mods

These four module cards use compatible upstream mods. Enable the card before
launching; Tar downloads it and its required dependencies from Modrinth. If you
change enabled state in game, **close and relaunch Minecraft through Tar** to
apply it. Motion blur can toggle live once installed. Profiles store enabled states and the Tar motion-blur settings; other detailed upstream settings remain global.

| Module | Provider and settings |
| --- | --- |
| TierTagger | [Official TierTagger](https://modrinth.com/mod/tiertagger), with ukulib. Displays published PvP tiers; use Mod settings / Mods for tier-list and display settings. |
| Motion blur | [Smooth Motion Blur](https://modrinth.com/mod/smooth-motion-blur). Tar Settings provides strength 1-100 and Pause blur in menus. Disable the module for zero blur. Strength uses the upstream display scale (20 = default). Upstream command/shortcut changes are synchronized back into Tar profiles. Its Increase/Decrease keys appear in Keybinds when installed. |
| 3D skins | [3D Skin Layers](https://modrinth.com/mod/3dskinlayers). Use Mod settings for the outer skin layer rendering options. |
| Pack organizer | [Resource Tree](https://modrinth.com/mod/resource-tree-mod). The Resource Packs screen gains subfolder navigation and folder-management controls. Use the Resource Packs button in its integration screen. |

The four integrations start disabled and are not copied into the Tar ZIP.
Installed-mod toggles for these four are reconciled with their Tar module setting
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
Minecraft account login still depends on Mojang's application review.
