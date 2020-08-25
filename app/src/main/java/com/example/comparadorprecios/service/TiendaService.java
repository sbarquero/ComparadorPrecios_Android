package com.example.comparadorprecios.service;

import com.example.comparadorprecios.data.model.Tienda;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Interface de servicio de Tienda
 *
 * Contiene los endpoints de la web API para las tiendas
 *
 * Santiago Barquero López - 2º DAM
 */
public interface TiendaService {

    @GET("Tiendas/{idTienda}")
    Call<Tienda> getTienda(@Path("idTienda") int idTienda);

    @POST("Tiendas/")
    Call<Tienda> postTienda(@Body Tienda tienda);

    @PUT("Tiendas/{idTienda}")
    Call<Tienda> putTienda(@Path("idTienda") int idTienda, @Body Tienda tienda);

    @DELETE("Tiendas/{idTienda}")
    Call<Tienda> deleteTienda(@Path("idTienda") int idTienda);

}
