package com.example.bloodconnect.ui.screen

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bloodconnect.data.model.Donor
import com.example.bloodconnect.ui.navigation.Screen
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import com.example.bloodconnect.ui.viewmodel.UiState

fun getLeafletHtml(donors: List<Donor>): String {
    val markersJs = donors.mapIndexed { index, donor ->
        // Generate coordinates clustered around Metro, Lampung (-5.1133, 105.3083)
        val lat = -5.1133 + (index * 0.007) - 0.004
        val lng = 105.3083 + (index * -0.008) + 0.003
        """
        L.marker([$lat, $lng], {
            icon: L.divIcon({
                className: 'custom-icon',
                html: '<div style="background-color: #E53935; color: white; width: 34px; height: 34px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; border: 2.5px solid white; box-shadow: 0 2px 6px rgba(0,0,0,0.4);">${donor.bloodType}</div>',
                iconSize: [34, 34],
                iconAnchor: [17, 17]
            })
        }).addTo(map)
          .bindPopup('<b>${donor.name}</b><br>${donor.bloodType} - ${donor.location}');
        """.trimIndent()
    }.joinToString("\n")

    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            html, body, #map { height: 100%; margin: 0; padding: 0; background-color: #f4f4f4; }
            .leaflet-control-zoom { display: none !important; }
            .leaflet-attribution-flag { display: none !important; }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <script>
            var map = L.map('map', { zoomControl: false }).setView([-5.1133, 105.3083], 14);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19
            }).addTo(map);

            // User location marker
            L.circle([-5.1133, 105.3083], {
                color: '#1E88E5',
                fillColor: '#1E88E5',
                fillOpacity: 0.4,
                radius: 120
            }).addTo(map).bindPopup('Lokasi Saya');
            
            L.circleMarker([-5.1133, 105.3083], {
                color: '#FFFFFF',
                fillColor: '#1E88E5',
                fillOpacity: 1.0,
                weight: 2,
                radius: 8
            }).addTo(map).bindPopup('Lokasi Saya');

            $markersJs
        </script>
    </body>
    </html>
    """.trimIndent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: BloodViewModel) {
    val bloodDataState by viewModel.bloodData.collectAsState()
    var selectedBloodType by remember { mutableStateOf("O+") }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 280.dp,
        sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        sheetShadowElevation = 20.dp,
        sheetContainerColor = Color.White,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(450.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Donor Terdekat", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = "Reset Filter", 
                        color = Color.Red, 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { 
                            selectedBloodType = "O+"
                            searchQuery = ""
                        }
                    )
                }

                when (val state = bloodDataState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.Red)
                        }
                    }
                    is UiState.Success -> {
                        val filteredDonors = state.data.donors.filter { 
                            (it.bloodType == selectedBloodType || selectedBloodType == "O+") &&
                            (it.name.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true))
                        }
                        
                        if (filteredDonors.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = "Tidak ada pendonor yang sesuai.", color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(filteredDonors) { donor ->
                                    MapDonorItem(
                                        donor = donor, 
                                        onCall = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${if (donor.name == "Andi Pratama") "08123456789" else "08987654321"}"))
                                            context.startActivity(intent)
                                        }, 
                                        onChat = {
                                            navController.navigate(Screen.Chat.route)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        Text(text = "Gagal memuat data pendonor", color = Color.Red)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // 1. The Map Layer (Interactive WebView)
            when (val state = bloodDataState) {
                is UiState.Success -> {
                    val filteredDonors = state.data.donors.filter { 
                        (it.bloodType == selectedBloodType || selectedBloodType == "O+") &&
                        (it.name.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true))
                    }
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                webViewClient = WebViewClient()
                                loadDataWithBaseURL(null, getLeafletHtml(filteredDonors), "text/html", "UTF-8", null)
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(null, getLeafletHtml(filteredDonors), "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Red)
                    }
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White,
                            shadowElevation = 6.dp
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cari lokasi atau pendonor...", fontSize = 14.sp, color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    disabledContainerColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = Color.Red,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
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
                            label = { Text(type, color = if (type == selectedBloodType) Color.White else Color.Black) },
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
            Text(text = donor.name, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
