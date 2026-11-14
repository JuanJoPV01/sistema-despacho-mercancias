package edu.unicartagena.despacho.servicio;

import edu.unicartagena.despacho.comun.Validador;
import edu.unicartagena.despacho.comun.ValidacionException;
import edu.unicartagena.despacho.modelo.Destino;
import edu.unicartagena.despacho.modelo.EstadoSolicitud;
import edu.unicartagena.despacho.modelo.Prioridad;
import edu.unicartagena.despacho.modelo.Producto;
import edu.unicartagena.despacho.modelo.SolicitudDespacho;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

public final class GestorDespachos {

    private final CatalogoProductos catalogo;
    private final RegistroDestinos registroDestinos;
    private final List<SolicitudDespacho> solicitudes = new ArrayList<>();
    private final Map<String, SolicitudDespacho> solicitudesPorCodigo = new HashMap<>();
    private final NavigableMap<Destino, List<SolicitudDespacho>> despachosPorDestino = new TreeMap<>();
    private final Queue<SolicitudDespacho> colaDespacho = new PriorityQueue<>(Comparadores.PRIORIDAD_DESPACHO);
    private long secuencia;

    public GestorDespachos(CatalogoProductos catalogo, RegistroDestinos registroDestinos) {
        this.catalogo = catalogo;
        this.registroDestinos = registroDestinos;
    }

    public SolicitudDespacho registrar(String codigo, String nombreDestino, String codigoProducto, int cantidad,
            Prioridad prioridad) throws ValidacionException {
        String destinoBuscado = Validador.textoObligatorio(nombreDestino, "nombre del destino");
        Destino destino = registroDestinos.buscar(destinoBuscado)
                .orElseThrow(() -> new ValidacionException(
                        "No existe un destino registrado con el nombre " + destinoBuscado + "."));
        String productoBuscado = Validador.textoObligatorio(codigoProducto, "código del producto");
        Producto producto = catalogo.buscar(productoBuscado)
                .orElseThrow(() -> new ValidacionException(
                        "No existe un producto registrado con el código " + productoBuscado.toUpperCase(Locale.ROOT) + "."));
        Validador.requerido(prioridad, "prioridad");

        SolicitudDespacho solicitud = new SolicitudDespacho(codigo, destino, producto, cantidad, prioridad,
                secuencia + 1, LocalDateTime.now());
        if (solicitudesPorCodigo.containsKey(solicitud.getCodigo())) {
            throw new ValidacionException("Ya existe una solicitud con el código " + solicitud.getCodigo() + ".");
        }

        secuencia++;
        solicitudes.add(solicitud);
        solicitudesPorCodigo.put(solicitud.getCodigo(), solicitud);
        agregarAlArbol(solicitud);
        return solicitud;
    }

    public List<SolicitudDespacho> listar() {
        return List.copyOf(solicitudes);
    }

    public List<SolicitudDespacho> listarPorEstado(EstadoSolicitud estado) {
        List<SolicitudDespacho> resultado = new ArrayList<>();
        for (SolicitudDespacho solicitud : solicitudes) {
            if (solicitud.getEstado() == estado) {
                resultado.add(solicitud);
            }
        }
        return List.copyOf(resultado);
    }

    public Optional<SolicitudDespacho> buscar(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(solicitudesPorCodigo.get(codigo.trim().toUpperCase(Locale.ROOT)));
    }

    public boolean existe(String codigo) {
        return buscar(codigo).isPresent();
    }

    public NavigableMap<Destino, List<SolicitudDespacho>> despachosPorDestino() {
        NavigableMap<Destino, List<SolicitudDespacho>> copia = new TreeMap<>(despachosPorDestino);
        copia.replaceAll((destino, grupo) -> List.copyOf(grupo));
        return Collections.unmodifiableNavigableMap(copia);
    }

    public int cantidad() {
        return solicitudes.size();
    }

    public int cantidadIndiceCodigos() {
        return solicitudesPorCodigo.size();
    }

    public int cantidadDestinosConDespachos() {
        return despachosPorDestino.size();
    }

