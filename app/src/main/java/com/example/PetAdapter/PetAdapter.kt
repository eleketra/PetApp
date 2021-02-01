package com.example.pet.PetAdapter

import android.content.Context
import android.database.Cursor
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import com.example.pet.R
import com.example.pet.data.PetContract


class PetAdapter(val context:Context, cursor: Cursor?): CursorAdapter(context,cursor,true){
    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false)
    }
    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {

        val nameTextView= view?.findViewById<TextView>(R.id.name)
        val summaryTextView=view?.findViewById<TextView>(R.id.summary)

        val nameColumnIndex= cursor?.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME)
        val breedColumnIndex=cursor?.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED)

        nameTextView!!.text=(cursor!!.getString(nameColumnIndex!!))
        var breed=cursor.getString(breedColumnIndex!!)
        // If the pet breed is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.

        if(TextUtils.isEmpty(breed))
            breed="Unknown Breed"
        summaryTextView!!.text=breed

    }

}
