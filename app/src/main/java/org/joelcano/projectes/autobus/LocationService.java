package org.joelcano.projectes.autobus;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.Calendar;

public class LocationService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    Auxiliar usdbh;
    SQLiteDatabase db;
    String matricula;
    int comptador = 0;

    @Override
    public void onCreate() {
        Toast.makeText(this, "Servicio creado",
                Toast.LENGTH_SHORT).show();

       //sucio codigo escrito por mi
        usdbh =new Auxiliar(this, "DBbusos", null, 1);
        db = usdbh.getWritableDatabase();
    }

    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {
        matricula =(String) intenc.getExtras().get("matricula");

        Toast.makeText(this,"Servicio arrancado "+ matricula,
                Toast.LENGTH_SHORT).show();

        actualizarPosicion();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "MyService Stopped", Toast.LENGTH_LONG).show();
        locationManager.removeUpdates(locationListener);
        stopSelf();
    }

    private void actualizarPosicion() {
        Log.i("LocAndroid", "Part superior metode");
        //Obtenemos una referencia al LocationManager
        locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Nos registramos para recibir actualizaciones de la posicion
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                subePosicion(location);
                comptador++;
                Log.i("LocAndroid", "comptador :"+comptador);
            }
            public void onProviderDisabled(String provider){
                Log.i("LocAndroid", "desacticat");
            }
            public void onProviderEnabled(String provider){
                Log.i("LocAndroid", "acticat");
            }
            public void onStatusChanged(String provider, int status, Bundle extras){
                Log.i("LocAndroid", "Provider Status: " + status);
            }
        };
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 15000, 0, locationListener);
    }

    private void subePosicion(Location loc) {
        if(loc != null)
        {
            ContentValues nuevoRegistro = new ContentValues();
            nuevoRegistro.put("matricula", matricula);
            nuevoRegistro.put("longitud", loc.getLongitude());
            nuevoRegistro.put("latitud", loc.getLatitude());
            nuevoRegistro.put("precision", loc.getAccuracy());
            nuevoRegistro.put("fecha", Calendar.getInstance().getTimeInMillis() / 1000L);

            db.insert("posicions", null, nuevoRegistro);

            //TRATAMOS DE INSERTAR A BD EXTERNA
            TareaWSInsertar tarea = new TareaWSInsertar();
            tarea.execute(
                    "0",
                    matricula,
                    String.valueOf(loc.getLongitude()),
                    String.valueOf(loc.getLatitude()),
                    String.valueOf(loc.getAccuracy()),
                    String.valueOf(Calendar.getInstance().getTimeInMillis() / 1000L));

            Log.i("nuevo registro", nuevoRegistro.toString());
            Toast.makeText(LocationService.this, "iNSERT N: "+comptador,
                    Toast.LENGTH_SHORT).show();
            comptador++;

            Log.i("LocAndroid", String.valueOf(loc.getLatitude() + " - " + String.valueOf(loc.getLongitude())));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //CODIGO ADICIONAL JOEL MARTES 15
    private class TareaWSInsertar extends AsyncTask<String,Integer,Boolean> {

        protected Boolean doInBackground(String... params) {

            boolean resul = true;

            HttpClient httpClient = new DefaultHttpClient();

            HttpPost post = new HttpPost("http://192.168.0.155:8080/WebApplication1/webresources/posicions.posicions?");
            post.setHeader("content-type", "application/json");

            try
            {
                //Construimos el objeto cliente en formato JSON
                JSONObject dato = new JSONObject();

                dato.put("id", params[0]);
                dato.put("matricula", params[1]);
                dato.put("longitud", Float.parseFloat(params[2]));
                dato.put("latitud", Float.parseFloat(params[3]));
                dato.put("precision", Float.parseFloat(params[4]));
                dato.put("fecha", Integer.parseInt(params[5]));

                StringEntity entity = new StringEntity(dato.toString());
                post.setEntity(entity);

                HttpResponse resp = httpClient.execute(post);
                String respStr = EntityUtils.toString(resp.getEntity());

                if(!respStr.equals("true"))
                    resul = false;
            }
            catch(Exception ex)
            {
                Log.e("ServicioRest","Error!", ex);
                resul = false;
            }

            return resul;
        }

        protected void onPostExecute(Boolean result) {

            if (result)
            {
                //lblResultado.setText("Insertado OK.");
            }
        }
    }
}
