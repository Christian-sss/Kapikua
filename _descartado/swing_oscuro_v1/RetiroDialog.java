package com.projects.infrastructure.adapter.in.swing;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

public class RetiroDialog extends JDialog {

    private static final BigDecimal SALDO_EJEMPLO = new BigDecimal("1250.00");
    private static final String[] BANCOS = {"BCP", "BBVA", "Interbank", "Scotiabank", "BanBif", "Banco de la Nación"};

    private final JComboBox<String> comboBanco = new JComboBox<>(BANCOS);
    private final JTextField campoCuenta = new JTextField(18);
    private final JTextField campoMonto = new JTextField(18);
    private final JLabel etiquetaError = new JLabel(" ");

    public RetiroDialog(Window owner) {
        super(owner, "Retirar a cuenta bancaria", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        var panel = new JPanel(new GridBagLayout());
        panel.setBorder(Estilos.margen(24));
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        etiquetaError.setForeground(Estilos.PELIGRO);

        var cancelar = new JButton("Cancelar");
        cancelar.addActionListener(e -> dispose());
        var retirar = Estilos.botonPrimario("Retirar");
        retirar.addActionListener(e -> retirar());
        var botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(cancelar);
        botones.add(retirar);

        agregar(panel, gbc, Estilos.titulo("Datos del retiro", 15f), 0, 0, 2);
        agregar(panel, gbc, new JLabel("Banco:"), 0, 1, 1);
        agregar(panel, gbc, comboBanco, 1, 1, 1);
        agregar(panel, gbc, new JLabel("N° de cuenta o CCI:"), 0, 2, 1);
        agregar(panel, gbc, campoCuenta, 1, 2, 1);
        agregar(panel, gbc, new JLabel("Monto (S/):"), 0, 3, 1);
        agregar(panel, gbc, campoMonto, 1, 3, 1);
        agregar(panel, gbc, Estilos.secundario("Saldo disponible: " + Estilos.soles(SALDO_EJEMPLO)), 1, 4, 1);
        agregar(panel, gbc, etiquetaError, 0, 5, 2);
        agregar(panel, gbc, botones, 0, 6, 2);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
    }

    private void retirar() {
        var cuenta = campoCuenta.getText().trim();
        if (!cuenta.matches("\\d{10,20}")) {
            etiquetaError.setText("Ingresa un número de cuenta o CCI válido.");
            return;
        }

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

        var banco = (String) comboBanco.getSelectedItem();
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Retirar " + Estilos.soles(monto) + " a la cuenta " + Estilos.enmascararCuenta(cuenta) + " de " + banco + "?",
                "Confirmar retiro", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        var datos = new LinkedHashMap<String, String>();
        datos.put("Monto", Estilos.soles(monto));
        datos.put("Banco", banco);
        datos.put("Cuenta destino", Estilos.enmascararCuenta(cuenta));
        datos.put("Saldo resultante", Estilos.soles(SALDO_EJEMPLO.subtract(monto)));

        dispose();
        ComprobanteDialog.mostrar(getOwner(), "Retiro a cuenta bancaria", datos);
    }

    private static void agregar(JPanel panel, GridBagConstraints gbc, JComponent componente, int x, int y, int ancho) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = ancho;
        panel.add(componente, gbc);
    }
}
