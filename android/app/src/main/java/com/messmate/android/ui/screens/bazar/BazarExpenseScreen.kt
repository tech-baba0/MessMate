package com.messmate.android.ui.screens.bazar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.messmate.android.data.expense.BalanceResponse
import com.messmate.android.data.expense.GroupBalanceResponse
import com.messmate.android.data.expense.SuggestedReimbursement

private val BG          = Color(0xFF0D0D0D)
private val CARD_BG     = Color(0xFF1A1A1A)
private val GREEN       = Color(0xFF2EB87E)
private val RED         = Color(0xFFE05252)
private val BLUE_TEXT   = Color(0xFF4D8EF5)
private val AVATAR_BG   = Color(0xFF3A3A3A)
private val DIVIDER     = Color(0xFF2A2A2A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazarExpenseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    viewModel: BazarViewModel = viewModel()
) {
    val history      by viewModel.history.collectAsState()
    val members      by viewModel.members.collectAsState()
    val groupBalance by viewModel.groupBalance.collectAsState()
    val myUserId     by viewModel.myUserId.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Expenses", "Balances")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bazar Expenses", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BG)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onNavigateToAddExpense,
                    containerColor = Color(0xFF00FFB2),
                    contentColor   = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        },
        containerColor = BG
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // ── Tab Row (styled like the screenshot) ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1C1C1C)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) Color(0xFF2E2E2E) else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title,
                            color     = if (selected) Color.White else Color.Gray,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize  = 15.sp
                        )
                    }
                }
            }

            // ── Tab Content ────────────────────────────────────────────────────
            when (selectedTab) {
                // ── Expenses Tab ────────────────────────────────────────────────
                0 -> {
                    if (history.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No expenses recorded yet.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp, top = 4.dp)
                        ) {
                            items(history) { expense ->
                                val payerName = members.find { it.userId == expense.purchasedById }?.name
                                    ?: expense.purchasedById ?: "Unknown"
                                Card(
                                    colors    = CardDefaults.cardColors(containerColor = CARD_BG),
                                    shape     = RoundedCornerShape(14.dp),
                                    modifier  = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier              = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment     = Alignment.CenterVertically
                                        ) {
                                            Text(expense.title,       fontSize = 18.sp, fontWeight = FontWeight.Bold,  color = Color.White)
                                            Text("₹%.2f".format(expense.totalAmount), color = GREEN, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text("Date: ${expense.date}",        color = Color.Gray, fontSize = 13.sp)
                                        Text("Paid by: $payerName",           color = Color.Gray, fontSize = 13.sp)
                                        Text("Category: ${expense.category ?: "N/A"}", color = Color.Gray, fontSize = 13.sp)
                                        if (!expense.splitMethod.isNullOrBlank())
                                            Text("Split: ${expense.splitMethod}", color = Color(0xFF555565), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Balances Tab ────────────────────────────────────────────────
                1 -> {
                    if (groupBalance == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GREEN)
                        }
                    } else {
                        BalancesTabContent(
                            groupBalance = groupBalance!!,
                            myUserId     = myUserId
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Balances Tab — matches the screenshot exactly
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BalancesTabContent(
    groupBalance: GroupBalanceResponse,
    myUserId: String?
) {
    val myBalance      = groupBalance.userBalances.find { it.userId == myUserId }
    val reimbursements = groupBalance.suggestedReimbursements

    // Who owes me vs who I owe
    val debtorsToMe = reimbursements.filter { it.toUserId   == myUserId }
    val iOweTo      = reimbursements.filter { it.fromUserId == myUserId }

    var showAllReimbursements by remember { mutableStateOf(false) }

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(BG),
        contentPadding      = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // ── Hero Card ────────────────────────────────────────────────────────
        item {
            if (myBalance != null) {
                val isPositive = myBalance.netBalance > 0.01
                val isNegative = myBalance.netBalance < -0.01

                Card(
                    colors = CardDefaults.cardColors(containerColor = CARD_BG),
                    shape  = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji
                        Text(
                            text     = if (isPositive) "🤑" else if (isNegative) "😬" else "😊",
                            fontSize = 42.sp,
                            modifier = Modifier.size(56.dp),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            val amtStr = "₹%,.2f".format(Math.abs(myBalance.netBalance))
                            Text(
                                text       = when {
                                    isPositive -> "You are owed $amtStr"
                                    isNegative -> "You owe $amtStr"
                                    else       -> "You're all settled up!"
                                },
                                color      = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 17.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text     = when {
                                    isPositive && debtorsToMe.isNotEmpty() ->
                                        "See how ${debtorsToMe.joinToString(" and ") { it.fromUserName }} need${if (debtorsToMe.size == 1) "s" else ""} to pay you back"
                                    isNegative && iOweTo.isNotEmpty() ->
                                        "You need to pay ${iOweTo.joinToString(" and ") { it.toUserName }}"
                                    else -> "No outstanding balances"
                                },
                                color    = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint   = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ── View All Suggested Reimbursements button ─────────────────────────
        item {
            Card(
                colors   = CardDefaults.cardColors(containerColor = CARD_BG),
                shape    = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().clickable { showAllReimbursements = !showAllReimbursements }
            ) {
                Box(
                    modifier          = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment  = Alignment.Center
                ) {
                    Text(
                        if (!showAllReimbursements) "View All Suggested Reimbursements"
                        else "Hide Reimbursements",
                        color      = BLUE_TEXT,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp
                    )
                }
            }
        }

        // ── Reimbursement Cards (expanded) ───────────────────────────────────
        if (showAllReimbursements) {
            if (reimbursements.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        Text("No reimbursements needed 🎉", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                items(reimbursements) { r ->
                    ReimbursementRow(r = r, myUserId = myUserId)
                }
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        // ── "Balances" section header ────────────────────────────────────────
        item {
            Text(
                "Balances",
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize   = 22.sp,
                modifier   = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // ── Member balance rows ──────────────────────────────────────────────
        items(groupBalance.userBalances.sortedByDescending { it.netBalance }) { balance ->
            MemberBalanceCard(balance = balance, isMe = balance.userId == myUserId)
        }
    }
}

// ─── Member Balance Card ──────────────────────────────────────────────────────
@Composable
fun MemberBalanceCard(balance: BalanceResponse, isMe: Boolean) {
    val isPositive = balance.netBalance > 0.01
    val isNegative = balance.netBalance < -0.01
    val amtColor   = when { isPositive -> GREEN;  isNegative -> RED;  else -> Color.Gray }
    val prefix     = when { isPositive -> "+";    isNegative -> "-";  else -> "" }
    val initial    = balance.name.firstOrNull()?.uppercase() ?: "?"

    Card(
        colors   = CardDefaults.cardColors(containerColor = CARD_BG),
        shape    = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle
            Box(
                modifier        = Modifier.size(46.dp).clip(CircleShape).background(AVATAR_BG),
                contentAlignment = Alignment.Center
            ) {
                if (isMe) {
                    Text("👤", fontSize = 20.sp)
                } else {
                    Text(
                        initial,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    balance.name,
                    color      = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 17.sp
                )
                if (isMe) {
                    Text("Me", color = Color.Gray, fontSize = 13.sp)
                }
            }

            Text(
                "${prefix}₹%,.2f".format(Math.abs(balance.netBalance)),
                color      = amtColor,
                fontWeight = FontWeight.Bold,
                fontSize   = 17.sp
            )
        }
    }
}

// ─── Reimbursement Row ────────────────────────────────────────────────────────
@Composable
fun ReimbursementRow(r: SuggestedReimbursement, myUserId: String?) {
    val isDebtor   = r.fromUserId == myUserId
    val isCreditor = r.toUserId   == myUserId

    val cardColor  = when { isDebtor -> Color(0xFF1F1010); isCreditor -> Color(0xFF0F1F14); else -> CARD_BG }
    val amtColor   = when { isDebtor -> RED; isCreditor -> GREEN; else -> Color.White }

    Card(
        colors   = CardDefaults.cardColors(containerColor = cardColor),
        shape    = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val from = if (isDebtor)   "You"         else r.fromUserName
                val to   = if (isCreditor) "you"         else r.toUserName
                Text("$from → $to", color = Color.LightGray, fontSize = 14.sp)
                Text(
                    "₹%,.2f".format(r.amount),
                    color      = amtColor,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isDebtor) {
                Text("You owe", color = RED, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            } else if (isCreditor) {
                Text("Owed to you", color = GREEN, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
