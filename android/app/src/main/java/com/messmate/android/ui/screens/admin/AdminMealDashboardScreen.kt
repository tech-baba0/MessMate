package com.messmate.android.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
                title = { Text("Admin: Today's Meals") },
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
