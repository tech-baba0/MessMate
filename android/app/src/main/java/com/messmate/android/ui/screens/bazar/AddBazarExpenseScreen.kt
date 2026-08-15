package com.messmate.android.ui.screens.bazar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.messmate.android.data.expense.ExpenseRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBazarExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: BazarViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val members by viewModel.members.collectAsState()
    
    val checkedMembers = remember { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(members) {
        if (checkedMembers.isEmpty()) {
            members.forEach { checkedMembers[it.userId] = true }
        }
    }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Grocery") }
    var notes by remember { mutableStateOf("") }
    
    var splitMethod by remember { mutableStateOf("AUTO_MEAL") }
    var mealScope by remember { mutableStateOf("BOTH") }
    
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showPaidByMenu by remember { mutableStateOf(false) }
    var showSplitMenu by remember { mutableStateOf(false) }
    var showMealScopeMenu by remember { mutableStateOf(false) }

    val categories = listOf("Grocery", "Vegetables", "Meat", "Fish", "Chicken", "Rice", "Oil", "Milk", "Gas", "Electricity", "Water", "Snacks", "Other")
    val splitMethods = listOf("AUTO_MEAL" to "Auto Split by Meals", "EQUAL" to "Split Equally")
    val mealScopes = listOf("LUNCH" to "Lunch", "DINNER" to "Dinner", "BOTH" to "Both")

    val glassmorphicColor = Color(0xFF1E1E2C)
    val inputBgColor = Color(0xFF2A2A3C)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Bazar Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF111119),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF111119)
    ) { padding ->
        if (state is BazarState.PreviewReady) {
            val previewData = state as BazarState.PreviewReady
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Expense Summary", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = glassmorphicColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${previewData.request.title} - ₹${previewData.request.totalAmount}", color = Color.Green, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Category: ${previewData.request.category}", color = Color.LightGray)
                        Text("Paid by: ${previewData.request.paidBy}", color = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Calculated Split:", color = Color.White, fontWeight = FontWeight.Bold)
                        Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                        
                        LazyColumn {
                            items(previewData.shares) { share ->
                                val memberName = members.find { it.userId == share.userId }?.name ?: "Unknown"
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(memberName, color = Color.White)
                                    Text("₹%.2f".format(share.shareAmount), color = Color(0xFF00FFB2))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.resetState() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit", color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.submitExpense(previewData.request) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm & Save", color = Color.White)
                    }
                }
            }
        } else if (state is BazarState.Success) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Expense Added Successfully!", color = Color(0xFF00FFB2), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))) {
                    Text("Go Back")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title (e.g. Rice, Milk)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBgColor,
                            unfocusedContainerColor = inputBgColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBgColor,
                            unfocusedContainerColor = inputBgColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBgColor,
                            unfocusedContainerColor = inputBgColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            label = { Text("Category") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showCategoryMenu = true }) {
                                    Icon(Icons.Default.ArrowDropDown, "Select Category")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = inputBgColor,
                                unfocusedContainerColor = inputBgColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = { 
                                        category = cat
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val paidByName = members.find { it.userId == paidBy }?.name ?: "Select Member"
                        OutlinedTextField(
                            value = paidByName,
                            onValueChange = {},
                            label = { Text("Paid By") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showPaidByMenu = true }) {
                                    Icon(Icons.Default.ArrowDropDown, "Select Payer")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = inputBgColor,
                                unfocusedContainerColor = inputBgColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        DropdownMenu(
                            expanded = showPaidByMenu,
                            onDismissRequest = { showPaidByMenu = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name) },
                                    onClick = { 
                                        paidBy = m.userId
                                        showPaidByMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                item {
                    val totalAmt = amount.toDoubleOrNull() ?: 0.0
                    val checkedCount = checkedMembers.values.count { it }
                    val splitAmount = if (checkedCount > 0) totalAmt / checkedCount else 0.0

                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checkedCount > 0 && checkedCount == members.size,
                                onCheckedChange = { isChecked ->
                                    members.forEach { checkedMembers[it.userId] = isChecked }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Split", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Text("Equally ⌄", color = Color.LightGray, fontSize = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = inputBgColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            members.forEach { member ->
                                val isChecked = checkedMembers[member.userId] ?: false
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { checkedMembers[member.userId] = !isChecked }
                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checkedMembers[member.userId] = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF3B82F6),
                                                uncheckedColor = Color.Gray
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(member.name, color = Color.White, fontSize = 16.sp)
                                    }
                                    val displayAmount = if (isChecked) splitAmount else 0.0
                                    Text("₹%.2f".format(displayAmount), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val totalAmt = amount.toDoubleOrNull() ?: 0.0
                            val checkedCount = checkedMembers.values.count { it }
                            val splitAmount = if (checkedCount > 0) totalAmt / checkedCount else 0.0
                            val customSplitsList = members
                                .filter { checkedMembers[it.userId] == true }
                                .map { com.messmate.android.data.expense.CustomSplit(it.userId, amount = splitAmount) }
                                
                            val request = ExpenseRequest(
                                title = title,
                                description = notes.ifBlank { null },
                                date = date,
                                category = category,
                                mealScope = null,
                                paidBy = paidBy.ifBlank { null },
                                totalAmount = totalAmt,
                                splitMethod = "CUSTOM_FIXED",
                                items = emptyList(),
                                customSplits = customSplitsList
                            )
                            viewModel.calculatePreview(request)
                        },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        enabled = title.isNotBlank() && amount.toDoubleOrNull() != null && date.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFB2), contentColor = Color.Black)
                    ) {
                        Text("Preview Split", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    if (state is BazarState.Error) {
                        Text(
                            text = (state as BazarState.Error).message,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
