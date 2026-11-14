package edu.unicartagena.despacho.modelo;

import edu.unicartagena.despacho.comun.Validador;
import edu.unicartagena.despacho.comun.ValidacionException;

public final class Producto {

    private static final String PATRON_CODIGO = "[A-Z]{3}-[0-9]{3}";
    private static final String FORMATO_CODIGO = "tres letras, guion y tres dígitos (por ejemplo ALM-101)";

    private final String codigo;
    private final String nombre;
    private final String categoria;
    private final double pesoUnitarioKg;

    public Producto(String codigo, String nombre, String categoria, double pesoUnitarioKg) throws ValidacionException {
        this.codigo = Validador.codigo(codigo, "código del producto", PATRON_CODIGO, FORMATO_CODIGO);
        this.nombre = Validador.textoObligatorio(nombre, "nombre del producto");
        this.categoria = Validador.nombre(categoria, "categoría del producto");
        this.pesoUnitarioKg = Validador.decimalEnRango(pesoUnitarioKg, 0.01, 1000.0, "peso unitario");
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getPesoUnitarioKg() {
        return pesoUnitarioKg;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Producto otro)) {
            return false;
        }
        return codigo.equals(otro.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + categoria + ")";
    }
}
