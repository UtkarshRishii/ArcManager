package com.arcmanager.presentation.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")

    // Main (bottom nav)
    data object Dashboard : Screen("dashboard")
    data object Clients : Screen("clients")
    data object Payments : Screen("payments")
    data object Recurring : Screen("recurring")
    data object More : Screen("more")

    // Client
    data object AddClient : Screen("add_client")
    data object ClientDetail : Screen("client_detail/{clientId}") {
        fun createRoute(clientId: String) = "client_detail/$clientId"
    }

    // Project
    data object Projects : Screen("projects/{clientId}") {
        fun createRoute(clientId: String) = "projects/$clientId"
    }
    data object CreateProject : Screen("create_project?clientId={clientId}") {
        fun createRoute(clientId: String? = null) =
            if (clientId != null) "create_project?clientId=$clientId" else "create_project"
    }
    data object ProjectDetail : Screen("project_detail/{projectId}") {
        fun createRoute(projectId: String) = "project_detail/$projectId"
    }

    // Payment
    data object AddPayment : Screen("add_payment?clientId={clientId}&projectId={projectId}") {
        fun createRoute(clientId: String? = null, projectId: String? = null): String {
            val params = mutableListOf<String>()
            if (clientId != null) params.add("clientId=$clientId")
            if (projectId != null) params.add("projectId=$projectId")
            return if (params.isEmpty()) "add_payment" else "add_payment?${params.joinToString("&")}"
        }
    }
    data object PaymentDetail : Screen("payment_detail/{paymentId}") {
        fun createRoute(paymentId: String) = "payment_detail/$paymentId"
    }
    data object PaymentScheduleBuilder : Screen("schedule_builder/{projectId}") {
        fun createRoute(projectId: String) = "schedule_builder/$projectId"
    }

    // Bank
    data object BankAccounts : Screen("bank_accounts")
    data object AddBankAccount : Screen("add_bank_account")
    data object BankAccountDetail : Screen("bank_account_detail/{accountId}") {
        fun createRoute(accountId: String) = "bank_account_detail/$accountId"
    }

    // More
    data object Calendar : Screen("calendar")
    data object Analytics : Screen("analytics")
    data object Notifications : Screen("notifications")
    data object Settings : Screen("settings")
    data object Profile : Screen("profile")
}
