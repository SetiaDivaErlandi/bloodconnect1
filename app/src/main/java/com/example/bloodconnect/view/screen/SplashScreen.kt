package com.example.bloodconnect.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.painterResource
import com.example.bloodconnect.R
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bloodconnect.view.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, isLoggedIn: Boolean) {
    val currentIsLoggedIn by rememberUpdatedState(isLoggedIn)
    LaunchedEffect(key1 = true) {
        delay(1500)
        if (currentIsLoggedIn) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_logo_droplet),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = "BloodConnect",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Connecting Blood Donors Nearby",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
        
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .size(30.dp),
            strokeWidth = 3.dp
        )
    }
}
