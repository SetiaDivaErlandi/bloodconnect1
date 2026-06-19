package com.example.bloodconnect.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.bloodconnect.model.ChatMessage
import com.example.bloodconnect.view.navigation.Screen
import com.example.bloodconnect.viewmodel.BloodViewModel
import com.example.bloodconnect.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Message(
    val text: String,
    val isFromMe: Boolean,
    val time: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    chatName: String,
    viewModel: BloodViewModel,
    authViewModel: AuthViewModel
) {
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentUser by authViewModel.userData.collectAsState()
    val messagesState by viewModel.chatMessages.collectAsState()

    var contactId by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(chatName) {
        scope.launch(Dispatchers.IO) {
            val donors = viewModel.repository.getBloodData().donors
            val matchedDonor = donors.find { it.name.equals(chatName, ignoreCase = true) }
            if (matchedDonor != null) {
                contactId = matchedDonor.id
                contactPhone = matchedDonor.phone
                contactImageUrl = matchedDonor.imageUrl
            } else {
                val users = viewModel.repository.getUsers()
                val matchedUser = users.find { it.name?.equals(chatName, ignoreCase = true) == true }
                if (matchedUser != null) {
                    contactId = matchedUser.id ?: ""
                    contactPhone = matchedUser.phone ?: "08123456789"
                    contactImageUrl = matchedUser.imageUrl ?: ""
                } else {
                    contactId = chatName.lowercase().replace(" ", "_")
                    contactPhone = "08123456789"
                    contactImageUrl = ""
                }
            }
        }
    }

    val roomId = remember(currentUser, contactId) {
        val myId = currentUser?.id ?: ""
        if (myId.isNotEmpty() && contactId.isNotEmpty()) {
            if (myId < contactId) "${myId}_${contactId}" else "${contactId}_${myId}"
        } else {
            ""
        }
    }

    LaunchedEffect(roomId) {
        if (roomId.isNotEmpty() && currentUser != null) {
            viewModel.clearUnreadCount(currentUser!!.id, contactId)
            while (true) {
                viewModel.fetchChatMessages(roomId)
                delay(2000)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE53935)
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = chatName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Online",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (contactPhone.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$contactPhone"))
                            context.startActivity(intent)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 10.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 14.dp,
                            vertical = 10.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = {
                            messageText = it
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text("Ketik pesan...")
                        },
                        shape = RoundedCornerShape(30.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Red,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank() && currentUser != null && contactId.isNotEmpty() && roomId.isNotEmpty()) {
                                viewModel.sendChatMessage(
                                    roomId = roomId,
                                    senderId = currentUser!!.id,
                                    senderName = currentUser!!.name,
                                    senderImageUrl = currentUser!!.imageUrl,
                                    recipientId = contactId,
                                    recipientName = chatName,
                                    recipientImageUrl = contactImageUrl,
                                    text = messageText
                                )
                                messageText = ""
                            }
                        },
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White,
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Lokasi Saya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentUser?.location ?: "RSUD Abdul Moeloek",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            navController.navigate(Screen.Map.route)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Lihat di Map", color = Color.White)
                    }
                }
            }

            val displayMessages = remember(messagesState, chatName, currentUser) {
                val myId = currentUser?.id ?: "me"
                val mockList = when {
                    chatName.contains("Andi", ignoreCase = true) -> {
                        listOf(
                            com.example.bloodconnect.model.ChatMessage("m1", "d5", "Andi Pratama", "Halo, apakah Anda masih membutuhkan donor darah B+?", System.currentTimeMillis() - 7200000),
                            com.example.bloodconnect.model.ChatMessage("m2", myId, "Me", "Iya benar, apakah Anda bersedia mendonorkan darah?", System.currentTimeMillis() - 6600000),
                            com.example.bloodconnect.model.ChatMessage("m3", "d5", "Andi Pratama", "Tentu, saya bisa ke rumah sakit sekarang.", System.currentTimeMillis() - 6000000)
                        )
                    }
                    chatName.contains("Sinta", ignoreCase = true) -> {
                        listOf(
                            com.example.bloodconnect.model.ChatMessage("m4", "sinta_amelia", "Sinta Amelia", "Hai, saya melihat postingan SOS Anda. Golongan darah saya A+.", System.currentTimeMillis() - 10200000),
                            com.example.bloodconnect.model.ChatMessage("m5", myId, "Me", "Terima kasih banyak! Bisakah kita bertemu di RSUD?", System.currentTimeMillis() - 9600000),
                            com.example.bloodconnect.model.ChatMessage("m6", "sinta_amelia", "Sinta Amelia", "Bisa, saya sedang dalam perjalanan ke sana.", System.currentTimeMillis() - 9000000)
                        )
                    }
                    chatName.contains("Rido", ignoreCase = true) -> {
                        listOf(
                            com.example.bloodconnect.model.ChatMessage("m7", "rido_putra", "Rido Putra", "Butuh golongan darah O+ mendesak ya?", System.currentTimeMillis() - 13200000),
                            com.example.bloodconnect.model.ChatMessage("m8", myId, "Me", "Betul kak, untuk keluarga saya.", System.currentTimeMillis() - 12600000),
                            com.example.bloodconnect.model.ChatMessage("m9", "rido_putra", "Rido Putra", "Baik, saya siap membantu. Hubungi saya jika sudah di lokasi.", System.currentTimeMillis() - 12000000)
                        )
                    }
                    else -> emptyList()
                }
                val firebaseList = messagesState.filter { fMsg ->
                    mockList.none { mMsg -> mMsg.text.trim().equals(fMsg.text.trim(), ignoreCase = true) }
                }
                mockList + firebaseList
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(
                    bottom = 20.dp
                )
            ) {
                items(displayMessages) { message ->
                    val isFromMe = message.senderId == (currentUser?.id ?: "")
                    val timeStr = try {
                        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                    } catch (e: Exception) {
                        "Now"
                    }
                    ChatBubble(
                        Message(
                            text = message.text,
                            isFromMe = isFromMe,
                            time = timeStr
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromMe)
            Alignment.End
        else
            Alignment.Start
    ) {
        Surface(
            color = if (message.isFromMe)
                Color(0xFFE53935)
            else
                MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isFromMe) 18.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 18.dp
            ),
            shadowElevation = 2.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
                color = if (message.isFromMe)
                    Color.White
                else
                    MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message.time,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}
