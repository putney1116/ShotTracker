# Course Search by Zip / Place

Let the user search for importable courses by entering a zip code or place name, instead of only their current GPS location — so they can add courses they aren't physically at yet.

## Motivation

The "Nearby Courses" screen currently auto-searches the user's GPS location on open and only lets them import courses near where they physically are. Golfers often want to pre-load a course before traveling to it. Searching by zip/place removes that limitation.

## Decisions (from brainstorming)

- **UX model:** Replace the auto-search-on-open with an explicit search box. Nothing runs until the user searches. A "Use my location" button covers the GPS case.
- **Input:** the box accepts a zip code **or** a place name (e.g., "Naperville, IL"). Same geocoder call handles both.
- **Name filter:** removed. Results are already scoped to the searched location and paginated.
- **Geocoding:** Android's built-in `Geocoder` (no new dependency, no API key). Async listener API on Android 13+ (API 33), blocking call on a background thread for API 31–32.

## Architecture

### Components

1. **`GeocodingService`** — `core/geo/GeocodingService.kt`. Hilt `@Singleton`, constructor-injected `@ApplicationContext context`.
   - `suspend fun geocode(query: String): GpsLocation?`
   - Returns the first match's coordinates, or `null` if unavailable / no match / no network.
   - Runs off the main thread (`Dispatchers.IO`).
   - `Geocoder.isPresent()` guard → return null if false.
   - Android 13+ (`Build.VERSION.SDK_INT >= 33`): the async `getFromLocationName(name, maxResults, GeocodeListener)` bridged to a coroutine via `suspendCancellableCoroutine`.
   - API 31–32: the deprecated blocking `getFromLocationName(name, maxResults)` inside `withContext(Dispatchers.IO)`, wrapped in try/catch (`IOException` → null).
   - Reuses the existing `GpsLocation(latitude, longitude)` domain model.

2. **`CourseSearchViewModel`** — inject `GeocodingService` alongside the existing `OverpassRemoteDataSource` + `LocationService`.
   - State changes to `CourseSearchUiState`:
     - Add `locationQuery: String = ""` (the search box).
     - Add `hasSearched: Boolean = false` (distinguish the initial prompt from an empty result set).
     - Add `lastSearch: LastSearch = LastSearch.None` where `LastSearch = None | Query(text) | Gps` — so Retry re-runs the correct search.
     - Remove `searchQuery`, `filteredCourses`, `onSearchQueryChanged`. `pagedCourses`/`totalPages` now page over `courses` directly.
   - `onLocationQueryChanged(query: String)`.
   - `searchByQuery()`: blank query → no-op. Else set loading, `geocode(query)`; null → error "Couldn't find \"<query>\". Try a zip code or 'City, State'."; else `searchNearbyCourses(lat,lng)`, set results, `hasSearched = true`, `lastSearch = Query`.
   - `searchNearby()`: existing GPS path, but also sets `hasSearched = true`, `lastSearch = Gps`.
   - `retryLastSearch()`: dispatches to `searchByQuery()` or `searchNearby()` based on `lastSearch`.

3. **`CourseSearchScreen`** — retitled **"Find Courses"**.
   - Remove the `LaunchedEffect(Unit)` that auto-requests permission + searches on open.
   - Top row: an `OutlinedTextField` (zip/place, `KeyboardType.Text`, IME action Search → `searchByQuery()`), a **Search** button (disabled while query blank), and a **"Use my location"** icon/button.
   - The location permission launcher is triggered by the "Use my location" button; on grant → `searchNearby()`, on denial → error.
   - States:
     - Not yet searched (`!hasSearched && !isLoading`): prompt — "Search by zip code or place, or use your location."
     - Loading: generic "Searching…".
     - Error with no results: `ErrorState` with Retry → `retryLastSearch()`.
     - Results empty after search: existing `EmptyState`.
     - Results: list + pagination (unchanged).

### Data flow

```
Open screen → nothing runs → prompt state.

Type "60515" / "Naperville, IL" → onLocationQueryChanged
Search (button or IME) → searchByQuery():
    geocode(query) → null  → error message
                   → latlng → searchNearbyCourses(latlng) → results

"Use my location" → permission → searchNearby() (existing GPS path)

Retry (on error) → retryLastSearch() → re-runs Query or Gps
```

## Error handling

| Case | Behavior |
|------|----------|
| Empty query + Search | Search button disabled; no-op. |
| Geocoder unavailable (`!isPresent()`) | geocode → null → "Couldn't find…" message. |
| No network during geocode | `IOException` caught → null → "Couldn't find…" message. |
| No match for query | null → "Couldn't find \"<query>\". Try a zip code or 'City, State'." |
| Overpass failure | Existing error path; Retry re-runs the last search (query or GPS). |
| No courses at location | Existing `EmptyState`. |
| Location permission denied (GPS button) | Error: "Location permission denied." |

## Testing

- **`GeocodingService`** is `Geocoder`-coupled (device/Play services) → manual test, consistent with the project's posture for `LocationService`.
- **ViewModel** depends on concrete `OverpassRemoteDataSource` + `LocationService` (no interfaces); not adding fakes/interfaces just for this → manual test, consistent with existing ViewModels.
- **Manual test plan:**
  1. Open screen → prompt shown, no search runs.
  2. Enter a known zip (e.g., a course town) → Search → courses appear.
  3. Enter "City, State" → courses appear.
  4. Enter garbage → friendly "Couldn't find" error.
  5. Empty box → Search button disabled.
  6. "Use my location" → permission prompt (first run) → GPS results.
  7. Airplane mode → geocode fails → friendly error.
  8. Force an Overpass error → Retry re-runs the correct last search.

## Out of scope (v1)

- Adjustable search radius (reuse the existing nearby radius).
- Autocomplete / suggestions as you type.
- International postal formats beyond what `Geocoder` handles natively.
- Saving recent searches.
