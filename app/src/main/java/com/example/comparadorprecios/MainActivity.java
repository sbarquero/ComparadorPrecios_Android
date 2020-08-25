package com.example.comparadorprecios;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Clase controladora del activity Main que es el punto de entrada de la aplicación.
 * Pide usuario y contraseña y permite el acceso a la aplicación.
 *
 * Santiago Barquero López  -2º DAM
 */
public class MainActivity extends AppCompatActivity {

    // Controles UI
    private EditText editUsuario;
    private EditText editPassword;
    private CheckBox chkRecordarPw;
    private Button btnAcceder;
    private ImageButton imgBtnCreditos;

    // SharedPreferences
    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enlazarUI();
        cargarSharedPreferences();
    }

    private void enlazarUI() {
        // Enlazo UI
        editUsuario = (EditText)findViewById(R.id.editUsuario);
        editPassword = (EditText)findViewById(R.id.editPassword);
        chkRecordarPw = (CheckBox)findViewById(R.id.chkRecordarPw);
        btnAcceder = (Button)findViewById(R.id.btnAcceder);
        imgBtnCreditos = (ImageButton)findViewById(R.id.imgBtnCreditos);
    }

    private void cargarSharedPreferences() {
        String pw = "";
        // Cargo configuración de las SharedPreferences
        prefs = getApplication().getSharedPreferences("Configuracion", MODE_PRIVATE);
        editUsuario.setText(prefs.getString("Usuario", ""));
        String pwShared = prefs.getString("Password", "");
        if (!pwShared.equals("")) {
            pw = new String(Base64.decode(pwShared, Base64.NO_WRAP));
        }
        editPassword.setText(pw);
        chkRecordarPw.setChecked(prefs.getBoolean("Recordar", false));
    }

    public void onClickBtnAcceder(View view) {
        SharedPreferences.Editor editor = prefs.edit();
        if (chkRecordarPw.isChecked()) {
            // Recordar datos de acceso
            editor.putString("Usuario", editUsuario.getText().toString());
            String pw = editPassword.getText().toString();
            editor.putString("Password", Base64.encodeToString(pw.getBytes(), Base64.NO_WRAP));
            editor.putBoolean("Recordar", true);
        } else {
            // No recordar datos acceso
            editor.putString("Usuario", "");
            editor.putString("Password", "");
            editor.putBoolean("Recordar", false);
        }
        editor.commit();
        // Creo el intent para la nueva activity que quiero lanzar
        Intent intent = new Intent(this, MenuActivity.class);

        startActivity(intent);
    }

    public void onClickImgBtnCreditos(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.mensaje_creditos)
                .setCancelable(false)
                .setNeutralButton(R.string.cerrar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle(R.string.creditos);
        alert.show();

    }


}
