package com.example.bloodconnect.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.bloodconnect.ui.viewmodel.AuthViewModel
import com.example.bloodconnect.ui.viewmodel.BloodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorFormScreen(
    navController: NavController,
    bloodViewModel: BloodViewModel,
    authViewModel: AuthViewModel
) {
    val userData by authViewModel.userData.collectAsState()
    var hospital by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Input Tidak Lengkap", fontWeight = FontWeight.Bold) },
            text = { Text("Silakan masukkan Lokasi / Rumah Sakit tempat Anda mendonor.") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK", color = Color.Red)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Formulir Donor Darah", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Catat Donor Darah Anda", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                text = "Masukkan data tempat donor Anda untuk mencatat riwayat donor baru Anda.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = hospital,
                onValueChange = { hospital = it },
                label = { Text("Lokasi / Rumah Sakit Tempat Donor") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Red,
                    focusedLabelColor = Color.Red,
                    cursorColor = Color.Red
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan Tambahan (Opsional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Red,
                    focusedLabelColor = Color.Red,
                    cursorColor = Color.Red
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (hospital.isNotBlank() && userData != null) {
                        val donation = DonationResponse(
                            id = "don_${System.currentTimeMillis()}",
                            date = "09 JUN 2026",
                            hospital = hospital.trim(),
                            status = if (notes.isBlank()) "Selesai" else notes.trim(),
                            isCompleted = true
                        )
                        bloodViewModel.submitDonation(
                            userId = userData!!.id,
                            donation = donation,
                            onSuccess = {
                                navController.popBackStack()
                            },
                            onError = {
                                showErrorDialog = true
                            }
                        )
                    } else {
                        showErrorDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SUBMIT RIWAYAT DONOR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
