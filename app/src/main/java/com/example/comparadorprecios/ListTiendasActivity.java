package com.example.comparadorprecios;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.comparadorprecios.data.model.TiendaListado;
import com.example.comparadorprecios.data.remote.RetrofitClient;
import com.example.comparadorprecios.service.ListTiendasService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Activity que lista las tiendas, permite filtrarlas y seleccionar una de ellas.
 *
 * Santiago Barquero López - 2º DAM
 *
 * https://naps.com.mx/blog/uso-de-un-listview-en-android/
 * https://www.vogella.com/tutorials/AndroidListView/article.html
 */
public class ListTiendasActivity extends AppCompatActivity {

    private EditText edBuscarTienda;
    private ImageButton btnBuscarTienda;
    private ListView lvTiendas;

    // URL base de la Web API
    private String baseUrl;
    // ArrayList de ID de Tienda
    private ArrayList<Integer> idTiendas;
    // ArrayList de Nombre de Tiendas
    private ArrayList<String> nombreTiendas;
    // Servicio para el listado Tiendas
    private ListTiendasService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_tiendas);

        enlazarUI();
        cargarDatosWebApi();

        // Creo adaptador
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_tienda_list, nombreTiendas);
        // Indico que adaptador utiliza el ListView
        lvTiendas.setAdapter(adapter);

        lvTiendas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(ListTiendasActivity.this, TiendaActivity.class);
                intent.putExtra("idTienda", idTiendas.get(position));
                startActivity(intent);
            }
        });
        ocultarTeclado();


    }


    private void enlazarUI() {
        edBuscarTienda = (EditText)findViewById(R.id.edBuscarTienda);
        btnBuscarTienda = (ImageButton)findViewById(R.id.btnBuscarTienda);
        lvTiendas = (ListView)findViewById(R.id.lvTiendas);
    }

    private void cargarDatosWebApi() {
        idTiendas = new ArrayList<>();
        nombreTiendas = new ArrayList<String>();

        // Recupero URL base de la web API de las SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("Configuracion", MODE_PRIVATE);
        baseUrl = prefs.getString("url_base", "http://192.168.1.183:50145/api/");

        Retrofit retrofit = RetrofitClient.getClient(baseUrl);

        service = retrofit.create(ListTiendasService.class);
        // Recupera listado de Tiendas
        listaTiendasBuscadas("");

    }

    private void listaTiendasBuscadas(String textoBuscado) {
        try {
            vaciarListView();

            Call<TiendaListado[]> callListTiendas = service.getListTiendas(textoBuscado);

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
                            Toast.makeText(ListTiendasActivity.this, "Tiendas no encontradas", Toast.LENGTH_LONG).show();
                            //vaciarListView();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ListTiendasActivity.this, "Error al recuperar listado de tiendas.\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        //vaciarListView();
                    }
                }

                // En caso de fallo
                @Override
                public void onFailure(Call<TiendaListado[]> call, Throwable t) {
                    Toast.makeText(ListTiendasActivity.this, "Ocurrió un error al recuperar las tiendas.\n" + t.getMessage(), Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                    //vaciarListView();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void vaciarListView() {
        idTiendas.clear();
        nombreTiendas.clear();
    }

    public void onClickbtnBuscar(View v) {
        listaTiendasBuscadas(edBuscarTienda.getText().toString());
        ocultarTeclado();
    }

    /**
     * Oculta teclado virtual
     *
     * https://es.stackoverflow.com/questions/298/c%C3%B3mo-puedo-abrir-y-cerrar-el-teclado-virtual-soft-keyboard
     */
    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edBuscarTienda.getWindowToken(), 0);
        edBuscarTienda.requestFocus();
    }

    public void onClickNuevaTienda(View view) {
        Intent intent = new Intent(ListTiendasActivity.this, TiendaActivity.class);
        intent.putExtra("idTienda", "0");
        startActivity(intent);
    }
}
