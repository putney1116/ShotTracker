# ShotTracker

An Android app for tracking golf shots, scores, and handicap during a round —
with GPS distance-to-green measurement on a satellite map and per-course data
imported from OpenStreetMap.

## Features

- **Live round tracking** — record shots, putts, and penalties hole-by-hole with
  a running score; long-press a counter to undo.
- **Distance to green** — satellite map oriented down the fairway showing front /
  center / back distances; tap anywhere to measure carry to that spot and on to
  the green, with labels that stay on screen as you zoom and rotate.
- **Scorecard & handicap** — full scorecard, per-tee stroke indexes, score
  adjustment (Net Double Bogey / ESC), and a WHS-style handicap differential with
  Playing Conditions Calculation.
- **Courses** — search nearby courses by GPS or ZIP and import greens/holes from
  OpenStreetMap, or build a course by hand with the 6-step creation wizard.
- **Per-course hole notes** that persist across rounds.
- **Round history & statistics** with an auto Do-Not-Disturb mode during play.

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + repository pattern
- **DI:** Hilt
- **Persistence:** Room
- **Networking:** Retrofit + kotlinx.serialization (OpenStreetMap Overpass API)
- **Maps/Location:** Google Maps Compose + Play Services Location
- **Min SDK 31 · Target SDK 35 · JVM 17**

## Project structure

```
com.example.shottracker/
├── core/        # DI modules, LocationService, DND, geocoding, utils
├── data/        # Room (local), Overpass (remote), mappers, repository impls
├── domain/      # models, repository interfaces, handicap calculator
├── feature/     # screens: home, round, scorecard, map, history, statistics,
│                #          course search / management / creation
└── navigation/  # navigation graph
```

## Building

1. Clone the repo and open it in Android Studio (or build from the command line).
2. Add your **Google Maps API key** to `local.properties` (this file is
   git-ignored and never committed):

   ```properties
   MAPS_API_KEY=your_api_key_here
   ```

   The key is injected into the manifest at build time via a `manifestPlaceholder`
   in `app/build.gradle.kts`. Without it the app builds and runs, but the map will
   not render.

3. Build and install a debug build:

   ```bash
   ./gradlew installDebug
   ```

## Testing

Unit tests run on the JVM:

```bash
./gradlew testDebugUnitTest
```

Room DAO tests are instrumented and run on a device/emulator:

```bash
./gradlew connectedDebugAndroidTest
```

## License

Personal project — all rights reserved. This code is published for reference only
and is not licensed for reuse, redistribution, or modification.
