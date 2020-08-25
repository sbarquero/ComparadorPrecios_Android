package com.example.comparadorprecios;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Clase controladora del activity de configuracion
 *
 * Santiago Barquero López  -2º DAM
 */
public class ConfiguracionActivity extends AppCompatActivity {

    // UI
    EditText edUrlBase;
    ImageButton btnGuardar;

    // URL base de la Web API
    private String baseUrl;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        // Enlazo UI
        edUrlBase = (EditText) findViewById(R.id.edUrlBase);
        btnGuardar = (ImageButton) findViewById(R.id.btnGrabarConfiguracion);

        // Recupero URL base de la web API de las SharedPreferences
        prefs = getApplication().getSharedPreferences("Configuracion", MODE_PRIVATE);
        baseUrl = prefs.getString("url_base", "http://192.168.1.183:50145/api/");

        edUrlBase.setText(baseUrl);
    }

    public void onClickGuardar(View v) {
        SharedPreferences.Editor editor = prefs.edit();

        String url = edUrlBase.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, R.string.url_vacia, Toast.LENGTH_SHORT).show();
        }
        editor.putString("url_base", url);
        editor.commit();

        finish();
    }
}
