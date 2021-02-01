package com.example.pet

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.DialogInterface
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnTouchListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.pet.data.DatabaseHandler
import com.example.pet.data.PetContract
import kotlinx.android.synthetic.main.activity_editor.*


class editor : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    var mGender: Int = PetContract.PetEntry.GENDER_UNKNOWN
    lateinit var mdbHelper: DatabaseHandler
    private var mCurrentPetURI: Uri? = null
    var mPetHasChanged = false
    private val EXISTING_PET_LOADER: Int = 1  // unique id for the loader used

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mPetHasChanged boolean to true.
    @SuppressLint("ClickableViewAccessibility")
    private val mTouchListener = OnTouchListener { view, motionEvent ->
        mPetHasChanged = true
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Toast.makeText(this, "New Activity Started", Toast.LENGTH_SHORT).show()
        setSpinner()
        edit_pet_name.setOnTouchListener(mTouchListener)
        edit_pet_breed.setOnTouchListener(mTouchListener)
        edit_pet_weight.setOnTouchListener(mTouchListener)
        spinner_gender.setOnTouchListener(mTouchListener)
        mCurrentPetURI = intent.data
        if (mCurrentPetURI == null) {
            title = getString(R.string.editor_activity_title_new_pet)
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            //this function tells android that menu item has been changed and it should be redrawn
            //and then it signals OS to call onPrepareOptionsMenu() for making the changes
            //since onCreateOptionsMenu is called once.
            invalidateOptionsMenu();

        } else {
            title = (getString(R.string.editor_activity_title_edit_pet))
            supportLoaderManager.initLoader(EXISTING_PET_LOADER, null, this)
        }
    }

    fun setSpinner() {
        val genderSpinnerAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item)
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner_gender.adapter = genderSpinnerAdapter
        spinner_gender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = p0!!.getItemAtPosition(position).toString()
                Log.d("selected",selected)
                if (!TextUtils.isEmpty(selected)) {
                    Log.d("hello","hello")
                    mGender = when (selected) {
                        R.string.gender_male.toString() -> PetContract.PetEntry.GENDER_MALE
                        R.string.gender_female.toString() -> PetContract.PetEntry.GENDER_FEMALE
                        else -> PetContract.PetEntry.GENDER_UNKNOWN

                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                mGender = PetContract.PetEntry.GENDER_UNKNOWN


            }

        }
    }

    fun savePet() {
        // Gets the database in write mode
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default
        var weight = 0
        val nameString = edit_pet_name.text.toString().trim()
        val breedString = edit_pet_breed.text.toString().trim()
        if (!TextUtils.isEmpty(edit_pet_weight.text)) {
            weight = Integer.parseInt(edit_pet_weight.text.toString().trim())
        }
        //if the fields are empty
        if (mCurrentPetURI == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) &&
                TextUtils.isEmpty(weight.toString()) && mGender == PetContract.PetEntry.GENDER_UNKNOWN) {
            return
        }
        mdbHelper = DatabaseHandler(this)
        Log.d("in the cursor", mGender.toString())

        //val db: SQLiteDatabase = mdbHelper.getWritableDatabase()
        val cv = ContentValues()
        cv.put(PetContract.PetEntry.COLUMN_PET_NAME, nameString)
        cv.put(PetContract.PetEntry.COLUMN_PET_BREED, breedString)
        cv.put(PetContract.PetEntry.COLUMN_PET_GENDER, mGender)
        cv.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, weight)
        if (mCurrentPetURI == null) {
            val newUri = contentResolver.insert(PetContract.CONTENT_URI, cv)

            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            val rowsAffected = contentResolver.update(mCurrentPetURI!!, cv, null, null)
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show()
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show()
            }
        }

    }

    // called once during activity(or fragment)creation
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // User clicked on a menu option in the app bar overflow menu
        return when (item.itemId) {
            R.id.action_save -> {
                // Save pet to database
                savePet()
                // Exit activity
                finish()
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            R.id.home -> {
                // Navigate back to parent activity (CatalogActivity)
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(this)
                    true
                }
                val discardButtonClickListener = DialogInterface.OnClickListener { dialog, id ->
                    // User clicked "Discard" button, close the current activity
                    NavUtils.navigateUpFromSameTask(this)

                }
                // Show dialog that there are unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        //Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        val projection = arrayOf(
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED,
                PetContract.PetEntry.COLUMN_PET_GENDER,
                PetContract.PetEntry.COLUMN_PET_WEIGHT)
        // This loader will execute the ContentProvider's query method on a background thread

        // This loader will execute the ContentProvider's query method on a background thread
        return CursorLoader(this,  // Parent activity context
                mCurrentPetURI!!,  // Query the content URI for the current pet
                projection,  // Columns to include in the resulting Cursor
                null,  // No selection clause
                null,  // No selection arguments
                null) // Default sort order

    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        //Proceed with moving to the first row of the cursor and reading data from it.
        //Even though it has only one item, it starts from position -1.
        if (cursor!!.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME))
            val breed = cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED))
            val gender = cursor.getInt(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_GENDER))
            val weight = cursor.getInt(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_WEIGHT))
            edit_pet_name.setText(name)
            edit_pet_breed.setText(breed)
            edit_pet_weight.setText(weight.toString())

            when (gender) {
                PetContract.PetEntry.GENDER_FEMALE -> spinner_gender.setSelection(2)
                PetContract.PetEntry.GENDER_MALE -> spinner_gender.setSelection(1)
                else -> spinner_gender.setSelection(0)

            }
        }

    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        TODO("Not yet implemented")
    }

    fun showUnsavedChangesDialog(discardButtonClickListener: DialogInterface.OnClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.unsaved_changes_dialog_msg)
        builder.setPositiveButton(R.string.discard, discardButtonClickListener)
        builder.setNegativeButton(R.string.keep_editing, DialogInterface.OnClickListener { dialog,
                                                                                           id ->
            if (dialog != null) {
                dialog.dismiss()
            }
        })
        //create the alert dialog and show.
        val alertDialog = builder.create()
        alertDialog.show()

    }

    override fun onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed()
            return
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded
        val discardButtonClickListener = DialogInterface.OnClickListener { dialog, id ->
            // User clicked "Discard" button, close the current activity.
            finish()
        }
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)

        if (mCurrentPetURI == null) {
            val menuItem = menu?.findItem(R.id.action_delete)
            menuItem!!.setVisible(false)

        }
        return true
    }

    fun showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.delete_dialog_msg)
        builder.setPositiveButton(R.string.delete, DialogInterface.OnClickListener { dialog,
                                                                                     id ->
            deletePet()
        })
        builder.setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog,
                                                                                     id ->
            if (dialog != null) {
                dialog.dismiss()
            }
        })
        //create the alert dialog and show.
        val alertDialog = builder.create()
        alertDialog.show()

    }

    private fun deletePet() {
        val rowsDeleted = contentResolver.delete(mCurrentPetURI!!, null, null)
        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                    Toast.LENGTH_SHORT).show();

        }
        finish()
    }
}
