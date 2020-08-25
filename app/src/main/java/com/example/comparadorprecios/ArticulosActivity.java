package com.example.comparadorprecios;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comparadorprecios.auxiliar.FechaHora;
import com.example.comparadorprecios.auxiliar.Utils;
import com.example.comparadorprecios.data.model.Articulo;
import com.example.comparadorprecios.data.model.Precio;
import com.example.comparadorprecios.service.ArticuloService;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Clase controladora del activity de articulos
 *
 * Santiago Barquero López  -2º DAM
 */
public class ArticulosActivity extends AppCompatActivity {

    private  int REQUEST_IMAGE_CAPTURE = 1;

    // UI
    private TextView edId;
    private EditText edDescripcion;
    private EditText edEan;
    private TextView edFecha;
    private ImageView imgArticulo;
    private ImageButton btnGuardar;
    private ImageButton btnBorrar;

    // Tabla
    private TableLayout tablePrecios;
    private TableRow tableRowPrecio;  // Fila tabla
    // Columnas tabla
    private EditText trIdPrecio;
    private EditText trNombreTienda;
    private EditText trPrecio;

    // URL base de la Web API
    private String baseUrl;

    // Servicio web API para el Artículo
    private ArticuloService service;
    // Artículo actual
    private Integer idArticulo;
    private Articulo articulo;

    // Bandera para saber si tenemos que guardar imagen o no
    private boolean guardarImagen = false;

    // HashMap con el listado Id y nombre de las tiendas
    private HashMap<Integer, String> hashMapTiendas;

    /**
     * Al crear el Activity:
     * - Leo el id del artículo pasado como Bundle
     * - Recupero URL base de la web API
     * - Inicializo retrofit y creo el servicio web
     * - Recupero artículo de la web API
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articulos);

        enlazarUI(); // Enlazo con vista

        // Creo un nuevo artículo
        articulo = new Articulo();

        // Recupera la información pasada por el Intent
        Bundle extras = getIntent().getExtras();
        idArticulo = extras.getInt("idArticulo");

        // Recupero URL base de la web API de las SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("Configuracion", MODE_PRIVATE);
        baseUrl = prefs.getString("url_base", "http://192.168.1.193:50145/api/");

        // Inicializo retrofit y creo el servicio web
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ArticuloService.class);

        // Recupero articulo de la web API
        getDatosArticuloFromAPI(idArticulo);

        // Recupera HashMap de Tiendas
        hashMapTiendas = Utils.getHashMapTiendas(this, baseUrl);
    }

    /**
     * Grabo el artículo utilizando el servicio web API (POST o PUT)
     */
    public void onClicGuardar(View view) {
        Call<Articulo> call;

        // Recupera datos de la vista y compruebo que no esté vacío
        articulo.setDescripcion(edDescripcion.getText().toString());
        if (articulo.getDescripcion().trim().isEmpty()) {
            Toast.makeText(this, R.string.descripcion_articulo_vacia, Toast.LENGTH_SHORT).show();
            return;
        }
        articulo.setEan(edEan.getText().toString());
        if (guardarImagen) {
            articulo.setImagen(obtenerImagenBase64());
        }

        // Actualizo la fecha a la actual
        articulo.setFechaAlta(FechaHora.actualBD());

        // Grabo artículo
        // POST Si se trata de un artículo nuevo
        if (articulo.getId() == null) {
            call = service.postArticulo(articulo);
        }
        // PUT si se está editando artículo existente
        else {
            call = service.putArticulo(articulo.getId(), articulo);
        }
        call.enqueue(new Callback<Articulo>() {
            @Override
            public void onResponse(Call<Articulo> call, Response<Articulo> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ArticulosActivity.this, R.string.articulo_guardado, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ArticulosActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
                volverAListadoArticulos();
            }

            @Override
            public void onFailure(Call<Articulo> call, Throwable t) {
                Toast.makeText(ArticulosActivity.this, R.string.error_guardar_articulo, Toast.LENGTH_SHORT).show();
                volverAListadoArticulos();
            }
        });

    }

