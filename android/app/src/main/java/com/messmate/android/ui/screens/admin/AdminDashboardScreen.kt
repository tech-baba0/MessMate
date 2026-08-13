package com.messmate.android.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.messmate.android.data.mess.MessMemberResponse
import com.messmate.android.data.meal.MemberMealDetailResponse
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAdminMenu: () -> Unit,
    onNavigateToAdminExpense: () -> Unit,
    onNavigateToAdminBill: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))))
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
                is AdminState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AdminState.Error -> {
                    Text(text = s.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is AdminState.Success -> {
                    val pendingMembers = s.members.filter { it.status == "PENDING" }
                    val activeMembers = s.members.filter { it.status != "PENDING" }
                    var showDetailsDialog by remember { mutableStateOf(false) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Meal Stats Section
                        item {
                            val stats = s.mealDashboard
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Today's Meal Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        StatBox(title = "Lunch", yesCount = stats.todayLunchYes, status = stats.lunchVotingStatus, modifier = Modifier.weight(1f))
                                        StatBox(title = "Dinner", yesCount = stats.todayDinnerYes, status = stats.dinnerVotingStatus, modifier = Modifier.weight(1f))
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showDetailsDialog = true },
                                        modifier = Modifier.fillMaxWidth().height(40.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("View Detailed Logs", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 2. Quick Actions Grid
                        item {
                            Text("Management Controls", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QuickActionCard(icon = Icons.Default.RestaurantMenu, title = "Menu", onClick = onNavigateToAdminMenu, modifier = Modifier.weight(1f))
                                QuickActionCard(icon = Icons.Default.AttachMoney, title = "Expenses", onClick = onNavigateToAdminExpense, modifier = Modifier.weight(1f))
                                QuickActionCard(icon = Icons.Default.Receipt, title = "Bills", onClick = onNavigateToAdminBill, modifier = Modifier.weight(1f))
                            }
                        }

                        // 3. Pending Members (Highlight if any)
                        if (pendingMembers.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pending Approvals (${pendingMembers.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            }
                            items(pendingMembers) { member ->
                                MemberCard(
                                    member = member,
                                    onApprove = { viewModel.approveMember(member.userId) },
                                    onReject = { viewModel.rejectMember(member.userId) },
                                    onPromote = { viewModel.changeRole(member.userId, "ROLE_ADMIN") }
                                )
                            }
                        }

                        // 4. Active Members
                        if (activeMembers.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Active Members (${activeMembers.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            }
                            items(activeMembers) { member ->
                                MemberCard(
                                    member = member,
                                    onApprove = { },
                                    onReject = { },
                                    onPromote = { viewModel.changeRole(member.userId, "ROLE_ADMIN") }
                                )
                            }
                        }
                    }
                    if (showDetailsDialog && s.mealDashboard.memberDetails != null) {
                        DetailedLogsModal(
                            details = s.mealDashboard.memberDetails,
                            onDismiss = { showDetailsDialog = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(title: String, yesCount: Int, status: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text("$yesCount Opted", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        val statusColor = if (status == "OPEN") Color(0xFF10B981) else Color(0xFFEF4444)
        Text(status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor)
    }
}

@Composable
fun QuickActionCard(icon: ImageVector, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(85.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun MemberCard(
    member: MessMemberResponse,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onPromote: () -> Unit
) {
    val isPending = member.status == "PENDING"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPending) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(member.name.take(1).uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = member.email, color = Color.Gray, fontSize = 13.sp)
                }
                if (member.role.contains("ADMIN")) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF3B82F6).copy(alpha=0.1f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("ADMIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                    }
                }
            }
            
            if (isPending) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Approve", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reject", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (member.role == "ROLE_USER") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onPromote,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Promote to Admin", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
