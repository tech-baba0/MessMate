package com.messmate.android.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.messmate.android.data.menu.Menu

val dayNames = arrayOf("Unknown", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigateBack: () -> Unit,
    viewModel: MenuViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Menu") },
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
                is MenuState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is MenuState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is MenuState.Success -> {
                    if (s.menus.isEmpty()) {
                        Text(
                            text = "No weekly menu published yet.",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(s.menus.sortedBy { it.dayOfWeek }) { menu ->
                                MenuCard(menu)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuCard(menu: Menu) {
    val dayName = if (menu.dayOfWeek in 1..7) dayNames[menu.dayOfWeek] else "Special Day"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = dayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (menu.lunchItems.isNullOrEmpty().not()) {
                Text("Lunch", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(menu.lunchItems!!.joinToString(", "))
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (menu.dinnerItems.isNullOrEmpty().not()) {
                Text("Dinner", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(menu.dinnerItems!!.joinToString(", "))
            }
        }
    }
}
