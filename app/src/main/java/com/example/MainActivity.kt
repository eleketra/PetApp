
package com.example.pet

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.pet.PetAdapter.PetAdapter
import com.example.pet.data.DatabaseHandler
import com.example.pet.data.PetContract
import com.example.pet.data.PetContract.PetEntry
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    lateinit var context: Context
    lateinit var db: DatabaseHandler
    private val PET_LOADER: Int = 0   // unique id for the loader used
    lateinit var petCursorAdapter: PetAdapter    // adapter for our listView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        db = DatabaseHandler(context)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, editor::class.java)
            startActivity(intent)
        }

        pets_list_view.emptyView=empty_view
        supportLoaderManager.initLoader(PET_LOADER, null, this)
        petCursorAdapter = PetAdapter(context, null)
        // pets_list_view.layoutManager=LinearLayoutManager(applicationContext)
        pets_list_view.adapter = petCursorAdapter   // cursoradapter does not works with recycler view

        //setup item click listener
        //adapter View is the listView, view is the particular item
        pets_list_view.setOnItemClickListener { adapterView, view, position, id ->
            val intent=Intent(MainActivity@ this, editor::class.java)
            //as a parameter to setData contentUri is formed which represent specific pet that
            //was clicked on,by appending the id to the Content_URI
            intent.setData(ContentUris.withAppendedId(PetContract.CONTENT_URI, id))
            startActivity(intent)
        }
    }

    /*override fun onStart() {
        super.onStart()
        displayPets()
    }*/

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_catalog, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all_entries -> {
                deleteAllPets()
              //  Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.action_insert_dummy_data -> {
                insertPet()
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    /*private fun displayPets() {
        val projection = arrayOf(
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED,
                PetContract.PetEntry.COLUMN_PET_WEIGHT,
                PetContract.PetEntry.COLUMN_PET_GENDER
        )
        val cursor = contentResolver.query(PetContract.CONTENT_URI, projection, null, null, null)
        //text_view_pet.text = ""
        cursor.use { cursor ->
            // Figure out the index of each column

            // Figure out the index of each column
            val idColumnIndex = cursor!!.getColumnIndex(PetContract.PetEntry._ID)
            val nameColumnIndex = cursor!!.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME)
            val breedColumnIndex = cursor!!.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED)
            val genderColumnIndex = cursor!!.getColumnIndex(PetContract.PetEntry.COLUMN_PET_GENDER)
            val weightColumnIndex = cursor!!.getColumnIndex(PetContract.PetEntry.COLUMN_PET_WEIGHT)

            while (cursor!!.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
               // text_view_pet.append(
                    //    cursor.getInt(idColumnIndex).toString() + "" + cursor.getString(nameColumnIndex) + " " + cursor.getString(breedColumnIndex) + " "
                            //    + cursor.getInt(genderColumnIndex).toString() + " " + cursor.getInt(weightColumnIndex).toString() + "\n")
            }
        }

    }*/

    fun insertPet() {

        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        val values = ContentValues()
        values.put(PetContract.PetEntry.COLUMN_PET_NAME, "Toto")
        values.put(PetContract.PetEntry.COLUMN_PET_BREED, "Terrier")
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER, PetContract.PetEntry.GENDER_MALE)
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, 7)

        val newURI = contentResolver.insert(PetContract.CONTENT_URI, values)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val projection = arrayOf(
            PetContract.PetEntry._ID,
            PetContract.PetEntry.COLUMN_PET_NAME,
            PetContract.PetEntry.COLUMN_PET_BREED
        )
        return CursorLoader(this, PetContract.CONTENT_URI, projection, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        petCursorAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        petCursorAdapter.swapCursor(null)
    }

    private fun deleteAllPets(){
        val rowsDeleted = contentResolver.delete(PetContract.CONTENT_URI, null, null)
        if(rowsDeleted==0)
            Toast.makeText(this,"Pet Store is Empty",Toast.LENGTH_SHORT).show()
        //Log.v("CatalogActivity", "$rowsDeleted rows deleted from pet database")

    }
}
