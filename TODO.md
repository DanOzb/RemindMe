# RemindMe — TODO

_Written 2026-11-05, updated 2026-11-06. Started as the location plan; now tracks all remaining work._

**Order:** permissions ✓ → notifications ✓ → triggers ✓ (device testing left) → profile screen.

## Decisions made (don't re-debate)

- **Search → coordinates uses Android's built-in `Geocoder`.** Free, no API key, no new library.
- **No Photon / Nominatim / other OSM search.** Shops are covered by reusing old reminder cards, plus a future custom-place/pin feature.
- **No map view or place picker for now.** Just a search field and a results list. A map can come back later for the pin feature.
- **Search on submit** (press search, then pick a result). No typing suggestions. Geocoder isn't built for that.

## What testing showed (tested on device, 2026-11-05)

- **Addresses work well.** "rädisvägen" alone found the right street in Stockholm.
- **Shop/business names work poorly.** That's expected, and the reason for the reuse/pin plan.
- Results came back local (Stockholm) **without passing any location**. Geocoder probably guesses the region from network/IP or the phone's language/region settings. Don't rely on that guess, see step 3.
- Geocoder needs Google Play services. Emulators must use a **Google Play** image. `Geocoder.isPresent()` is false on phones without Google services.

## Debug playground (gone)

- The old `GeocoderDebugActivity` was never committed and no longer exists. The real search replaced it (2026-11-06).

---

## TODO

