package com.projects.infrastructure.adapter.in.swing.gui;

import com.projects.application.dto.command.BuscarDestinatarioCommand;
import com.projects.application.dto.command.GenerarComprobanteCommand;
import com.projects.application.dto.command.TransferirMontoCommand;
import com.projects.application.port.in.BuscarDestinatarioUseCase;
import com.projects.application.port.in.GenerarComprobanteUseCase;
import com.projects.application.port.in.TransferirMontoUseCase;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;

/**
 * FrmTransferencia - Formulario para realizar transferencias de dinero por número telefónico.
 * Implementado según la Sección 6.4 del Plan de Implementación.
 * Posee validaciones numéricas, verificación de saldo disponible y retorno a FrmBilletera.
 */
public class FrmTransferencia extends javax.swing.JFrame {

    private static final BigDecimal SALDO_EJEMPLO = new BigDecimal("1250.00");

    private final FrmBilletera parentBilletera;
    private final BuscarDestinatarioUseCase buscarDestinatarioUseCase;
    private final TransferirMontoUseCase transferirMontoUseCase;
    private final GenerarComprobanteUseCase generarComprobanteUseCase;

    /**
     * Constructor por defecto
     */
    public FrmTransferencia() {
        this(null);
    }

    /**
     * Constructor con referencia a la billetera principal
     */
    public FrmTransferencia(FrmBilletera parentBilletera) {
        this(parentBilletera,
                CompositionRoot.crearBuscarDestinatarioUseCase(),
                CompositionRoot.crearTransferirMontoUseCase(),
                CompositionRoot.crearGenerarComprobanteUseCase());
    }

    public FrmTransferencia(FrmBilletera parentBilletera, BuscarDestinatarioUseCase buscarDestinatarioUseCase,
                             TransferirMontoUseCase transferirMontoUseCase, GenerarComprobanteUseCase generarComprobanteUseCase) {
        this.parentBilletera = parentBilletera;
        this.buscarDestinatarioUseCase = buscarDestinatarioUseCase;
        this.transferirMontoUseCase = transferirMontoUseCase;
        this.generarComprobanteUseCase = generarComprobanteUseCase;
        initComponents();
        configurarEstilos();
        actualizarSaldoVisual();
        UITheme.setupWindow(this, "KAPIKUA - Transferir dinero");
    }

    private void configurarEstilos() {
        getContentPane().setBackground(UITheme.BG_WHITE);
        pnlFondo.setBackground(UITheme.BG_WHITE);
        UITheme.styleCardPanel(pnlTarjeta);

        UITheme.styleTextField(txtNumeroDestino);
        UITheme.styleTextField(txtMonto);

        UITheme.stylePrimaryButtonGreen(btnTransferir);
        UITheme.styleNeutralButton(btnCancelar);

        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png")));
        lblTitulo.setIconTextGap(8);
        lblTitulo.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitulo.setForeground(UITheme.VERDE_PROFUNDO);

        lblSubtitulo.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitulo.setForeground(UITheme.TEXT_SECONDARY);
        lblSubtitulo.setBorder(new javax.swing.border.EmptyBorder(0, 0, 10, 0) {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                int lineW = 60;
                int startX = x + (width - lineW) / 2;
                int lineY = y + height - 3;
                g2.setColor(UITheme.AMARILLO_KAPIKUA);
                g2.fillRect(startX, lineY, lineW / 2, 3);
                g2.setColor(UITheme.VERDE_KAPIKUA);
                g2.fillRect(startX + lineW / 2, lineY, lineW / 2, 3);
                g2.dispose();
            }
        });

        lblNumeroDestino.setFont(UITheme.FONT_LABEL);
        lblNumeroDestino.setForeground(UITheme.TEXT_PRIMARY);
        lblMonto.setFont(UITheme.FONT_LABEL);
        lblMonto.setForeground(UITheme.TEXT_PRIMARY);

        lblSaldoDisponible.setFont(UITheme.FONT_REGULAR);
        lblSaldoDisponible.setForeground(UITheme.VERDE_PROFUNDO);

