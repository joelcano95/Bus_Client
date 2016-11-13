package org.joelcano.projectes.autobus;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class Auxiliar extends SQLiteOpenHelper {

    String sqlCreate = "CREATE TABLE posicions (id INTEGER PRIMARY KEY AUTOINCREMENT, matricula TEXT, longitud REAL," +
            " latitud REAL, precision REAL, fecha INTEGER);";

    String sqlCreate2 = "CREATE TABLE busos (matricula TEXT PRIMARY KEY, marca TEXT);";

    public Auxiliar(Context contexto, String nombre, CursorFactory factory, int version) {
        super(contexto, nombre, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sqlCreate);
        db.execSQL(sqlCreate2);
        insert(db);
    }

    public void insert(SQLiteDatabase db){
        db.execSQL("INSERT INTO busos VALUES (0, 'Mercerdes')");
        db.execSQL("INSERT INTO busos VALUES (1, 'Volvo')");
        db.execSQL("INSERT INTO busos VALUES (2, 'Renault')");
        db.execSQL("INSERT INTO busos VALUES (3, 'Scania')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        db.execSQL("DROP TABLE IF EXISTS posicions");

        db.execSQL(sqlCreate);
    }
}