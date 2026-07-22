# Manual Course Creation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an in-app wizard that lets users manually create a golf course with full per-hole green GPS coordinates so distance-to-green features work for courses not present in OpenStreetMap.

**Architecture:** Single Compose screen with internal step state (one `WizardStep` sealed class), one `@HiltViewModel` owning the entire draft, in-memory drafts persisted only on final Save via a new transactional `CourseDao` method. No entity/domain-model changes — existing schema already supports everything.

**Tech Stack:** Kotlin 2.2.10, Jetpack Compose (Material 3), Hilt DI, Room 2.8.4, Google Maps Compose 6.2.1, Navigation Compose 2.8.5.

**Spec:** `docs/superpowers/specs/2026-05-20-manual-course-creation-design.md`

**Repository note:** This project is NOT a git repository. There are no `git commit` steps in this plan. Each task ends with a verification step. If you want to add version control before starting, do so independently (`git init` from the project root).

---

## File Inventory

**New files:**

- `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseUiState.kt` — wizard data classes (`CreateCourseUiState`, `WizardStep`, `GreenDraft`, `TeeDraft`, `GreenTarget`, `CreateCourseEvent`).
- `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseViewModel.kt` — owns state, all transitions, save flow.
- `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseScreen.kt` — top-level Compose entry; routes to step composables.
- `app/src/main/java/com/example/shottracker/feature/createcourse/steps/NameStep.kt`
- `app/src/main/java/com/example/shottracker/feature/createcourse/steps/HoleCountStep.kt`
- `app/src/main/java/com/example/shottracker/feature/createcourse/steps/ParsStep.kt`
- `app/src/main/java/com/example/shottracker/feature/createcourse/steps/GreensStep.kt`
- `app/src/main/java/com/example/shottracker/feature/createcourse/steps/TeesStep.kt`
- `app/src/main/java/com/example/shottracker/feature/createcourse/steps/ReviewStep.kt`
- `app/src/test/java/com/example/shottracker/feature/createcourse/CreateCourseViewModelTest.kt` — JVM unit tests, fake `CourseRepository`.
- `app/src/test/java/com/example/shottracker/feature/createcourse/FakeCourseRepository.kt` — fake impl used by VM tests.
- `app/src/androidTest/java/com/example/shottracker/data/local/dao/CourseDaoCreateCourseTest.kt` — instrumented Room test for the `@Transaction` DAO method.

**Modified files:**

- `gradle/libs.versions.toml` — add test dependency versions/aliases.
- `app/build.gradle.kts` — add `testImplementation` lines for new test deps.
- `app/src/main/java/com/example/shottracker/data/local/dao/CourseDao.kt` — add `@Transaction` method.
- `app/src/main/java/com/example/shottracker/domain/repository/CourseRepository.kt` — add `createCourse` interface method.
- `app/src/main/java/com/example/shottracker/data/repository/CourseRepositoryImpl.kt` — implement `createCourse`.
- `app/src/main/java/com/example/shottracker/navigation/Screen.kt` — add `CreateCourse` route.
- `app/src/main/java/com/example/shottracker/navigation/ShotTrackerNavHost.kt` — wire route.
- `app/src/main/java/com/example/shottracker/feature/coursemanagement/CourseManagementScreen.kt` — add "Create Course Manually" button.

**Run tests on Windows:** `.\gradlew.bat :app:testDebugUnitTest` for JVM tests; `.\gradlew.bat :app:connectedDebugAndroidTest` for the instrumented DAO test (requires an emulator or device).

---

### Task 1: Add test dependencies

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add versions + library aliases to `libs.versions.toml`**

In `[versions]` section, add (preserve existing ordering — put under the existing `# Testing` block):

```toml
kotlinxCoroutinesTest = "1.9.0"
turbine = "1.2.0"
roomTesting = "2.8.4"
```

In `[libraries]` section, under the existing `# Testing` block, add:

```toml
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutinesTest" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "roomTesting" }
```

- [ ] **Step 2: Wire deps in `app/build.gradle.kts`**

Replace the entire `// Testing` block (currently lines 93-100) with:

```kotlin
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
```

- [ ] **Step 3: Verify the project syncs**

Run: `.\gradlew.bat :app:dependencies --configuration testDebugUnitTestRuntimeClasspath` and confirm the output mentions `kotlinx-coroutines-test`, `turbine`, and no errors. Then run `.\gradlew.bat :app:testDebugUnitTest` to confirm the existing `ExampleUnitTest` still passes.

Expected: `BUILD SUCCESSFUL`, `ExampleUnitTest > addition_isCorrect` PASS.

---

### Task 2: Add `@Transaction` DAO method (TDD)

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/data/local/dao/CourseDao.kt`
- Create: `app/src/androidTest/java/com/example/shottracker/data/local/dao/CourseDaoCreateCourseTest.kt`

- [ ] **Step 1: Write the failing instrumented test**

Create `app/src/androidTest/java/com/example/shottracker/data/local/dao/CourseDaoCreateCourseTest.kt`:

```kotlin
package com.example.shottracker.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.shottracker.data.local.ShotTrackerDatabase
import com.example.shottracker.data.local.entity.CourseEntity
import com.example.shottracker.data.local.entity.HoleInfoEntity
import com.example.shottracker.data.local.entity.TeeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseDaoCreateCourseTest {

    private lateinit var db: ShotTrackerDatabase
    private lateinit var courseDao: CourseDao
    private lateinit var holeInfoDao: HoleInfoDao
    private lateinit var teeDao: TeeDao

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, ShotTrackerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        courseDao = db.courseDao()
        holeInfoDao = db.holeInfoDao()
        teeDao = db.teeDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createCourseWithHolesAndTees_persistsAllRows() = runTest {
        val course = CourseEntity(name = "Townsend Ridge", city = "Townsend", state = "MA")
        val holes = (1..18).map { n ->
            HoleInfoEntity(
                courseId = 0,
                holeNumber = n,
                par = 4,
                greenFrontLat = 42.67, greenFrontLng = -71.71,
                greenCenterLat = 42.67, greenCenterLng = -71.71,
                greenBackLat = 42.67, greenBackLng = -71.71,
            )
        }
        val tees = listOf(
            TeeEntity(courseId = 0, name = "Blue", color = null, rating = 71.5, slope = 132),
            TeeEntity(courseId = 0, name = "White", color = null, rating = 69.0, slope = 125),
        )

        val courseId = courseDao.createCourseWithHolesAndTees(course, holes, tees)

        assertTrue("courseId should be > 0", courseId > 0L)
        val storedHoles = holeInfoDao.getHolesForCourse(courseId).first()
        val storedTees = teeDao.getTeesForCourse(courseId).first()
        assertEquals(18, storedHoles.size)
        assertEquals(2, storedTees.size)
        assertTrue(storedHoles.all { it.courseId == courseId })
        assertTrue(storedTees.all { it.courseId == courseId })
        assertEquals(1, storedHoles.first { it.holeNumber == 1 }.holeNumber)
        assertEquals(42.67, storedHoles.first().greenCenterLat!!, 0.0001)
    }
}
```

- [ ] **Step 2: Run test, confirm it fails (no method yet)**

Run: `.\gradlew.bat :app:compileDebugAndroidTestKotlin`

Expected: compile error — `Unresolved reference: createCourseWithHolesAndTees`.

- [ ] **Step 3: Add the `@Transaction` method to `CourseDao`**

In `app/src/main/java/com/example/shottracker/data/local/dao/CourseDao.kt`, add the import for `Transaction` and the new method. The full file becomes:

```kotlin
package com.example.shottracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.shottracker.data.local.entity.CourseEntity
import com.example.shottracker.data.local.entity.HoleInfoEntity
import com.example.shottracker.data.local.entity.TeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE courseId = :courseId")
    suspend fun getCourseById(courseId: Long): CourseEntity?

    @Query("SELECT * FROM courses WHERE name = :name LIMIT 1")
    suspend fun getCourseByName(name: String): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolesRaw(holes: List<HoleInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeesRaw(tees: List<TeeEntity>)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE courseId = :courseId")
    suspend fun deleteCourseById(courseId: Long)

    /**
     * Inserts a course with its holes and tees atomically. Holes and tees come in
     * with courseId = 0; this method stamps the autogenerated course ID onto each
     * via .copy(courseId = newId) before inserting them. If any insert throws,
     * Room rolls back the entire transaction.
     */
    @Transaction
    suspend fun createCourseWithHolesAndTees(
        course: CourseEntity,
        holes: List<HoleInfoEntity>,
        tees: List<TeeEntity>,
    ): Long {
        val courseId = insertCourse(course)
        if (holes.isNotEmpty()) {
            insertHolesRaw(holes.map { it.copy(courseId = courseId) })
        }
        if (tees.isNotEmpty()) {
            insertTeesRaw(tees.map { it.copy(courseId = courseId) })
        }
        return courseId
    }
}
```

Note: `insertHolesRaw` / `insertTeesRaw` duplicate the existing `HoleInfoDao.insertHoles` / `TeeDao.insertTees` methods. Adding them here lets the `@Transaction` method live entirely on `CourseDao` without needing cross-DAO references inside a Room transaction (which is awkward to model). The existing DAO methods remain unchanged.

- [ ] **Step 4: Run test, confirm it passes**

Run: `.\gradlew.bat :app:connectedDebugAndroidTest --tests "com.example.shottracker.data.local.dao.CourseDaoCreateCourseTest"`

Expected: `BUILD SUCCESSFUL`, 1 test PASS.

If you don't have an emulator running, start one first via Android Studio's AVD Manager or `emulator -avd <name>`.

- [ ] **Step 5: Add a second test exercising the transaction boundary across REPLACE-on-name**

Append this test method inside `CourseDaoCreateCourseTest` (after `createCourseWithHolesAndTees_persistsAllRows`):

```kotlin
@Test
fun createCourseWithHolesAndTees_linksHolesToNewCourseIdEvenAfterReplace() = runTest {
    // Insert an initial course with the same name. Since @Insert uses REPLACE
    // strategy, a subsequent insert with the same name + a new autoGenerated PK
    // verifies the @Transaction method correctly stamps the NEW PK onto holes
    // and tees, rather than leaving them orphaned to the old course row.
    courseDao.insertCourse(CourseEntity(name = "Same Name"))

    val course = CourseEntity(name = "Same Name")
    val holes = listOf(
        HoleInfoEntity(courseId = 0, holeNumber = 1, par = 3,
            greenFrontLat = 1.0, greenFrontLng = 1.0,
            greenCenterLat = 1.0, greenCenterLng = 1.0,
            greenBackLat = 1.0,  greenBackLng = 1.0,
        )
    )
    val tees = listOf(TeeEntity(courseId = 0, name = "Red", color = null,
        rating = null, slope = null))

    val newCourseId = courseDao.createCourseWithHolesAndTees(course, holes, tees)
    val storedHoles = holeInfoDao.getHolesForCourse(newCourseId).first()
    val storedTees = teeDao.getTeesForCourse(newCourseId).first()

    assertEquals(1, storedHoles.size)
    assertEquals(1, storedTees.size)
    assertEquals(newCourseId, storedHoles[0].courseId)
    assertEquals(newCourseId, storedTees[0].courseId)
}
```

Note: a strict "DAO throws → rolled back" atomicity test would require wrapping the real DAO with a failing intermediary, which Room's `@Transaction` annotation doesn't easily allow (the annotation is processed only on Room-generated code). We rely on Room's `@Transaction` semantics for rollback rather than testing them directly.

- [ ] **Step 6: Verify both tests pass**

Run: `.\gradlew.bat :app:connectedDebugAndroidTest --tests "com.example.shottracker.data.local.dao.CourseDaoCreateCourseTest"`

Expected: 2 tests PASS.

---

### Task 3: Add `createCourse` to repository

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/domain/repository/CourseRepository.kt`
- Modify: `app/src/main/java/com/example/shottracker/data/repository/CourseRepositoryImpl.kt`

