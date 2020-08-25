package com.example.comparadorprecios.service;

import com.example.comparadorprecios.data.model.ArticuloListado;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface de servicio de ListArticulo
 *
 * Contiene los endpoints de la web API para el listado de artículos
 *
 * Santiago Barquero López - 2º DAM
 */
public interface ListArticulosService {

    @GET("Articulos")
    Call<ArticuloListado[]> getListArticulos(@Query("buscar") String textoBuscado);

}
