# Privacy

Tar Client has no Tar-operated analytics, advertising, account server, or automatic
crash-report upload in this preview. Opening the launcher reads and writes local
settings; installation, account sign-in, searches and game launching are initiated
by the user.

## Network activity

- Installation and launching fetch Minecraft metadata, libraries and assets from
  Mojang/Microsoft, and Fabric metadata/libraries from FabricMC. A launch checks
  installation metadata online and installs the default mods if needed.
- Mod discovery sends your search text, game version and loader filter to Modrinth.
  Opening Discover mods requests a compatible catalog; displayed project icons
  are fetched from Modrinth's CDN. Installation fetches mod metadata and files,
  including required dependencies.
- Microsoft sign-in opens the provider's page in your browser. The launcher uses
  Microsoft, Xbox and Minecraft services to obtain a game session and check your
  entitlement. It does not ask for your Microsoft password.
- Opening a project's web page sends a normal browser request to that site.
  Providers receive normal connection data such as your IP address and request
  headers. Metadata can designate upstream file hosts for downloads.

Provider policies:
[Microsoft](https://privacy.microsoft.com/privacystatement) and
[Modrinth](https://modrinth.com/legal/privacy).
Fabric's service is operated by [FabricMC](https://fabricmc.net/); a separate
published policy covering its metadata/download services has not been verified.
Minecraft, multiplayer servers and any additional mods can have their own network
behavior and policies; Tar does not control or disable that behavior.

## Local data

Profiles store module settings under the game's `config/tar-profiles` directory.
They contain no account tokens. The Server Address HUD displays the connected
server address on screen; consider disabling it before sharing screenshots.

Optional integrations are downloaded from Modrinth when enabled. TierTagger
contacts its tier-list providers for player ranks; its requests and those of
other optional mods are governed by their own projects. These integrations are
off by default and can be disabled for the next launch in Tar's module menu.

The default data directory is `%LOCALAPPDATA%\TarClient`. It holds settings,
downloaded game files, mods, saves, screenshots and game output. Microsoft session
tokens are held in memory. To start Java, a temporary argument file contains the
game access token; deletion is attempted shortly after starting and when the game
exits. A crash or failed cleanup can leave that file behind. Do not share raw
instance folders or launch argument files. Review logs before posting them publicly.

Custom data paths are supported. See [Uninstall](README.md#uninstall) to remove
local data after backing up your worlds. No data is uploaded to Tar maintainers
unless you choose to share it yourself.

## In-game accounts and Streamer Mode

In-game sign-in uses the same Microsoft/Xbox/Minecraft services as the launcher. Switching verifies the entitlement and UUID again, fetches profile restrictions and user permissions, and refreshes Minecraft's account services including its chat-signing keys, telemetry context and Realms client. Tar keeps these account tokens only in the game process; Minecraft's own profile-key cache and networking remain vanilla behavior. Closing Minecraft clears Tar's account list. The list is separate from the launcher's in-memory account.

Streamer Mode changes only the coordinate text in Tar's HUD, vanilla F3 and supported BetterF3 displays. It does not hide information in chat, maps or unrelated mods. Desktop installation copies application files to `%LOCALAPPDATA%\Programs\Tar Client` and creates `Tar Client.lnk` on the desktop; it does not copy or move the game data folder.
