package com.example.bloodconnect.ui.screen

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bloodconnect.data.model.Donor
import com.example.bloodconnect.ui.components.ArticleCard
import com.example.bloodconnect.ui.components.ErrorMessage
import com.example.bloodconnect.ui.navigation.Screen
import com.example.bloodconnect.ui.viewmodel.AuthViewModel
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import com.example.bloodconnect.ui.viewmodel.UiState
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun isInternetAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun HomeScreen(
    navController: NavController, 
    bloodViewModel: BloodViewModel,
    authViewModel: AuthViewModel
) {
    val bloodDataState by bloodViewModel.bloodData.collectAsState()
    val userData by authViewModel.userData.collectAsState()
    val donationsState by bloodViewModel.donations.collectAsState()
    val context = LocalContext.current
    var isOffline by remember { mutableStateOf(!isInternetAvailable(context)) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(userData) {
        userData?.id?.let { userId ->
            bloodViewModel.fetchDonations(userId)
        }
    }

    // Eligibility Logic
    val eligibilityInfo = remember(donationsState) {
        if (donationsState is UiState.Success) {
            val list = (donationsState as UiState.Success).data
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            var latestDate: Date? = null
            
            list.forEach { donation ->
                try {
                    val d = sdf.parse(donation.date)
                    if (latestDate == null || (d != null && d.after(latestDate))) {
                        latestDate = d
                    }
                } catch (e: Exception) {}
            }

            if (latestDate == null) {
                Pair(true, "")
            } else {
                val nextCal = Calendar.getInstance().apply {
                    time = latestDate
                    add(Calendar.DAY_OF_YEAR, 90)
                }
                val isEligible = Calendar.getInstance().after(nextCal)
                Pair(isEligible, sdf.format(nextCal.time).uppercase())
            }
        } else {
            Pair(true, "")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                HeaderSection(navController, userData?.name ?: "User", userData?.bloodType ?: "-")
            }

            item {
                SOSRequestSection(navController)
            }

            item {
                Text(
                    text = "Menu Utama",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                MenuSection(navController, eligibilityInfo.first, eligibilityInfo.second)
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
                        color = MaterialTheme.colorScheme.onBackground,
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
                                    navController.navigate(Screen.Chat.createRoute(donor.name))
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
                        color = MaterialTheme.colorScheme.onBackground,
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
                                    val encodedTitle = URLEncoder.encode(article.title, StandardCharsets.UTF_8.toString())
                                    val encodedUrl = URLEncoder.encode(article.imageUrl, StandardCharsets.UTF_8.toString())
                                    navController.navigate(Screen.EducationDetail.createRoute(encodedTitle, encodedUrl))
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

    if (isOffline) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Koneksi Terputus", fontWeight = FontWeight.Bold) },
            text = { Text("Tidak ada koneksi internet. Silakan hubungkan ke internet untuk memuat data.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (isInternetAvailable(context)) {
                            isOffline = false
                            bloodViewModel.fetchBloodData()
                            userData?.id?.let { userId ->
                                bloodViewModel.fetchDonations(userId)
                            }
                        } else {
                            Toast.makeText(context, "Masih offline. Periksa koneksi Anda.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Muat Ulang", color = Color.White)
                }
            },
            dismissButton = null,
            containerColor = Color.White
        )
    }
}

@Composable
fun HeaderSection(navController: NavController, name: String, bloodType: String) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Hello, $name", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(text = "Thank you for being a hero!", color = Color.White.copy(alpha = 0.8f))
            }
            IconButton(
                onClick = { navController.navigate(Screen.NotificationList.route) }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
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
fun MenuSection(navController: NavController, isEligible: Boolean, nextDate: String) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MenuItem("Mendonor Darah", Icons.Default.Bloodtype) { 
                if (isEligible) {
                    navController.navigate(Screen.DonorForm.route)
                } else {
                    Toast.makeText(context, "Belum memenuhi syarat jeda 90 hari. Anda bisa donor kembali pada $nextDate", Toast.LENGTH_LONG).show()
                }
            }
            MenuItem("Cari Pendonor Terdekat", Icons.Default.LocationOn) { navController.navigate(Screen.Map.route) }
            MenuItem("Riwayat Donor", Icons.Default.History) { navController.navigate(Screen.DonorHistory.route) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MenuItem("Chat", Icons.AutoMirrored.Filled.Chat) { navController.navigate(Screen.ChatList.route) }
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.size(60.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(icon, contentDescription = label, tint = Color.Red, modifier = Modifier.size(28.dp))
            }
        }
        Text(
            text = label, 
            color = MaterialTheme.colorScheme.onBackground,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    Text(text = donor.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = donor.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = donor.distance, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
