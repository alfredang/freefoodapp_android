# Changelog

## 1.0 (versionCode 1) — Initial Android release

Native Android port of the iOS **FreeFood** app, built with Jetpack Compose + Material 3.

- Nearby feed with search and nearest-listing highlight.
- Map view (osmdroid) with tappable listing markers.
- Share-food form: title, details, date, start/end time, up to 3 photos, location
  search, recurrence (one-time / daily / weekly), and a lightweight human-check.
- Listing detail: photos, like, share, subscribe (recurring), info card, map, open-in-maps.
- Settings: country filter, distance filter, subscriptions, version.
- Local persistence (Room) + preferences (DataStore); sample listings seeded on first run.
- Auto-expiry of ended one-time listings (and posts older than 7 days).

### Play submission
- Submitted to **Google Play Production** for review on 2026-09-06 under the company
  developer account **Tertiary Infotech Academy Pte Ltd** (sales@tertiarycourses.com.sg,
  account 6132721113732535566), app id 4976305672717188645.
- Data safety: **no data collection declared** — listings, photos and location stay on
  device (Room + DataStore); the app has no backend and no networking library.
- Category Food & Drink, target audience 18+, no ads, no advertising ID.
