# Course Search by Zip / Place — Implementation Plan

> REQUIRED SUB-SKILL: superpowers:executing-plans.

**Goal:** Replace GPS-auto-search on the course-search screen with an explicit box that geocodes a zip/place (Android `Geocoder`) and searches there; keep a "Use my location" GPS option; drop the name filter.

**Spec:** `docs/superpowers/specs/2026-05-31-course-search-by-zip-design.md`

**Repo note:** Not a git repo. Verify with `:app:compileDebugKotlin`; install with `:app:installDebug`.

---

## Files
- **New:** `app/src/main/java/com/example/shottracker/core/geo/GeocodingService.kt`
- **Modify:** `feature/coursesearch/CourseSearchViewModel.kt`
- **Modify:** `feature/coursesearch/CourseSearchScreen.kt`

---

### Task 1: GeocodingService
- [ ] Create `GeocodingService.kt`:

```kotlin
package com.example.shottracker.core.geo

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import com.example.shottracker.domain.model.GpsLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class GeocodingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Convert a zip code or place name to coordinates, or null if unavailable/no match. */
    suspend fun geocode(query: String): GpsLocation? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        if (!Geocoder.isPresent()) {
            Log.w("GeocodingService", "Geocoder not present on device")
            return null
        }
        val geocoder = Geocoder(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocodeAsync(geocoder, trimmed)
        } else {
            geocodeBlocking(geocoder, trimmed)
        }
    }

    private suspend fun geocodeAsync(geocoder: Geocoder, query: String): GpsLocation? =
        suspendCancellableCoroutine { cont ->
            try {
                geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        cont.resume(addresses.firstOrNull()?.toGps())
                    }
                    override fun onError(errorMessage: String?) {
                        Log.w("GeocodingService", "geocode error: $errorMessage")
                        cont.resume(null)
                    }
                })
            } catch (e: Exception) {
                Log.w("GeocodingService", "geocodeAsync failed", e)
                cont.resume(null)
            }
        }

    @Suppress("DEPRECATION")
    private suspend fun geocodeBlocking(geocoder: Geocoder, query: String): GpsLocation? =
        withContext(Dispatchers.IO) {
            try {
                geocoder.getFromLocationName(query, 1)?.firstOrNull()?.toGps()
            } catch (e: Exception) {
                Log.w("GeocodingService", "geocodeBlocking failed", e)
                null
            }
        }

    private fun Address.toGps() = GpsLocation(latitude = latitude, longitude = longitude)
}
```
- [ ] `:app:compileDebugKotlin` → SUCCESS.

---

### Task 2: ViewModel
- [ ] Inject `GeocodingService`.
- [ ] Rework `CourseSearchUiState`: remove `searchQuery` + `filteredCourses`; add `locationQuery: String = ""`, `hasSearched: Boolean = false`. `pagedCourses`/`totalPages` page over `courses`. Add `enum class LastSearch { NONE, GPS, QUERY }` + `lastSearch` + `lastQuery`.
- [ ] Replace `onSearchQueryChanged` with `onLocationQueryChanged(query)`.
- [ ] Add `searchByQuery()`:
```kotlin
fun searchByQuery() {
    val query = _uiState.value.locationQuery.trim()
    if (query.isEmpty() || _uiState.value.isLoading) return
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null,
            lastSearch = LastSearch.QUERY, lastQuery = query)
        val loc = geocodingService.geocode(query)
        if (loc == null) {
            _uiState.value = _uiState.value.copy(isLoading = false, hasSearched = true,
                courses = emptyList(),
                error = "Couldn't find \"$query\". Try a zip code or 'City, State'.")
            return@launch
        }
        runOverpass(loc.latitude, loc.longitude)
    }
}
```
- [ ] Refactor existing `searchNearby()` to set `lastSearch = GPS` and share a private `runOverpass(lat, lng)` that sets `courses`, `hasSearched = true`, `currentPage = 0`, handles try/catch. Keep the null-location error.
- [ ] Add `retryLastSearch()` → when `LastSearch.QUERY` call `searchByQuery()`, when `GPS` call `searchNearby()`.
- [ ] `:app:compileDebugKotlin` → SUCCESS.

---

### Task 3: Screen
- [ ] Remove the `LaunchedEffect(Unit)` auto permission+search.
- [ ] Keep the permission launcher, but invoke it from a "Use my location" button; on grant → `viewModel.searchNearby()`, on denial → surface an error via a new `viewModel.onLocationPermissionDenied()` (sets error) OR snackbar.
- [ ] Replace the name-filter `OutlinedTextField` with: a location `OutlinedTextField` (value `uiState.locationQuery`, `onValueChange = viewModel::onLocationQueryChanged`, IME `ImeAction.Search` → `searchByQuery()`, clear button), a **Search** `Button` (enabled when query not blank && !isLoading), and a **"Use my location"** `OutlinedButton`/icon.
- [ ] Update state rendering: initial prompt when `!hasSearched && !isLoading && error == null`; keep loading/error/empty/results. `ErrorState.onRetry` → `viewModel.retryLastSearch()`. Loading text → "Searching…".
- [ ] Retitle TopAppBar to "Find Courses".
- [ ] `:app:compileDebugKotlin` → SUCCESS.

---

### Task 4: Build + manual test
- [ ] `:app:assembleDebug` → SUCCESS.
- [ ] `:app:installDebug`; run the manual test plan from the spec.
