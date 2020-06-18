package com.example.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
//import androidx.loader.content.CursorLoader;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.pets.data.PetContract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int PET_LOADER=0;
    PetCursorAdapter mPetCursorAdapter;


    //***********************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


    /////////////////////////////////////////////////////////////////////////


        ListView petListView = (ListView) findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);


        mPetCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mPetCursorAdapter);

        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent=new Intent(CatalogActivity.this,EditorActivity.class);

                //On construit l'URI de l'element cliqué avec son id et on l'envoie à la class EditActivity
                Uri itemUri= ContentUris.withAppendedId(PetContract.PetColumns.PREFIX_AND_AUTHORITY_AND_PATH,id);

                //Transfert de l'URI pour verifier si l'intent contion de la data
                intent.setData(itemUri);

                startActivity(intent);
            }
        });
        getSupportLoaderManager().initLoader(PET_LOADER, null, this);


    }

    //--------------------------------------------------------------------------------------------//


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //--------------------------------------------------------------------------------------------------//

    private void insertPet() {

        ContentValues values = new ContentValues();
        values.put(PetContract.PetColumns.NAME_COLUMN, "Toto");
        values.put(PetContract.PetColumns.BREED_COLUMN, "Terrier");
        values.put(PetContract.PetColumns.GENDER_COLUMN, PetContract.PetColumns.GENDER_MALE_VALUE);
        values.put(PetContract.PetColumns.WEIGHT_COLUMN, 7);

        Uri newUri = getContentResolver().insert(PetContract.PetColumns.PREFIX_AND_AUTHORITY_AND_PATH, values);


    }
    //----------------------------------------------------------------------------------------------//


    //C'est ici qu'on envoie la requette à la methode query dans la PetProvider, on defini la projection et l'URI
    //et on recupere le cursor dans PetCursorAdapter pour les afficher dans la list view
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PetContract.PetColumns.ID_COLUMN,
                PetContract.PetColumns.NAME_COLUMN,
                PetContract.PetColumns.BREED_COLUMN,};

        return new CursorLoader(this,
                PetContract.PetColumns.PREFIX_AND_AUTHORITY_AND_PATH,
                projection,
                null,
                null,
                null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPetCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPetCursorAdapter.swapCursor(null);


    }



    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetContract.PetColumns.PREFIX_AND_AUTHORITY_AND_PATH,
                null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

}