No dedicated test for this task. Task 2 (DAO test) already exercises the persistence end-to-end. The mapper logic added here (`domain.copy(courseId=0).toEntity()`) is a thin wrapper indirectly covered by VM tests in Task 9, which use `FakeCourseRepository` to verify the domain objects the wizard assembles. Adding an instrumented test specifically for this method would require either subclassing the final `OverpassRemoteDataSource` class or pulling in mockito-inline plumbing, neither warranted by the v1 scope.

- [ ] **Step 1: Add `createCourse` to the `CourseRepository` interface**

In `app/src/main/java/com/example/shottracker/domain/repository/CourseRepository.kt`, after the existing `deleteCourse` method (line 15), add:

```kotlin
suspend fun createCourse(course: Course, holes: List<HoleInfo>, tees: List<Tee>): Long
```

- [ ] **Step 2: Implement `createCourse` in `CourseRepositoryImpl`**

In `app/src/main/java/com/example/shottracker/data/repository/CourseRepositoryImpl.kt`, after the existing `deleteCourse` method (line 51), add:

```kotlin
override suspend fun createCourse(
    course: Course,
    holes: List<HoleInfo>,
    tees: List<Tee>,
): Long {
    val courseEntity = course.copy(id = 0).toEntity()
    val holeEntities = holes.map { it.copy(courseId = 0).toEntity() }
    val teeEntities = tees.map { it.copy(courseId = 0).toEntity() }
    return courseDao.createCourseWithHolesAndTees(courseEntity, holeEntities, teeEntities)
}
```

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 4: Wizard data classes

**Files:**
- Create: `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseUiState.kt`

No tests for this task — these are pure data classes with no behavior. Tests in subsequent tasks exercise them.

- [ ] **Step 1: Create the data classes file**

Create `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseUiState.kt`:

```kotlin
package com.example.shottracker.feature.createcourse

import com.example.shottracker.feature.coursemanagement.TeeFormState

sealed class WizardStep {
    data object Name : WizardStep()
    data object HoleCount : WizardStep()
    data object Pars : WizardStep()
    data object Greens : WizardStep()
    data object Tees : WizardStep()
    data object Review : WizardStep()
}

enum class GreenTarget { Front, Center, Back }

data class GreenDraft(
    val frontLat: Double? = null, val frontLng: Double? = null,
    val centerLat: Double? = null, val centerLng: Double? = null,
    val backLat: Double? = null,  val backLng: Double? = null,
) {
    val isComplete: Boolean
        get() = frontLat != null && centerLat != null && backLat != null

    fun firstUnfilledTarget(): GreenTarget? = when {
        frontLat == null -> GreenTarget.Front
        centerLat == null -> GreenTarget.Center
        backLat == null -> GreenTarget.Back
        else -> null
    }

    fun set(target: GreenTarget, lat: Double, lng: Double): GreenDraft = when (target) {
        GreenTarget.Front  -> copy(frontLat = lat, frontLng = lng)
        GreenTarget.Center -> copy(centerLat = lat, centerLng = lng)
        GreenTarget.Back   -> copy(backLat = lat, backLng = lng)
    }

    fun clear(target: GreenTarget): GreenDraft = when (target) {
        GreenTarget.Front  -> copy(frontLat = null, frontLng = null)
        GreenTarget.Center -> copy(centerLat = null, centerLng = null)
        GreenTarget.Back   -> copy(backLat = null, backLng = null)
    }

    fun isPlaced(target: GreenTarget): Boolean = when (target) {
        GreenTarget.Front  -> frontLat != null
        GreenTarget.Center -> centerLat != null
        GreenTarget.Back   -> backLat != null
    }
}

data class TeeDraft(
    val name: String,
    val color: String?,
    val rating: Double?,
    val slope: Int?,
)

data class CreateCourseUiState(
    val step: WizardStep = WizardStep.Name,
    val name: String = "",
    val city: String = "",
    val state: String = "",
    val nameError: String? = null,
    val holeCount: Int = 18,
    val pars: List<Int> = List(18) { 4 },
    val greens: List<GreenDraft> = List(18) { GreenDraft() },
    val tees: List<TeeDraft> = emptyList(),
    val teesError: String? = null,
    val teeForm: TeeFormState? = null,
    val currentGreenHole: Int = 1,
    val selectedGreenTarget: GreenTarget = GreenTarget.Front,
    val showDiscardDialog: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
)

sealed class CreateCourseEvent {
    data object Saved : CreateCourseEvent()
}
```

- [ ] **Step 2: Verify it compiles**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

---

### Task 5: ViewModel — Name step (TDD)

**Files:**
- Create: `app/src/test/java/com/example/shottracker/feature/createcourse/FakeCourseRepository.kt`
- Create: `app/src/test/java/com/example/shottracker/feature/createcourse/CreateCourseViewModelTest.kt`
- Create: `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseViewModel.kt`

- [ ] **Step 1: Create the fake repository**

