package com.messmate.android.ui.screens.bazar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazarExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: BazarViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bazar Expenses", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(20.dp).clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface).padding(20.dp)
            ) {
                 Column {
                     Text("Record New Expense", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                     Spacer(modifier = Modifier.height(16.dp))
                     
                     if (state is BazarState.Loading) {
                         CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                     } else if (state is BazarState.Success) {
                         Text("Expense added successfully!", color = Color(0xFF10B981), modifier = Modifier.padding(bottom = 16.dp))
                     } else if (state is BazarState.Error) {
                         Text((state as BazarState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
                     }
                     
                     OutlinedTextField(
                         value = title,
                         onValueChange = { title = it },
                         label = { Text("Items/Title") },
                         modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                     )
                     
                     OutlinedTextField(
                         value = amount,
                         onValueChange = { amount = it },
                         label = { Text("Amount (₹)") },
                         keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                         modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                     )
                     
                     OutlinedTextField(
                         value = description,
                         onValueChange = { description = it },
                         label = { Text("Description (Optional)") },
                         modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                     )
                     
                     Button(
                         onClick = { 
                             viewModel.addExpense(title, amount.toDoubleOrNull() ?: 0.0, description) 
                         },
                         modifier = Modifier.fillMaxWidth().height(50.dp),
                         shape = RoundedCornerShape(12.dp),
                         colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                     ) {
                         Text("Save Expense", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                     }
                 }
            }
        }
    }
}
