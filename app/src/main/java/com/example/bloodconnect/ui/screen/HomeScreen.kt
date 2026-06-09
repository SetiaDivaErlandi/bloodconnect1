package com.example.bloodconnect.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bloodconnect.data.model.Article
import com.example.bloodconnect.data.model.Donor
import com.example.bloodconnect.ui.components.ArticleCard
import com.example.bloodconnect.ui.components.ErrorMessage
import com.example.bloodconnect.ui.components.SectionTitle
import com.example.bloodconnect.ui.navigation.Screen
import com.example.bloodconnect.ui.viewmodel.AuthViewModel
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import com.example.bloodconnect.ui.viewmodel.UiState
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(
    navController: NavController, 
    bloodViewModel: BloodViewModel,
    authViewModel: AuthViewModel
) {
    val bloodDataState by bloodViewModel.bloodData.collectAsState()
    val userData by authViewModel.userData.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        item {
            HeaderSection(userData?.name ?: "User", userData?.bloodType ?: "-")
        }

        item {
            SOSRequestSection(navController)
        }

        item {
            Text(
                text = "Menu Utama",
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            MenuSection(navController)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Donor Terdekat",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Lihat Semua",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { navController.navigate(Screen.Map.route) }
                )
            }
        }

        item {
            when (val state = bloodDataState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Red)
                    }
                }
                is UiState.Success -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data.donors) { donor ->
                            DonorCard(donor) {
                                navController.navigate(Screen.Chat.route)
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorMessage(state.message)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edukasi Terbaru",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Text(
                    text = "Lihat Semua",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable { navController.navigate(Screen.Education.route) }
                )
            }
        }

        item {
            when (val state = bloodDataState) {
                is UiState.Success -> {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.data.articles.take(3).forEach { article ->
                            ArticleCard(article) {
                                val encodedUrl = URLEncoder.encode(article.imageUrl, StandardCharsets.UTF_8.toString())
                                navController.navigate(Screen.EducationDetail.createRoute(article.title, encodedUrl))
                            }
                        }
                    }
                }
                else -> {}
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HeaderSection(name: String, bloodType: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Red, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .padding(24.dp)
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Hello, $name", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(text = "Thank you for being a hero!", color = Color.White.copy(alpha = 0.8f))
            }
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = bloodType, color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Blood type", color = Color.Red, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SOSRequestSection(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Red),
        shape = RoundedCornerShape(16.dp),
        onClick = { navController.navigate(Screen.SOS.route) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "SOS REQUEST", color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = "Butuh darah sekarang?", color = Color.White.copy(alpha = 0.8f))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun MenuSection(navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MenuItem("Cari Pendonor", Icons.Default.Search) { navController.navigate(Screen.Map.route) }
            MenuItem("Pendonor Terdekat", Icons.Default.LocationOn) { navController.navigate(Screen.Map.route) }
            MenuItem("Riwayat Donor", Icons.Default.History) { navController.navigate(Screen.DonorHistory.route) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MenuItem("Chat", Icons.AutoMirrored.Filled.Chat) { navController.navigate(Screen.Chat.route) }
            MenuItem("Edukasi", Icons.AutoMirrored.Filled.MenuBook) { navController.navigate(Screen.Education.route) }
            MenuItem("Profil Saya", Icons.Default.Person) { navController.navigate(Screen.Profile.route) }
        }
    }
}

@Composable
fun MenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp).clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.size(60.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(icon, contentDescription = label, tint = Color.Red, modifier = Modifier.size(28.dp))
            }
        }
        Text(
            text = label, 
            color = Color.Black,
            fontSize = 11.sp, 
            modifier = Modifier.padding(top = 8.dp), 
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DonorCard(donor: Donor, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(220.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = donor.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = donor.name, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Text(text = donor.location, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.Red.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = donor.bloodType, 
                        fontSize = 12.sp, 
                        color = Color.Red, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Text(text = donor.distance, fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}
