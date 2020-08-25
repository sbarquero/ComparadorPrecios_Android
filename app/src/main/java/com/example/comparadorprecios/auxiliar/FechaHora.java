package com.example.comparadorprecios.auxiliar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Clase con métodos estáticos para la conversión de formatos de fecha entre el formato de base de datos y el
 * formato de visualización
 *
 * https://stackoverflow.com/questions/5369682/how-to-get-current-time-and-date-in-android
 * https://stackoverflow.com/questions/9277747/android-simpledateformat-how-to-use-it
 *
 * @author Santiago Barquero <sbarquero AT gmail.com>
 */
public class FechaHora {

    /**
     * Método que recibe la fecha y hora de la base de datos y devuelve la fecha y hora legible para la vista.
     *
     * @param fechaBD Fecha y hora en formato base de datos.
     * @return Fecha y hora en formato legible para la vista.
     */
    public static String decodifica(String fechaBD) {
        SimpleDateFormat formatoBD = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date fecha;
        try {
            fecha = formatoBD.parse(fechaBD);
        } catch (ParseException e) {
            return ("Error fecha");
        }
        SimpleDateFormat formatoVista = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatoVista.format(fecha);
    }

    /**
     * Método que devuelve una cadena con la fecha y hora actual en formato para la BD.
     * @return Cadena con la fecha y hora actual en formato para la BD.
     */
    public static String actualBD() {
        SimpleDateFormat formatoVista = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date fecha = Calendar.getInstance().getTime();
        return formatoVista.format(fecha);
    }

    /**
     * Método que devuelve una cadena con la fecha y hora actual en formato legible para la vista.
     * @return Cadena con la fecha y hora actual en formato legible para la vista.
     */
    public static String actual() {
        SimpleDateFormat formatoVista = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date fecha = Calendar.getInstance().getTime();
        return formatoVista.format(fecha);
    }

}
