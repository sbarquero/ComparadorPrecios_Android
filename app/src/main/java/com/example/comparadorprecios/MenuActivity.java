package com.example.comparadorprecios;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Clase controladora del activity del Menú
 *
 * Santiago Barquero López  -2º DAM
 */
public class MenuActivity extends AppCompatActivity {

    // Controles UI
    private Button btnArticulos;
    private Button btnTiendas;
    private Button btnConfiguracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Asocio UI
        btnArticulos = (Button)findViewById(R.id.btnArticulos);
        btnTiendas = (Button)findViewById(R.id.btnTiendas);
        btnConfiguracion = (Button)findViewById(R.id.btnConfiguracion);
    }

    public void onClickArticulos(View view) {
        // Creo el intent para la nueva activity que quiero lanzar
        Intent intent = new Intent(this, ListArticulosActivity.class);
        startActivity(intent);
    }

    public void onClickTiendas(View view) {
        // Creo el intent para la nueva activity que quiero lanzar
        Intent intent = new Intent(this, ListTiendasActivity.class);
        startActivity(intent);
    }

    public void onClickConfiguracion(View view) {
        // Creo el intent para la nueva activity que quiero lanzar
        Intent intent = new Intent(this, ConfiguracionActivity.class);
        startActivity(intent);
    }

}
