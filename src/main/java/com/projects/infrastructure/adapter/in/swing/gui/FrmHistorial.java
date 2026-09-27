package com.projects.infrastructure.adapter.in.swing.gui;

import com.projects.application.dto.command.ConsultarMovimientosCommand;
import com.projects.application.dto.command.GenerarComprobanteCommand;
import com.projects.application.port.in.ConsultarMovimientosUseCase;
import com.projects.application.port.in.GenerarComprobanteUseCase;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * FrmHistorial - Ventana para visualizar el historial de transacciones del cliente.
 * Implementado según la Sección 6.5 del Plan de Implementación.
 * Contiene JTable (tblHistorial), filtros por tipo y retorno a FrmBilletera.
 */
public class FrmHistorial extends javax.swing.JFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FrmBilletera parentBilletera;
    private final ConsultarMovimientosUseCase consultarMovimientosUseCase;
    private final GenerarComprobanteUseCase generarComprobanteUseCase;
    private final List<Long> transaccionIdsPorFila = new ArrayList<>();
    private DefaultTableModel tableModel;

    public FrmHistorial() {
        this(null);
    }

    public FrmHistorial(FrmBilletera parentBilletera) {
        this(parentBilletera,
                CompositionRoot.crearConsultarMovimientosUseCase(),
                CompositionRoot.crearGenerarComprobanteUseCase());
    }

    public FrmHistorial(FrmBilletera parentBilletera, ConsultarMovimientosUseCase consultarMovimientosUseCase,
                         GenerarComprobanteUseCase generarComprobanteUseCase) {
        this.parentBilletera = parentBilletera;
        this.consultarMovimientosUseCase = consultarMovimientosUseCase;
        this.generarComprobanteUseCase = generarComprobanteUseCase;
        initComponents();
        configurarEstilos();
        inicializarTabla();
        UITheme.setupWindow(this, "KAPIKUA - Historial de Transacciones");
    }

    private void configurarEstilos() {
        getContentPane().setBackground(UITheme.BG_WHITE);
        pnlFondo.setBackground(UITheme.BG_WHITE);
        UITheme.styleCardPanel(pnlTarjeta);

        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png")));
        lblTitulo.setIconTextGap(8);
        lblTitulo.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitulo.setForeground(UITheme.VERDE_PROFUNDO);

        lblSubtitulo.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitulo.setForeground(UITheme.TEXT_SECONDARY);
        lblSubtitulo.setBorder(new javax.swing.border.EmptyBorder(0, 0, 8, 0) {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                int lineW = 60;
                int startX = x;
                int lineY = y + height - 3;
                g2.setColor(UITheme.AMARILLO_KAPIKUA);
                g2.fillRect(startX, lineY, lineW / 2, 3);
                g2.setColor(UITheme.VERDE_KAPIKUA);
                g2.fillRect(startX + lineW / 2, lineY, lineW / 2, 3);
                g2.dispose();
            }
        });

        lblFiltro.setFont(UITheme.FONT_LABEL);
        lblFiltro.setForeground(UITheme.TEXT_PRIMARY);

        UITheme.styleSecondaryButtonYellow(btnActualizar);
        UITheme.stylePrimaryButtonGreen(btnDescargarComprobante);
        UITheme.styleNeutralButton(btnVolver);
    }

    private void inicializarTabla() {
        String[] columnas = {"Código", "Fecha", "Tipo", "Saldo posterior", "Monto", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }
        };

        tblHistorial.setModel(tableModel);
        UITheme.styleTable(tblHistorial);

        // Alineación centrada para columnas clave y derecha para monto
        var centerRenderer = UITheme.createCenterRenderer();
        var rightRenderer = UITheme.createRightRenderer();
        tblHistorial.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblHistorial.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tblHistorial.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tblHistorial.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tblHistorial.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        tblHistorial.getColumnModel().getColumn(0).setPreferredWidth(85);
        tblHistorial.getColumnModel().getColumn(1).setPreferredWidth(130);
        tblHistorial.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblHistorial.getColumnModel().getColumn(3).setPreferredWidth(130);
        tblHistorial.getColumnModel().getColumn(4).setPreferredWidth(95);
        tblHistorial.getColumnModel().getColumn(5).setPreferredWidth(90);

        cargarMovimientos();
    }

    private void cargarMovimientos() {
        tableModel.setRowCount(0);
        transaccionIdsPorFila.clear();

        Long clienteId = (parentBilletera != null) ? parentBilletera.getClienteId() : null;
        if (clienteId == null) {
            return;
        }

        var resultado = consultarMovimientosUseCase.ejecutar(new ConsultarMovimientosCommand(clienteId));

        if (resultado.isFailure()) {
            JOptionPane.showMessageDialog(this, resultado.getError().get().message(),
                    "No se pudo cargar el historial", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String filtro = (String) cmbFiltroTipo.getSelectedItem();

        for (var movimiento : resultado.getValue().get()) {
            if (filtro != null && !"Todos".equals(filtro) && !movimiento.tipoCodigo().equals(filtro)) {
                continue;
            }

            String signoTexto = movimiento.signo() == '+' ? "+ " : "- ";

            tableModel.addRow(new Object[]{
                    "TX-" + movimiento.transaccionId(),
                    movimiento.fecha().atZoneSameInstant(ZoneId.systemDefault()).format(FORMATO_FECHA),
                    movimiento.tipoCodigo(),
                    String.format(Locale.US, "S/ %,.2f", movimiento.saldoPosterior()),
                    signoTexto + String.format(Locale.US, "S/ %,.2f", movimiento.monto()),
                    movimiento.estadoTransaccion()
            });
            transaccionIdsPorFila.add(movimiento.transaccionId());
        }
    }

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {
        cargarMovimientos();
    }

    private void btnDescargarComprobanteActionPerformed(java.awt.event.ActionEvent evt) {
        int fila = tblHistorial.getSelectedRow();
        if (fila < 0 || fila >= transaccionIdsPorFila.size()) {
            JOptionPane.showMessageDialog(this, "Selecciona un movimiento de la tabla.",
                    "Selecciona un movimiento", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long clienteId = (parentBilletera != null) ? parentBilletera.getClienteId() : null;
        if (clienteId == null) {
            return;
        }

        Long transaccionId = transaccionIdsPorFila.get(fila);
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

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {
        if (parentBilletera != null) {
            parentBilletera.setVisible(true);
        }
        this.dispose();
    }

    private void cmbFiltroTipoActionPerformed(java.awt.event.ActionEvent evt) {
        cargarMovimientos();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblFiltro = new javax.swing.JLabel();
        cmbFiltroTipo = new javax.swing.JComboBox<>();
        btnActualizar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHistorial = new javax.swing.JTable();
        btnDescargarComprobante = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Historial de Transacciones");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(720, 580));

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(228, 235, 231), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTitulo.setForeground(new java.awt.Color(2, 123, 113));
        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png")));
        lblTitulo.setText("Historial de Operaciones");
        lblTitulo.setIconTextGap(8);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblSubtitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblSubtitulo.setText("Revisa tus transferencias, pagos y recargas realizadas");

        lblFiltro.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblFiltro.setForeground(new java.awt.Color(32, 49, 45));
        lblFiltro.setText("Filtrar por tipo:");

        cmbFiltroTipo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cmbFiltroTipo.setForeground(new java.awt.Color(32, 49, 45));
        cmbFiltroTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "TRANSFERENCIA", "DEPOSITO", "RETIRO", "PAGO_CREDITO", "DESEMBOLSO_CREDITO" }));
        cmbFiltroTipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFiltroTipoActionPerformed(evt);
            }
        });

        btnActualizar.setBackground(new java.awt.Color(255, 224, 20));
        btnActualizar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnActualizar.setForeground(new java.awt.Color(32, 49, 45));
        btnActualizar.setText("Actualizar");
        btnActualizar.setBorderPainted(false);
        btnActualizar.setFocusPainted(false);
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        btnDescargarComprobante.setBackground(new java.awt.Color(2, 123, 113));
        btnDescargarComprobante.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnDescargarComprobante.setForeground(new java.awt.Color(255, 255, 255));
        btnDescargarComprobante.setText("Descargar comprobante");
        btnDescargarComprobante.setBorderPainted(false);
        btnDescargarComprobante.setFocusPainted(false);
        btnDescargarComprobante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDescargarComprobanteActionPerformed(evt);
            }
        });

        tblHistorial.setFont(new java.awt.Font("Segoe UI", 0, 12));
        tblHistorial.setGridColor(new java.awt.Color(228, 235, 231));
        tblHistorial.setRowHeight(28);
        tblHistorial.setSelectionBackground(new java.awt.Color(232, 249, 240));
        tblHistorial.setSelectionForeground(new java.awt.Color(32, 49, 45));
        tblHistorial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Fecha", "Tipo", "Saldo posterior", "Monto", "Estado"
            }
        ));
        jScrollPane1.setViewportView(tblHistorial);

        btnVolver.setBackground(new java.awt.Color(255, 255, 255));
        btnVolver.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnVolver.setForeground(new java.awt.Color(32, 49, 45));
        btnVolver.setText("Volver a mi billetera");
        btnVolver.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolverActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlTarjetaLayout = new javax.swing.GroupLayout(pnlTarjeta);
        pnlTarjeta.setLayout(pnlTarjetaLayout);
        pnlTarjetaLayout.setHorizontalGroup(
            pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSubtitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblFiltro)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbFiltroTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnActualizar))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addComponent(btnDescargarComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnVolver, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlTarjetaLayout.setVerticalGroup(
            pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addComponent(lblTitulo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSubtitulo))
                    .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFiltro)
                        .addComponent(cmbFiltroTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDescargarComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlFondoLayout = new javax.swing.GroupLayout(pnlFondo);
        pnlFondo.setLayout(pnlFondoLayout);
        pnlFondoLayout.setHorizontalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlFondoLayout.setVerticalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
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
            new FrmHistorial().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnDescargarComprobante;
    private javax.swing.JButton btnVolver;
    private javax.swing.JComboBox<String> cmbFiltroTipo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFiltro;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JTable tblHistorial;
    // End of variables declaration//GEN-END:variables
}
