package com.example.bloodconnect.ui.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bloodconnect.data.model.DonationResponse
import com.example.bloodconnect.ui.viewmodel.AuthViewModel
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorFormScreen(
    navController: NavController,
    bloodViewModel: BloodViewModel,
    authViewModel: AuthViewModel
) {
    val userData by authViewModel.userData.collectAsState()
    val context = LocalContext.current
    
    var hospital by remember { mutableStateOf("") }
    var donorDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                donorDate = sdf.format(selectedDate.time).uppercase()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Set minimal tanggal adalah hari ini
            datePicker.minDate = System.currentTimeMillis() - 1000
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Input Tidak Lengkap", fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage.ifBlank { "Silakan lengkapi semua kolom yang diperlukan." }) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK", color = Color.Red)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            topBarSection(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Catat Donor Darah Anda", 
                fontSize = 22.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Data profil Anda akan terisi secara otomatis.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Auto-filled read-only fields
            ReadOnlyTextField(label = "Nama Lengkap", value = userData?.name ?: "")
            Spacer(modifier = Modifier.height(16.dp))
            ReadOnlyTextField(label = "Golongan Darah", value = userData?.bloodType ?: "")
            Spacer(modifier = Modifier.height(16.dp))
            ReadOnlyTextField(label = "No. HP", value = userData?.phone ?: "")
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // Manual input fields
            OutlinedTextField(
                value = hospital,
                onValueChange = { hospital = it },
                label = { Text("Nama Rumah Sakit / Lokasi") },
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
                value = donorDate,
                onValueChange = { },
                label = { Text("Tanggal Donor") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                enabled = false,
                readOnly = true,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal", tint = Color.Red)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan (Opsional)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
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
                    if (hospital.isBlank() || donorDate.isBlank()) {
                        errorMessage = "Nama Rumah Sakit dan Tanggal Donor harus diisi."
                        showErrorDialog = true
                    } else if (userData != null) {
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val selectedDate = try { sdf.parse(donorDate) } catch (e: Exception) { null }
                        
                        val todayCalendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val today = todayCalendar.time
                        
                        // Cek apakah tanggal di masa depan
                        val isFuture = selectedDate?.after(today) ?: false
                        val defaultStatus = if (isFuture) "Menunggu" else "Selesai"

                        val donation = DonationResponse(
                            id = "don_${System.currentTimeMillis()}",
                            date = donorDate,
                            hospital = hospital.trim(),
                            status = if (notes.isBlank()) defaultStatus else notes.trim(),
                            isCompleted = !isFuture
                        )
                        bloodViewModel.submitDonation(
                            userId = userData!!.id,
                            donation = donation,
                            onSuccess = {
                                navController.popBackStack()
                            },
                            onError = {
                                errorMessage = it
                                showErrorDialog = true
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SIMPAN RIWAYAT DONOR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun topBarSection(navController: NavController) {
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

@Composable
fun ReadOnlyTextField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    )
}
