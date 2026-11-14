package edu.unicartagena.despacho.modelo;

import edu.unicartagena.despacho.comun.Validador;
import edu.unicartagena.despacho.comun.ValidacionException;
import java.time.LocalDateTime;

public final class SolicitudDespacho {

    private static final String PATRON_CODIGO = "DSP-[0-9]{3,6}";
    private static final String FORMATO_CODIGO = "DSP- seguido de tres a seis dígitos (por ejemplo DSP-001)";

    private String codigo;
    private Destino destino;
    private Producto producto;
    private int cantidad;
    private Prioridad prioridad;
    private EstadoSolicitud estado;
    private final long secuencia;
    private final LocalDateTime fechaRegistro;

    public SolicitudDespacho(String codigo, Destino destino, Producto producto, int cantidad, Prioridad prioridad,
            long secuencia, LocalDateTime fechaRegistro) throws ValidacionException {
        this.codigo = validarCodigo(codigo);
        this.destino = Validador.requerido(destino, "destino de la solicitud");
        this.producto = Validador.requerido(producto, "producto de la solicitud");
        this.cantidad = Validador.enteroEnRango(cantidad, 1, 50000, "cantidad");
        this.prioridad = Validador.requerido(prioridad, "prioridad");
        this.estado = EstadoSolicitud.PENDIENTE;
        if (secuencia <= 0) {
            throw new ValidacionException("La secuencia de registro debe ser mayor que cero.");
        }
        this.secuencia = secuencia;
        this.fechaRegistro = Validador.requerido(fechaRegistro, "fecha de registro");
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) throws ValidacionException {
        this.codigo = validarCodigo(codigo);
    }

    public static String validarCodigo(String codigo) throws ValidacionException {
        return Validador.codigo(codigo, "código de la solicitud", PATRON_CODIGO, FORMATO_CODIGO);
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) throws ValidacionException {
        this.destino = Validador.requerido(destino, "destino de la solicitud");
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) throws ValidacionException {
        this.producto = Validador.requerido(producto, "producto de la solicitud");
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) throws ValidacionException {
        this.cantidad = Validador.enteroEnRango(cantidad, 1, 50000, "cantidad");
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Prioridad prioridad) throws ValidacionException {
        this.prioridad = Validador.requerido(prioridad, "prioridad");
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) throws ValidacionException {
        this.estado = Validador.requerido(estado, "estado de la solicitud");
    }

    public long getSecuencia() {
        return secuencia;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public double getPesoTotalKg() {
        return producto.getPesoUnitarioKg() * cantidad;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof SolicitudDespacho otra)) {
            return false;
        }
        return codigo.equals(otra.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return codigo + " | " + destino.getNombre() + " | " + producto.getCodigo() + " | " + cantidad
                + " | " + prioridad.getEtiqueta() + " | " + estado.getEtiqueta();
    }
}
