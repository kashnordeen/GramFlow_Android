package com.gramflow.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.gramflow.app.data.local.entity.CustomerEntity
import com.gramflow.app.data.local.entity.SaleEntity
import com.gramflow.app.data.repository.CustomerLedgerStatement
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateMasterLedgerPdf(
        context: Context,
        sales: List<SaleEntity>,
        customers: List<CustomerEntity>,
        totalStock: Double,
        totalDebt: Double,
        totalRevenue: Double
    ) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            var pageNumber = 1
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint()
            val dateFormat = SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.getDefault())
            val dateShort = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

            // Header Background
            paint.color = Color.rgb(17, 19, 18) // BgDark
            canvas.drawRect(0f, 0f, 595f, 95f, paint)

            // Brand Title
            paint.color = Color.rgb(198, 255, 0) // Neon Lime
            paint.textSize = 22f
            paint.isFakeBoldText = true
            canvas.drawText("GramFlow Master Audit Report", 30f, 45f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11f
            paint.isFakeBoldText = false
            canvas.drawText("Generated on ${dateFormat.format(Date())} · System ID: GF-LEDGER-2026", 30f, 70f, paint)

            // KPI Overview Tiles
            paint.color = Color.rgb(240, 242, 237)
            canvas.drawRect(30f, 110f, 565f, 175f, paint)

            paint.color = Color.rgb(18, 20, 22)
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText("VAULT STOCK", 45f, 130f, paint)
            canvas.drawText("TOTAL DISPENSED", 175f, 130f, paint)
            canvas.drawText("TOTAL REVENUE", 305f, 130f, paint)
            canvas.drawText("OUTSTANDING DEBT", 435f, 130f, paint)

            val totalGramsSold = sales.sumOf { it.gramsSold }

            paint.textSize = 14f
            paint.color = Color.BLACK
            canvas.drawText("${"%.2f".format(totalStock)}g", 45f, 155f, paint)
            canvas.drawText("${"%.2f".format(totalGramsSold)}g", 175f, 155f, paint)

            paint.color = Color.rgb(34, 197, 94) // Success Green
            canvas.drawText("Rs. ${"%.2f".format(totalRevenue)}", 305f, 155f, paint)

            paint.color = Color.rgb(234, 179, 8) // Warning Amber
            canvas.drawText("Rs. ${"%.2f".format(totalDebt)}", 435f, 155f, paint)

            // Section 1: Customer Debt Overview
            var y = 205f
            paint.color = Color.rgb(17, 19, 18)
            paint.textSize = 13f
            paint.isFakeBoldText = true
            canvas.drawText("Active Debtors & Ledger Balances", 30f, y, paint)

            y += 15f
            paint.color = Color.rgb(17, 19, 18)
            canvas.drawRect(30f, y, 565f, y + 22f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9f
            paint.isFakeBoldText = true
            canvas.drawText("Customer Name", 40f, y + 15f, paint)
            canvas.drawText("Phone", 210f, y + 15f, paint)
            canvas.drawText("Legacy Debt", 340f, y + 15f, paint)
            canvas.drawText("Sales Debt", 430f, y + 15f, paint)
            canvas.drawText("Total Due", 505f, y + 15f, paint)

            y += 36f
            paint.color = Color.BLACK
            paint.isFakeBoldText = false

            val debtors = customers.filter { (it.oldLoan + it.totalLoan) > 0 }
            if (debtors.isEmpty()) {
                canvas.drawText("All customer balances settled (Zero outstanding debt).", 40f, y, paint)
                y += 20f
            } else {
                for (c in debtors) {
                    val combined = c.oldLoan + c.totalLoan
                    canvas.drawText(c.name, 40f, y, paint)
                    canvas.drawText(c.phone ?: "N/A", 210f, y, paint)
                    canvas.drawText("Rs. ${"%.2f".format(c.oldLoan)}", 340f, y, paint)
                    canvas.drawText("Rs. ${"%.2f".format(c.totalLoan)}", 430f, y, paint)

                    paint.isFakeBoldText = true
                    paint.color = Color.rgb(180, 83, 9)
                    canvas.drawText("Rs. ${"%.2f".format(combined)}", 505f, y, paint)
                    paint.isFakeBoldText = false
                    paint.color = Color.BLACK

                    y += 18f
                    if (y > 780f) break
                }
            }

            // Section 2: Recent Sales Ledger
            y += 15f
            if (y < 760f) {
                paint.color = Color.rgb(17, 19, 18)
                paint.textSize = 13f
                paint.isFakeBoldText = true
                canvas.drawText("Transaction Ledger Audit (${sales.size} records)", 30f, y, paint)

                y += 15f
                paint.color = Color.rgb(17, 19, 18)
                canvas.drawRect(30f, y, 565f, y + 22f, paint)

                paint.color = Color.WHITE
                paint.textSize = 9f
                paint.isFakeBoldText = true
                canvas.drawText("Date", 40f, y + 15f, paint)
                canvas.drawText("Customer", 130f, y + 15f, paint)
                canvas.drawText("Weight", 250f, y + 15f, paint)
                canvas.drawText("Billed", 330f, y + 15f, paint)
                canvas.drawText("Received", 410f, y + 15f, paint)
                canvas.drawText("Status / Balance", 490f, y + 15f, paint)

                y += 34f
                paint.color = Color.BLACK
                paint.isFakeBoldText = false

                for (s in sales) {
                    val dStr = dateShort.format(Date(s.createdAt))
                    canvas.drawText(dStr, 40f, y, paint)
                    canvas.drawText(s.customerName, 130f, y, paint)
                    canvas.drawText("${"%.2f".format(s.gramsSold)}g", 250f, y, paint)
                    canvas.drawText("Rs. ${"%.2f".format(s.finalAmount)}", 330f, y, paint)
                    canvas.drawText("Rs. ${"%.2f".format(s.amountReceived)}", 410f, y, paint)

                    if (s.balance > 0) {
                        paint.color = Color.rgb(180, 83, 9)
                        canvas.drawText("Loan: Rs.${"%.2f".format(s.balance)}", 490f, y, paint)
                        paint.color = Color.BLACK
                    } else {
                        paint.color = Color.rgb(22, 101, 52)
                        canvas.drawText("Paid", 490f, y, paint)
                        paint.color = Color.BLACK
                    }

                    y += 18f
                    if (y > 800f) break
                }
            }

            document.finishPage(page)

            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!outputDir.exists()) outputDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val file = File(outputDir, "GramFlow_Master_Ledger_$timestamp.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()

            // Open / Share PDF via Intent
            sharePdfFile(context, file, "GramFlow Master Audit Report")
            Toast.makeText(context, "Master PDF generated successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF Generation failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateCustomerLedgerPdf(context: Context, statement: CustomerLedgerStatement) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

            // Header Background
            paint.color = Color.rgb(17, 19, 18) // BgDark
            canvas.drawRect(0f, 0f, 595f, 90f, paint)

            // Brand Title
            paint.color = Color.rgb(198, 255, 0) // Neon Lime
            paint.textSize = 22f
            paint.isFakeBoldText = true
            canvas.drawText("GramFlow Ledger Statement", 30f, 45f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11f
            paint.isFakeBoldText = false
            canvas.drawText("Generated on ${dateFormat.format(Date())}", 30f, 68f, paint)

            // Customer Info Card
            paint.color = Color.rgb(240, 242, 237)
            canvas.drawRect(30f, 110f, 565f, 170f, paint)

            paint.color = Color.rgb(18, 20, 22)
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("Client: ${statement.customer.name}", 45f, 135f, paint)

            paint.textSize = 11f
            paint.isFakeBoldText = false
            paint.color = Color.rgb(110, 116, 111)
            canvas.drawText("Phone: ${statement.customer.phone ?: "N/A"}", 45f, 155f, paint)
            canvas.drawText("Total Balance Due: Rs. ${"%.2f".format(statement.currentBalance)}", 360f, 145f, paint)

            // Table Header
            var y = 205f
            paint.color = Color.rgb(17, 19, 18)
            canvas.drawRect(30f, y, 565f, y + 25f, paint)

            paint.color = Color.WHITE
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText("Date", 40f, y + 17f, paint)
            canvas.drawText("Weight", 150f, y + 17f, paint)
            canvas.drawText("Final Bill", 260f, y + 17f, paint)
            canvas.drawText("Paid", 370f, y + 17f, paint)
            canvas.drawText("Balance", 480f, y + 17f, paint)

            y += 40f
            paint.color = Color.BLACK
            paint.isFakeBoldText = false

            for (sale in statement.sales) {
                val dStr = dateFormat.format(Date(sale.createdAt))
                canvas.drawText(dStr, 40f, y, paint)
                canvas.drawText("${"%.2f".format(sale.gramsSold)}g", 150f, y, paint)
                canvas.drawText("Rs. ${"%.2f".format(sale.finalAmount)}", 260f, y, paint)
                canvas.drawText("Rs. ${"%.2f".format(sale.amountReceived)}", 370f, y, paint)
                canvas.drawText("Rs. ${"%.2f".format(sale.balance)}", 480f, y, paint)
                y += 22f
                if (y > 780f) break
            }

            document.finishPage(page)

            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!outputDir.exists()) outputDir.mkdirs()

            val file = File(outputDir, "Ledger_${statement.customer.name.replace(" ", "_")}.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()

            sharePdfFile(context, file, "Customer Statement - ${statement.customer.name}")
            Toast.makeText(context, "Customer statement PDF generated!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF Generation failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sharePdfFile(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Open / Share $title")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
