package edu.unicartagena.despacho.servicio;

import edu.unicartagena.despacho.modelo.SolicitudDespacho;
import java.util.Comparator;

public final class Comparadores {

    public static final Comparator<SolicitudDespacho> PRIORIDAD_DESPACHO =
            Comparator.comparingInt((SolicitudDespacho solicitud) -> solicitud.getPrioridad().getPeso())
                    .reversed()
                    .thenComparingLong(SolicitudDespacho::getSecuencia);

    private Comparadores() {
    }
}
