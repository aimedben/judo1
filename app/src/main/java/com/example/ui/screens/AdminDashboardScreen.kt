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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AdminSettings
import com.example.data.model.Belt
import com.example.data.model.Category
import com.example.data.model.Coach
import com.example.data.model.Member
import com.example.data.model.TrainingSession
import com.example.ui.components.DouchetteTestDialog
import com.example.ui.theme.JudoBlue
import com.example.ui.theme.JudoGreen
import com.example.ui.theme.JudoOrange
import com.example.ui.theme.JudoRed
import com.example.util.CsvExporter

enum class AdminTab(val title: String) {
    OVERVIEW("Vue d'ensemble"),
    CATEGORIES("Catégories"),
    COACHES("Coachs"),
    MEMBERS("Athlètes"),
    ASSIGNMENTS("Affectations"),
    SESSIONS("Séances"),
    SETTINGS("Paramètres")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    categories: List<Category>,
    coaches: List<Coach>,
    members: List<Member>,
    sessions: List<TrainingSession>,
    adminSettings: AdminSettings?,
    onSaveCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onSaveCoach: (Coach) -> Unit,
    onDeleteCoach: (Coach) -> Unit,
    onSaveMember: (Member) -> Unit,
    onDeleteMember: (Member) -> Unit,
    onUpdateAdminPin: (String) -> Unit,
    onResetDemoData: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }
    val context = LocalContext.current
    var showDouchetteTest by remember { mutableStateOf(false) }

    // Dialog state
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    var editingCoach by remember { mutableStateOf<Coach?>(null) }
    var showCoachDialog by remember { mutableStateOf(false) }

    var newAdminPin by remember { mutableStateOf("") }
    var showPinChangeDialog by remember { mutableStateOf(false) }
    var pinMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = JudoBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Espace Administrateur",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quitter l'espace admin"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onBackClick) {
                        Text("Quitter", color = JudoRed, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable Tab Row for Admin sections
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                AdminTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == tab) JudoRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (selectedTab) {
                    AdminTab.OVERVIEW -> {
                        AdminOverviewSection(
                            categories = categories,
                            coaches = coaches,
                            members = members,
                            sessions = sessions,
                            onExportAll = {
                                CsvExporter.exportMembersToCsv(context, members)
                            }
                        )
                    }
                    AdminTab.CATEGORIES -> {
                        AdminCategoriesSection(
                            categories = categories,
                            onAddCategory = {
                                editingCategory = null
                                showCategoryDialog = true
                            },
                            onEditCategory = {
                                editingCategory = it
                                showCategoryDialog = true
                            },
                            onDeleteCategory = onDeleteCategory
                        )
                    }
                    AdminTab.COACHES -> {
                        AdminCoachesSection(
                            coaches = coaches,
                            onAddCoach = {
                                editingCoach = null
                                showCoachDialog = true
                            },
                            onEditCoach = {
                                editingCoach = it
                                showCoachDialog = true
                            },
                            onDeleteCoach = onDeleteCoach
                        )
                    }
                    AdminTab.MEMBERS -> {
                        AdminMembersSection(
                            members = members,
                            categories = categories
                        )
                    }
                    AdminTab.ASSIGNMENTS -> {
                        AdminAssignmentsSection(
                            categories = categories,
                            coaches = coaches,
                            onSaveCategory = onSaveCategory
                        )
                    }
                    AdminTab.SESSIONS -> {
                        AdminSessionsSection(sessions = sessions)
                    }
                    AdminTab.SETTINGS -> {
                        AdminSettingsSection(
                            adminSettings = adminSettings,
                            onChangePinClick = {
                                newAdminPin = ""
                                pinMessage = null
                                showPinChangeDialog = true
                            },
                            onTestDouchetteClick = { showDouchetteTest = true },
                            onResetDemoData = onResetDemoData
                        )
                    }
                }
            }
        }
    }

    // Category Edit Dialog
    if (showCategoryDialog) {
        CategoryEditDialog(
            category = editingCategory,
            onSave = { cat ->
                onSaveCategory(cat)
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }

    // Coach Edit Dialog
    if (showCoachDialog) {
        CoachEditDialog(
            coach = editingCoach,
            categories = categories,
            onSave = { c ->
                onSaveCoach(c)
                showCoachDialog = false
            },
            onDismiss = { showCoachDialog = false }
        )
    }

    // Admin PIN Change Dialog
    if (showPinChangeDialog) {
        AlertDialog(
            onDismissRequest = { showPinChangeDialog = false },
            title = { Text("Modifier le code PIN Administrateur") },
            text = {
                Column {
                    Text("Saisissez le nouveau code PIN (4 chiffres ou plus) :")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newAdminPin,
                        onValueChange = { if (it.length <= 8) newAdminPin = it },
                        label = { Text("Nouveau PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(pinMessage ?: "", color = JudoRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAdminPin.trim().length >= 4) {
                            onUpdateAdminPin(newAdminPin.trim())
                            showPinChangeDialog = false
                        } else {
                            pinMessage = "Le code PIN doit comporter au moins 4 caractères."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JudoBlue)
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinChangeDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Douchette Test Dialog
    if (showDouchetteTest) {
        DouchetteTestDialog(onDismiss = { showDouchetteTest = false })
    }
}

@Composable
private fun AdminOverviewSection(
    categories: List<Category>,
    coaches: List<Coach>,
    members: List<Member>,
    sessions: List<TrainingSession>,
    onExportAll: () -> Unit
) {
    val completedSessions = sessions.filter { it.isCompleted }
    val totalPresences = completedSessions.sumOf { it.presentCount }
    val totalExpected = completedSessions.sumOf { it.totalRegisteredAthletes }
    val globalPresencePct = if (totalExpected > 0) {
        ((totalPresences.toDouble() / totalExpected.toDouble()) * 100).toInt().coerceIn(0, 100)
    } else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 760.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Indicateurs globaux du Club",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Judokas
                AdminMetricCard(
                    title = "Judokas Actifs",
                    value = "${members.count { it.status == "ACTIF" }}",
                    color = JudoRed,
                    modifier = Modifier.weight(1f)
                )
                // Coachs Actifs
                AdminMetricCard(
                    title = "Coachs Actifs",
                    value = "${coaches.count { it.isActive }}",
                    color = JudoBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Séances
                AdminMetricCard(
                    title = "Séances Réalisées",
                    value = "${completedSessions.size}",
                    color = JudoGreen,
                    modifier = Modifier.weight(1f)
                )
                // Taux global
                AdminMetricCard(
                    title = "Taux de Présence",
                    value = "$globalPresencePct%",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Exports & Rapports",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Téléchargez les listes des adhérents et les bilans d'assiduité au format CSV pour Excel.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onExportAll,
                        colors = ButtonDefaults.buttonColors(containerColor = JudoBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exporter la liste des membres (CSV)")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun AdminCategoriesSection(
    categories: List<Category>,
    onAddCategory: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 760.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gestion des Catégories (${categories.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = onAddCategory,
                    colors = ButtonDefaults.buttonColors(containerColor = JudoRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nouvelle Catégorie")
                }
            }
        }

        items(categories, key = { it.id }) { cat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = JudoRed.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = cat.ageRange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JudoRed
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Coachs : ${cat.coach1Name} • ${cat.coach2Name}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Row {
                        IconButton(onClick = { onEditCategory(cat) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifier", tint = JudoBlue)
                        }
                        IconButton(onClick = { onDeleteCategory(cat) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer", tint = JudoRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCoachesSection(
    coaches: List<Coach>,
    onAddCoach: () -> Unit,
    onEditCoach: (Coach) -> Unit,
    onDeleteCoach: (Coach) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 760.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comptes Coachs (${coaches.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = onAddCoach,
                    colors = ButtonDefaults.buttonColors(containerColor = JudoBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nouveau Coach")
                }
            }
        }

        items(coaches, key = { it.id }) { coach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = coach.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tél: ${coach.phone} • Rôle: ${coach.role}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Affectations : ${if (coach.assignedCategoryCodes.isNotBlank()) coach.assignedCategoryCodes else "Toutes"}",
                            style = MaterialTheme.typography.labelSmall.copy(color = JudoBlue)
                        )
                    }

                    Row {
                        IconButton(onClick = { onEditCoach(coach) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifier", tint = JudoBlue)
                        }
                        IconButton(onClick = { onDeleteCoach(coach) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer", tint = JudoRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMembersSection(
    members: List<Member>,
    categories: List<Category>
) {
    var query by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("ALL") }

    val filtered = remember(members, query, selectedGroup) {
        members.filter { m ->
            (query.isBlank() || m.fullName.contains(query, ignoreCase = true) || m.qrCode.contains(query, ignoreCase = true)) &&
                (selectedGroup == "ALL" || m.groupCode.equals(selectedGroup, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 760.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Fiches Athlètes (${filtered.size} / ${members.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Rechercher un judoka ou un code...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filtered, key = { it.id }) { member ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(member.belt.primaryColor)
                                .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.firstName.take(1) + member.lastName.take(1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (member.belt.name == "BLANCHE" || member.belt.name.contains("JAUNE")) Color.Black else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = member.fullName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Code QR : ${member.qrCode} • Catégorie : ${member.groupCode}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = JudoGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = member.status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = JudoGreen, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAssignmentsSection(
    categories: List<Category>,
    coaches: List<Coach>,
    onSaveCategory: (Category) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 760.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Affectation des Coachs aux Catégories",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Chaque catégorie a deux coachs référents pour animer et pointer les entraînements.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        items(categories, key = { it.id }) { cat ->
            var coach1 by remember { mutableStateOf(cat.coach1Name) }
            var coach2 by remember { mutableStateOf(cat.coach2Name) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${cat.name} (${cat.ageRange})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = JudoRed)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = coach1,
                            onValueChange = { coach1 = it },
                            label = { Text("Coach 1") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = coach2,
                            onValueChange = { coach2 = it },
                            label = { Text("Coach 2") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onSaveCategory(cat.copy(coach1Name = coach1.trim(), coach2Name = coach2.trim()))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JudoBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Enregistrer affectation", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSessionsSection(sessions: List<TrainingSession>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 760.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Toutes les Séances (${sessions.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(sessions, key = { it.id }) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = session.categoryName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (session.isCompleted) JudoGreen.copy(alpha = 0.12f) else JudoOrange.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (session.isCompleted) "Clôturée" else "En cours",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (session.isCompleted) JudoGreen else JudoOrange
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${session.dateString} • ${session.startTime} à ${if (session.endTime.isNotBlank()) session.endTime else "..."}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Présents : ${session.presentCount} / ${session.totalRegisteredAthletes} • Coachs : ${session.presentCoachNames}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = JudoBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${session.attendancePercentage}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(color = JudoBlue, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSettingsSection(
    adminSettings: AdminSettings?,
    onChangePinClick: () -> Unit,
    onTestDouchetteClick: () -> Unit,
    onResetDemoData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Paramètres & Sécurité",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Code PIN Administrateur",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ce code protège l'accès à la gestion complète du club.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onChangePinClick,
                    colors = ButtonDefaults.buttonColors(containerColor = JudoBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Modifier le code PIN")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Douchette QR (Bluetooth / USB OTG)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Vérifiez que la douchette transmet correctement les caractères HID et la touche Entrée.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onTestDouchetteClick,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Tester la douchette QR")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Données & Réinitialisation",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Recharge les catégories et données initiales si la base est vide.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onResetDemoData,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Recharger les données initiales")
                }
            }
        }
    }
}

@Composable
private fun CategoryEditDialog(
    category: Category?,
    onSave: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var code by remember { mutableStateOf(category?.code ?: "") }
    var ageRange by remember { mutableStateOf(category?.ageRange ?: "") }
    var coach1 by remember { mutableStateOf(category?.coach1Name ?: "") }
    var coach2 by remember { mutableStateOf(category?.coach2Name ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(18.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (category == null) "Nouvelle Catégorie" else "Modifier la Catégorie",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom (ex: Minimes (U15))") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code (ex: U15)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ageRange,
                    onValueChange = { ageRange = it },
                    label = { Text("Tranche d'âge (ex: 12-13 ans)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = coach1,
                    onValueChange = { coach1 = it },
                    label = { Text("Coach Responsable 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = coach2,
                    onValueChange = { coach2 = it },
                    label = { Text("Coach Responsable 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    Category(
                                        id = category?.id ?: 0L,
                                        name = name.trim(),
                                        code = if (code.isNotBlank()) code.trim().uppercase() else name.take(4).uppercase(),
                                        ageRange = ageRange.trim(),
                                        coach1Name = coach1.trim(),
                                        coach2Name = coach2.trim()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JudoRed)
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachEditDialog(
    coach: Coach?,
    categories: List<Category>,
    onSave: (Coach) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(coach?.name ?: "") }
    var phone by remember { mutableStateOf(coach?.phone ?: "") }
    var email by remember { mutableStateOf(coach?.email ?: "") }
    var assignedCodes by remember { mutableStateOf(coach?.assignedCategoryCodes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(18.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (coach == null) "Nouveau Coach" else "Modifier le Coach",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom complet") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Facultatif)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = assignedCodes,
                    onValueChange = { assignedCodes = it },
                    label = { Text("Catégories autorisées (séparées par virgule, ex: U13,U15)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    Coach(
                                        id = coach?.id ?: 0L,
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        assignedCategoryCodes = assignedCodes.trim()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JudoBlue)
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}
