package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceWithMember
import com.example.data.model.ClubCategory
import com.example.data.model.Coach
import com.example.data.model.Member
import com.example.data.model.TrainingSession
import com.example.ui.ActiveSessionState
import com.example.ui.components.BeltBadge
import com.example.ui.components.JudokaAvatar
import com.example.ui.theme.JudoGold
import com.example.ui.theme.JudoRed
import com.example.ui.theme.JudoSuccess
import com.example.ui.theme.JudoWarning
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CoachSessionPreparationScreen(
    category: ClubCategory,
    coaches: List<Coach>,
    onStartSession: (categoryCode: String, categoryLabel: String, presentCoaches: List<String>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDateStr = remember {
        SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(Date()).replaceFirstChar { it.uppercase() }
    }
    val currentTimeStr = remember {
        SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date())
    }

    // Default select active coaches
    var selectedCoachNames by remember {
        mutableStateOf(coaches.filter { it.isActive }.take(2).map { it.name }.toSet())
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("session_prep_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }

                    Column {
                        Text(
                            text = "Préparation de Séance",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Vérification des paramètres d'entraînement",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Category Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = JudoRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = category.code,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = JudoRed,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                            Column {
                                Text(
                                    text = category.label,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Tranche d'âge : ${category.ageRange}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📅 $currentDateStr", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                Text(text = "🕒 Début : $currentTimeStr", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = JudoRed))
                            }
                        }
                    }
                }
            }

            // Select Coaches Present on Tatami
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🥋 Coachs présents sur le tatami aujourd'hui :",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        coaches.forEach { coach ->
                            val isSelected = selectedCoachNames.contains(coach.name)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) JudoRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCoachNames = if (isSelected) {
                                            selectedCoachNames - coach.name
                                        } else {
                                            selectedCoachNames + coach.name
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = coach.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                        Text(
                                            text = "${coach.danGrade} • ${coach.title}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }

                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedCoachNames = if (checked) {
                                                selectedCoachNames + coach.name
                                            } else {
                                                selectedCoachNames - coach.name
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = JudoRed,
                                            checkmarkColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Start Session Button
            item {
                Button(
                    onClick = {
                        val coachesList = if (selectedCoachNames.isEmpty()) {
                            listOf(coaches.firstOrNull()?.name ?: "Sensei")
                        } else {
                            selectedCoachNames.toList()
                        }
                        onStartSession(category.code, category.label, coachesList)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JudoRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_session_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null)
                        Text(
                            text = "DÉMARRER LA SÉANCE",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoachLiveSessionScreen(
    sessionState: ActiveSessionState,
    allMembers: List<Member>,
    onQrScanned: (String) -> Unit,
    onManualCheckIn: (Long) -> Unit,
    onOpenScannerCamera: () -> Unit,
    onFinishSession: () -> Unit,
    onCancelSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    var manualBarcodeInput by remember { mutableStateOf("") }
    var elapsedTimeStr by remember { mutableStateOf("00:00") }

    LaunchedEffect(sessionState.startTimestamp) {
        while (true) {
            val elapsedSec = (System.currentTimeMillis() - sessionState.startTimestamp) / 1000
            val mins = elapsedSec / 60
            val secs = elapsedSec % 60
            elapsedTimeStr = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
            delay(1000)
        }
    }

    val categoryMembers = allMembers.filter {
        it.groupCode.equals(sessionState.categoryCode, ignoreCase = true) && it.status == "ACTIF"
    }
    val presentCount = sessionState.checkedInMemberIds.size
    val totalCount = categoryMembers.size.coerceAtLeast(presentCount)
    val rate = if (totalCount > 0) ((presentCount.toDouble() / totalCount.toDouble()) * 100).toInt() else 0

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("live_session_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Status Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(JudoSuccess)
                                )
                                Text(
                                    text = "SÉANCE EN COURS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = JudoSuccess,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = JudoRed.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = JudoRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = elapsedTimeStr,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = JudoRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${sessionState.categoryLabel} (${sessionState.categoryCode})",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = "🥋 Coachs : ${sessionState.presentCoachNames.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        // KPI row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "Présents", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    Text(text = "$presentCount", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = JudoSuccess))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "Total Groupe", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    Text(text = "$totalCount", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "Taux", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    Text(text = "$rate%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = JudoGold))
                                }
                            }
                        }
                    }
                }
            }

            // External Douchette / Manual Input Bar
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = JudoGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Douchette externe (Bluetooth / USB HID) & Saisie",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = manualBarcodeInput,
                                onValueChange = { manualBarcodeInput = it },
                                placeholder = { Text("Code QR / Numéro de carte...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Button(
                                onClick = {
                                    if (manualBarcodeInput.isNotBlank()) {
                                        onQrScanned(manualBarcodeInput.trim())
                                        manualBarcodeInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JudoRed)
                            ) {
                                Text("Valider")
                            }
                        }

                        // Camera Scan Button
                        OutlinedButton(
                            onClick = onOpenScannerCamera,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = JudoRed)
                                Text("Ouvrir la caméra de pointage", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // Athlete Check-in List for this Category
            item {
                Text(
                    text = "Judokas de la catégorie (${categoryMembers.size}) :",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(categoryMembers) { member ->
                val isChecked = sessionState.checkedInMemberIds.contains(member.id)
                val time = sessionState.checkInTimes[member.id]

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isChecked) JudoSuccess.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                    border = if (isChecked) CardDefaults.outlinedCardBorder() else CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isChecked) {
                                onManualCheckIn(member.id)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        JudokaAvatar(
                            name = member.fullName,
                            belt = member.belt,
                            photoUrl = member.photoUrl,
                            sizeDp = 44
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.fullName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BeltBadge(belt = member.belt)
                                Text(
                                    text = member.phone,
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        if (isChecked) {
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = JudoSuccess.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = JudoSuccess,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Présent",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = JudoSuccess,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                if (time != null) {
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onManualCheckIn(member.id) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+ Pointer", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // End Session Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onFinishSession,
                    colors = ButtonDefaults.buttonColors(containerColor = JudoRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("finish_session_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.StopCircle, contentDescription = null)
                        Text(
                            text = "TERMINER LA SÉANCE",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ÉCRAN DE FIN DE SÉANCE PROFESSIONNEL AVEC RÉCAPITULATIF EXACT
// -------------------------------------------------------------

@Composable
fun CompletedSessionSummaryScreen(
    session: TrainingSession,
    allMembers: List<Member>,
    todayAttendance: List<AttendanceWithMember>,
    onSaveAndClose: (notes: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notes by remember { mutableStateOf(session.notes) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Présents, 1: Absents

    val categoryMembers = remember(allMembers, session.categoryCode) {
        allMembers.filter { it.groupCode.equals(session.categoryCode, ignoreCase = true) }
    }

    val presentMembers = remember(categoryMembers, todayAttendance, session) {
        val todayIds = todayAttendance.filter { it.groupCode.equals(session.categoryCode, ignoreCase = true) }.map { it.memberId }.toSet()
        categoryMembers.filter { it.id in todayIds }
    }

    val absentMembers = remember(categoryMembers, presentMembers) {
        categoryMembers.filter { it !in presentMembers }
    }

    val attendanceRate = if (categoryMembers.isNotEmpty()) {
        ((presentMembers.size.toDouble() / categoryMembers.size.toDouble()) * 100).toInt()
    } else 0

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("session_summary_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }

                    Column {
                        Text(
                            text = "Récapitulatif de Séance",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Judo Seddouk • Clôture d'entraînement",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Session Stats Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Section : ${session.categoryCode}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = JudoRed
                                )
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = JudoSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Terminée ✓",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = JudoSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Text(
                            text = "📅 Date : ${session.dateString}  •  🕒 ${session.startTime} à ${session.endTime} (${session.durationMinutes} min)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )

                        Text(
                            text = "🥋 Encadrants : ${session.presentCoachNames}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        // Big Stats Summary Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = JudoSuccess.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Présents", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    Text("${presentMembers.size}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = JudoSuccess))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Absents", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    Text("${absentMembers.size}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = JudoGold.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Taux", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    Text("$attendanceRate%", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = JudoGold))
                                }
                            }
                        }
                    }
                }
            }

            // Tab bar: Présents vs Absents
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = JudoRed
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Présents (${presentMembers.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Absents (${absentMembers.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Member list based on selected tab
            val displayList = if (selectedTab == 0) presentMembers else absentMembers

            if (displayList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == 0) "Aucun présent enregistré" else "Aucun absent ! 100% de présence",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                items(displayList) { member ->
                    val att = todayAttendance.find { it.memberId == member.id }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            JudokaAvatar(
                                name = member.fullName,
                                belt = member.belt,
                                photoUrl = member.photoUrl,
                                sizeDp = 42
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.fullName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                BeltBadge(belt = member.belt)
                            }

                            if (selectedTab == 0) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = JudoSuccess.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Pointé ${att?.timeString ?: ""}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = JudoSuccess,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Absent",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Training Observations / Notes field
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observations & Bilan pédagogique (optionnel)") },
                    placeholder = { Text("Ex: Bon engagement au sol, perfectionnement o-soto-gari...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
            }

            // Final Save Button
            item {
                Button(
                    onClick = { onSaveAndClose(notes) },
                    colors = ButtonDefaults.buttonColors(containerColor = JudoRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_and_close_session_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Text(
                            text = "ENREGISTRER DÉFINITIVEMENT LA SÉANCE",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}
