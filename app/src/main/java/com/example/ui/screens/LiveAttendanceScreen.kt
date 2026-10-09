package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.model.Member
import com.example.data.model.SessionCheckInWithMember
import com.example.data.model.TrainingSession
import com.example.data.repository.SessionCheckInResult
import com.example.ui.components.DouchetteTestDialog
import com.example.ui.theme.JudoBlue
import com.example.ui.theme.JudoGreen
import com.example.ui.theme.JudoOrange
import com.example.ui.theme.JudoRed
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

enum class LiveScanInputMode {
    DOUCHETTE, CAMERA
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveAttendanceScreen(
    session: TrainingSession,
    recentCheckIns: List<SessionCheckInWithMember>,
    registeredMembers: List<Member>,
    lastResult: SessionCheckInResult?,
    isSoundEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    onScanCode: (String, method: String) -> Unit,
    onManualCheckIn: (Long) -> Unit,
    onFinishSessionClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scanMode by remember { mutableStateOf(LiveScanInputMode.DOUCHETTE) }
    var douchetteBuffer by remember { mutableStateOf("") }
    var showTestDialog by remember { mutableStateOf(false) }
    var showManualCheckInDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Auto-focus receiver when in douchette mode
    LaunchedEffect(scanMode) {
        if (scanMode == LiveScanInputMode.DOUCHETTE) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(JudoGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pointage en direct",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = "${session.categoryName} • Début ${session.startTime}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_back_from_live_attendance")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    // Sound toggle
                    IconButton(onClick = { onToggleSound(!isSoundEnabled) }) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (isSoundEnabled) "Désactiver son" else "Activer son",
                            tint = if (isSoundEnabled) JudoBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Stats Banner (4 summary indicators)
                item {
                    LiveSessionStatsHeader(
                        presentCount = session.presentCount,
                        absentCount = session.absentCount,
                        totalAthletes = session.totalRegisteredAthletes,
                        attendancePct = session.attendancePercentage
                    )
                }

                // Mode Selector Bar (Douchette vs Caméra)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mode de lecture :",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )

                                Row {
                                    FilterChip(
                                        selected = scanMode == LiveScanInputMode.DOUCHETTE,
                                        onClick = {
                                            scanMode = LiveScanInputMode.DOUCHETTE
                                        },
                                        label = { Text("Douchette externe") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Keyboard,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = JudoRed,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("tab_mode_douchette")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilterChip(
                                        selected = scanMode == LiveScanInputMode.CAMERA,
                                        onClick = { scanMode = LiveScanInputMode.CAMERA },
                                        label = { Text("Caméra") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = JudoRed,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("tab_mode_camera")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Mode-specific instructions and status
                            if (scanMode == LiveScanInputMode.DOUCHETTE) {
                                DouchetteModeReceiverPanel(
                                    douchetteBuffer = douchetteBuffer,
                                    onBufferChange = { newText ->
                                        douchetteBuffer = newText
                                        if (newText.endsWith("\n") || newText.endsWith("\r")) {
                                            val clean = newText.trim()
                                            if (clean.isNotBlank()) {
                                                onScanCode(clean, "DOUCHETTE")
                                            }
                                            douchetteBuffer = ""
                                        }
                                    },
                                    focusRequester = focusRequester,
                                    onOpenTestDialog = { showTestDialog = true },
                                    onOpenManualCheckIn = { showManualCheckInDialog = true }
                                )
                            } else {
                                CameraModeScannerPanel(
                                    onQrScanned = { code ->
                                        onScanCode(code, "CAMERA")
                                    }
                                )
                            }
                        }
                    }
                }

                // Live Scan Feedback Banner (Success / Already checked in / Error / Wrong category)
                item {
                    LiveScanFeedbackBanner(lastResult = lastResult)
                }

                // Action Button: "Terminer la séance"
                item {
                    Button(
                        onClick = onFinishSessionClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("btn_finish_session"),
                        colors = ButtonDefaults.buttonColors(containerColor = JudoRed),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Terminer la séance & Voir le récapitulatif",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Section "Derniers pointages"
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pointages récents (${recentCheckIns.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        OutlinedButton(
                            onClick = { showManualCheckInDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pointage manuel", fontSize = 12.sp)
                        }
                    }
                }

                if (recentCheckIns.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Aucun judoka pointé pour le moment.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Scannez les cartes des judokas avec la douchette ou la caméra.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(recentCheckIns, key = { it.checkInId }) { checkIn ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Belt badge circle
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(checkIn.belt.primaryColor)
                                            .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = checkIn.firstName.take(1) + checkIn.lastName.take(1),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (checkIn.belt.name == "BLANCHE" || checkIn.belt.name.contains("JAUNE")) Color.Black else Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = checkIn.fullName,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Ceinture ${checkIn.belt.displayName} • Scan : ${checkIn.scanTime}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = if (checkIn.method == "DOUCHETTE") "Douchette" else "Caméra",
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = JudoGreen.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = JudoGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Présent",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = JudoGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Douchette Test Dialog
    if (showTestDialog) {
        DouchetteTestDialog(onDismiss = { showTestDialog = false })
    }

    // Manual Check-In Dialog (select athlete from registered list)
    if (showManualCheckInDialog) {
        ManualCheckInDialog(
            members = registeredMembers,
            alreadyCheckedInIds = recentCheckIns.map { it.memberId }.toSet(),
            onSelectMember = { memberId ->
                onManualCheckIn(memberId)
                showManualCheckInDialog = false
            },
            onDismiss = { showManualCheckInDialog = false }
        )
    }
}

@Composable
private fun LiveSessionStatsHeader(
    presentCount: Int,
    absentCount: Int,
    totalAthletes: Int,
    attendancePct: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Présents
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = JudoGreen.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$presentCount",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = JudoGreen
                    )
                )
                Text(
                    text = "Présents",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = JudoGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Absents
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = JudoRed.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$absentCount",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = JudoRed
                    )
                )
                Text(
                    text = "Absents",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = JudoRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Taux %
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = JudoBlue.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$attendancePct%",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = JudoBlue
                    )
                )
                Text(
                    text = "Présence",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = JudoBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Total
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$totalAthletes",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Inscrits",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun DouchetteModeReceiverPanel(
    douchetteBuffer: String,
    onBufferChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onOpenTestDialog: () -> Unit,
    onOpenManualCheckIn: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JudoGreen.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(JudoGreen.copy(alpha = 0.5f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(JudoGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mode douchette prêt (En attente de scan)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = JudoGreen
                        )
                    )
                }

                TextButton(
                    onClick = onOpenTestDialog,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = JudoBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tester", fontSize = 12.sp, color = JudoBlue)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Passez la carte du judoka devant la douchette Bluetooth ou USB OTG. Le pointage est instantané sans toucher l'écran.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // HID receiver field (focused to grab scanner input)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (douchetteBuffer.isEmpty()) {
                    Text(
                        text = "En attente du prochain scan...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }
                BasicTextField(
                    value = douchetteBuffer,
                    onValueChange = onBufferChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("douchette_scan_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val clean = douchetteBuffer.trim()
                            if (clean.isNotBlank()) {
                                onBufferChange(clean + "\n")
                            }
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun CameraModeScannerPanel(
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Autorisation de la caméra nécessaire",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Autoriser la caméra")
            }
        }
    } else {
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val scanner = BarcodeScanning.getClient()
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            barcode.rawValue?.let { code ->
                                                onQrScanned(code)
                                            }
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning overlay frame
            Box(
                modifier = Modifier
                    .size(160.dp, 120.dp)
                    .border(2.dp, JudoRed, RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
private fun LiveScanFeedbackBanner(lastResult: SessionCheckInResult?) {
    if (lastResult == null) return

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val (bgColor, icon, title, message) = when (lastResult) {
            is SessionCheckInResult.Success -> {
                Quadruple(
                    JudoGreen.copy(alpha = 0.15f),
                    Icons.Default.CheckCircle,
                    "Judoka pointé avec succès !",
                    "${lastResult.member.fullName} (${lastResult.member.beltName}) — Présence enregistrée à ${lastResult.checkIn.scanTime}."
                )
            }
            is SessionCheckInResult.AlreadyCheckedIn -> {
                Quadruple(
                    JudoOrange.copy(alpha = 0.15f),
                    Icons.Default.Warning,
                    "Cet athlète est déjà pointé",
                    "${lastResult.member.fullName} a déjà été validé à ${lastResult.existingCheckIn.scanTime}."
                )
            }
            is SessionCheckInResult.WrongCategory -> {
                Quadruple(
                    JudoRed.copy(alpha = 0.15f),
                    Icons.Default.Error,
                    "Catégorie non autorisée",
                    "${lastResult.member.fullName} est dans la catégorie [${lastResult.memberCategory}] alors que la séance en cours est [${lastResult.sessionCategory}]."
                )
            }
            is SessionCheckInResult.UnknownQr -> {
                Quadruple(
                    JudoRed.copy(alpha = 0.15f),
                    Icons.Default.Error,
                    "QR code inconnu",
                    "Le code '${lastResult.rawCode}' ne correspond à aucun athlète enregistré."
                )
            }
            is SessionCheckInResult.Error -> {
                Quadruple(
                    JudoRed.copy(alpha = 0.15f),
                    Icons.Default.Error,
                    "Erreur de pointage",
                    lastResult.message
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("live_scan_feedback_banner"),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = when (lastResult) {
                        is SessionCheckInResult.Success -> JudoGreen
                        is SessionCheckInResult.AlreadyCheckedIn -> JudoOrange
                        else -> JudoRed
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun ManualCheckInDialog(
    members: List<Member>,
    alreadyCheckedInIds: Set<Long>,
    onSelectMember: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(members, query) {
        members.filter {
            it.fullName.contains(query, ignoreCase = true) ||
                it.qrCode.contains(query, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(480.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Pointage manuel d'un athlète",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text("Rechercher par nom...", color = Color.Gray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { member ->
                        val isAlreadyCheckedIn = alreadyCheckedInIds.contains(member.id)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAlreadyCheckedIn) JudoGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isAlreadyCheckedIn) {
                                    onSelectMember(member.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = member.fullName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${member.groupCode} • ${member.beltName}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                if (isAlreadyCheckedIn) {
                                    Text(
                                        text = "Déjà pointé ✓",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = JudoGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                } else {
                                    Text(
                                        text = "Pointer +",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = JudoBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}
