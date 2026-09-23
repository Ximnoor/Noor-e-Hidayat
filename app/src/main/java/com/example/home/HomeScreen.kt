package com.example.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.essentials.DailyDuaCard

@Composable
fun HomeScreen(onNavigate: (String) -> Unit = {}) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("السلام علیکم", style = MaterialTheme.typography.headlineLarge) }
        item { Text("Noor-e-Hidayat", style = MaterialTheme.typography.titleLarge) }
        item { DailyDuaCard() }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onNavigate("quran") }, modifier = Modifier.weight(1f)) { Text("قرآن و تفسیر") }
                Button(onClick = { onNavigate("prayer") }, modifier = Modifier.weight(1f)) { Text("اوقات و قبلہ") }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onNavigate("masnoon") }, modifier = Modifier.weight(1f)) { Text("مسنون دعائیں") }
                OutlinedButton(onClick = { onNavigate("stories") }, modifier = Modifier.weight(1f)) { Text("قصص الانبیاء") }
            }
        }
        item { Text("Choose your city in Prayer to set local timings and Azan alerts. Quran reading, recitation and tafseer need internet. Daily duas and story summaries work offline.") }
    }
}
