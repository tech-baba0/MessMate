@file:OptIn(ExperimentalMaterial3Api::class)
package com.messmate.android.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.time.LocalDate

@Composable
fun AdminBillScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminBillViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val now = LocalDate.now()
    var monthYearInput by remember { mutableStateOf(
        String.format("%02d-%04d", now.monthValue, now.year)
    )}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Bill Management") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Generate Monthly Bill", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = monthYearInput,
                onValueChange = { monthYearInput = it },
                label = { Text("Month-Year (MM-YYYY)") },
                placeholder = { Text("e.g. 05-2024") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state is AdminBillState.Error
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.generateBill(monthYearInput) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Generate Bill")
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (val s = state) {
                is AdminBillState.Loading -> CircularProgressIndicator()
                is AdminBillState.Error -> {
                    Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                is AdminBillState.Success -> {
                    Text(s.message, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Expenses: ₹${s.settlement.totalExpenses}", fontWeight = FontWeight.Bold)
                            Text("Total Meals: ${s.settlement.totalMeals}")
                            Text("Meal Rate: ₹${String.format("%.2f", s.settlement.mealRate)}")
                            Text("Status: ${s.settlement.status}", color = if(s.settlement.status == "OPEN") Color(0xFF3B82F6) else Color.Gray)
                            
                            if (s.settlement.status != "CLOSED") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.closeBill(monthYearInput, s.settlement.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Finalize & Close Month")
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