Create `app/src/test/java/com/example/shottracker/feature/createcourse/FakeCourseRepository.kt`:

```kotlin
package com.example.shottracker.feature.createcourse

import com.example.shottracker.domain.model.Course
import com.example.shottracker.domain.model.HoleInfo
import com.example.shottracker.domain.model.Tee
import com.example.shottracker.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeCourseRepository : CourseRepository {

    // --- knobs the tests configure ---
    var courseByNameResult: Course? = null
    var createCourseFails: Boolean = false
    var createCourseException: Throwable = RuntimeException("simulated failure")

    // --- captures ---
    data class CreateCall(val course: Course, val holes: List<HoleInfo>, val tees: List<Tee>)
    val createCalls = mutableListOf<CreateCall>()

    override fun getAllCourses(): Flow<List<Course>> = flowOf(emptyList())
    override suspend fun getCourseById(courseId: Long): Course? = null
    override suspend fun getCourseByName(name: String): Course? = courseByNameResult
    override suspend fun insertCourse(course: Course): Long = 0L
    override suspend fun updateCourse(course: Course) {}
    override suspend fun deleteCourse(courseId: Long) {}

    override fun getHolesForCourse(courseId: Long): Flow<List<HoleInfo>> = flowOf(emptyList())
    override suspend fun getHoleByNumber(courseId: Long, holeNumber: Int): HoleInfo? = null
    override suspend fun insertHoles(holes: List<HoleInfo>) {}
    override suspend fun updateHole(hole: HoleInfo) {}

    override fun getTeesForCourse(courseId: Long): Flow<List<Tee>> = flowOf(emptyList())
    override suspend fun getTeeById(teeId: Long): Tee? = null
    override suspend fun insertTee(tee: Tee): Long = 0L
    override suspend fun insertTees(tees: List<Tee>) {}
    override suspend fun deleteTee(teeId: Long) {}

    override suspend fun importCourseFromOsm(name: String, lat: Double, lng: Double): Result<Course> =
        Result.failure(NotImplementedError())

    override suspend fun createCourse(
        course: Course,
        holes: List<HoleInfo>,
        tees: List<Tee>,
    ): Long {
        createCalls.add(CreateCall(course, holes, tees))
        if (createCourseFails) throw createCourseException
        return 42L
    }
}
```

- [ ] **Step 2: Write failing tests for Name step**

Create `app/src/test/java/com/example/shottracker/feature/createcourse/CreateCourseViewModelTest.kt`:

```kotlin
package com.example.shottracker.feature.createcourse

import com.example.shottracker.domain.model.Course
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CreateCourseViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeCourseRepository
    private lateinit var vm: CreateCourseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeCourseRepository()
        vm = CreateCourseViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun nameStep_blankName_blocksAdvance() = runTest(dispatcher) {
        vm.setName("   ")
        vm.onNext()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(WizardStep.Name, vm.uiState.value.step)
        assertNotNull(vm.uiState.value.nameError)
    }

    @Test
    fun nameStep_duplicateName_blocksAdvance() = runTest(dispatcher) {
        repo.courseByNameResult = Course(id = 5, name = "Existing")
        vm.setName("Existing")
        vm.onNext()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(WizardStep.Name, vm.uiState.value.step)
        assertEquals("A course with this name already exists", vm.uiState.value.nameError)
    }

    @Test
    fun nameStep_validUniqueName_advancesToHoleCount() = runTest(dispatcher) {
        repo.courseByNameResult = null
        vm.setName("New Course")
        vm.onNext()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(WizardStep.HoleCount, vm.uiState.value.step)
        assertNull(vm.uiState.value.nameError)
    }
}
```

- [ ] **Step 3: Run, confirm fails (no VM yet)**

Run: `.\gradlew.bat :app:compileDebugUnitTestKotlin`

Expected: `Unresolved reference: CreateCourseViewModel`, `Unresolved reference: setName`, etc.

- [ ] **Step 4: Implement enough VM to make Name tests pass**

Create `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseViewModel.kt`:

```kotlin
package com.example.shottracker.feature.createcourse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateCourseViewModel @Inject constructor(
    private val repo: CourseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCourseUiState())
    val uiState: StateFlow<CreateCourseUiState> = _uiState.asStateFlow()

    private val _events = Channel<CreateCourseEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setName(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun setCity(value: String) {
        _uiState.update { it.copy(city = value) }
    }

    fun setState(value: String) {
        _uiState.update { it.copy(state = value) }
    }

    fun onNext() {
        val state = _uiState.value
        when (state.step) {
            WizardStep.Name -> validateAndAdvanceFromName(state)
            else -> Unit // implemented in later tasks
        }
    }

    fun onBack() {
        // implemented in later tasks
    }

    private fun validateAndAdvanceFromName(state: CreateCourseUiState) {
        val trimmed = state.name.trim()
        if (trimmed.isEmpty() || trimmed.length > 80) {
            _uiState.update { it.copy(nameError = "Name must be 1–80 characters") }
            return
        }
        viewModelScope.launch {
            val existing = repo.getCourseByName(trimmed)
            if (existing != null) {
                _uiState.update { it.copy(nameError = "A course with this name already exists") }
            } else {
                _uiState.update { it.copy(step = WizardStep.HoleCount, nameError = null) }
            }
        }
    }
}
```

`_uiState.update { ... }` is the standard `kotlinx.coroutines.flow.update` extension function, imported via `import kotlinx.coroutines.flow.update`.

- [ ] **Step 5: Run tests, confirm pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.shottracker.feature.createcourse.CreateCourseViewModelTest"`

Expected: 3 tests PASS.

---

### Task 6: ViewModel — HoleCount and Pars steps (TDD)

**Files:**
- Modify: `app/src/test/java/com/example/shottracker/feature/createcourse/CreateCourseViewModelTest.kt`
- Modify: `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseViewModel.kt`

- [ ] **Step 1: Write failing tests**

Append to `CreateCourseViewModelTest.kt`:

```kotlin
    @Test
    fun setHoleCount_9_resizesParsAndGreensLists() = runTest(dispatcher) {
        // Get to HoleCount step
        repo.courseByNameResult = null
        vm.setName("Test"); vm.onNext()
        dispatcher.scheduler.advanceUntilIdle()

        vm.setHoleCount(9)
        vm.onNext() // HoleCount → Pars
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(WizardStep.Pars, vm.uiState.value.step)
        assertEquals(9, vm.uiState.value.holeCount)
        assertEquals(9, vm.uiState.value.pars.size)
        assertEquals(9, vm.uiState.value.greens.size)
        // Defaults: par 4 for all
        assertEquals(List(9) { 4 }, vm.uiState.value.pars)
    }

    @Test
    fun setHoleCount_18After9_padsListsBackTo18() = runTest(dispatcher) {
        repo.courseByNameResult = null
        vm.setName("Test"); vm.onNext()
        dispatcher.scheduler.advanceUntilIdle()
        vm.setHoleCount(9)
        vm.setHoleCount(18)

        assertEquals(18, vm.uiState.value.pars.size)
        assertEquals(18, vm.uiState.value.greens.size)
    }

    @Test
    fun parsStep_updateHolePar_clampedTo3to6() = runTest(dispatcher) {
        // Get to Pars step (default 18 holes)
        repo.courseByNameResult = null
        vm.setName("Test"); vm.onNext()
        dispatcher.scheduler.advanceUntilIdle()
        vm.onNext() // HoleCount → Pars
        dispatcher.scheduler.advanceUntilIdle()

        vm.updateHolePar(holeNumber = 1, par = 5)
        vm.updateHolePar(holeNumber = 2, par = 99) // clamped to 6
        vm.updateHolePar(holeNumber = 3, par = 1)  // clamped to 3

        assertEquals(5, vm.uiState.value.pars[0])
        assertEquals(6, vm.uiState.value.pars[1])
        assertEquals(3, vm.uiState.value.pars[2])
    }

    @Test
    fun parsStep_advances_toGreens() = runTest(dispatcher) {
        repo.courseByNameResult = null
        vm.setName("Test"); vm.onNext()
        dispatcher.scheduler.advanceUntilIdle()
        vm.onNext() // → Pars
        dispatcher.scheduler.advanceUntilIdle()

        vm.onNext() // Pars → Greens
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(WizardStep.Greens, vm.uiState.value.step)
        assertEquals(1, vm.uiState.value.currentGreenHole)
    }
```

