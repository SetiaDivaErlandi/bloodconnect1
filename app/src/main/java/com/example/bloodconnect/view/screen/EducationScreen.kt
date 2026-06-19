package com.example.bloodconnect.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bloodconnect.view.components.ArticleCard
import com.example.bloodconnect.view.components.ErrorMessage
import com.example.bloodconnect.view.navigation.Screen
import com.example.bloodconnect.viewmodel.BloodViewModel
import com.example.bloodconnect.viewmodel.UiState
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationScreen(navController: NavController, viewModel: BloodViewModel) {
    val bloodDataState by viewModel.bloodData.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edukasi", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Red)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Cari topik edukasi...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Red,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = Color.Red,
                        focusedLabelColor = Color.Red,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { navController.navigate(Screen.EducationSteps.route) },
                    colors = CardDefaults.cardColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Langkah Donor Darah", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Pelajari 5 langkah proses sebelum melakukan donor.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Kategori",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isSemuaSelected = selectedCategoryFilter == null
                    EducationCategoryItem("Semua", Icons.AutoMirrored.Filled.List, Color.Red, isSemuaSelected) {
                        selectedCategoryFilter = null
                    }
                    val isDonorSelected = selectedCategoryFilter == "Donor"
                    EducationCategoryItem("Donor", Icons.Default.Bloodtype, Color.Red, isDonorSelected) { 
                        selectedCategoryFilter = if (isDonorSelected) null else "Donor"
                    }
                    val isTipsSelected = selectedCategoryFilter == "Tips"
                    EducationCategoryItem("Syarat & Tips", Icons.AutoMirrored.Filled.Assignment, Color.Red, isTipsSelected) { 
                        selectedCategoryFilter = if (isTipsSelected) null else "Tips"
                    }
                    val isManfaatSelected = selectedCategoryFilter == "Kesehatan"
                    EducationCategoryItem("Manfaat", Icons.Default.Favorite, Color.Red, isManfaatSelected) { 
                        selectedCategoryFilter = if (isManfaatSelected) null else "Kesehatan"
                    }
                    EducationCategoryItem("Favorit Saya", Icons.Default.Bookmark, Color.Red, false) { 
                        navController.navigate(Screen.EducationBookmark.route) 
                    }
                }
            }

            item {
                Text(
                    text = "Artikel Populer",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            when (val state = bloodDataState) {
                is UiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.Red)
                        }
                    }
                }
                is UiState.Success -> {
                    val filteredArticles = state.data.articles.filter { article ->
                        val matchesSearch = article.title.contains(searchQuery, ignoreCase = true)
                        val matchesCategory = selectedCategoryFilter == null || article.category.contains(selectedCategoryFilter!!, ignoreCase = true)
                        matchesSearch && matchesCategory
                    }
                    items(filteredArticles) { article ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            ArticleCard(article) {
                                val encodedTitle = URLEncoder.encode(article.title, StandardCharsets.UTF_8.toString())
                                val encodedUrl = URLEncoder.encode(article.imageUrl, StandardCharsets.UTF_8.toString())
                                navController.navigate(Screen.EducationDetail.createRoute(encodedTitle, encodedUrl))
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            ErrorMessage(state.message)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EducationCategoryItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(if (isSelected) color else color.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = if (isSelected) Color.White else color, modifier = Modifier.size(28.dp))
        }
        Text(
            text = label, 
            fontSize = 11.sp, 
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 6.dp), 
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
