package com.cartouchoo.cartarocho.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cartouchoo.cartarocho.data.local.dao.GameDao
import com.cartouchoo.cartarocho.data.local.dao.GamePlayerDao
import com.cartouchoo.cartarocho.data.local.dao.PlayerDao
import com.cartouchoo.cartarocho.data.local.dao.RoundDao
import com.cartouchoo.cartarocho.data.local.dao.RoundMisereDao
import com.cartouchoo.cartarocho.data.local.dao.RoundScoreDao
import com.cartouchoo.cartarocho.data.local.entity.GameEntity
import com.cartouchoo.cartarocho.data.local.entity.GamePlayerEntity
import com.cartouchoo.cartarocho.data.local.entity.PlayerEntity
import com.cartouchoo.cartarocho.data.local.entity.RoundEntity
import com.cartouchoo.cartarocho.data.local.entity.RoundMisereEntity
import com.cartouchoo.cartarocho.data.local.entity.RoundScoreEntity

@Database(
    entities = [
        PlayerEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        RoundEntity::class,
        RoundMisereEntity::class,
        RoundScoreEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun gamePlayerDao(): GamePlayerDao
    abstract fun roundDao(): RoundDao
    abstract fun roundMisereDao(): RoundMisereDao
    abstract fun roundScoreDao(): RoundScoreDao

    companion object {
        private const val DATABASE_NAME = "cartarocho.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN name TEXT")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
