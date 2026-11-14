package edu.unicartagena.despacho.comun;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Validador {

    private Validador() {
    }

    public static String textoObligatorio(String valor, String campo) throws ValidacionException {
        if (valor == null) {
            throw new ValidacionException("El campo " + campo + " es obligatorio.");
        }
        String limpio = valor.trim();
        if (limpio.isEmpty()) {
            throw new ValidacionException("El campo " + campo + " es obligatorio.");
        }
        if (limpio.length() > 80) {
            throw new ValidacionException("El campo " + campo + " no puede superar 80 caracteres.");
        }
        return limpio;
    }

    public static String nombre(String valor, String campo) throws ValidacionException {
        String limpio = textoObligatorio(valor, campo);
        if (!limpio.matches("[\\p{L} .'()-]+")) {
            throw new ValidacionException("El campo " + campo + " solo admite letras, espacios y los signos . ' ( ) -.");
        }
        return limpio;
    }

    public static String codigo(String valor, String campo, String patron, String formato) throws ValidacionException {
        String limpio = textoObligatorio(valor, campo).toUpperCase(Locale.ROOT);
        if (!Pattern.matches(patron, limpio)) {
            throw new ValidacionException("El campo " + campo + " debe tener el formato " + formato + ".");
        }
        return limpio;
    }

    public static int enteroEnRango(int valor, int minimo, int maximo, String campo) throws ValidacionException {
        if (valor < minimo || valor > maximo) {
            throw new ValidacionException("El campo " + campo + " debe estar entre " + minimo + " y " + maximo + ".");
        }
        return valor;
    }

    public static double decimalEnRango(double valor, double minimo, double maximo, String campo)
            throws ValidacionException {
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor < minimo || valor > maximo) {
            throw new ValidacionException("El campo " + campo + " debe estar entre " + minimo + " y " + maximo + ".");
        }
        return valor;
    }

    public static <T> T requerido(T valor, String campo) throws ValidacionException {
        if (valor == null) {
            throw new ValidacionException("El campo " + campo + " es obligatorio.");
        }
        return valor;
    }
}
