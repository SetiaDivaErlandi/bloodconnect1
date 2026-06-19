package com.example.bloodconnect.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bloodconnect.view.components.ArticleCard
import com.example.bloodconnect.view.navigation.Screen
import com.example.bloodconnect.viewmodel.BloodViewModel
import com.example.bloodconnect.viewmodel.UiState
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationBookmarkScreen(navController: NavController, viewModel: BloodViewModel) {
    val bloodDataState by viewModel.bloodData.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val readHistory by viewModel.readHistory.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edukasi Saya", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color.Red,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color.Red
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { 
                        Text(
                            "Bookmark", 
                            fontWeight = FontWeight.Bold, 
                            color = if (selectedTab == 0) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { 
                        Text(
                            "Riwayat Baca", 
                            fontWeight = FontWeight.Bold, 
                            color = if (selectedTab == 1) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
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
                    val filteredArticles = state.data.articles.filter { article ->
                        if (selectedTab == 0) {
                            bookmarks.contains(article.title)
                        } else {
                            readHistory.contains(article.title)
                        }
                    }

                    if (filteredArticles.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (selectedTab == 0) "Belum ada artikel yang dibookmark." else "Belum ada riwayat membaca.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredArticles) { article ->
                                ArticleCard(article) {
                                    val encodedTitle = URLEncoder.encode(article.title, StandardCharsets.UTF_8.toString())
                                    val encodedUrl = URLEncoder.encode(article.imageUrl, StandardCharsets.UTF_8.toString())
                                    navController.navigate(Screen.EducationDetail.createRoute(encodedTitle, encodedUrl))
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Text(text = state.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
