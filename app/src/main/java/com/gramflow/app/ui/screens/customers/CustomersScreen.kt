package com.gramflow.app.ui.screens.customers

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.core.net.toUri
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
import com.gramflow.app.data.repository.CustomerLedgerStatement
import com.gramflow.app.ui.theme.*
import com.gramflow.app.util.VibrationHelper
import com.gramflow.app.viewmodel.CustomerFilter
import com.gramflow.app.viewmodel.CustomersViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomersScreen(
    viewModel: CustomersViewModel
) {
    val context = LocalContext.current
    val allCustomers by viewModel.customers.collectAsState()
    val filteredCustomers by viewModel.filteredCustomers.collectAsState()
    val totalDebt by viewModel.totalOutstandingLoan.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filterType.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedForPayment by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedForOverride by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedForEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedForLedger by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerLedgerData by remember { mutableStateOf<CustomerLedgerStatement?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    val debtCount = allCustomers.count { (it.oldLoan + it.totalLoan) > 0 }
    val settledCount = allCustomers.count { (it.oldLoan + it.totalLoan) == 0.0 }

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
                Text("Customers", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
                Text("Client registry & debt balances", fontSize = 13.sp, color = TextSecondary)
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentLime,
                    contentColor = BgDark
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Customer", fontWeight = FontWeight.Bold, color = BgDark, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero KPI Outstanding Debt Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
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
                    Text("TOTAL OUTSTANDING LOANS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${"%.2f".format(totalDebt ?: 0.0)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarningAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$debtCount Active Debtors",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search customer name or phone...") },
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
            CustomerFilterPill(
                label = "All (${allCustomers.size})",
                isSelected = currentFilter == CustomerFilter.ALL,
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.filterType.value = CustomerFilter.ALL
                }
            )

            CustomerFilterPill(
                label = "Has Debt ($debtCount)",
                isSelected = currentFilter == CustomerFilter.DEBT,
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.filterType.value = CustomerFilter.DEBT
                }
            )

            CustomerFilterPill(
                label = "Settled ($settledCount)",
                isSelected = currentFilter == CustomerFilter.SETTLED,
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.filterType.value = CustomerFilter.SETTLED
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Customer Cards List
        if (filteredCustomers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No customers match criteria", color = TextMuted, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCustomers, key = { it.id }) { customer ->
                    val combinedDebt = customer.oldLoan + customer.totalLoan

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedForLedger = customer
                                viewModel.getLedgerStatement(customer.id) { stmt ->
                                    customerLedgerData = stmt
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface),
                        border = if (combinedDebt > 0) BorderStroke(1.dp, WarningAmber.copy(alpha = 0.35f)) else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Avatar + Name + Phone + Debt Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Customer Initial Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (combinedDebt > 0) WarningBg else BadgeGreenBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = customer.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = if (combinedDebt > 0) WarningAmber else BadgeGreenText
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                        if (!customer.phone.isNullOrBlank()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    val intent = Intent(Intent.ACTION_DIAL, "tel:${customer.phone}".toUri())
                                                    context.startActivity(intent)
                                                }
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = "Call", tint = BrandGreen, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(customer.phone, fontSize = 12.sp, color = TextSecondary)
                                            }
                                        } else {
                                            Text("No phone linked", fontSize = 11.sp, color = TextMuted)
                                        }
                                    }
                                }

                                // Total Debt Badge
                                Column(horizontalAlignment = Alignment.End) {
                                    if (combinedDebt > 0) {
                                        Text(
                                            text = "₹${"%.2f".format(combinedDebt)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarningAmber,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text("Outstanding Debt", fontSize = 10.sp, color = TextSecondary)
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(BadgeGreenBg)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("No Debt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BadgeGreenText)
                                        }
                                    }
                                }
                            }

                            // Sub-breakdown if loan exists
                            if (combinedDebt > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BgSubtle)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Legacy Debt: ₹${"%.2f".format(customer.oldLoan)}", fontSize = 11.sp, color = TextSecondary)
                                    Text("Sales Debt: ₹${"%.2f".format(customer.totalLoan)}", fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSubtle)

                            // Action Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Statement button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedForLedger = customer
                                            viewModel.getLedgerStatement(customer.id) { stmt ->
                                                customerLedgerData = stmt
                                            }
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = "Statement", tint = BrandGreen, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Statement", fontSize = 12.sp, color = BrandGreen, fontWeight = FontWeight.SemiBold)
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Settle / Pay button
                                    if (combinedDebt > 0) {
                                        Button(
                                            onClick = { selectedForPayment = customer },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AccentLime,
                                                contentColor = BgDark
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Settle Debt", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BgDark)
                                        }
                                    }

                                    // Override Loans
                                    IconButton(
                                        onClick = { selectedForOverride = customer },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = "Adjust Debt", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }

                                    // Edit Customer Info
                                    IconButton(
                                        onClick = { selectedForEdit = customer },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }

                                    // Delete Customer
                                    IconButton(
                                        onClick = { customerToDelete = customer },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. Add Customer Dialog
    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, oldLoan ->
                viewModel.registerCustomer(name, phone, oldLoan) { success, err ->
                    if (success) {
                        Toast.makeText(context, "Customer registered!", Toast.LENGTH_SHORT).show()
                        showAddDialog = false
                    } else {
                        Toast.makeText(context, err ?: "Failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // 2. Edit Customer Info Dialog
    selectedForEdit?.let { cust ->
        EditCustomerDialog(
            customer = cust,
            onDismiss = { selectedForEdit = null },
            onConfirm = { name, phone ->
                viewModel.updateCustomer(cust.id, name, phone) { success, err ->
                    if (success) {
                        Toast.makeText(context, "Customer details updated!", Toast.LENGTH_SHORT).show()
                        selectedForEdit = null
                    } else {
                        Toast.makeText(context, err ?: "Failed to update", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // 3. Record Payment Dialog
    selectedForPayment?.let { cust ->
        RecordPaymentDialog(
            customer = cust,
            onDismiss = { selectedForPayment = null },
            onConfirm = { amount ->
                viewModel.recordPayment(cust.id, amount) { success, err ->
                    if (success) {
                        VibrationHelper.vibrateSuccess(context)
                        Toast.makeText(context, "Payment of ₹${amount} recorded!", Toast.LENGTH_SHORT).show()
                        selectedForPayment = null
                    } else {
                        Toast.makeText(context, err ?: "Payment failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // 4. Override Customer Loans Dialog
    selectedForOverride?.let { cust ->
        OverrideLoanDialog(
            customer = cust,
            onDismiss = { selectedForOverride = null },
            onConfirm = { oldL, appL ->
                viewModel.overrideLoan(cust.id, oldL, appL) { success, err ->
                    if (success) {
                        Toast.makeText(context, "Ledger balance updated!", Toast.LENGTH_SHORT).show()
                        selectedForOverride = null
                    } else {
                        Toast.makeText(context, err ?: "Failed to update", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // 5. Customer Ledger Statement Dialog
    selectedForLedger?.let { cust ->
        CustomerLedgerStatementDialog(
            customer = cust,
            statement = customerLedgerData,
            onDismiss = {
                selectedForLedger = null
                customerLedgerData = null
            }
        )
    }

    // 6. Delete Confirmation Dialog
    customerToDelete?.let { cust ->
        DeleteCustomerConfirmDialog(
            customer = cust,
            onDismiss = { customerToDelete = null },
            onConfirm = {
                viewModel.deleteCustomer(cust.id) { success, err ->
                    if (success) {
                        Toast.makeText(context, "Customer deleted and stock restored", Toast.LENGTH_SHORT).show()
                        customerToDelete = null
                    } else {
                        Toast.makeText(context, err ?: "Delete failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun CustomerFilterPill(
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

@Composable
private fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var oldLoanText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            modifier = Modifier.fillMaxWidth().imePadding()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Register New Customer", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Name *", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. John Doe") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Phone (Optional)", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("+91 98765 43210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Initial / Legacy Loan (₹)", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = oldLoanText,
                    onValueChange = { oldLoanText = it },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name, phone.takeIf { it.isNotBlank() }, oldLoanText.toDoubleOrNull() ?: 0.0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentLime, contentColor = BgDark),
                        shape = RoundedCornerShape(10.dp),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Register", fontWeight = FontWeight.Bold, color = BgDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditCustomerDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phone ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            modifier = Modifier.fillMaxWidth().imePadding()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Edit Customer Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Name *", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Phone Number", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name, phone.takeIf { it.isNotBlank() })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentLime, contentColor = BgDark),
                        shape = RoundedCornerShape(10.dp),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold, color = BgDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordPaymentDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val totalDebt = customer.oldLoan + customer.totalLoan
    var paymentText by remember { mutableStateOf(totalDebt.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            modifier = Modifier.fillMaxWidth().imePadding()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Record Debt Repayment", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Customer: ${customer.name} · Current Debt: ₹${"%.2f".format(totalDebt)}",
                    fontSize = 12.sp,
                    color = WarningAmber
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Payment Amount Received (₹) *", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = paymentText,
                    onValueChange = { paymentText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = paymentText.toDoubleOrNull() ?: 0.0
                            if (amount > 0) onConfirm(amount)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentLime, contentColor = BgDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply Payment", fontWeight = FontWeight.Bold, color = BgDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverrideLoanDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    var oldLoanText by remember { mutableStateOf(customer.oldLoan.toInt().toString()) }
    var totalLoanText by remember { mutableStateOf(customer.totalLoan.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            modifier = Modifier.fillMaxWidth().imePadding()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Adjust Debt Ledger", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Manually correct ledger debt for ${customer.name}", fontSize = 12.sp, color = TextSecondary)

                Spacer(modifier = Modifier.height(14.dp))

                Text("Legacy / Previous Loan (₹)", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = oldLoanText,
                    onValueChange = { oldLoanText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("App Sales Loan (₹)", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = totalLoanText,
                    onValueChange = { totalLoanText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val oL = oldLoanText.toDoubleOrNull() ?: 0.0
                            val tL = totalLoanText.toDoubleOrNull() ?: 0.0
                            onConfirm(oL, tL)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentLime, contentColor = BgDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update Balances", fontWeight = FontWeight.Bold, color = BgDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerLedgerStatementDialog(
    customer: CustomerEntity,
    statement: CustomerLedgerStatement?,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                        Text("Customer Statement & Order History", fontSize = 12.sp, color = TextSecondary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // KPI Overview
                statement?.let { stmt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BgSubtle)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Grams", fontSize = 11.sp, color = TextSecondary)
                            Text("${"%.2f".format(stmt.totalPurchasedGrams)}g", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        }
                        Column {
                            Text("Total Spent", fontSize = 11.sp, color = TextSecondary)
                            Text("₹${"%.2f".format(stmt.totalPurchasedAmount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Current Debt", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                "₹${"%.2f".format(stmt.currentBalance)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (stmt.currentBalance > 0) WarningAmber else SuccessGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Transaction Records (${stmt.sales.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (stmt.sales.isEmpty()) {
                        Text("No sale transactions recorded yet.", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(vertical = 10.dp))
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(stmt.sales) { sale ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BgSubtle)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${"%.2f".format(sale.gramsSold)}g · ₹${"%.2f".format(sale.finalAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text(
                                            text = if (sale.balance > 0) "Loan: ₹${"%.2f".format(sale.balance)}" else "Paid",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (sale.balance > 0) WarningAmber else SuccessGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dateFormat.format(Date(sale.createdAt)),
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val context = LocalContext.current
                    Button(
                        onClick = {
                            com.gramflow.app.util.PdfGenerator.generateCustomerLedgerPdf(context, stmt)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentLime,
                            contentColor = BgDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Statement (PDF)", fontWeight = FontWeight.Bold, color = BgDark, fontSize = 13.sp)
                    }
                } ?: run {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("Loading statement data...", fontSize = 13.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteCustomerConfirmDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Delete Customer?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DangerRed)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Are you sure you want to delete ${customer.name}? All associated sales will be rolled back and stock will be restored automatically.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Delete & Rollback", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
