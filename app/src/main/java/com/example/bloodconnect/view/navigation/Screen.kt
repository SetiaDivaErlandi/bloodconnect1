package com.example.bloodconnect.view.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Map : Screen("map")
    object SOS : Screen("sos")
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{name}") {
        fun createRoute(name: String) = "chat/$name"
    }
    object Profile : Screen("profile")
    object DonorHistory : Screen("donor_history")
    object Education : Screen("education")
    object EducationDetail : Screen("education_detail/{title}/{imageUrl}") {
        fun createRoute(title: String, imageUrl: String) = "education_detail/$title/$imageUrl"
    }
    object EducationSteps : Screen("education_steps")
    object EducationBookmark : Screen("education_bookmark")
    object SearchDonor : Screen("search_donor")
    object DonorForm : Screen("donor_form")
    object ForgotPassword : Screen("forgot_password")
    object NotificationList : Screen("notification_list")
}
