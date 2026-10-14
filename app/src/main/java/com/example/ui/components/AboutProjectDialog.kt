package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CleanGreenContainer
import com.example.ui.theme.CleanGreenPrimary
import com.example.ui.theme.StatusCompletedColor
import com.example.ui.viewmodel.CleanTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutProjectDialog(
    viewModel: CleanTrackViewModel,
    onDismiss: () -> Unit
) {
    val isProductionBackend by viewModel.isProductionBackend.collectAsState()
    val backendUrl by viewModel.backendUrl.collectAsState()

    var customUrlInput by remember { mutableStateOf(backendUrl) }
    var showUrlEdit by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CleanGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = CleanGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text("North Poblacion CleanTrack", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Release v1.0.0 (Build 1)", fontSize = 11.sp, color = CleanGreenPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Official Version & Package ID
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Package Identifier:", fontSize = 11.sp, color = Color.Gray)
                            Text("ph.northpoblacion.cleantrack", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Version:", fontSize = 11.sp, color = Color.Gray)
                            Text("1.0.0 (Build 1)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CleanGreenPrimary)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Release Asset:", fontSize = 11.sp, color = Color.Gray)
                            Text("North-Poblacion-CleanTrack-v1.0.0.apk", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Backend Connectivity Configuration
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CleanGreenContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = CleanGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Backend Server Mode",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CleanGreenPrimary
                                )
                            }

                            Switch(
                                checked = isProductionBackend,
                                onCheckedChange = { checked ->
                                    viewModel.toggleBackendMode(checked, customUrlInput)
                                },
                                modifier = Modifier.testTag("switch_backend_mode")
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isProductionBackend) {
                                "Connected to Production HTTPS Endpoint:\n$backendUrl"
                            } else {
                                "Embedded Capstone Live Demo Mode (Pre-seeded with all 18 Puroks of North Poblacion for offline evaluation)."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp
                        )

                        if (isProductionBackend) {
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { showUrlEdit = !showUrlEdit },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(if (showUrlEdit) "Hide Server URL Config" else "Configure Server URL", fontSize = 11.sp)
                            }

                            if (showUrlEdit) {
                                OutlinedTextField(
                                    value = customUrlInput,
                                    onValueChange = { customUrlInput = it },
                                    label = { Text("HTTPS Server Base URL") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        viewModel.toggleBackendMode(true, customUrlInput)
                                        showUrlEdit = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Apply HTTPS Endpoint", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Project Title Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Full Official Project Title:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleanGreenPrimary
                        )
                        Text(
                            text = "Cross-Platform Barangay Solid Waste Collection Management and Household Notification System for North Poblacion, Maramag, Bukidnon",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                    }
                }

                Text(
                    text = "Project Scope & Coverage:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Built specifically for Barangay North Poblacion, Municipality of Maramag, Province of Bukidnon, Philippines. Serves all 18 official puroks and sub-puroks:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "1. Purok 1  •  2. Purok 1A  •  3. Purok 1B\n4. Purok 2  •  5. Purok 2A  •  6. Purok 3\n7. Purok 4  •  8. Purok 5  •  9. Purok 5A\n10. Purok 5B  •  11. Purok 6  •  12. Purok 6A\n13. Purok 7  •  14. Purok 8  •  15. Purok 9\n16. Purok 9A  •  17. Purok 9B  •  18. Purok 10",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp),
                        lineHeight = 16.sp
                    )
                }

                Text(
                    text = "Three Demonstration Roles:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "• Administrator: Schedule dispatching, 18 puroks oversight, complaint resolution, and CSV analytics.\n• Worker/Driver: Route assignment tracking, 'Start' & 'Mark Completed' real-time status transitions with timestamps, and delay reporting.\n• Resident: Live pickup tracking, next scheduled collection, missed waste reporting, and in-app notifications.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Text(
                    text = "Legal & Privacy Standards:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Compliant with Republic Act No. 9003 (Ecological Solid Waste Management Act of 2000) and Republic Act No. 10173 (Philippine Data Privacy Act of 2012). Resident private phone numbers and addresses are restricted to authorized administrators.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary),
                modifier = Modifier.testTag("btn_close_about")
            ) {
                Text("Close")
            }
        }
    )
}
