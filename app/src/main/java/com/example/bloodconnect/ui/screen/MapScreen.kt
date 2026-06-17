package com.example.bloodconnect.ui.screen

import android.Manifest
import android.content.Context
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
import com.example.bloodconnect.ui.viewmodel.AuthViewModel
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
import kotlin.random.Random

fun getCoordinatesForLocation(locationName: String, id: String = ""): GeoPoint {
    val clean = locationName.lowercase().trim()
    val base = when {
        clean.contains("teluk betung") -> GeoPoint(-5.4497, 105.2633)
        clean.contains("kedaton") -> GeoPoint(-5.3821, 105.2589)
        clean.contains("way halim") -> GeoPoint(-5.3921, 105.2822)
        clean.contains("sukarame") -> GeoPoint(-5.3911, 105.3011)
        clean.contains("panjang") -> GeoPoint(-5.4716, 105.3183)
        clean.contains("tanjung karang") -> GeoPoint(-5.4167, 105.2500)
        clean.contains("kemiling") -> GeoPoint(-5.3980, 105.2180)
        clean.contains("rajabasa") -> GeoPoint(-5.3750, 105.2420)
        else -> GeoPoint(-5.3971, 105.2668)
    }
    
    val seed = (id + locationName).hashCode().toLong()
    val random = Random(seed)
    val offsetLat = (random.nextDouble() - 0.5) * 0.02
    val offsetLng = (random.nextDouble() - 0.5) * 0.02
    
    return GeoPoint(base.latitude + offsetLat, base.longitude + offsetLng)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: BloodViewModel,
    authViewModel: AuthViewModel
) {
    val bloodDataState by viewModel.bloodData.collectAsState()
    val sosRequestsState by viewModel.sosRequests.collectAsState()
    val currentUser by authViewModel.userData.collectAsState()

    var selectedBloodType by remember { mutableStateOf("Semua") }
    var locationSearchQuery by remember { mutableStateOf("") }
    
    var showSos by remember { mutableStateOf(true) }
    var showDonors by remember { mutableStateOf(true) }
    var showUser by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var selectedDonor by remember { mutableStateOf<Donor?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val dummyDonors = remember {
        listOf(
            Donor("d1", "Hendra Wijaya", "O+", "0.5 km", "Teluk Betung", "", "081277665544", -5.4497, 105.2633),
            Donor("d2", "Siti Aminah", "O+", "0.8 km", "Teluk Betung Utara", "", "081399887766", -5.4421, 105.2689),
            Donor("d3", "Budi Santoso", "O+", "1.2 km", "Teluk Betung Barat", "", "085211223344", -5.4522, 105.2511),
            Donor("d13", "Ahmad Fauzi", "A+", "1.5 km", "Teluk Betung", "", "081212121212", -5.4550, 105.2600),
            Donor("d37", "Rizky Pratama", "O+", "0.6 km", "Teluk Betung", "", "081234567808", -5.4460, 105.2620),
            Donor("d38", "Anisa Rahma", "A-", "0.7 km", "Teluk Betung", "", "081234567809", -5.4440, 105.2670),
            Donor("d4", "Rina Putri", "A+", "0.8 km", "Kedaton", "", "081122334455", -5.3821, 105.2589),
            Donor("d5", "Andi Pratama", "B+", "1.5 km", "Kedaton", "", "081955443322", -5.3755, 105.2511),
            Donor("d22", "Kurniawan", "O-", "1.7 km", "Kedaton", "", "081234567893", -5.3800, 105.2550),
            Donor("d6", "Dewi Lestari", "O+", "2.1 km", "Way Halim", "", "081288776655", -5.3921, 105.2822),
            Donor("d25", "Indah Sari", "B+", "2.3 km", "Way Halim", "", "081234567896", -5.3850, 105.2800),
            Donor("d8", "Eko Prasetyo", "O+", "3.2 km", "Sukarame", "", "085711223344", -5.3911, 105.3011),
            Donor("d9", "Maya Sari", "A-", "3.5 km", "Korpri Sukarame", "", "081233445566", -5.3855, 105.3122)
        )
    }

    val defaultPoint = GeoPoint(-5.3971, 105.2668)
    val mapView = remember { MapView(context) }
    var userLocationPoint by remember { mutableStateOf<GeoPoint?>(null) }
    
    var hasInitializedFocus by remember { mutableStateOf(false) }

    val fetchLocation = {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasFine || hasCoarse) {
                val provider = if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    android.location.LocationManager.GPS_PROVIDER
                } else {
                    android.location.LocationManager.NETWORK_PROVIDER
                }
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    userLocationPoint = GeoPoint(location.latitude, location.longitude)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            fetchLocation()
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
        
        viewModel.fetchSosRequests()
        viewModel.fetchBloodData()
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            fetchLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val finalUserPoint = remember(userLocationPoint, currentUser) {
        userLocationPoint ?: currentUser?.let { user ->
            val clean = user.location.lowercase().trim()
            val base = when {
                clean.contains("teluk betung") -> GeoPoint(-5.4497, 105.2633)
                clean.contains("kedaton") -> GeoPoint(-5.3821, 105.2589)
                clean.contains("way halim") -> GeoPoint(-5.3921, 105.2822)
                clean.contains("sukarame") -> GeoPoint(-5.3911, 105.3011)
                clean.contains("panjang") -> GeoPoint(-5.4716, 105.3183)
                clean.contains("tanjung karang") -> GeoPoint(-5.4167, 105.2500)
                clean.contains("kemiling") -> GeoPoint(-5.3980, 105.2180)
                clean.contains("rajabasa") -> GeoPoint(-5.3750, 105.2420)
                else -> GeoPoint(-5.3971, 105.2668)
            }
            base
        } ?: defaultPoint
    }

    LaunchedEffect(finalUserPoint) {
        if (!hasInitializedFocus && finalUserPoint != defaultPoint) {
            mapView.controller.animateTo(finalUserPoint)
            mapView.controller.setZoom(15.0)
            hasInitializedFocus = true
        }
    }

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

    fun searchLocation(query: String) {
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
                                Toast.makeText(context, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
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
                        controller.setZoom(15.0)
                        controller.setCenter(finalUserPoint)
                    }
                },
                update = { view ->
                    view.overlays.clear()

                    if (showDonors) {
                        val firebaseDonors = if (bloodDataState is UiState.Success) {
                            (bloodDataState as UiState.Success).data.donors.filter { it.id != (currentUser?.id ?: "") }
                        } else emptyList()
                        val allDonors = firebaseDonors + dummyDonors
                        val filteredDonors = allDonors.filter {
                            selectedBloodType == "Semua" || it.bloodType.equals(selectedBloodType, ignoreCase = true)
                        }

                        filteredDonors.forEach { donor ->
                            val point = if (donor.latitude != null && donor.longitude != null) {
                                GeoPoint(donor.latitude, donor.longitude)
                            } else {
                                getCoordinatesForLocation(donor.location, donor.id)
                            }
                            val marker = Marker(view)
                            marker.position = point
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = donor.name
                            marker.snippet = "Pendonor - Golongan Darah: ${donor.bloodType}"
                            marker.icon = ContextCompat.getDrawable(context, com.example.bloodconnect.R.drawable.ic_pin_green)
                            marker.setOnMarkerClickListener { m, _ ->
                                selectedDonor = donor
                                showBottomSheet = true
                                m.showInfoWindow()
                                true
                            }
                            view.overlays.add(marker)
                        }
                    }

                    if (showSos) {
                        val allSos = if (sosRequestsState is UiState.Success) {
                            (sosRequestsState as UiState.Success).data.filter { it.requesterId != (currentUser?.id ?: "") }
                        } else emptyList()

                        val filteredSos = allSos.filter {
                            selectedBloodType == "Semua" || it.bloodType.equals(selectedBloodType, ignoreCase = true)
                        }

                        filteredSos.forEach { request ->
                            val loc = request.location ?: ""
                            val reqId = request.requesterId ?: ""
                            val point = getCoordinatesForLocation(loc, reqId)
                            val marker = Marker(view)
                            marker.position = point
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = request.requesterName ?: "User"
                            marker.snippet = "Butuh Darah: ${request.bloodType ?: "-"} (Qty: ${request.quantity})"
                            marker.icon = ContextCompat.getDrawable(context, com.example.bloodconnect.R.drawable.ic_pin_red)
                            marker.setOnMarkerClickListener { m, _ ->
                                selectedDonor = Donor(
                                    id = reqId,
                                    name = request.requesterName ?: "User",
                                    bloodType = request.bloodType ?: "-",
                                    distance = "SOS",
                                    location = loc,
                                    imageUrl = "",
                                    phone = if (request.requesterPhone.isNullOrBlank() || request.requesterPhone == "null") "08123456789" else request.requesterPhone
                                )
                                showBottomSheet = true
                                m.showInfoWindow()
                                true
                            }
                            view.overlays.add(marker)
                        }
                    }

                    if (showUser) {
                        val selfMarker = Marker(view)
                        selfMarker.position = finalUserPoint
                        selfMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        selfMarker.title = currentUser?.name ?: "Lokasi Saya"
                        selfMarker.snippet = "Lokasi Saya ${currentUser?.let { "(Golongan: ${it.bloodType})" } ?: ""}"
                        selfMarker.icon = ContextCompat.getDrawable(context, com.example.bloodconnect.R.drawable.ic_pin_blue)
                        selfMarker.setOnMarkerClickListener { m, _ ->
                            currentUser?.let { me ->
                                selectedDonor = Donor(
                                    id = me.id,
                                    name = me.name,
                                    bloodType = me.bloodType,
                                    distance = "0 km",
                                    location = me.location,
                                    imageUrl = me.imageUrl,
                                    phone = me.phone
                                )
                                showBottomSheet = true
                            }
                            m.showInfoWindow()
                            true
                        }
                        view.overlays.add(selfMarker)
                    }

                    view.invalidate()
                }
            )

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
                            modifier = Modifier.background(Color.White, CircleShape).size(48.dp)
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
                
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = showSos,
                        onClick = { 
                            showSos = !showSos 
                            if (showSos) {
                            }
                        },
                        label = { Text("Butuh Darah", fontSize = 12.sp) },
                        leadingIcon = { Box(Modifier.size(10.dp).background(Color.Red, CircleShape)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.Red.copy(alpha = 0.2f))
                    )
                    FilterChip(
                        selected = showDonors,
                        onClick = { showDonors = !showDonors },
                        label = { Text("Pendonor", fontSize = 12.sp) },
                        leadingIcon = { Box(Modifier.size(10.dp).background(Color(0xFF4CAF50), CircleShape)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.2f))
                    )
                    FilterChip(
                        selected = showUser,
                        onClick = { 
                            showUser = !showUser 
                            if (showUser) {
                                mapView.controller.animateTo(finalUserPoint)
                                mapView.controller.setZoom(16.0)
                            }
                        },
                        label = { Text("Saya", fontSize = 12.sp) },
                        leadingIcon = { Box(Modifier.size(10.dp).background(Color(0xFF2196F3), CircleShape)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2196F3).copy(alpha = 0.2f))
                    )
                }
            }
        }

        if (showBottomSheet && selectedDonor != null) {
            val isMe = selectedDonor!!.id == (currentUser?.id ?: "")
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
                    Text(text = if (isMe) "Profil Saya" else "Detail Pengguna", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                            Text(
                                text = if (isMe) "Ini Anda, ${selectedDonor!!.name}" else selectedDonor!!.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            if (!isMe) {
                                Text(text = "Lokasi: ${selectedDonor!!.location}", color = Color.Gray)
                                Text(text = "No. HP: ${selectedDonor!!.phone}", color = Color.Black)
                            }
                        }
                    }
                    
                    if (!isMe) {
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
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ini adalah lokasi Anda saat ini.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}
