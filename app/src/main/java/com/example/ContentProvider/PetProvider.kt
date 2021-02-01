package com.example.pet.ContentProvider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.pet.data.DatabaseHandler
import com.example.pet.data.PetContract
import com.example.pet.data.PetContract.PetEntry


class PetProvider : ContentProvider() {
    lateinit var mDbHelper: DatabaseHandler
    lateinit var database: SQLiteDatabase
    lateinit var cursor: Cursor

    /** URI matcher code for the content URI for a single pet in the pets table  */

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private val PETS = 100
    private val PET_ID = 101
    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    // Static initializer. This is run the first time anything is called from this class.
    init {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS)
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID)
    }

    /**
     * Initialize the provider and the database helper object.
     */
    override fun onCreate(): Boolean {
        // Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = DatabaseHandler(getContext()!!)
        database = mDbHelper.writableDatabase
        return true
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val queryBuilder = SQLiteQueryBuilder()
        val database = mDbHelper.readableDatabase
        queryBuilder.tables = PetContract.PetEntry.TABLE_NAME
        val match = sUriMatcher.match(uri)
        when (match) {
            PETS -> {
            }
            PET_ID -> {
                queryBuilder.appendWhere(PetContract.PetEntry._ID + "="
                        + uri.lastPathSegment)
            }
            else -> {
                throw IllegalArgumentException("Cannot query unknown uri $uri")
            }
        }
        cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder)
        //Set notification URI on the cursor
        // so we know what content URI cursor was created for
        //if the data at this URI changes, then we know that we need to update the cursor.
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    override fun getType(uri: Uri): String? {

        return null
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    override fun insert(uri: Uri, cv: ContentValues?): Uri? {
        val match = sUriMatcher.match(uri)
        return when (match) {
            PETS -> {
                insertPet(uri, cv!!)
            }
            else -> throw IllegalArgumentException("Insertion is not supported for " + uri);
        }


    }

    private fun insertPet(uri: Uri, values: ContentValues): Uri? {
        val name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME)
                ?: throw  IllegalArgumentException("Pet requires a name");
        val gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
            throw IllegalArgumentException("Pet requires valid gender");
        }
        val weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT)
        if (weight != null && weight < 0) {
            throw IllegalArgumentException("Pet requires valid weight");
        }

        val id = database.insert(PetContract.PetEntry.TABLE_NAME, null, values)
        if (id == -1L) {
            Log.e("LOG_TAG", "Failed to insert row for " + uri);
            return null
        }
        // notify all the listeners that the data has changed for the pet content URI
        // second arg is optional content observer parameter. Its a class that  receives callbacks or
        //changes to the content.But passing null by default cursor adapter gets notified.
        context!!.contentResolver.notifyChange(uri, null)
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id)
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
      //  Log.d("uri", uri.toString())
        return deletePet(uri, selection, selectionArgs)
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    override fun update(uri: Uri, cv: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {

        return updatePet(uri, cv, selection, selectionArgs)
    }

    fun updatePet(uri: Uri, cv: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        var rowsUpdated = 0
        val match = sUriMatcher.match(uri)
        when (match) {
            PETS -> rowsUpdated = database.update(PetEntry.TABLE_NAME,cv, selection, selectionArgs)
            PET_ID -> {
                val id = uri.lastPathSegment
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = database.update(PetEntry.TABLE_NAME,cv,
                            PetEntry._ID + "=" + id,
                            null)
                } else {
                    rowsUpdated = database.update(PetEntry.TABLE_NAME,cv,
                            PetEntry._ID + "=" + id
                                    + " and " + selection,
                            selectionArgs)
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: " + uri)

        }
        if (rowsUpdated != 0)
            context!!.contentResolver.notifyChange(uri, null)
        return rowsUpdated


    }

    fun deletePet(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        var rowsDeleted = 0
        val match = sUriMatcher.match(uri)
        when (match) {
            PETS -> rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs)
            PET_ID -> {
                val id = uri.lastPathSegment
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = database.delete(PetEntry.TABLE_NAME,
                            PetEntry._ID + "=" + id,
                            null)
                } else {
                    rowsDeleted = database.delete(PetEntry.TABLE_NAME,
                            PetEntry._ID + "=" + id
                                    + " and " + selection,
                            selectionArgs)
                }
                //rowsDeleted = database.delete(PetEntry.TABLE_NAME, "where", selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI: " + uri)
        }
        if (rowsDeleted != 0)
            context!!.contentResolver.notifyChange(uri, null)
        return rowsDeleted
    }


}
