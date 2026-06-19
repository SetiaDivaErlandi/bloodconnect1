package com.example.bloodconnect.view.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bloodconnect.model.Donor
import com.example.bloodconnect.view.navigation.Screen
import com.example.bloodconnect.viewmodel.BloodViewModel
import com.example.bloodconnect.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDonorScreen(
    navController: NavController,
    viewModel: BloodViewModel
) {
    val bloodDataState by viewModel.bloodData.collectAsState()
    var selectedBloodType by remember { mutableStateOf("O+") }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cari Pendonor", color = Color.White, fontWeight = FontWeight.Bold) },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Cari lokasi atau nama pendonor...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Red,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = Color.Red,
                    focusedLabelColor = Color.Red,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )

            Text(
                text = "Filter Golongan Darah",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
                items(types) { type ->
                    FilterChip(
                        selected = type == selectedBloodType,
                        onClick = { selectedBloodType = type },
                        label = { 
                            Text(
                                type, 
                                color = if (type == selectedBloodType) Color.White else MaterialTheme.colorScheme.onSurface 
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Red,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            when (val state = bloodDataState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Red)
                    }
                }
                is UiState.Success -> {
                    val filteredDonors = state.data.donors.filter {
                        (it.bloodType == selectedBloodType) &&
                        (it.name.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true))
                    }

                    if (filteredDonors.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Tidak ada pendonor yang sesuai.", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredDonors) { donor ->
                                SearchDonorItem(
                                    donor = donor,
                                    onCall = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                                        context.startActivity(intent)
                                    },
                                    onChat = {
                                        navController.navigate(Screen.Chat.createRoute(donor.name))
                                    }
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SearchDonorItem(donor: Donor, onCall: () -> Unit, onChat: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = donor.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = donor.name, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp
                )
                Text(
                    text = "Golongan Darah: ${donor.bloodType}", 
                    fontSize = 12.sp, 
                    color = Color.Red, 
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = donor.location, 
                    fontSize = 12.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onChat,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE3F2FD).copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.Blue, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE8F5E9).copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
