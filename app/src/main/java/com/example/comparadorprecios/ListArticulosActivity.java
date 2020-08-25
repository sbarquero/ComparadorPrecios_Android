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

import com.example.comparadorprecios.data.model.ArticuloListado;
import com.example.comparadorprecios.data.remote.RetrofitClient;
import com.example.comparadorprecios.service.ListArticulosService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Activity que lista los artículos, permite filtrarlos y seleccionar uno de ellos.
 *
 * Santiago Barquero López - 2º DAM
 *
 * https://naps.com.mx/blog/uso-de-un-listview-en-android/
 * https://www.vogella.com/tutorials/AndroidListView/article.html
 */
public class ListArticulosActivity extends AppCompatActivity {

    private EditText edBuscarArticulo;
    private ImageButton btnBuscarArticulo;
    private ListView lvArticulos;

    // URL base de la Web API
    private String baseUrl;
    // ArrayList de ID de Artículos
    private ArrayList<Integer> idArticulos;
    // ArrayList de Descripciones de artículos
    private ArrayList<String> descripcionArticulos;
    // Servicio para el listado Artículos
    private ListArticulosService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_articulos);

        enlazarUI();
        cargarDatosWebApi();

        // Creo adaptador
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_articulo_list, descripcionArticulos);
        // Indico que adaptador utiliza el ListView
        lvArticulos.setAdapter(adapter);

        lvArticulos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(ListArticulosActivity.this, ArticulosActivity.class);
                intent.putExtra("idArticulo", idArticulos.get(position));
                startActivity(intent);
            }
        });
        ocultarTeclado();
    }

    private void enlazarUI() {
        edBuscarArticulo = (EditText)findViewById(R.id.edBuscarArticulo);
        btnBuscarArticulo = (ImageButton)findViewById(R.id.btnBuscarArticulo);
        lvArticulos = (ListView)findViewById(R.id.lvArticulos);
    }

    private void cargarDatosWebApi() {
        idArticulos = new ArrayList<>();
        descripcionArticulos = new ArrayList<String>();

        // Recupero URL base de la web API de las SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("Configuracion", MODE_PRIVATE);
        baseUrl = prefs.getString("url_base", "http://192.168.1.193:50145/api/");

        Retrofit retrofit = RetrofitClient.getClient(baseUrl);

        service = retrofit.create(ListArticulosService.class);
        // Recupera listado de Artículos
        listaArticulosBuscados("");

    }

    private void listaArticulosBuscados(String textoBuscado) {
        try {
            vaciarListView();

            Call<ArticuloListado[]> callListArticulos = service.getListArticulos(textoBuscado);

            callListArticulos.enqueue(new Callback<ArticuloListado[]>() {

                // En caso de respuesta
                @Override
                public void onResponse(Call<ArticuloListado[]> call, Response<ArticuloListado[]> response) {
                    try {
                        // Se comprueba que no sea nulo
                        if (response.isSuccessful()) {
                            ArticuloListado[] articulos = response.body();
                            for (ArticuloListado articulo : articulos) {
                                idArticulos.add(articulo.getId());
                                descripcionArticulos.add(articulo.getDescripcion());
                            }
                        }
                        else {
                            Toast.makeText(ListArticulosActivity.this, R.string.articulos_no_encontrados, Toast.LENGTH_LONG).show();
                            vaciarListView();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ListArticulosActivity.this, R.string.error_recuperar_articulos  + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        vaciarListView();
                    }
                }

                // En caso de fallo
                @Override
                public void onFailure(Call<ArticuloListado[]> call, Throwable t) {
                    Toast.makeText(ListArticulosActivity.this, R.string.error_recuperar_articulos  + "\n" + t.getMessage(), Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                    vaciarListView();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void vaciarListView() {
        idArticulos.clear();
        descripcionArticulos.clear();
    }

    public void onClickbtnBuscar(View v) {
        ocultarTeclado();
        listaArticulosBuscados(edBuscarArticulo.getText().toString());
    }

    /**
     * Oculta teclado virtual
     *
     * https://es.stackoverflow.com/questions/298/c%C3%B3mo-puedo-abrir-y-cerrar-el-teclado-virtual-soft-keyboard
     */
    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edBuscarArticulo.getWindowToken(), 0);
        edBuscarArticulo.requestFocus();
    }

    public void onClickNuevoArticulo(View view) {
        Intent intent = new Intent(ListArticulosActivity.this, ArticulosActivity.class);
        intent.putExtra("idArticulo", "0");
        startActivity(intent);
    }

}
