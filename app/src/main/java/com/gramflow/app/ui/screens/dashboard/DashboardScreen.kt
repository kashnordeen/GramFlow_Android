package com.gramflow.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gramflow.app.ui.components.EcgPulseCanvas
import com.gramflow.app.ui.components.EditProfileDialog
import com.gramflow.app.ui.components.GramFlowTopBar
import com.gramflow.app.ui.components.IsometricCubeCanvas
import com.gramflow.app.ui.components.MetricLineChartCanvas
import com.gramflow.app.ui.components.PendingLoansDialog
import com.gramflow.app.ui.components.PrivacyMaskText
import com.gramflow.app.ui.theme.*
import com.gramflow.app.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToStock: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val metrics by viewModel.metrics.collectAsState()
    val user by viewModel.activeUser.collectAsState()
    val isPrivacyVisible by viewModel.isPrivacyVisible.collectAsState()

    var showDebtsModal by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when (currentHour) {
        in 4..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    val firstName = user?.name?.split(" ")?.firstOrNull() ?: "Admin"
    val isLowStock = metrics.totalStock in 0.001..4.999
    val isZeroStock = metrics.totalStock == 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
    ) {
        // Header
        GramFlowTopBar(
            user = user,
            onNavigateToSettings = onNavigateToSettings,
            onEditProfileClick = { showEditProfileDialog = true },
            onExportLedgerClick = { /* Export */ },
            onBackupDbClick = { /* Backup */ },
            onLogoutClick = onLogout
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Hero Greeting
            Text(
                text = "$greeting, $firstName! 👋",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Here's what's happening with your business today.",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Low Stock Warning Card
            if (isLowStock || isZeroStock) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isZeroStock) Color(0xFF1C1212) else BgCharcoal)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isZeroStock) DangerRed else AccentLime,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isZeroStock) "OUT OF STOCK" else "LOW STOCK WARNING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isZeroStock) DangerRed else AccentLime,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Only ${"%.2f".format(metrics.totalStock)}g remaining",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }

                        EcgPulseCanvas(
                            modifier = Modifier
                                .width(70.dp)
                                .height(26.dp),
                            color = if (isZeroStock) DangerRed else AccentLime
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2x2 KPI Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Total Stock (Dark Visual Anchor)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(170.dp)
                        .clickable { onNavigateToStock() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCardDark)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = AccentLime,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "STOCK",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.7f),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Text("Vault", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "%.2f".format(metrics.totalStock),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentLime,
                                    fontFamily = FontFamily.SansSerif
                                )
                                Text("g", fontSize = 14.sp, color = AccentLime, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                            }

                            Text(
                                text = "Available Stock",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        // Isometric Cube Graphic
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp)
                        ) {
                            IsometricCubeCanvas(modifier = Modifier.size(54.dp))
                        }
                    }
                }

                // Card 2: Sales Today
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(170.dp)
                        .clickable { onNavigateToTransactions() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurface)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SALES TODAY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Icon(
                                    imageVector = if (isPrivacyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Privacy",
                                    tint = TextMuted,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.togglePrivacy() }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("₹", fontSize = 14.sp, color = TextMuted)
                                PrivacyMaskText(
                                    text = "%.0f".format(metrics.salesTodayAmount),
                                    isVisible = isPrivacyVisible,
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }

                            Text(
                                text = "${metrics.salesTodayCount} sales (${"%.1f".format(metrics.salesTodayGrams)}g)",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                            MetricLineChartCanvas(modifier = Modifier.fillMaxWidth().height(36.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 3: Lifetime Profit
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(170.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CurrencyRupee,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PROFIT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("₹", fontSize = 14.sp, color = TextMuted)
                                PrivacyMaskText(
                                    text = "%.0f".format(metrics.totalProfit),
                                    isVisible = isPrivacyVisible,
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }

                            Text(
                                text = "FIFO cost deducted",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        // Bar graphic
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            listOf(0.2f, 0.4f, 0.35f, 0.6f, 0.5f, 0.75f, 0.85f, 1f).forEachIndexed { idx, frac ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height((28 * frac).dp)
                                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                        .background(if (idx >= 6) AccentLime else BgSubtle)
                                )
                            }
                        }
                    }
                }

                // Card 4: Pending Loans
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(170.dp)
                        .clickable { showDebtsModal = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.People,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LOANS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("₹", fontSize = 14.sp, color = TextMuted)
                                PrivacyMaskText(
                                    text = "%.0f".format(metrics.totalLoan),
                                    isVisible = isPrivacyVisible,
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }
                        }

                        // Action Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(BgDark)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${metrics.customersWithLoans.size} active debts",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentLime
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                Icons.Default.ArrowOutward,
                                contentDescription = null,
                                tint = AccentLime,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Debts Dialog
    if (showDebtsModal) {
        PendingLoansDialog(
            totalLoan = metrics.totalLoan,
            customersWithLoans = metrics.customersWithLoans,
            onDismiss = { showDebtsModal = false }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            user = user,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName, currentPass, newPass ->
                viewModel.updateProfile(newName, currentPass, newPass) { success, _ ->
                    if (success) showEditProfileDialog = false
                }
            }
        )
    }
}
