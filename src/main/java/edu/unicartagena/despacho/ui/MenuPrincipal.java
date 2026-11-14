package edu.unicartagena.despacho.ui;

import edu.unicartagena.despacho.comun.ValidacionException;
import edu.unicartagena.despacho.modelo.Destino;
import edu.unicartagena.despacho.modelo.EstadoSolicitud;
import edu.unicartagena.despacho.modelo.Prioridad;
import edu.unicartagena.despacho.modelo.Producto;
import edu.unicartagena.despacho.modelo.SolicitudDespacho;
import edu.unicartagena.despacho.servicio.ResumenColecciones;
import edu.unicartagena.despacho.servicio.SistemaDespacho;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;

public final class MenuPrincipal {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Consola consola;
    private final SistemaDespacho sistema;
    private boolean ejecutando = true;

    public MenuPrincipal(Consola consola, SistemaDespacho sistema) {
        this.consola = consola;
        this.sistema = sistema;
    }

    public void ejecutar() {
        consola.imprimir("");
        consola.info("Bienvenido al sistema de despacho de mercancías.");
        while (ejecutando) {
            mostrarMenu();
            try {
                int opcion = pedirEntero("Seleccione una opción: ");
                ejecutarOpcion(opcion);
            } catch (OperacionCancelada e) {
                consola.imprimir("");
                consola.info("Entrada finalizada. Cerrando el sistema.");
                ejecutando = false;
            } catch (ValidacionException e) {
                consola.error(e.getMessage());
            }
            consola.imprimir("");
        }
    }

    private void mostrarMenu() {
        ResumenColecciones resumen = sistema.resumen();
        consola.imprimir("Registros: " + resumen.solicitudes()
                + " | En cola: " + resumen.enCola()
                + " | Destinos: " + resumen.destinos()
                + " | Productos: " + resumen.productos());
        consola.imprimir("""
                ==========================================
                 SISTEMA DE DESPACHO DE MERCANCÍAS
                ==========================================
                 1. Registrar producto
                 2. Registrar destino
                 3. Registrar solicitud de despacho
                 4. Consultar todas las solicitudes
                 5. Buscar solicitud por código
                 6. Consultar destinos registrados
                 7. Mostrar despachos por destino
                 8. Agregar solicitud pendiente a la cola
                 9. Consultar próximo despacho prioritario
                10. Procesar siguiente despacho prioritario
                11. Consultar cola en orden de atención
                12. Modificar solicitud
                13. Eliminar solicitud
                14. Mostrar estado de las colecciones
                15. Cargar datos de demostración
                16. Consultar solicitudes por estado
                 0. Salir
                ==========================================""");
    }

    private void ejecutarOpcion(int opcion) throws ValidacionException {
        switch (opcion) {
            case 1 -> registrarProducto();
            case 2 -> registrarDestino();
            case 3 -> registrarSolicitud();
            case 4 -> consultarTodas();
            case 5 -> buscarPorCodigo();
            case 6 -> consultarDestinos();
            case 7 -> mostrarDespachosPorDestino();
            case 8 -> agregarACola();
            case 9 -> consultarProximo();
            case 10 -> procesarSiguiente();
            case 11 -> consultarCola();
            case 12 -> modificarSolicitud();
            case 13 -> eliminarSolicitud();
            case 14 -> mostrarResumen();
            case 15 -> cargarDatosDemostracion();
            case 16 -> consultarPorEstado();
            case 0 -> {
                consola.info("Sesión finalizada.");
                ejecutando = false;
            }
            default -> consola.error("Opción inválida. Seleccione un número entre 0 y 16.");
        }
    }

    private void registrarProducto() throws ValidacionException {
        consola.titulo("REGISTRAR PRODUCTO");
        String codigo = pedirTexto("Código (tres letras, guion y tres dígitos; ejemplo ALM-101): ");
        String nombre = pedirTexto("Nombre del producto: ");
        String categoria = pedirTexto("Categoría: ");
        double peso = pedirDecimal("Peso unitario en kg: ");
        Producto producto = sistema.catalogo().registrar(codigo, nombre, categoria, peso);
        consola.info("Producto registrado: " + producto + " | " + peso(producto.getPesoUnitarioKg()));
    }

