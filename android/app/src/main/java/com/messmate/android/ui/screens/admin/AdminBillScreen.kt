package com.messmate.android.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBillScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminBillViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var monthYearInput by remember { mutableStateOf(
        String.format("%02d-%04d", LocalDate.now().monthValue, LocalDate.now().year)
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
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.generateBill(monthYearInput) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Bill")
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (val s = state) {
                is AdminBillState.Loading -> CircularProgressIndicator()
                is AdminBillState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
                is AdminBillState.Success -> {
                    Text(s.message, color = Color.Green, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Expenses: ₹${s.settlement.totalExpenses}")
                            Text("Total Meals: ${s.settlement.totalMeals}")
                            Text("Status: ${s.settlement.status}")
                            
                            if (s.settlement.status != "CLOSED") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.closeBill(monthYearInput, s.settlement.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Close Month")
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
