package com.messmate.android.ui.screens.meal

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
fun MealScreen(
    onNavigateBack: () -> Unit,
    viewModel: MealViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Selection") },
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
                is MealState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MealState.Error -> {
                    Text(text = s.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is MealState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Today's Meal Voting",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = s.dateStr, fontSize = 16.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(32.dp))

                        MealToggleCard(
                            mealName = "Lunch",
                            isActive = s.lunchActive,
                            isSaving = s.isSaving,
                            onToggle = { viewModel.toggleMeal(isLunch = true) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        MealToggleCard(
                            mealName = "Dinner",
                            isActive = s.dinnerActive,
                            isSaving = s.isSaving,
                            onToggle = { viewModel.toggleMeal(isLunch = false) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MealToggleCard(
    mealName: String,
    isActive: Boolean,
    isSaving: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF10B981).copy(alpha = 0.1f) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = mealName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (isActive) "Opted IN" else "Opted OUT",
                    color = if (isActive) Color(0xFF10B981) else Color.Gray
                )
            }

            Switch(
                checked = isActive,
                onCheckedChange = { onToggle() },
                enabled = !isSaving,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF10B981)
                )
            )
        }
    }
}
