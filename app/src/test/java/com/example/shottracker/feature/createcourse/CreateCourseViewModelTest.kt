package com.example.shottracker.feature.createcourse

import com.example.shottracker.domain.model.Course
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        assertEquals(GreenTarget.Front, vm.uiState.value.selectedGreenTarget)
    }

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
        val job = launch {
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
        val job = launch {
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
}
