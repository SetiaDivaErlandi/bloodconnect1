package com.example.bloodconnect.ui.screen

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
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.bloodconnect.ui.navigation.Screen

data class Message(
    val text: String,
    val isFromMe: Boolean,
    val time: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController) {

    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val messages = remember {
        mutableStateListOf(
            Message("Halo, saya siap membantu.", false, "10:30"),
            Message("Terima kasih banyak!", true, "10:31"),
            Message("Saya berada di dekat RSUD Abdul Moeloek.", false, "10:32"),
            Message("Oke, saya segera menuju lokasi.", true, "10:33"),
            Message("Baik, hati-hati ya 🙏", false, "10:33")
        )
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),

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
                                text = "Andi Pratama",
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
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:08123456789"))
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
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
                color = Color.White
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
                            focusedContainerColor = Color(0xFFF3F3F3),
                            unfocusedContainerColor = Color(0xFFF3F3F3),

                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,

                            cursorColor = Color.Red
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    FloatingActionButton(
                        onClick = {

                            if (messageText.isNotBlank()) {

                                messages.add(
                                    Message(
                                        text = messageText,
                                        isFromMe = true,
                                        time = "Now"
                                    )
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
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Lokasi Saya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "RSUD Abdul Moeloek",
                        color = Color.Gray,
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
                        Text("Lihat di Map")
                    }
                }
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

                items(messages) { message ->

                    ChatBubble(message)
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
                Color.White,

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
                    Color.Black,

                fontSize = 14.sp,

                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = message.time,
            color = Color.Gray,
            fontSize = 10.sp
        )
    }
}