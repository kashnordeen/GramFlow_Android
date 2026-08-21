package com.gramflow.app.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.gramflow.app.data.local.entity.SaleEntity
import com.gramflow.app.data.repository.CustomerLedgerStatement
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateCustomerLedgerPdf(context: Context, statement: CustomerLedgerStatement): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
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
        val file = File(outputDir, "Ledger_${statement.customer.name.replace(" ", "_")}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        return file
    }
}
