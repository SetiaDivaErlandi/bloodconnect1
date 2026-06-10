package com.example.bloodconnect.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bloodconnect.data.model.SosRequestResponse
import com.example.bloodconnect.ui.navigation.Screen
import com.example.bloodconnect.ui.viewmodel.AuthViewModel
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import com.example.bloodconnect.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(
    navController: NavController,
    bloodViewModel: BloodViewModel,
    authViewModel: AuthViewModel
) {
    val userData by authViewModel.userData.collectAsState()
    val sosRequestsState by bloodViewModel.sosRequests.collectAsState()
    val sosHistoryState by bloodViewModel.sosHistory.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    var bloodType by remember { mutableStateOf("O+") }
    var location by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(2) }
    var notes by remember { mutableStateOf("") }

    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    var expanded by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(userData?.id, selectedTab) {
        if (selectedTab == 1) {
            bloodViewModel.fetchSosRequests(userData?.id)
        } else if (selectedTab == 2) {
            userData?.id?.let { bloodViewModel.fetchSosHistory(it) }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("SOS Alert Dikirim!", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("Permintaan bantuan darah darurat Anda telah berhasil disebarkan ke pendonor terdekat.") },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = false
                    selectedTab = 1
                }) {
                    Text("LIHAT SOS SAYA", color = Color.Red)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SOS Emergency", color = Color.White, fontWeight = FontWeight.Bold) },
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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color.Red,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color.Red
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Minta", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("SOS Saya", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Riwayat", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            when (selectedTab) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Butuh Darah Darurat?", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(32.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = bloodType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Golongan Darah") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, focusedLabelColor = Color.Red)
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                bloodTypes.forEach { type ->
                                    DropdownMenuItem(text = { Text(type) }, onClick = { bloodType = type; expanded = false })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Lokasi / Rumah Sakit") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, focusedLabelColor = Color.Red)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Jumlah Kantong")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (quantity > 1) quantity-- }) { Icon(Icons.Default.Remove, contentDescription = null, tint = Color.Red) }
                                Text(text = quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                IconButton(onClick = { quantity++ }) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.Red) }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Catatan (Opsional)") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, focusedLabelColor = Color.Red)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (location.isNotBlank()) {
                                    val sosReq = SosRequestResponse(
                                        id = "sos_${System.currentTimeMillis()}",
                                        requesterName = userData?.name ?: "User",
                                        bloodType = bloodType,
                                        location = location,
                                        quantity = quantity,
                                        notes = notes,
                                        timestamp = System.currentTimeMillis(),
                                        requesterPhone = userData?.phone ?: "",
                                        requesterId = userData?.id ?: ""
                                    )
                                    bloodViewModel.sendSosRequest(sosReq, { showSuccessDialog = true }, { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() })
                                } else {
                                    Toast.makeText(context, "Mohon isi lokasi", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("KIRIM SOS ALERT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                1 -> {
                    when (val state = sosRequestsState) {
                        is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color.Red) }
                        is UiState.Success -> {
                            if (state.data.isEmpty()) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Tidak ada permintaan SOS aktif.") }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.data) { request ->
                                        SosRequestCard(
                                            request = request,
                                            currentUserId = userData?.id ?: "",
                                            onCall = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${request.requesterPhone}"))
                                                context.startActivity(intent)
                                            },
                                            onChat = { navController.navigate(Screen.Chat.createRoute(request.requesterName)) },
                                            onComplete = {
                                                bloodViewModel.completeSosRequest(request) {
                                                    Toast.makeText(context, "SOS Berhasil Diselesaikan", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.message, color = Color.Red) }
                    }
                }
                2 -> {
                    when (val state = sosHistoryState) {
                        is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color.Red) }
                        is UiState.Success -> {
                            if (state.data.isEmpty()) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Belum ada riwayat SOS.") }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.data) { history ->
                                        SosHistoryCard(history)
                                    }
                                }
                            }
                        }
                        is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.message, color = Color.Red) }
                    }
                }
            }
        }
    }
}

@Composable
fun SosRequestCard(
    request: SosRequestResponse, 
    currentUserId: String, 
    onCall: () -> Unit, 
    onChat: () -> Unit,
    onComplete: () -> Unit
) {
    val isOwnRequest = request.requesterId == currentUserId

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color.Red), Alignment.Center) {
                    Text(text = request.bloodType, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(text = request.requesterName, fontWeight = FontWeight.Bold)
                    Text(text = "Butuh: ${request.quantity} Kantong", color = Color.Red, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Lokasi: ${request.location}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (request.notes.isNotBlank()) {
                Text(text = "\"${request.notes}\"", color = Color.Gray, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (isOwnRequest) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth().height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SOS Teratasi / Selesai", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onChat, modifier = Modifier.size(40.dp).background(Color(0xFFE3F2FD), CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.Blue)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onCall, modifier = Modifier.size(40.dp).background(Color(0xFFE8F5E9), CircleShape)) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}

@Composable
fun SosHistoryCard(history: SosRequestResponse) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(history.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color.Gray), Alignment.Center) {
                    Text(text = history.bloodType, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(text = "SOS Selesai", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text(text = dateString, fontSize = 11.sp, color = Color.Gray)
                }
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Lokasi: ${history.location}", fontSize = 13.sp)
            Text(text = "Jumlah: ${history.quantity} Kantong", fontSize = 13.sp)
        }
    }
}
