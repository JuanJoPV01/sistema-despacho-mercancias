package edu.unicartagena.despacho.servicio;

import edu.unicartagena.despacho.comun.ValidacionException;
import edu.unicartagena.despacho.modelo.Destino;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class RegistroDestinos {

    private final Set<Destino> destinos = new HashSet<>();

    public Destino registrar(String nombre, String departamento) throws ValidacionException {
        Destino destino = new Destino(nombre, departamento);
        if (!destinos.add(destino)) {
            throw new ValidacionException("El destino " + destino.getNombre() + " ya está registrado.");
        }
        return destino;
    }

    public Optional<Destino> buscar(String nombre) {
        if (nombre == null) {
            return Optional.empty();
        }
        String buscado = nombre.trim();
        for (Destino destino : destinos) {
            if (destino.getNombre().equalsIgnoreCase(buscado)) {
                return Optional.of(destino);
            }
        }
        return Optional.empty();
    }

    public boolean existe(String nombre) {
        return buscar(nombre).isPresent();
    }

    public List<Destino> listar() {
        return List.copyOf(destinos);
    }

    public int cantidad() {
        return destinos.size();
    }

    public void limpiar() {
        destinos.clear();
    }
}
