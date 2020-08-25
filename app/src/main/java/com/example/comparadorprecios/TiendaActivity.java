package com.example.comparadorprecios;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.example.comparadorprecios.auxiliar.FechaHora;
import com.example.comparadorprecios.data.model.Tienda;
import com.example.comparadorprecios.service.TiendaService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Clase controladora del activity de Tienda
 *
 * Santiago Barquero López  -2º DAM
 */
public class TiendaActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    // UI
    private TextView tvId;
    private TextView tvFecha;
    private EditText edNombre;
    private EditText edLatitud;
    private EditText edLongitud;
    private MapView mapView;
    private ImageButton btnNuevo;
    private ImageButton btnGuardar;
    private ImageButton btnEliminar;

    // Tienda actual
    private Integer idTienda;
    private Tienda tienda;
    // URL base de la Web API
    private String baseUrl;
    // Servicio para las peticiones de Articulo de la Web API
    private TiendaService service;
    // Localización y mapa
    private GoogleMap map;
    private SupportMapFragment mapaFragment;
    private FusedLocationProviderClient fusedLocationClient;
    private Location localizacion;
    private double lat = 0;
    private double lng = 0;

    /**
     * Al crear el Activity:
     * - Leo el id de la tienda pasado como Bundle
     * - Recupero URL base de la web API
     * - Inicializo retrofit y creo el servicio web
     * - Recupero tienda de la web API
     * - Recupero el fragamento de mapa
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tienda);
        // Para conocer la ubicación más reciente
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        conocerUbicacionMasReciente();

        enlazarUI(); // Enlazo con vista

        // Creo nueva tienda
        tienda = new Tienda();

        // Recupera la información pasada por el Intent
        Bundle extras = getIntent().getExtras();
        idTienda = extras.getInt("idTienda");

        // Recupero URL base de la web API de las SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("Configuracion", MODE_PRIVATE);
        baseUrl = prefs.getString("url_base", "http://192.168.1.183:50145/api/");

        // Inicializo retrofit y creo el servicio web
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(TiendaService.class);

        // Recupero tienda de la web API
        getDatosTiendaFromAPI(idTienda);

        // Recupero el fragamento de mapa
        mapaFragment.getMapAsync(this);
    }

    /**
     * Método sobreescrito que implementa las acciones a realizar cuando se ha terminado de cargar
     * el mapa.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        // Establezco escuchador de eventos
        map.setOnMapClickListener(this);
    }

    /**
     * Al hacer click sobre el mapa toma el punto del mapa como la nueva ubicación de la tienda.
     *
     * @param latLng Localización del punto del mapa pulsado
     */
    @Override
    public void onMapClick(LatLng latLng) {

        lat = latLng.latitude;
        lng = latLng.longitude;
        edLatitud.setText(String.valueOf(lat));
        edLongitud.setText(String.valueOf(lng));
        latLng = new LatLng(lat, lng);
        map.clear();
        map.addMarker(new MarkerOptions().position(latLng).title(tienda.getNombre()));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
    }

    /**
     * Grabo la tienda utilizando el servicio web API (POST o PUT)
     */
    public void onClickGuardarTienda(View v) {
        Call<Tienda> call;

        // Recupera datos de la vista y compruebo que no esté vacío
        tienda.setNombre(edNombre.getText().toString());
        if (tienda.getNombre().trim().isEmpty()) {
            Toast.makeText(this, R.string.nombre_tienda_vacio, Toast.LENGTH_SHORT).show();
            return;
        }
        // Compruebo y establezco Latitud
        String strLatitud = edLatitud.getText().toString();
        if (!strLatitud.isEmpty()) {
            Double latitud = Double.valueOf(strLatitud);
            if (latitud >= -90.0 && latitud <= 90.0) {
                tienda.setLatitud(latitud);
            } else {
                Toast.makeText(this, R.string.latitud_error, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Compruebo y establezco Longitud
        String strLongitud = edLongitud.getText().toString();
        if (!strLongitud.isEmpty()) {
            Double longitud = Double.valueOf(strLongitud);
            if (longitud >= -180 && longitud <= 180) {
                tienda.setLongitud(Double.valueOf(edLongitud.getText().toString()));
            } else {
                Toast.makeText(this, R.string.longitud_error, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Actualizo la fecha a la actual
        tienda.setFechaAlta(FechaHora.actualBD());

        // Grabo tienda
        if (tienda.getId() == null) { // Nueva Tienda => POST
            call = service.postTienda(tienda);
        } else { // Modifico Tienda => PUT
            call = service.putTienda(tienda.getId(), tienda);
        }
        // Callback de la grabación de la tienda
        call.enqueue(new Callback<Tienda>() {
            @Override
            public void onResponse(Call<Tienda> call, Response<Tienda> response) {
                if (response.isSuccessful()) {
                    // use response.code, response.headers, etc.
                    Toast.makeText(TiendaActivity.this, R.string.tienda_guardada, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(TiendaActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
                volverAListadoTiendas();
            }

            @Override
            public void onFailure(Call<Tienda> call, Throwable t) {
                // handle failure
                Toast.makeText(TiendaActivity.this, R.string.error_guardar_tienda, Toast.LENGTH_SHORT).show();
                volverAListadoTiendas();
            }
        });
    }

    /**
     * Elimino tienda utilizando el servicio de la web API (DELETE)
     */
    public void onClickEliminarTienda(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.desea_eliminar_tienda)
                .setCancelable(false)
                // Si confirmo elimino la tienda
                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteTienda();
                        volverAListadoTiendas();
                    }
                })
                // Si no confirmo eliminación no hago nada
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle(R.string.salir);
        alert.show();
    }

    /**
     * Al pulsar el botón con el icono del mapa permite ver en el mapa la ubicación introducida
     * en los EditText de latitud y longitud.
     */
    public void onClickVerUbicacion(View view) {
        try {
            lat = Double.parseDouble(edLatitud.getText().toString());
        } catch (Exception ex) {
            lat = 0;
        }
        try {
            lng = Double.parseDouble(edLongitud.getText().toString());
        } catch (Exception ex) {
            lng = 0;
        }
        visualizarMapa();
        ocultarTeclado();
    }

    /**
     * Llamada a DELETE del servicio
     */
    private void deleteTienda() {
        try {
            if (tienda.getId() == null) {
                //finish(); // Retrocedo al intent de listado de tiendas
                return;
            }
            int id = Integer.parseInt(tvId.getText().toString());
            Call<Tienda> call = service.deleteTienda(id);

            call.enqueue(new Callback<Tienda>() {
                @Override
                public void onResponse(Call<Tienda> call, Response<Tienda> response) {
                    // use response.code, response.headers, etc.
                    Toast.makeText(TiendaActivity.this, R.string.tienda_borrada, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Tienda> call, Throwable t) {
                    // handle failure
                    Toast.makeText(TiendaActivity.this, R.string.error_borrar_tienda, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ex) {
            Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Enlazo interfaz de usuario
     */
    private void enlazarUI() {
        tvId = (TextView) findViewById(R.id.tvId);
        tvFecha = (TextView) findViewById(R.id.tvFecha);
        edNombre = (EditText) findViewById(R.id.edNombre);
        edLatitud = (EditText) findViewById(R.id.edLatitud);
        edLongitud = (EditText) findViewById(R.id.edLongitud);
        btnGuardar = (ImageButton) findViewById(R.id.btnGrabarTienda);
        btnEliminar = (ImageButton) findViewById(R.id.btnEliminarTienda);
        mapaFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapaFragment);
    }

    /**
     * Recupera con GET los datos de la tienda con id
     */
    private void getDatosTiendaFromAPI(int id) {
        try {
            Call<Tienda> callTienda = service.getTienda(id);
            callTienda.enqueue(new Callback<Tienda>() {

                // En caso de respuesta
                @Override
                public void onResponse(Call<Tienda> call, Response<Tienda> response) {
                    try {
                        // Se comprueba que no sea nulo
                        if (response.isSuccessful()) {
                            tienda = response.body();
                        }
                        visualizar(tienda);
                    } catch (Exception e) {
                        Toast.makeText(TiendaActivity.this, R.string.error_recuperar_tienda + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }

                // En caso de fallo
                @Override
                public void onFailure(Call<Tienda> call, Throwable t) {
                    Toast.makeText(TiendaActivity.this, R.string.error_recuperar_tienda + "\n" + t.getMessage(), Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                    finish();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Muestra el objeto tienda en la vista
     */
    private void visualizar(Tienda tienda) {

        if (tienda.getId() == null) {
            tvId.setText("Nueva");
            tvFecha.setText(FechaHora.actual());
        } else {
            tvId.setText(String.valueOf(tienda.getId()));
            tvFecha.setText(FechaHora.decodifica(tienda.getFechaAlta()));
            edNombre.setText(tienda.getNombre());
            if (tienda.getLatitud() != null) {
                edLatitud.setText(String.valueOf(tienda.getLatitud()));
                lat = tienda.getLatitud();
            }
            if (tienda.getLongitud() != null) {
                edLongitud.setText(String.valueOf(tienda.getLongitud()));
                lng = tienda.getLongitud();
            }
        }
        visualizarMapa();
    }

    /**
     * Retrocede al Activity de listado de tiendas
     */
    private void volverAListadoTiendas() {
        Intent intent = new Intent(this, ListTiendasActivity.class);
        // Salto al activity del ListadoTiendas
        navigateUpTo(intent);
    }

    /**
     * Conocer la ubicación más reciente
     */
    private void conocerUbicacionMasReciente() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            localizacion = location;
                        }
                    }
                });
    }

    /**
     * Calculo la latitud y longitud actual y visualizo mapa del fragment
     */
    private void visualizarMapa() {
        int zoom = 0;
        map.clear();
        LatLng latLngTienda;
        if (lat == 0 && lng == 0) {
            if (localizacion == null) {
                lat = 40.4636688;
                lng = -3.7492199;
                zoom = 5;
            } else {
                lat = localizacion.getLatitude();
                lng = localizacion.getLongitude();
                zoom = 12;
            }
            latLngTienda = new LatLng(lat, lng);
        } else if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            latLngTienda = new LatLng(0, 0);
        } else {
            zoom = 18;
            latLngTienda = new LatLng(lat, lng);
            map.addMarker(new MarkerOptions().position(latLngTienda)
                    .title(tienda.getNombre()));
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngTienda, zoom));
    }

    /**
     * Oculta teclado virtual
     * <p>
     * https://es.stackoverflow.com/questions/298/c%C3%B3mo-puedo-abrir-y-cerrar-el-teclado-virtual-soft-keyboard
     */
    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edNombre.getWindowToken(), 0);
    }
}