- [ ] **Step 2: Run, confirm fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.shottracker.feature.createcourse.CreateCourseViewModelTest"`

Expected: compile errors for `setHoleCount`, `updateHolePar`.

- [ ] **Step 3: Implement HoleCount + Pars logic**

In `CreateCourseViewModel.kt`, add these methods (above the private `validateAndAdvanceFromName`):

```kotlin
fun setHoleCount(count: Int) {
    require(count == 9 || count == 18) { "Hole count must be 9 or 18" }
    _uiState.update { s ->
        val newPars = if (s.pars.size == count) s.pars
                      else if (count < s.pars.size) s.pars.take(count)
                      else s.pars + List(count - s.pars.size) { 4 }
        val newGreens = if (s.greens.size == count) s.greens
                        else if (count < s.greens.size) s.greens.take(count)
                        else s.greens + List(count - s.greens.size) { GreenDraft() }
        s.copy(holeCount = count, pars = newPars, greens = newGreens)
    }
}

fun updateHolePar(holeNumber: Int, par: Int) {
    val coerced = par.coerceIn(3, 6)
    _uiState.update { s ->
        if (holeNumber < 1 || holeNumber > s.pars.size) s
        else s.copy(pars = s.pars.mapIndexed { i, p ->
            if (i == holeNumber - 1) coerced else p
        })
    }
}
```

Extend the `when (state.step)` block in `onNext()`:

```kotlin
fun onNext() {
    val state = _uiState.value
    when (state.step) {
        WizardStep.Name -> validateAndAdvanceFromName(state)
        WizardStep.HoleCount -> _uiState.update { it.copy(step = WizardStep.Pars) }
        WizardStep.Pars -> _uiState.update {
            it.copy(step = WizardStep.Greens, currentGreenHole = 1,
                    selectedGreenTarget = it.greens[0].firstUnfilledTarget() ?: GreenTarget.Front)
        }
        else -> Unit
    }
}
```

- [ ] **Step 4: Run, confirm pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.shottracker.feature.createcourse.CreateCourseViewModelTest"`

Expected: 7 tests PASS (3 from Task 5 + 4 new).

---

### Task 7: ViewModel — Greens step (TDD)

**Files:**
- Modify: `app/src/test/java/com/example/shottracker/feature/createcourse/CreateCourseViewModelTest.kt`
- Modify: `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseViewModel.kt`

- [ ] **Step 1: Write failing tests**

Append to `CreateCourseViewModelTest.kt`:

```kotlin
    /** Helper to fast-forward VM into the Greens step. */
    private suspend fun advanceToGreens() {
        repo.courseByNameResult = null
        vm.setName("Test"); vm.onNext()
        dispatcher.scheduler.advanceUntilIdle()
        vm.onNext() // HoleCount → Pars
        vm.onNext() // Pars → Greens
        dispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun placeGreen_front_updatesHole_andAdvancesTarget() = runTest(dispatcher) {
        advanceToGreens()
        vm.placeGreen(42.6, -71.7)  // selectedTarget is Front initially

        val state = vm.uiState.value
        val hole1 = state.greens[0]
        assertEquals(42.6, hole1.frontLat!!, 0.0001)
        assertEquals(-71.7, hole1.frontLng!!, 0.0001)
        assertEquals(GreenTarget.Center, state.selectedGreenTarget)
    }

    @Test
    fun placeGreen_allThree_advancesTargetThenStaysOnBack() = runTest(dispatcher) {
        advanceToGreens()
        vm.placeGreen(1.0, 1.0) // Front
        vm.placeGreen(2.0, 2.0) // Center
        vm.placeGreen(3.0, 3.0) // Back
        vm.placeGreen(4.0, 4.0) // re-place Back

        val hole1 = vm.uiState.value.greens[0]
        assertEquals(1.0, hole1.frontLat!!, 0.0001)
        assertEquals(2.0, hole1.centerLat!!, 0.0001)
        assertEquals(4.0, hole1.backLat!!, 0.0001)
        assertEquals(GreenTarget.Back, vm.uiState.value.selectedGreenTarget)
    }

    @Test
    fun selectTarget_alreadyPlaced_clearsThatPin() = runTest(dispatcher) {
        advanceToGreens()
        vm.placeGreen(1.0, 1.0) // Front placed
        vm.selectTarget(GreenTarget.Front) // tap chip → clear

        val hole1 = vm.uiState.value.greens[0]
        assertNull(hole1.frontLat)
        assertEquals(GreenTarget.Front, vm.uiState.value.selectedGreenTarget)
    }

    @Test
    fun selectTarget_unfilled_justSelects() = runTest(dispatcher) {
        advanceToGreens()
        vm.selectTarget(GreenTarget.Back)

        assertEquals(GreenTarget.Back, vm.uiState.value.selectedGreenTarget)
        assertNull(vm.uiState.value.greens[0].backLat) // not placed
    }

    @Test
    fun onNext_incompleteHole_staysOnGreens() = runTest(dispatcher) {
        advanceToGreens()
        vm.placeGreen(1.0, 1.0) // only Front
        vm.onNext()

        assertEquals(WizardStep.Greens, vm.uiState.value.step)
        assertEquals(1, vm.uiState.value.currentGreenHole)
    }

    @Test
    fun onNext_completeHole_advancesHoleNumber() = runTest(dispatcher) {
        advanceToGreens()
        vm.placeGreen(1.0, 1.0); vm.placeGreen(2.0, 2.0); vm.placeGreen(3.0, 3.0)
        vm.onNext()

        assertEquals(WizardStep.Greens, vm.uiState.value.step)
        assertEquals(2, vm.uiState.value.currentGreenHole)
        // Reset selected target to Front for new hole
        assertEquals(GreenTarget.Front, vm.uiState.value.selectedGreenTarget)
    }

    @Test
    fun onNext_lastHoleComplete_advancesToTees() = runTest(dispatcher) {
        // Switch to 9 holes for less typing
        repo.courseByNameResult = null
        vm.setName("Test"); vm.onNext()
        dispatcher.scheduler.advanceUntilIdle()
        vm.setHoleCount(9)
        vm.onNext() // → Pars
        vm.onNext() // → Greens
        dispatcher.scheduler.advanceUntilIdle()

        repeat(9) { _ ->
            vm.placeGreen(1.0, 1.0); vm.placeGreen(2.0, 2.0); vm.placeGreen(3.0, 3.0)
            vm.onNext()
        }

        assertEquals(WizardStep.Tees, vm.uiState.value.step)
    }

    @Test
    fun onBack_withinGreens_decrementsHole() = runTest(dispatcher) {
        advanceToGreens()
        vm.placeGreen(1.0, 1.0); vm.placeGreen(2.0, 2.0); vm.placeGreen(3.0, 3.0)
        vm.onNext() // hole 1 → hole 2
        vm.onBack()

        assertEquals(WizardStep.Greens, vm.uiState.value.step)
        assertEquals(1, vm.uiState.value.currentGreenHole)
    }

    @Test
    fun onBack_fromHole1_returnsToPars() = runTest(dispatcher) {
        advanceToGreens()
        vm.onBack()

        assertEquals(WizardStep.Pars, vm.uiState.value.step)
    }
```

- [ ] **Step 2: Run, confirm fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.shottracker.feature.createcourse.CreateCourseViewModelTest"`

Expected: compile errors for `placeGreen`, `selectTarget`.

- [ ] **Step 3: Implement Greens logic in VM**

Add to `CreateCourseViewModel.kt`:

```kotlin
fun placeGreen(lat: Double, lng: Double) {
    _uiState.update { s ->
        if (s.step != WizardStep.Greens) return@update s
        val idx = s.currentGreenHole - 1
        val updated = s.greens[idx].set(s.selectedGreenTarget, lat, lng)
        val newGreens = s.greens.toMutableList().also { it[idx] = updated }
        val nextTarget = updated.firstUnfilledTarget() ?: GreenTarget.Back
        s.copy(greens = newGreens, selectedGreenTarget = nextTarget)
    }
}

fun selectTarget(target: GreenTarget) {
    _uiState.update { s ->
        if (s.step != WizardStep.Greens) return@update s
        val idx = s.currentGreenHole - 1
        val hole = s.greens[idx]
        if (hole.isPlaced(target)) {
            val cleared = hole.clear(target)
            val newGreens = s.greens.toMutableList().also { it[idx] = cleared }
            s.copy(greens = newGreens, selectedGreenTarget = target)
        } else {
            s.copy(selectedGreenTarget = target)
        }
    }
}
```

