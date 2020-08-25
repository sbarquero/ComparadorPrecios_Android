package com.example.comparadorprecios;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comparadorprecios.auxiliar.FechaHora;
import com.example.comparadorprecios.auxiliar.Utils;
import com.example.comparadorprecios.data.model.Precio;
import com.example.comparadorprecios.data.model.TiendaListado;
import com.example.comparadorprecios.data.remote.RetrofitClient;
import com.example.comparadorprecios.service.ListTiendasService;
import com.example.comparadorprecios.service.PrecioService;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Clase controladora del activity de Precios
 *
 * Santiago Barquero López  -2º DAM
 */
public class PrecioActivity extends AppCompatActivity {

    //UI
    private TextView tvId;
    private TextView tvFecha;
    private TextView tvDescripcionArticulo;
    private EditText edPrecio;
    private TextView tvNombreTienda;
    private ListView lvTiendas;
    private ImageButton btnGrabarPrecio;
    private ImageButton btnEliminarPrecio;

    // URL base de la Web API
    private String baseUrl;
    // Precio actual
    private Integer idPrecio;
    private Integer idArticulo;
    private String descripcionArticulo;
    private Precio precio;
    // Servicio web API para el Artículo
    private PrecioService servicePrecio;
    // Servicio web API para las tiendas
    private ListTiendasService serviceListTiendas;
    // HashMap con el listado Id y nombre de las tiendas
    private HashMap<Integer, String> hashMapTiendas;
    // ArrayList de ID de Tienda
    private ArrayList<Integer> idTiendas;
    // ArrayList de Nombre de Tiendas
    private ArrayList<String> nombreTiendas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_precio);

        enlazarUI(); // Enlazo con vista

        // Creo un nuevo artículo
        precio = new Precio();

        // Recupera la información pasada por el Intent
        Bundle extras = getIntent().getExtras();
        idPrecio = extras.getInt("idPrecio");
        idArticulo = extras.getInt("idArticulo");
        descripcionArticulo = extras.getString("descripcionArticulo");

        // Recupero URL base de la web API de las SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("Configuracion", MODE_PRIVATE);
        baseUrl = prefs.getString("url_base", "http://192.168.1.183:50145/api/");

        // Inicializo retrofit y creo el servicio web
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        servicePrecio = retrofit.create(PrecioService.class);

        // Recupero precio de la web API
        getDatosPrecioFromAPI(idPrecio);

        cargarDatosTiendas();

        // Creo adaptador listView tiendas
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_tienda_list_precio, nombreTiendas);
        // Indico que adaptador utiliza el ListView
        lvTiendas.setAdapter(adapter);

        lvTiendas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                precio.setTiendaId(idTiendas.get(position));
                tvNombreTienda.setText(nombreTiendas.get(position));
            }
        });
    }

    private void cargarDatosTiendas() {
        // Recupera HashMap de Tiendas
        hashMapTiendas = Utils.getHashMapTiendas(this, baseUrl);

        idTiendas = new ArrayList<>();
        nombreTiendas = new ArrayList<String>();

        // Recupero URL base de la web API de las SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("Configuracion", MODE_PRIVATE);
        baseUrl = prefs.getString("url_base", "http://192.168.1.183:50145/api/");

        Retrofit retrofit = RetrofitClient.getClient(baseUrl);

        serviceListTiendas = retrofit.create(ListTiendasService.class);

        try {
            vaciarListView();

            Call<TiendaListado[]> callListTiendas = serviceListTiendas.getListTiendas("");

            callListTiendas.enqueue(new Callback<TiendaListado[]>() {

                // En caso de respuesta
                @Override
                public void onResponse(Call<TiendaListado[]> call, Response<TiendaListado[]> response) {
                    try {
                        // Se comprueba que no sea nulo
                        if (response.isSuccessful()) {
                            TiendaListado[] tiendas = response.body();
                            for (TiendaListado tienda : tiendas) {
                                idTiendas.add(tienda.getId());
                                nombreTiendas.add(tienda.getNombre());
                            }
                        }
                        else {
                            Toast.makeText(PrecioActivity.this, "Tiendas no encontradas", Toast.LENGTH_LONG).show();
                            vaciarListView();
                        }
                    } catch (Exception e) {
                        Toast.makeText(PrecioActivity.this, "Error al recuperar listado de tiendas.\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        vaciarListView();
                    }
                }

                // En caso de fallo
                @Override
                public void onFailure(Call<TiendaListado[]> call, Throwable t) {
                    Toast.makeText(PrecioActivity.this, "Ocurrió un error al recuperar las tiendas.\n" + t.getMessage(), Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                    vaciarListView();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void vaciarListView() {
        //nombreTiendas.clear();
    }

    private void enlazarUI() {
        tvId = (TextView)findViewById(R.id.tvId);
        tvFecha = (TextView)findViewById(R.id.tvFecha);
        tvDescripcionArticulo = (TextView)findViewById(R.id.tvDescripcionArticulo);
        edPrecio = (EditText) findViewById(R.id.edPrecio);
        tvNombreTienda = (TextView) findViewById(R.id.tvNombreTienda);
        lvTiendas = (ListView) findViewById(R.id.lvTiendas);
        btnGrabarPrecio = (ImageButton) findViewById(R.id.btnGrabarPrecio);
        btnEliminarPrecio = (ImageButton) findViewById(R.id.btnEliminarPrecio);
    }

    /**
     * Recupera con GET los datos del artículo con id
     */
    private void getDatosPrecioFromAPI(int id) {
        try {
            Call<Precio> callPrecio = servicePrecio.getPrecioId(id);
            callPrecio.enqueue(new Callback<Precio>() {

                // En caso de respuesta
                @Override
                public void onResponse(Call<Precio> call, Response<Precio> response) {
                    try {
                        // Se comprueba que no sea nulo
                        if (response.isSuccessful()) {
                            precio = response.body();
                        }
                        visualizar(precio);
                    } catch (Exception e) {
                        Toast.makeText(PrecioActivity.this, R.string.error_recuperar_precio + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }

                // En caso de fallo
                @Override
                public void onFailure(Call<Precio> call, Throwable t) {
                    Toast.makeText(PrecioActivity.this, R.string.error_recuperar_precio + "\n" + t.getMessage(), Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                    finish();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Muestra el objeto precio en la vista
     */
    private void visualizar(final Precio precio) {
        tvDescripcionArticulo.setText(descripcionArticulo);
        if (precio.getId() == null) {
            tvId.setText("Nuevo");
            tvFecha.setText(FechaHora.actual());
        } else {
            tvId.setText(String.valueOf(precio.getId()));
            tvFecha.setText(FechaHora.decodifica(precio.getFecha()));
            tvDescripcionArticulo.setText(descripcionArticulo);
            edPrecio.setText(String.valueOf(precio.getImporte()));
            tvNombreTienda.setText(hashMapTiendas.get(precio.getTiendaId()));
        }
    }

    public void onClickGuardarPrecio(View v) {
        Call<Precio> call;


        // Recupera datos de la vista y compruebo que no esté vacío
        if (tvNombreTienda.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.tienda_no_seleccionada, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Double doublePrecio = Double.parseDouble(edPrecio.getText().toString());
            precio.setImporte(doublePrecio);
        } catch (Exception ex) {
            Toast.makeText(this, R.string.precio_no_valido, Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizo la fecha a la actual
        precio.setFecha(FechaHora.actualBD());

        // Grabo precio
        // POST Si se trata de un precio nuevo
        if (precio.getId() == null) {
            precio.setArticuloId(idArticulo);
            call = servicePrecio.postPrecio(precio);
        }
        // PUT si se está editando artículo existente
        else {
            call = servicePrecio.putPrecio(precio.getId(), precio);
        }
        call.enqueue(new Callback<Precio>() {
            @Override
            public void onResponse(Call<Precio> call, Response<Precio> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PrecioActivity.this, R.string.precio_guardado, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(PrecioActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
                volveraArticulo();
            }

            @Override
            public void onFailure(Call<Precio> call, Throwable t) {
                Toast.makeText(PrecioActivity.this, R.string.error_guardar_precio, Toast.LENGTH_SHORT).show();
                volveraArticulo();
            }
        });
    }

    public void onClickEliminarPrecio(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.desea_eliminar_precio)
                .setCancelable(false)
                // Si confirmo elimino el artículo
                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deletePrecio();
                        volveraArticulo();
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

    private void volveraArticulo() {
        Intent intent = new Intent(PrecioActivity.this, ArticulosActivity.class);
        intent.putExtra("idArticulo", idArticulo);
        // Salto al activity de Articulos
        navigateUpTo(intent);
    }

    /**
     * Llamada a DELETE del servicio
     */
    private void deletePrecio() {
        try {
            if (precio.getId() == null) {
                //finish(); // Retrocedo al intent articulo
                return;
            }
            Call<Precio> call = servicePrecio.deletePrecio(idPrecio);

            call.enqueue(new Callback<Precio>() {
                @Override
                public void onResponse(Call<Precio> call, Response<Precio> response) {
                    // use response.code, response.headers, etc.
                    Toast.makeText(PrecioActivity.this, R.string.precio_borrado, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Precio> call, Throwable t) {
                    // handle failure
                    Toast.makeText(PrecioActivity.this, R.string.error_borrar_precio, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ex) {
            Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
