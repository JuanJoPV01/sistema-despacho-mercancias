package edu.unicartagena.despacho.modelo;

import java.util.Optional;

public enum EstadoSolicitud {

    PENDIENTE("Pendiente"),
    DESPACHADA("Despachada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoSolicitud(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Optional<EstadoSolicitud> desdeOpcion(int opcion) {
        return switch (opcion) {
            case 1 -> Optional.of(PENDIENTE);
            case 2 -> Optional.of(DESPACHADA);
            case 3 -> Optional.of(CANCELADA);
            default -> Optional.empty();
        };
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