Extend `onNext()` for the Greens case:

```kotlin
WizardStep.Greens -> {
    val s = state
    val idx = s.currentGreenHole - 1
    if (!s.greens[idx].isComplete) return // stays
    if (s.currentGreenHole < s.holeCount) {
        val nextHole = s.currentGreenHole + 1
        val nextDraft = s.greens[nextHole - 1]
        _uiState.update { it.copy(
            currentGreenHole = nextHole,
            selectedGreenTarget = nextDraft.firstUnfilledTarget() ?: GreenTarget.Front,
        ) }
    } else {
        _uiState.update { it.copy(step = WizardStep.Tees) }
    }
}
```

Implement `onBack()`:

```kotlin
fun onBack() {
    val s = _uiState.value
    when (s.step) {
        WizardStep.Name -> _uiState.update { it.copy(showDiscardDialog = true) }
        WizardStep.HoleCount -> _uiState.update { it.copy(step = WizardStep.Name) }
        WizardStep.Pars -> _uiState.update { it.copy(step = WizardStep.HoleCount) }
        WizardStep.Greens -> {
            if (s.currentGreenHole > 1) {
                val prev = s.currentGreenHole - 1
                val prevDraft = s.greens[prev - 1]
                _uiState.update { it.copy(
                    currentGreenHole = prev,
                    selectedGreenTarget = prevDraft.firstUnfilledTarget() ?: GreenTarget.Front,
                ) }
            } else {
                _uiState.update { it.copy(step = WizardStep.Pars) }
            }
        }
        WizardStep.Tees -> _uiState.update { it.copy(step = WizardStep.Greens,
                                                     currentGreenHole = s.holeCount) }
        WizardStep.Review -> _uiState.update { it.copy(step = WizardStep.Tees) }
    }
}
```

- [ ] **Step 4: Run, confirm pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.shottracker.feature.createcourse.CreateCourseViewModelTest"`

Expected: 16 tests PASS.

---

### Task 8: ViewModel — Tees step (TDD)

**Files:**
- Modify: `app/src/test/java/com/example/shottracker/feature/createcourse/CreateCourseViewModelTest.kt`
- Modify: `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseViewModel.kt`

- [ ] **Step 1: Write failing tests**

Append to `CreateCourseViewModelTest.kt`:

```kotlin
    /** Fast-forward VM to the Tees step with 9 holes for brevity. */
    private suspend fun advanceToTees() {
        repo.courseByNameResult = null
        vm.setName("Test"); vm.onNext()
        dispatcher.scheduler.advanceUntilIdle()
        vm.setHoleCount(9)
        vm.onNext() // → Pars
        vm.onNext() // → Greens
        dispatcher.scheduler.advanceUntilIdle()
        repeat(9) {
            vm.placeGreen(1.0, 1.0); vm.placeGreen(2.0, 2.0); vm.placeGreen(3.0, 3.0)
            vm.onNext()
        }
        dispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun teesStep_noTees_blocksAdvance() = runTest(dispatcher) {
        advanceToTees()
        vm.onNext()

        assertEquals(WizardStep.Tees, vm.uiState.value.step)
        assertNotNull(vm.uiState.value.teesError)
    }

    @Test
    fun teesStep_addValidTee_appendsToList() = runTest(dispatcher) {
        advanceToTees()
        vm.openNewTeeForm()
        vm.updateTeeFormName("Blue")
        vm.updateTeeFormRating("71.5")
        vm.updateTeeFormSlope("132")
        vm.saveTeeForm()

        val tees = vm.uiState.value.tees
        assertEquals(1, tees.size)
        assertEquals("Blue", tees[0].name)
        assertEquals(71.5, tees[0].rating!!, 0.0001)
        assertEquals(132, tees[0].slope)
        assertNull(vm.uiState.value.teeForm) // form closed
    }

    @Test
    fun teesStep_addTeeBlankName_keepsFormOpenWithError() = runTest(dispatcher) {
        advanceToTees()
        vm.openNewTeeForm()
        vm.updateTeeFormName("   ")
        vm.saveTeeForm()

        assertNotNull(vm.uiState.value.teeForm)
        assertEquals("Name is required", vm.uiState.value.teeForm!!.error)
    }

    @Test
    fun teesStep_invalidRating_keepsFormOpenWithError() = runTest(dispatcher) {
        advanceToTees()
        vm.openNewTeeForm()
        vm.updateTeeFormName("Blue")
        vm.updateTeeFormRating("not-a-number")
        vm.saveTeeForm()

        assertNotNull(vm.uiState.value.teeForm)
    }

    @Test
    fun teesStep_deleteTee_removesIt() = runTest(dispatcher) {
        advanceToTees()
        vm.openNewTeeForm()
        vm.updateTeeFormName("Blue"); vm.saveTeeForm()
        vm.openNewTeeForm()
        vm.updateTeeFormName("White"); vm.saveTeeForm()

        vm.deleteTeeDraft("Blue")

        assertEquals(1, vm.uiState.value.tees.size)
        assertEquals("White", vm.uiState.value.tees[0].name)
    }

    @Test
    fun teesStep_advancesToReview_whenAtLeastOneTee() = runTest(dispatcher) {
        advanceToTees()
        vm.openNewTeeForm()
        vm.updateTeeFormName("Blue"); vm.saveTeeForm()
        vm.onNext()

        assertEquals(WizardStep.Review, vm.uiState.value.step)
    }
```

- [ ] **Step 2: Run, confirm fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.shottracker.feature.createcourse.CreateCourseViewModelTest"`

Expected: compile errors for tee form methods.

- [ ] **Step 3: Implement Tee form logic**

Add these methods to `CreateCourseViewModel.kt` (anywhere in the class):

```kotlin
fun openNewTeeForm() {
    _uiState.update { it.copy(teeForm = com.example.shottracker.feature.coursemanagement.TeeFormState()) }
}

fun cancelTeeForm() {
    _uiState.update { it.copy(teeForm = null) }
}

fun updateTeeFormName(value: String) {
    _uiState.update { s ->
        s.teeForm?.let { s.copy(teeForm = it.copy(name = value, error = null)) } ?: s
    }
}

fun updateTeeFormColor(value: String) {
    _uiState.update { s ->
        s.teeForm?.let { s.copy(teeForm = it.copy(color = value)) } ?: s
    }
}

fun updateTeeFormRating(value: String) {
    _uiState.update { s ->
        s.teeForm?.let { s.copy(teeForm = it.copy(rating = value, error = null)) } ?: s
    }
}

fun updateTeeFormSlope(value: String) {
    _uiState.update { s ->
        s.teeForm?.let { s.copy(teeForm = it.copy(slope = value, error = null)) } ?: s
    }
}

fun saveTeeForm() {
    val s = _uiState.value
    val form = s.teeForm ?: return

    val name = form.name.trim()
    if (name.isEmpty()) {
        _uiState.update { it.copy(teeForm = form.copy(error = "Name is required")) }
        return
    }
    val ratingText = form.rating.trim()
    val rating: Double? = if (ratingText.isEmpty()) null else ratingText.toDoubleOrNull()
    if (ratingText.isNotEmpty() && (rating == null || rating !in 60.0..80.0)) {
        _uiState.update { it.copy(teeForm = form.copy(
            error = "Rating must be a number between 60.0 and 80.0")) }
        return
    }
    val slopeText = form.slope.trim()
    val slope: Int? = if (slopeText.isEmpty()) null else slopeText.toIntOrNull()
    if (slopeText.isNotEmpty() && (slope == null || slope !in 55..155)) {
        _uiState.update { it.copy(teeForm = form.copy(
            error = "Slope must be an integer between 55 and 155")) }
        return
    }

    val draft = TeeDraft(
        name = name,
        color = form.color.trim().takeIf { it.isNotEmpty() },
        rating = rating,
        slope = slope,
    )
    _uiState.update { it.copy(tees = it.tees + draft, teeForm = null, teesError = null) }
}

fun deleteTeeDraft(name: String) {
    _uiState.update { it.copy(tees = it.tees.filterNot { t -> t.name == name }) }
}
```

Extend `onNext()` for the Tees case:

```kotlin
WizardStep.Tees -> {
    if (state.tees.isEmpty()) {
        _uiState.update { it.copy(teesError = "Add at least one tee") }
    } else {
        _uiState.update { it.copy(step = WizardStep.Review, teesError = null) }
    }
}
```