    private void registrarDestino() throws ValidacionException {
        consola.titulo("REGISTRAR DESTINO");
        String nombre = pedirTexto("Nombre del destino: ");
        String departamento = pedirTexto("Departamento: ");
        Destino destino = sistema.destinos().registrar(nombre, departamento);
        consola.info("Destino registrado: " + destino);
    }

    private void registrarSolicitud() throws ValidacionException {
        consola.titulo("REGISTRAR SOLICITUD DE DESPACHO");
        if (sistema.destinos().cantidad() == 0 || sistema.catalogo().cantidad() == 0) {
            consola.info("Primero debe registrar al menos un destino y un producto.");
            return;
        }
        mostrarDestinosDisponibles();
        mostrarProductosDisponibles();
        String codigo = pedirTexto("Código de la solicitud (DSP-019): ");
        String destino = pedirTexto("Destino: ");
        String producto = pedirTexto("Código del producto: ");
        int cantidad = pedirEntero("Cantidad (1 a 50000): ");
        Prioridad prioridad = pedirPrioridad();
        SolicitudDespacho solicitud = sistema.despachos().registrar(codigo, destino, producto, cantidad, prioridad);
        consola.info("Solicitud registrada: " + solicitud);
        consola.info("La solicitud quedó pendiente y aún no está en la cola de despacho.");
    }

    private void consultarTodas() {
        consola.titulo("SOLICITUDES REGISTRADAS");
        List<SolicitudDespacho> solicitudes = sistema.despachos().listar();
        if (solicitudes.isEmpty()) {
            consola.info("No hay solicitudes registradas.");
            return;
        }
        consola.imprimir("Orden de registro conservado por ArrayList:");
        imprimirEncabezadoSolicitudes();
        for (SolicitudDespacho solicitud : solicitudes) {
            imprimirFilaSolicitud(solicitud);
        }
        consola.imprimir("Total: " + solicitudes.size() + " solicitudes.");
    }

    private void buscarPorCodigo() {
        consola.titulo("BUSCAR SOLICITUD POR CÓDIGO");
        String codigo = pedirTexto("Código de la solicitud: ");
        Optional<SolicitudDespacho> encontrada = sistema.despachos().buscar(codigo);
        if (encontrada.isEmpty()) {
            consola.info("No existe una solicitud con el código " + codigo.toUpperCase(Locale.ROOT) + ".");
            return;
        }
        mostrarDetalle(encontrada.get());
    }

    private void consultarDestinos() {
        consola.titulo("DESTINOS REGISTRADOS");
        List<Destino> destinos = sistema.destinos().listar();
        if (destinos.isEmpty()) {
            consola.info("No hay destinos registrados.");
            return;
        }
        consola.imprimir("Destinos únicos almacenados en HashSet: " + destinos.size());
        for (Destino destino : destinos) {
            consola.imprimir("  - " + destino);
        }
        consola.imprimir("El registro rechaza cualquier nombre de destino repetido.");
    }

    private void mostrarDespachosPorDestino() {
        consola.titulo("DESPACHOS AGRUPADOS POR DESTINO");
        NavigableMap<Destino, List<SolicitudDespacho>> agrupados = sistema.despachos().despachosPorDestino();
        if (agrupados.isEmpty()) {
            consola.info("No hay despachos registrados.");
            return;
        }
        for (Map.Entry<Destino, List<SolicitudDespacho>> entrada : agrupados.entrySet()) {
            Destino destino = entrada.getKey();
            List<SolicitudDespacho> solicitudes = entrada.getValue();
            consola.imprimir("");
            consola.imprimir(destino.getNombre() + " (" + destino.getDepartamento() + ") - "
                    + solicitudes.size() + " despacho(s)");
            for (SolicitudDespacho solicitud : solicitudes) {
                consola.imprimir("   " + solicitud.getCodigo() + " | " + solicitud.getProducto().getCodigo()
                        + " | " + solicitud.getCantidad() + " unidades | " + solicitud.getPrioridad().getEtiqueta()
                        + " | " + solicitud.getEstado().getEtiqueta());
            }
        }
        consola.imprimir("");
        consola.imprimir("Orden alfabético por nombre de destino mantenido por TreeMap.");
    }

