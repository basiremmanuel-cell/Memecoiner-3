# Emmanuel November 2026 Memecoiner — Android project

**Package:** `com.emmanuel.mecoiner`  
**Version:** 1.0.0  
**Target:** Android 13+ compatible (min Android 8; target SDK 35)

## Included
- Bybit public Spot ticker scanner for a configurable starter list of meme coins.
- Automatic market refresh every 10 seconds while the app is open.
- Manual refresh button.
- Ethereum and BNB Chain token-transfer scan using Etherscan API V2 (chain IDs 1 and 56).
- User-configurable token contract, explorer API key, and large-transfer threshold.
- Android notifications for detected large transfers.
- No wallet connection and no automatic trading.

## Build and install
1. Install **Android Studio** on a computer.
2. Extract this ZIP.
3. In Android Studio choose **Open**, then select the extracted `Emmanuel_November_2026_Memecoiner` folder.
4. Allow Gradle sync to finish. Use JDK 17.
5. Connect your Android phone with USB debugging enabled, or create an APK with **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. Find the debug APK under `app/build/outputs/apk/debug/app-debug.apk`. Transfer it to your phone and install it. Android may ask you to allow installs from that file manager.
7. On first launch, allow notifications.

## Whale scan setup
- Choose Ethereum or BNB Chain.
- Paste the token's contract address.
- Enter your own Etherscan API key (Etherscan API V2 supports chain IDs 1 and 56).
- Set a threshold in token units and tap **CHECK RECENT WHALE TRANSFERS**.
- The scan checks the latest 30 token transfers returned by the explorer. It is a screening signal, not proof that a wallet is a whale or that a trade will be profitable.

## Important limitations
- This ZIP is the **upgradeable Android Studio source project**, not a precompiled APK.
- Live Bybit public ticker data requires internet access. Bybit may change API availability or symbols.
- Whale scans are manual, not a continuous background service. Explorer API access, rate limits, contract correctness, and token decimals affect results.
- The starter watchlist is hard-coded in `MainActivity.kt`; edit the `symbols` list to add/remove symbols.
- No API key, wallet secret, seed phrase, or trading permission is included or requested.
- This tool does not promise profits and does not place trades.

## Upgrade path
Open the same folder in Android Studio, edit the Kotlin source, increment `versionCode` and `versionName` in `app/build.gradle.kts`, then build a new APK. Keep the same `applicationId` to install upgrades over the existing app.
