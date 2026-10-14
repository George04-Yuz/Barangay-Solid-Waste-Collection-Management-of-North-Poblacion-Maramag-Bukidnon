package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.AboutProjectDialog
import com.example.ui.components.NotificationSheet
import com.example.ui.components.PersonaSwitcherBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ResidentDashboardScreen
import com.example.ui.screens.WorkerDashboardScreen
import com.example.ui.theme.CleanGreenPrimary
import com.example.ui.theme.CleanTrackTheme
import com.example.ui.viewmodel.CleanTrackViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanTrackTheme {
                CleanTrackApp()
            }
        }
    }
}

@Composable
fun CleanTrackApp(viewModel: CleanTrackViewModel = viewModel()) {
    val currentPersona by viewModel.currentPersona.collectAsState()
    val residentPurok by viewModel.residentPurok.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifCount by viewModel.unreadNotificationCount.collectAsState()
    val showNotifCenter by viewModel.showNotificationCenter.collectAsState()
    val transientBanner by viewModel.transientBanner.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }

    // Intercept back button if notification drawer is open
    BackHandler(enabled = showNotifCenter) {
        viewModel.toggleNotificationCenter(false)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            PersonaSwitcherBar(
                currentPersona = currentPersona,
                selectedPurok = residentPurok,
                unreadNotificationCount = unreadNotifCount,
                onPersonaSelected = { viewModel.setPersona(it) },
                onPurokSelected = { viewModel.setResidentPurok(it) },
                onOpenNotifications = { viewModel.toggleNotificationCenter(true) },
                onOpenAbout = { showAboutDialog = true },
                availablePersonas = viewModel.demoPersonas
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Role Content
            when (currentPersona.role) {
                UserRole.ADMIN -> AdminDashboardScreen(viewModel = viewModel)
                UserRole.WORKER -> WorkerDashboardScreen(viewModel = viewModel)
                UserRole.RESIDENT -> ResidentDashboardScreen(viewModel = viewModel)
            }

            // Floating Transient Alert Banner
            AnimatedVisibility(
                visible = transientBanner != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                transientBanner?.let { message ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF263238),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transient_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFA5D6A7),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = message,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearBanner() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // In-App Notification Center Bottom Sheet
    if (showNotifCenter) {
        NotificationSheet(
            notifications = notifications,
            onClose = { viewModel.toggleNotificationCenter(false) },
            onMarkAsRead = { viewModel.markNotificationRead(it) },
            onMarkAllAsRead = { viewModel.markAllNotificationsRead() }
        )
    }

    // About BSIT Capstone Project Dialog
    if (showAboutDialog) {
        AboutProjectDialog(viewModel = viewModel, onDismiss = { showAboutDialog = false })
    }
}