    private void agregarACola() throws ValidacionException {
        consola.titulo("AGREGAR SOLICITUD A LA COLA");
        if (sistema.despachos().cantidad() == 0) {
            consola.info("No hay solicitudes registradas.");
            return;
        }
        String codigo = pedirTexto("Código de la solicitud pendiente: ");
        SolicitudDespacho solicitud = sistema.despachos().encolar(codigo);
        consola.info("Solicitud agregada a la cola: " + solicitud.getCodigo()
                + " | prioridad " + solicitud.getPrioridad().getEtiqueta());
    }

    private void consultarProximo() {
        consola.titulo("PRÓXIMO DESPACHO PRIORITARIO");
        Optional<SolicitudDespacho> proximo = sistema.despachos().proximoDespacho();
        if (proximo.isEmpty()) {
            consola.info("La cola de despacho está vacía.");
            return;
        }
        consola.imprimir("La cola no retira el elemento al consultarlo.");
        mostrarDetalle(proximo.get());
        consola.imprimir("Criterio: mayor prioridad primero y, en empate, menor secuencia de registro.");
    }

    private void procesarSiguiente() throws ValidacionException {
        consola.titulo("PROCESAR SIGUIENTE DESPACHO");
        Optional<SolicitudDespacho> procesada = sistema.despachos().procesarSiguiente();
        if (procesada.isEmpty()) {
            consola.info("La cola de despacho está vacía. No hay despachos por procesar.");
            return;
        }
        consola.info("Despacho procesado y marcado como despachado:");
        mostrarDetalle(procesada.get());
    }

    private void consultarCola() {
        consola.titulo("COLA EN ORDEN DE ATENCIÓN");
        List<SolicitudDespacho> orden = sistema.despachos().colaEnOrdenDeAtencion();
        if (orden.isEmpty()) {
            consola.info("La cola de despacho está vacía.");
            return;
        }
        consola.formato("%-4s %-10s %-11s %-10s %-18s %s%n",
                "POS", "CÓDIGO", "PRIORIDAD", "SECUENCIA", "DESTINO", "ESTADO");
        int posicion = 1;
        for (SolicitudDespacho solicitud : orden) {
            consola.formato("%-4d %-10s %-11s %-10d %-18s %s%n",
                    posicion,
                    solicitud.getCodigo(),
                    solicitud.getPrioridad().getEtiqueta(),
                    solicitud.getSecuencia(),
                    solicitud.getDestino().getNombre(),
                    solicitud.getEstado().getEtiqueta());
            posicion++;
        }
        consola.imprimir("Total en cola: " + orden.size());
    }

    private void modificarSolicitud() throws ValidacionException {
        consola.titulo("MODIFICAR SOLICITUD");
        if (sistema.despachos().cantidad() == 0) {
            consola.info("No hay solicitudes registradas.");
            return;
        }
        String codigo = pedirTexto("Código de la solicitud: ");
        Optional<SolicitudDespacho> encontrada = sistema.despachos().buscar(codigo);
        if (encontrada.isEmpty()) {
            consola.info("No existe una solicitud con el código " + codigo.toUpperCase(Locale.ROOT) + ".");
            return;
        }
        SolicitudDespacho solicitud = encontrada.get();
        mostrarDetalle(solicitud);
        consola.imprimir("""
                1. Código
                2. Destino
                3. Producto
                4. Cantidad
                5. Prioridad
                6. Estado
                0. Cancelar""");
        int opcion = pedirEntero("Seleccione el campo a modificar: ");
        if (opcion == 0) {
            consola.info("Modificación cancelada.");
            return;
        }
        switch (opcion) {
            case 1 -> {
                String nuevoCodigo = pedirTexto("Nuevo código: ");
                sistema.despachos().modificarCodigo(solicitud.getCodigo(), nuevoCodigo);
            }
            case 2 -> {
                mostrarDestinosDisponibles();
                String nuevoDestino = pedirTexto("Nuevo destino: ");
                sistema.despachos().modificarDestino(solicitud.getCodigo(), nuevoDestino);
            }
            case 3 -> {
                mostrarProductosDisponibles();
                String nuevoProducto = pedirTexto("Nuevo código de producto: ");
                sistema.despachos().modificarProducto(solicitud.getCodigo(), nuevoProducto);
            }
            case 4 -> {
                int nuevaCantidad = pedirEntero("Nueva cantidad (1 a 50000): ");
                sistema.despachos().modificarCantidad(solicitud.getCodigo(), nuevaCantidad);
            }
            case 5 -> {
                Prioridad nuevaPrioridad = pedirPrioridad();
                sistema.despachos().modificarPrioridad(solicitud.getCodigo(), nuevaPrioridad);
            }
            case 6 -> {
                EstadoSolicitud nuevoEstado = pedirEstado();
                sistema.despachos().modificarEstado(solicitud.getCodigo(), nuevoEstado);
            }
            default -> {
                consola.error("Opción de modificación inválida.");
                return;
            }
        }
        consola.info("Solicitud actualizada:");
        mostrarDetalle(solicitud);
    }

