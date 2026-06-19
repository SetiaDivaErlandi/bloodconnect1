package com.example.bloodconnect.view.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bloodconnect.view.navigation.Screen
import com.example.bloodconnect.viewmodel.AuthViewModel
import com.example.bloodconnect.viewmodel.BloodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    bloodViewModel: BloodViewModel,
    authViewModel: AuthViewModel
) {
    val userData by authViewModel.userData.collectAsState()
    val isDarkModeEnabled by authViewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    val dummyTotalDonations = 5
    val dummyLivesSaved = 12

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }

    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var oldPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var showOldPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editBloodType by remember { mutableStateOf("") }
    var editGender by remember { mutableStateOf("") }
    var editImageUrl by remember { mutableStateOf("") }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var isNotificationEnabled by remember { mutableStateOf(true) }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    
    var locationExpanded by remember { mutableStateOf(false) }
    var bloodTypeExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    val locations = listOf("Bandar Lampung")
    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val genders = listOf("Laki-laki", "Perempuan")

    val avatars = listOf(
        "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix&eyes=default&mouth=smile&eyebrowType=default",
        "https://api.dicebear.com/7.x/avataaars/svg?seed=Jack&eyes=default&mouth=smile&eyebrowType=default",
        "https://api.dicebear.com/7.x/avataaars/svg?seed=Lily&eyes=default&mouth=smile&eyebrowType=default",
        "https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka&eyes=default&mouth=smile&eyebrowType=default"
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(dialogTitle, fontWeight = FontWeight.Bold) },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK", color = Color.Red)
                }
            }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Konfirmasi Logout", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("Apakah Anda yakin ingin keluar dari akun Anda?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Keluar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                oldPasswordInput = ""
                newPasswordInput = ""
                confirmPasswordInput = ""
            },
            title = { Text("Ubah Password", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = oldPasswordInput,
                        onValueChange = { oldPasswordInput = it },
                        label = { Text("Password Lama") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showOldPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showOldPassword = !showOldPassword }) {
                                Icon(
                                    imageVector = if (showOldPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            focusedLabelColor = Color.Red,
                            cursorColor = Color.Red
                        )
                    )
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("Password Baru") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            focusedLabelColor = Color.Red,
                            cursorColor = Color.Red
                        )
                    )
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("Konfirmasi Password Baru") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            focusedLabelColor = Color.Red,
                            cursorColor = Color.Red
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedNew = newPasswordInput.trim()
                        val trimmedOldInput = oldPasswordInput.trim()
                        val trimmedConfirm = confirmPasswordInput.trim()
                        val currentPassword = (userData?.password ?: "").trim()
                        
                        if (trimmedOldInput.isEmpty() || trimmedNew.isEmpty() || trimmedConfirm.isEmpty()) {
                            Toast.makeText(context, "Semua bidang harus diisi", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (currentPassword.isNotEmpty() && trimmedOldInput != currentPassword) {
                            Toast.makeText(context, "Password lama tidak sesuai", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (trimmedNew.length < 8) {
                            Toast.makeText(context, "Password baru harus minimal 8 karakter!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (trimmedNew == currentPassword) {
                            Toast.makeText(context, "Password baru tidak boleh sama dengan password lama", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (trimmedNew != trimmedConfirm) {
                            Toast.makeText(context, "Konfirmasi password baru tidak cocok", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        authViewModel.changePassword(
                            newPassword = trimmedNew,
                            onSuccess = {
                                showChangePasswordDialog = false
                                oldPasswordInput = ""
                                newPasswordInput = ""
                                confirmPasswordInput = ""
                                Toast.makeText(context, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                            },
                            onError = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Ubah", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showChangePasswordDialog = false
                        oldPasswordInput = ""
                        newPasswordInput = ""
                        confirmPasswordInput = ""
                    }
                ) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profil", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama Lengkap") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            focusedLabelColor = Color.Red,
                            cursorColor = Color.Red
                        )
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Nomor Telepon") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            focusedLabelColor = Color.Red,
                            cursorColor = Color.Red
                        )
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = bloodTypeExpanded,
                        onExpandedChange = { bloodTypeExpanded = !bloodTypeExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editBloodType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Golongan Darah") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodTypeExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Red,
                                focusedLabelColor = Color.Red,
                                cursorColor = Color.Red
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = bloodTypeExpanded,
                            onDismissRequest = { bloodTypeExpanded = false }
                        ) {
                            bloodTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        editBloodType = type
                                        bloodTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editGender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jenis Kelamin") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Red,
                                focusedLabelColor = Color.Red,
                                cursorColor = Color.Red
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            genders.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g) },
                                    onClick = {
                                        editGender = g
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = locationExpanded,
                        onExpandedChange = { locationExpanded = !locationExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editLocation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Lokasi") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Red,
                                focusedLabelColor = Color.Red,
                                cursorColor = Color.Red
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = locationExpanded,
                            onDismissRequest = { locationExpanded = false }
                        ) {
                            locations.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        editLocation = city
                                        locationExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text(text = "Pilih Avatar Animasi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatars.forEach { url ->
                            val isSelected = editImageUrl == url
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.Red else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { editImageUrl = url }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.updateProfile(editName, editPhone, editLocation, editImageUrl, editBloodType, editGender)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Pengaturan", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notifikasi", fontWeight = FontWeight.Bold)
                            Text("Dapatkan pemberitahuan SOS terdekat", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { isNotificationEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.Red
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mode Gelap", fontWeight = FontWeight.Bold)
                            Text("Gunakan tema gelap untuk aplikasi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isDarkModeEnabled,
                            onCheckedChange = { authViewModel.setDarkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.Red
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Selesai", color = Color.White)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Tentang Aplikasi", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "BloodConnect v1.0.0",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "BloodConnect adalah platform sosial kemanusiaan yang menghubungkan pendonor darah dengan mereka yang membutuhkan secara real-time berdasarkan lokasi terdekat.",
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Tutup", color = Color.White)
                }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Bantuan & FAQ", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("1. Bagaimana cara mendonorkan darah?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Anda dapat menemukan permintaan SOS di menu SOS, lalu menekan tombol 'Hubungi' untuk berkomunikasi.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Mengerti", color = Color.White)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.Red,
                    RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (userData?.imageUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.Gray
                        )
                    }
                } else {
                    AsyncImage(
                        model = userData?.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userData?.name ?: "User",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " ${userData?.bloodType ?: "-"} | ${userData?.location ?: "-"} | ${userData?.gender ?: "Laki-laki"}",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStatCard(label = "Total Donor", value = "$dummyTotalDonations Kali")
            ProfileStatCard(label = "Nyawa Terselamatkan", value = "$dummyLivesSaved")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            ProfileMenuItem("Edit Profil", Icons.Default.Edit) {
                editName = userData?.name ?: ""
                editPhone = userData?.phone ?: ""
                editLocation = userData?.location ?: "Bandar Lampung"
                editBloodType = userData?.bloodType ?: "O+"
                editGender = userData?.gender ?: "Laki-laki"
                editImageUrl = userData?.imageUrl ?: ""
                showEditDialog = true
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            ProfileMenuItem("Pengaturan", Icons.Default.Settings) {
                showSettingsDialog = true
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            ProfileMenuItem("Tentang Aplikasi", Icons.Default.Info) {
                showAboutDialog = true
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            ProfileMenuItem("Bantuan", Icons.AutoMirrored.Filled.Help) {
                showHelpDialog = true
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            ProfileMenuItem("Ubah Password", Icons.Default.Lock) {
                showChangePasswordDialog = true
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                showLogoutConfirmDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = BorderStroke(1.dp, Color.Red)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProfileStatCard(label: String, value: String) {
    Card(
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Red)
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProfileMenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label, 
                modifier = Modifier.weight(1f), 
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
