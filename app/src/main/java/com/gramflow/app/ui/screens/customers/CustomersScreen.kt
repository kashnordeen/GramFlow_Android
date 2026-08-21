package com.gramflow.app.ui.screens.customers

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gramflow.app.data.local.entity.CustomerEntity
import com.gramflow.app.ui.theme.*
import com.gramflow.app.viewmodel.CustomersViewModel

@Composable
fun CustomersScreen(
    viewModel: CustomersViewModel
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    val totalDebt by viewModel.totalOutstandingLoan.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedForPayment by remember { mutableStateOf<CustomerEntity?>(null) }
    var paymentAmountText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
            .padding(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Customers", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
                Text("Ledger balances & repayments", fontSize = 13.sp, color = TextSecondary)
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BgDark,
                    contentColor = AccentLime
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AccentLime, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add", fontWeight = FontWeight.Bold, color = AccentLime, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total Debt Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL OUTSTANDING DEBT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
                    Text(
                        text = "₹${"%.2f".format(totalDebt ?: 0.0)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if ((totalDebt ?: 0.0) > 0) WarningAmber else SuccessGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgSubtle)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("${customers.size} registered", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Customer List
        Text("Directory & Outstandings", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(customers) { c ->
                val combinedDebt = c.oldLoan + c.totalLoan
                val initial = c.name.firstOrNull()?.uppercase() ?: "C"

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
                                    Text(c.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(c.phone ?: "No contact", fontSize = 12.sp, color = TextMuted)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(if (combinedDebt > 0) WarningBg else SuccessBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (combinedDebt > 0) "₹${"%.0f".format(combinedDebt)}" else "Paid",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (combinedDebt > 0) WarningAmber else SuccessGreen
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgSubtle)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (c.oldLoan > 0) {
                                    Text("Legacy: ₹${"%.0f".format(c.oldLoan)}", fontSize = 11.sp, color = DangerRed)
                                }
                                if (c.totalLoan > 0) {
                                    Text("App: ₹${"%.0f".format(c.totalLoan)}", fontSize = 11.sp, color = WarningAmber)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (combinedDebt > 0) {
                                    Button(
                                        onClick = {
                                            selectedForPayment = c
                                            paymentAmountText = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AccentLime,
                                            contentColor = BgDark
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Pay", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BgDark)
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.deleteCustomer(c.id) { success, _ ->
                                            if (success) Toast.makeText(context, "Customer deleted & stock restored", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newOldLoan by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Register Customer", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newOldLoan,
                        onValueChange = { newOldLoan = it },
                        label = { Text("Legacy Debt (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                viewModel.registerCustomer(newName, newPhone, newOldLoan.toDoubleOrNull() ?: 0.0) { success, err ->
                                    if (success) {
                                        showAddDialog = false
                                        Toast.makeText(context, "Customer registered!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, err ?: "Error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentLime,
                                contentColor = BgDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold, color = BgDark)
                        }
                    }
                }
            }
        }
    }

    // Payment Dialog
    if (selectedForPayment != null) {
        val targetCustomer = selectedForPayment!!
        Dialog(onDismissRequest = { selectedForPayment = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Record Payment", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("For ${targetCustomer.name}", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text("Payment Amount (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { selectedForPayment = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val amt = paymentAmountText.toDoubleOrNull() ?: 0.0
                                viewModel.recordPayment(targetCustomer.id, amt) { success, err ->
                                    if (success) {
                                        selectedForPayment = null
                                        Toast.makeText(context, "Payment recorded!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, err ?: "Payment failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentLime,
                                contentColor = BgDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm", fontWeight = FontWeight.Bold, color = BgDark)
                        }
                    }
                }
            }
        }
    }
}
