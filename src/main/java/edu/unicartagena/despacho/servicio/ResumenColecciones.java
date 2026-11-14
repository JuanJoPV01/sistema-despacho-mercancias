package edu.unicartagena.despacho.servicio;

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
        if (pendientes + despachadas + canceladas != solicitudes) {
            throw new IllegalArgumentException("Los estados no coinciden con el total de solicitudes.");
        }
    }
}
