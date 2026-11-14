package edu.unicartagena.despacho.modelo;

import edu.unicartagena.despacho.comun.Validador;
import edu.unicartagena.despacho.comun.ValidacionException;
import java.util.Locale;

public final class Destino implements Comparable<Destino> {

    private final String nombre;
    private final String departamento;
    private final String clave;

    public Destino(String nombre, String departamento) throws ValidacionException {
        this.nombre = Validador.nombre(nombre, "nombre del destino");
        this.departamento = Validador.nombre(departamento, "departamento del destino");
        this.clave = this.nombre.toLowerCase(Locale.ROOT);
    }

    public String getNombre() {
        return nombre;
    }

    public String getDepartamento() {
        return departamento;
    }

    @Override
    public int compareTo(Destino otro) {
        return String.CASE_INSENSITIVE_ORDER.compare(nombre, otro.nombre);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Destino otro)) {
            return false;
        }
        return clave.equals(otro.clave);
    }

    @Override
    public int hashCode() {
        return clave.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " (" + departamento + ")";
    }
}
