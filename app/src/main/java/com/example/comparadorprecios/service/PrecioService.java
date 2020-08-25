package com.example.comparadorprecios.service;

import com.example.comparadorprecios.data.model.Precio;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Interface de servicio de Precio
 *
 * Contiene los endpoints de la web API para los precios
 *
 * Santiago Barquero López - 2º DAM
 */
public interface PrecioService {

    @GET("Precios/{idPrecio}")
    Call<Precio> getPrecioId(@Path("idPrecio") int idPrecio);

    @POST("Precios/")
    Call<Precio> postPrecio(@Body Precio precio);

    @PUT("Precios/{idPrecio}")
    Call<Precio> putPrecio(@Path("idPrecio") int idPrecio, @Body Precio precio);

    @DELETE("Precios/{idPrecio}")
    Call<Precio> deletePrecio(@Path("idPrecio") int idPrecio);

}
