# ShotTracker Android Application - Implementation Plan

## Overview
A personal golf shot tracking Android app using modern Android best practices (Kotlin, Jetpack Compose, MVVM + Clean Architecture).

## Core Features
1. **Home Menu**: Start new round, view history, view statistics
2. **Active Round**: GPS distances to green (front/middle/back), shot tracking with club selection, putt tracking, hole navigation
3. **Scorecard**: View/edit scores for all holes
4. **Map View**: Google Maps showing user location and green position
5. **Statistics**: Overall scoring, club distances, trends

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.0+ |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room with Kotlin Flow |
| Location | FusedLocationProviderClient |
| Maps | Google Maps Compose SDK |
| Navigation | Navigation Compose |
| Async | Kotlin Coroutines + Flow |

---

## Project Structure

```
com.shottracker/
├── ShotTrackerApplication.kt
├── MainActivity.kt
├── core/
│   ├── di/                    # Hilt modules
│   ├── location/              # LocationService
│   ├── util/                  # DistanceCalculator, formatters
│   └── ui/theme/              # Theme, colors, typography
├── data/
│   ├── local/
│   │   ├── ShotTrackerDatabase.kt
│   │   ├── dao/               # CourseDao, RoundDao, ShotDao, etc.
│   │   └── entity/            # Room entities
│   ├── repository/            # Repository implementations
│   └── mapper/                # Entity <-> Domain mappers
├── domain/
│   ├── model/                 # Domain models (Round, Hole, Shot, Club)
│   ├── repository/            # Repository interfaces
│   └── usecase/               # Business logic
├── feature/
│   ├── home/                  # Home screen
│   ├── round/                 # Active round tracking
│   ├── scorecard/             # Scorecard view/edit
│   ├── map/                   # Google Maps view
│   ├── history/               # Round history
│   └── statistics/            # Stats dashboard
└── navigation/                # NavHost, Screen routes
```

---

## Database Schema

### Tables
1. **courses** - Golf course info (name, location)
2. **hole_info** - Course hole data (par, green GPS coordinates)
3. **tees** - Tee box options per course (name, color, rating, slope)
4. **tee_hole_info** - Yardage per hole per tee
5. **clubs** - User's clubs (name, category, loft)
6. **rounds** - Round sessions (course, tee, start/end time, status)
7. **hole_scores** - Per-hole data (score, putts, fairway, GIR)
8. **shots** - Individual shots (club, GPS location, distance)

### Key Entities
```kotlin
// Round tracking
RoundEntity(roundId, teeId, startTime, endTime, status, holesPlayed)
HoleScoreEntity(holeScoreId, roundId, holeNumber, par, score, putts, fairwayHit, gir)
ShotEntity(shotId, holeScoreId, clubId, shotNumber, latitude, longitude, distanceYards)

// Course data
CourseEntity(courseId, name, city, state)
HoleInfoEntity(holeInfoId, courseId, holeNumber, par, greenFront/Center/BackLat/Lng)
```

---

## Key Implementation Details

### Location Service
- Use FusedLocationProviderClient with HIGH_ACCURACY priority
- 5-second update interval (balanced mode)
- Accuracy filtering (reject > 20m accuracy)
- Battery optimization with different modes (high/balanced/low power)

### Distance Calculation
- Haversine formula for GPS-to-yards conversion
- Calculate distances to front/middle/back of green
- Auto-calculate shot distances between consecutive shots

### Course Data
- Database schema designed to support future GIS data import
- Course green coordinates (front/center/back) stored per hole
- Future enhancement: Import course data from public GIS datasets

### Map View
- Satellite view for course visibility
- User location marker (blue)
- Green markers (front=green, center=yellow, back=red)
- Shot location markers (orange, numbered)
- Camera positioned with user at bottom, green at top

---

## Statistics Calculations

1. **Scoring**: Average score, best score, score to par
2. **Fairways**: Hit percentage (par 4/5 holes only)
3. **Greens in Regulation**: GIR percentage
4. **Putting**: Average putts per hole/round, 1-putt/2-putt/3+ percentages
5. **Club Distances**: Average/max/min distance per club
6. **Scoring by Par**: Average score on par 3s, 4s, 5s
7. **Trends**: Rolling 5-round scoring average

---

## Navigation Flow

```
HOME
├── Start New Round → NEW ROUND SETUP → ACTIVE ROUND
│                                        ├── Scorecard
│                                        ├── Map View
│                                        └── End Round → HOME
├── View History → HISTORY LIST → ROUND DETAIL
└── View Statistics → STATISTICS DASHBOARD
```

---

## Implementation Phases

### Phase 1: Project Setup
- Create Android project with Compose
- Configure build.gradle with dependencies
- Set up Hilt DI
- Create Room database with entities/DAOs
- Implement basic navigation
- Set up Material 3 theme

### Phase 2: Home & Round Setup
- Home screen with menu options
- New round setup (course/tee selection)
- Course creation flow
- Basic active round screen layout

### Phase 3: GPS & Distance Tracking
- Implement LocationService
- Distance calculation (Haversine)
- Distance to green display
- Shot recording with club selection
- Shot distance calculation

### Phase 4: Scorecard & Editing
- Scorecard screen
- Score/putts editing
- Hole navigation
- Round statistics summary

### Phase 5: Google Maps Integration
- Google Maps API setup
- Course map screen
- User/green markers
- Shot position markers
- Camera positioning

### Phase 6: History & Statistics
- Round history list
- Round detail view
- Statistics calculations
- Statistics dashboard
- Club distance tracking

### Phase 7: Polish
- Error handling
- Loading/empty states
- UI animations
- Testing on device

---

## Critical Files to Create First

1. `app/build.gradle.kts` - Dependencies configuration
2. `ShotTrackerDatabase.kt` - Room database definition
3. `LocationService.kt` - GPS location tracking
4. `DistanceCalculator.kt` - Haversine distance formula
5. `ActiveRoundViewModel.kt` - Core round tracking logic
6. `ShotTrackerNavHost.kt` - Navigation graph

---

## Permissions Required

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
```

---

## Verification Plan

1. **Build verification**: `./gradlew assembleDebug` compiles without errors
2. **Location test**: GPS distances update in real-time on device
3. **Shot tracking test**: Record shots, verify distance calculations
4. **Scorecard test**: Edit scores, verify persistence
5. **Map test**: User location and green markers display correctly
6. **Statistics test**: Complete a round, verify stats update
7. **Round persistence**: Close app mid-round, verify data retained

---

## Notes

- **Minimum SDK**: 31 (Android 12) - Modern API features
- **Target SDK**: 35 (Android 15)
- **Google Maps API Key**: Required for map functionality
- **Units**: Yards (standard for US golf)
- **Offline capable**: All core features work without internet
