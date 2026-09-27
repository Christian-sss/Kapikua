package com.projects.infrastructure.adapter.in.swing.gui;

import com.projects.application.dto.command.ConsultarEstadisticasCommand;
import com.projects.application.dto.command.PeriodoEstadistica;
import com.projects.application.dto.response.TransaccionAdminResponse;
import com.projects.application.port.in.ConsultarEstadisticasUseCase;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * FrmTransaccionesAdmin - Vista administrativa de consulta y auditoría de transacciones globales (solo ADMIN).
 * Muestra las últimas transacciones (ConsultarEstadisticasUseCase) con filtros por tipo, estado y búsqueda.
 */
public class FrmTransaccionesAdmin extends javax.swing.JFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FrmAdministrador parentAdmin;
    private final ConsultarEstadisticasUseCase consultarEstadisticasUseCase;
    private List<TransaccionAdminResponse> transacciones = List.of();
    private DefaultTableModel tableModel;

    public FrmTransaccionesAdmin() {
        this(null);
    }

    public FrmTransaccionesAdmin(FrmAdministrador parentAdmin) {
        this(parentAdmin, CompositionRoot.crearConsultarEstadisticasUseCase());
    }

    public FrmTransaccionesAdmin(FrmAdministrador parentAdmin, ConsultarEstadisticasUseCase consultarEstadisticasUseCase) {
        this.parentAdmin = parentAdmin;
        this.consultarEstadisticasUseCase = consultarEstadisticasUseCase;
        initComponents();
        configurarEstilos();
        inicializarTabla();
        UITheme.setupWindow(this, "KAPIKUA - Auditoría de Transacciones");
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

        lblFiltroTipo.setFont(UITheme.FONT_LABEL);
        lblFiltroTipo.setForeground(UITheme.TEXT_PRIMARY);
        lblFiltroEstado.setFont(UITheme.FONT_LABEL);
        lblFiltroEstado.setForeground(UITheme.TEXT_PRIMARY);
        lblBuscar.setFont(UITheme.FONT_LABEL);
        lblBuscar.setForeground(UITheme.TEXT_PRIMARY);

        UITheme.styleTextField(txtBuscar);
        UITheme.stylePrimaryButtonGreen(btnFiltrar);
        UITheme.styleSecondaryButtonYellow(btnLimpiar);
        UITheme.styleNeutralButton(btnVolver);
    }

    private void inicializarTabla() {
        String[] columnas = {"ID", "Fecha", "Origen", "Destino", "Tipo", "Monto", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTransacciones.setModel(tableModel);
        UITheme.styleTable(tblTransacciones);

        var centerRenderer = UITheme.createCenterRenderer();
        var rightRenderer = UITheme.createRightRenderer();
        tblTransacciones.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblTransacciones.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tblTransacciones.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblTransacciones.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tblTransacciones.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        tblTransacciones.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblTransacciones.getColumnModel().getColumn(1).setPreferredWidth(130);
        tblTransacciones.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblTransacciones.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblTransacciones.getColumnModel().getColumn(4).setPreferredWidth(110);
        tblTransacciones.getColumnModel().getColumn(5).setPreferredWidth(100);
        tblTransacciones.getColumnModel().getColumn(6).setPreferredWidth(90);

        cargarTransacciones();
    }

    private void cargarTransacciones() {
        var resultado = consultarEstadisticasUseCase.ejecutar(new ConsultarEstadisticasCommand(PeriodoEstadistica.DIA));

        if (resultado.isFailure()) {
            transacciones = List.of();
            aplicarFiltros();
            JOptionPane.showMessageDialog(this, resultado.getError().get().message(),
                    "No se pudieron cargar las transacciones", JOptionPane.ERROR_MESSAGE);
            return;
        }

        transacciones = resultado.getValue().get().transacciones();
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        tableModel.setRowCount(0);

        String tipoFiltro = (String) cmbFiltroTipo.getSelectedItem();
        String estadoFiltro = (String) cmbFiltroEstado.getSelectedItem();
        String busqueda = txtBuscar.getText().trim().toLowerCase();

        for (var transaccion : transacciones) {
            String id = "TX-" + transaccion.transaccionId();
            String origen = transaccion.origen();
            String destino = transaccion.destino();
            String tipo = transaccion.tipo();
            String estado = transaccion.estado();

            boolean coincideTipo = (tipoFiltro == null || "Todos".equals(tipoFiltro) || tipo.equalsIgnoreCase(tipoFiltro));
            boolean coincideEstado = (estadoFiltro == null || "Todos".equals(estadoFiltro) || estado.equalsIgnoreCase(estadoFiltro));
            boolean coincideBusqueda = busqueda.isEmpty() || id.toLowerCase().contains(busqueda) || origen.toLowerCase().contains(busqueda) || destino.toLowerCase().contains(busqueda);

            if (coincideTipo && coincideEstado && coincideBusqueda) {
                tableModel.addRow(new Object[]{
                        id,
                        transaccion.fecha().atZoneSameInstant(ZoneId.systemDefault()).format(FORMATO_FECHA),
                        origen,
                        destino,
                        tipo,
                        String.format(Locale.US, "S/ %,.2f", transaccion.monto()),
                        estado
                });
            }
        }
    }

    private void btnFiltrarActionPerformed(java.awt.event.ActionEvent evt) {
        aplicarFiltros();
    }

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {
        txtBuscar.setText("");
        cmbFiltroTipo.setSelectedIndex(0);
        cmbFiltroEstado.setSelectedIndex(0);
        aplicarFiltros();
    }

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {
        if (parentAdmin != null) {
            parentAdmin.setVisible(true);
        } else {
            new FrmAdministrador().setVisible(true);
        }
        this.dispose();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblFiltroTipo = new javax.swing.JLabel();
        cmbFiltroTipo = new javax.swing.JComboBox<>();
        lblFiltroEstado = new javax.swing.JLabel();
        cmbFiltroEstado = new javax.swing.JComboBox<>();
        lblBuscar = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        btnFiltrar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblTransacciones = new javax.swing.JTable();
        btnVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Auditoría de Transacciones");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(980, 640));

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(2, 123, 113));
        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png"))); // NOI18N
        lblTitulo.setText("Registro Global de Transacciones");
        lblTitulo.setIconTextGap(8);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblSubtitulo.setText("Auditoría general de operaciones de la red KAPIKUA");

        lblFiltroTipo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblFiltroTipo.setForeground(new java.awt.Color(32, 49, 45));
        lblFiltroTipo.setText("Tipo:");

        cmbFiltroTipo.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        cmbFiltroTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "TRANSFERENCIA", "DEPOSITO", "RETIRO", "PAGO_CREDITO", "DESEMBOLSO_CREDITO" }));

        lblFiltroEstado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblFiltroEstado.setForeground(new java.awt.Color(32, 49, 45));
        lblFiltroEstado.setText("Estado:");

        cmbFiltroEstado.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        cmbFiltroEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "EXITOSA", "FALLIDA" }));

        lblBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblBuscar.setForeground(new java.awt.Color(32, 49, 45));
        lblBuscar.setText("Buscar:");

        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBuscar.setForeground(new java.awt.Color(32, 49, 45));
        txtBuscar.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(184, 199, 192), 1, true), javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        btnFiltrar.setBackground(new java.awt.Color(2, 123, 113));
        btnFiltrar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnFiltrar.setForeground(new java.awt.Color(255, 255, 255));
        btnFiltrar.setText("Filtrar");
        btnFiltrar.setBorderPainted(false);
        btnFiltrar.setFocusPainted(false);
        btnFiltrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFiltrarActionPerformed(evt);
            }
        });

        btnLimpiar.setBackground(new java.awt.Color(255, 224, 20));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(32, 49, 45));
        btnLimpiar.setText("Restablecer");
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        tblTransacciones.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        tblTransacciones.setGridColor(new java.awt.Color(228, 235, 231));
        tblTransacciones.setRowHeight(28);
        tblTransacciones.setSelectionBackground(new java.awt.Color(232, 249, 240));
        tblTransacciones.setSelectionForeground(new java.awt.Color(32, 49, 45));
        tblTransacciones.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Fecha", "Origen", "Destino", "Tipo", "Monto", "Estado"
            }
        ));
        jScrollPane1.setViewportView(tblTransacciones);

        btnVolver.setBackground(new java.awt.Color(255, 255, 255));
        btnVolver.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnVolver.setForeground(new java.awt.Color(32, 49, 45));
        btnVolver.setText("Volver al panel principal");
        btnVolver.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)));
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
                            .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSubtitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFiltroTipo)
                            .addComponent(cmbFiltroTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFiltroEstado)
                            .addComponent(cmbFiltroEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblBuscar)
                            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                                .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnFiltrar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnLimpiar))))
                    .addComponent(jScrollPane1)
                    .addComponent(btnVolver, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFiltroTipo)
                            .addComponent(lblFiltroEstado)
                            .addComponent(lblBuscar))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbFiltroTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbFiltroEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnFiltrar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            new FrmTransaccionesAdmin().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFiltrar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JComboBox<String> cmbFiltroEstado;
    private javax.swing.JComboBox<String> cmbFiltroTipo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblFiltroEstado;
    private javax.swing.JLabel lblFiltroTipo;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JTable tblTransacciones;
    private javax.swing.JTextField txtBuscar;
    // End of variables declaration//GEN-END:variables
}
