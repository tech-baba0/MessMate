package com.messmate.android.ui.screens.meal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealSelectionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: MealViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Derived local states — updated from server state when date changes
    var lunchSelected by remember { mutableStateOf(true) }
    var dinnerSelected by remember { mutableStateOf(true) }
    var pendingChange by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is MealState.Success) {
            val s = state as MealState.Success
            lunchSelected = s.lunchActive
            dinnerSelected = s.dinnerActive
            pendingChange = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Dashboard", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF047857))))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val s = state) {
                is MealState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MealState.Error -> {
                    Text(s.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is MealState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ── Date selector strip ───────────────────────────────────
                        val dates = s.dashboardData.futureSelections
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dates.forEach { futureMeal ->
                                val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(futureMeal.date)
                                val dayName = dateObj?.let { SimpleDateFormat("EEE", Locale.getDefault()).format(it) } ?: ""
                                val dayNum = dateObj?.let { SimpleDateFormat("d", Locale.getDefault()).format(it) } ?: ""

                                val isSelected = s.selectedDateStr == futureMeal.date
                                val bgColor = if (isSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant
                                val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                                Column(
                                    modifier = Modifier
                                        .size(60.dp, 75.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor)
                                        .clickable {
                                            viewModel.selectDate(futureMeal.date)
                                        }
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(dayName.uppercase(), fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(dayNum, fontSize = 20.sp, color = textColor, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        // ── Meal selection card ──────────────────────────────────
                        val todayStr = dates.firstOrNull()?.date ?: ""
                        val isPast = !dates.any { it.date == s.selectedDateStr }
                        val isToday = s.selectedDateStr == todayStr

                        val isLunchLocked = isPast || (isToday && isTimePassed(s.dashboardData.currentServerTime, s.dashboardData.lunchVotingDeadline))
                        val isDinnerLocked = isPast || (isToday && isTimePassed(s.dashboardData.currentServerTime, s.dashboardData.dinnerVotingDeadline))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Meal Plan for ${s.selectedDateStr}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // Saving indicator
                                    if (s.isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color(0xFF10B981)
                                        )
                                    } else if (s.saveSuccess) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Saved",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (isPast) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "⚠️ Past date — voting is closed.",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // ── Lunch row ─────────────────────────────────────
                                MealToggleRow(
                                    label = "Lunch",
                                    checked = lunchSelected,
                                    locked = isLunchLocked,
                                    saving = s.isSaving,
                                    deadlineText = if (isLunchLocked) "LOCKED" else "Closes at ${s.dashboardData.lunchVotingDeadline}",
                                    onCheckedChange = { newVal ->
                                        lunchSelected = newVal
                                        pendingChange = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // ── Dinner row ────────────────────────────────────
                                MealToggleRow(
                                    label = "Dinner",
                                    checked = dinnerSelected,
                                    locked = isDinnerLocked,
                                    saving = s.isSaving,
                                    deadlineText = if (isDinnerLocked) "LOCKED" else "Closes at ${s.dashboardData.dinnerVotingDeadline}",
                                    onCheckedChange = { newVal ->
                                        dinnerSelected = newVal
                                        pendingChange = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // ── Save button (only shown when there's a pending change) ─
                                val canSave = !s.isSaving && (!isLunchLocked || !isDinnerLocked) && pendingChange
                                if (pendingChange || s.isSaving) {
                                    Button(
                                        onClick = { viewModel.updateMeals(lunchSelected, dinnerSelected) },
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = canSave,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF10B981)
                                        )
                                    ) {
                                        if (s.isSaving) {
                                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                if (s.saveSuccess) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "✅ Meal saved successfully",
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }

                        // ── Default-YES info banner ───────────────────────────────
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E40AF).copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ℹ️", fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Your default is YES for all meals. Only save if you want to change a day — " +
                                    "admin sees your vote the moment you save.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // ── This month summary ────────────────────────────────────
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "This Month Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryCard(title = "Total", count = s.dashboardData.currentMonthTotalMeals, color = Color(0xFF3B82F6), modifier = Modifier.weight(1f))
                            SummaryCard(title = "Lunch", count = s.dashboardData.currentMonthLunchCount, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                            SummaryCard(title = "Dinner", count = s.dashboardData.currentMonthDinnerCount, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Recent History",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        s.dashboardData.recentHistory.forEach { meal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(meal.date, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(if (meal.lunch) "L: ✅" else "L: ❌", fontSize = 13.sp, color = if (meal.lunch) Color(0xFF10B981) else Color.Gray, modifier = Modifier.padding(end = 8.dp))
                                Text(if (meal.dinner) "D: ✅" else "D: ❌", fontSize = 13.sp, color = if (meal.dinner) Color(0xFFF59E0B) else Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 32.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Full History", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealToggleRow(
    label: String,
    checked: Boolean,
    locked: Boolean,
    saving: Boolean,
    deadlineText: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    locked -> Color.Gray.copy(alpha = 0.06f)
                    checked -> Color(0xFF10B981).copy(alpha = 0.08f)
                    else -> Color(0xFFEF4444).copy(alpha = 0.06f)
                }
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(
                deadlineText,
                fontSize = 11.sp,
                color = if (locked) Color.Red else Color(0xFFF59E0B),
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            if (checked) "YES" else "NO",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (checked) Color(0xFF10B981) else Color.Gray,
            modifier = Modifier.padding(end = 8.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = !saving && !locked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF10B981),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFEF4444)
            )
        )
    }
}

@Composable
fun SummaryCard(title: String, count: Int, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(count.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Basic string time comparison (e.g. "14:30:22.569" vs "10:00")
fun isTimePassed(serverTime: String, deadline: String): Boolean {
    return try {
        serverTime.take(5) > deadline
    } catch (e: Exception) {
        false
    }
}
