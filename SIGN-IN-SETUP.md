# Microsoft sign-in

Tar Client 1.0 includes public application ID `c8d8f6e2-12dc-4499-911c-1c7294e91f44` for every download. Users do not need Azure, a client secret or an ID to paste. Mojang's AppID review was approved on 21 September 2026; that approval does not override individual account restrictions.

1. Open Accounts > Sign in with Microsoft / Add another account.
2. Tar opens Microsoft's device-code page in a fresh, isolated private Edge or Chrome profile. Enter the displayed code and the email for the Microsoft account you want. A Gmail address can identify a Microsoft account; full play requires that account to own Minecraft Java or have an eligible subscription.
3. Finish Microsoft's confirmation. Tar verifies Xbox/XSTS, Minecraft ownership and profile, then shows the player name.

If the browser cannot open, Tar copies the Microsoft URL. Open it in a fresh private window yourself. Tar never embeds a password form, reads normal browser cookies, borrows another launcher's app ID or offers cracked accounts.

## Remembered accounts

Microsoft refresh tokens and Minecraft sessions are encrypted with Windows CurrentUser DPAPI in `%LOCALAPPDATA%/TarClient/accounts.dpapi`. The selected account is refreshed on reopening. An expired/revoked refresh token, account restriction or unavailable Microsoft service may require signing in again. The launcher remains usable when restoration fails and reports the failure. Switch verifies the selected saved account; Forget/Sign out removes its saved entry. No credentials are saved in `launcher.json`.

Right Shift > Accounts loads the same encrypted account list. Adding and switching accounts works without restarting Minecraft. Switching asks before leaving the current world/server; reconnect afterwards. Permissions, entitlement, UUID, chat signing and Realms services are refreshed. The launcher's currently selected account and a running game's active account remain independent.

A temporary Java argument file necessarily contains the Minecraft token while starting the game and is removed after startup or exit when possible. Never share `.launch-*.args`, raw account files, passwords or verification codes. See PRIVACY.md.

## Source forks

Register your own public-client application supporting personal Microsoft accounts and device-code flow, then obtain Minecraft AppID approval through the [official review form](https://aka.ms/mce-reviewappid). Never embed a client secret. Relevant Microsoft documentation: [device-code flow](https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-device-code).

Synthetic-response tests cover token rotation, account-identity checks and failures. Successful login with two real accounts and authenticated reconnection still require real-machine verification; no such live test is claimed for this build.
