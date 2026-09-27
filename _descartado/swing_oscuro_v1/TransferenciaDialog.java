package com.projects.infrastructure.adapter.in.swing;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

public class TransferenciaDialog extends JDialog {

    private static final BigDecimal SALDO_EJEMPLO = new BigDecimal("1250.00");

    private final JTextField campoCelular = new JTextField(12);
    private final JLabel etiquetaDestinatario = new JLabel(" ");
    private final JTextField campoMonto = new JTextField(12);
    private final JButton botonEnviar = Estilos.botonPrimario("Enviar");
    private final JLabel etiquetaError = new JLabel(" ");
    private String destinatario;

    public TransferenciaDialog(Window owner) {
        super(owner, "Transferir dinero", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        var panel = new JPanel(new GridBagLayout());
        panel.setBorder(Estilos.margen(24));
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        var buscar = new JButton("Buscar");
        buscar.addActionListener(e -> buscarDestinatario());

        campoMonto.setEnabled(false);
        botonEnviar.setEnabled(false);
        botonEnviar.addActionListener(e -> enviar());
        etiquetaError.setForeground(Estilos.PELIGRO);

        var cancelar = new JButton("Cancelar");
        cancelar.addActionListener(e -> dispose());
        var botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(cancelar);
        botones.add(botonEnviar);

        agregar(panel, gbc, Estilos.titulo("1. Destinatario", 15f), 0, 0, 3);
        agregar(panel, gbc, new JLabel("Celular:"), 0, 1, 1);
        agregar(panel, gbc, campoCelular, 1, 1, 1);
        agregar(panel, gbc, buscar, 2, 1, 1);
        agregar(panel, gbc, etiquetaDestinatario, 1, 2, 2);
        agregar(panel, gbc, Estilos.titulo("2. Monto", 15f), 0, 3, 3);
        agregar(panel, gbc, new JLabel("Monto (S/):"), 0, 4, 1);
        agregar(panel, gbc, campoMonto, 1, 4, 2);
        agregar(panel, gbc, Estilos.secundario("Saldo disponible: " + Estilos.soles(SALDO_EJEMPLO)), 1, 5, 2);
        agregar(panel, gbc, etiquetaError, 0, 6, 3);
        agregar(panel, gbc, botones, 0, 7, 3);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
    }

    private void buscarDestinatario() {
        if (!campoCelular.getText().trim().matches("\\d{9}")) {
            etiquetaError.setText("Ingresa un celular de 9 dígitos.");
            return;
        }
        destinatario = "Juan P***";
        etiquetaDestinatario.setText("Destinatario: " + destinatario);
        etiquetaDestinatario.setForeground(Estilos.EXITO);
        etiquetaError.setText(" ");
        campoMonto.setEnabled(true);
        botonEnviar.setEnabled(true);
        campoMonto.requestFocusInWindow();
    }

    private void enviar() {
        BigDecimal monto;
        try {
            monto = new BigDecimal(campoMonto.getText().trim());
        } catch (NumberFormatException ex) {
            etiquetaError.setText("Ingresa un monto válido.");
            return;
        }
        if (monto.signum() <= 0) {
            etiquetaError.setText("El monto debe ser mayor a cero.");
            return;
        }
        if (monto.compareTo(SALDO_EJEMPLO) > 0) {
            etiquetaError.setText("Saldo insuficiente.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Enviar " + Estilos.soles(monto) + " a " + destinatario + "?",
                "Confirmar transferencia", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        var datos = new LinkedHashMap<String, String>();
        datos.put("Monto", Estilos.soles(monto));
        datos.put("Destinatario", destinatario);
        datos.put("Celular destino", Estilos.enmascararCuenta(campoCelular.getText().trim()));
        datos.put("Saldo resultante", Estilos.soles(SALDO_EJEMPLO.subtract(monto)));

        dispose();
        ComprobanteDialog.mostrar(getOwner(), "Transferencia enviada", datos);
    }

    private static void agregar(JPanel panel, GridBagConstraints gbc, JComponent componente, int x, int y, int ancho) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = ancho;
        panel.add(componente, gbc);
    }
}
