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
    
    // Derived local states for switches
    var lunchSelected by remember { mutableStateOf(false) }
    var dinnerSelected by remember { mutableStateOf(false) }
    
    LaunchedEffect(state) {
        if (state is MealState.Success) {
            val s = state as MealState.Success
            lunchSelected = s.lunchActive
            dinnerSelected = s.dinnerActive
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
                        // Horizontal Date Selector
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
                                        .clickable { viewModel.selectDate(futureMeal.date) }
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

                        // Meal Selection Box (Depends on selected Date vs deadlines)
                        val todayStr = dates.firstOrNull()?.date ?: ""
                        val isPast = !dates.any { it.date == s.selectedDateStr } // if it's not in future list, it's past
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
                                Text("Meal Plan for ${s.selectedDateStr}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Lunch Row
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Lunch", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                        Text(
                                            if (isLunchLocked) "LOCKED" else "Closes at ${s.dashboardData.lunchVotingDeadline}",
                                            fontSize = 12.sp,
                                            color = if (isLunchLocked) Color.Red else Color(0xFFF59E0B),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Switch(
                                        checked = lunchSelected,
                                        onCheckedChange = { lunchSelected = it },
                                        enabled = !s.isSaving && !isLunchLocked
                                    )
                                }
                                
                                // Dinner Row
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Dinner", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                        Text(
                                            if (isDinnerLocked) "LOCKED" else "Closes at ${s.dashboardData.dinnerVotingDeadline}",
                                            fontSize = 12.sp,
                                            color = if (isDinnerLocked) Color.Red else Color(0xFFF59E0B),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Switch(
                                        checked = dinnerSelected,
                                        onCheckedChange = { dinnerSelected = it },
                                        enabled = !s.isSaving && !isDinnerLocked
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Button(
                                    onClick = { viewModel.updateMeals(lunchSelected, dinnerSelected) },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !s.isSaving && (!isLunchLocked || !isDinnerLocked)
                                ) {
                                    if (s.isSaving) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text("Save Selection", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                if (s.saveSuccess) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Meal saved successfully", 
                                        color = Color(0xFF10B981), 
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }

                        // Summary & History Section
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
                                Text(if (meal.lunch) "L: YES" else "L: NO", fontSize = 12.sp, color = if (meal.lunch) Color(0xFF10B981) else Color.Gray, modifier = Modifier.padding(end = 8.dp))
                                Text(if (meal.dinner) "D: YES" else "D: NO", fontSize = 12.sp, color = if (meal.dinner) Color(0xFFF59E0B) else Color.Gray)
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
    try {
        val st = serverTime.take(5) // Just compare HH:mm
        return st > deadline
    } catch (e: Exception) {
        return false
    }
}