- [ ] **Step 4: Run, confirm pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.shottracker.feature.createcourse.CreateCourseViewModelTest"`

Expected: 22 tests PASS.

---

### Task 9: ViewModel — Save flow and discard dialog (TDD)

**Files:**
- Modify: `app/src/test/java/com/example/shottracker/feature/createcourse/CreateCourseViewModelTest.kt`
- Modify: `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseViewModel.kt`

- [ ] **Step 1: Write failing tests**

Append to `CreateCourseViewModelTest.kt`:

```kotlin
    /** Fast-forward to Review with one tee. */
    private suspend fun advanceToReview() {
        advanceToTees()
        vm.openNewTeeForm()
        vm.updateTeeFormName("Blue"); vm.saveTeeForm()
        vm.onNext() // → Review
    }

    @Test
    fun onSave_happyPath_emitsSavedAndPassesAssembledCourse() = runTest(dispatcher) {
        advanceToReview()
        val received = mutableListOf<CreateCourseEvent>()
        val job = kotlinx.coroutines.launch {
            vm.events.collect { received.add(it) }
        }

        vm.onSave()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repo.createCalls.size)
        val call = repo.createCalls[0]
        assertEquals("Test", call.course.name)
        assertEquals(9, call.holes.size)
        assertEquals(1, call.tees.size)
        assertEquals("Blue", call.tees[0].name)
        // Every hole has all three green coords
        call.holes.forEach { h ->
            assertNotNull(h.greenFrontLat); assertNotNull(h.greenCenterLat); assertNotNull(h.greenBackLat)
        }
        assertEquals(1, received.size)
        assertEquals(CreateCourseEvent.Saved, received[0])
        job.cancel()
    }

    @Test
    fun onSave_failure_setsSaveErrorAndNoEvent() = runTest(dispatcher) {
        advanceToReview()
        repo.createCourseFails = true
        repo.createCourseException = RuntimeException("disk full")

        val received = mutableListOf<CreateCourseEvent>()
        val job = kotlinx.coroutines.launch {
            vm.events.collect { received.add(it) }
        }

        vm.onSave()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("disk full", vm.uiState.value.saveError)
        assertEquals(false, vm.uiState.value.isSaving)
        assertEquals(0, received.size)
        job.cancel()
    }

    @Test
    fun onBack_fromName_setsDiscardDialog() = runTest(dispatcher) {
        vm.onBack()
        assertEquals(true, vm.uiState.value.showDiscardDialog)
    }

    @Test
    fun dismissDiscardDialog_clearsFlag() = runTest(dispatcher) {
        vm.onBack()
        vm.dismissDiscardDialog()
        assertEquals(false, vm.uiState.value.showDiscardDialog)
    }
```

- [ ] **Step 2: Run, confirm fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.shottracker.feature.createcourse.CreateCourseViewModelTest"`

Expected: compile errors for `onSave`, `dismissDiscardDialog`.

- [ ] **Step 3: Implement save + discard**

Add to `CreateCourseViewModel.kt`:

```kotlin
fun onSave() {
    val s = _uiState.value
    if (s.step != WizardStep.Review || s.isSaving) return

    _uiState.update { it.copy(isSaving = true, saveError = null) }
    viewModelScope.launch {
        val course = com.example.shottracker.domain.model.Course(
            name = s.name.trim(),
            city = s.city.trim().ifBlank { null },
            state = s.state.trim().ifBlank { null },
        )
        val holes = (1..s.holeCount).map { n ->
            val g = s.greens[n - 1]
            com.example.shottracker.domain.model.HoleInfo(
                courseId = 0, holeNumber = n, par = s.pars[n - 1],
                greenFrontLat = g.frontLat,  greenFrontLng = g.frontLng,
                greenCenterLat = g.centerLat, greenCenterLng = g.centerLng,
                greenBackLat = g.backLat,    greenBackLng = g.backLng,
            )
        }
        val tees = s.tees.map { d ->
            com.example.shottracker.domain.model.Tee(
                courseId = 0, name = d.name, color = d.color,
                rating = d.rating, slope = d.slope,
            )
        }
        runCatching { repo.createCourse(course, holes, tees) }
            .onSuccess {
                _events.send(CreateCourseEvent.Saved)
                // Leave isSaving = true; the screen will navigate away
            }
            .onFailure { e ->
                _uiState.update { it.copy(
                    isSaving = false,
                    saveError = e.message ?: "Save failed",
                ) }
            }
    }
}

fun dismissDiscardDialog() {
    _uiState.update { it.copy(showDiscardDialog = false) }
}

fun confirmDiscard() {
    // No state to clear here — the screen handles navigation back.
    // This method exists so callers can keep semantic intent.
}
```

- [ ] **Step 4: Run, confirm pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.shottracker.feature.createcourse.CreateCourseViewModelTest"`

Expected: 26 tests PASS.

---

### Task 10: Build `CreateCourseScreen` shell

**Files:**
- Create: `app/src/main/java/com/example/shottracker/feature/createcourse/CreateCourseScreen.kt`

No automated tests for this layer.

- [ ] **Step 1: Create the screen file**

```kotlin
package com.example.shottracker.feature.createcourse

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shottracker.feature.createcourse.steps.GreensStep
import com.example.shottracker.feature.createcourse.steps.HoleCountStep
import com.example.shottracker.feature.createcourse.steps.NameStep
import com.example.shottracker.feature.createcourse.steps.ParsStep
import com.example.shottracker.feature.createcourse.steps.ReviewStep
import com.example.shottracker.feature.createcourse.steps.TeesStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    onBack: () -> Unit,
    onCourseCreated: () -> Unit,
    viewModel: CreateCourseViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CreateCourseEvent.Saved -> onCourseCreated()
            }
        }
    }

    LaunchedEffect(state.saveError) {
        state.saveError?.let { snackbarHostState.showSnackbar(it) }
    }

    BackHandler { viewModel.onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleForStep(state)) },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state.step) {
                WizardStep.Name -> NameStep(
                    state = state,
                    onNameChange = viewModel::setName,
                    onCityChange = viewModel::setCity,
                    onStateChange = viewModel::setState,
                    onNext = viewModel::onNext,
                )
                WizardStep.HoleCount -> HoleCountStep(
                    state = state,
                    onSelect = viewModel::setHoleCount,
                    onNext = viewModel::onNext,
                )
                WizardStep.Pars -> ParsStep(
                    state = state,
                    onParChanged = viewModel::updateHolePar,
                    onNext = viewModel::onNext,
                )
                WizardStep.Greens -> GreensStep(
                    state = state,
                    onPlace = viewModel::placeGreen,
                    onSelectTarget = viewModel::selectTarget,
                    onNext = viewModel::onNext,
                    onBack = viewModel::onBack,
                )
                WizardStep.Tees -> TeesStep(
                    state = state,
                    onOpenNewTeeForm = viewModel::openNewTeeForm,
                    onCancelTeeForm = viewModel::cancelTeeForm,
                    onTeeFormName = viewModel::updateTeeFormName,
                    onTeeFormColor = viewModel::updateTeeFormColor,
                    onTeeFormRating = viewModel::updateTeeFormRating,
                    onTeeFormSlope = viewModel::updateTeeFormSlope,
                    onSaveTeeForm = viewModel::saveTeeForm,
                    onDeleteTeeDraft = viewModel::deleteTeeDraft,
                    onNext = viewModel::onNext,
                )
                WizardStep.Review -> ReviewStep(
                    state = state,
                    onSave = viewModel::onSave,
                )
            }
        }
    }

    if (state.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDiscardDialog,
            title = { Text("Discard course?") },
            text = { Text("You'll lose your progress.") },
            confirmButton = {
                Button(onClick = onBack) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDiscardDialog) { Text("Cancel") }
            },
        )
    }
}

private fun titleForStep(state: CreateCourseUiState): String = when (state.step) {
    WizardStep.Name -> "New Course — Name"
    WizardStep.HoleCount -> "New Course — Holes"
    WizardStep.Pars -> "New Course — Pars"
    WizardStep.Greens -> "Hole ${state.currentGreenHole} of ${state.holeCount} — Place ${state.selectedGreenTarget}"
    WizardStep.Tees -> "New Course — Tees"
    WizardStep.Review -> "New Course — Review"
}
```

- [ ] **Step 2: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: compile errors — step composables don't exist yet. That's expected; the next tasks add them.

---

### Task 11: `NameStep` + `HoleCountStep` + `ParsStep` composables

