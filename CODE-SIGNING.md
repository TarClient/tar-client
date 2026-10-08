# Code signing policy

Status: applying for free open-source signing; no provider acceptance, certificate access or signed Tar release is claimed. Tarre Industries is a personal project name, not a registered company or verified Windows publisher. The latest public 1.0.2 release is unsigned; the 1.0.3 candidate adds installation privacy controls.

## Proposed provider and team

We seek free code signing through [SignPath.io](https://about.signpath.io), with a certificate issued to [SignPath Foundation](https://signpath.org). If accepted, Windows would identify SignPath Foundation as the certificate publisher. Provider approval is discretionary, including review of project reputation and artifact eligibility.

- Maintainer, committer and reviewer: [TarClient](https://github.com/TarClient), the repository owner (previously prutprut2003-creator).
- Release/signing approver: the same owner. Every signing request requires their manual approval.
- Contributions from others must receive maintainer review. The maintainer reported enabling GitHub two-factor authentication on 8 October 2026; SignPath MFA must also be enabled during onboarding.

## Build and artifact controls

Public GitHub Actions builds from the exact source commit, runs tests, and packages the Windows launcher. The signing provider must review the jpackage-generated launcher before authorizing its signing. Bundled Java executables/DLLs keep their upstream signatures. Never sign upstream code as if Tar authored it.

Both the installed native launcher and the outer C# bootstrap require signing. The installed launcher must be signed before creating the payload ZIP, payload hash and bootstrap. The final bootstrap must then be signed and final checksums regenerated. A self-signed certificate or project name does not establish public trust. Provider artifact configuration and credentials will be added only after approval; no signing secret is stored in source or downloads.

## Privacy and installation

See [Privacy](PRIVACY.md), [Third-party software](THIRD-PARTY.md), and README uninstall instructions. The signing candidate shows the privacy policy before installation and provides an optional badge-sharing switch, enabled by default. Sharing requires a valid saved choice and can be disabled in launcher Settings. Profiles cannot re-enable an opt-out. Microsoft/Mojang, Fabric and Modrinth connections needed for requested sign-in, game installation and downloads are described in the privacy policy.

No SignPath sponsorship attribution will be presented as an existing benefit until the provider approves the project.
