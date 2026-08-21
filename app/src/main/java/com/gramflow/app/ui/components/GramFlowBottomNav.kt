package com.gramflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gramflow.app.ui.navigation.Screen
import com.gramflow.app.ui.theme.*
import com.gramflow.app.util.VibrationHelper

@Composable
fun GramFlowBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
    ) {
        // Bottom Bar Surface
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(68.dp)
                .background(BgSurface)
        ) {
            HorizontalDivider(
                color = BorderSubtle,
                thickness = 1.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(67.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = Icons.Default.Dashboard,
                    label = "Dashboard",
                    isSelected = currentRoute == Screen.Dashboard.route,
                    onClick = {
                        VibrationHelper.vibrateClick(context)
                        onNavigate(Screen.Dashboard.route)
                    }
                )

                NavItem(
                    icon = Icons.Default.ListAlt,
                    label = "History",
                    isSelected = currentRoute == Screen.Transactions.route,
                    onClick = {
                        VibrationHelper.vibrateClick(context)
                        onNavigate(Screen.Transactions.route)
                    }
                )

                // Center Spacer for floating FAB cutout
                Box(modifier = Modifier.size(56.dp))

                NavItem(
                    icon = Icons.Default.People,
                    label = "Customers",
                    isSelected = currentRoute == Screen.Customers.route,
                    onClick = {
                        VibrationHelper.vibrateClick(context)
                        onNavigate(Screen.Customers.route)
                    }
                )

                NavItem(
                    icon = Icons.Default.Inventory,
                    label = "Stock",
                    isSelected = currentRoute == Screen.StockVault.route,
                    onClick = {
                        VibrationHelper.vibrateClick(context)
                        onNavigate(Screen.StockVault.route)
                    }
                )
            }
        }

        // Floating Center Plus Action Button with circular backing cutout (no crossing line)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(62.dp)
                .clip(CircleShape)
                .background(BgCanvas),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    onNavigate(Screen.AddSale.route)
                },
                shape = CircleShape,
                containerColor = AccentLime,
                contentColor = BgDark,
                modifier = Modifier
                    .size(52.dp)
                    .shadow(10.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Record Sale",
                    tint = BgDark,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) TextPrimary else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TextPrimary else TextMuted
        )
    }
}