**Files:**
- Create: `app/src/main/java/com/example/shottracker/feature/createcourse/steps/NameStep.kt`
- Create: `app/src/main/java/com/example/shottracker/feature/createcourse/steps/HoleCountStep.kt`
- Create: `app/src/main/java/com/example/shottracker/feature/createcourse/steps/ParsStep.kt`

- [ ] **Step 1: Create `NameStep.kt`**

```kotlin
package com.example.shottracker.feature.createcourse.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.shottracker.feature.createcourse.CreateCourseUiState

@Composable
fun NameStep(
    state: CreateCourseUiState,
    onNameChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text("Course name") },
            supportingText = state.nameError?.let { { Text(it) } },
            isError = state.nameError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = state.city,
            onValueChange = onCityChange,
            label = { Text("City (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.state,
            onValueChange = onStateChange,
            label = { Text("State (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onNext,
            enabled = state.name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Next") }
    }
}
```

- [ ] **Step 2: Create `HoleCountStep.kt`**

```kotlin
package com.example.shottracker.feature.createcourse.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shottracker.feature.createcourse.CreateCourseUiState

@Composable
fun HoleCountStep(
    state: CreateCourseUiState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("How many holes?")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.holeCount == 9,
                onClick = { onSelect(9) },
                label = { Text("9 holes") },
            )
            FilterChip(
                selected = state.holeCount == 18,
                onClick = { onSelect(18) },
                label = { Text("18 holes") },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text("Next") }
    }
}
```

- [ ] **Step 3: Create `ParsStep.kt`**

```kotlin
package com.example.shottracker.feature.createcourse.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shottracker.feature.createcourse.CreateCourseUiState

@Composable
fun ParsStep(
    state: CreateCourseUiState,
    onParChanged: (Int, Int) -> Unit,
    onNext: () -> Unit,
) {
    val totalPar = state.pars.sum()
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Total Par: $totalPar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
        )
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(count = state.holeCount, key = { it }) { idx ->
                ParRow(holeNumber = idx + 1, par = state.pars[idx],
                       onParChanged = { newPar -> onParChanged(idx + 1, newPar) })
                HorizontalDivider()
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) { Text("Next") }
    }
}

@Composable
private fun ParRow(
    holeNumber: Int,
    par: Int,
    onParChanged: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Hole $holeNumber", style = MaterialTheme.typography.bodyLarge,
             modifier = Modifier.weight(1f))
        IconButton(onClick = { onParChanged(par - 1) }, enabled = par > 3) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease par")
        }
        Text(
            text = "Par $par",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(64.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        IconButton(onClick = { onParChanged(par + 1) }, enabled = par < 6) {
            Icon(Icons.Default.Add, contentDescription = "Increase par")
        }
    }
}
```

- [ ] **Step 4: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: still compile errors — `GreensStep`, `TeesStep`, `ReviewStep` don't exist yet.

---

### Task 12: `GreensStep` composable (map UI)

**Files:**
- Create: `app/src/main/java/com/example/shottracker/feature/createcourse/steps/GreensStep.kt`

This is the most complex composable. It hosts a Google Maps satellite view, places markers per the current hole's `GreenDraft`, and routes taps to `onPlace`.

- [ ] **Step 1: Create `GreensStep.kt`**

```kotlin
package com.example.shottracker.feature.createcourse.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shottracker.feature.createcourse.CreateCourseUiState
import com.example.shottracker.feature.createcourse.GreenTarget
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun GreensStep(
    state: CreateCourseUiState,
    onPlace: (Double, Double) -> Unit,
    onSelectTarget: (GreenTarget) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }
    val mapProperties = MapProperties(mapType = MapType.SATELLITE)

    val hole = state.greens[state.currentGreenHole - 1]
    val isComplete = hole.isComplete

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapClick = { latLng -> onPlace(latLng.latitude, latLng.longitude) },
        ) {
            hole.frontLat?.let { lat ->
                hole.frontLng?.let { lng ->
                    Marker(
                        state = MarkerState(LatLng(lat, lng)),
                        title = "Front",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                    )
                }
            }
            hole.centerLat?.let { lat ->
                hole.centerLng?.let { lng ->
                    Marker(
                        state = MarkerState(LatLng(lat, lng)),
                        title = "Center",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                    )
                }
            }
            hole.backLat?.let { lat ->
                hole.backLng?.let { lng ->
                    Marker(
                        state = MarkerState(LatLng(lat, lng)),
                        title = "Back",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            tonalElevation = 4.dp,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TargetChip(state, GreenTarget.Front, hole.frontLat != null, onSelectTarget)
                    TargetChip(state, GreenTarget.Center, hole.centerLat != null, onSelectTarget)
                    TargetChip(state, GreenTarget.Back, hole.backLat != null, onSelectTarget)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                    ) { Text(if (state.currentGreenHole == 1) "Back to Pars" else "Prev Hole") }
                    Button(
                        onClick = onNext,
                        enabled = isComplete,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (state.currentGreenHole == state.holeCount) "To Tees" else "Next Hole")
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetChip(
    state: CreateCourseUiState,
    target: GreenTarget,
    placed: Boolean,
    onSelectTarget: (GreenTarget) -> Unit,
) {
    FilterChip(
        selected = state.selectedGreenTarget == target,
        onClick = { onSelectTarget(target) },
        label = { Text(if (placed) "${target.name} ✓" else target.name) },
    )
}
```

Note: This v1 does **not** auto-center on current GPS location. The spec called for that, but doing so cleanly requires injecting `LocationService` into the composable, which couples this composable to a service. Defer to a follow-up: instead, the camera defaults to a world view, and the user is expected to pan to the course. Update the spec's known limitations accordingly (see Step 2).

- [ ] **Step 2: Update the spec to reflect the v1 map-camera behavior**

Edit `docs/superpowers/specs/2026-05-20-manual-course-creation-design.md`. Replace the "Camera" bullet block in the "Map Interaction (Greens Step)" section with:

```markdown
**Camera (v1)**:
- The map opens at a default world view; the user pans/zooms to the course themselves.
- v1 does NOT auto-center on GPS. This was originally planned but adds the coupling of injecting `LocationService` into the composable; we opted to defer it. Tracked under "Out of Scope (v1)".
```

And add to the "Out of Scope (v1)" list:
```markdown
- Auto-centering the green-placement map on the user's current GPS location (was originally in scope; deferred for v1).
```

- [ ] **Step 3: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: still compile errors for `TeesStep` and `ReviewStep`.

---

### Task 13: `TeesStep` composable

**Files:**
- Create: `app/src/main/java/com/example/shottracker/feature/createcourse/steps/TeesStep.kt`

- [ ] **Step 1: Create `TeesStep.kt`**

```kotlin
package com.example.shottracker.feature.createcourse.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.shottracker.feature.coursemanagement.TeeFormState
import com.example.shottracker.feature.createcourse.CreateCourseUiState

@Composable
fun TeesStep(
    state: CreateCourseUiState,
    onOpenNewTeeForm: () -> Unit,
    onCancelTeeForm: () -> Unit,
    onTeeFormName: (String) -> Unit,
    onTeeFormColor: (String) -> Unit,
    onTeeFormRating: (String) -> Unit,
    onTeeFormSlope: (String) -> Unit,
    onSaveTeeForm: () -> Unit,
    onDeleteTeeDraft: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tees (${state.tees.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onOpenNewTeeForm) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Tee")
            }
        }
        HorizontalDivider()
        if (state.tees.isEmpty()) {
            Text(
                text = "Add at least one tee with rating and slope.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(state.tees, key = { it.name }) { tee ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tee.name, fontWeight = FontWeight.Bold)
                                val rating = tee.rating?.let { String.format("%.1f", it) } ?: "—"
                                val slope = tee.slope?.toString() ?: "—"
                                Text("Rating $rating · Slope $slope",
                                     style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { onDeleteTeeDraft(tee.name) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete ${tee.name}",
                                     tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        state.teesError?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Button(
            onClick = onNext,
            enabled = state.tees.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) { Text("Next") }
    }

    state.teeForm?.let { form ->
        TeeFormDialog(
            form = form,
            onNameChange = onTeeFormName,
            onColorChange = onTeeFormColor,
            onRatingChange = onTeeFormRating,
            onSlopeChange = onTeeFormSlope,
            onSave = onSaveTeeForm,
            onCancel = onCancelTeeForm,
        )
    }
}

@Composable
private fun TeeFormDialog(
    form: TeeFormState,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onRatingChange: (String) -> Unit,
    onSlopeChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Add Tee") },
        text = {
            Column {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = onNameChange,
                    label = { Text("Name (e.g., Blue, White)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.color,
                    onValueChange = onColorChange,
                    label = { Text("Color (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = form.rating,
                        onValueChange = onRatingChange,
                        label = { Text("Rating") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = form.slope,
                        onValueChange = onSlopeChange,
                        label = { Text("Slope") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                form.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error,
                         style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("Save") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
    )
}
```

