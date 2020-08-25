package com.example.comparadorprecios.service;

import com.example.comparadorprecios.data.model.Articulo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Interface de servicio de Articulo
 *
 * Contiene los endpoints de la web API para los artículos
 *
 * Santiago Barquero López - 2º DAM
 */
public interface ArticuloService {

    @GET("Articulos/{idArticulo}")
    Call<Articulo> getArticuloId(@Path("idArticulo") int idArticulo);

    @GET("Articulos/EAN/{eanArticulo}")
    Call<Articulo> getArticuloEan(@Path("eanArticulo") String eanArticulo);

    @POST("Articulos/")
    Call<Articulo> postArticulo(@Body Articulo articulo);

    @PUT("Articulos/{idArticulo}")
    Call<Articulo> putArticulo(@Path("idArticulo") int idArticulo, @Body Articulo articulo);

    @DELETE("Articulos/{idArticulo}")
    Call<Articulo> deleteArticulo(@Path("idArticulo") int idArticulo);

}