    /**
     * Elimino artículo utilizando el servicio de la web API (DELETE)
     */
    public void onClicBorrar(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.desea_eliminar_articulo)
                .setCancelable(false)
                // Si confirmo elimino el artículo
                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteArticulo();
                        volverAListadoArticulos();
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
     * Llamada a DELETE del servicio
     */
    private void deleteArticulo() {
        try {
            if (articulo.getId() == null) {
                //finish(); // Retrocedo al intent de listado de tiendas
                return;
            }
            int id = Integer.parseInt(edId.getText().toString());
            Call<Articulo> call = service.deleteArticulo(id);

            call.enqueue(new Callback<Articulo>() {
                @Override
                public void onResponse(Call<Articulo> call, Response<Articulo> response) {
                    // use response.code, response.headers, etc.
                    Toast.makeText(ArticulosActivity.this, R.string.articulo_borrado, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Articulo> call, Throwable t) {
                    // handle failure
                    Toast.makeText(ArticulosActivity.this, R.string.error_borrar_articulo, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ex) {
            Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void enlazarUI() {
        edId = (TextView)findViewById(R.id.tvId);
        edDescripcion = (EditText)findViewById(R.id.edDescripcion);
        edEan = (EditText)findViewById(R.id.edEan);
        edFecha = (TextView)findViewById(R.id.tvFecha);
        tablePrecios = (TableLayout)findViewById(R.id.tablePrecios);
        tableRowPrecio = (TableRow)findViewById(R.id.tableRowPrecio);
        imgArticulo = (ImageView)findViewById(R.id.imgArticulo);
        btnGuardar = (ImageButton)findViewById(R.id.btnGrabarArticulo);
        btnBorrar = (ImageButton)findViewById(R.id.btnEliminarArticulo);
    }

    /**
     * Recupera con GET los datos del artículo con id
     */
    private void getDatosArticuloFromAPI(int id) {
        try {
            Call<Articulo> callArticulo = service.getArticuloId(id);
            callArticulo.enqueue(new Callback<Articulo>() {

                // En caso de respuesta
                @Override
                public void onResponse(Call<Articulo> call, Response<Articulo> response) {
                    try {
                        // Se comprueba que no sea nulo
                        if (response.isSuccessful()) {
                            articulo = response.body(); // Recupero el artículo
                        }
                        visualizar(articulo);
                    } catch (Exception e) {
                        Toast.makeText(ArticulosActivity.this, R.string.error_recuperar_articulo + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }

                // En caso de fallo
                @Override
                public void onFailure(Call<Articulo> call, Throwable t) {
                    Toast.makeText(ArticulosActivity.this, R.string.error_recuperar_articulo + "\n" + t.getMessage(), Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                    finish();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Retrocede al Activity de listado de artículos
     */
    private void volverAListadoArticulos() {
        Intent intent = new Intent(this, ListArticulosActivity.class);
        // Salto al activity del ListadoArticulos
        navigateUpTo(intent);
    }

    /**
     * Muestra el objeto artículo en la vista
     */
    private void visualizar(final Articulo articulo) {
        // Borro fila 1 que contiene ejemplo de precio a cero
        tablePrecios.removeViewAt(1);

        if (articulo.getId() == null) {
            edId.setText("Nuevo");
            edFecha.setText(FechaHora.actual());
        }
        else {
            edId.setText(String.valueOf(articulo.getId()));
            edFecha.setText(FechaHora.decodifica(articulo.getFechaAlta()));
            edDescripcion.setText(articulo.getDescripcion());
            edEan.setText(articulo.getEan());

            // https://es.stackoverflow.com/questions/20180/crear-columnas-de-forma-din%C3%A1mica-en-un-tablelayout-de-android-studio
            for(Precio precio : articulo.getPrecios()) {
                // Creo una fila de la tabla TableRow
                final TableRow tableRow = new TableRow(this);
                tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
                tablePrecios.addView(tableRow);
                // Columna Id Precio
                TextView colIdPrecio = new TextView(this);
                colIdPrecio.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                colIdPrecio.setPadding(0,0,44,0);
                colIdPrecio.setTextSize(18);
                tableRow.addView(colIdPrecio);
                // Establezco el tag
                tableRow.setTag(precio.getId());
                colIdPrecio.setText(String.valueOf(precio.getId()));
                // Columna nombre tienda
                TextView colNombreTienda = new TextView(this);
                tableRow.addView(colNombreTienda);
                colNombreTienda.setPadding(0,0,44,0);
                colNombreTienda.setTextSize(18);
                colNombreTienda.setText(hashMapTiendas.get(precio.getTiendaId()));
                // Columna Precio
                TextView colPrecio = new TextView(this);
                tableRow.addView(colPrecio);
                colPrecio.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                colPrecio.setTextSize(18);
                colPrecio.setText(String.format("%.2f", precio.getImporte()));

                // Añado un listener para cuando se pulsa sobre una fila de la tabla de precios
                // Cuando se pulsa sobre una fila abro el Activity del precio seleccionado
                // https://stackoverflow.com/questions/6976971/index-of-table-row-in-tablelayout
                tableRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // https://stackoverflow.com/questions/6976971/index-of-table-row-in-tablelayout
                        int rowIndex = tablePrecios.indexOfChild(view) - 1; // Resto cabecera
                        int idPrecio = articulo.getPrecios().get(rowIndex).getId();

                        // Abro Activity Precio
                        Intent intent = new Intent(ArticulosActivity.this, PrecioActivity.class);
                        intent.putExtra("idPrecio", idPrecio);
                        intent.putExtra("idArticulo", idArticulo);
                        intent.putExtra("descripcionArticulo", articulo.getDescripcion());
                        startActivity(intent);

                    }
                });
            }

            String imagenBase64 = articulo.getImagen();
            if (imagenBase64 != null) {
                // Convierto imagen en Base64 recibida a un array de Bitmap
                byte[] decodeString = Base64.decode(articulo.getImagen(), Base64.NO_WRAP);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodeString, 0, decodeString.length);
                imgArticulo.setImageBitmap(bitmap);
                guardarImagen = true;
            } else {
                imgArticulo.setImageResource(R.drawable.ic_photo_camera);
            }
            ocultarTeclado();
        }
    }

    public void onClicFoto(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgArticulo.setImageBitmap(imageBitmap);
            guardarImagen = true;
        }
        else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Lectura cancelada", Toast.LENGTH_LONG).show();
                    System.out.println("Se ha cancelado la lectura del codigo de barras");
                } else {
                    edEan.setText(result.getContents());
                    System.out.printf("Código leído: %s%n", result.getContents());
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private String obtenerImagenBase64() {
        // Obtengo el bitmap del ImageView
        Bitmap bitmap = ((BitmapDrawable) imgArticulo.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String fotoEnBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return fotoEnBase64;
    }

    public void onClickeditCodigoBarras(View view) {
        new IntentIntegrator(this).initiateScan(); // iniciar el escaneo
    }

    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edDescripcion.getWindowToken(), 0);
    }

    public void onClickNuevoPrecio(View view) {
        // Si es nuevo artículo debo grabarlo antes
        if (articulo.getId() == null) {
            Toast.makeText(this, R.string.guarde_articulo_antes, Toast.LENGTH_SHORT).show();
            return;
        }

        // Abro Activity Precio
        Intent intent = new Intent(ArticulosActivity.this, PrecioActivity.class);
        intent.putExtra("idPrecio", "0");
        intent.putExtra("idArticulo", idArticulo);
        intent.putExtra("descripcionArticulo", articulo.getDescripcion());
        startActivity(intent);
    }
}
