package edu.nd.pmcburne.hello

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [GroundsLocationEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(GroundsLocationConverter::class)
abstract class GroundsMapDatabase : RoomDatabase() {

    abstract fun groundsLocationService(): GroundsLocationService

    companion object {
        @Volatile
        private var GroundsDBInstance: GroundsMapDatabase? = null

        fun getGroundsDB(context: Context): GroundsMapDatabase {
            return GroundsDBInstance ?: synchronized(this) {
                val uvaDBInstance = Room.databaseBuilder(
                    context.applicationContext,
                    GroundsMapDatabase::class.java,
                    "campus_maps.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                GroundsDBInstance = uvaDBInstance
                uvaDBInstance
            }
        }
    }
}