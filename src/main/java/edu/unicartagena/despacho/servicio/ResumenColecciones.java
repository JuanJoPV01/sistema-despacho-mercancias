package edu.unicartagena.despacho.servicio;

import edu.unicartagena.despacho.modelo.EstadoSolicitud;

public record ResumenColecciones(
        int solicitudes,
        int indiceCodigos,
        int destinos,
        int productos,
        int destinosConDespachos,
        int enCola,
        int pendientes,
        int despachadas,
        int canceladas) {

    public ResumenColecciones {
        if (solicitudes < 0 || indiceCodigos < 0 || destinos < 0 || productos < 0
                || destinosConDespachos < 0 || enCola < 0 || pendientes < 0
                || despachadas < 0 || canceladas < 0) {
            throw new IllegalArgumentException("Los contadores no pueden ser negativos.");
        }
    }

    public int totalPorEstado(EstadoSolicitud estado) {
        return switch (estado) {
            case PENDIENTE -> pendientes;
            case DESPACHADA -> despachadas;
            case CANCELADA -> canceladas;
        };
    }
}
