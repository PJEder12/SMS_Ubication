package com.example.gpsjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //inicializar los tipos de variables
    Button btLocation, btEnviar;
    TextView textView1, textView2, textView3, textView4, textView5, etCel;
    FusedLocationProviderClient fusedLocationProviderClient; //Es el servicio que provee la ubicacion
    boolean SW_Ubi;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Asignar variables y se relacionan con los bloques de activity_main.xml
        btLocation = findViewById(R.id.bt_location);
        btEnviar =findViewById(R.id.bt_SMS);
        textView1 = findViewById(R.id.text_view1);
        textView2 = findViewById(R.id.text_view2);
        textView3 = findViewById(R.id.text_view3);
        textView4 = findViewById(R.id.text_view4);
        textView5 = findViewById(R.id.text_view5);
        etCel = findViewById(R.id.text_View6);


        //initialize fused location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //Lo que se va a ejecutar cuando den click en el boton
        btLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //Check permission
                if (ActivityCompat.checkSelfPermission(MainActivity.this
                        , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Cuando el permiso este concedido habilita metodo getlocalizacion
                    getLocation();


                } else {
                    //cuando  es denegado
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44); //Hace un request nuevamente del permiso
                }
            }
        });

        //check permissions de SMS
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 1); //redundante pedir ambas veces el permiso a los datos del receptor GPS
        }

        //inicializar Boton enviar
        btEnviar.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SmsManager smsManager = SmsManager.getDefault(); //Establece los valores por defecto de

                smsManager.sendTextMessage(etCel.getText().toString(), null, textView1.getText().toString()
                        + "</b> " +textView2.getText().toString(), null,null  );                      //Envío de latitud y longitud via SMS

                Toast.makeText(MainActivity.this,"SMS Enviado", Toast.LENGTH_LONG).show(); //indica en la app cuando pasa por esta parte
            }
        });


    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                SW_Ubi = true;
                while (SW_Ubi == true) {
                    //initialize location
                    Location location = task.getResult(); // No se que hace pero es importante para la localizacion, creo que obtiene todos los datod

                    //El resultado de task.getResult() por alguna razon esta dando null en el dispositivo de Aldair
                    if (location != null) {
                        SW_Ubi = false;
                        try {
                            //initializar geocodigo
                            Geocoder geocoder = new Geocoder(MainActivity.this,
                                    Locale.getDefault());
                            //inicializar lista de direcciones
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1
                            );
                            //conjunto de latitudes en textView
                            textView1.setText(Html.fromHtml(
                                    "<font color = '#6200EE'><b>Latitud :</b><br></font>"
                                            + addresses.get(0).getLatitude()
                            ));
                            //conjunto de longitud
                            textView2.setText(Html.fromHtml(
                                    "<font color = '#6200EE'><b>Longitud :</b><br></font>"
                                            + addresses.get(0).getLongitude()
                            ));
                            //Set country name
                            textView3.setText(Html.fromHtml(
                                    "<font color = '#6200EE'><b> Nombre del país :</b><br></font>"
                                            + addresses.get(0).getCountryName()
                            ));
                            //Set locality
                            textView4.setText(Html.fromHtml(
                                    "<font color = '#6200EE'><b>Localidad :</b><br></font>"
                                            + addresses.get(0).getLocality()
                            ));
                            //set address
                            textView5.setText(Html.fromHtml(
                                    "<font color = '#6200EE'><b>Direccion :</b><br></font>"
                                            + addresses.get(0).getAddressLine(0)
                            ));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        SW_Ubi = true;
                        counter = counter +1; // no es buena hacerla por numero de procesos, es necesario tambien poner un timer entre intentos
                        if(counter>=1000){
                            SW_Ubi = false;
                            counter = 0;
                            Toast.makeText(MainActivity.this,"No se encontró Ubicacion", Toast.LENGTH_LONG).show();
                        }

                    }
                }
            }
        });
    }
}