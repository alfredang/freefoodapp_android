# Publishing FreeFood to Google Play (Closed Testing)

This app is built and signed locally and ready for upload. Uploading to Google Play
requires a **Google Play Developer account** (one-time US$25) and is gated behind an
interactive, authenticated Play Console session, so it cannot be fully automated from
this machine without a Play **service-account key**. This guide covers both the manual
(Console) and automated (Fastlane) paths.

## What's already done

- ✅ Signed **release App Bundle**: `app/build/outputs/bundle/release/app-release.aab`
- ✅ Signed release APK (for sideload testing): `app/build/outputs/apk/release/app-release.apk`
- ✅ Upload keystore: `keystore/freefood-release.jks` (password in `keystore.properties`)
- ✅ Store listing copy: `fastlane/metadata/android/en-US/`
- ✅ 512×512 icon + 1024×500 feature graphic + phone screenshots: `fastlane/metadata/android/en-US/images/`
- ✅ `applicationId` = `com.tertiaryinfotech.freefood`, `versionCode` 1 / `versionName` 1.0, `targetSdk` 35

> ⚠️ **Back up `keystore/freefood-release.jks` and its passwords.** If you opt out of
> Play App Signing you can never update the app without this exact key. Recommended:
> enable **Play App Signing** and let Google manage the app signing key, using this
> keystore only as the upload key.

## Option A — Manual upload (Play Console)

1. Go to <https://play.google.com/console> and **Create app**:
   - App name: **FreeFood: Share Leftovers**
   - Default language: English (United States)
   - App or game: App · Free
2. Complete the onboarding: Privacy policy URL, App access, Ads, Content rating
   questionnaire, Target audience, Data safety form. (Location + photos are used; no
   account, no data shared with third parties — the app stores listings on-device.)
3. **Testing → Closed testing → Create track**. Add testers by email or a Google Group.
4. **Create new release** on that track → upload `app-release.aab`.
   Accept **Play App Signing** when prompted.
5. Paste release notes from `fastlane/metadata/android/en-US/changelogs/1.txt`.
6. Fill the **Store listing** (Main store listing) using the text + images in
   `fastlane/metadata/android/en-US/`.
7. **Save → Review release → Start rollout to Closed testing**. Google review for a
   first release typically takes a few hours to a couple of days.
8. Share the closed-testing opt-in URL with your testers.

## Option B — Automated upload (Fastlane)

A `fastlane` config is included.

1. Create a Play **service account** (Play Console → *Users and permissions* → *Invite*
   via Google Cloud), grant it *Release to testing tracks*, and download its JSON key.
2. Save the key as `play-service-account.json` in the repo root (git-ignored) or set
   `PLAY_STORE_JSON_KEY=/path/to/key.json`.
3. Install tooling: `brew install fastlane`
4. The very **first** release for a brand-new app must be uploaded once via the Console
   (Option A) — Google does not allow the API to create the first release. After that:

```sh
# upload the signed AAB to the closed-testing track as a draft, plus listing + screenshots
fastlane closed track:closed
# or just refresh the store listing text/images
fastlane metadata
```

`release_status` is set to `draft` so you can review in the Console before rolling out.

## Regenerating the artifacts

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew clean :app:bundleRelease     # -> app/build/outputs/bundle/release/app-release.aab
./gradlew :app:assembleRelease         # -> app/build/outputs/apk/release/app-release.apk
```
