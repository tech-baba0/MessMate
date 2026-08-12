package com.messmate.android.ui.screens.meal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.messmate.android.data.meal.MealStatusResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: MealHistoryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedFilter by remember { mutableStateOf("MONTH") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Meal History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = selectedFilter == "WEEK",
                    onClick = { selectedFilter = "WEEK"; viewModel.loadHistoryForFilter("WEEK") },
                    label = { Text("Last 7 Days") }
                )
                FilterChip(
                    selected = selectedFilter == "MONTH",
                    onClick = { selectedFilter = "MONTH"; viewModel.loadHistoryForFilter("MONTH") },
                    label = { Text("This Month") }
                )
            }

            when (val s = state) {
                is MealHistoryState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MealHistoryState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = s.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is MealHistoryState.Success -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Total Lunch: ${s.summary.totalLunch}")
                                Text("Total Dinner: ${s.summary.totalDinner}")
                                Text("Total Units: ${s.summary.totalMeals}")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Date", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Lunch", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Dinner", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        }
                        Divider()

                        LazyColumn {
                            items(s.summary.meals) { meal ->
                                MealHistoryRow(meal)
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealHistoryRow(meal: MealStatusResponse) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = meal.date, modifier = Modifier.weight(1f))
        Text(
            text = if (meal.lunch) "YES" else "NO",
            color = if (meal.lunch) Color(0xFF10B981) else Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (meal.dinner) "YES" else "NO",
            color = if (meal.dinner) Color(0xFF10B981) else Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}
