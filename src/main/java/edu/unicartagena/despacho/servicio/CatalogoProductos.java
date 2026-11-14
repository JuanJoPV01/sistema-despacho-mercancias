package edu.unicartagena.despacho.servicio;

import edu.unicartagena.despacho.comun.ValidacionException;
import edu.unicartagena.despacho.modelo.Producto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class CatalogoProductos {

    private final Map<String, Producto> productosPorCodigo = new HashMap<>();

    public Producto registrar(String codigo, String nombre, String categoria, double pesoUnitarioKg)
            throws ValidacionException {
        Producto producto = new Producto(codigo, nombre, categoria, pesoUnitarioKg);
        if (productosPorCodigo.containsKey(producto.getCodigo())) {
            throw new ValidacionException("Ya existe un producto con el código " + producto.getCodigo() + ".");
        }
        productosPorCodigo.put(producto.getCodigo(), producto);
        return producto;
    }

    public Optional<Producto> buscar(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(productosPorCodigo.get(codigo.trim().toUpperCase(Locale.ROOT)));
    }

    public boolean existe(String codigo) {
        return buscar(codigo).isPresent();
    }

    public List<Producto> listar() {
        List<Producto> productos = new ArrayList<>(productosPorCodigo.values());
        productos.sort(Comparator.comparing(Producto::getCodigo));
        return List.copyOf(productos);
    }

    public int cantidad() {
        return productosPorCodigo.size();
    }

    public void limpiar() {
        productosPorCodigo.clear();
    }
}
