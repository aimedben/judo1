package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ClubCategory
import com.example.data.model.Coach
import com.example.data.model.CoachCategoryAssignment
import com.example.data.model.Member
import com.example.ui.theme.JudoGold
import com.example.ui.theme.JudoRed

@Composable
fun CategoriesScreen(
    categories: List<ClubCategory>,
    members: List<Member>,
    coaches: List<Coach>,
    assignments: List<CoachCategoryAssignment>,
    onSaveCategory: (ClubCategory) -> Unit,
    onDeleteCategory: (ClubCategory) -> Unit,
    onSelectCategoryForMembers: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryToEdit by remember { mutableStateOf<ClubCategory?>(null) }
    var isAddDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("categories_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDialogOpen = true },
                containerColor = JudoRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_category_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter catégorie")
            }
        }
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gestion des Catégories",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${categories.size} sections d'entraînement au dojo",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Categories List
            items(categories) { category ->
                val categoryMembers = members.filter { it.groupCode.equals(category.code, ignoreCase = true) }
                val assignedCoachIds = assignments.filter { it.categoryCode.equals(category.code, ignoreCase = true) }.map { it.coachId }
                val assignedCoaches = coaches.filter { it.id in assignedCoachIds }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_card_${category.code}")
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
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Tranche : ${category.ageRange}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { categoryToEdit = category },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modifier",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Schedule
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = JudoGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = category.schedule,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // Coaches assigned
                        if (assignedCoaches.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🥋 Encadrement :",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = assignedCoaches.joinToString(", ") { it.name.substringAfter("Sensei ") },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = JudoGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Bottom Action: count and view members
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = JudoRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${categoryMembers.size} judokas inscrits",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { onSelectCategoryForMembers(category.code) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Voir effectif", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddDialogOpen || categoryToEdit != null) {
        val initial = categoryToEdit
        var code by remember { mutableStateOf(initial?.code ?: "") }
        var label by remember { mutableStateOf(initial?.label ?: "") }
        var ageRange by remember { mutableStateOf(initial?.ageRange ?: "") }
        var schedule by remember { mutableStateOf(initial?.schedule ?: "Lundi - Mercredi 18h00") }

        Dialog(onDismissRequest = {
            isAddDialogOpen = false
            categoryToEdit = null
        }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (initial == null) "Nouvelle Catégorie" else "Modifier Catégorie",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        label = { Text("Code (ex: U15, EVEIL)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = initial == null
                    )

                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Intitulé (ex: Minimes (U15))") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ageRange,
                        onValueChange = { ageRange = it },
                        label = { Text("Tranche d'âge (ex: 12-13 ans)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = schedule,
                        onValueChange = { schedule = it },
                        label = { Text("Créneaux d'entraînement") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isAddDialogOpen = false
                                categoryToEdit = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Annuler")
                        }

                        Button(
                            onClick = {
                                if (code.isNotBlank() && label.isNotBlank()) {
                                    onSaveCategory(
                                        ClubCategory(
                                            code = code.trim(),
                                            label = label.trim(),
                                            ageRange = ageRange.trim(),
                                            schedule = schedule.trim(),
                                            isActive = true
                                        )
                                    )
                                    isAddDialogOpen = false
                                    categoryToEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JudoRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Enregistrer")
                        }
                    }
                }
            }
        }
    }
}
