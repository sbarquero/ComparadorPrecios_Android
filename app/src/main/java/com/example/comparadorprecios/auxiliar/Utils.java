package com.example.comparadorprecios.auxiliar;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import com.example.comparadorprecios.data.model.TiendaListado;
import com.example.comparadorprecios.data.remote.RetrofitClient;
import com.example.comparadorprecios.service.ListTiendasService;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Utils
{
    /**
     * Codifica array de byte recibido en un string en Base64.
     * @param datos Array de byte con los datos a codificar en Base64
     * @return Devuelve string que contiene el array codificado en Base64
     */
    public static String encodeBase64(byte[] datos) {
        return Base64.encodeToString(datos, Base64.NO_WRAP);
    }

    /**
     * Decodifica string recibido en Base64 a un array de bytes.
     * @param cadenaBase64 Contiene string en Base64 a decodificar
     * @return Array de bytes que contiene el string decodificado
     */
    public static byte[] decodeBase64(String cadenaBase64) {
        return Base64.decode(cadenaBase64, Base64.NO_WRAP);
    }

    /**
     * Creo HashMap tiendas con todas la tiendas que devuelve la Web API
     */
    public static HashMap<Integer, String> getHashMapTiendas(final Context context, String baseUrl) {
        final HashMap<Integer, String> tiendas = new HashMap<>();

        Retrofit retrofit = RetrofitClient.getClient(baseUrl);
        ListTiendasService service = retrofit.create(ListTiendasService.class);
        Call<TiendaListado[]> callListTiendas = service.getListTiendas("");

        callListTiendas.enqueue(new Callback<TiendaListado[]>() {

            // En caso de respuesta
            @Override
            public void onResponse(Call<TiendaListado[]> call, Response<TiendaListado[]> response) {
                try {
                    // Se comprueba que no sea nulo
                    if (response.isSuccessful()) {
                        TiendaListado[] arrayTiendas = response.body();
                        for (TiendaListado tienda : arrayTiendas) {
                            tiendas.put(tienda.getId(), tienda.getNombre());
                        }
                    }
                    else {
                        Toast.makeText(context, "Tiendas no encontradas", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Error al recuperar listado de tiendas.\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            // En caso de fallo
            @Override
            public void onFailure(Call<TiendaListado[]> call, Throwable t) {
                Toast.makeText(context, "Ocurrió un error al recuperar las tiendas.\n" + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
        return tiendas;
    }


}
