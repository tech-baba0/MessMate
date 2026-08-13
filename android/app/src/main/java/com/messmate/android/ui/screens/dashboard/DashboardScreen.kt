package com.messmate.android.ui.screens.dashboard

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

import androidx.compose.material.icons.filled.ExitToApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToMeal: () -> Unit,
    onNavigateToBazar: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToMealHistory: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("MessMate", fontWeight = FontWeight.ExtraBold, color = Color.White) 
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                    )
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Balance or Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E1B4B), Color(0xFF4338CA))
                        )
                    )
                    .padding(24.dp)
            ) {
                if (state is DashboardState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
                } else if (state is DashboardState.NoMess) {
                    var inviteCode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "You are not in a mess yet.",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = inviteCode,
                            onValueChange = { inviteCode = it },
                            label = { Text("Invite Code", color = Color.White.copy(0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.joinMess(inviteCode) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Join Mess")
                        }
                    }
                } else if (state is DashboardState.PendingApproval) {
                    Text(
                        text = "Your account is waiting for Admin approval.",
                        color = Color.Yellow,
                        modifier = Modifier.align(Alignment.Center),
                        fontWeight = FontWeight.Bold
                    )
                } else if (state is DashboardState.Rejected) {
                    Text(
                        text = "Your request was rejected by the Admin.",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center),
                        fontWeight = FontWeight.Bold
                    )
                } else if (state is DashboardState.Inactive) {
                    Text(
                        text = "Your mess access is currently inactive.",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                        fontWeight = FontWeight.Bold
                    )
                } else if (state is DashboardState.Success) {
                    val balance = (state as DashboardState.Success).balance
                    
                    Column {
                        val netBal = balance.netBalance ?: 0.0
                        val balSign = if (netBal >= 0) "(Receive)" else "(Owe)"
                        
                        Text(
                            text = "Current Balance $balSign",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹ " + String.format("%.2f", Math.abs(netBal)),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DepositInfoColumn(label = "Total Deposit", amount = "₹ ${balance.paymentsMade ?: 0.0}")
                            DepositInfoColumn(label = "Total Expense", amount = "₹ ${balance.totalExpenseShare ?: 0.0}")
                        }
                    }
                } else if (state is DashboardState.Error) {
                    Text(
                        text = (state as DashboardState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            if (state is DashboardState.Success) {
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionCard(
                        title = "Meals",
                        icon = Icons.Default.RestaurantMenu,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigateToMeal()
                    }
                    ActionCard(
                        title = "Menu",
                        icon = Icons.Default.RestaurantMenu,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigateToMenu()
                    }
                    ActionCard(
                        title = "Bazar",
                        icon = Icons.Default.ShoppingCart,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigateToBazar()
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionCard(
                        title = "History",
                        icon = Icons.Default.History,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigateToMealHistory()
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                if ((state as DashboardState.Success).role == "ROLE_ADMIN") {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        onNavigateToAdmin()
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ActionCard(
                            title = "Admin Panel",
                            icon = Icons.Default.AccountCircle,
                            color = Color(0xFFE11D48),
                            modifier = Modifier.weight(1f)
                        ) {
                            onNavigateToAdmin()
                        }
                    }
                }

                val todayMenuState = viewModel.todayMenu.collectAsState().value
                if (todayMenuState != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Today's Menu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                    )
                    TodayMenuCard(menu = todayMenuState)
                }
            }
        }
    }
}

@Composable
fun DepositInfoColumn(label: String, amount: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Text(amount, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TodayMenuCard(menu: com.messmate.android.data.menu.Menu) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (menu.lunchItems != null && menu.lunchItems.isNotEmpty()) {
                Text("Lunch", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(menu.lunchItems.joinToString(", "), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (menu.dinnerItems != null && menu.dinnerItems.isNotEmpty()) {
                Text("Dinner", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(menu.dinnerItems.joinToString(", "), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
        }
    }
}
