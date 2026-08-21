package com.gramflow.app.ui.screens.stock

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.gramflow.app.ui.theme.*
import com.gramflow.app.viewmodel.StockViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockVaultScreen(
    viewModel: StockViewModel
) {
    val context = LocalContext.current
    val batches by viewModel.batches.collectAsState()
    val totalStock by viewModel.totalStock.collectAsState()

    var ingestGramsText by remember { mutableStateOf("") }
    var isIngesting by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
            .padding(16.dp)
    ) {
        // Header
        Text("Stock Vault", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
        Text("Batch inventory & FIFO lifecycle", fontSize = 13.sp, color = TextSecondary)

        Spacer(modifier = Modifier.height(14.dp))

        // Net Available Stock Dark Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgCardDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("NET AVAILABLE STOCK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%.2f".format(totalStock),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentLime,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text("g", fontSize = 16.sp, color = AccentLime, modifier = Modifier.padding(bottom = 3.dp, start = 2.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1F2221))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("FIFO Active", fontSize = 12.sp, color = AccentLime, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Ingest Batch Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ingest New Stock Batch", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = ingestGramsText,
                        onValueChange = { ingestGramsText = it },
                        placeholder = { Text("Grams e.g. 10.50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val grams = ingestGramsText.toDoubleOrNull() ?: 0.0
                            if (grams <= 0.0) {
                                Toast.makeText(context, "Enter valid gram weight", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isIngesting = true
                            viewModel.ingestBatch(grams) { success, err ->
                                isIngesting = false
                                if (success) {
                                    ingestGramsText = ""
                                    Toast.makeText(context, "Batch ingested to vault!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, err ?: "Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.height(54.dp),
                        enabled = !isIngesting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentLime,
                            contentColor = BgDark,
                            disabledContainerColor = AccentLime.copy(alpha = 0.5f),
                            disabledContentColor = BgDark.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isIngesting) "..." else "Ingest",
                            fontWeight = FontWeight.Bold,
                            color = BgDark,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Batches List
        Text("Historical Batches Master-list", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(batches) { b ->
                val isExhausted = b.remainingGrams == 0.0
                val dateStr = dateFormat.format(Date(b.createdAt))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isExhausted) BgSubtle.copy(alpha = 0.6f) else BgSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Layers, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Batch #${b.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(if (isExhausted) BgSubtle else SuccessBg)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isExhausted) "Depleted" else "Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExhausted) TextMuted else SuccessGreen
                                )
                            }
                        }

                        Text(dateStr, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(vertical = 4.dp))

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BgSubtle)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Ingested / Rem", fontSize = 11.sp, color = TextSecondary)
                                Text("${"%.2f".format(b.grams)}g / ${"%.2f".format(b.remainingGrams)}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            Column {
                                Text("Cost Basis", fontSize = 11.sp, color = TextSecondary)
                                Text("₹${"%.0f".format(b.totalCost)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Revenue Yield", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = "₹${"%.0f".format(b.totalRevenue)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (b.totalRevenue > 0) SuccessGreen else TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (!isExhausted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.closeBatch(b.id) { success, _ ->
                                            if (success) Toast.makeText(context, "Batch finalized & closed", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "Close Batch", tint = DangerRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
