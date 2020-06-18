package com.example.pets.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public final class PetContract {


    private PetContract() {
    }


    // Construction des elements des URI des tables de la database (pets) pour l'acceder
    //URI= content:// + nom de package du projet(authority)/ + nom de la table dans la database(path)

    public static final String AUTHORITY = "com.example.pets";
    public static final Uri PREFIX_AND_AUTHORITY = Uri.parse("content://" + AUTHORITY);
    public static final String PATH = "pets";


    //Construction des colonnes des tables de la database

    public static final class PetColumns implements BaseColumns{

        //Construction du chemin complet pour acceder a la table pets

        public static final Uri PREFIX_AND_AUTHORITY_AND_PATH = Uri.withAppendedPath(PREFIX_AND_AUTHORITY,PATH);


        //------------------------------------------------------------------

        //Squelette de la table pet, definition des noms de colonnes et nom de la table
        public static final String TABLE_NAME="pets";
        public static final String ID_COLUMN=BaseColumns._ID;
        public static final String NAME_COLUMN="name";
        public static final String BREED_COLUMN="breed";
        public static final String GENDER_COLUMN="gender";
        public static final String WEIGHT_COLUMN="weight";

        //--------------------------------------------------------------------

        //Definition des valeurs possibles que peut prendre la colonne gender(voire editText)
        public static final int GENDER_UNKNOWN_VALUE=0;
        public static final int GENDER_MALE_VALUE=1;
        public static final int GENDER_FEMALE_VALUE=2;



        //-----------------------------------------------------------------------


        //Encore à comprendre...
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH;


        //-------------------------------------------------------------------
        // Methode pour tester si le type de gender entré dans editText est correcte

        public static boolean isValidGender(int gender) {
            if (gender == GENDER_UNKNOWN_VALUE || gender == GENDER_MALE_VALUE || gender == GENDER_FEMALE_VALUE) {
                return true;
            }
            return false;
        }


        }

    }
