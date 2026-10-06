# Microsoft sign-in setup

Tar Client implements Microsoft's device-code flow, Xbox Live authentication,
XSTS, Minecraft token exchange and a Minecraft profile check. It does not collect
your password, borrow another launcher's identity, or provide cracked accounts.

**Tar Client 0.4.1 includes its registered Microsoft application ID automatically:**
`c8d8f6e2-12dc-4499-911c-1c7294e91f44`.
This is a public identifier, not a password. Users do not need an Azure account,
an app registration, or any manual application ID setup.

**Minecraft API review was approved on 21 September 2026.** The publisher received the official AppID review completion email confirming allowlist approval. Approval is separate from an end-to-end account test; full sign-in and an authenticated server connection have not yet been verified during development.

1. Open **Accounts** and click **Sign in with Microsoft**.
2. Complete the official Microsoft device-code flow in your own browser.
3. A successful Minecraft profile lookup will show your player name. Full-game
   launch requires that successful lookup and a valid entitlement.

Every download uses the built-in ID. Version 0.4.1 ignores old overrides and removes the setup field; there is nothing to paste.

To add a second account, choose **Accounts > Add another account**. Tar's sign-in dialog also has **Use another account (private window)**, which opens a private Edge or Chrome window without clearing your normal browser session. Enter the displayed device code there. If neither browser is available, Tar copies the Microsoft URL so you can paste it into your browser's private window. Alternatively, select **Use another account** on Microsoft's page. Complete the confirmation and return to Tar. Each Microsoft account must independently have Minecraft Java access.

The launcher keeps verified accounts in memory and offers Switch/Forget. Closing it clears these sessions. Minecraft's Right Shift > Accounts list is separate and also lasts only for that running game. Switching in game leaves the current world/server with confirmation.

Sign-in progress identifies Microsoft, Xbox, Minecraft exchange, ownership and profile steps. If it fails, share the displayed step and error, never a password or token. This does not bypass family restrictions, missing Xbox profiles, service outages or ownership requirements.

For source forks that use their own identity: register an application supporting
personal Microsoft accounts and enable public-client flows for device-code sign-in.
Do not create or embed a client secret. Request Minecraft API access through
the official [AppID review form](https://aka.ms/mce-reviewappid).

Use an account with Minecraft Java ownership or a qualifying active subscription.
Family restrictions or missing Xbox profiles may need to be resolved in Microsoft's
own account interface. Tar Client only reports the service error; it cannot remove
account restrictions.

Sessions are held in memory and never saved to the preferences JSON. Sign in after
each launcher restart. Signing out clears the launcher's in-memory session. The
game necessarily receives a token when launched; its temporary Java argument file
is deleted after startup or process exit. If the launcher is forcibly killed during
startup, a `.launch-*.args` file could remain in the game folder; do not share it.

## Switch accounts without closing Minecraft

Open **Right Shift > Accounts > Add Microsoft / Minecraft account**. Open Microsoft's page and enter the displayed device code there. Tar verifies ownership, UUID, profile and account permissions before offering the switch. Adding an account does not automatically leave a world: the **Leave and switch** confirmation controls that step. Reconnect from the title screen after switching. Minecraft's multiplayer, chat and ban restrictions are preserved.

Added accounts stay in the game process's memory, separately from the launcher's selected session. **Forget** clears an inactive saved session. Closing Minecraft clears this list. Signing in again is necessary when an access token expires. No Tar account database or refresh tokens are saved. Minecraft still manages its own profile-key cache.

Microsoft references:
- https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-device-code
- https://learn.microsoft.com/en-us/entra/identity-platform/quickstart-register-app
- https://learn.microsoft.com/en-us/gaming/gdk/docs/services/fundamentals/xbox-services-overview

No live Microsoft account login was performed during development of this build.
