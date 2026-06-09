package com.example.bloodconnect.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bloodconnect.data.model.DonationResponse
import com.example.bloodconnect.ui.navigation.Screen
import com.example.bloodconnect.ui.viewmodel.AuthViewModel
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import com.example.bloodconnect.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorHistoryScreen(
    navController: NavController,
    bloodViewModel: BloodViewModel,
    authViewModel: AuthViewModel
) {
    val userData by authViewModel.userData.collectAsState()
    val donationsState by bloodViewModel.donations.collectAsState()

    LaunchedEffect(userData) {
        userData?.id?.let { userId ->
            bloodViewModel.fetchDonations(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Donor", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Red)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            when (val state = donationsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Red)
                    }
                }
                is UiState.Success -> {
                    val list = state.data
                    val hasJuneDonation = list.any { it.date.contains("JUN 2026", ignoreCase = true) }

                    if (list.isEmpty() && userData != null) {
                        LaunchedEffect(Unit) {
                            val defaultHistory = listOf(
                                DonationResponse("d1", "12 JAN 2026", "RSUD Metro", "Selesai", true),
                                DonationResponse("d2", "09 MAR 2026", "RS Hermina Lampung", "Selesai", true)
                            )
                            defaultHistory.forEach { item ->
                                bloodViewModel.submitDonation(userData!!.id, item, {}, {})
                            }
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list) { item ->
                            HistoryCard(item)
                        }

                        item {
                            NextDonorCard(
                                eligible = !hasJuneDonation,
                                onFillForm = {
                                    navController.navigate(Screen.DonorForm.route)
                                }
                            )
                        }

                        item {
                            Text(
                                text = "Terima kasih! Goresan dari darah Anda sangat berarti bagi mereka.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(item: DonationResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = item.date.split(" ")[0], fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                Text(text = item.date.split(" ").drop(1).joinToString(" "), fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.hospital, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = "Donor Darah", fontSize = 12.sp, color = Color.Gray)
                Text(text = item.status, fontSize = 12.sp, color = Color.Gray)
            }
            if (item.isCompleted) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Completed",
                        color = Color(0xFF2E7D32),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NextDonorCard(eligible: Boolean, onFillForm: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Red, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Donor Berikutnya", fontWeight = FontWeight.Bold, color = Color.Black)
                    if (eligible) {
                        Text(text = "Status: Anda Sudah Bisa Donor Kembali!", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    } else {
                        Text(text = "Dapat donor kembali pada:", fontSize = 12.sp, color = Color.Black)
                        Text(text = "09 September 2026", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
            if (eligible) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onFillForm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Isi Formulir Donor", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
