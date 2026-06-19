package com.example.bloodconnect.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationStepsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Langkah Donor Darah", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Langkah demi Langkah", 
                fontSize = 22.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Pahami tahapan proses donor darah.", 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            DonorStep(1, "Pendaftaran", "Isi formulir pendaftaran dengan data diri lengkap.")
            DonorStep(2, "Pemeriksaan Kesehatan", "Tim medis akan mengecek tekanan darah dan HB Anda.")
            DonorStep(3, "Pengambilan Darah", "Proses pengambilan darah sekitar 10-15 menit.")
            DonorStep(4, "Istirahat", "Istirahat sejenak sambil menikmati makanan ringan.")
            DonorStep(5, "Selesai", "Anda telah membantu menyelamatkan nyawa!")
        }
    }
}

@Composable
fun DonorStep(number: Int, title: String, description: String) {
    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        Box(
            modifier = Modifier.size(36.dp).background(Color.Red, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number.toString(), color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title, 
                fontWeight = FontWeight.Bold, 
                fontSize = 16.sp, 
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
