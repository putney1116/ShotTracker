package com.example.shottracker.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.shottracker.feature.coursemanagement.CourseManagementScreen
import com.example.shottracker.feature.coursesearch.CourseSearchScreen
import com.example.shottracker.feature.history.HistoryScreen
import com.example.shottracker.feature.history.RoundDetailScreen
import com.example.shottracker.feature.home.HomeScreen
import com.example.shottracker.feature.map.MapScreen
import com.example.shottracker.feature.round.ActiveRoundScreen
import com.example.shottracker.feature.round.NewRoundSetupScreen
import com.example.shottracker.feature.scorecard.ScorecardScreen
import com.example.shottracker.feature.createcourse.CreateCourseScreen
import com.example.shottracker.feature.statistics.StatisticsScreen

@Composable
fun ShotTrackerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartNewRound = { navController.navigate(Screen.NewRoundSetup.route) },
                onManageCourses = { navController.navigate(Screen.CourseManagement.route) },
                onViewHistory = { navController.navigate(Screen.History.route) },
                onViewStatistics = { navController.navigate(Screen.Statistics.route) },
                onResumeRound = { roundId ->
                    navController.navigate(Screen.ActiveRound.createRoute(roundId))
                }
            )
        }

        composable(Screen.NewRoundSetup.route) {
            NewRoundSetupScreen(
                onRoundStarted = { roundId ->
                    navController.navigate(Screen.ActiveRound.createRoute(roundId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ActiveRound.route,
            arguments = listOf(navArgument("roundId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roundId = backStackEntry.arguments?.getLong("roundId") ?: return@composable
            ActiveRoundScreen(
                roundId = roundId,
                onOpenScorecard = {
                    navController.navigate(Screen.Scorecard.createRoute(roundId))
                },
                onOpenMap = { holeNumber ->
                    navController.navigate(Screen.MapView.createRoute(roundId, holeNumber))
                },
                onRoundComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Scorecard.route,
            arguments = listOf(navArgument("roundId") { type = NavType.LongType }),
            // Slide down from the top when opened from the in-round screen.
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(500)
                )
            },
            // Slide back up off the top when dismissed.
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500)
                )
            }
        ) { backStackEntry ->
            val roundId = backStackEntry.arguments?.getLong("roundId") ?: return@composable
            ScorecardScreen(
                roundId = roundId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MapView.route,
            arguments = listOf(
                navArgument("roundId") { type = NavType.LongType },
                navArgument("holeNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val roundId = backStackEntry.arguments?.getLong("roundId") ?: return@composable
            val holeNumber = backStackEntry.arguments?.getInt("holeNumber") ?: return@composable
            MapScreen(
                roundId = roundId,
                holeNumber = holeNumber,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onRoundClick = { roundId ->
                    navController.navigate(Screen.RoundDetail.createRoute(roundId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RoundDetail.route,
            arguments = listOf(navArgument("roundId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roundId = backStackEntry.arguments?.getLong("roundId") ?: return@composable
            RoundDetailScreen(
                roundId = roundId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CourseSearch.route) {
            CourseSearchScreen(
                onBack = { navController.popBackStack() },
                onCourseImported = { navController.popBackStack() }
            )
        }

        composable(Screen.CourseManagement.route) {
            CourseManagementScreen(
                onSearchCourses = { navController.navigate(Screen.CourseSearch.route) },
                onCreateCourse = { navController.navigate(Screen.CreateCourse.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateCourse.route) {
            CreateCourseScreen(
                onBack = { navController.popBackStack() },
                onCourseCreated = { navController.popBackStack() },
            )
        }
    }
}
