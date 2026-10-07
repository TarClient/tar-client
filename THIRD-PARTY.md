# Third-party software

Tar's own source is MIT licensed. Dependencies keep their original licenses.

Bundled in the launcher JAR:
- Gson 2.13.2 â€” Apache 2.0 â€” https://github.com/google/gson
- FlatLaf 3.7 â€” Apache 2.0 â€” https://github.com/JFormDesigner/FlatLaf
- Fabric Loader API/implementation 0.19.5 (version predicate parsing) â€” Apache 2.0 â€” https://github.com/FabricMC/fabric-loader
- TwelveMonkeys ImageIO WebP and supporting modules 3.15.2 â€” BSD 3-Clause â€” https://github.com/haraldk/TwelveMonkeys

The Windows package contains a Temurin OpenJDK 21.0.10 runtime, GPLv2 with
Classpath Exception and third-party notices. Its license files are retained
under `runtime/legal`. Corresponding upstream source is available at
https://github.com/adoptium/temurin21-binaries/releases/tag/jdk-21.0.10%2B7 and
https://github.com/openjdk/jdk21u/tree/jdk-21.0.10%2B7 .

Downloaded on first installation, not bundled in this distribution:
- Minecraft 1.21.11 and its libraries/assets â€” Mojang/Microsoft and their respective authors
- Fabric Loader and Fabric API â€” FabricMC contributors
- Mod Menu â€” https://modrinth.com/mod/modmenu
- BetterF3 â€” https://modrinth.com/mod/betterf3
- Cloth Config API â€” https://modrinth.com/mod/cloth-config
- Placeholder API â€” https://modrinth.com/mod/placeholder-api

These projects and additional user-installed mods remain under their own licenses.
Tar does not claim authorship of BetterF3 or any third-party mod.

Optional 1.21.11 integrations, downloaded from Modrinth when enabled (not bundled):
- TierTagger 2.4.1 â€” MPL-2.0 â€” https://modrinth.com/mod/tiertagger â€” https://github.com/mctiers-dev/TierTagger (requires ukulib).
- Smooth Motion Blur 1.0.0 â€” LGPL-3.0-only â€” https://modrinth.com/mod/smooth-motion-blur â€” https://github.com/realjahleel/smooth-motion-blur.
- Resource Tree 1.2 â€” MIT â€” https://modrinth.com/mod/resource-tree-mod â€” https://github.com/Naw7k/resource-tree.

Versions above were verified during 0.3.0 development. The launcher resolves a
current compatible Fabric 1.21.11 release and verifies Modrinth's file checksum.
Required dependencies retain their upstream licenses.

## Tar branding and bootstrap

The Tar logo was supplied by the project owner and traced into vector shapes with their permission. The generated raster/icon files derive from `branding/tar-logo.svg`. The Windows bootstrap uses built-in .NET Framework libraries; no third-party installer engine is bundled. The community service uses Cloudflare Workers and its SQLite Durable Object storage. Wrangler is a development tool and is not bundled in the launcher.
