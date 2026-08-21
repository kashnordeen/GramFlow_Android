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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val sales by viewModel.filteredSales.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filterType.collectAsState()

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
            .padding(16.dp)
    ) {
        Text("Transaction History", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
        Text("Active sales ledger with auto rollback", fontSize = 13.sp, color = TextSecondary)

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search by customer name or note...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // High-Contrast Custom Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterTabItem(
                label = "All (${sales.size})",
                isSelected = currentFilter == TransactionFilter.ALL,
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.filterType.value = TransactionFilter.ALL
                }
            )

            FilterTabItem(
                label = "Fully Paid",
                isSelected = currentFilter == TransactionFilter.PAID,
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.filterType.value = TransactionFilter.PAID
                }
            )

            FilterTabItem(
                label = "Loans",
                isSelected = currentFilter == TransactionFilter.LOAN,
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.filterType.value = TransactionFilter.LOAN
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Transactions List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sales) { s ->
                val isLoan = s.balance > 0
                val initial = s.customerName.firstOrNull()?.uppercase() ?: "C"
                val dateStr = dateFormat.format(Date(s.createdAt))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(AccentLime),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(initial, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BgDark)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(s.customerName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(dateStr, fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(if (isLoan) WarningBg else SuccessBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isLoan) "Loan (₹${"%.0f".format(s.balance)})" else "Paid",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isLoan) WarningAmber else SuccessGreen
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgSubtle)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Weight", fontSize = 11.sp, color = TextSecondary)
                                Text("${"%.2f".format(s.gramsSold)}g", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            }

                            Column {
                                Text("Billed", fontSize = 11.sp, color = TextSecondary)
                                Text("₹${"%.0f".format(s.finalAmount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                            }

                            Column {
                                Text("Received", fontSize = 11.sp, color = TextSecondary)
                                Text("₹${"%.0f".format(s.amountReceived)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                            }

                            IconButton(
                                onClick = {
                                    viewModel.rollbackSale(s.id) { success, _ ->
                                        if (success) {
                                            VibrationHelper.vibrateClick(context)
                                            Toast.makeText(context, "Sale rolled back and stock restored!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Rollback Sale", tint = DangerRed, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (!s.comments.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Note: ${s.comments}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BgSubtle)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isSelected) BgDark else BgSurface)
            .border(
                width = 1.dp,
                color = if (isSelected) BgDark else BorderSubtle,
                shape = RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) AccentLime else TextPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}
