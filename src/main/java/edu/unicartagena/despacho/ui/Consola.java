package edu.unicartagena.despacho.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public final class Consola {

    private final Scanner scanner;
    private final PrintStream salida;
    private boolean entradaFinalizada;

    public Consola(InputStream entrada) {
        this(entrada, System.out);
    }

    public Consola(InputStream entrada, PrintStream salida) {
        this.scanner = new Scanner(entrada);
        this.salida = salida;
    }

    public String leerTexto(String mensaje) {
        salida.print(mensaje);
        salida.flush();
        if (entradaFinalizada || !scanner.hasNextLine()) {
            entradaFinalizada = true;
            salida.println();
            return null;
        }
        return scanner.nextLine().trim();
    }

    public void imprimir(String mensaje) {
        salida.println(mensaje);
    }

    public void formato(String formato, Object... argumentos) {
        salida.printf(formato, argumentos);
    }

    public void titulo(String texto) {
        salida.println();
        salida.println("=== " + texto + " ===");
    }

    public void info(String mensaje) {
        salida.println("[INFO] " + mensaje);
    }

    public void error(String mensaje) {
        salida.println("[ERROR] " + mensaje);
    }
}
