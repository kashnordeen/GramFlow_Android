package com.gramflow.app.ui.screens.transactions

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gramflow.app.data.local.entity.SaleEntity
import com.gramflow.app.ui.theme.*
import com.gramflow.app.util.VibrationHelper
import com.gramflow.app.viewmodel.TransactionFilter
import com.gramflow.app.viewmodel.TransactionsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel
) {
    val context = LocalContext.current
    val allSales by viewModel.allSales.collectAsState()
    val filteredSales by viewModel.filteredSales.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filterType.collectAsState()

    var saleToRollback by remember { mutableStateOf<SaleEntity?>(null) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.getDefault()) }

    val paidCount = allSales.count { it.balance == 0.0 }
    val loanCount = allSales.count { it.balance > 0.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Transaction Ledger", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
                Text("Sales audit trail & inventory lineage", fontSize = 13.sp, color = TextSecondary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(BgSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${allSales.size} Records",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentLime
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search by customer, notes, or amount...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionFilterPill(
                label = "All (${allSales.size})",
                isSelected = currentFilter == TransactionFilter.ALL,
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.filterType.value = TransactionFilter.ALL
                }
            )

            TransactionFilterPill(
                label = "Today",
                isSelected = currentFilter == TransactionFilter.TODAY,
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.filterType.value = TransactionFilter.TODAY
                }
            )

            TransactionFilterPill(
                label = "Paid ($paidCount)",
                isSelected = currentFilter == TransactionFilter.PAID,
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.filterType.value = TransactionFilter.PAID
                }
            )

            TransactionFilterPill(
                label = "Loans ($loanCount)",
                isSelected = currentFilter == TransactionFilter.LOAN,
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.filterType.value = TransactionFilter.LOAN
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Transaction Cards List
        if (filteredSales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No transactions match criteria", color = TextMuted, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSales, key = { it.id }) { sale ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Avatar + Customer Name + Timestamp + Sale ID
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(AccentLime.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = sale.customerName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(sale.customerName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                        Text(dateFormat.format(Date(sale.createdAt)), fontSize = 11.sp, color = TextSecondary)
                                    }
                                }

                                // Sale ID Tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BgSubtle)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "#${sale.id}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Luxury Dark Background Financial Card with Dedicated Attribute Colors
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF111312))
                                    .border(1.dp, Color(0xFF222624), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Gram Sold
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = "GRAM SOLD",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA1A8A3),
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${"%.2f".format(sale.gramsSold)}g",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentLime,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    // 2. Received Amount
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "RECEIVED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA1A8A3),
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "₹${"%.2f".format(sale.amountReceived)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    // 3. Discount
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "DISCOUNT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA1A8A3),
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = if (sale.discount > 0) "-₹${"%.2f".format(sale.discount)}" else "₹0.00",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sale.discount > 0) DangerRed else Color(0xFF6B7280),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    // 4. Status
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "STATUS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA1A8A3),
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        if (sale.balance > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(WarningAmber.copy(alpha = 0.22f))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Loan ₹${"%.2f".format(sale.balance)}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WarningAmber,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(SuccessGreen.copy(alpha = 0.22f))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Paid",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SuccessGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Optional Note Banner
                            if (!sale.comments.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BgSubtle)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Notes, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(sale.comments, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSubtle)

                            // Action Row: Billed Total + Rollback Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Billed: ₹${"%.2f".format(sale.finalAmount)}",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { saleToRollback = sale }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = "Rollback", tint = DangerRed.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rollback Sale", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DangerRed.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rollback Confirmation Dialog
    saleToRollback?.let { sale ->
        Dialog(onDismissRequest = { saleToRollback = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text("Rollback Transaction?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DangerRed)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This will delete Sale #${sale.id} for ${sale.customerName}, restore ${"%.2f".format(sale.gramsSold)}g back to the inventory batches, and reverse any loan debt.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { saleToRollback = null }, shape = RoundedCornerShape(10.dp)) {
                            Text("Cancel", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.rollbackSale(sale.id) { success, err ->
                                    if (success) {
                                        VibrationHelper.vibrateSuccess(context)
                                        Toast.makeText(context, "Sale #${sale.id} successfully rolled back!", Toast.LENGTH_SHORT).show()
                                        saleToRollback = null
                                    } else {
                                        Toast.makeText(context, err ?: "Rollback failed", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Confirm Rollback", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) AccentLime else BgSurface)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BgDark else TextSecondary
        )
    }
}
