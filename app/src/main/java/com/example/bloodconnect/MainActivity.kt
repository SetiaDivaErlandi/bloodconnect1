package com.example.bloodconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.decode.SvgDecoder
import com.example.bloodconnect.ui.ViewModelFactory
import com.example.bloodconnect.ui.navigation.Screen
import com.example.bloodconnect.ui.screen.*
import com.example.bloodconnect.ui.theme.BloodconnectTheme
import com.example.bloodconnect.ui.viewmodel.AuthViewModel
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import com.example.bloodconnect.ui.viewmodel.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userPreferences = com.example.bloodconnect.data.local.UserPreferences(this)
        setContent {
            val isDarkMode by userPreferences.isDarkMode.collectAsState(initial = false)
            val imageLoader = ImageLoader.Builder(LocalContext.current)
                .components {
                    add(SvgDecoder.Factory())
                }
                .build()
            
            CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                BloodconnectTheme(darkTheme = isDarkMode) {
                    BloodConnectApp()
                }
            }
        }
    }
}

@Composable
fun BloodConnectApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModelFactory = remember { ViewModelFactory(context) }
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val bloodViewModel: BloodViewModel = viewModel(factory = viewModelFactory)
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUser by authViewModel.userData.collectAsState()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sosRequestsState by bloodViewModel.sosRequests.collectAsState()
    var lastNotifiedSosId by remember { mutableStateOf("") }

    LaunchedEffect(currentDestination) {
        if (isLoggedIn) {
            authViewModel.updateActivity()
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            while (true) {
                bloodViewModel.fetchSosRequests()
                delay(5000)
            }
        }
    }

    LaunchedEffect(sosRequestsState) {
        val state = sosRequestsState
        if (state is UiState.Success && isLoggedIn) {
            val list = state.data
            val latest = list.firstOrNull { it.requesterId != (currentUser?.id ?: "") }
            if (latest != null && latest.id != lastNotifiedSosId) {
                val timeDiff = System.currentTimeMillis() - latest.timestamp
                if (timeDiff < 60000L) {
                    lastNotifiedSosId = latest.id
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "SOS BARU: ${latest.requesterName} butuh darah ${latest.bloodType}!",
                            actionLabel = "Lihat",
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            navController.navigate(Screen.SOS.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }
        }
    }

    val bottomBarScreens = listOf(
        Screen.Home,
        Screen.Map,
        Screen.SOS,
        Screen.ChatList,
        Screen.Profile
    )

    val showBottomBar = currentDestination?.route in bottomBarScreens.map { it.route }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    bottomBarScreens.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                val icon = when (screen) {
                                    Screen.Home -> Icons.Default.Home
                                    Screen.Map -> Icons.Default.LocationOn
                                    Screen.SOS -> Icons.Default.Warning
                                    Screen.ChatList -> Icons.AutoMirrored.Filled.Chat
                                    Screen.Profile -> Icons.Default.Person
                                    else -> Icons.Default.Home
                                }
                                Icon(icon, contentDescription = null)
                            },
                            label = { Text(screen.route.split("_").first().replaceFirstChar { it.uppercase() }, fontSize = 10.sp) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Red,
                                selectedTextColor = Color.Red,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Red.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController, isLoggedIn)
            }
            composable(Screen.Login.route) {
                LoginScreen(navController, authViewModel)
            }
            composable(Screen.Register.route) {
                RegisterScreen(navController, authViewModel)
            }
            composable(Screen.Home.route) {
                HomeScreen(navController, bloodViewModel, authViewModel)
            }
            composable(Screen.SOS.route) {
                SOSScreen(navController, bloodViewModel, authViewModel)
            }
            composable(Screen.Map.route) {
                MapScreen(navController, bloodViewModel, authViewModel)
            }
            composable(Screen.ChatList.route) {
                ChatListScreen(navController, bloodViewModel, authViewModel)
            }
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name") ?: ""
                ChatScreen(navController, name, bloodViewModel, authViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController, authViewModel)
            }
            composable(Screen.DonorHistory.route) {
                DonorHistoryScreen(navController, bloodViewModel, authViewModel)
            }
            composable(Screen.Education.route) {
                EducationScreen(navController, bloodViewModel)
            }
            composable(
                route = Screen.EducationDetail.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType },
                    navArgument("imageUrl") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val title = URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", StandardCharsets.UTF_8.toString())
                val imageUrl = URLDecoder.decode(backStackEntry.arguments?.getString("imageUrl") ?: "", StandardCharsets.UTF_8.toString())
                EducationDetailScreen(navController, bloodViewModel, title, imageUrl)
            }
            composable(Screen.EducationSteps.route) {
                EducationStepsScreen(navController)
            }
            composable(Screen.EducationBookmark.route) {
                EducationBookmarkScreen(navController, bloodViewModel)
            }
            composable(Screen.SearchDonor.route) {
                SearchDonorScreen(navController, bloodViewModel)
            }
            composable(Screen.DonorForm.route) {
                DonorFormScreen(navController, bloodViewModel, authViewModel)
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(navController, authViewModel)
            }
            composable(Screen.NotificationList.route) {
                NotificationScreen(navController, bloodViewModel, authViewModel)
            }
        }
    }
}
