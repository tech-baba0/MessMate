package com.messmate.android.ui.screens.bazar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import java.util.Calendar
import java.time.LocalDate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.messmate.android.data.expense.CustomSplit
import com.messmate.android.data.expense.ExpenseRequest

// Split modes
private const val SPLIT_EQUAL  = "EQUAL"
private const val SPLIT_PARTS  = "PARTS"
private const val SPLIT_AMOUNT = "AMOUNT"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBazarExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: BazarViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val members by viewModel.members.collectAsState()

    val checkedMembers   = remember { mutableStateMapOf<String, Boolean>() }
    val partValues       = remember { mutableStateMapOf<String, String>() } // parts (e.g. 1, 2, 3)
    val fixedAmtValues   = remember { mutableStateMapOf<String, String>() } // fixed ₹ amounts

    LaunchedEffect(members) {
        members.forEach { m ->
            if (!checkedMembers.containsKey(m.userId))  checkedMembers[m.userId]  = true
            if (!partValues.containsKey(m.userId))       partValues[m.userId]       = "1"
            if (!fixedAmtValues.containsKey(m.userId))  fixedAmtValues[m.userId]  = ""
        }
    }

    var title    by remember { mutableStateOf("") }
    var amount   by remember { mutableStateOf("") }
    var paidBy   by remember { mutableStateOf("") }
    var date     by remember { mutableStateOf(LocalDate.now().toString()) }
    var category by remember { mutableStateOf("Grocery") }
    var notes    by remember { mutableStateOf("") }

    var splitMode by remember { mutableStateOf(SPLIT_EQUAL) }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showPaidByMenu   by remember { mutableStateOf(false) }

    val categories = listOf("Grocery", "Vegetables", "Meat", "Fish", "Chicken", "Rice", "Oil", "Milk", "Gas", "Electricity", "Water", "Snacks", "Other")

    val glassmorphicColor = Color(0xFF1E1E2C)
    val inputBgColor      = Color(0xFF2A2A3C)

    // ─── Derived split calculations ──────────────────────────────────────────
    val totalAmt    = amount.toDoubleOrNull() ?: 0.0
    val active      = members.filter { checkedMembers[it.userId] == true }
    val activeCount = active.size

    val splitAmounts: Map<String, Double> = when (splitMode) {
        SPLIT_EQUAL -> {
            if (activeCount == 0) emptyMap()
            else active.associate { it.userId to totalAmt / activeCount }
        }
        SPLIT_PARTS -> {
            val totalParts = active.sumOf { partValues[it.userId]?.toDoubleOrNull() ?: 1.0 }
            if (totalParts == 0.0) active.associate { it.userId to 0.0 }
            else active.associate { m ->
                val parts = partValues[m.userId]?.toDoubleOrNull() ?: 1.0
                m.userId to totalAmt * (parts / totalParts)
            }
        }
        SPLIT_AMOUNT -> {
            active.associate { m -> m.userId to (fixedAmtValues[m.userId]?.toDoubleOrNull() ?: 0.0) }
        }
        else -> emptyMap()
    }

    // Error for AMOUNT mode if total doesn't match
    val amountModeError: String? = if (splitMode == SPLIT_AMOUNT && totalAmt > 0) {
        val entered = splitAmounts.values.sum()
        val diff = Math.abs(entered - totalAmt)
        if (diff > 0.01) "Total entered ₹%.2f ≠ expense ₹%.2f".format(entered, totalAmt) else null
    } else null

    // ─────────────────────────────────────────────────────────────────────────

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
                    containerColor  = Color(0xFF111119),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF111119)
    ) { padding ->

        // ── Preview Screen ──────────────────────────────────────────────────
        if (state is BazarState.PreviewReady) {
            val previewData = state as BazarState.PreviewReady
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("Expense Summary", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = glassmorphicColor), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${previewData.request.title} - ₹${previewData.request.totalAmount}", color = Color.Green, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Category: ${previewData.request.category}", color = Color.LightGray)
                        Text("Paid by: ${previewData.request.paidBy}", color = Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text("Calculated Split:", color = Color.White, fontWeight = FontWeight.Bold)
                        Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                        LazyColumn {
                            items(previewData.shares) { share ->
                                val memberName = members.find { it.userId == share.userId }?.name ?: "Unknown"
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(memberName, color = Color.White)
                                    Text("₹%.2f".format(share.shareAmount), color = Color(0xFF00FFB2))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.resetState() }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray), modifier = Modifier.weight(1f)) {
                        Text("Edit", color = Color.White)
                    }
                    Button(onClick = { viewModel.submitExpense(previewData.request) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)), modifier = Modifier.weight(1f)) {
                        Text("Confirm & Save", color = Color.White)
                    }
                }
            }

        // ── Success Screen ──────────────────────────────────────────────────
        } else if (state is BazarState.Success) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Expense Added Successfully!", color = Color(0xFF00FFB2), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))) { Text("Go Back") }
            }

        // ── Add Expense Form ────────────────────────────────────────────────
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Title
                item {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title (e.g. Rice, Milk)") }, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = inputBgColor, unfocusedContainerColor = inputBgColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                }

                // Amount
                item {
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Total Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = inputBgColor, unfocusedContainerColor = inputBgColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                }

                // Date
                item {
                    val context = LocalContext.current
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                val cal = Calendar.getInstance()
                                DatePickerDialog(context, { _, y, m, d ->
                                    date = "$y-${(m+1).toString().padStart(2,'0')}-${d.toString().padStart(2,'0')}"
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                            }) { Icon(Icons.Default.DateRange, contentDescription = "Pick Date") }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = inputBgColor, unfocusedContainerColor = inputBgColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                }

                // Category dropdown
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = category, onValueChange = {}, label = { Text("Category") }, readOnly = true, modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { IconButton(onClick = { showCategoryMenu = true }) { Icon(Icons.Default.ArrowDropDown, "Category") } },
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = inputBgColor, unfocusedContainerColor = inputBgColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        DropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) {
                            categories.forEach { cat -> DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; showCategoryMenu = false }) }
                        }
                    }
                }

                // Paid by dropdown
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val paidByName = members.find { it.userId == paidBy }?.name ?: "Select Member"
                        OutlinedTextField(value = paidByName, onValueChange = {}, label = { Text("Paid By") }, readOnly = true, modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { IconButton(onClick = { showPaidByMenu = true }) { Icon(Icons.Default.ArrowDropDown, "Payer") } },
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = inputBgColor, unfocusedContainerColor = inputBgColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        DropdownMenu(expanded = showPaidByMenu, onDismissRequest = { showPaidByMenu = false }) {
                            members.forEach { m -> DropdownMenuItem(text = { Text(m.name) }, onClick = { paidBy = m.userId; showPaidByMenu = false }) }
                        }
                    }
                }

                // ── Split Section ─────────────────────────────────────────────────
                item {
                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                    // Header row: "Split" label + mode toggle chips
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = activeCount == members.size && members.isNotEmpty(),
                                onCheckedChange = { isChecked -> members.forEach { checkedMembers[it.userId] = isChecked } },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6))
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Split", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        // Split mode chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(SPLIT_EQUAL to "Equally", SPLIT_PARTS to "Parts", SPLIT_AMOUNT to "Amount").forEach { (mode, label) ->
                                val selected = splitMode == mode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (selected) Color(0xFF3B82F6) else Color(0xFF2A2A3C))
                                        .border(1.dp, if (selected) Color(0xFF3B82F6) else Color.DarkGray, RoundedCornerShape(20.dp))
                                        .clickable { splitMode = mode }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(label, color = if (selected) Color.White else Color.LightGray, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }

                    // Helper text
                    Text(
                        text = when (splitMode) {
                            SPLIT_EQUAL  -> "Split the total equally among selected members."
                            SPLIT_PARTS  -> "Enter how many parts each person shares (e.g. 1, 2, 3)."
                            SPLIT_AMOUNT -> "Enter the exact ₹ amount each person pays."
                            else         -> ""
                        },
                        color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    // Member rows
                    Card(colors = CardDefaults.cardColors(containerColor = inputBgColor), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column {
                            members.forEach { member ->
                                val isChecked = checkedMembers[member.userId] ?: false
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { checkedMembers[member.userId] = !isChecked }.padding(vertical = 8.dp, horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Checkbox + Name
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checkedMembers[member.userId] = it },
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6), uncheckedColor = Color.Gray)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(member.name, color = Color.White, fontSize = 15.sp)
                                    }

                                    // Right side: input or computed amount
                                    if (isChecked) {
                                        when (splitMode) {
                                            SPLIT_EQUAL -> {
                                                val eq = splitAmounts[member.userId] ?: 0.0
                                                Text("₹%.2f".format(eq), color = Color(0xFF00FFB2), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                            SPLIT_PARTS -> {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    OutlinedTextField(
                                                        value = partValues[member.userId] ?: "1",
                                                        onValueChange = { v -> if (v.all { c -> c.isDigit() } && v.length <= 3) partValues[member.userId] = v.ifBlank { "1" } },
                                                        label = { Text("Parts", fontSize = 10.sp) },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.width(72.dp),
                                                        singleLine = true,
                                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1A1A2E), unfocusedContainerColor = Color(0xFF1A1A2E))
                                                    )
                                                    val computed = splitAmounts[member.userId] ?: 0.0
                                                    Text("= ₹%.2f".format(computed), color = Color(0xFF00FFB2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            SPLIT_AMOUNT -> {
                                                OutlinedTextField(
                                                    value = fixedAmtValues[member.userId] ?: "",
                                                    onValueChange = { v -> fixedAmtValues[member.userId] = v },
                                                    label = { Text("₹ Amount", fontSize = 10.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                    modifier = Modifier.width(100.dp),
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1A1A2E), unfocusedContainerColor = Color(0xFF1A1A2E),
                                                        focusedBorderColor = if (amountModeError != null) Color.Red else Color(0xFF3B82F6))
                                                )
                                            }
                                        }
                                    } else {
                                        Text("₹0.00", color = Color.Gray, fontSize = 14.sp)
                                    }
                                }
                            }

                            // AMOUNT mode total validation footer
                            if (splitMode == SPLIT_AMOUNT && totalAmt > 0) {
                                val entered = splitAmounts.values.sum()
                                Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total entered", color = Color.Gray, fontSize = 13.sp)
                                    Text("₹%.2f / ₹%.2f".format(entered, totalAmt),
                                        color = if (amountModeError != null) Color.Red else Color(0xFF00FFB2),
                                        fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                if (amountModeError != null) {
                                    Text("  ⚠ $amountModeError", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp))
                                }
                            }
                        }
                    }
                }

                // Submit button
                item {
                    Spacer(Modifier.height(8.dp))

                    val canSubmit = title.isNotBlank() &&
                        totalAmt > 0 &&
                        date.isNotBlank() &&
                        activeCount > 0 &&
                        amountModeError == null

                    Button(
                        onClick = {
                            val customSplitsList = active.map { m ->
                                CustomSplit(m.userId, amount = splitAmounts[m.userId] ?: 0.0)
                            }
                            val request = ExpenseRequest(
                                title        = title,
                                description  = notes.ifBlank { null },
                                date         = date,
                                category     = category,
                                mealScope    = null,
                                paidBy       = paidBy.ifBlank { null },
                                totalAmount  = totalAmt,
                                splitMethod  = "CUSTOM_FIXED",
                                items        = emptyList(),
                                customSplits = customSplitsList
                            )
                            viewModel.calculatePreview(request)
                        },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        enabled  = canSubmit,
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFB2), contentColor = Color.Black)
                    ) {
                        Text("Preview Split", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    if (state is BazarState.Error) {
                        Text(text = (state as BazarState.Error).message, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
