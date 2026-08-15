package com.messmate.android.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Admin: Today's Meals") 
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TODAY'S AGGREGATION", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DashboardStatCard(
                                title = "Lunch - ${s.dashboard.lunchVotingStatus}",
                                info = "YES: ${s.dashboard.todayLunchYes} | NO: ${s.dashboard.todayLunchNo}",
                                color = if (s.dashboard.lunchVotingStatus == "OPEN") Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardStatCard(
                                title = "Dinner - ${s.dashboard.dinnerVotingStatus}",
                                info = "YES: ${s.dashboard.todayDinnerYes} | NO: ${s.dashboard.todayDinnerNo}",
                                color = if (s.dashboard.dinnerVotingStatus == "OPEN") Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("OVERALL", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Total Lunch Meals: ${s.dashboard.totalLunchMeals}")
                                Text("Total Dinner Meals: ${s.dashboard.totalDinnerMeals}")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Total Meal Units: ${s.dashboard.totalMealUnits}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("MEMBER DETAILS", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val members = s.dashboard.memberDetails ?: emptyList()
                        if (members.isEmpty()) {
                            Text("No users have saved their meals today yet.", color = Color.Gray)
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                items(members) { member ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(member.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Lunch: ${if (member.lunch) "YES" else "NO"} (${member.lunchUpdatedAt})", fontSize = 12.sp, color = if (member.lunch) Color(0xFF10B981) else Color.Gray)
                                                Text("Dinner: ${if (member.dinner) "YES" else "NO"} (${member.dinnerUpdatedAt})", fontSize = 12.sp, color = if (member.dinner) Color(0xFF10B981) else Color.Gray)
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
fun DashboardStatCard(title: String, info: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = info, fontWeight = FontWeight.Bold)
        }
    }
}
