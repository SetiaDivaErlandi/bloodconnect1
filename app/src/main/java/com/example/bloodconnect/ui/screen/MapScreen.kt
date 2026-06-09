package com.example.bloodconnect.ui.screen

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.bloodconnect.data.model.Donor
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import com.example.bloodconnect.ui.viewmodel.UiState

fun getLeafletHtml(donors: List<Donor>): String {
    val markersJs = donors.mapIndexed { index, donor ->
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

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
