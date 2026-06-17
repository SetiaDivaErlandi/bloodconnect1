package com.example.bloodconnect.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import com.example.bloodconnect.ui.navigation.Screen
import com.example.bloodconnect.ui.viewmodel.AuthViewModel
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import com.example.bloodconnect.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorHistoryScreen(
    navController: NavController,
    bloodViewModel: BloodViewModel,
    authViewModel: AuthViewModel
) {
    val userData by authViewModel.userData.collectAsState()
    val donationsState by bloodViewModel.donations.collectAsState()
    val context = LocalContext.current

    var showCancelDialog by remember { mutableStateOf(false) }
    var donationToCancel by remember { mutableStateOf<DonationResponse?>(null) }

    LaunchedEffect(userData) {
        userData?.id?.let { userId ->
            bloodViewModel.fetchDonations(userId)
        }
    }

    if (showCancelDialog && donationToCancel != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Batalkan Jadwal", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin membatalkan riwayat/jadwal donor ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userData?.id?.let { userId ->
                            bloodViewModel.deleteDonation(userId, donationToCancel!!.id) {
                                Toast.makeText(context, "Berhasil dibatalkan", Toast.LENGTH_SHORT).show()
                                showCancelDialog = false
                            }
                        }
                    }
                ) {
                    Text("YA, BATALKAN", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("TIDAK", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
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
                .background(MaterialTheme.colorScheme.background)
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
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time

                    var latestDate: Date? = null
                    var hasUpcoming = false

                    list.forEach { donation ->
                        try {
                            val date = sdf.parse(donation.date)
                            if (date != null) {
                                if (latestDate == null || date.after(latestDate)) {
                                    latestDate = date
                                }
                                if (date.after(today)) {
                                    hasUpcoming = true
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    val nextDonationCalendar = Calendar.getInstance()
                    var isEligible = true
                    var nextDonationDateStr = ""

                    latestDate?.let { lastDate ->
                        nextDonationCalendar.time = lastDate
                        nextDonationCalendar.add(Calendar.DAY_OF_YEAR, 90)
                        
                        isEligible = today.after(nextDonationCalendar.time)
                        nextDonationDateStr = sdf.format(nextDonationCalendar.time).uppercase()
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list.sortedByDescending { 
                            try { sdf.parse(it.date) } catch (e: Exception) { Date(0) } 
                        }) { item ->
                            HistoryCard(
                                item = item,
                                onCancel = {
                                    donationToCancel = item
                                    showCancelDialog = true
                                }
                            )
                        }

                        item {
                            NextDonorCard(
                                eligible = isEligible,
                                hasUpcoming = hasUpcoming,
                                nextDate = nextDonationDateStr,
                                onFillForm = {
                                    if (isEligible) {
                                        navController.navigate(Screen.DonorForm.route)
                                    } else if (hasUpcoming) {
                                        Toast.makeText(context, "Selesaikan jadwal donor Anda yang masih tertunda", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Anda belum memenuhi syarat jeda waktu 90 hari", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        item {
                            Text(
                                text = "Terima kasih! Goresan dari darah Anda sangat berarti bagi mereka.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
fun HistoryCard(item: DonationResponse, onCancel: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val donationDate = try { sdf.parse(item.date) } catch (e: Exception) { null }
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    val isFuture = donationDate?.after(today) ?: false
    val displayStatus = if (isFuture) "Menunggu" else "Selesai"
    val displayIsCompleted = !isFuture

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dateParts = item.date.split(" ")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (dateParts.isNotEmpty()) dateParts[0] else "", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 20.sp, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (dateParts.size > 1) dateParts.drop(1).joinToString(" ") else "", 
                    fontSize = 10.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.hospital, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Donor Darah", 
                    fontSize = 12.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displayStatus, 
                    fontSize = 12.sp, 
                    color = if (isFuture) Color.Blue else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (displayIsCompleted) {
                    Surface(
                        color = Color(0xFFE8F5E9).copy(alpha = 0.2f),
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
                } else {
                    Surface(
                        color = Color(0xFFE3F2FD).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Pending",
                            color = Color.Blue,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NextDonorCard(eligible: Boolean, hasUpcoming: Boolean, nextDate: String, onFillForm: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (MaterialTheme.colorScheme.background == Color(0xFF212121)) 
                Color(0xFF311B1B) else Color(0xFFFFEBEE)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (eligible) Color(0xFF2E7D32) else if (hasUpcoming) Color.Blue else Color.Red, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (eligible) Icons.Default.Check else Icons.Default.Info, 
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Donor Berikutnya", 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (eligible) {
                        Text(
                            text = "Status: Anda Sudah Bisa Donor Kembali!", 
                            color = Color(0xFF2E7D32), 
                            fontWeight = FontWeight.SemiBold, 
                            fontSize = 13.sp
                        )
                    } else if (hasUpcoming) {
                        Text(
                            text = "Status: Anda memiliki jadwal pending", 
                            color = Color.Blue, 
                            fontWeight = FontWeight.SemiBold, 
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Selesaikan jadwal tersebut terlebih dahulu.", 
                            fontSize = 11.sp, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Dapat donor kembali pada:", 
                            fontSize = 12.sp, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = nextDate, 
                            color = Color.Red, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 18.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onFillForm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (eligible) Color.Red else Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Isi Formulir Donor", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
