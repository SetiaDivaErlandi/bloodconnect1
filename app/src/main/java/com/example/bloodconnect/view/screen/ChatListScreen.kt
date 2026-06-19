package com.example.bloodconnect.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bloodconnect.model.ChatListEntry
import com.example.bloodconnect.view.navigation.Screen
import com.example.bloodconnect.viewmodel.BloodViewModel
import com.example.bloodconnect.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: BloodViewModel,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.userData.collectAsState()
    val chatHistory by viewModel.chatList.collectAsState()

    val mockChats = remember {
        listOf(
            ChatListEntry(
                contactId = "d5",
                contactName = "Andi Pratama",
                contactImageUrl = "",
                lastMessage = "Tentu, saya bisa ke rumah sakit sekarang.",
                timestamp = System.currentTimeMillis() - 7200000,
                unreadCount = 1
            ),
            ChatListEntry(
                contactId = "sinta_amelia",
                contactName = "Sinta Amelia",
                contactImageUrl = "",
                lastMessage = "Bisa, saya sedang dalam perjalanan ke sana.",
                timestamp = System.currentTimeMillis() - 10200000,
                unreadCount = 0
            ),
            ChatListEntry(
                contactId = "rido_putra",
                contactName = "Rido Putra",
                contactImageUrl = "",
                lastMessage = "Baik, saya siap membantu. Hubungi saya jika sudah di lokasi.",
                timestamp = System.currentTimeMillis() - 13200000,
                unreadCount = 0
            )
        )
    }

    val displayChats = remember(chatHistory, mockChats) {
        val list = chatHistory.toMutableList()
        mockChats.forEach { mock ->
            if (list.none { it.contactId == mock.contactId || it.contactName.equals(mock.contactName, ignoreCase = true) }) {
                list.add(mock)
            }
        }
        list.sortedByDescending { it.timestamp }
    }

    var searchQuery by remember { mutableStateOf("") }
    val filteredChats = remember(displayChats, searchQuery) {
        if (searchQuery.isBlank()) {
            displayChats
        } else {
            displayChats.filter {
                it.contactName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.id?.let { userId ->
            viewModel.fetchChatList(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesan", color = Color.White, fontWeight = FontWeight.Bold) },
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
            if (displayChats.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari kontak...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Red,
                        focusedLabelColor = Color.Red,
                        cursorColor = Color.Red
                    )
                )
            }

            if (filteredChats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "Tidak ada pesan aktif." else "Tidak ada hasil pencarian.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn {
                    items(filteredChats) { chat ->
                        ChatItem(chat) {
                            navController.navigate(Screen.Chat.createRoute(chat.contactName))
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: ChatListEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val avatarUrl = if (chat.contactImageUrl.isNullOrBlank()) {
            "https://api.dicebear.com/7.x/avataaars/svg?seed=${chat.contactName}&eyes=default&mouth=smile&eyebrowType=default"
        } else {
            chat.contactImageUrl
        }

        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.contactName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.lastMessage,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val timeStr = try {
                sdf.format(java.util.Date(chat.timestamp))
            } catch (e: Exception) {
                ""
            }
            Text(text = timeStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (chat.unreadCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
