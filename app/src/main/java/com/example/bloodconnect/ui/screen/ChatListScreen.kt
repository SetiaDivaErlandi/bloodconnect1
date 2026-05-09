package com.example.bloodconnect.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.bloodconnect.ui.navigation.Screen

data class ChatPreview(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController) {
    val chatHistory = listOf(
        ChatPreview("1", "Andi Pratama", "Oke, saya segera meluncur!", "10:35", "https://api.dicebear.com/7.x/avataaars/svg?seed=Andi"),
        ChatPreview("2", "Sinta Amelia", "Terima kasih banyak kak.", "Yesterday", "https://api.dicebear.com/7.x/avataaars/svg?seed=Sinta"),
        ChatPreview("3", "Rido Putra", "Bisa kirim lokasi detailnya?", "Monday", "https://api.dicebear.com/7.x/avataaars/svg?seed=Rido")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesan", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Red)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color.White)) {
            LazyColumn {
                items(chatHistory) { chat ->
                    ChatItem(chat) {
                        navController.navigate(Screen.Chat.route)
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: ChatPreview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = chat.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = chat.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = chat.lastMessage, fontSize = 14.sp, color = Color.Gray, maxLines = 1)
        }
        Text(text = chat.time, fontSize = 12.sp, color = Color.Gray)
    }
}
