package com.projects.infrastructure.adapter.in.swing;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class DashboardPanel extends JPanel {

    private static final String[] COLUMNAS_MOVIMIENTOS = {"Fecha", "Tipo", "Monto", "Saldo posterior"};

    private static final Object[][] MOVIMIENTOS_EJEMPLO = {
            {"25/09/2026 18:42", "Transferencia recibida", "+ S/ 367.08", "S/ 1,250.00"},
            {"10/09/2026 10:15", "Pago de cuota", "- S/ 152.58", "S/ 882.92"},
            {"05/09/2026 16:03", "Retiro", "- S/ 200.00", "S/ 1,035.50"},
            {"20/08/2026 09:30", "Transferencia enviada", "- S/ 64.50", "S/ 1,235.50"},
            {"10/07/2026 12:00", "Desembolso de crédito", "+ S/ 800.00", "S/ 1,300.00"},
            {"01/07/2026 08:20", "Carga inicial", "+ S/ 500.00", "S/ 500.00"},
    };

    public DashboardPanel(Runnable onTransferir, Runnable onRetirar, Runnable onCreditos, Runnable onCerrarSesion) {
        super(new BorderLayout(0, 24));
        setBorder(Estilos.margen(24));
        add(crearEncabezado(onTransferir, onRetirar, onCreditos, onCerrarSesion), BorderLayout.NORTH);
        add(Estilos.seccion("Últimos movimientos", crearTablaMovimientos()), BorderLayout.CENTER);
    }

    private JPanel crearEncabezado(Runnable onTransferir, Runnable onRetirar, Runnable onCreditos, Runnable onCerrarSesion) {
        var estado = new JLabel("● ACTIVA");
        estado.setForeground(Estilos.EXITO);

        var info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(Estilos.titulo("Hola, Juan", 22f));
        info.add(Box.createVerticalStrut(14));
        info.add(Estilos.secundario("Saldo disponible"));
        info.add(Estilos.titulo(Estilos.soles(new BigDecimal("1250.00")), 34f));
        info.add(Box.createVerticalStrut(4));
        info.add(estado);

        var actualizar = new JButton("Actualizar");
        var cerrarSesion = new JButton("Cerrar sesión");
        cerrarSesion.addActionListener(e -> onCerrarSesion.run());

        var sesion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sesion.add(actualizar);
        sesion.add(cerrarSesion);

        var derecha = new JPanel(new BorderLayout());
        derecha.add(sesion, BorderLayout.NORTH);

        var fila = new JPanel(new BorderLayout());
        fila.add(info, BorderLayout.WEST);
        fila.add(derecha, BorderLayout.EAST);

        var acciones = Box.createHorizontalBox();
        acciones.add(boton(Estilos.botonPrimario("Transferir"), onTransferir));
        acciones.add(Box.createHorizontalStrut(10));
        acciones.add(boton(new JButton("Retirar"), onRetirar));
        acciones.add(Box.createHorizontalStrut(10));
        acciones.add(boton(new JButton("Créditos"), onCreditos));

        var encabezado = new JPanel(new BorderLayout(0, 18));
        encabezado.add(fila, BorderLayout.CENTER);
        encabezado.add(acciones, BorderLayout.SOUTH);
        return encabezado;
    }

    private JScrollPane crearTablaMovimientos() {
        var tabla = Estilos.tabla(COLUMNAS_MOVIMIENTOS, MOVIMIENTOS_EJEMPLO);
        tabla.getColumnModel().getColumn(2).setCellRenderer(Estilos.rendererMontoConSigno());
        tabla.getColumnModel().getColumn(3).setCellRenderer(Estilos.rendererDerecha());
        return new JScrollPane(tabla);
    }

    private static JButton boton(JButton boton, Runnable accion) {
        boton.addActionListener(e -> accion.run());
        return boton;
    }
}
