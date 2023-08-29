package com.phongnn.imagepicker.data.dbentity.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.phongnn.imagepicker.data.dbentity.dao.ImageDao
import com.phongnn.imagepicker.data.dbentity.entity.ImageEntity

@Database(entities = [ImageEntity::class], version = 2, exportSchema = false)
abstract class ImageDatabase : RoomDatabase() {

    abstract fun imageDao(): ImageDao

    companion object {
        @Volatile
        private var INSTANCE: ImageDatabase? = null

        fun getDatabase(context: Context): ImageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImageDatabase::class.java,
                    "image_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Step 1: Create the new table with the new schema
                database.execSQL("CREATE TABLE image_table_new (id INTEGER PRIMARY KEY NOT NULL, imageUrl BLOB NOT NULL, path TEXT NOT NULL, type TEXT NOT NULL)")

                // Step 2: Copy the data from the old table to the new table
                database.execSQL("INSERT INTO image_table_new (id, imageUrl, path, type) SELECT id, imageUrl, '', '' FROM image_table")

                // Step 3: Remove the old table
                database.execSQL("DROP TABLE image_table")

                // Step 4: Rename the new table to the original table name
                database.execSQL("ALTER TABLE image_table_new RENAME TO image_table")
            }
        }

    }

}