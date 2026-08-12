package com.messmate.android.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

val adminDayNames = arrayOf("Unknown", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminMenuViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    var selectedDay by remember { mutableStateOf(1) } // Default Monday

    // Form states
    var lunchText by remember { mutableStateOf("") }
    var dinnerText by remember { mutableStateOf("") }
    var isPublished by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state, selectedDay) {
        if (state is AdminMenuState.Success) {
            val menus = (state as AdminMenuState.Success).menus
            val m = menus.find { it.dayOfWeek == selectedDay }
            if (m != null) {
                lunchText = m.lunchItems?.joinToString(", ") ?: ""
                dinnerText = m.dinnerItems?.joinToString(", ") ?: ""
                isPublished = m.isPublished
            } else {
                lunchText = ""
                dinnerText = ""
                isPublished = false
            }
        }
    }

    LaunchedEffect(saveState) {
        if (saveState is AdminMenuState.Success) {
            snackbarHostState.showSnackbar("Menu saved successfully!")
            viewModel.resetSaveState()
        } else if (saveState is AdminMenuState.Error) {
            snackbarHostState.showSnackbar((saveState as AdminMenuState.Error).message)
            viewModel.resetSaveState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Day Selector
                ScrollableTabRow(
                    selectedTabIndex = selectedDay - 1,
                    edgePadding = 0.dp,
                    indicator = { },
                    divider = { }
                ) {
                    (1..7).forEach { day ->
                        val selected = selectedDay == day
                        Tab(
                            selected = selected,
                            onClick = { selectedDay = day },
                            text = { Text(adminDayNames[day], fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .background(
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                if (state is AdminMenuState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    OutlinedTextField(
                        value = lunchText,
                        onValueChange = { lunchText = it },
                        label = { Text("Lunch Items (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = dinnerText,
                        onValueChange = { dinnerText = it },
                        label = { Text("Dinner Items (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Published", fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isPublished,
                            onCheckedChange = { isPublished = it }
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.upsertMenu(selectedDay, lunchText, dinnerText, isPublished) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = saveState !is AdminMenuState.Loading
                    ) {
                        if (saveState is AdminMenuState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Save Menu")
                        }
                    }
                }
            }
        }
    }
}
