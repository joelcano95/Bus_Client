package org.joelcano.projectes.autobus;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    Spinner spinner;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Auxiliar usdbh =new Auxiliar(this, "DBbusos", null, 1);

        SQLiteDatabase db = usdbh.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT matricula FROM busos", null);
        //fem un array amb les matricules dels busos
        List<String> matricules = new ArrayList<String>() ;
        Integer i=0;
        // recuperem les matricules amb un bucle
        if (c.moveToFirst()) {
            do {
                matricules.add(i, c.getString(0));
                i+=1;
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        //creem l'arrayadapter a partir de l'array de matricules
        ArrayAdapter<String> spinnerAdapter;
        spinnerAdapter = new ArrayAdapter<String>(LoginActivity.this,
                android.R.layout.simple_spinner_item, matricules);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //bolquem l'array a l'spinner
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);

        Button btActivar = (Button) findViewById(R.id.btActivar);
        btActivar.setOnClickListener(this);
        Button btDesactivar = (Button) findViewById(R.id.btDesactivar);
        btDesactivar.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() ==  R.id.btActivar){
            intent = new Intent(LoginActivity.this,
                    LocationService.class);
            intent.putExtra("matricula",spinner.getSelectedItem().toString() );

            startService(intent);
        } else if (v.getId() == R.id.btDesactivar){
            stopService(new Intent(LoginActivity.this,
                    LocationService.class));
        }
    }
}
