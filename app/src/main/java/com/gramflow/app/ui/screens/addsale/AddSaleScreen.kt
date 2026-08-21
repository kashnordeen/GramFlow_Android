package com.gramflow.app.ui.screens.addsale

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gramflow.app.ui.theme.*
import com.gramflow.app.viewmodel.AddSaleViewModel
import kotlinx.coroutines.delay

@Composable
fun AddSaleScreen(
    viewModel: AddSaleViewModel,
    onSaleCompleted: () -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    val totalStock by viewModel.totalStock.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    val selectedCustomerId by viewModel.selectedCustomerId.collectAsState()
    val gramsText by viewModel.gramsText.collectAsState()
    val discountText by viewModel.discountText.collectAsState()
    val amountReceivedText by viewModel.amountReceivedText.collectAsState()
    val selectedBatchId by viewModel.selectedBatchId.collectAsState()
    val commentsText by viewModel.commentsText.collectAsState()

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var batchDropdownExpanded by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }

    val (gross, finalAmount, balance) = viewModel.calculateComputation()
    val selectedCustomer = customers.find { it.id == selectedCustomerId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Record a Sale",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = TextPrimary
        )
        Text(
            text = "FIFO deductions executed automatically.",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Main Sale Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Customer Selector
                Text("Customer *", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderInput, RoundedCornerShape(12.dp))
                        .clickable { customerDropdownExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCustomer?.name ?: "-- Select Customer --",
                            color = if (selectedCustomer != null) TextPrimary else TextMuted,
                            fontSize = 14.sp
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextMuted)
                    }

                    DropdownMenu(
                        expanded = customerDropdownExpanded,
                        onDismissRequest = { customerDropdownExpanded = false }
                    ) {
                        customers.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.name} ${if (c.phone != null) "(${c.phone})" else ""}") },
                                onClick = {
                                    viewModel.selectedCustomerId.value = c.id
                                    customerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Grams Sold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Grams Sold *", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                    Text("Vault: ${"%.2f".format(totalStock)}g", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = gramsText,
                    onValueChange = { viewModel.gramsText.value = it },
                    placeholder = { Text("e.g. 0.30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Discount
                Text("Discount (₹)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { viewModel.discountText.value = it },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Received
                Text("Amount Received (₹) *", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountReceivedText,
                    onValueChange = { viewModel.amountReceivedText.value = it },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Batch Selection
                Text("Batch Assignment", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderInput, RoundedCornerShape(12.dp))
                        .clickable { batchDropdownExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    val activeBatch = batches.find { it.id == selectedBatchId }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activeBatch != null) "Batch #${activeBatch.id} (${activeBatch.remainingGrams}g)" else "Auto-Pilot (FIFO / Oldest First)",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextMuted)
                    }

                    DropdownMenu(
                        expanded = batchDropdownExpanded,
                        onDismissRequest = { batchDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Auto-Pilot (FIFO / Oldest First)") },
                            onClick = {
                                viewModel.selectedBatchId.value = null
                                batchDropdownExpanded = false
                            }
                        )
                        batches.filter { it.remainingGrams > 0 }.forEach { b ->
                            DropdownMenuItem(
                                text = { Text("Batch #${b.id} (${b.remainingGrams}g remaining)") },
                                onClick = {
                                    viewModel.selectedBatchId.value = b.id
                                    batchDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Comments
                Text("Notes / Comments", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = commentsText,
                    onValueChange = { viewModel.commentsText.value = it },
                    placeholder = { Text("Special transaction notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Computation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Live Computation", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gross Output", fontSize = 13.sp, color = TextSecondary)
                    Text("₹${"%.2f".format(gross)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Discount Adjusted", fontSize = 13.sp, color = TextSecondary)
                    Text("-₹${"%.2f".format(discountText.toDoubleOrNull() ?: 0.0)}", fontSize = 14.sp, color = DangerRed, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSubtle)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Final Billing", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("₹${"%.2f".format(finalAmount)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSubtle)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Remaining Balance (Loan)", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = "₹${"%.2f".format(balance)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (balance > 0) WarningAmber else SuccessGreen
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // High-Contrast Neon Lime Finalize Sale Button
                Button(
                    onClick = {
                        viewModel.submitSale { success, error ->
                            if (success) {
                                com.gramflow.app.util.VibrationHelper.vibrateSuccess(context)
                                showSuccessModal = true
                            } else {
                                Toast.makeText(context, error ?: "Transaction failed", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentLime,
                        contentColor = BgDark,
                        disabledContainerColor = AccentLime.copy(alpha = 0.5f),
                        disabledContentColor = BgDark.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (isSubmitting) "Processing FIFO..." else "Finalize Sale",
                        fontWeight = FontWeight.Bold,
                        color = BgDark,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Animated Success Modal
    if (showSuccessModal) {
        val scaleAnim = remember { Animatable(0.2f) }

        LaunchedEffect(Unit) {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            delay(1800)
            showSuccessModal = false
            onSaleCompleted()
        }

        Dialog(onDismissRequest = {
            showSuccessModal = false
            onSaleCompleted()
        }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scaleAnim.value)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing Checkmark Circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentLime)
                            .shadow(12.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = BgDark,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Sale Processed!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )

                    Text(
                        text = "Stock batches deducted & ledger updated.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    // Summary details pill
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BgSubtle)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Customer", fontSize = 12.sp, color = TextSecondary)
                            Text(selectedCustomer?.name ?: "Customer", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Weight Sold", fontSize = 12.sp, color = TextSecondary)
                            Text("${gramsText}g", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Billed Amount", fontSize = 12.sp, color = TextSecondary)
                            Text("₹${"%.2f".format(finalAmount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showSuccessModal = false
                            onSaleCompleted()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BgDark,
                            contentColor = AccentLime
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Return to Dashboard", fontWeight = FontWeight.Bold, color = AccentLime)
                    }
                }
            }
        }
    }
}
