package com.messmate.android.ui.screens.bazar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazarExpenseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    viewModel: BazarViewModel = viewModel()
) {
    val history by viewModel.history.collectAsState()
    val members by viewModel.members.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bazar History", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF111119))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                containerColor = Color(0xFF00FFB2),
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        containerColor = Color(0xFF111119)
    ) { paddingValues ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No expenses recorded yet.", color = Color.LightGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(history) { expense ->
                    val payerName = members.find { it.userId == expense.purchasedById }?.name ?: expense.purchasedById ?: "Unknown"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(expense.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("₹%.2f".format(expense.totalAmount), color = Color(0xFF00FFB2), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Date: ${expense.date}", color = Color.LightGray, fontSize = 14.sp)
                            Text("Paid by: $payerName", color = Color.LightGray, fontSize = 14.sp)
                            Text("Category: ${expense.category ?: "N/A"}", color = Color.LightGray, fontSize = 14.sp)
                            Text("Scope: ${expense.mealScope ?: "BOTH"}", color = Color.LightGray, fontSize = 14.sp)
                            Text("Split Method: ${expense.splitMethod ?: "AUTO_MEAL"}", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
