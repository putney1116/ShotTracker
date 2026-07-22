package com.example.shottracker.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object NewRoundSetup : Screen("new_round_setup")
    data object ActiveRound : Screen("active_round/{roundId}") {
        fun createRoute(roundId: Long) = "active_round/$roundId"
    }
    data object Scorecard : Screen("scorecard/{roundId}") {
        fun createRoute(roundId: Long) = "scorecard/$roundId"
    }
    data object MapView : Screen("map/{roundId}/{holeNumber}") {
        fun createRoute(roundId: Long, holeNumber: Int) = "map/$roundId/$holeNumber"
    }
    data object History : Screen("history")
    data object RoundDetail : Screen("round_detail/{roundId}") {
        fun createRoute(roundId: Long) = "round_detail/$roundId"
    }
    data object Statistics : Screen("statistics")
    data object CourseSearch : Screen("course_search")
    data object CourseManagement : Screen("course_management")
    data object CreateCourse : Screen("create_course")
}
