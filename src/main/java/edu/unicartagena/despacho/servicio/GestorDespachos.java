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
import java.util.TreeMap;

public final class GestorDespachos {

    private final CatalogoProductos catalogo;
    private final RegistroDestinos registroDestinos;
    private final List<SolicitudDespacho> solicitudes = new ArrayList<>();
    private final Map<String, SolicitudDespacho> solicitudesPorCodigo = new HashMap<>();
    private final NavigableMap<Destino, List<SolicitudDespacho>> despachosPorDestino = new TreeMap<>();
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

    public void limpiar() {
        solicitudes.clear();
        solicitudesPorCodigo.clear();
        despachosPorDestino.clear();
        secuencia = 0;
    }

    private void agregarAlArbol(SolicitudDespacho solicitud) {
        despachosPorDestino.computeIfAbsent(solicitud.getDestino(), destino -> new ArrayList<>())
                .add(solicitud);
    }
}
