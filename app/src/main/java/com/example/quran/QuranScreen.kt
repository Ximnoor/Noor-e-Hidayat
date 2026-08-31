package com.example.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun QuranScreen() {
    val viewModel: QuranViewModel = viewModel()
    val completedSurahs by viewModel.completedSurahs.collectAsState()
    val currentlyPlayingSurah by viewModel.currentlyPlayingSurah.collectAsState()
    val currentlyPlayingAyah by viewModel.currentlyPlayingAyah.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isLoadingAudio by viewModel.isLoadingAudio.collectAsState()
    
    val totalSurahs = SurahData.surahs.size
    val completedCount = completedSurahs.size
    val progress = if (totalSurahs > 0) completedCount.toFloat() / totalSurahs else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Progress Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "QURAN COMPLETION",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$completedCount / $totalSurahs",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.background,
                )
            }
        }

        // Surah List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(SurahData.surahs) { surah ->
                val isCompleted = completedSurahs.contains(surah.number)
                SurahItem(
                    surah = surah,
                    isCompleted = isCompleted,
                    currentlyPlayingSurah = currentlyPlayingSurah,
                    currentlyPlayingAyah = currentlyPlayingAyah,
                    isPlaying = isPlaying,
                    isLoadingAudio = isLoadingAudio,
                    onToggleCompletion = {
                        viewModel.toggleSurahCompletion(surah.number, !isCompleted)
                    },
                    onPlay = { ayah ->
                        viewModel.playAudio(surah.number, ayah)
                    },
                    onDownload = { ayah ->
                        viewModel.downloadAudio(surah.number, ayah)
                    }
                )
            }
        }
    }
}

@Composable
fun SurahItem(
    surah: Surah, 
    isCompleted: Boolean,
    currentlyPlayingSurah: Int?,
    currentlyPlayingAyah: Int?,
    isPlaying: Boolean,
    isLoadingAudio: Boolean,
    onToggleCompletion: () -> Unit,
    onPlay: (Int?) -> Unit,
    onDownload: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable { expanded = !expanded }
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Number Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = surah.number.toString(),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Surah Names
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${surah.ayahs} Ayahs",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            
            // Arabic Name
            Text(
                text = surah.arabicName,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            // Checkbox/Icon
            Icon(
                imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (isCompleted) "Marked as completed" else "Mark as completed",
                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onToggleCompletion() }
            )
        }
        
        if (expanded) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            // Full Surah Row
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.SpaceBetween, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Full Surah Recitation", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Row {
                    val isPlayingThis = currentlyPlayingSurah == surah.number && currentlyPlayingAyah == null
                    IconButton(onClick = { onPlay(null) }) {
                        if (isPlayingThis && isLoadingAudio) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Icon(if (isPlayingThis && isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Play Surah")
                        }
                    }
                    IconButton(onClick = { onDownload(null) }) {
                        Icon(Icons.Filled.Download, contentDescription = "Download Surah")
                    }
                }
            }
            
            // Specific Ayah Row
            var selectedAyah by remember { mutableStateOf(1f) }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Ayah ${selectedAyah.toInt()}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.width(60.dp))
                
                Slider(
                    value = selectedAyah,
                    onValueChange = { selectedAyah = it },
                    valueRange = 1f..(surah.ayahs.toFloat()),
                    modifier = Modifier.weight(1f)
                )
                
                val isPlayingThisAyah = currentlyPlayingSurah == surah.number && currentlyPlayingAyah == selectedAyah.toInt()
                
                IconButton(onClick = { onPlay(selectedAyah.toInt()) }) {
                    if (isPlayingThisAyah && isLoadingAudio) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(if (isPlayingThisAyah && isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Play Ayah")
                    }
                }
                IconButton(onClick = { onDownload(selectedAyah.toInt()) }) {
                    Icon(Icons.Filled.Download, contentDescription = "Download Ayah")
                }
            }
        }
    }
}

