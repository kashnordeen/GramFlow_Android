package com.gramflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gramflow.app.data.local.dao.CustomerDao
import com.gramflow.app.data.local.dao.SaleDao
import com.gramflow.app.data.local.dao.SettingsDao
import com.gramflow.app.data.local.dao.StockBatchDao
import com.gramflow.app.data.local.dao.UserDao
import com.gramflow.app.data.local.entity.CustomerEntity
import com.gramflow.app.data.local.entity.SaleBatchAssignmentEntity
import com.gramflow.app.data.local.entity.SaleEntity
import com.gramflow.app.data.local.entity.SettingEntity
import com.gramflow.app.data.local.entity.StockBatchEntity
import com.gramflow.app.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CustomerEntity::class,
        StockBatchEntity::class,
        SaleEntity::class,
        SaleBatchAssignmentEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun customerDao(): CustomerDao
    abstract fun stockBatchDao(): StockBatchDao
    abstract fun saleDao(): SaleDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gramflow.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Seed default pricing settings
                    database.settingsDao().setSettings(
                        listOf(
                            SettingEntity("rate_per_gram", "1000"),
                            SettingEntity("special_025_030", "250"),
                            SettingEntity("special_050_060", "500")
                        )
                    )
                    // Seed default admin user
                    database.userDao().insertUser(
                        UserEntity(
                            email = "admin@hemp.com",
                            name = "Admin",
                            passwordHash = "admin123"
                        )
                    )
                }
            }
        }
    }
}
