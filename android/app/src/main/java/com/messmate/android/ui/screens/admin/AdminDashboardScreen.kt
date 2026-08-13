package com.messmate.android.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.messmate.android.data.mess.MessMemberResponse

import androidx.compose.material.icons.filled.ExitToApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAdminMenu: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val s = state) {
                is AdminState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AdminState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = s.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is AdminState.Success -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mess Members",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(onClick = onNavigateToAdminMenu) {
                            Text("Manage Menu")
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(s.members) { member ->
                            MemberCard(
                                member = member,
                                onApprove = { viewModel.approveMember(member.userId) },
                                onReject = { viewModel.rejectMember(member.userId) },
                                onPromote = { viewModel.changeRole(member.userId, "ROLE_ADMIN") }
                            )
                        }
                    }
                }
            }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = member.email, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Status: ${member.status}", fontWeight = FontWeight.SemiBold)
                Text(text = "Role: ${member.role.replace("ROLE_", "")}", color = Color.Blue)
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (member.status == "PENDING") {
                    Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.5f))) {
                        Text("Approve")
                    }
                    Button(onClick = onReject, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.5f))) {
                        Text("Reject")
                    }
                } else if (member.status == "ACTIVE" && member.role == "ROLE_USER") {
                    Button(onClick = onPromote, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Text("Make Admin")
                    }
                }
            }
        }
    }
}
