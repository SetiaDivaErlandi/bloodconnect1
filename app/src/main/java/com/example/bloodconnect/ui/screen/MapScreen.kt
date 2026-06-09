package com.example.bloodconnect.ui.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.bloodconnect.data.model.Donor
import com.example.bloodconnect.ui.navigation.Screen
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import com.example.bloodconnect.ui.viewmodel.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: BloodViewModel) {
    val bloodDataState by viewModel.bloodData.collectAsState()
    var selectedBloodType by remember { mutableStateOf("Semua") }
    var locationSearchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var selectedDonor by remember { mutableStateOf<Donor?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // DATA PENDONOR SIMULASI YANG LEBIH LENGKAP
    val dummyDonors = remember {
        listOf(
            // Area Teluk Betung
            Donor("d1", "Hendra Wijaya", "O+", "0.5 km", "Teluk Betung", "", "081277665544", -5.4497, 105.2633),
            Donor("d2", "Siti Aminah", "O+", "0.8 km", "Teluk Betung Utara", "", "081399887766", -5.4421, 105.2689),
            Donor("d3", "Budi Santoso", "O+", "1.2 km", "Teluk Betung Barat", "", "085211223344", -5.4522, 105.2511),
            Donor("d13", "Ahmad Fauzi", "A+", "1.5 km", "Teluk Betung", "", "081212121212", -5.4550, 105.2600),
            Donor("d37", "Rizky Pratama", "O+", "0.6 km", "Teluk Betung", "", "081234567808", -5.4460, 105.2620),
            Donor("d38", "Anisa Rahma", "A-", "0.7 km", "Teluk Betung", "", "081234567809", -5.4440, 105.2670),

            // Area Kedaton
            Donor("d4", "Rina Putri", "A+", "0.8 km", "Kedaton", "", "081122334455", -5.3821, 105.2589),
            Donor("d5", "Andi Pratama", "B+", "1.5 km", "Kedaton", "", "081955443322", -5.3755, 105.2511),
            Donor("d22", "Kurniawan", "O-", "1.7 km", "Kedaton", "", "081234567893", -5.3800, 105.2550),

            // Area Way Halim
            Donor("d6", "Dewi Lestari", "O+", "2.1 km", "Way Halim", "", "081288776655", -5.3921, 105.2822),
            Donor("d25", "Indah Sari", "B+", "2.3 km", "Way Halim", "", "081234567896", -5.3850, 105.2800),

            // Area Sukarame
            Donor("d8", "Eko Prasetyo", "O+", "3.2 km", "Sukarame", "", "085711223344", -5.3911, 105.3011),
            Donor("d9", "Maya Sari", "A-", "3.5 km", "Korpri Sukarame", "", "081233445566", -5.3855, 105.3122)
        )
    }

    val defaultPoint = GeoPoint(-5.3971, 105.2668) // Bandar Lampung
    val mapView = remember { MapView(context) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Kelola Lifecycle MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Fungsi Geocoder untuk mencari lokasi
    fun searchLocation(query: String) {
        // Tambahkan konteks kota agar pencarian lebih akurat
        val finalQuery = if (!query.lowercase().contains("lampung")) "$query, Bandar Lampung" else query

        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(finalQuery, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<android.location.Address>) {
                            if (addresses.isNotEmpty()) {
                                val address = addresses[0]
                                val targetPoint = GeoPoint(address.latitude, address.longitude)
                                scope.launch(Dispatchers.Main) {
                                    mapView.controller.animateTo(targetPoint)
                                    mapView.controller.setZoom(16.0)
                                }
                            }
                        }
                        override fun onError(errorMessage: String?) {
                            scope.launch(Dispatchers.Main) {
                                Toast.makeText(context, "Lokasi tidak ditemukan: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(finalQuery, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val targetPoint = GeoPoint(address.latitude, address.longitude)
                        withContext(Dispatchers.Main) {
                            mapView.controller.animateTo(targetPoint)
                            mapView.controller.setZoom(16.0)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    mapView.apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(14.0)
                        controller.setCenter(defaultPoint)
                    }
                },
                update = { view ->
                    view.overlays.clear()

                    // Gabungkan data Firebase dan Dummy
                    val firebaseDonors = if (bloodDataState is UiState.Success) (bloodDataState as UiState.Success).data.donors else emptyList()
                    val allDonors = firebaseDonors + dummyDonors

                    // Filter berdasarkan Golongan Darah
                    val filteredDonors = allDonors.filter {
                        selectedBloodType == "Semua" || it.bloodType.equals(selectedBloodType, ignoreCase = true)
                    }

                    filteredDonors.forEach { donor ->
                        val lat = donor.latitude ?: (-5.3971 + (Math.random() * 0.05))
                        val lng = donor.longitude ?: (105.2668 + (Math.random() * 0.05))

                        val marker = Marker(view)
                        marker.position = GeoPoint(lat, lng)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = donor.name
                        marker.snippet = "Golongan Darah: ${donor.bloodType}"

                        marker.setOnMarkerClickListener { m, _ ->
                            selectedDonor = donor
                            showBottomSheet = true
                            m.showInfoWindow()
                            true
                        }
                        view.overlays.add(marker)
                    }
                    view.invalidate()
                }
            )

            // UI Search Bar & Filter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    color = Color.Transparent
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 6.dp
                        ) {
                            TextField(
                                value = locationSearchQuery,
                                onValueChange = { locationSearchQuery = it },
                                placeholder = { Text("Cari lokasi (cth: Teluk Betung)") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = Color.Red
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    if (locationSearchQuery.isNotBlank()) {
                                        TextButton(onClick = { searchLocation(locationSearchQuery) }) {
                                            Text("CARI", color = Color.Red, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("Semua", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
                    items(types) { type ->
                        FilterChip(
                            selected = type == selectedBloodType,
                            onClick = { selectedBloodType = type },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Red,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Bottom Sheet Detail Pendonor
        if (showBottomSheet && selectedDonor != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(text = "Detail Pendonor", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(60.dp).background(Color.Red, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedDonor!!.bloodType, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = selectedDonor!!.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Lokasi: ${selectedDonor!!.location}", color = Color.Gray)
                            Text(text = "No. HP: ${selectedDonor!!.phone}", color = Color.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                showBottomSheet = false
                                navController.navigate(Screen.Chat.createRoute(selectedDonor!!.name))
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chat")
                        }
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${selectedDonor!!.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Telepon")
                        }
                    }
                }
            }
        }
    }
}
