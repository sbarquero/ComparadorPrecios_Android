package com.example.comparadorprecios.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Clase ArticuloListado. Se utiliza para pedir un listado de artículos a la web API que solo contiene el ID,
 * la descripción y el código EAN del artículo y se muestra en el listview.
 *
 * Utilizado http://www.jsonschema2pojo.org/ para generar la clase POJO
 *
 * @author Santiago Barquero <sbarquero AT gmail.com>
 */
public class ArticuloListado {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("descripcion")
    @Expose
    private String descripcion;
    @SerializedName("ean")
    @Expose
    private String ean;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

}
