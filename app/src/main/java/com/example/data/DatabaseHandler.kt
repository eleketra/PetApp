package com.example.pet.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pet.data.PetContract.PetEntry


const val DATABASE_NAME="shelter.db"
const val TABLE_NAME="PET"
const val COL_NAME="name"
const val COL_AGE="age"
const val COL_ID="id"
class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    /**
     * This is called when the database is created for the first time.
     */
    override fun onCreate(db: SQLiteDatabase?) {
        val SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + PetEntry.TABLE_NAME +
                " (" + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "+
                PetEntry.COLUMN_PET_BREED + " TEXT, "+
                PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "+
                PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);"

        db!!.execSQL(SQL_CREATE_PETS_TABLE)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
        // The database is still at version 1, so there's nothing to do be done here.
        // code updated if there any changes ex..addition of new column
    }



}
