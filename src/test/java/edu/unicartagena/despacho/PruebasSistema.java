package edu.unicartagena.despacho;

import edu.unicartagena.despacho.comun.ValidacionException;
import edu.unicartagena.despacho.modelo.Destino;
import edu.unicartagena.despacho.modelo.EstadoSolicitud;
import edu.unicartagena.despacho.modelo.Prioridad;
import edu.unicartagena.despacho.modelo.Producto;
import edu.unicartagena.despacho.modelo.SolicitudDespacho;
import edu.unicartagena.despacho.servicio.ResumenColecciones;
import edu.unicartagena.despacho.servicio.SistemaDespacho;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class PruebasSistema {

    private static int verificaciones;

    private PruebasSistema() {
    }

    public static void main(String[] args) {
        try {
            ejecutarPruebas();
            System.out.println("OK - " + verificaciones + " verificaciones completadas.");
        } catch (Throwable error) {
            System.err.println("FALLO - " + error.getMessage());
            error.printStackTrace();
            System.exit(1);
        }
    }

    private static void ejecutarPruebas() throws Exception {
        probarCargaYConsistencia();
        probarBusquedaPorClave();
        probarControlDeDuplicados();
        probarValidaciones();
        probarOrdenPorDestino();
        probarPrioridadYEmpates();
        probarColaDeDespacho();
        probarCambioDePrioridad();
        probarCambioDeEstado();
        probarCambioDeCodigoYDestino();
        probarEliminacion();
        probarIgualdadYHash();
        probarRecargaDeDatos();
    }

    private static void probarCargaYConsistencia() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();
        ResumenColecciones resumen = sistema.resumen();

        verificarIgual("solicitudes registradas", 18, resumen.solicitudes());
        verificarIgual("claves del indice", 18, resumen.indiceCodigos());
        verificarIgual("destinos unicos", 8, resumen.destinos());
        verificarIgual("productos registrados", 8, resumen.productos());
        verificarIgual("destinos con despachos", 8, resumen.destinosConDespachos());
        verificarIgual("solicitudes en cola", 14, resumen.enCola());
        verificarIgual("solicitudes pendientes", 14, resumen.pendientes());
        verificarIgual("solicitudes despachadas", 2, resumen.despachadas());
        verificarIgual("solicitudes canceladas", 2, resumen.canceladas());
        verificarIgual("total por estado", 18,
                resumen.pendientes() + resumen.despachadas() + resumen.canceladas());
        verificarIgual("listado de pendientes", resumen.pendientes(),
                sistema.despachos().listarPorEstado(EstadoSolicitud.PENDIENTE).size());
        verificarIgual("listado de despachadas", resumen.despachadas(),
                sistema.despachos().listarPorEstado(EstadoSolicitud.DESPACHADA).size());
        verificarIgual("listado de canceladas", resumen.canceladas(),
                sistema.despachos().listarPorEstado(EstadoSolicitud.CANCELADA).size());
        verificarIgual("tamano de la lista", resumen.solicitudes(), sistema.despachos().cantidad());
        verificarIgual("tamano del indice", resumen.indiceCodigos(),
                sistema.despachos().cantidadIndiceCodigos());
        verificarIgual("cola igual a pendientes", resumen.pendientes(),
                sistema.despachos().colaEnOrdenDeAtencion().size());

        int totalArbol = 0;
        for (List<SolicitudDespacho> grupo : sistema.despachos().despachosPorDestino().values()) {
            totalArbol += grupo.size();
            verificar("grupo de destino no vacio", !grupo.isEmpty());
        }
        verificarIgual("el arbol contiene todas las solicitudes", 18, totalArbol);
        verificarIgual("primera clave ordenada", "Barranquilla",
                sistema.despachos().despachosPorDestino().firstKey().getNombre());
        verificarIgual("ultima clave ordenada", "Santa Marta",
                sistema.despachos().despachosPorDestino().lastKey().getNombre());
    }

    private static void probarBusquedaPorClave() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();

        verificar("busqueda existente", sistema.despachos().buscar("DSP-002").isPresent());
        verificarIgual("busqueda sin distinguir mayusculas", "DSP-002",
                sistema.despachos().buscar("dsp-002").orElseThrow().getCodigo());
        verificarIgual("prioridad recuperada", Prioridad.CRITICA,
                sistema.despachos().buscar("DSP-002").orElseThrow().getPrioridad());
        verificar("busqueda inexistente", sistema.despachos().buscar("DSP-999").isEmpty());
        verificar("busqueda nula", sistema.despachos().buscar(null).isEmpty());
        verificar("producto por codigo", sistema.catalogo().buscar("alm-101").isPresent());
        verificar("producto inexistente", sistema.catalogo().buscar("ZZZ-999").isEmpty());
        verificar("destino por nombre", sistema.destinos().buscar("medellín").isPresent());
        verificar("destino inexistente", sistema.destinos().buscar("Tunja").isEmpty());
    }

    private static void probarControlDeDuplicados() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();

        verificarError("destino duplicado", "ya está registrado",
                () -> sistema.destinos().registrar("medellín", "Antioquia"));
        verificarError("destino duplicado con otro departamento", "ya está registrado",
                () -> sistema.destinos().registrar("Medellín", "Caldas"));
        verificarIgual("destinos sin cambios", 8, sistema.destinos().cantidad());

        verificarError("producto duplicado", "ya existe",
                () -> sistema.catalogo().registrar("alm-101", "Otro producto", "Alimentos", 1.0));
        verificarIgual("productos sin cambios", 8, sistema.catalogo().cantidad());

        verificarError("solicitud duplicada", "ya existe",
                () -> sistema.despachos().registrar("dsp-001", "Cali", "ALM-101", 10, Prioridad.BAJA));
        verificarIgual("solicitudes sin cambios", 18, sistema.despachos().cantidad());
        verificarIgual("indice sin cambios", 18, sistema.resumen().indiceCodigos());
        verificarIgual("cola sin cambios", 14, sistema.despachos().cantidadEnCola());
    }

    private static void probarValidaciones() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();

        verificarError("codigo vacio", "obligatorio",
                () -> sistema.despachos().registrar("   ", "Cali", "ALM-101", 10, Prioridad.ALTA));
        verificarError("codigo con formato invalido", "formato",
                () -> sistema.despachos().registrar("123", "Cali", "ALM-101", 10, Prioridad.ALTA));
        verificarError("destino inexistente en solicitud", "no existe",
                () -> sistema.despachos().registrar("DSP-900", "Tunja", "ALM-101", 10, Prioridad.ALTA));
        verificarError("producto inexistente en solicitud", "no existe",
                () -> sistema.despachos().registrar("DSP-901", "Cali", "ZZZ-999", 10, Prioridad.ALTA));
        verificarError("cantidad en cero", "entre 1 y 50000",
                () -> sistema.despachos().registrar("DSP-902", "Cali", "ALM-101", 0, Prioridad.ALTA));
        verificarError("cantidad fuera de rango", "entre 1 y 50000",
                () -> sistema.despachos().registrar("DSP-903", "Cali", "ALM-101", 50001, Prioridad.ALTA));
        verificarError("prioridad nula", "prioridad",
                () -> sistema.despachos().registrar("DSP-904", "Cali", "ALM-101", 10, null));
        verificarError("destino vacio", "obligatorio",
                () -> sistema.destinos().registrar(" ", "Antioquia"));
        verificarError("peso invalido", "entre",
                () -> sistema.catalogo().registrar("PRU-001", "Producto de prueba", "Pruebas", 0.0));
        verificarError("codigo de producto invalido", "formato",
                () -> sistema.catalogo().registrar("AB-1", "Producto de prueba", "Pruebas", 1.0));

        verificarIgual("solicitudes conservadas", 18, sistema.despachos().cantidad());
        verificarIgual("productos conservados", 8, sistema.catalogo().cantidad());
    }

    private static void probarOrdenPorDestino() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();
        List<String> nombres = new ArrayList<>();
        for (Destino destino : sistema.despachos().despachosPorDestino().keySet()) {
            nombres.add(destino.getNombre());
        }
        verificarIgual("orden alfabetico de destinos",
                List.of("Barranquilla", "Bogotá D.C.", "Bucaramanga", "Cali",
                        "Cartagena", "Medellín", "Pereira", "Santa Marta"),
                List.copyOf(nombres));
        List<SolicitudDespacho> cali = sistema.despachos().despachosPorDestino()
                .get(new Destino("cali", "Valle del Cauca"));
        verificar("consulta de grupo por clave", cali != null);
        verificarIgual("solicitudes de Cali", 3, cali.size());
        verificarIgual("Cali conserva orden de registro", List.of("DSP-003", "DSP-008", "DSP-015"),
                codigos(cali));
    }

    private static void probarPrioridadYEmpates() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();
        List<String> esperado = List.of(
                "DSP-002", "DSP-007", "DSP-011", "DSP-016",
                "DSP-001", "DSP-006", "DSP-009", "DSP-018",
                "DSP-003", "DSP-010", "DSP-014",
                "DSP-004", "DSP-012", "DSP-015");
        verificarIgual("orden completo de la cola", esperado,
                codigos(sistema.despachos().colaEnOrdenDeAtencion()));
        verificarIgual("proximo despacho", "DSP-002",
                sistema.despachos().proximoDespacho().orElseThrow().getCodigo());
        verificarIgual("consultar no retira", 14, sistema.despachos().cantidadEnCola());

        SolicitudDespacho procesada = sistema.despachos().procesarSiguiente().orElseThrow();
        verificarIgual("procesa la critica mas antigua", "DSP-002", procesada.getCodigo());
        verificarIgual("estado actualizado", EstadoSolicitud.DESPACHADA, procesada.getEstado());
        verificarIgual("cola reduce", 13, sistema.despachos().cantidadEnCola());
        verificar("solicitud permanece en el registro", sistema.despachos().buscar("DSP-002").isPresent());
        verificarIgual("siguiente despacho", "DSP-007",
                sistema.despachos().proximoDespacho().orElseThrow().getCodigo());
        verificarIgual("cola vacia", Optional.empty(), new SistemaDespacho().despachos().procesarSiguiente());
    }

    private static void probarColaDeDespacho() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();

        verificarError("solicitud ya encolada", "ya está en la cola",
                () -> sistema.despachos().encolar("DSP-001"));
        verificarError("solicitud despachada", "no está pendiente",
                () -> sistema.despachos().encolar("DSP-005"));
        verificarError("solicitud inexistente", "no existe",
                () -> sistema.despachos().encolar("DSP-999"));

        sistema.despachos().registrar("DSP-019", "Cali", "ALM-101", 10, Prioridad.BAJA);
        verificarIgual("registrar no encola", 14, sistema.despachos().cantidadEnCola());
        verificarIgual("nueva pendiente", 15, sistema.resumen().pendientes());
        verificar("nueva solicitud fuera de la cola", !sistema.despachos().estaEnCola("DSP-019"));

        sistema.despachos().encolar("DSP-019");
        verificar("nueva solicitud encolada", sistema.despachos().estaEnCola("DSP-019"));
        verificarIgual("cola aumenta", 15, sistema.despachos().cantidadEnCola());
        verificarIgual("ultima posicion por prioridad y secuencia", "DSP-019",
                sistema.despachos().colaEnOrdenDeAtencion().get(14).getCodigo());
        verificarError("no se encola dos veces", "ya está en la cola",
                () -> sistema.despachos().encolar("DSP-019"));
    }

    private static void probarCambioDePrioridad() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();

        sistema.despachos().modificarPrioridad("DSP-004", Prioridad.CRITICA);
        verificarIgual("prioridad modificada", Prioridad.CRITICA,
                sistema.despachos().buscar("DSP-004").orElseThrow().getPrioridad());
        List<String> orden = codigos(sistema.despachos().colaEnOrdenDeAtencion());
        verificarIgual("se reubica primero la mas antigua", "DSP-002", orden.get(0));
        verificarIgual("se reubica por prioridad", "DSP-004", orden.get(1));
        sistema.despachos().procesarSiguiente();
        verificarIgual("nuevo siguiente", "DSP-004",
                sistema.despachos().proximoDespacho().orElseThrow().getCodigo());

        verificarError("prioridad nula al modificar", "prioridad",
                () -> sistema.despachos().modificarPrioridad("DSP-004", null));
        verificarError("solicitud inexistente al modificar", "no existe",
                () -> sistema.despachos().modificarPrioridad("DSP-999", Prioridad.ALTA));
    }

    private static void probarCambioDeEstado() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();

        sistema.despachos().modificarEstado("DSP-001", EstadoSolicitud.PENDIENTE);
        verificarIgual("confirmar pendiente conserva la cola", 14, sistema.despachos().cantidadEnCola());
        verificar("confirmar pendiente no retira de la cola", sistema.despachos().estaEnCola("DSP-001"));

        sistema.despachos().modificarEstado("DSP-004", EstadoSolicitud.CANCELADA);
        verificarIgual("cancelada sale de la cola", 13, sistema.despachos().cantidadEnCola());
        verificar("cancelada no esta en cola", !sistema.despachos().estaEnCola("DSP-004"));
        verificar("cancelada permanece en el registro", sistema.despachos().buscar("DSP-004").isPresent());
        verificarIgual("estado cancelada", EstadoSolicitud.CANCELADA,
                sistema.despachos().buscar("DSP-004").orElseThrow().getEstado());

        sistema.despachos().modificarEstado("DSP-004", EstadoSolicitud.PENDIENTE);
        verificarIgual("pendiente no se encola automaticamente", 13, sistema.despachos().cantidadEnCola());
        sistema.despachos().encolar("DSP-004");
        verificarIgual("pendiente vuelve a la cola", 14, sistema.despachos().cantidadEnCola());
        verificarIgual("posicion dentro de su prioridad", "DSP-004",
                sistema.despachos().colaEnOrdenDeAtencion().get(11).getCodigo());

        sistema.despachos().modificarEstado("DSP-004", EstadoSolicitud.DESPACHADA);
        verificarIgual("despachada sale de la cola", 13, sistema.despachos().cantidadEnCola());
        verificarIgual("total despachadas", 3, sistema.resumen().despachadas());
    }

    private static void probarCambioDeCodigoYDestino() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();

        sistema.despachos().modificarCodigo("DSP-011", "DSP-110");
        verificar("codigo anterior eliminado", sistema.despachos().buscar("DSP-011").isEmpty());
        verificar("codigo nuevo disponible", sistema.despachos().buscar("dsp-110").isPresent());
        verificarIgual("indice conserva tamano", 18, sistema.resumen().indiceCodigos());
        verificarIgual("lista conserva tamano", 18, sistema.despachos().cantidad());
        verificar("cola actualiza el objeto", sistema.despachos().estaEnCola("DSP-110"));
        List<SolicitudDespacho> santaMarta = sistema.despachos().despachosPorDestino()
                .get(new Destino("Santa Marta", "Magdalena"));
        verificar("arbol conserva la solicitud", santaMarta != null && santaMarta.size() == 1);
        verificarIgual("codigo actualizado en el arbol", "DSP-110", santaMarta.get(0).getCodigo());

        verificarError("codigo duplicado", "ya existe",
                () -> sistema.despachos().modificarCodigo("DSP-110", "DSP-001"));
        verificar("codigo se conserva tras el error", sistema.despachos().buscar("DSP-110").isPresent());

        sistema.despachos().modificarDestino("DSP-110", "Cali");
        verificar("destino anterior se retira del arbol",
                !sistema.despachos().despachosPorDestino().containsKey(new Destino("Santa Marta", "Magdalena")));
        List<SolicitudDespacho> cali = sistema.despachos().despachosPorDestino()
                .get(new Destino("Cali", "Valle del Cauca"));
        verificarIgual("nuevo destino agrupa la solicitud", 4, cali.size());
        verificar("la solicitud esta en el nuevo grupo", codigos(cali).contains("DSP-110"));

        sistema.despachos().modificarDestino("DSP-110", "Santa Marta");
        verificarIgual("el arbol recupera el destino", 8,
                sistema.despachos().despachosPorDestino().size());
        verificarError("destino inexistente al modificar", "no existe",
                () -> sistema.despachos().modificarDestino("DSP-110", "Tunja"));

        sistema.despachos().modificarProducto("DSP-110", "ELE-302");
        verificarIgual("producto modificado", "ELE-302",
                sistema.despachos().buscar("DSP-110").orElseThrow().getProducto().getCodigo());
        verificarError("producto inexistente al modificar", "no existe",
                () -> sistema.despachos().modificarProducto("DSP-110", "ZZZ-999"));

        sistema.despachos().modificarCantidad("DSP-110", 75);
        verificarIgual("cantidad modificada", 75,
                sistema.despachos().buscar("DSP-110").orElseThrow().getCantidad());
        verificarError("cantidad invalida al modificar", "entre 1 y 50000",
                () -> sistema.despachos().modificarCantidad("DSP-110", 0));
        verificarIgual("cantidad se conserva tras el error", 75,
                sistema.despachos().buscar("DSP-110").orElseThrow().getCantidad());
        verificarError("codigo inexistente al modificar", "no existe",
                () -> sistema.despachos().modificarCantidad("DSP-999", 10));
    }

    private static void probarEliminacion() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();

        SolicitudDespacho eliminada = sistema.despachos().eliminar("DSP-002");
        verificarIgual("retorna la solicitud eliminada", "DSP-002", eliminada.getCodigo());
        verificar("eliminada del indice", sistema.despachos().buscar("DSP-002").isEmpty());
        verificarIgual("lista reducida", 17, sistema.despachos().cantidad());
        verificarIgual("indice reducido", 17, sistema.resumen().indiceCodigos());
        verificarIgual("cola reducida", 13, sistema.despachos().cantidadEnCola());
        verificar("no permanece en la cola", !sistema.despachos().estaEnCola("DSP-002"));
        verificar("no permanece en la cola en orden", !codigos(sistema.despachos().colaEnOrdenDeAtencion())
                .contains("DSP-002"));

        int totalArbol = 0;
        for (List<SolicitudDespacho> grupo : sistema.despachos().despachosPorDestino().values()) {
            totalArbol += grupo.size();
        }
        verificarIgual("arbol reducido", 17, totalArbol);
        List<SolicitudDespacho> medellin = sistema.despachos().despachosPorDestino()
                .get(new Destino("Medellín", "Antioquia"));
        verificarIgual("grupo de Medellin reducido", 2, medellin.size());
        verificar("grupo de Medellin sin la eliminada", !codigos(medellin).contains("DSP-002"));

        sistema.despachos().eliminar("DSP-011");
        verificar("destino sin solicitudes se retira del arbol",
                !sistema.despachos().despachosPorDestino().containsKey(new Destino("Santa Marta", "Magdalena")));
        verificarIgual("claves del arbol reducidas", 7, sistema.despachos().despachosPorDestino().size());
        verificarIgual("destinos del registro se conservan", 8, sistema.destinos().cantidad());

        verificarError("eliminacion repetida", "no existe",
                () -> sistema.despachos().eliminar("DSP-002"));
    }

    private static void probarIgualdadYHash() throws ValidacionException {
        Destino destinoUno = new Destino("Medellín", "Antioquia");
        Destino destinoDos = new Destino(" medellín ", "Antioquia");
        verificar("destinos iguales ignorando mayusculas y espacios", destinoUno.equals(destinoDos));
        verificarIgual("hashCode de destinos iguales", destinoUno.hashCode(), destinoDos.hashCode());
        verificarIgual("compareTo de destinos iguales", 0, destinoUno.compareTo(destinoDos));

        Producto productoUno = new Producto("alm-101", "Harina", "Alimentos", 50.0);
        Producto productoDos = new Producto("ALM-101", "Otro nombre", "Otros", 10.0);
        verificar("productos iguales por codigo", productoUno.equals(productoDos));
        verificarIgual("hashCode de productos iguales", productoUno.hashCode(), productoDos.hashCode());

        SistemaDespacho sistema = sistemaConDatos();
        SolicitudDespacho primera = sistema.despachos().buscar("DSP-001").orElseThrow();
        SolicitudDespacho segunda = sistema.despachos().buscar("dsp-001").orElseThrow();
        verificar("misma solicitud recuperada", primera.equals(segunda));
        verificarIgual("hashCode de la solicitud", primera.hashCode(), segunda.hashCode());
    }

    private static void probarRecargaDeDatos() throws ValidacionException {
        SistemaDespacho sistema = sistemaConDatos();
        sistema.destinos().registrar("Tunja", "Boyacá");
        sistema.catalogo().registrar("PRU-001", "Producto de prueba", "Pruebas", 1.5);
        sistema.despachos().registrar("DSP-900", "Tunja", "PRU-001", 5, Prioridad.ALTA);

        verificarIgual("solicitudes antes de recargar", 19, sistema.despachos().cantidad());
        verificarIgual("cola antes de recargar", 14, sistema.despachos().cantidadEnCola());
        verificarIgual("pendientes antes de recargar", 15, sistema.resumen().pendientes());

        sistema.cargarDatosDemostracion();
        verificarIgual("solicitudes restauradas", 18, sistema.despachos().cantidad());
        verificarIgual("destinos restaurados", 8, sistema.destinos().cantidad());
        verificarIgual("productos restaurados", 8, sistema.catalogo().cantidad());
        verificarIgual("cola restaurada", 14, sistema.despachos().cantidadEnCola());
        verificar("destino de prueba eliminado", sistema.destinos().buscar("Tunja").isEmpty());
        verificar("producto de prueba eliminado", sistema.catalogo().buscar("PRU-001").isEmpty());
        verificar("solicitud de prueba eliminada", sistema.despachos().buscar("DSP-900").isEmpty());

        sistema.cargarDatosDemostracion();
        verificarIgual("recarga repetida estable", 18, sistema.despachos().cantidad());
        verificarIgual("cola estable", 14, sistema.despachos().cantidadEnCola());
    }

    private static SistemaDespacho sistemaConDatos() throws ValidacionException {
        SistemaDespacho sistema = new SistemaDespacho();
        sistema.cargarDatosDemostracion();
        return sistema;
    }

    private static List<String> codigos(List<SolicitudDespacho> solicitudes) {
        List<String> codigos = new ArrayList<>();
        for (SolicitudDespacho solicitud : solicitudes) {
            codigos.add(solicitud.getCodigo());
        }
        return List.copyOf(codigos);
    }

    private static void verificar(String caso, boolean condicion) {
        verificaciones++;
        if (!condicion) {
            throw new AssertionError("Fallo: " + caso);
        }
    }

    private static void verificarIgual(String caso, Object esperado, Object obtenido) {
        verificaciones++;
        if (!Objects.equals(esperado, obtenido)) {
            throw new AssertionError("Fallo: " + caso + " | esperado=" + esperado + " | obtenido=" + obtenido);
        }
    }

    private static void verificarError(String caso, String fragmento, Accion accion) {
        verificaciones++;
        try {
            accion.ejecutar();
            throw new AssertionError("Fallo: " + caso + " (no se lanzo ValidacionException)");
        } catch (ValidacionException error) {
            String mensaje = error.getMessage().toLowerCase(Locale.ROOT);
            if (!mensaje.contains(fragmento.toLowerCase(Locale.ROOT))) {
                throw new AssertionError("Fallo: " + caso + " | mensaje=" + error.getMessage());
            }
        }
    }

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws ValidacionException;
    }
}
