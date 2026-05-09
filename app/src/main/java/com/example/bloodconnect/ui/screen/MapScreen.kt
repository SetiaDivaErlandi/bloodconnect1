package com.example.bloodconnect.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bloodconnect.data.remote.Donor
import com.example.bloodconnect.ui.navigation.Screen
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import com.example.bloodconnect.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: BloodViewModel) {
    val bloodDataState by viewModel.bloodData.collectAsState()
    var selectedBloodType by remember { mutableStateOf("O+") }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Zoom and Pan state for the map background
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // 1. The Map Layer (Zoomable & Pannable)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0E0E0))
                .transformable(state = transformState)
                .graphicsLayer(
                    scaleX = scale.coerceIn(0.5f, 5f),
                    scaleY = scale.coerceIn(0.5f, 5f),
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            // Simulated Markers on the map
            MapMarker(Modifier.align(Alignment.Center).offset(x = (-100).dp, y = (-40).dp), "A+")
            MapMarker(Modifier.align(Alignment.Center).offset(x = 120.dp, y = (-160).dp), "B+")
            MapMarker(Modifier.align(Alignment.Center).offset(x = 40.dp, y = 80.dp), "O-")
            MapMarker(Modifier.align(Alignment.Center).offset(x = (-160).dp, y = 220.dp), "AB+")
            
            // Current Location Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
                    .background(Color.Blue.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(10.dp).background(Color.Blue, CircleShape))
            }
        }

        // 2. The Interaction Layer (Top Bar & Search) - On top of the map
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Transparent
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Search Bar - Fixed height and clear container to ensure clickability
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        shadowElevation = 6.dp
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari lokasi atau pendonor...", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color.Red
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(28.dp)
                        )
                    }
                }
            }
            
            // Filter Chips Layer
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
                items(types) { type ->
                    FilterChip(
                        selected = type == selectedBloodType,
                        onClick = { selectedBloodType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Red,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color.Black
                        ),
                        elevation = FilterChipDefaults.filterChipElevation(elevation = 4.dp)
                    )
                }
            }
        }

        // 3. Bottom List Layer
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 20.dp,
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 4.dp)
                        .background(Color.LightGray, RoundedCornerShape(2.dp))
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Donor Terdekat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = "Lihat Semua", 
                        color = Color.Red, 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { /* Reset filters */ }
                    )
                }

                when (val state = bloodDataState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.Red)
                        }
                    }
                    is UiState.Success -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            val filteredDonors = state.data.donors.filter { 
                                (it.bloodType == selectedBloodType || selectedBloodType == "O+") &&
                                (it.name.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true))
                            }
                            items(filteredDonors) { donor ->
                                MapDonorItem(
                                    donor = donor, 
                                    onCall = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:08123456789"))
                                        context.startActivity(intent)
                                    }, 
                                    onChat = {
                                        navController.navigate(Screen.Chat.route)
                                    }
                                )
                            }
                        }
                    }
                    is UiState.Error -> {
                        Text(text = "Gagal memuat data pendonor", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun MapMarker(modifier: Modifier, bloodType: String) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(Color.Red.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.Red, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = bloodType, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MapDonorItem(donor: Donor, onCall: () -> Unit, onChat: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = donor.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = donor.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = donor.location, fontSize = 12.sp, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onChat, 
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFFE3F2FD), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.Blue, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onCall, 
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFFE8F5E9), CircleShape)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
            }
        }
    }
}