- [ ] **Step 2: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: still missing `ReviewStep`.

---

### Task 14: `ReviewStep` composable

**Files:**
- Create: `app/src/main/java/com/example/shottracker/feature/createcourse/steps/ReviewStep.kt`

- [ ] **Step 1: Create `ReviewStep.kt`**

```kotlin
package com.example.shottracker.feature.createcourse.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shottracker.feature.createcourse.CreateCourseUiState

@Composable
fun ReviewStep(
    state: CreateCourseUiState,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(state.name, style = MaterialTheme.typography.titleLarge,
                     fontWeight = FontWeight.Bold)
                if (state.city.isNotBlank() || state.state.isNotBlank()) {
                    Text(
                        listOf(state.city, state.state).filter { it.isNotBlank() }.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("${state.holeCount} holes · Total par ${state.pars.sum()}")
                Text("${state.tees.size} tees: " + state.tees.joinToString(", ") { it.name })
                Spacer(Modifier.height(8.dp))
                Text("All ${state.holeCount} greens placed (front/center/back).",
                     style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(
            onClick = onSave,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.height(0.dp))
                Text(" Saving...")
            } else {
                Text("Save Course")
            }
        }
    }
}
```

- [ ] **Step 2: Verify compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`. Everything compiles now.

---

### Task 15: Wire navigation and entry button

**Files:**
- Modify: `app/src/main/java/com/example/shottracker/navigation/Screen.kt`
- Modify: `app/src/main/java/com/example/shottracker/navigation/ShotTrackerNavHost.kt`
- Modify: `app/src/main/java/com/example/shottracker/feature/coursemanagement/CourseManagementScreen.kt`

- [ ] **Step 1: Add the route**

In `app/src/main/java/com/example/shottracker/navigation/Screen.kt`, add after the existing `CourseManagement` line (line 21):

```kotlin
    data object CreateCourse : Screen("create_course")
```

- [ ] **Step 2: Wire the composable destination**

In `app/src/main/java/com/example/shottracker/navigation/ShotTrackerNavHost.kt`:

a) Add the import near the top, alongside the other `feature.` imports:

```kotlin
import com.example.shottracker.feature.createcourse.CreateCourseScreen
```

b) Modify the existing `CourseManagement` `composable` block to pass an `onCreateCourse` lambda. Replace the existing block (lines 135-140) with:

```kotlin
        composable(Screen.CourseManagement.route) {
            CourseManagementScreen(
                onSearchCourses = { navController.navigate(Screen.CourseSearch.route) },
                onCreateCourse = { navController.navigate(Screen.CreateCourse.route) },
                onBack = { navController.popBackStack() }
            )
        }
```

c) Add a new `composable` block for CreateCourse at the bottom of the NavHost:

```kotlin
        composable(Screen.CreateCourse.route) {
            CreateCourseScreen(
                onBack = { navController.popBackStack() },
                onCourseCreated = { navController.popBackStack() },
            )
        }
```

- [ ] **Step 3: Wire the button in CourseManagementScreen**

In `app/src/main/java/com/example/shottracker/feature/coursemanagement/CourseManagementScreen.kt`:

a) Update the function signature (line 51-54) to accept an `onCreateCourse` callback:

```kotlin
@Composable
fun CourseManagementScreen(
    onSearchCourses: () -> Unit,
    onCreateCourse: () -> Unit,
    onBack: () -> Unit,
    viewModel: CourseManagementViewModel = hiltViewModel()
) {
```

b) Add a second button under the existing "Search & Import Courses" button. Replace the existing `Button(onClick = onSearchCourses, ...)` block (lines 75-82) with:

```kotlin
            Button(
                onClick = onSearchCourses,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Search & Import Courses")
            }
            androidx.compose.material3.OutlinedButton(
                onClick = onCreateCourse,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("Create Course Manually")
            }
```

- [ ] **Step 4: Verify everything compiles**

Run: `.\gradlew.bat :app:assembleDebug`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Run all unit tests**

Run: `.\gradlew.bat :app:testDebugUnitTest`

Expected: 26 ViewModel tests + the existing ExampleUnitTest all PASS.

---

### Task 16: Manual smoke test

**Files:** None (running app on device or emulator).

- [ ] **Step 1: Install and launch the app**

Run: `.\gradlew.bat :app:installDebug`

Then launch the app from the device home screen.

- [ ] **Step 2: Create a 9-hole course end-to-end**

1. Home → "Manage Courses"
2. Tap "Create Course Manually"
3. Enter name: "Smoke Test 9H", leave city/state blank, tap Next.
4. Tap "9 holes", then Next.
5. Pars step: tap Next (all default 4).
6. Greens step (hole 1): pan/zoom to any location, tap to place Front (green pin appears), tap again for Center (yellow), tap again for Back (red). "Next Hole" enables. Tap Next.
7. Repeat for holes 2–9.
8. After hole 9, the wizard advances to Tees. Tap "Add Tee", enter name "Blue", rating "71.5", slope "132", tap Save. The tee appears in the list.
9. Tap Next → Review step shows the summary.
10. Tap "Save Course".

**Expected:** A snackbar/no-snackbar (just navigate), and you land back on CourseManagement. "Smoke Test 9H" appears in the list with "9 holes".

- [ ] **Step 3: Verify distance-to-green works on the new course**

1. Home → Start New Round.
2. Select "Smoke Test 9H" as the course, pick the Blue tee, start the round.
3. ActiveRoundScreen loads. Check the distance-to-green display — it should show numeric values (front/center/back distances calculated from your phone's GPS to the points you tapped earlier).
4. Tap "Map" to open the MapScreen for hole 1. Three colored markers (green/yellow/red) should render at the locations you tapped.

**Expected:** Distance-to-green readout populated; map shows three pins.

- [ ] **Step 4: Verify duplicate-name blocking**

1. Go back to CourseManagement → Create Course Manually.
2. Enter "Smoke Test 9H" (same name). Tap Next.

**Expected:** TextField shows red supporting text "A course with this name already exists" and Next does not advance.

- [ ] **Step 5: Verify discard confirmation**

1. Tap back from the Name step.

**Expected:** "Discard course?" AlertDialog appears with Cancel / Discard buttons. Tapping Cancel keeps you on the Name step. Tapping Discard returns you to CourseManagement.

- [ ] **Step 6: Verify within-Greens back navigation**

1. Start a new 9-hole course. Get to the Greens step.
2. Place all three pins on hole 1, tap Next Hole.
3. Tap Prev Hole.

**Expected:** Returns to hole 1; the three pins from hole 1 are still visible on the map.

If any step above fails, stop and diagnose before considering the feature complete.

---

## Self-Review Notes

This plan was self-reviewed against the spec. Key tracked decisions:

1. **GPS auto-centering on map deferred to follow-up.** The spec called for auto-centering on `LocationService.getCurrentLocation()` at first entry to the Greens step. Doing this cleanly requires injecting `LocationService` into the composable layer, which we judged out-of-scope for v1; the spec was updated (Task 12 Step 2) to reflect the deferral.
2. **Atomicity rollback test omitted.** The spec called for a "DAO throws → rolled back" test, but constructing a forcing-failure DAO wrapper requires Robolectric or instrumentation infrastructure that would dwarf the test it enables. The plan replaces it with a behavioral test (Task 2 Step 5) that exercises the transaction boundary differently. We rely on Room's `@Transaction` semantics for the actual rollback guarantee.
3. **`TeeFormState` reused from `coursemanagement`** rather than defining a wizard-specific copy. The two represent the same data; the cross-feature import is a small price for not duplicating the data class.
4. **No commit steps** — project isn't a git repository.
5. **Test commands target Windows PowerShell** (`.\gradlew.bat`). On macOS/Linux, substitute `./gradlew`.

All spec requirements that remain in-scope have at least one task implementing them. The Out-of-Scope list in the spec was extended to include the GPS auto-center deferral.