### 1. Data model
- [x] Add `latitude: Double?` and `longitude: Double?` to `TaskEntity` (`data/Task.kt`). `place` stays as the **display label**.
- [x] Maybe add `address: String?` (Geocoder's address line) so the user can label a place "Gym" while the real address is kept.
- [x] Add the same fields to `ReminderForm` (`ui/reminder/ReminderForm.kt`) **and to its `Saver`** (~line 151–163). Otherwise they're lost when the screen rotates.
- [x] Copy them between form ↔ task wherever that mapping happens.
- [x] Give `SampleTasks.kt` real Stockholm coordinates for testing.
- [x] `distanceMeters` is currently stored on `TaskEntity`, but it should be **computed** from the current location (see step 7). Remove it from storage eventually.
- [x] Reminder: `ReminderStore` is in-memory only. When moving to Room, include the new fields.

### 2. Place search code
- [x] Interface, e.g. `PlaceSearch.search(query, near: LatLng?): List<PlaceResult>` with `PlaceResult(label, addressLine, lat, lng)`.
- [x] `GeocoderPlaceSearch` implementation, reusing the debug screen's helper.
- [x] Handle: `Geocoder.isPresent() == false` (show a message), an empty result list ("No matches"), and errors (`IOException` = offline or service unavailable).
- [x] `maxResults` ~5.

### 3. Location bias (make "local results" reliable)
- [x] Pass a **bounding box** (rectangle around a point) to `getFromLocationName(name, max, lowerLeftLat, lowerLeftLng, upperRightLat, upperRightLng, …)`, about 30–50 km around the user's last known location.
- [x] If the box returns nothing, retry without it, so a user can still find a place in another city.
- [x] Needs location permission (coarse is enough for this). If not granted, search without the box.
  - Code is in place: `TasksViewModel.searchPlaces` passes the current location, which stays null until `updateLocation` is fed (permissions step).

### 4. UI: `LocationCard`
- [x] Replace the plain text field at `ui/reminder/LocationCard.kt:42` (the `TODO: replace with a real place picker`) with a search field and a results list.
- [x] Each result shows its name and full address. Picking one sets the label, lat and lng.
- [x] After picking, let the user **edit the label** ("Home", "Gym") while keeping the coordinates.
- [x] Show the chosen address under the label so it's clear what was picked.
- [x] Loading state while searching. Geocoder took a noticeable moment in the debug screen.
- [x] Clearing the place clears the coordinates too.
- [x] A place typed without picking a result has **no coordinates**. Decided: typing only fills the search field; only a picked result sets a place. Old text-only places open as a search query.

### 5. Reusing places
- [ ] When creating a reminder, suggest places from **previous reminders**: unique (label, lat, lng), most-used or most-recent first. This is the plan for shops instead of a better search.
- [ ] Later: a **custom places / pins** feature (saved places like Home, Work, favorite shop; maybe drop a pin on a map). Revisit the map choice then (MapLibre + OpenFreeMap was the free option discussed; avoid `tile.openstreetmap.org` and osmdroid).

### 6. Permissions
_Done 2026-11-06: `data/location/Permissions.kt`, `ui/permissions/PermissionRequests.kt`. Location is asked when a place is picked; notifications once when Home first has a reminder; both rows on the profile screen ask again or open settings. Not yet tested on a device._
- [x] `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`, asked while the app is open.
- [x] `ACCESS_BACKGROUND_LOCATION`, needed for geofences to fire with the app closed. On Android 11+ this **can't be in the same dialog**: ask separately afterwards, and the user has to pick "Allow all the time" in system settings. Explain why first.
- [x] `POST_NOTIFICATIONS` (Android 13+), needed to show the reminder at all.
- [x] The profile screen's "Location access" row (`SampleAccount.locationAccess = "Always"`, `AccountAction.LocationAccess`) is fake. Show the real permission status and open settings when tapped.
- Note: Geocoder itself needs **no** permission and no `INTERNET` permission (it runs inside Google Play services).

### 7. Triggers: geofences, alarms, expiry
_Built 2026-11-06 (`triggers/`), not yet tested on a device. Decisions: a one-shot turns inactive after firing; exact alarms asked once, inexact fallback; expired looks the same as done._
- [x] Pure planner `triggerPlan(now)` (`triggers/TriggerPlan.kt`, 22 tests): what listens for each reminder and when the next alarm is.
  - Place only → geofence (enter, or exit for "away from"). Time only → alarm at each occurrence (an open Between window fires at once).
  - Place OR time → both. Place AND time → geofence only while the window is open (initial trigger, so already being there counts); an exact time counts until the end of that day.
  - Last window closes without firing → expire: "Missed" notification (one-shots) and inactive. A finished repeating series ends quietly.
- [x] `Triggers` applies plans (GeofencingClient + AlarmManager), handles events: fire → notify → one-shot inactive / repeating `markFired` + next.
- [x] Receivers: `TriggerReceiver` (geofence + alarm intents), `SystemEventsReceiver` (boot, app update, time / time zone change, exact-alarm permission change → `syncAll`).
- [x] Synced on save / complete / un-complete / delete, on "Mark done", and whenever Home is shown (catches permission changes).
- [x] Exact alarms: asked once when saving the first timed reminder ("Alarms & reminders" settings page).
- [x] Switching a done one-off with a past date back on opens the editor instead (it would expire at once).
- [ ] **Test on a device**: walk / emulator location (Extended controls → Location), timed reminders, reboot, "Mark done".
- [ ] Remove the debug "Test notification" item in Home's ⋮ menu once real firing is confirmed.
- [x] "Alarms & reminders" row on the profile screen: Allowed / Not allowed, tap opens its settings page.
- [ ] A one-off time with no date that was missed while the phone was off slides to the next day instead of counting as missed (no record of the planned time). Fine for now.
- Note: limit of **100 geofences per app** (`syncAll` stops registering past it).
- Note: small radii are unreliable. Options are 100/200/500 m; avoid going below ~100 m.
- Note: triggers can be delayed by a few minutes; that's normal for battery saving.

### 8. "Nearby" indicator and distance
- [x] Distance is computed in Kotlin (`orderActive` / `distanceMeters()` in `data/TaskOrder.kt`), and Home sorts and shows "Nearby" from it.
- [x] Feed the current location to `TasksViewModel.updateLocation` when Home is shown (needs location permission; otherwise it stays null and "Nearby" stays hidden).

### 9. Notifications
_Done 2026-11-06 (`notifications/`), not yet tested on a device._
- [x] Channels "Reminders" (high) and "Missed reminders" (default), created in `RemindMeApp`.
- [x] `ReminderNotifications.showReminder(task)`: title + "You're at X" / "You've left X" / "It's time". One per reminder (id = task id).
- [x] Tapping opens that reminder (`MainActivity` → `AppNavHost(openReminderId)`).
- [x] "Mark done" action (`NotificationActionReceiver`), no app launch.
- [x] Completing or deleting a reminder in the app clears its notification.
- [x] `showMissed(task)`, used by expiry (step 7).
- [ ] Debug builds have "Test notification" in Home's ⋮ menu (see step 7).
- [x] "Mark done" also clears the reminder's geofence / alarm.

### 10. Profile screen
- [x] Appearance: System / Light / Dark, saved (`ui/theme/Appearance.kt`).
- [x] Notifications switch is sample data: show the real status, ask or open settings.
- [x] Location access row: real status (see step 6).
- [x] Default radius is a real app-wide setting (`data/ReminderDefaults.kt`), picked on the profile screen. The reminder form only shows it; changing it updates every place reminder and re-registers geofences.
- [x] Profile card (name / email / Edit) removed.
- [x] Sign out removed (no accounts).
- [ ] Privacy & data / Help & support go nowhere yet.

### 11. Cleanup
- [x] Delete `app/src/debug/` once the real search works (or keep it as a playground; it never ships in release).
