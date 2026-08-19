package com.messmate.android.ui.screens.bazar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.messmate.android.data.expense.BalanceResponse
import com.messmate.android.data.expense.SuggestedReimbursement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazarExpenseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    viewModel: BazarViewModel = viewModel()
) {
    val history by viewModel.history.collectAsState()
    val members by viewModel.members.collectAsState()
    val groupBalance by viewModel.groupBalance.collectAsState()
    val myUserId by viewModel.myUserId.collectAsState()

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF111119))
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onNavigateToAddExpense,
                    containerColor = Color(0xFF00FFB2),
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        },
        containerColor = Color(0xFF111119)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1A1A2E),
                contentColor = Color(0xFF00FFB2),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF00FFB2),
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        selectedContentColor = Color(0xFF00FFB2),
                        unselectedContentColor = Color.Gray
                    ) {
                        Text(
                            title,
                            modifier = Modifier.padding(vertical = 14.dp),
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // ── Expenses Tab ────────────────────────────────
                    if (history.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No expenses recorded yet.", color = Color.LightGray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
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

                1 -> {
                    // ── Balances Tab ────────────────────────────────
                    if (groupBalance == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF00FFB2))
                        }
                    } else {
                        BalancesTabContent(
                            groupBalance = groupBalance!!,
                            myUserId = myUserId
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BalancesTabContent(
    groupBalance: com.messmate.android.data.expense.GroupBalanceResponse,
    myUserId: String?
) {
    val myBalance = groupBalance.userBalances.find { it.userId == myUserId }
    val reimbursements = groupBalance.suggestedReimbursements

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── My Balance Hero Card ──────────────────────────────────
        if (myBalance != null) {
            item {
                MyBalanceHeroCard(myBalance = myBalance, reimbursements = reimbursements)
            }
        }

        // ── All Members Balances ──────────────────────────────────
        item {
            Text(
                "Balances",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        items(groupBalance.userBalances) { balance ->
            MemberBalanceRow(balance = balance, isMe = balance.userId == myUserId)
        }

        // ── Suggested Reimbursements ──────────────────────────────
        if (reimbursements.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Suggested Reimbursements",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
            }
            items(reimbursements) { r ->
                ReimbursementCard(r = r, myUserId = myUserId)
            }
        }

        item { Spacer(Modifier.height(80.dp)) } // FAB clearance
    }
}

@Composable
fun MyBalanceHeroCard(
    myBalance: BalanceResponse,
    reimbursements: List<SuggestedReimbursement>
) {
    val isPositive = myBalance.netBalance > 0.01
    val isNegative = myBalance.netBalance < -0.01
    val gradientColors = when {
        isPositive -> listOf(Color(0xFF0D3B26), Color(Color(0xFF00FFB2).value))
        isNegative -> listOf(Color(0xFF3B0D0D), Color(0xFFFF5252))
        else       -> listOf(Color(0xFF1E1E2C), Color(0xFF2D2D3E))
    }
    val accentColor = when {
        isPositive -> Color(0xFF00FFB2)
        isNegative -> Color(0xFFFF5252)
        else       -> Color.Gray
    }

    // Who owes me vs who I owe
    val owedToMe = reimbursements.filter { it.toUserId == myBalance.userId }
    val iOwe     = reimbursements.filter { it.fromUserId == myBalance.userId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradientColors), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPositive) "💰" else if (isNegative) "💸" else "✅",
                        fontSize = 32.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = when {
                                isPositive -> "You are owed"
                                isNegative -> "You owe"
                                else       -> "You're settled!"
                            },
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "₹%.2f".format(Math.abs(myBalance.netBalance)),
                            color = accentColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (owedToMe.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = owedToMe.joinToString(", ") { it.fromUserName } + " need" +
                            (if (owedToMe.size == 1) "s" else "") + " to pay you back",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
                if (iOwe.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "You need to pay back " + iOwe.joinToString(", ") { it.toUserName },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MemberBalanceRow(balance: BalanceResponse, isMe: Boolean) {
    val isPositive = balance.netBalance > 0.01
    val isNegative = balance.netBalance < -0.01
    val color = when {
        isPositive -> Color(0xFF00FFB2)
        isNegative -> Color(0xFFFF5252)
        else       -> Color.Gray
    }
    val prefix = if (isPositive) "+" else if (isNegative) "-" else "±"
    val initial = balance.name.firstOrNull()?.uppercase() ?: "?"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E2E42)),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(balance.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                if (isMe) Text("Me", color = Color.Gray, fontSize = 12.sp)
            }

            Text(
                "$prefix₹%.2f".format(Math.abs(balance.netBalance)),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ReimbursementCard(r: SuggestedReimbursement, myUserId: String?) {
    val isInvolvedAsDebtor   = r.fromUserId == myUserId
    val isInvolvedAsCreditor = r.toUserId   == myUserId

    val cardColor = when {
        isInvolvedAsDebtor   -> Color(0xFF2A1A1A)
        isInvolvedAsCreditor -> Color(0xFF0D2A1A)
        else                 -> Color(0xFF1E1E2C)
    }
    val amountColor = when {
        isInvolvedAsDebtor   -> Color(0xFFFF5252)
        isInvolvedAsCreditor -> Color(0xFF00FFB2)
        else                 -> Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = buildString {
                            append(if (r.fromUserId == myUserId) "You" else r.fromUserName)
                            append(" owes ")
                            append(if (r.toUserId   == myUserId) "you (me)" else r.toUserName)
                        },
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                    Text(
                        "₹%.2f".format(r.amount),
                        color = amountColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isInvolvedAsCreditor) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Waiting for payment", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.End)
                    }
                }
                if (isInvolvedAsDebtor) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("You need to pay", color = Color(0xFFFF5252), fontSize = 12.sp, textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}
