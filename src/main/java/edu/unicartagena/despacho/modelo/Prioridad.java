package edu.unicartagena.despacho.modelo;

import java.util.Optional;

public enum Prioridad {

    CRITICA(4, "Crítica"),
    ALTA(3, "Alta"),
    MEDIA(2, "Media"),
    BAJA(1, "Baja");

    private final int peso;
    private final String etiqueta;

    Prioridad(int peso, String etiqueta) {
        this.peso = peso;
        this.etiqueta = etiqueta;
    }

    public int getPeso() {
        return peso;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Optional<Prioridad> desdeOpcion(int opcion) {
        return switch (opcion) {
            case 1 -> Optional.of(CRITICA);
            case 2 -> Optional.of(ALTA);
            case 3 -> Optional.of(MEDIA);
            case 4 -> Optional.of(BAJA);
            default -> Optional.empty();
        };
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
