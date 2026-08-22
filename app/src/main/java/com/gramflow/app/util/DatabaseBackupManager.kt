package com.gramflow.app.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.gramflow.app.data.local.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object DatabaseBackupManager {

    private const val AES_KEY = "GramFlowSecure2026MasterKeyAES25" // 32 bytes for AES-256

    fun createEncryptedBackup(context: Context, database: AppDatabase) {
        try {
            // Checkpoint database to flush WAL to main db file
            database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL);")

            val dbPath = context.getDatabasePath("gramflow_v3.db")
            if (!dbPath.exists()) {
                Toast.makeText(context, "Database file not found", Toast.LENGTH_SHORT).show()
                return
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!outputDir.exists()) outputDir.mkdirs()

            // 1. Raw DB Backup
            val backupDbFile = File(outputDir, "gramflow_backup_$timestamp.db")
            dbPath.copyTo(backupDbFile, overwrite = true)

            // 2. Secure Encrypted Backup with CBC + Random IV (.enc)
            val encryptedFile = File(outputDir, "gramflow_encrypted_vault_$timestamp.enc")
            val secretKey = SecretKeySpec(AES_KEY.toByteArray(), "AES")
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

            FileInputStream(dbPath).use { fis ->
                FileOutputStream(encryptedFile).use { fos ->
                    fos.write(iv) // Write IV prefix
                    CipherOutputStream(fos, cipher).use { cos ->
                        fis.copyTo(cos)
                    }
                }
            }

            // Share File via Intent
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupDbFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "GramFlow Database Backup ($timestamp)")
                putExtra(Intent.EXTRA_TEXT, "GramFlow SQLite Database Backup generated on ${SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.getDefault()).format(Date())}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Export Encrypted Database Backup")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Toast.makeText(context, "Database backup created successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
