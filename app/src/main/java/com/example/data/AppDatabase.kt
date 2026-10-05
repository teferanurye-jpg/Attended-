package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [AttendanceRecord::class, CompanyBranch::class, WorkLog::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao
    abstract fun branchDao(): BranchDao
    abstract fun workLogDao(): WorkLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ethio_attendance_database"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        val DEFAULT_BRANCHES = listOf(
            CompanyBranch(
                id = 1L,
                name = "የኢትዮጵያ ዋና መ/ቤት - ቸርችል ጎዳና",
                city = "አዲስ አበባ",
                latitude = 9.0227,
                longitude = 38.7468,
                radiusMeters = 300,
                isDefault = true,
                address = "ቸርችል ጎዳና፣ ፖስታ ቤት አደባባይ፣ አዲስ አበባ"
            ),
            CompanyBranch(
                id = 2L,
                name = "ቦሌ መድሃኒዓለም ቅርንጫፍ",
                city = "አዲስ አበባ",
                latitude = 8.9956,
                longitude = 38.7884,
                radiusMeters = 250,
                isDefault = false,
                address = "ቦሌ ክፍለ ከተማ፣ ካሜሩን ጎዳና፣ አዲስ አበባ"
            ),
            CompanyBranch(
                id = 3L,
                name = "አዳማ ቴክኖሎጂ ቅርንጫፍ",
                city = "አዳማ",
                latitude = 8.5414,
                longitude = 39.2689,
                radiusMeters = 350,
                isDefault = false,
                address = "ዋና የንግድ ቀጠና፣ አዳማ"
            ),
            CompanyBranch(
                id = 4L,
                name = "ሀዋሳ ኢንዱስትሪ ፓርክ ቅርንጫፍ",
                city = "ሀዋሳ",
                latitude = 7.0504,
                longitude = 38.4955,
                radiusMeters = 400,
                isDefault = false,
                address = "የኢንዱስትሪ ፓርክ ዞን ለ፣ ሀዋሳ"
            ),
            CompanyBranch(
                id = 5L,
                name = "ድሬዳዋ ኦፕሬሽንስ ቅርንጫፍ",
                city = "ድሬዳዋ",
                latitude = 9.5931,
                longitude = 41.8661,
                radiusMeters = 250,
                isDefault = false,
                address = "ቀበሌ 02፣ ባቡር ጣቢያ መንገድ፣ ድሬዳዋ"
            )
        )

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.branchDao().insertBranches(DEFAULT_BRANCHES)
                    }
                }
            }
        }
    }
}
