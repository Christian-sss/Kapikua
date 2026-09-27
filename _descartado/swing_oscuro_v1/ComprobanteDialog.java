package com.projects.infrastructure.adapter.in.swing;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class ComprobanteDialog extends JDialog {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void mostrar(Window owner, String tipoOperacion, Map<String, String> datos) {
        new ComprobanteDialog(owner, tipoOperacion, datos).setVisible(true);
    }

    private ComprobanteDialog(Window owner, String tipoOperacion, Map<String, String> datos) {
        super(owner, "Comprobante", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        var panel = new JPanel(new BorderLayout(0, 16));
        panel.setBorder(Estilos.margen(24));

        var exito = Estilos.titulo("✔ Operación exitosa", 18f);
        exito.setForeground(Estilos.EXITO);
        var encabezado = new JPanel(new GridLayout(2, 1, 0, 4));
        encabezado.add(exito);
        encabezado.add(Estilos.titulo(tipoOperacion, 15f));
        panel.add(encabezado, BorderLayout.NORTH);

        var filas = new LinkedHashMap<String, String>();
        filas.put("N° de operación", String.format("OP-%06d", System.currentTimeMillis() % 1_000_000));
        filas.put("Fecha y hora", LocalDateTime.now().format(FORMATO_FECHA));
        filas.putAll(datos);
        panel.add(crearDetalle(filas), BorderLayout.CENTER);

        var descargarPdf = new JButton("Descargar PDF");
        descargarPdf.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "La exportación a PDF (iText) aún no está implementada.",
                "Pendiente", JOptionPane.INFORMATION_MESSAGE));
        var cerrar = Estilos.botonPrimario("Cerrar");
        cerrar.addActionListener(e -> dispose());

        var botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(descargarPdf);
        botones.add(cerrar);
        panel.add(botones, BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
        setMinimumSize(new Dimension(380, getHeight()));
        setLocationRelativeTo(owner);
    }

    private JPanel crearDetalle(Map<String, String> filas) {
        var detalle = new JPanel(new GridBagLayout());
        detalle.setBorder(Estilos.bordeTarjeta());
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);

        int fila = 0;
        for (var entrada : filas.entrySet()) {
            gbc.gridy = fila++;
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.LINE_START;
            detalle.add(Estilos.secundario(entrada.getKey()), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.anchor = GridBagConstraints.LINE_END;
            detalle.add(Estilos.titulo(entrada.getValue(), 13f), gbc);
        }
        return detalle;
    }
}
