package edu.unicartagena.despacho;

import edu.unicartagena.despacho.comun.ValidacionException;
import edu.unicartagena.despacho.servicio.SistemaDespacho;
import edu.unicartagena.despacho.ui.Consola;
import edu.unicartagena.despacho.ui.MenuPrincipal;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Consola consola = new Consola(System.in);
        SistemaDespacho sistema = new SistemaDespacho();
        try {
            sistema.cargarDatosDemostracion();
        } catch (ValidacionException e) {
            consola.error("No fue posible cargar los datos iniciales: " + e.getMessage());
            return;
        }
        new MenuPrincipal(consola, sistema).ejecutar();
    }
}
