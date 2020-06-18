package com.example.pets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
//import androidx.loader.content.CursorLoader;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.pets.data.PetContract;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mNameEditText;
    private EditText mBreedEditText;
    private EditText mWeightEditText;
    private Spinner mGenderSpinner;

    private int mGender = PetContract.PetColumns.GENDER_UNKNOWN_VALUE;
    private static final int EXISTING_PET_LOADER=1;
    private Uri mCurrentPetUri;
    public static final String LOG_TAG=EditorActivity.class.getSimpleName();
    private static Boolean isTouched;

    private View.OnTouchListener touchListener= new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            isTouched=true;
            //Toast.makeText(EditorActivity.this,String.valueOf(isTouched), Toast.LENGTH_SHORT).show();

            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        isTouched=false;

        //Toast.makeText(EditorActivity.this,String.valueOf(isTouched), Toast.LENGTH_SHORT).show();


        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);


        mNameEditText.setOnTouchListener(touchListener);
        mBreedEditText.setOnTouchListener(touchListener);
        mWeightEditText.setOnTouchListener(touchListener);
        mGenderSpinner.setOnTouchListener(touchListener);


        //Reception de l'intent
        Intent intent=getIntent();
        //Reception de sa data
        mCurrentPetUri=intent.getData();

        //Test si la data de l'intent contient de l'URI
        if(mCurrentPetUri==null){
            setTitle(getString(R.string.add_pet));
        }else
        {
            setTitle(getString(R.string.edit_pet));
            getSupportLoaderManager().initLoader(EXISTING_PET_LOADER,null,EditorActivity.this);

        }

        setupSpinner();

    }

    ////////////////////////////////////////////////////////////////////////////////////

    private void setupSpinner() {
        ArrayAdapter<CharSequence> genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetContract.PetColumns.GENDER_MALE_VALUE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetContract.PetColumns.GENDER_FEMALE_VALUE; // Female
                    } else {
                        mGender = PetContract.PetColumns.GENDER_UNKNOWN_VALUE; // Unknown
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetContract.PetColumns.GENDER_UNKNOWN_VALUE; // Unknown
            }
        });

    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                savePet();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:

                if (isTouched==true)
                    showAlet();
                if (isTouched==false)
                 NavUtils.navigateUpFromSameTask(EditorActivity.this);

                    return true;
                }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The {@link #getOnBackPressedDispatcher() OnBackPressedDispatcher} will be given a
     * chance to handle the back button before the default behavior of
     * {@link Activity#onBackPressed()} is invoked.
     *
     * @see #getOnBackPressedDispatcher()
     */
    @Override
    public void onBackPressed() {

        if (isTouched==true) {
            showAlet();
        }else  super.onBackPressed();

    }

    private void savePet(){

        int weight;
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();

        try {
            weight = Integer.valueOf(weightString);//Integer.parseInt(weightString);
        } catch (NumberFormatException e) {
            weight = 0;
        }

        int gender = mGender;
        if (mCurrentPetUri == null && (TextUtils.isEmpty(nameString) )){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PetContract.PetColumns.NAME_COLUMN, nameString);
        values.put(PetContract.PetColumns.BREED_COLUMN, breedString);
        values.put(PetContract.PetColumns.GENDER_COLUMN, mGender);
        values.put(PetContract.PetColumns.WEIGHT_COLUMN, weight);



        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
        if (mCurrentPetUri == null) {
            // This is a NEW pet, so insert a new pet into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(PetContract.PetColumns.PREFIX_AND_AUTHORITY_AND_PATH, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }

    }

        else {
            int rowsAffected = getContentResolver().update(mCurrentPetUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }


        }
    }


    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * <p>This will always be called from the process's main thread.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */

    //---------------------------------------------------------------------------------
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        String[] projection = {
                PetContract.PetColumns.ID_COLUMN,
                PetContract.PetColumns.NAME_COLUMN,
                PetContract.PetColumns.BREED_COLUMN,
                PetContract.PetColumns.GENDER_COLUMN,
                PetContract.PetColumns.WEIGHT_COLUMN};


        /*if(mCurrentPetUri==null)
            return null;*/


        return new CursorLoader(this,
                mCurrentPetUri,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {


        if (cursor == null || cursor.getCount() < 1)
            return;

       if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(PetContract.PetColumns.NAME_COLUMN);
            int breedColumnIndex = cursor.getColumnIndex(PetContract.PetColumns.BREED_COLUMN);
            int genderColumnIndex = cursor.getColumnIndex(PetContract.PetColumns.GENDER_COLUMN);
            int weightColumnIndex = cursor.getColumnIndex(PetContract.PetColumns.WEIGHT_COLUMN);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);
            int weight = cursor.getInt(weightColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            switch (gender) {
                case PetContract.PetColumns.GENDER_MALE_VALUE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetContract.PetColumns.GENDER_FEMALE_VALUE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }

    //-------------------------------------------------------------------------------

    private void showAlet(){

        AlertDialog.Builder builder= new AlertDialog.Builder(EditorActivity.this);

        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });

        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog=builder.create();
        alertDialog.show();

    }


    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // TODO: Implement this method

        // Only perform the delete if this is an existing pet.
        if (mCurrentPetUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);

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

        }

        finish();
    }


    }