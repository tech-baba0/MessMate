package com.messmate.android.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMealDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminMealDashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todayDate = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"))
    val isToday = selectedDate == todayDate
    val isFuture = selectedDate.isAfter(todayDate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Admin: Meals") 
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isToday) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                is AdminMealState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AdminMealState.Error -> {
                    Text(text = s.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is AdminMealState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // ── Date navigator ──────────────────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.changeDate(-1) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Day")
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isToday) "Today" else if (isFuture) "Future" else "Past",
                                    fontSize = 12.sp,
                                    color = when {
                                        isToday -> Color(0xFF10B981)
                                        isFuture -> Color(0xFF3B82F6)
                                        else -> Color.Gray
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = selectedDate.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                if (s.dashboard.totalActiveMembers > 0) {
                                    Text(
                                        text = "${s.dashboard.totalActiveMembers} active members",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.changeDate(1) }) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "Next Day",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ── Lunch / Dinner stat cards ────────────────────────────
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DashboardStatCard(
                                title = "Lunch - ${s.dashboard.lunchVotingStatus}",
                                info = "✅ YES: ${s.dashboard.todayLunchYes}  |  ❌ NO: ${s.dashboard.todayLunchNo}",
                                color = if (s.dashboard.lunchVotingStatus == "OPEN") Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardStatCard(
                                title = "Dinner - ${s.dashboard.dinnerVotingStatus}",
                                info = "✅ YES: ${s.dashboard.todayDinnerYes}  |  ❌ NO: ${s.dashboard.todayDinnerNo}",
                                color = if (s.dashboard.dinnerVotingStatus == "OPEN") Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Overall totals ───────────────────────────────────────
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TotalChip(label = "Lunch", count = s.dashboard.totalLunchMeals, color = Color(0xFF10B981))
                                TotalChip(label = "Dinner", count = s.dashboard.totalDinnerMeals, color = Color(0xFFF59E0B))
                                TotalChip(label = "Meal Units", count = s.dashboard.totalMealUnits, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Default-YES info banner ──────────────────────────────
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E40AF).copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ℹ️", fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Members who haven't voted are counted as YES by default. " +
                                    "\"Default\" badges mean the member never changed their status.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("MEMBER DETAILS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // ── Member list ──────────────────────────────────────────
                        val members = s.dashboard.memberDetails ?: emptyList()
                        if (members.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No active members found for this date.",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                items(members) { member ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(member.userName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                MealStatusChip(
                                                    label = "Lunch",
                                                    isYes = member.lunch,
                                                    isDefault = member.lunchIsDefault,
                                                    time = member.lunchUpdatedAt,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                MealStatusChip(
                                                    label = "Dinner",
                                                    isYes = member.dinner,
                                                    isDefault = member.dinnerIsDefault,
                                                    time = member.dinnerUpdatedAt,
                                                    modifier = Modifier.weight(1f)
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
    }
}

@Composable
private fun MealStatusChip(
    label: String,
    isYes: Boolean,
    isDefault: Boolean,
    time: String,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isYes && isDefault -> Color(0xFF10B981).copy(alpha = 0.12f)
        isYes -> Color(0xFF10B981).copy(alpha = 0.2f)
        else -> Color(0xFFEF4444).copy(alpha = 0.12f)
    }
    val statusText = if (isYes) "YES" else "NO"
    val badgeText = if (isDefault) "Default" else "Changed · $time"

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            "$label: $statusText",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isYes) Color(0xFF10B981) else Color(0xFFEF4444)
        )
        Text(
            badgeText,
            fontSize = 10.sp,
            color = if (isDefault) Color.Gray else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TotalChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DashboardStatCard(title: String, info: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = info, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}
