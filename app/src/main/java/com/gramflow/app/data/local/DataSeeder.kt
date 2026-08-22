package com.gramflow.app.data.local

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

object DataSeeder {
    private const val TAG = "DataSeeder"

    fun seedIfEmpty(context: Context, database: AppDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if customers already exist
                val existingCustomers = database.customerDao().getAllCustomers()
                if (existingCustomers.isNotEmpty()) {
                    Log.d(TAG, "Database already populated with ${existingCustomers.size} customers. Skipping seed.")
                    return@launch
                }

                Log.d(TAG, "Seeding database from assets/databases/seed.sql...")
                val inputStream = context.assets.open("databases/seed.sql")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                reader.close()

                val db = database.openHelper.writableDatabase
                val statements = content.split(";\n", ";\r\n", ";")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                db.beginTransaction()
                try {
                    var successCount = 0
                    for (sql in statements) {
                        try {
                            db.execSQL(sql)
                            successCount++
                        } catch (lineErr: Exception) {
                            Log.e(TAG, "Error running SQL line: $sql", lineErr)
                        }
                    }
                    db.setTransactionSuccessful()
                    Log.d(TAG, "Database seed completed successfully! Executed $successCount statements.")
                } finally {
                    db.endTransaction()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seed database from asset", e)
                // Fallback default setup
                try {
                    val db = database.openHelper.writableDatabase
                    db.execSQL("INSERT OR REPLACE INTO settings (key, value) VALUES ('rate_per_gram', '1000')")
                    db.execSQL("INSERT OR REPLACE INTO settings (key, value) VALUES ('special_025_030', '250')")
                    db.execSQL("INSERT OR REPLACE INTO settings (key, value) VALUES ('special_050_060', '500')")
                    db.execSQL("INSERT OR REPLACE INTO users (id, email, name, passwordHash, createdAt) VALUES (1, 'admin@hemp.com', 'Admin', 'admin123', ${System.currentTimeMillis()})")
                } catch (fallbackErr: Exception) {
                    Log.e(TAG, "Fallback seed failed", fallbackErr)
                }
            }
        }
    }
}