        lblMensaje.setFont(UITheme.FONT_SMALL);
        lblMensaje.setForeground(UITheme.DANGER);
        lblMensaje.setText(" ");
    }

    private BigDecimal saldoActual() {
        return (parentBilletera != null) ? parentBilletera.getSaldo() : SALDO_EJEMPLO;
    }

    private void actualizarSaldoVisual() {
        lblSaldoDisponible.setText(String.format(java.util.Locale.US, "Saldo disponible: S/ %,.2f", saldoActual()));
    }

    /**
     * Valida los campos del formulario según Sección 6.4
     */
    public boolean validarCampos() {
        String celular = txtNumeroDestino.getText().trim();
        String montoStr = txtMonto.getText().trim();

        if (celular.isEmpty() || montoStr.isEmpty()) {
            mostrarMensaje("Todos los campos son obligatorios.", true);
            return false;
        }

        if (!celular.matches("^\\d{9}$")) {
            mostrarMensaje("El celular de destino debe tener 9 dígitos numéricos.", true);
            txtNumeroDestino.requestFocus();
            return false;
        }

        BigDecimal monto;
        try {
            monto = new BigDecimal(montoStr);
        } catch (NumberFormatException e) {
            mostrarMensaje("El monto ingresado no es válido.", true);
            txtMonto.requestFocus();
            return false;
        }

        if (monto.signum() <= 0) {
            mostrarMensaje("El monto debe ser mayor que cero.", true);
            txtMonto.requestFocus();
            return false;
        }

        if (monto.compareTo(saldoActual()) > 0) {
            mostrarMensaje("Saldo insuficiente para completar la transferencia.", true);
            txtMonto.requestFocus();
            return false;
        }

        limpiarMensaje();
        return true;
    }

    public void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setForeground(esError ? UITheme.DANGER : UITheme.ACCENT_SUCCESS);
        lblMensaje.setText(mensaje);
    }

    public void limpiarMensaje() {
        lblMensaje.setText(" ");
    }

    public void limpiarCampos() {
        txtNumeroDestino.setText("");
        txtMonto.setText("");
        limpiarMensaje();
    }

    public void setDatos(String celular, String monto) {
        txtNumeroDestino.setText(celular != null ? celular : "");
        txtMonto.setText(monto != null ? monto : "");
    }

    /**
     * Acción Transferir
     */
    private void btnTransferirActionPerformed(java.awt.event.ActionEvent evt) {
        if (!validarCampos()) {
            return;
        }

        Long clienteOrigenId = (parentBilletera != null) ? parentBilletera.getClienteId() : null;
        if (clienteOrigenId == null) {
            mostrarMensaje("No se pudo identificar tu cuenta. Vuelve a iniciar sesión.", true);
            return;
        }

        String celular = txtNumeroDestino.getText().trim();
        BigDecimal monto = new BigDecimal(txtMonto.getText().trim());

        // RF-21: se busca y se muestra el nombre enmascarado del destinatario antes de confirmar.
        var destinatario = buscarDestinatarioUseCase.ejecutar(new BuscarDestinatarioCommand(clienteOrigenId, celular));
        if (destinatario.isFailure()) {
            mostrarMensaje(destinatario.getError().get().message(), true);
            return;
        }

        String nombreDestinatario = destinatario.getValue().get().nombreEnmascarado();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format(Locale.US, "¿Deseas confirmar la transferencia de S/ %,.2f a %s (%s)?",
                        monto, nombreDestinatario, celular),
                "Confirmar Transferencia",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        var resultado = transferirMontoUseCase.ejecutar(new TransferirMontoCommand(clienteOrigenId, celular, monto));

        if (resultado.isFailure()) {
            mostrarMensaje(resultado.getError().get().message(), true);
            return;
        }

        var transferencia = resultado.getValue().get();

        Object[] opciones = {"Aceptar", "Descargar comprobante"};
        int opcion = JOptionPane.showOptionDialog(
                this,
                String.format(Locale.US, "¡Transferencia exitosa de S/ %,.2f a %s!\nTu nuevo saldo: S/ %,.2f",
                        transferencia.monto(), transferencia.destinatarioNombreEnmascarado(), transferencia.saldoResultante()),
                "Operación completada",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (opcion == 1) {
            descargarComprobante(transferencia.transaccionId(), clienteOrigenId);
        }

        if (parentBilletera != null) {
            parentBilletera.cargarBilletera();
        }

        volverALaBilletera();
    }

    /**
     * Pide el comprobante en PDF (GenerarComprobanteUseCase) y lo guarda donde el usuario elija.
     */
    private void descargarComprobante(Long transaccionId, Long clienteId) {
        var resultado = generarComprobanteUseCase.ejecutar(new GenerarComprobanteCommand(transaccionId, clienteId));

        if (resultado.isFailure()) {
            JOptionPane.showMessageDialog(this, resultado.getError().get().message(),
                    "No se pudo generar el comprobante", JOptionPane.ERROR_MESSAGE);
            return;
        }

        var selector = new JFileChooser();
        selector.setSelectedFile(new File("comprobante-" + transaccionId + ".pdf"));

        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (var out = new FileOutputStream(selector.getSelectedFile())) {
                out.write(resultado.getValue().get());
                JOptionPane.showMessageDialog(this, "Comprobante guardado correctamente.",
                        "Descarga completa", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Acción Cancelar / Volver
     */
    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {
        volverALaBilletera();
    }

    private void volverALaBilletera() {
        if (parentBilletera != null) {
            parentBilletera.setVisible(true);
        }
        this.dispose();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblSaldoDisponible = new javax.swing.JLabel();
        lblNumeroDestino = new javax.swing.JLabel();
        txtNumeroDestino = new javax.swing.JTextField();
        lblMonto = new javax.swing.JLabel();
        txtMonto = new javax.swing.JTextField();
        lblMensaje = new javax.swing.JLabel();
        btnTransferir = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Transferir dinero");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(460, 560));

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(228, 235, 231), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTitulo.setForeground(new java.awt.Color(2, 123, 113));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png")));
        lblTitulo.setText("Transferir Dinero");
        lblTitulo.setIconTextGap(8);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblSubtitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblSubtitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitulo.setText("Envía dinero rápido y seguro a otro número KAPIKUA");

        lblSaldoDisponible.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblSaldoDisponible.setForeground(new java.awt.Color(2, 123, 113));
        lblSaldoDisponible.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSaldoDisponible.setText("Saldo disponible: S/ 0.00");

        lblNumeroDestino.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblNumeroDestino.setForeground(new java.awt.Color(32, 49, 45));
        lblNumeroDestino.setText("Número de celular destino (9 dígitos):");

        txtNumeroDestino.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtNumeroDestino.setForeground(new java.awt.Color(32, 49, 45));
        txtNumeroDestino.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new java.awt.Color(184, 199, 192), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        lblMonto.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblMonto.setForeground(new java.awt.Color(32, 49, 45));
        lblMonto.setText("Monto a transferir (S/):");

        txtMonto.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtMonto.setForeground(new java.awt.Color(32, 49, 45));
        txtMonto.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new java.awt.Color(184, 199, 192), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        lblMensaje.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblMensaje.setForeground(new java.awt.Color(200, 60, 60));
        lblMensaje.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMensaje.setText(" ");

        btnTransferir.setBackground(new java.awt.Color(2, 123, 113));
        btnTransferir.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnTransferir.setForeground(new java.awt.Color(255, 255, 255));
        btnTransferir.setText("TRANSFERIR DINERO");
        btnTransferir.setBorderPainted(false);
        btnTransferir.setFocusPainted(false);
        btnTransferir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransferirActionPerformed(evt);
            }
        });

        btnCancelar.setBackground(new java.awt.Color(255, 255, 255));
        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnCancelar.setForeground(new java.awt.Color(32, 49, 45));
        btnCancelar.setText("Cancelar y volver");
        btnCancelar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlTarjetaLayout = new javax.swing.GroupLayout(pnlTarjeta);
        pnlTarjeta.setLayout(pnlTarjetaLayout);
        pnlTarjetaLayout.setHorizontalGroup(
            pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                    .addComponent(lblSaldoDisponible, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblNumeroDestino, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtNumeroDestino)
                    .addComponent(lblMonto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtMonto)
                    .addComponent(lblMensaje, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnTransferir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlTarjetaLayout.setVerticalGroup(
            pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(lblTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblSaldoDisponible)
                .addGap(20, 20, 20)
                .addComponent(lblNumeroDestino)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNumeroDestino, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblMonto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMonto, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblMensaje)
                .addGap(18, 18, 18)
                .addComponent(btnTransferir, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlFondoLayout = new javax.swing.GroupLayout(pnlFondo);
        pnlFondo.setLayout(pnlFondoLayout);
        pnlFondoLayout.setHorizontalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );
        pnlFondoLayout.setVerticalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlFondo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlFondo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new FrmTransferencia().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnTransferir;
    private javax.swing.JLabel lblMensaje;
    private javax.swing.JLabel lblMonto;
    private javax.swing.JLabel lblNumeroDestino;
    private javax.swing.JLabel lblSaldoDisponible;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JTextField txtMonto;
    private javax.swing.JTextField txtNumeroDestino;
    // End of variables declaration//GEN-END:variables
}
