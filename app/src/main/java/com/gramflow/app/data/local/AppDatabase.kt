package com.gramflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
                    "gramflow_v3.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
