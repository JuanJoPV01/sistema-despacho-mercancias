package edu.unicartagena.despacho.servicio;

import edu.unicartagena.despacho.comun.ValidacionException;
import edu.unicartagena.despacho.modelo.EstadoSolicitud;
import edu.unicartagena.despacho.modelo.Prioridad;

public final class DatosDemostracion {

    private DatosDemostracion() {
    }

    public static void cargar(SistemaDespacho sistema) throws ValidacionException {
        sistema.destinos().registrar("Bogotá D.C.", "Cundinamarca");
        sistema.destinos().registrar("Medellín", "Antioquia");
        sistema.destinos().registrar("Cali", "Valle del Cauca");
        sistema.destinos().registrar("Barranquilla", "Atlántico");
        sistema.destinos().registrar("Cartagena", "Bolívar");
        sistema.destinos().registrar("Bucaramanga", "Santander");
        sistema.destinos().registrar("Pereira", "Risaralda");
        sistema.destinos().registrar("Santa Marta", "Magdalena");

        sistema.catalogo().registrar("ALM-101", "Harina de trigo 50 kg", "Alimentos", 50.0);
        sistema.catalogo().registrar("ALM-102", "Arroz blanco 25 kg", "Alimentos", 25.0);
        sistema.catalogo().registrar("ALM-103", "Aceite vegetal 20 L", "Alimentos", 18.5);
        sistema.catalogo().registrar("BEB-201", "Agua mineral 12 L", "Bebidas", 12.0);
        sistema.catalogo().registrar("ELE-301", "Televisor 55 pulgadas", "Electrónica", 14.2);
        sistema.catalogo().registrar("ELE-302", "Computador portátil", "Electrónica", 2.4);
        sistema.catalogo().registrar("ASE-401", "Jabón industrial 10 kg", "Aseo", 10.0);
        sistema.catalogo().registrar("EMP-501", "Caja de cartón reforzada", "Empaques", 2.0);

        registrar(sistema, "DSP-001", "Bogotá D.C.", "ALM-101", 120, Prioridad.ALTA);
        registrar(sistema, "DSP-002", "Medellín", "ELE-301", 15, Prioridad.CRITICA);
        registrar(sistema, "DSP-003", "Cali", "BEB-201", 80, Prioridad.MEDIA);
        registrar(sistema, "DSP-004", "Barranquilla", "ASE-401", 200, Prioridad.BAJA);
        registrar(sistema, "DSP-005", "Cartagena", "EMP-501", 500, Prioridad.MEDIA);
        registrar(sistema, "DSP-006", "Bogotá D.C.", "ELE-302", 30, Prioridad.ALTA);
        registrar(sistema, "DSP-007", "Medellín", "ALM-103", 90, Prioridad.CRITICA);
        registrar(sistema, "DSP-008", "Cali", "ALM-101", 60, Prioridad.BAJA);
        registrar(sistema, "DSP-009", "Bucaramanga", "ELE-301", 8, Prioridad.ALTA);
        registrar(sistema, "DSP-010", "Pereira", "ASE-401", 150, Prioridad.MEDIA);
        registrar(sistema, "DSP-011", "Santa Marta", "ALM-102", 100, Prioridad.CRITICA);
        registrar(sistema, "DSP-012", "Cartagena", "ELE-302", 12, Prioridad.BAJA);
        registrar(sistema, "DSP-013", "Bogotá D.C.", "ASE-401", 75, Prioridad.ALTA);
        registrar(sistema, "DSP-014", "Medellín", "EMP-501", 300, Prioridad.MEDIA);
        registrar(sistema, "DSP-015", "Cali", "ELE-301", 6, Prioridad.BAJA);
        registrar(sistema, "DSP-016", "Barranquilla", "ALM-103", 110, Prioridad.CRITICA);
        registrar(sistema, "DSP-017", "Bucaramanga", "ALM-101", 45, Prioridad.MEDIA);
        registrar(sistema, "DSP-018", "Pereira", "ELE-302", 20, Prioridad.ALTA);

        sistema.despachos().modificarEstado("DSP-005", EstadoSolicitud.DESPACHADA);
        sistema.despachos().modificarEstado("DSP-013", EstadoSolicitud.DESPACHADA);
        sistema.despachos().modificarEstado("DSP-008", EstadoSolicitud.CANCELADA);
        sistema.despachos().modificarEstado("DSP-017", EstadoSolicitud.CANCELADA);
    }

    private static void registrar(SistemaDespacho sistema, String codigo, String destino, String producto,
            int cantidad, Prioridad prioridad) throws ValidacionException {
        sistema.despachos().registrar(codigo, destino, producto, cantidad, prioridad);
        sistema.despachos().encolar(codigo);
    }
}