    public SolicitudDespacho encolar(String codigo) throws ValidacionException {
        SolicitudDespacho solicitud = buscarObligatoria(codigo);
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ValidacionException("La solicitud " + solicitud.getCodigo()
                    + " no está pendiente y no puede agregarse a la cola de despacho.");
        }
        if (colaDespacho.contains(solicitud)) {
            throw new ValidacionException("La solicitud " + solicitud.getCodigo()
                    + " ya está en la cola de despacho.");
        }
        colaDespacho.add(solicitud);
        return solicitud;
    }

    public Optional<SolicitudDespacho> proximoDespacho() {
        return Optional.ofNullable(colaDespacho.peek());
    }

    public Optional<SolicitudDespacho> procesarSiguiente() throws ValidacionException {
        SolicitudDespacho solicitud = colaDespacho.poll();
        if (solicitud == null) {
            return Optional.empty();
        }
        solicitud.setEstado(EstadoSolicitud.DESPACHADA);
        return Optional.of(solicitud);
    }

    public List<SolicitudDespacho> colaEnOrdenDeAtencion() {
        Queue<SolicitudDespacho> copia = new PriorityQueue<>(Comparadores.PRIORIDAD_DESPACHO);
        copia.addAll(colaDespacho);
        List<SolicitudDespacho> orden = new ArrayList<>();
        while (!copia.isEmpty()) {
            orden.add(copia.poll());
        }
        return List.copyOf(orden);
    }

    public int cantidadEnCola() {
        return colaDespacho.size();
    }

    public int cantidadPorEstado(EstadoSolicitud estado) {
        int total = 0;
        for (SolicitudDespacho solicitud : solicitudes) {
            if (solicitud.getEstado() == estado) {
                total++;
            }
        }
        return total;
    }

    public SolicitudDespacho modificarCodigo(String codigoActual, String nuevoCodigo) throws ValidacionException {
        SolicitudDespacho solicitud = buscarObligatoria(codigoActual);
        String normalizado = SolicitudDespacho.validarCodigo(nuevoCodigo);
        String anterior = solicitud.getCodigo();
        if (normalizado.equals(anterior)) {
            return solicitud;
        }
        if (solicitudesPorCodigo.containsKey(normalizado)) {
            throw new ValidacionException("Ya existe una solicitud con el código " + normalizado + ".");
        }
        solicitudesPorCodigo.remove(anterior);
        solicitud.setCodigo(normalizado);
        solicitudesPorCodigo.put(normalizado, solicitud);
        return solicitud;
    }

    public SolicitudDespacho modificarDestino(String codigo, String nombreDestino) throws ValidacionException {
        SolicitudDespacho solicitud = buscarObligatoria(codigo);
        String buscado = Validador.textoObligatorio(nombreDestino, "nombre del destino");
        Destino nuevoDestino = registroDestinos.buscar(buscado)
                .orElseThrow(() -> new ValidacionException(
                        "No existe un destino registrado con el nombre " + buscado + "."));
        if (nuevoDestino.equals(solicitud.getDestino())) {
            return solicitud;
        }
        quitarDelArbol(solicitud);
        solicitud.setDestino(nuevoDestino);
        agregarAlArbol(solicitud);
        return solicitud;
    }

    public SolicitudDespacho modificarProducto(String codigo, String codigoProducto) throws ValidacionException {
        SolicitudDespacho solicitud = buscarObligatoria(codigo);
        String buscado = Validador.textoObligatorio(codigoProducto, "código del producto");
        Producto nuevoProducto = catalogo.buscar(buscado)
                .orElseThrow(() -> new ValidacionException(
                        "No existe un producto registrado con el código " + buscado.toUpperCase(Locale.ROOT) + "."));
        solicitud.setProducto(nuevoProducto);
        return solicitud;
    }

    public SolicitudDespacho modificarCantidad(String codigo, int cantidad) throws ValidacionException {
        SolicitudDespacho solicitud = buscarObligatoria(codigo);
        solicitud.setCantidad(cantidad);
        return solicitud;
    }

    public SolicitudDespacho modificarPrioridad(String codigo, Prioridad prioridad) throws ValidacionException {
        SolicitudDespacho solicitud = buscarObligatoria(codigo);
        solicitud.setPrioridad(prioridad);
        boolean estabaEnCola = colaDespacho.remove(solicitud);
        if (estabaEnCola) {
            colaDespacho.add(solicitud);
        }
        return solicitud;
    }

    public SolicitudDespacho modificarEstado(String codigo, EstadoSolicitud estado) throws ValidacionException {
        SolicitudDespacho solicitud = buscarObligatoria(codigo);
        solicitud.setEstado(estado);
        colaDespacho.remove(solicitud);
        return solicitud;
    }

    public SolicitudDespacho eliminar(String codigo) throws ValidacionException {
        SolicitudDespacho solicitud = buscarObligatoria(codigo);
        solicitudes.remove(solicitud);
        solicitudesPorCodigo.remove(solicitud.getCodigo());
        quitarDelArbol(solicitud);
        colaDespacho.remove(solicitud);
        return solicitud;
    }

    public void limpiar() {
        solicitudes.clear();
        solicitudesPorCodigo.clear();
        despachosPorDestino.clear();
        colaDespacho.clear();
        secuencia = 0;
    }

    private SolicitudDespacho buscarObligatoria(String codigo) throws ValidacionException {
        return buscar(codigo).orElseThrow(() -> new ValidacionException("No existe una solicitud con el código "
                + (codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT)) + "."));
    }

    private void agregarAlArbol(SolicitudDespacho solicitud) {
        despachosPorDestino.computeIfAbsent(solicitud.getDestino(), destino -> new ArrayList<>())
                .add(solicitud);
    }

    private void quitarDelArbol(SolicitudDespacho solicitud) {
        List<SolicitudDespacho> grupo = despachosPorDestino.get(solicitud.getDestino());
        if (grupo != null) {
            grupo.remove(solicitud);
            if (grupo.isEmpty()) {
                despachosPorDestino.remove(solicitud.getDestino());
            }
        }
    }
}
