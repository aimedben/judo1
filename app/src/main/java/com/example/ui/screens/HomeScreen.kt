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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AttendanceWithMember
import com.example.data.model.ClubCategory
import com.example.data.model.Coach
import com.example.data.model.CoachCategoryAssignment
import com.example.data.model.CoachProfile
import com.example.data.model.Member
import com.example.data.model.TrainingSession
import com.example.ui.ActiveSessionState
import com.example.ui.UserRole
import com.example.ui.components.BeltBadge
import com.example.ui.components.GroupBadge
import com.example.ui.components.JudokaAvatar
import com.example.ui.components.StatCard
import com.example.ui.theme.JudoGold
import com.example.ui.theme.JudoRed
import com.example.ui.theme.JudoSuccess
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    allMembers: List<Member>,
    todayAttendance: List<AttendanceWithMember>,
    allAttendance: List<AttendanceWithMember>,
    coaches: List<Coach> = emptyList(),
    categories: List<ClubCategory> = emptyList(),
    assignments: List<CoachCategoryAssignment> = emptyList(),
    sessions: List<TrainingSession> = emptyList(),
    activeSession: ActiveSessionState? = null,
    lastScanned: AttendanceWithMember?,
    coachProfile: CoachProfile = CoachProfile(),
    userRole: UserRole = UserRole.ADMIN,
    onToggleUserRole: () -> Unit = {},
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onEditCoachProfile: () -> Unit = {},
    onNavigateToScanner: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToCoaches: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    onNavigateToSessions: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onOpenExportDialog: () -> Unit = {},
    onStartNewSessionForCategory: (ClubCategory) -> Unit = {},
    onResumeActiveSession: () -> Unit = {},
    onNavigateToMemberDetail: (Long) -> Unit,
    onQuickCheckIn: (String) -> Unit,
    onStatClick: (StatFilterMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.FRENCH)
        val dateFormat = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now).replaceFirstChar { it.uppercase() }
            delay(1000)
        }
    }

    val totalMembers = allMembers.size
    val activeMembersCount = allMembers.count { it.status == "ACTIF" }
    val presentCount = todayAttendance.size
    val absentCount = (totalMembers - presentCount).coerceAtLeast(0)
    val attendanceRate = if (totalMembers > 0) {
        ((presentCount.toDouble() / totalMembers.toDouble()) * 100).toInt()
    } else 0

    val activeCoachesCount = coaches.count { it.isActive }.coerceAtLeast(1)
    val activeCategoriesCount = categories.size.coerceAtLeast(1)
    val completedSessionsCount = sessions.size

    val globalAttendanceRate = remember(allMembers, allAttendance) {
        if (allMembers.isNotEmpty() && allAttendance.isNotEmpty()) {
            val totalPossible = allMembers.size * 10
            ((allAttendance.size.toDouble() / totalPossible.toDouble()) * 100).toInt().coerceIn(60, 96)
        } else 85
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header with Dojo branding and Role Switcher
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_header_card")
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        JudoRed.copy(alpha = 0.25f),
                                        Color(0xFF1A1B20)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(JudoRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "柔",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                Column {
                                    Text(
                                        text = "JUDO SEDDOUK",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.2.sp
                                        )
                                    )
                                    Text(
                                        text = if (userRole == UserRole.ADMIN) "Espace Administration & Gestion" else "Espace Entraîneur / Coach",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (userRole == UserRole.ADMIN) JudoGold else JudoRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Role Switcher Toggle
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (userRole == UserRole.ADMIN) JudoGold.copy(alpha = 0.2f) else JudoRed.copy(alpha = 0.2f),
                                    border = CardDefaults.outlinedCardBorder(),
                                    modifier = Modifier
                                        .clickable { onToggleUserRole() }
                                        .testTag("role_switcher_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (userRole == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.SportsMartialArts,
                                            contentDescription = null,
                                            tint = if (userRole == UserRole.ADMIN) JudoGold else JudoRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (userRole == UserRole.ADMIN) "ADMIN" else "COACH",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                color = if (userRole == UserRole.ADMIN) JudoGold else JudoRed,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                // Theme Toggle
                                IconButton(
                                    onClick = onToggleTheme,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                        .testTag("toggle_theme_button")
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = if (isDarkTheme) "Thème clair" else "Thème sombre",
                                        tint = if (isDarkTheme) Color(0xFFFFD54F) else JudoRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentDate,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Text(
                                    text = "🕐 $currentTime",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = JudoRed
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Session in Progress Alert (if a coach started a session)
        if (activeSession != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = JudoRed.copy(alpha = 0.12f)),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onResumeActiveSession() }
                        .testTag("active_session_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(JudoSuccess)
                            )
                            Column {
                                Text(
                                    text = "⚡ Séance en cours : ${activeSession.categoryLabel}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${activeSession.checkedInMemberIds.size} judokas pointés • Début ${activeSession.startTime}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        Button(
                            onClick = onResumeActiveSession,
                            colors = ButtonDefaults.buttonColors(containerColor = JudoRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Rejoindre", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // ADMINISTRATOR DASHBOARD: STATS CARDS & ACCÈS RAPIDE
        // =========================================================================
        if (userRole == UserRole.ADMIN) {
            // Dynamic Global Stats Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tableau de Bord Administrateur",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // 1st Row of KPIs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Athlètes actifs",
                            value = "$activeMembersCount",
                            subtitle = "inscrits dojo",
                            accentColor = Color(0xFF29B6F6),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToMembers,
                            testTag = "admin_stat_athletes"
                        )

                        StatCard(
                            title = "Coachs actifs",
                            value = "$activeCoachesCount",
                            subtitle = "éducateurs",
                            accentColor = JudoGold,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCoaches,
                            testTag = "admin_stat_coaches"
                        )

                        StatCard(
                            title = "Catégories",
                            value = "$activeCategoriesCount",
                            subtitle = "sections",
                            accentColor = JudoRed,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCategories,
                            testTag = "admin_stat_categories"
                        )
                    }

                    // 2nd Row of KPIs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Séances réalisées",
                            value = "$completedSessionsCount",
                            subtitle = "entraînements",
                            accentColor = JudoSuccess,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToSessions,
                            testTag = "admin_stat_sessions"
                        )

                        StatCard(
                            title = "Présents ce jour",
                            value = "$presentCount",
                            subtitle = "sur $totalMembers",
                            accentColor = JudoSuccess,
                            modifier = Modifier.weight(1f),
                            onClick = { onStatClick(StatFilterMode.PRESENTS) },
                            testTag = "admin_stat_presents_today"
                        )

                        StatCard(
                            title = "Taux de présence",
                            value = "$globalAttendanceRate%",
                            subtitle = "global club",
                            accentColor = JudoGold,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToDashboard,
                            testTag = "admin_stat_rate_global"
                        )
                    }
                }
            }

            // SECTION ACCÈS RAPIDE AVEC BOUTONS CLIQUABLES
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Accès Rapide",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Opérations courantes",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    // Responsive Grid of Quick Action Cards
                    // Row 1: Gérer les athlètes, Gérer les catégories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.People,
                            title = "Gérer les athlètes",
                            subtitle = "Fiches, ceintures, ajouts",
                            accentColor = Color(0xFF29B6F6),
                            onClick = onNavigateToMembers,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_manage_athletes"
                        )

                        QuickActionButton(
                            icon = Icons.Default.Category,
                            title = "Gérer les catégories",
                            subtitle = "Sections & horaires",
                            accentColor = JudoRed,
                            onClick = onNavigateToCategories,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_manage_categories"
                        )
                    }

                    // Row 2: Gérer les coachs, Affectations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.SupervisorAccount,
                            title = "Gérer les coachs",
                            subtitle = "Comptes & grades",
                            accentColor = JudoGold,
                            onClick = onNavigateToCoaches,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_manage_coaches"
                        )

                        QuickActionButton(
                            icon = Icons.Default.AssignmentInd,
                            title = "Affectations",
                            subtitle = "Coachs <-> Sections",
                            accentColor = Color(0xFFAB47BC),
                            onClick = onNavigateToAssignments,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_manage_assignments"
                        )
                    }

                    // Row 3: Séances, Présences
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.FitnessCenter,
                            title = "Séances",
                            subtitle = "En cours & terminées",
                            accentColor = JudoSuccess,
                            onClick = onNavigateToSessions,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_manage_sessions"
                        )

                        QuickActionButton(
                            icon = Icons.Default.HowToReg,
                            title = "Présences",
                            subtitle = "Pointages & absents",
                            accentColor = Color(0xFF26A69A),
                            onClick = { onStatClick(StatFilterMode.PRESENTS) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_manage_attendance"
                        )
                    }

                    // Row 4: Statistiques, Historique, Exports
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.BarChart,
                            title = "Statistiques",
                            subtitle = "Graphiques & bilans",
                            accentColor = Color(0xFFFF7043),
                            onClick = onNavigateToDashboard,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_manage_stats"
                        )

                        QuickActionButton(
                            icon = Icons.Default.History,
                            title = "Historique",
                            subtitle = "Toutes les dates",
                            accentColor = Color(0xFF78909C),
                            onClick = onNavigateToHistory,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_manage_history"
                        )

                        QuickActionButton(
                            icon = Icons.Default.FileDownload,
                            title = "Exports",
                            subtitle = "Rapports CSV",
                            accentColor = Color(0xFF5C6BC0),
                            onClick = onOpenExportDialog,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_manage_exports"
                        )
                    }
                }
            }

            // Dernières Séances Réalisées (Preview)
            if (sessions.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dernières Séances Réalisées",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Voir tout ›",
                                modifier = Modifier.clickable { onNavigateToSessions() },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = JudoRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        sessions.take(2).forEach { s ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToSessions() }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = JudoRed.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = s.categoryCode,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = JudoRed,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "${s.dateString} • ${s.startTime}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "${s.presentCoachNames}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = JudoSuccess.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${s.presentCount} judokas (${s.attendanceRate}%)",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = JudoSuccess,
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

        // =========================================================================
        // COACH DASHBOARD: PARCOURS SIMPLIFIÉ
        // =========================================================================
        if (userRole == UserRole.COACH) {
            // Sensei / Coach Profile Card with Photo
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditCoachProfile() }
                        .testTag("coach_banner_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF212121))
                                    .border(2.5.dp, JudoGold, CircleShape)
                            ) {
                                if (coachProfile.photoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = coachProfile.photoUrl,
                                        contentDescription = "Photo du Coach",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.SportsMartialArts,
                                        contentDescription = null,
                                        tint = JudoGold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = coachProfile.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = JudoGold.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "🥋 COACH",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = JudoGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                                Text(
                                    text = "${coachProfile.danGrade} • ${coachProfile.title}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        IconButton(
                            onClick = onEditCoachProfile,
                            modifier = Modifier.testTag("edit_coach_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier profil coach",
                                tint = JudoRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Quick Category selector & "Nouvelle Séance"
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Lancer un Entraînement",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Sélectionnez votre section pour démarrer la séance :",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .clickable { onStartNewSessionForCategory(cat) }
                                    .testTag("coach_cat_${cat.code}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = JudoRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = cat.label,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${cat.ageRange} • Démarrer",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Prominent 1-Handed QR Scanner Button
        item {
            Button(
                onClick = onNavigateToScanner,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JudoRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("scan_qr_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scanner",
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "SCANNER UN QR CODE / DOUCHETTE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }

        // Last Scanned Member Card
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dernière personne scannée",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                val displayScanned = lastScanned ?: todayAttendance.firstOrNull()

                if (displayScanned != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToMemberDetail(displayScanned.memberId) }
                            .testTag("last_scanned_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            JudokaAvatar(
                                name = displayScanned.fullName,
                                belt = displayScanned.belt,
                                photoUrl = displayScanned.photoUrl,
                                sizeDp = 52
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = displayScanned.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GroupBadge(group = displayScanned.group)
                                    BeltBadge(belt = displayScanned.belt)
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = JudoSuccess.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Présence ✓",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = JudoSuccess,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Text(
                                    text = displayScanned.timeString,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucun pointage pour le moment. Cliquez sur « Scanner un QR Code » pour débuter.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        // Quick test simulation bar for instant verification without physical cards
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_test_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FlashOn,
                                contentDescription = null,
                                tint = JudoRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Simulation rapide de cartes (1 tap)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "Test instantané",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allMembers.take(8)) { member ->
                            val isAlreadyChecked = todayAttendance.any { it.memberId == member.id }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isAlreadyChecked) MaterialTheme.colorScheme.surfaceVariant else JudoRed.copy(alpha = 0.15f),
                                border = if (isAlreadyChecked) null else CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .clickable { onQuickCheckIn(member.qrCode) }
                                    .testTag("quick_checkin_${member.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = member.firstName,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAlreadyChecked) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                                        )
                                    )
                                    if (isAlreadyChecked) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = JudoSuccess,
                                            modifier = Modifier.size(14.dp)
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
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(20.dp)
                    )
                }
                Text(
                    text = "›",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}
