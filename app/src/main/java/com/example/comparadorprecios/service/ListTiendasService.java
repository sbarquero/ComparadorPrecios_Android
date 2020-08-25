package com.example.comparadorprecios.service;

import com.example.comparadorprecios.data.model.TiendaListado;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface de servicio de ListTiendas
 *
 * Contiene los endpoints de la web API para el listado de tiendas
 *
 * Santiago Barquero López - 2º DAM
 */
public interface ListTiendasService {
    @GET("Tiendas")
    Call<TiendaListado[]> getListTiendas(@Query("buscar") String textoBuscado);
}
