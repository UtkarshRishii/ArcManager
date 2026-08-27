package com.arcmanager.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arcmanager.presentation.screens.auth.ForgotPasswordScreen
import com.arcmanager.presentation.screens.auth.LoginScreen
import com.arcmanager.presentation.screens.auth.RegisterScreen
import com.arcmanager.presentation.screens.clients.AddClientScreen
import com.arcmanager.presentation.screens.clients.ClientDetailScreen
import com.arcmanager.presentation.screens.clients.ClientsScreen
import com.arcmanager.presentation.screens.dashboard.DashboardScreen
import com.arcmanager.presentation.screens.main.MainScreen
import com.arcmanager.presentation.screens.payments.AddPaymentScreen
import com.arcmanager.presentation.screens.payments.PaymentDetailScreen
import com.arcmanager.presentation.screens.payments.PaymentScheduleBuilderScreen
import com.arcmanager.presentation.screens.payments.PaymentsScreen
import com.arcmanager.presentation.screens.projects.CreateProjectScreen
import com.arcmanager.presentation.screens.projects.ProjectDetailScreen
import com.arcmanager.presentation.screens.splash.SplashScreen

// ──────────────────────────────────────────────
// Liquid Spatial Spring Transitions
// ──────────────────────────────────────────────
private val springSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow
)

@Composable
fun ArcManagerNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    initialOffset = { (it * 0.25f).toInt() }
                ) +
                scaleIn(initialScale = 0.94f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(250, easing = FastOutLinearInEasing)) +
                scaleOut(targetScale = 0.96f, animationSpec = tween(300, easing = FastOutLinearInEasing))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    initialOffset = { (-it * 0.25f).toInt() }
                ) +
                scaleIn(initialScale = 0.96f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(250, easing = FastOutLinearInEasing)) +
                scaleOut(targetScale = 0.92f, animationSpec = tween(300, easing = FastOutLinearInEasing))
        }
    ) {
        // ── Auth ──
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Main App (Bottom Dock) ──
        composable(Screen.Dashboard.route) {
            MainScreen(navController = navController, currentRoute = Screen.Dashboard.route)
        }

        composable(Screen.Clients.route) {
            MainScreen(navController = navController, currentRoute = Screen.Clients.route)
        }

        composable(Screen.Payments.route) {
            MainScreen(navController = navController, currentRoute = Screen.Payments.route)
        }

        composable(Screen.More.route) {
            MainScreen(navController = navController, currentRoute = Screen.More.route)
        }

        // ── Client Details & Add ──
        composable(route = Screen.AddClient.route) {
            AddClientScreen(
                onNavigateBack = { navController.popBackStack() },
                onClientAdded = { clientId -> navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ClientDetail.route,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable
            ClientDetailScreen(
                clientId = clientId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProject = { projectId ->
                    navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                },
                onNavigateToCreateProject = {
                    navController.navigate(Screen.CreateProject.createRoute(clientId))
                },
                onNavigateToAddPayment = { cId, pId ->
                    navController.navigate(Screen.AddPayment.createRoute(cId, pId))
                }
            )
        }

        // ── Projects ──
        composable(
            route = Screen.CreateProject.route,
            arguments = listOf(
                navArgument("clientId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId")
            CreateProjectScreen(
                preselectedClientId = clientId,
                onNavigateBack = { navController.popBackStack() },
                onProjectCreated = { projectId ->
                    navController.navigate(Screen.PaymentScheduleBuilder.createRoute(projectId)) {
                        popUpTo(Screen.CreateProject.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ProjectDetail.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            ProjectDetailScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScheduleBuilder = {
                    navController.navigate(Screen.PaymentScheduleBuilder.createRoute(projectId))
                },
                onNavigateToAddPayment = { clientId ->
                    navController.navigate(Screen.AddPayment.createRoute(clientId, projectId))
                }
            )
        }

        // ── Payment Schedule Builder ──
        composable(
            route = Screen.PaymentScheduleBuilder.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            PaymentScheduleBuilderScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() },
                onScheduleSaved = { navController.popBackStack() }
            )
        }

        // ── Payment ──
        composable(
            route = Screen.AddPayment.route,
            arguments = listOf(
                navArgument("clientId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("projectId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId")
            val projectId = backStackEntry.arguments?.getString("projectId")
            AddPaymentScreen(
                preselectedClientId = clientId,
                preselectedProjectId = projectId,
                onNavigateBack = { navController.popBackStack() },
                onPaymentRecorded = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PaymentDetail.route,
            arguments = listOf(navArgument("paymentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val paymentId = backStackEntry.arguments?.getString("paymentId") ?: return@composable
            PaymentDetailScreen(
                paymentId = paymentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