    private void eliminarSolicitud() throws ValidacionException {
        consola.titulo("ELIMINAR SOLICITUD");
        String codigo = pedirTexto("Código de la solicitud: ");
        Optional<SolicitudDespacho> encontrada = sistema.despachos().buscar(codigo);
        if (encontrada.isEmpty()) {
            consola.info("No existe una solicitud con el código " + codigo.toUpperCase(Locale.ROOT) + ".");
            return;
        }
        mostrarDetalle(encontrada.get());
        if (!confirmar("¿Confirma la eliminación definitiva? (S/N): ")) {
            consola.info("Eliminación cancelada.");
            return;
        }
        SolicitudDespacho eliminada = sistema.despachos().eliminar(encontrada.get().getCodigo());
        consola.info("Solicitud eliminada de la lista, el índice, el árbol de destinos y la cola:");
        mostrarDetalle(eliminada);
    }

    private void mostrarResumen() {
        consola.titulo("ESTADO DE LAS COLECCIONES");
        ResumenColecciones resumen = sistema.resumen();
        consola.imprimir("ArrayList<SolicitudDespacho>        : " + resumen.solicitudes() + " registros");
        consola.imprimir("HashMap<String, SolicitudDespacho>  : " + resumen.indiceCodigos() + " claves");
        consola.imprimir("HashMap<String, Producto>           : " + resumen.productos() + " claves");
        consola.imprimir("HashSet<Destino>                    : " + resumen.destinos() + " destinos");
        consola.imprimir("TreeMap<Destino, List<Solicitud...>>: " + resumen.destinosConDespachos() + " destinos agrupados");
        consola.imprimir("PriorityQueue<SolicitudDespacho>    : " + resumen.enCola() + " en cola");
        consola.imprimir("");
        consola.imprimir("Pendientes: " + resumen.pendientes()
                + " | Despachadas: " + resumen.despachadas()
                + " | Canceladas: " + resumen.canceladas());
        consola.imprimir("Pendientes sin programar en la cola: "
                + (resumen.pendientes() - resumen.enCola()));
    }

    private void cargarDatosDemostracion() throws ValidacionException {
        consola.titulo("CARGAR DATOS DE DEMOSTRACIÓN");
        if (!confirmar("Se reemplazará la información actual. ¿Continuar? (S/N): ")) {
            consola.info("Carga cancelada.");
            return;
        }
        sistema.cargarDatosDemostracion();
        ResumenColecciones resumen = sistema.resumen();
        consola.info("Datos cargados correctamente.");
        consola.info("Solicitudes: " + resumen.solicitudes()
                + " | Productos: " + resumen.productos()
                + " | Destinos: " + resumen.destinos()
                + " | En cola: " + resumen.enCola());
    }

    private void consultarPorEstado() throws ValidacionException {
        consola.titulo("CONSULTAR SOLICITUDES POR ESTADO");
        EstadoSolicitud estado = pedirEstado();
        List<SolicitudDespacho> solicitudes = sistema.despachos().listarPorEstado(estado);
        if (solicitudes.isEmpty()) {
            consola.info("No hay solicitudes en estado " + estado.getEtiqueta().toLowerCase(Locale.ROOT) + ".");
            return;
        }
        imprimirEncabezadoSolicitudes();
        for (SolicitudDespacho solicitud : solicitudes) {
            imprimirFilaSolicitud(solicitud);
        }
        consola.imprimir("Total en estado " + estado.getEtiqueta().toLowerCase(Locale.ROOT)
                + ": " + solicitudes.size());
        if (estado == EstadoSolicitud.PENDIENTE) {
            consola.imprimir("De ellas, " + sistema.despachos().cantidadEnCola()
                    + " están programadas en la cola de despacho.");
        }
    }

