package com.gramflow.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gramflow.app.R
import com.gramflow.app.data.local.entity.UserEntity
import com.gramflow.app.ui.theme.*

@Composable
fun GramFlowTopBar(
    user: UserEntity?,
    onNavigateToSettings: () -> Unit,
    onEditProfileClick: () -> Unit,
    onExportLedgerClick: () -> Unit,
    onBackupDbClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val userInitial = user?.name?.firstOrNull()?.uppercase() ?: "A"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(BgCanvas)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo and Brand Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "GramFlow Logo",
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "GramFlow",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
        }

        // Profile Pill with Dropdown
        val context = LocalContext.current
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(BgSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(50.dp))
                    .clickable {
                        com.gramflow.app.util.VibrationHelper.vibrateClick(context)
                        dropdownExpanded = !dropdownExpanded
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(BgDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitial,
                        color = AccentLime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = user?.name?.split(" ")?.firstOrNull() ?: "Admin",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Global Settings") },
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = TextPrimary) },
                    onClick = {
                        dropdownExpanded = false
                        onNavigateToSettings()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Edit Profile & Security") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextPrimary) },
                    onClick = {
                        dropdownExpanded = false
                        onEditProfileClick()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Export Master Ledger (PDF)") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = TextPrimary) },
                    onClick = {
                        dropdownExpanded = false
                        onExportLedgerClick()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Encrypted DB Backup") },
                    leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = TextPrimary) },
                    onClick = {
                        dropdownExpanded = false
                        onBackupDbClick()
                    }
                )

                HorizontalDivider(color = BorderSubtle)

                DropdownMenuItem(
                    text = { Text("Sign Out", color = DangerRed) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = DangerRed) },
                    onClick = {
                        dropdownExpanded = false
                        onLogoutClick()
                    }
                )
            }
        }
    }
}
