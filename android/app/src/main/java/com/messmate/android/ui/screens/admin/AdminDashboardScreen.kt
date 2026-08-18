@file:OptIn(ExperimentalMaterial3Api::class)
package com.messmate.android.ui.screens.admin

import androidx.compose.animation.core.*
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
import androidx.compose.runtime.*
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
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Admin Panel", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(12.dp))
                        LiveIndicator()
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
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
                        // 1. Total Meal Count (Live)
                        item {
                            val stats = s.mealDashboard
                            val totalActiveCount = stats.totalActiveMembers

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("TODAY'S MEAL COUNT", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🍚", fontSize = 22.sp)
                                            Text(
                                                text = "${stats.todayLunchYes}",
                                                color = Color(0xFF10B981),
                                                fontSize = 42.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text("Lunch", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                                            if (stats.todayLunchNo > 0) {
                                                Text(
                                                    "${stats.todayLunchNo} opted out",
                                                    color = Color(0xFFEF4444),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(80.dp)
                                                .background(Color.White.copy(alpha = 0.15f))
                                        )
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🌙", fontSize = 22.sp)
                                            Text(
                                                text = "${stats.todayDinnerYes}",
                                                color = Color(0xFFF59E0B),
                                                fontSize = 42.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text("Dinner", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                                            if (stats.todayDinnerNo > 0) {
                                                Text(
                                                    "${stats.todayDinnerNo} opted out",
                                                    color = Color(0xFFEF4444),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    // Default-YES context
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("ℹ️", fontSize = 14.sp)
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "Default YES for all $totalActiveCount active members",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Live updates active", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        // 2. Voting Status Cards
                        item {
                            val stats = s.mealDashboard
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatBox(title = "Lunch", yesCount = stats.todayLunchYes, noCount = stats.todayLunchNo, status = stats.lunchVotingStatus, modifier = Modifier.weight(1f))
                                StatBox(title = "Dinner", yesCount = stats.todayDinnerYes, noCount = stats.todayDinnerNo, status = stats.dinnerVotingStatus, modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showDetailsDialog = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("View Member Detailed Logs", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }


                        // 3. Quick Actions
                        item {
                            Text("Management", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QuickActionCard(icon = Icons.Default.RestaurantMenu, title = "Menu", onClick = onNavigateToAdminMenu, modifier = Modifier.weight(1f))
                                QuickActionCard(icon = Icons.Default.AttachMoney, title = "Expenses", onClick = onNavigateToAdminExpense, modifier = Modifier.weight(1f))
                                QuickActionCard(icon = Icons.Default.Receipt, title = "Bills", onClick = onNavigateToAdminBill, modifier = Modifier.weight(1f))
                            }
                        }


                        // 4. Pending Approvals
                        if (pendingMembers.isNotEmpty()) {
                            item {
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

                        // 5. Active Members
                        item {
                            Text("Active Members (${activeMembers.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                    
                    if (showDetailsDialog && s.mealDashboard.memberDetails != null) {
                        DetailedLogsModal(
                            details = s.mealDashboard.memberDetails,
                            onDismiss = { showDetailsDialog = false }
                        )
                    }
                } // AdminState.Success
            } // when
        } // Box
    }
}

@Composable
fun LiveIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.Red.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("LIVE", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun StatBox(title: String, yesCount: Int, noCount: Int = 0, status: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text("$yesCount", fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text("YES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        if (noCount > 0) {
            Text("$noCount NO", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
        }
        Spacer(modifier = Modifier.height(4.dp))
        val statusColor = if (status == "OPEN") Color(0xFF10B981) else Color(0xFFEF4444)
        Text(status, fontSize = 10.sp, fontWeight = FontWeight.Black, color = statusColor)
    }
}

@Composable
fun QuickActionCard(icon: ImageVector, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPending) 6.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(member.name.take(1).uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = member.email, color = Color.Gray, fontSize = 13.sp)
                }
                if (member.role.contains("ADMIN")) {
                    Badge(containerColor = Color(0xFF3B82F6).copy(alpha = 0.1f)) {
                        Text("ADMIN", color = Color(0xFF3B82F6), modifier = Modifier.padding(4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Approve", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reject", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (member.role == "ROLE_USER") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onPromote,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Promote to Admin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailedLogsModal(
    details: List<MemberMealDetailResponse>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Today's Member Choices", fontWeight = FontWeight.ExtraBold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(details) { detail ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(detail.userName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                MealStatusItem(
                                    label = "Lunch",
                                    status = detail.lunch,
                                    time = detail.lunchUpdatedAt,
                                    isDefault = detail.lunchIsDefault
                                )
                                MealStatusItem(
                                    label = "Dinner",
                                    status = detail.dinner,
                                    time = detail.dinnerUpdatedAt,
                                    isDefault = detail.dinnerIsDefault
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun MealStatusItem(label: String, status: Boolean, time: String, isDefault: Boolean = true) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (status) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (status) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("$label: ${if (status) "YES" else "NO"}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Text(
            if (isDefault) "Default" else time,
            fontSize = 10.sp,
            color = if (isDefault) Color.Gray else Color(0xFF10B981)
        )
    }
}

