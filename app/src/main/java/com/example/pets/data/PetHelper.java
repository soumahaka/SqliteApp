package com.example.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PetHelper extends SQLiteOpenHelper {
    //C'est ici qu'on crée la database sqlite et les tables en question


    //ON DECLARE CES DEUX ATTRIBUTS POUR LES METTRE DANS LE SUPER CONSTRUCTEUR
    public static final String DATABASE_NAME = "shelter.db";
    public static final int DATABASE_VERSION = 1;


    public PetHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    //Création de la table pet
    @Override
    public void onCreate(SQLiteDatabase db) {

        String DEFINE_TABLE=" CREATE TABLE " + PetContract.PetColumns.TABLE_NAME + " (" +
                PetContract.PetColumns.ID_COLUMN + " INTEGER PRIMARY KEY, " +
                PetContract.PetColumns.NAME_COLUMN + " TEXT NOT NULL, " +
                PetContract.PetColumns.BREED_COLUMN + " TEXT, " +
                PetContract.PetColumns.GENDER_COLUMN + " INTEGER NOT NULL, " +
                PetContract.PetColumns.WEIGHT_COLUMN + " INTEGER NOT NULL DEFAULT 0 );";

        db.execSQL(DEFINE_TABLE);

    }


    //Mise à jour de la table pet
    @Override
    public void onUpgrade(SQLiteDatabase mSQLiteDatabase, int oldVersion, int newVersion) {

        // The database is still at version 1, so there's nothing to do be done here.
    }
}