    private void mostrarDestinosDisponibles() {
        List<Destino> destinos = sistema.destinos().listar();
        if (destinos.isEmpty()) {
            consola.info("No hay destinos registrados.");
            return;
        }
        consola.imprimir("Destinos disponibles:");
        for (Destino destino : destinos) {
            consola.imprimir("  - " + destino);
        }
    }

    private void mostrarProductosDisponibles() {
        List<Producto> productos = sistema.catalogo().listar();
        if (productos.isEmpty()) {
            consola.info("No hay productos registrados.");
            return;
        }
        consola.imprimir("Productos disponibles:");
        for (Producto producto : productos) {
            consola.imprimir("  - " + producto);
        }
    }

    private void imprimirEncabezadoSolicitudes() {
        consola.formato("%-10s %-18s %-9s %8s %-10s %-11s %-6s %s%n",
                "CÓDIGO", "DESTINO", "PRODUCTO", "CANTIDAD", "PRIORIDAD", "ESTADO", "SEC.", "REGISTRO");
    }

    private void imprimirFilaSolicitud(SolicitudDespacho solicitud) {
        consola.formato("%-10s %-18s %-9s %8d %-10s %-11s %-6d %s%n",
                solicitud.getCodigo(),
                solicitud.getDestino().getNombre(),
                solicitud.getProducto().getCodigo(),
                solicitud.getCantidad(),
                solicitud.getPrioridad().getEtiqueta(),
                solicitud.getEstado().getEtiqueta(),
                solicitud.getSecuencia(),
                solicitud.getFechaRegistro().format(FORMATO_FECHA));
    }

    private void mostrarDetalle(SolicitudDespacho solicitud) {
        consola.imprimir("Código:     " + solicitud.getCodigo());
        consola.imprimir("Destino:    " + solicitud.getDestino());
        consola.imprimir("Producto:   " + solicitud.getProducto());
        consola.imprimir("Cantidad:   " + solicitud.getCantidad());
        consola.imprimir("Peso total: " + peso(solicitud.getPesoTotalKg()));
        consola.imprimir("Prioridad:  " + solicitud.getPrioridad().getEtiqueta());
        consola.imprimir("Estado:     " + solicitud.getEstado().getEtiqueta());
        consola.imprimir("Registro:   secuencia " + solicitud.getSecuencia()
                + " | " + solicitud.getFechaRegistro().format(FORMATO_FECHA));
        consola.imprimir("En cola:    " + (sistema.despachos().estaEnCola(solicitud.getCodigo()) ? "Sí" : "No"));
    }

    private Prioridad pedirPrioridad() throws ValidacionException {
        consola.imprimir("Prioridades: 1) Crítica  2) Alta  3) Media  4) Baja");
        int opcion = pedirEntero("Seleccione la prioridad: ");
        return Prioridad.desdeOpcion(opcion)
                .orElseThrow(() -> new ValidacionException("La prioridad debe estar entre 1 y 4."));
    }

    private EstadoSolicitud pedirEstado() throws ValidacionException {
        consola.imprimir("Estados: 1) Pendiente  2) Despachada  3) Cancelada");
        int opcion = pedirEntero("Seleccione el estado: ");
        return EstadoSolicitud.desdeOpcion(opcion)
                .orElseThrow(() -> new ValidacionException("El estado debe estar entre 1 y 3."));
    }

    private boolean confirmar(String mensaje) {
        while (true) {
            String respuesta = pedirTexto(mensaje).toLowerCase(Locale.ROOT);
            if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) {
                return true;
            }
            if (respuesta.equals("n") || respuesta.equals("no")) {
                return false;
            }
            consola.error("Responda S o N.");
        }
    }

    private String pedirTexto(String mensaje) {
        String valor = consola.leerTexto(mensaje);
        if (valor == null) {
            throw new OperacionCancelada();
        }
        return valor;
    }

    private int pedirEntero(String mensaje) {
        while (true) {
            String valor = pedirTexto(mensaje);
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                consola.error("Ingrese un número entero válido.");
            }
        }
    }

    private double pedirDecimal(String mensaje) {
        while (true) {
            String valor = pedirTexto(mensaje).replace(',', '.');
            try {
                return Double.parseDouble(valor);
            } catch (NumberFormatException e) {
                consola.error("Ingrese un número decimal válido.");
            }
        }
    }

    private String peso(double kilogramos) {
        return String.format(Locale.ROOT, "%.2f kg", kilogramos);
    }

    private static final class OperacionCancelada extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}
