package com.example.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class PetProvider extends ContentProvider {

    //C'est ici que les requettes utisateurs passent avant de se diriger vers la table en question
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    //Les codes pour capter et diriger les requetes utilisateurs
    private static final int MATCH_PET_TABLE = 100;
    private static final int MATCH_INDIVIDUAL_PET = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(PetContract.AUTHORITY, PetContract.PATH, MATCH_PET_TABLE);
        sUriMatcher.addURI(PetContract.AUTHORITY, PetContract.PATH + "/#", MATCH_INDIVIDUAL_PET);

    }


//----------------------------------------------------------------------------------DAY 1


    //Temoin des tables existantes
    private PetHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new PetHelper(getContext());
        return true;
    }


    //La methode qui s'occuppe de tout ce qui est SELECT venant de l'utilisateur
    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {


        //Acceder aux tables en mode lecture
        SQLiteDatabase db = mHelper.getReadableDatabase();
        //C'est le curseur qui lit les requetes utulisateurs
        Cursor cursor;


        // La forme de la requete utilisateur correspond à quelle forme de URI predefinis ?
        switch (sUriMatcher.match(uri)) {

            case MATCH_PET_TABLE:

                //SELECT *FROM TABLE_NAME
                cursor = db.query(PetContract.PetColumns.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MATCH_INDIVIDUAL_PET:

                //SELECT (projection) FROM TABLE_NAME WHERE (selection) = selectionArgs
                // Les arguments sont déja prédefini dans la requete de l'utilisateur
                selection = PetContract.PetColumns.ID_COLUMN + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(PetContract.PetColumns.TABLE_NAME,
                        projection, selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }


        //Notifier le curseur de chaque changement dans la table
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }


    //----------------------------------------------------------------------------DAY 2


    @Override
    public String getType(Uri uri) {

        switch (sUriMatcher.match(uri)) {
            case MATCH_PET_TABLE:
                return PetContract.PetColumns.CONTENT_LIST_TYPE;
            case MATCH_INDIVIDUAL_PET:
                return PetContract.PetColumns.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + sUriMatcher.match(uri));
        }

    }


    //------------------------------------------------------------------------------DAY 3

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        //La methode d'insertion d'element dans les tables disponibles


        switch (sUriMatcher.match(uri)) {


            case MATCH_PET_TABLE:

                //Verification des elements que l'utilisateur entre dans les editText conformement
                // aux contraintes des tables avant de les inserer

                String name = contentValues.getAsString(PetContract.PetColumns.NAME_COLUMN);
                if (name == null) {
                    throw new IllegalArgumentException("Pet requires a name");
                }

                Integer gender = contentValues.getAsInteger(PetContract.PetColumns.GENDER_COLUMN);
                if (gender == null || !PetContract.PetColumns.isValidGender(gender)) {
                    throw new IllegalArgumentException("Pet requires valid gender");
                }

                Integer weight = contentValues.getAsInteger(PetContract.PetColumns.WEIGHT_COLUMN);
                if (weight != null && weight < 0) {
                    throw new IllegalArgumentException("Pet requires valid weight");
                }


                //Acceder aux tables en mode ecriture
                SQLiteDatabase database = mHelper.getWritableDatabase();

                //INSERT INTO TABLE_NAME(some columns) VALUES(values per column)----------->> ContentValues
                long id = database.insert(PetContract.PetColumns.TABLE_NAME, null, contentValues);

                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to insert row for " + uri);
                    return null;
                } else
                    // Return the new URI with the ID (of the newly inserted row) appended at the end

                    //Notifier chaque changement dans la table
                    getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }


    //-------------------------------------------------------------------------------DAY 4

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        //Acceder aux tables en mode ecriture pour la suppression

        SQLiteDatabase database = mHelper.getWritableDatabase();
        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case MATCH_PET_TABLE:
                // Delete all rows that match the selection and selection args
                //DELETE FROM TABLE_NAME WHERE (selection)=(selectionArgs)
                rowsDeleted = database.delete(PetContract.PetColumns.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {

                    //Notifier chaque changement dans la table
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;

                case MATCH_INDIVIDUAL_PET:

                    //L'utilisateur veut supprimer un element avec l'id comme selectionArgs
                // Delete a single row given by the ID in the URI
                selection = PetContract.PetColumns.ID_COLUMN + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                    rowsDeleted = database.delete(PetContract.PetColumns.TABLE_NAME, selection, selectionArgs);
                    if (rowsDeleted != 0) {
                        //Notifier chaque changement dans la table
                        getContext().getContentResolver().notifyChange(uri, null);
                    }
                    return rowsDeleted;

                    default:
                        throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

    }

    //---------------------------------------------------------------------------------------DAY 5

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {


        switch (sUriMatcher.match(uri)) {

            case MATCH_PET_TABLE:

                return updatePet(uri, values, selection, selectionArgs);


            case MATCH_INDIVIDUAL_PET:

                selection = PetContract.PetColumns.ID_COLUMN + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }


    }


    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        //Mode ecriture dans les tables

        SQLiteDatabase database = mHelper.getWritableDatabase();


        // check that the name value is not null.
        if (values.containsKey(PetContract.PetColumns.NAME_COLUMN)) {
            String name = values.getAsString(PetContract.PetColumns.NAME_COLUMN);
            if (name.equals("")) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        if (values.containsKey(PetContract.PetColumns.WEIGHT_COLUMN)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetContract.PetColumns.WEIGHT_COLUMN);
            if (weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }


        if (values.size() == 0) {
            return 0;
        }


        // Returns the number of database rows affected by the update statement
        int rowsUpdated = database.update(PetContract.PetColumns.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;

    }

}